import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/core/services/pax_terminal_gateway.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel('pos_app/pax_terminal');

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test(
    'maps approved method-channel payload to terminal checkout result',
    () async {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(channel, (call) async {
            expect(call.method, 'checkoutCard');
            expect(call.arguments, containsPair('order_id', 'order-1'));

            return <String, dynamic>{
              'status': 'approved',
              'provider_key': 'fiserv_bluepay',
              'provider_transaction_id': '100000001',
              'auth_code': 'ALVSGO',
              'masked_pan': '************1111',
              'terminal_id': 'PAX-A920-01',
              'entry_mode': 'chip',
              'application_label': 'VISA CREDIT',
              'aid': 'A0000000031010',
              'tvr': '0000008000',
              'tsi': 'E800',
              'terminal_status_code': 'approved',
              'terminal_result_code': '00',
              'terminal_timestamp': '2026-04-24T12:00:00Z',
            };
          });

      final gateway = MethodChannelPaxTerminalGateway(channel);
      final result = await gateway.checkoutCard(
        orderId: 'order-1',
        amountMinor: 1320,
        tipMinor: 200,
      );

      expect(result.isApproved, isTrue);
      expect(result.providerTransactionId, '100000001');
      expect(result.authCode, 'ALVSGO');
      expect(result.terminalStatusCode, 'approved');
      expect(result.terminalResultCode, '00');
    },
  );

  test('maps declined method-channel payload to declined status', () async {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
          return <String, dynamic>{
            'status': 'declined',
            'terminal_status_code': 'declined',
            'terminal_result_code': '05',
            'message': 'Do not honor',
          };
        });

    final gateway = MethodChannelPaxTerminalGateway(channel);
    final result = await gateway.checkoutCard(
      orderId: 'order-2',
      amountMinor: 1320,
      tipMinor: 0,
    );

    expect(result.status, TerminalCheckoutStatus.declined);
    expect(result.isInDoubt, isFalse);
    expect(result.message, 'Do not honor');
  });

  test('maps timeout/no-response payload to in-doubt status', () async {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
          return <String, dynamic>{
            'status': 'in_doubt',
            'provider_transaction_id': '100000003',
            'terminal_status_code': 'timeout',
            'terminal_result_code': 'no_response',
          };
        });

    final gateway = MethodChannelPaxTerminalGateway(channel);
    final result = await gateway.checkoutCard(
      orderId: 'order-3',
      amountMinor: 1320,
      tipMinor: 0,
    );

    expect(result.isInDoubt, isTrue);
    expect(result.terminalStatusCode, 'timeout');
    expect(result.terminalResultCode, 'no_response');
  });
}
