package com.posplatform.pos_app

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DeviceSecurityChannel(
    private val applicationContext: Context,
) : MethodChannel.MethodCallHandler {
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "loadOrCreateEnrollmentIdentity" -> {
                runSafely(result) {
                    loadOrCreateEnrollmentIdentity()
                }
            }

            "loadOrCreateDatabaseKeyState" -> {
                runSafely(result) {
                    loadOrCreateDatabaseKeyState()
                }
            }

            "beginDatabaseKeyRotation" -> {
                runSafely(result) {
                    val reason = call.argument<String>("reason")
                        ?: throw IllegalArgumentException("Rotation reason is required.")
                    val requestedAtMs = call.argument<Number>("requested_at_ms")?.toLong()

                    beginDatabaseKeyRotation(reason, requestedAtMs)
                }
            }

            "commitDatabaseKeyRotation" -> {
                runSafely(result) {
                    commitDatabaseKeyRotation()
                }
            }

            "cancelDatabaseKeyRotation" -> {
                runSafely(result) {
                    cancelDatabaseKeyRotation()
                }
            }

            else -> result.notImplemented()
        }
    }

    private fun runSafely(
        result: MethodChannel.Result,
        action: () -> Map<String, Any>,
    ) {
        try {
            result.success(action())
        } catch (exception: Exception) {
            result.error(
                "DEVICE_SECURITY_ERROR",
                exception.message ?: "Unable to resolve Android device security state.",
                null,
            )
        }
    }

    private fun loadOrCreateEnrollmentIdentity(): Map<String, Any> {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            "Android 7.0 or newer is required for secure POS enrollment."
        }

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val challenge = loadOrCreateAttestationChallenge()

        if (!keyStore.containsAlias(IDENTITY_KEY_ALIAS)) {
            generateIdentityKey(IDENTITY_KEY_ALIAS, challenge)
        }

        val entry = keyStore.getEntry(IDENTITY_KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
            ?: throw IllegalStateException("Android keystore entry is unavailable.")
        val certificate = entry.certificate as? X509Certificate
            ?: throw IllegalStateException("Leaf attestation certificate is unavailable.")
        val certificateChain = keyStore.getCertificateChain(IDENTITY_KEY_ALIAS)
            ?.mapNotNull { it as? X509Certificate }
            ?.map { encodeBase64(it.encoded) }
            ?: throw IllegalStateException("Attestation certificate chain is unavailable.")

        val publicKeyBytes = certificate.publicKey.encoded
        val publicKey = encodeBase64(publicKeyBytes)
        val androidId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: ""

        return mapOf(
            "public_key" to publicKey,
            "device_fingerprint" to digestHex(
                publicKey +
                    "|" +
                    androidId +
                    "|" +
                    applicationContext.packageName,
            ),
            "attestation" to mapOf(
                "provider" to "android_keystore",
                "platform" to "android",
                "package_name" to applicationContext.packageName,
                "key_alias" to IDENTITY_KEY_ALIAS,
                "key_algorithm" to certificate.publicKey.algorithm,
                "hardware_backed" to isHardwareBacked(entry),
                "certificate_chain" to certificateChain,
                "attestation_challenge" to encodeBase64(challenge),
            ),
        )
    }

    private fun loadOrCreateDatabaseKeyState(): Map<String, Any> {
        val stored = loadStoredDatabaseState() ?: createStoredDatabaseState(
            version = 1,
            createdAtMs = System.currentTimeMillis(),
        ).also(::saveStoredDatabaseState)

        return storedStateToFlutterMap(stored)
    }

    private fun beginDatabaseKeyRotation(
        reason: String,
        requestedAtMs: Long?,
    ): Map<String, Any> {
        val current = loadStoredDatabaseState() ?: createStoredDatabaseState(
            version = 1,
            createdAtMs = System.currentTimeMillis(),
        )

        if (current.pending != null) {
            return storedStateToFlutterMap(current)
        }

        val rotated = StoredDatabaseKeyState(
            active = current.active,
            pending = wrapDatabaseMaterial(
                version = current.active.version + 1,
                createdAtMs = requestedAtMs ?: System.currentTimeMillis(),
                reason = reason,
            ),
        )
        saveStoredDatabaseState(rotated)

        return storedStateToFlutterMap(rotated)
    }

    private fun commitDatabaseKeyRotation(): Map<String, Any> {
        val current = loadStoredDatabaseState() ?: createStoredDatabaseState(
            version = 1,
            createdAtMs = System.currentTimeMillis(),
        )
        val pending = current.pending ?: return storedStateToFlutterMap(current)
        val committed = StoredDatabaseKeyState(active = pending)
        saveStoredDatabaseState(committed)

        return storedStateToFlutterMap(committed)
    }

    private fun cancelDatabaseKeyRotation(): Map<String, Any> {
        val current = loadStoredDatabaseState() ?: createStoredDatabaseState(
            version = 1,
            createdAtMs = System.currentTimeMillis(),
        )
        val cancelled = StoredDatabaseKeyState(active = current.active)
        saveStoredDatabaseState(cancelled)

        return storedStateToFlutterMap(cancelled)
    }

    private fun createStoredDatabaseState(
        version: Int,
        createdAtMs: Long,
    ): StoredDatabaseKeyState {
        return StoredDatabaseKeyState(
            active = wrapDatabaseMaterial(
                version = version,
                createdAtMs = createdAtMs,
                reason = null,
            ),
        )
    }

    private fun wrapDatabaseMaterial(
        version: Int,
        createdAtMs: Long,
        reason: String?,
    ): StoredKeyEnvelope {
        val rawKey = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encodedKey = encodeBase64(rawKey)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, loadOrCreateDatabaseWrappingKey())
        val ciphertext = cipher.doFinal(encodedKey.toByteArray(StandardCharsets.UTF_8))

        return StoredKeyEnvelope(
            ciphertext = encodeBase64(ciphertext),
            iv = encodeBase64(cipher.iv),
            version = version,
            createdAtMs = createdAtMs,
            reason = reason,
        )
    }

    private fun decryptDatabaseKey(envelope: StoredKeyEnvelope): String {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        val iv = Base64.decode(envelope.iv, Base64.NO_WRAP)
        cipher.init(
            Cipher.DECRYPT_MODE,
            loadExistingDatabaseWrappingKey(),
            GCMParameterSpec(128, iv),
        )
        val decrypted = cipher.doFinal(Base64.decode(envelope.ciphertext, Base64.NO_WRAP))

        return String(decrypted, StandardCharsets.UTF_8)
    }

    private fun loadOrCreateDatabaseWrappingKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if (keyStore.containsAlias(DATABASE_WRAP_KEY_ALIAS)) {
            return loadExistingDatabaseWrappingKey()
        }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE,
        )
        val baseBuilder = KeyGenParameterSpec.Builder(
            DATABASE_WRAP_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)

        val parameters = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                baseBuilder.setIsStrongBoxBacked(true).build()
            } else {
                baseBuilder.build()
            }
        } catch (_: Exception) {
            baseBuilder.build()
        }

        generator.init(parameters)

        return generator.generateKey()
    }

    private fun loadExistingDatabaseWrappingKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(DATABASE_WRAP_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            ?: throw IllegalStateException(
                "Local database key is unavailable because the Android wrapping key is missing.",
            )

        return entry.secretKey
    }

    private fun loadStoredDatabaseState(): StoredDatabaseKeyState? {
        val raw = databasePreferences().getString(DATABASE_KEY_STATE_KEY, null)

        if (raw.isNullOrBlank()) {
            return null
        }

        return StoredDatabaseKeyState.fromJson(JSONObject(raw))
    }

    private fun saveStoredDatabaseState(state: StoredDatabaseKeyState) {
        databasePreferences()
            .edit()
            .putString(DATABASE_KEY_STATE_KEY, state.toJson().toString())
            .apply()
    }

    private fun databasePreferences() = applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private fun generateIdentityKey(alias: String, challenge: ByteArray) {
        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE,
        )

        val baseBuilder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
        ).setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setUserAuthenticationRequired(false)
            .setAttestationChallenge(challenge)

        val parameters = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                baseBuilder.setIsStrongBoxBacked(true).build()
            } else {
                baseBuilder.build()
            }
        } catch (_: Exception) {
            baseBuilder.build()
        }

        generator.initialize(parameters)
        generator.generateKeyPair()
    }

    private fun loadOrCreateAttestationChallenge(): ByteArray {
        val existing = databasePreferences().getString(ATTESTATION_CHALLENGE_KEY, null)

        if (!existing.isNullOrBlank()) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }

        val challenge = ByteArray(32).also { SecureRandom().nextBytes(it) }
        databasePreferences()
            .edit()
            .putString(ATTESTATION_CHALLENGE_KEY, encodeBase64(challenge))
            .apply()

        return challenge
    }

    private fun isHardwareBacked(entry: KeyStore.PrivateKeyEntry): Boolean {
        return try {
            val privateKey = entry.privateKey
            val keyFactory = KeyFactory.getInstance(privateKey.algorithm, ANDROID_KEYSTORE)
            val keyInfo = keyFactory.getKeySpec(privateKey, KeyInfo::class.java)

            keyInfo.isInsideSecureHardware
        } catch (_: Exception) {
            false
        }
    }

    private fun encodeBase64(value: ByteArray): String =
        Base64.encodeToString(value, Base64.NO_WRAP)

    private fun digestHex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))

        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun storedStateToFlutterMap(state: StoredDatabaseKeyState): Map<String, Any> {
        return buildMap {
            put("active", storedEnvelopeToFlutterMap(state.active))
            if (state.pending != null) {
                put("pending", storedEnvelopeToFlutterMap(state.pending))
            }
        }
    }

    private fun storedEnvelopeToFlutterMap(envelope: StoredKeyEnvelope): Map<String, Any> {
        return buildMap {
            put("key", decryptDatabaseKey(envelope))
            put("version", envelope.version)
            put("created_at_ms", envelope.createdAtMs)
            if (!envelope.reason.isNullOrBlank()) {
                put("reason", envelope.reason)
            }
        }
    }

    private data class StoredDatabaseKeyState(
        val active: StoredKeyEnvelope,
        val pending: StoredKeyEnvelope? = null,
    ) {
        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("active", active.toJson())
                if (pending != null) {
                    put("pending", pending.toJson())
                }
            }
        }

        companion object {
            fun fromJson(json: JSONObject): StoredDatabaseKeyState {
                return StoredDatabaseKeyState(
                    active = StoredKeyEnvelope.fromJson(json.getJSONObject("active")),
                    pending = if (json.has("pending")) {
                        StoredKeyEnvelope.fromJson(json.getJSONObject("pending"))
                    } else {
                        null
                    },
                )
            }
        }
    }

    private data class StoredKeyEnvelope(
        val ciphertext: String,
        val iv: String,
        val version: Int,
        val createdAtMs: Long,
        val reason: String?,
    ) {
        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("ciphertext", ciphertext)
                put("iv", iv)
                put("version", version)
                put("created_at_ms", createdAtMs)
                if (!reason.isNullOrBlank()) {
                    put("reason", reason)
                }
            }
        }

        companion object {
            fun fromJson(json: JSONObject): StoredKeyEnvelope {
                return StoredKeyEnvelope(
                    ciphertext = json.getString("ciphertext"),
                    iv = json.getString("iv"),
                    version = json.getInt("version"),
                    createdAtMs = json.getLong("created_at_ms"),
                    reason = if (json.has("reason")) json.getString("reason") else null,
                )
            }
        }
    }

    companion object {
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val IDENTITY_KEY_ALIAS = "pos_device_identity_v2"
        private const val DATABASE_WRAP_KEY_ALIAS = "pos_sqlite_wrap_key_v1"
        private const val PREFERENCES_NAME = "pos_device_security"
        private const val ATTESTATION_CHALLENGE_KEY = "attestation_challenge_v1"
        private const val DATABASE_KEY_STATE_KEY = "database_key_state_v1"
    }
}
