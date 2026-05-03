import 'dart:convert';
import 'dart:io';
import 'dart:math';

import 'package:http/http.dart' as http;

import '../../core/models/pos_models.dart';
import '../../core/services/device_credentials_store.dart';
import '../../core/services/device_identity_store.dart';
import '../repositories/sync_outbox_repository.dart';
import 'pos_gateway.dart';

class PosApiClient implements PosGateway {
  PosApiClient({
    required DeviceCredentialsStore credentialsStore,
    required DeviceIdentityStore identityStore,
    http.Client? httpClient,
  }) : _credentialsStore = credentialsStore,
       _identityStore = identityStore,
       _httpClient = httpClient ?? http.Client();

  static const _appVersion = '0.1.0';
  static const _protocolVersion = '1';

  final DeviceCredentialsStore _credentialsStore;
  final DeviceIdentityStore _identityStore;
  final http.Client _httpClient;

  @override
  Future<DeviceCredentials> enroll({
    required String apiBaseUrl,
    required String enrollmentCode,
    required String deviceName,
  }) async {
    if (!Platform.isAndroid) {
      throw const PosApiException(
        message:
            'Android-only secure enrollment is enabled. Enroll this device from a managed Android POS device.',
        statusCode: 422,
        errorCode: 'DEVICE_PLATFORM_UNSUPPORTED',
      );
    }

    final identity = await _identityStore.loadOrCreate();
    final response = await _requestJson(
      apiBaseUrl: apiBaseUrl,
      path: '/api/pos/v1/auth/enroll',
      method: 'POST',
      body: {
        'enrollment_code': enrollmentCode,
        'device_name': deviceName,
        'platform': Platform.operatingSystem,
        'device_fingerprint': identity.deviceFingerprint,
        'public_key': identity.publicKey,
        'attestation': identity.attestation,
      },
      authenticate: false,
    );
    final payload = _dataMap(response);

    final credentials = DeviceCredentials(
      apiBaseUrl: _normalizeBaseUrl(apiBaseUrl),
      deviceId: (payload['device'] as Map<String, dynamic>)['id'] as String,
      accessToken:
          (payload['auth'] as Map<String, dynamic>)['access_token'] as String,
      accessTokenExpiresAt: DateTime.parse(
        (payload['auth'] as Map<String, dynamic>)['access_token_expires_at']
            as String,
      ).toUtc(),
      refreshToken:
          (payload['auth'] as Map<String, dynamic>)['refresh_token'] as String,
      refreshTokenExpiresAt: DateTime.parse(
        (payload['auth'] as Map<String, dynamic>)['refresh_token_expires_at']
            as String,
      ).toUtc(),
      tokenFamilyId:
          (payload['auth'] as Map<String, dynamic>)['token_family_id']
              as String,
      silentRefreshWindowMinutes:
          (payload['auth']
                  as Map<String, dynamic>)['silent_refresh_window_minutes']
              as int? ??
          5,
    );

    await _credentialsStore.save(credentials);

    return credentials;
  }

  @override
  Future<Map<String, dynamic>> bootstrap() async {
    return _requestJson(path: '/api/pos/v1/bootstrap', method: 'GET');
  }

  @override
  Future<Map<String, dynamic>> config() async {
    return _requestJson(path: '/api/pos/v1/config', method: 'GET');
  }

  @override
  Future<Map<String, dynamic>> pullSyncDeltas(String? cursor) async {
    final query = cursor == null || cursor.isEmpty
        ? ''
        : '?cursor=${Uri.encodeQueryComponent(cursor)}';

    return _requestJson(path: '/api/pos/v1/sync/deltas$query', method: 'GET');
  }

  @override
  Future<List<Map<String, dynamic>>> pushSyncEvents(
    List<SyncOutboxEntryData> events,
  ) async {
    if (events.isEmpty) {
      return const [];
    }

    final response = await _requestJson(
      path: '/api/pos/v1/sync/events',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {
        'events': events
            .map(
              (event) => {
                'local_event_id': event.localEventId,
                'entity_type': event.entityType,
                'action': event.action,
                'payload': event.payload,
              },
            )
            .toList(),
      },
    );

    return _dataList(response);
  }

  @override
  Future<RegisterSessionSnapshot> openRegisterSession(
    int openingFloatMinor,
  ) async {
    final response = await _requestJson(
      path: '/api/pos/v1/register-sessions/open',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {'opening_float_minor': openingFloatMinor},
    );

    return RegisterSessionSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<RegisterSessionSnapshot> closeRegisterSession({
    required String registerSessionId,
    required int countedCashMinor,
    required int sessionVersion,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/register-sessions/$registerSessionId/close',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {
        'counted_cash_minor': countedCashMinor,
        'session_version': sessionVersion,
      },
    );

    return RegisterSessionSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<List<CustomerSummary>> searchCustomers(String query) async {
    final response = await _requestJson(
      path: '/api/pos/v1/customers/search?q=${Uri.encodeQueryComponent(query)}',
      method: 'GET',
    );

    return _dataList(
      response,
    ).map(CustomerSummary.fromJson).toList(growable: false);
  }

  @override
  Future<List<DiningTableSnapshot>> listDiningTables() async {
    final response = await _requestJson(
      path: '/api/pos/v1/restaurant/tables',
      method: 'GET',
    );
    final tables =
        ((response['tables'] as List<dynamic>? ?? const <dynamic>[]).map(
          (item) => DiningTableSnapshot.fromJson(
            (item as Map).cast<String, dynamic>(),
          ),
        )).toList(growable: false);

    return tables;
  }

  @override
  Future<DiningTableSnapshot> claimDiningTable({
    required String diningTableId,
    String? currentPartyName,
    int? guestCount,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/restaurant/tables/$diningTableId/claim',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {
        ...?(currentPartyName?.isNotEmpty ?? false)
            ? {'current_party_name': currentPartyName}
            : null,
        ...?guestCount == null ? null : {'guest_count': guestCount},
      },
    );

    return DiningTableSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<DiningTableSnapshot> heartbeatDiningTable(String diningTableId) async {
    final response = await _requestJson(
      path: '/api/pos/v1/restaurant/tables/$diningTableId/heartbeat',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: const <String, dynamic>{},
    );

    return DiningTableSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<DiningTableSnapshot> releaseDiningTable(String diningTableId) async {
    final response = await _requestJson(
      path: '/api/pos/v1/restaurant/tables/$diningTableId/release',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: const <String, dynamic>{},
    );

    return DiningTableSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<BusinessDaySummarySnapshot> getBusinessDaySummary() async {
    final response = await _requestJson(
      path: '/api/pos/v1/reports/business-day-summary',
      method: 'GET',
    );

    return BusinessDaySummarySnapshot.fromJson(response);
  }

  @override
  Future<OpenExceptionSummary> getOpenExceptions() async {
    final response = await _requestJson(
      path: '/api/pos/v1/exceptions/open',
      method: 'GET',
    );

    return OpenExceptionSummary.fromJson(response);
  }

  @override
  Future<OrderSummary> createOrder({
    required String registerSessionId,
    required List<Map<String, dynamic>> lines,
    String? customerId,
    String? discountRuleId,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/orders',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {
        'register_session_id': registerSessionId,
        'lines': lines,
        ...?customerId == null ? null : {'customer_id': customerId},
        ...?discountRuleId == null
            ? null
            : {'discount_rule_id': discountRuleId},
      },
    );

    final data = _dataMap(response);

    return OrderSummary(
      id: data['id'] as String,
      orderNumber: data['order_number'] as String,
      totalMinor: data['total_minor'] as int,
      customerId: data['customer_id'] as String?,
      discountMinor: data['discount_minor'] as int?,
    );
  }

  @override
  Future<ReceiptSummary> tenderOrder({
    required String orderId,
    required List<Map<String, dynamic>> tenders,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/orders/$orderId/tenders',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {'tenders': tenders},
    );

    final data = _dataMap(response);

    return ReceiptSummary(
      receiptId: data['receipt_id'] as String,
      receiptNumber: data['receipt_number'] as String,
      payload: (data['payload'] as Map).cast<String, dynamic>(),
    );
  }

  @override
  Future<ReceiptSummary> cashCheckout({
    required String orderId,
    required int tenderedMinor,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/orders/$orderId/cash-checkout',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {'tendered_minor': tenderedMinor},
    );

    final data = _dataMap(response);

    return ReceiptSummary(
      receiptId: data['receipt_id'] as String,
      receiptNumber: data['receipt_number'] as String,
      payload: (data['payload'] as Map).cast<String, dynamic>(),
    );
  }

  @override
  Future<RefundSummary> refundPayment({
    required String paymentId,
    int? amountMinor,
    String? reason,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/payments/$paymentId/refunds',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {
        ...?amountMinor == null ? null : {'amount_minor': amountMinor},
        ...?(reason?.isNotEmpty ?? false) ? {'reason': reason} : null,
      },
    );

    return RefundSummary.fromJson(_dataMap(response));
  }

  @override
  Future<VoidSummary> voidPayment({
    required String paymentId,
    String? reason,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/payments/$paymentId/void',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {
        ...?(reason?.isNotEmpty ?? false) ? {'reason': reason} : null,
      },
    );

    return VoidSummary.fromJson(_dataMap(response));
  }

  @override
  Future<GiftCardSnapshot> lookupGiftCard(String code) async {
    final response = await _requestJson(
      path:
          '/api/pos/v1/gift-cards/lookup?code=${Uri.encodeQueryComponent(code)}',
      method: 'GET',
    );

    return GiftCardSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<GiftCardSnapshot> issueGiftCard({
    required int amountMinor,
    String? customerId,
    String? requestedCode,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/gift-cards/issue',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {
        'amount_minor': amountMinor,
        ...?customerId == null ? null : {'customer_id': customerId},
        ...?(requestedCode?.isNotEmpty ?? false)
            ? {'requested_code': requestedCode}
            : null,
      },
    );

    return GiftCardSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<GiftCardSnapshot> topUpGiftCard({
    required String giftCardCode,
    required int amountMinor,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/gift-cards/top-up',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {'gift_card_code': giftCardCode, 'amount_minor': amountMinor},
    );

    return GiftCardSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<MembershipLookupSnapshot> lookupMembership({
    String? memberNumber,
    String? customerId,
  }) async {
    final queryParameters = <String, String>{};

    if (memberNumber != null && memberNumber.isNotEmpty) {
      queryParameters['member_number'] = memberNumber;
    }

    if (customerId != null && customerId.isNotEmpty) {
      queryParameters['customer_id'] = customerId;
    }

    final query = queryParameters.isEmpty
        ? ''
        : '?${Uri(queryParameters: queryParameters).query}';

    final response = await _requestJson(
      path: '/api/pos/v1/memberships/lookup$query',
      method: 'GET',
    );

    return MembershipLookupSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<MembershipLookupSnapshot> activateMembership({
    required String customerId,
    required String membershipPlanId,
    String? memberNumber,
  }) async {
    final response = await _requestJson(
      path: '/api/pos/v1/memberships/activate',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: {
        'customer_id': customerId,
        'membership_plan_id': membershipPlanId,
        ...?(memberNumber?.isNotEmpty ?? false)
            ? {'member_number': memberNumber}
            : null,
      },
    );

    return MembershipLookupSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<SyncRecoveryRunSnapshot> startSyncRecoveryRun() async {
    final response = await _requestJson(
      path: '/api/pos/v1/sync/recovery-runs',
      method: 'POST',
      idempotencyKey: _randomToken(),
      body: const <String, dynamic>{},
    );

    return SyncRecoveryRunSnapshot.fromJson(_dataMap(response));
  }

  @override
  Future<SyncRecoveryRunSnapshot> getSyncRecoveryRun(
    String recoveryRunId,
  ) async {
    final response = await _requestJson(
      path: '/api/pos/v1/sync/recovery-runs/$recoveryRunId',
      method: 'GET',
    );

    return SyncRecoveryRunSnapshot.fromJson(_dataMap(response));
  }

  Future<DeviceCredentials> _refreshCredentials(
    DeviceCredentials current,
  ) async {
    final response = await _requestJson(
      path: '/api/pos/v1/auth/refresh',
      method: 'POST',
      authenticate: false,
      apiBaseUrl: current.apiBaseUrl,
      body: {'refresh_token': current.refreshToken},
    );
    final payload = _dataMap(response);

    final refreshed = current.copyWith(
      accessToken:
          (payload['auth'] as Map<String, dynamic>)['access_token'] as String,
      accessTokenExpiresAt: DateTime.parse(
        (payload['auth'] as Map<String, dynamic>)['access_token_expires_at']
            as String,
      ).toUtc(),
      refreshToken:
          (payload['auth'] as Map<String, dynamic>)['refresh_token'] as String,
      refreshTokenExpiresAt: DateTime.parse(
        (payload['auth'] as Map<String, dynamic>)['refresh_token_expires_at']
            as String,
      ).toUtc(),
      tokenFamilyId:
          (payload['auth'] as Map<String, dynamic>)['token_family_id']
              as String,
      silentRefreshWindowMinutes:
          (payload['auth']
                  as Map<String, dynamic>)['silent_refresh_window_minutes']
              as int? ??
          current.silentRefreshWindowMinutes,
    );

    await _credentialsStore.save(refreshed);

    return refreshed;
  }

  Future<Map<String, dynamic>> _requestJson({
    required String path,
    required String method,
    Map<String, dynamic>? body,
    String? apiBaseUrl,
    bool authenticate = true,
    String? idempotencyKey,
    bool retryOnAuthFailure = true,
  }) async {
    var credentials = authenticate ? await _credentialsStore.load() : null;
    final identity = await _identityStore.loadOrCreate();

    if (authenticate && credentials == null) {
      throw const PosApiException(
        message: 'Device enrollment is required before calling POS APIs.',
        statusCode: 401,
        errorCode: 'DEVICE_REAUTH_REQUIRED',
        requiresReauth: true,
      );
    }

    if (authenticate && credentials!.shouldRefresh(DateTime.now().toUtc())) {
      credentials = await _refreshCredentials(credentials);
    }

    final request = http.Request(
      method,
      Uri.parse(
        '${_normalizeBaseUrl(apiBaseUrl ?? credentials!.apiBaseUrl)}$path',
      ),
    );

    request.headers.addAll({
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'X-POS-App-Version': _appVersion,
      'X-Device-Protocol-Version': _protocolVersion,
      'X-Platform': Platform.operatingSystem,
      'X-Device-Fingerprint': identity.deviceFingerprint,
    });

    if (idempotencyKey != null) {
      request.headers['Idempotency-Key'] = idempotencyKey;
    }

    if (authenticate) {
      request.headers['Authorization'] = 'Bearer ${credentials!.accessToken}';
    }

    if (body != null) {
      request.body = jsonEncode(body);
    }

    final streamedResponse = await _httpClient.send(request);
    final response = await http.Response.fromStream(streamedResponse);
    final parsed = response.body.isEmpty
        ? <String, dynamic>{}
        : jsonDecode(response.body) as Map<String, dynamic>;

    if (response.statusCode == 401 &&
        authenticate &&
        retryOnAuthFailure &&
        credentials != null) {
      try {
        await _refreshCredentials(credentials);

        return _requestJson(
          path: path,
          method: method,
          body: body,
          apiBaseUrl: apiBaseUrl,
          authenticate: authenticate,
          idempotencyKey: idempotencyKey,
          retryOnAuthFailure: false,
        );
      } on PosApiException catch (exception) {
        throw PosApiException(
          message: exception.message,
          statusCode: exception.statusCode,
          errorCode: exception.errorCode,
          requiresReauth: true,
        );
      }
    }

    if (response.statusCode >= 400) {
      final errorCode = parsed['error_code'] as String?;
      final requiresReauth = errorCode == 'DEVICE_REAUTH_REQUIRED';

      throw PosApiException(
        message:
            parsed['message'] as String? ??
            'The POS API request failed with status ${response.statusCode}.',
        statusCode: response.statusCode,
        errorCode: errorCode,
        requiresReauth: requiresReauth,
      );
    }

    return parsed;
  }

  Map<String, dynamic> _dataMap(Map<String, dynamic> response) {
    return ((response['data'] as Map?) ?? const {}).cast<String, dynamic>();
  }

  List<Map<String, dynamic>> _dataList(Map<String, dynamic> response) {
    return (response['data'] as List<dynamic>? ?? const <dynamic>[])
        .map((item) => (item as Map).cast<String, dynamic>())
        .toList(growable: false);
  }

  String _normalizeBaseUrl(String value) {
    return value.endsWith('/') ? value.substring(0, value.length - 1) : value;
  }

  String _randomToken() {
    final random = Random.secure();
    final values = List<int>.generate(18, (_) => random.nextInt(256));

    return base64UrlEncode(values);
  }

  @override
  void close() {
    _httpClient.close();
  }
}
