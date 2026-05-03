import 'package:flutter/services.dart';

enum TerminalCheckoutStatus { approved, declined, inDoubt }

class TerminalCheckoutResult {
  const TerminalCheckoutResult({
    required this.status,
    required this.providerKey,
    required this.providerTransactionId,
    required this.authCode,
    required this.maskedPan,
    required this.terminalId,
    required this.entryMode,
    required this.applicationLabel,
    required this.aid,
    required this.tvr,
    required this.tsi,
    required this.terminalStatusCode,
    required this.terminalResultCode,
    required this.terminalTimestamp,
    this.message,
    this.terminalReference,
  });

  final TerminalCheckoutStatus status;
  final String providerKey;
  final String providerTransactionId;
  final String authCode;
  final String maskedPan;
  final String terminalId;
  final String entryMode;
  final String? applicationLabel;
  final String? aid;
  final String? tvr;
  final String? tsi;
  final String terminalStatusCode;
  final String terminalResultCode;
  final DateTime terminalTimestamp;
  final String? message;
  final String? terminalReference;

  bool get isApproved => status == TerminalCheckoutStatus.approved;

  bool get isInDoubt => status == TerminalCheckoutStatus.inDoubt;
}

abstract interface class CardTerminalGateway {
  Future<TerminalCheckoutResult> checkoutCard({
    required String orderId,
    required int amountMinor,
    required int tipMinor,
    String? terminalReference,
  });
}

class MethodChannelPaxTerminalGateway implements CardTerminalGateway {
  MethodChannelPaxTerminalGateway([MethodChannel? channel])
    : _channel = channel ?? const MethodChannel('pos_app/pax_terminal');

  final MethodChannel _channel;

  @override
  Future<TerminalCheckoutResult> checkoutCard({
    required String orderId,
    required int amountMinor,
    required int tipMinor,
    String? terminalReference,
  }) async {
    final payload = await _channel
        .invokeMapMethod<String, dynamic>('checkoutCard', {
          'order_id': orderId,
          'amount_minor': amountMinor,
          'tip_minor': tipMinor,
          if (terminalReference != null && terminalReference.isNotEmpty)
            'terminal_reference': terminalReference,
        });

    if (payload == null || payload.isEmpty) {
      throw PlatformException(
        code: 'PAX_TERMINAL_RESULT_EMPTY',
        message: 'PAX terminal did not return a result payload.',
      );
    }

    final statusValue = (payload['status'] as String? ?? '').toLowerCase();
    final status = switch (statusValue) {
      'approved' => TerminalCheckoutStatus.approved,
      'declined' => TerminalCheckoutStatus.declined,
      _ => TerminalCheckoutStatus.inDoubt,
    };

    return TerminalCheckoutResult(
      status: status,
      providerKey: payload['provider_key'] as String? ?? 'fiserv_bluepay',
      providerTransactionId:
          payload['provider_transaction_id'] as String? ?? '',
      authCode: payload['auth_code'] as String? ?? '',
      maskedPan: payload['masked_pan'] as String? ?? '',
      terminalId: payload['terminal_id'] as String? ?? '',
      entryMode: payload['entry_mode'] as String? ?? '',
      applicationLabel: payload['application_label'] as String?,
      aid: payload['aid'] as String?,
      tvr: payload['tvr'] as String?,
      tsi: payload['tsi'] as String?,
      terminalStatusCode:
          payload['terminal_status_code'] as String? ?? statusValue,
      terminalResultCode:
          payload['terminal_result_code'] as String? ?? statusValue,
      terminalTimestamp:
          DateTime.tryParse(
            payload['terminal_timestamp'] as String? ?? '',
          )?.toUtc() ??
          DateTime.now().toUtc(),
      message: payload['message'] as String?,
      terminalReference: payload['terminal_reference'] as String?,
    );
  }
}
