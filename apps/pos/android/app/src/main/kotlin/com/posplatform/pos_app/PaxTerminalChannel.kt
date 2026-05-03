package com.posplatform.pos_app

import android.content.Context
import android.os.Build
import com.pax.poslink.CommSetting
import com.pax.poslink.PaymentRequest
import com.pax.poslink.PaymentResponse
import com.pax.poslink.PosLink
import com.pax.poslink.ProcessTransResult
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.zip.CRC32

class PaxTerminalChannel(
    private val applicationContext: Context,
) : MethodChannel.MethodCallHandler {
    private val terminalExecutor = Executors.newSingleThreadExecutor()

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "checkoutCard" -> {
                runSafely(result) {
                    checkoutCard(call)
                }
            }

            else -> result.notImplemented()
        }
    }

    private fun runSafely(
        result: MethodChannel.Result,
        action: () -> Map<String, Any?>,
    ) {
        try {
            result.success(action())
        } catch (exception: Exception) {
            result.error(
                "PAX_TERMINAL_ERROR",
                exception.message ?: "PAX terminal checkout failed.",
                null,
            )
        }
    }

    private fun checkoutCard(call: MethodCall): Map<String, Any?> {
        val orderId = call.argument<String>("order_id")
            ?: throw IllegalArgumentException("order_id is required.")
        val amountMinor = call.argument<Number>("amount_minor")?.toLong()
            ?: throw IllegalArgumentException("amount_minor is required.")
        val tipMinor = call.argument<Number>("tip_minor")?.toLong() ?: 0L
        val terminalReference = call.argument<String>("terminal_reference")
            ?: "PAX-${Build.MODEL.take(8).uppercase(Locale.US)}"
        val totalMinor = (amountMinor + tipMinor).coerceAtLeast(1L)
        val execution = executeSale(
            orderId = orderId,
            totalMinor = totalMinor,
            ecrRefNum = buildEcrRefNum(orderId, amountMinor, tipMinor),
        )
        val extData = execution.response?.ExtData
        val providerTransactionId = firstNotBlank(
            execution.response?.RefNum,
            execution.response?.HostCode,
        )
        val terminalTimestamp = terminalTimestampToIso(
            execution.response?.Timestamp,
            execution.completedAtIso,
        )
        val aid = extractExtDataValue(extData, "AID")
        val tvr = extractExtDataValue(extData, "TVR")
        val tsi = extractExtDataValue(extData, "TSI")
        val appLabel = firstNotBlank(
            extractExtDataValue(extData, "APPLAB"),
            execution.response?.CardType,
        )
        val entryModeCode = extractExtDataValue(extData, "PLEntryMode")
        val entryMode = normalizeEntryMode(entryModeCode)
        val resultCode = execution.response?.ResultCode?.trim().orEmpty()
        val resultTxt = execution.response?.ResultTxt?.trim().orEmpty()
        val ptrCode = execution.processCode
        val ptrMessage = execution.processMessage.trim()
        val transportCode = normalizeCode(ptrCode ?: "unknown")

        if (ptrCode == ProcessTransResult.ProcessTransResultCode.TimeOut.name) {
            return basePayload(
                status = "in_doubt",
                providerTransactionId = providerTransactionId,
                authCode = execution.response?.AuthCode,
                maskedPan = execution.response?.BogusAccountNum,
                terminalReference = terminalReference,
                entryMode = entryMode,
                applicationLabel = appLabel,
                aid = aid,
                tvr = tvr,
                tsi = tsi,
                terminalStatusCode = "timeout",
                terminalResultCode = "no_response",
                terminalTimestamp = terminalTimestamp,
                message = "Terminal timeout/no response. Recovery inquiry is required.",
            )
        }

        if (ptrCode != ProcessTransResult.ProcessTransResultCode.OK.name) {
            val uncertainTransport = isInDoubtTransportFailure(ptrMessage)

            return basePayload(
                status = if (uncertainTransport) "in_doubt" else "declined",
                providerTransactionId = providerTransactionId,
                authCode = execution.response?.AuthCode,
                maskedPan = execution.response?.BogusAccountNum,
                terminalReference = terminalReference,
                entryMode = entryMode,
                applicationLabel = appLabel,
                aid = aid,
                tvr = tvr,
                tsi = tsi,
                terminalStatusCode = if (uncertainTransport) "transport_uncertain" else "transport_error",
                terminalResultCode = transportCode,
                terminalTimestamp = terminalTimestamp,
                message = if (uncertainTransport) {
                    "Terminal transport result is uncertain. Recovery inquiry is required."
                } else {
                    ptrMessage.ifBlank { "Card terminal declined the transaction." }
                },
            )
        }

        if (resultCode == "000000") {
            if (providerTransactionId.isBlank()) {
                return basePayload(
                    status = "in_doubt",
                    providerTransactionId = providerTransactionId,
                    authCode = execution.response?.AuthCode,
                    maskedPan = execution.response?.BogusAccountNum,
                    terminalReference = terminalReference,
                    entryMode = entryMode,
                    applicationLabel = appLabel,
                    aid = aid,
                    tvr = tvr,
                    tsi = tsi,
                    terminalStatusCode = "approved_missing_reference",
                    terminalResultCode = "unknown",
                    terminalTimestamp = terminalTimestamp,
                    message = "Terminal approved without transaction reference. Recovery inquiry is required.",
                )
            }

            return basePayload(
                status = "approved",
                providerTransactionId = providerTransactionId,
                authCode = execution.response?.AuthCode,
                maskedPan = execution.response?.BogusAccountNum,
                terminalReference = terminalReference,
                entryMode = entryMode,
                applicationLabel = appLabel,
                aid = aid,
                tvr = tvr,
                tsi = tsi,
                terminalStatusCode = "approved",
                terminalResultCode = "00",
                terminalTimestamp = terminalTimestamp,
                message = resultTxt.ifBlank { "Terminal approved." },
            )
        }

        return basePayload(
            status = "declined",
            providerTransactionId = providerTransactionId,
            authCode = execution.response?.AuthCode,
            maskedPan = execution.response?.BogusAccountNum,
            terminalReference = terminalReference,
            entryMode = entryMode,
            applicationLabel = appLabel,
            aid = aid,
            tvr = tvr,
            tsi = tsi,
            terminalStatusCode = "declined",
            terminalResultCode = normalizeCode(resultCode.ifBlank { "declined" }),
            terminalTimestamp = terminalTimestamp,
            message = firstNotBlank(resultTxt, execution.response?.Message, "Transaction declined by terminal."),
        )
    }

    private fun executeSale(
        orderId: String,
        totalMinor: Long,
        ecrRefNum: String,
    ): TerminalExecution {
        val future = terminalExecutor.submit<TerminalExecution> {
            val posLink = PosLink(applicationContext)
            posLink.SetCommSetting(defaultCommSetting())
            posLink.PaymentRequest = PaymentRequest().apply {
                TenderType = ParseTenderType("CREDIT")
                TransType = ParseTransType("SALE")
                Amount = totalMinor.toString()
                TipAmt = "0"
                ECRRefNum = ecrRefNum
                InvNum = orderId.take(16)
                ClerkID = "POS"
            }

            val process = posLink.ProcessTrans()
            val response = posLink.PaymentResponse

            TerminalExecution(
                processCode = process.Code?.name,
                processMessage = process.Msg ?: "",
                response = response,
                completedAtIso = isoNow(),
            )
        }

        return try {
            future.get(70, TimeUnit.SECONDS)
        } catch (_: TimeoutException) {
            future.cancel(true)
            TerminalExecution(
                processCode = ProcessTransResult.ProcessTransResultCode.TimeOut.name,
                processMessage = "POSLink ProcessTrans timeout.",
                response = null,
                completedAtIso = isoNow(),
            )
        } catch (exception: Exception) {
            TerminalExecution(
                processCode = ProcessTransResult.ProcessTransResultCode.ERROR.name,
                processMessage = exception.message ?: "POSLink ProcessTrans error.",
                response = null,
                completedAtIso = isoNow(),
            )
        }
    }

    private fun defaultCommSetting(): CommSetting {
        return CommSetting().apply {
            setType(CommSetting.AIDL)
            setTimeOut("60000")
        }
    }

    private fun basePayload(
        status: String,
        providerTransactionId: String,
        authCode: String?,
        maskedPan: String?,
        terminalReference: String,
        entryMode: String,
        applicationLabel: String?,
        aid: String?,
        tvr: String?,
        tsi: String?,
        terminalStatusCode: String,
        terminalResultCode: String,
        terminalTimestamp: String,
        message: String,
    ): Map<String, Any?> {
        return mapOf(
            "status" to status,
            "provider_key" to "fiserv_bluepay",
            "provider_transaction_id" to providerTransactionId,
            "auth_code" to (authCode ?: ""),
            "masked_pan" to (maskedPan ?: ""),
            "terminal_id" to terminalReference,
            "entry_mode" to entryMode,
            "application_label" to applicationLabel,
            "aid" to aid,
            "tvr" to tvr,
            "tsi" to tsi,
            "terminal_status_code" to terminalStatusCode,
            "terminal_result_code" to terminalResultCode,
            "terminal_timestamp" to terminalTimestamp,
            "message" to message,
            "terminal_reference" to terminalReference,
        )
    }

    private fun buildEcrRefNum(
        orderId: String,
        amountMinor: Long,
        tipMinor: Long,
    ): String {
        val payload = "$orderId|$amountMinor|$tipMinor"
        val checksum = CRC32()
        checksum.update(payload.toByteArray(StandardCharsets.UTF_8))
        val numeric = checksum.value.toString().takeLast(10).padStart(10, '0')

        return "ECR$numeric"
    }

    private fun terminalTimestampToIso(
        terminalTimestamp: String?,
        fallbackIso: String,
    ): String {
        val value = terminalTimestamp?.trim().orEmpty()
        if (value.isEmpty()) {
            return fallbackIso
        }

        return try {
            val parser = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            val parsedDate = parser.parse(value)
            if (parsedDate != null) {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                formatter.format(parsedDate)
            } else {
                fallbackIso
            }
        } catch (_: Exception) {
            fallbackIso
        }
    }

    private fun extractExtDataValue(
        extData: String?,
        tag: String,
    ): String? {
        val payload = extData?.trim().orEmpty()
        if (payload.isEmpty()) {
            return null
        }

        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.IGNORE_CASE)
        val value = regex.find(payload)?.groupValues?.getOrNull(1)?.trim()

        return if (value.isNullOrEmpty()) null else value
    }

    private fun normalizeEntryMode(rawEntryMode: String?): String {
        return when (rawEntryMode?.trim()) {
            "0" -> "manual"
            "1" -> "swipe"
            "2" -> "contactless"
            "3" -> "scanner"
            "4" -> "chip"
            "5" -> "chip_fallback_swipe"
            else -> "unknown"
        }
    }

    private fun isInDoubtTransportFailure(message: String): Boolean {
        val normalized = message.lowercase(Locale.US)

        return normalized.contains("timeout") ||
            normalized.contains("time out") ||
            normalized.contains("recv") ||
            normalized.contains("socket") ||
            normalized.contains("proxy") ||
            normalized.contains("session") ||
            normalized.contains("connect") ||
            normalized.contains("ssl")
    }

    private fun normalizeCode(value: String): String {
        return value.trim()
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifEmpty { "unknown" }
    }

    private fun firstNotBlank(vararg values: String?): String {
        for (value in values) {
            val normalized = value?.trim().orEmpty()
            if (normalized.isNotEmpty()) {
                return normalized
            }
        }

        return ""
    }

    private fun isoNow(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")

        return formatter.format(Date())
    }

    private data class TerminalExecution(
        val processCode: String?,
        val processMessage: String,
        val response: PaymentResponse?,
        val completedAtIso: String,
    )
}
