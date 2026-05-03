// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pos_database.dart';

// ignore_for_file: type=lint
class $DeviceBootstrapCachesTable extends DeviceBootstrapCaches
    with TableInfo<$DeviceBootstrapCachesTable, DeviceBootstrapCache> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $DeviceBootstrapCachesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _apiBaseUrlMeta = const VerificationMeta(
    'apiBaseUrl',
  );
  @override
  late final GeneratedColumn<String> apiBaseUrl = GeneratedColumn<String>(
    'api_base_url',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _deviceIdMeta = const VerificationMeta(
    'deviceId',
  );
  @override
  late final GeneratedColumn<String> deviceId = GeneratedColumn<String>(
    'device_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _merchantIdMeta = const VerificationMeta(
    'merchantId',
  );
  @override
  late final GeneratedColumn<String> merchantId = GeneratedColumn<String>(
    'merchant_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _storeIdMeta = const VerificationMeta(
    'storeId',
  );
  @override
  late final GeneratedColumn<String> storeId = GeneratedColumn<String>(
    'store_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _payloadJsonMeta = const VerificationMeta(
    'payloadJson',
  );
  @override
  late final GeneratedColumn<String> payloadJson = GeneratedColumn<String>(
    'payload_json',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _syncedAtMeta = const VerificationMeta(
    'syncedAt',
  );
  @override
  late final GeneratedColumn<DateTime> syncedAt = GeneratedColumn<DateTime>(
    'synced_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    apiBaseUrl,
    deviceId,
    merchantId,
    storeId,
    payloadJson,
    syncedAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'device_bootstrap_caches';
  @override
  VerificationContext validateIntegrity(
    Insertable<DeviceBootstrapCache> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('api_base_url')) {
      context.handle(
        _apiBaseUrlMeta,
        apiBaseUrl.isAcceptableOrUnknown(
          data['api_base_url']!,
          _apiBaseUrlMeta,
        ),
      );
    }
    if (data.containsKey('device_id')) {
      context.handle(
        _deviceIdMeta,
        deviceId.isAcceptableOrUnknown(data['device_id']!, _deviceIdMeta),
      );
    }
    if (data.containsKey('merchant_id')) {
      context.handle(
        _merchantIdMeta,
        merchantId.isAcceptableOrUnknown(data['merchant_id']!, _merchantIdMeta),
      );
    }
    if (data.containsKey('store_id')) {
      context.handle(
        _storeIdMeta,
        storeId.isAcceptableOrUnknown(data['store_id']!, _storeIdMeta),
      );
    }
    if (data.containsKey('payload_json')) {
      context.handle(
        _payloadJsonMeta,
        payloadJson.isAcceptableOrUnknown(
          data['payload_json']!,
          _payloadJsonMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_payloadJsonMeta);
    }
    if (data.containsKey('synced_at')) {
      context.handle(
        _syncedAtMeta,
        syncedAt.isAcceptableOrUnknown(data['synced_at']!, _syncedAtMeta),
      );
    } else if (isInserting) {
      context.missing(_syncedAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  DeviceBootstrapCache map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return DeviceBootstrapCache(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      apiBaseUrl: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}api_base_url'],
      ),
      deviceId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}device_id'],
      ),
      merchantId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}merchant_id'],
      ),
      storeId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}store_id'],
      ),
      payloadJson: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}payload_json'],
      )!,
      syncedAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}synced_at'],
      )!,
    );
  }

  @override
  $DeviceBootstrapCachesTable createAlias(String alias) {
    return $DeviceBootstrapCachesTable(attachedDatabase, alias);
  }
}

class DeviceBootstrapCache extends DataClass
    implements Insertable<DeviceBootstrapCache> {
  final String id;
  final String? apiBaseUrl;
  final String? deviceId;
  final String? merchantId;
  final String? storeId;
  final String payloadJson;
  final DateTime syncedAt;
  const DeviceBootstrapCache({
    required this.id,
    this.apiBaseUrl,
    this.deviceId,
    this.merchantId,
    this.storeId,
    required this.payloadJson,
    required this.syncedAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    if (!nullToAbsent || apiBaseUrl != null) {
      map['api_base_url'] = Variable<String>(apiBaseUrl);
    }
    if (!nullToAbsent || deviceId != null) {
      map['device_id'] = Variable<String>(deviceId);
    }
    if (!nullToAbsent || merchantId != null) {
      map['merchant_id'] = Variable<String>(merchantId);
    }
    if (!nullToAbsent || storeId != null) {
      map['store_id'] = Variable<String>(storeId);
    }
    map['payload_json'] = Variable<String>(payloadJson);
    map['synced_at'] = Variable<DateTime>(syncedAt);
    return map;
  }

  DeviceBootstrapCachesCompanion toCompanion(bool nullToAbsent) {
    return DeviceBootstrapCachesCompanion(
      id: Value(id),
      apiBaseUrl: apiBaseUrl == null && nullToAbsent
          ? const Value.absent()
          : Value(apiBaseUrl),
      deviceId: deviceId == null && nullToAbsent
          ? const Value.absent()
          : Value(deviceId),
      merchantId: merchantId == null && nullToAbsent
          ? const Value.absent()
          : Value(merchantId),
      storeId: storeId == null && nullToAbsent
          ? const Value.absent()
          : Value(storeId),
      payloadJson: Value(payloadJson),
      syncedAt: Value(syncedAt),
    );
  }

  factory DeviceBootstrapCache.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return DeviceBootstrapCache(
      id: serializer.fromJson<String>(json['id']),
      apiBaseUrl: serializer.fromJson<String?>(json['apiBaseUrl']),
      deviceId: serializer.fromJson<String?>(json['deviceId']),
      merchantId: serializer.fromJson<String?>(json['merchantId']),
      storeId: serializer.fromJson<String?>(json['storeId']),
      payloadJson: serializer.fromJson<String>(json['payloadJson']),
      syncedAt: serializer.fromJson<DateTime>(json['syncedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'apiBaseUrl': serializer.toJson<String?>(apiBaseUrl),
      'deviceId': serializer.toJson<String?>(deviceId),
      'merchantId': serializer.toJson<String?>(merchantId),
      'storeId': serializer.toJson<String?>(storeId),
      'payloadJson': serializer.toJson<String>(payloadJson),
      'syncedAt': serializer.toJson<DateTime>(syncedAt),
    };
  }

  DeviceBootstrapCache copyWith({
    String? id,
    Value<String?> apiBaseUrl = const Value.absent(),
    Value<String?> deviceId = const Value.absent(),
    Value<String?> merchantId = const Value.absent(),
    Value<String?> storeId = const Value.absent(),
    String? payloadJson,
    DateTime? syncedAt,
  }) => DeviceBootstrapCache(
    id: id ?? this.id,
    apiBaseUrl: apiBaseUrl.present ? apiBaseUrl.value : this.apiBaseUrl,
    deviceId: deviceId.present ? deviceId.value : this.deviceId,
    merchantId: merchantId.present ? merchantId.value : this.merchantId,
    storeId: storeId.present ? storeId.value : this.storeId,
    payloadJson: payloadJson ?? this.payloadJson,
    syncedAt: syncedAt ?? this.syncedAt,
  );
  DeviceBootstrapCache copyWithCompanion(DeviceBootstrapCachesCompanion data) {
    return DeviceBootstrapCache(
      id: data.id.present ? data.id.value : this.id,
      apiBaseUrl: data.apiBaseUrl.present
          ? data.apiBaseUrl.value
          : this.apiBaseUrl,
      deviceId: data.deviceId.present ? data.deviceId.value : this.deviceId,
      merchantId: data.merchantId.present
          ? data.merchantId.value
          : this.merchantId,
      storeId: data.storeId.present ? data.storeId.value : this.storeId,
      payloadJson: data.payloadJson.present
          ? data.payloadJson.value
          : this.payloadJson,
      syncedAt: data.syncedAt.present ? data.syncedAt.value : this.syncedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('DeviceBootstrapCache(')
          ..write('id: $id, ')
          ..write('apiBaseUrl: $apiBaseUrl, ')
          ..write('deviceId: $deviceId, ')
          ..write('merchantId: $merchantId, ')
          ..write('storeId: $storeId, ')
          ..write('payloadJson: $payloadJson, ')
          ..write('syncedAt: $syncedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    apiBaseUrl,
    deviceId,
    merchantId,
    storeId,
    payloadJson,
    syncedAt,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is DeviceBootstrapCache &&
          other.id == this.id &&
          other.apiBaseUrl == this.apiBaseUrl &&
          other.deviceId == this.deviceId &&
          other.merchantId == this.merchantId &&
          other.storeId == this.storeId &&
          other.payloadJson == this.payloadJson &&
          other.syncedAt == this.syncedAt);
}

class DeviceBootstrapCachesCompanion
    extends UpdateCompanion<DeviceBootstrapCache> {
  final Value<String> id;
  final Value<String?> apiBaseUrl;
  final Value<String?> deviceId;
  final Value<String?> merchantId;
  final Value<String?> storeId;
  final Value<String> payloadJson;
  final Value<DateTime> syncedAt;
  final Value<int> rowid;
  const DeviceBootstrapCachesCompanion({
    this.id = const Value.absent(),
    this.apiBaseUrl = const Value.absent(),
    this.deviceId = const Value.absent(),
    this.merchantId = const Value.absent(),
    this.storeId = const Value.absent(),
    this.payloadJson = const Value.absent(),
    this.syncedAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  DeviceBootstrapCachesCompanion.insert({
    required String id,
    this.apiBaseUrl = const Value.absent(),
    this.deviceId = const Value.absent(),
    this.merchantId = const Value.absent(),
    this.storeId = const Value.absent(),
    required String payloadJson,
    required DateTime syncedAt,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       payloadJson = Value(payloadJson),
       syncedAt = Value(syncedAt);
  static Insertable<DeviceBootstrapCache> custom({
    Expression<String>? id,
    Expression<String>? apiBaseUrl,
    Expression<String>? deviceId,
    Expression<String>? merchantId,
    Expression<String>? storeId,
    Expression<String>? payloadJson,
    Expression<DateTime>? syncedAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (apiBaseUrl != null) 'api_base_url': apiBaseUrl,
      if (deviceId != null) 'device_id': deviceId,
      if (merchantId != null) 'merchant_id': merchantId,
      if (storeId != null) 'store_id': storeId,
      if (payloadJson != null) 'payload_json': payloadJson,
      if (syncedAt != null) 'synced_at': syncedAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  DeviceBootstrapCachesCompanion copyWith({
    Value<String>? id,
    Value<String?>? apiBaseUrl,
    Value<String?>? deviceId,
    Value<String?>? merchantId,
    Value<String?>? storeId,
    Value<String>? payloadJson,
    Value<DateTime>? syncedAt,
    Value<int>? rowid,
  }) {
    return DeviceBootstrapCachesCompanion(
      id: id ?? this.id,
      apiBaseUrl: apiBaseUrl ?? this.apiBaseUrl,
      deviceId: deviceId ?? this.deviceId,
      merchantId: merchantId ?? this.merchantId,
      storeId: storeId ?? this.storeId,
      payloadJson: payloadJson ?? this.payloadJson,
      syncedAt: syncedAt ?? this.syncedAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (apiBaseUrl.present) {
      map['api_base_url'] = Variable<String>(apiBaseUrl.value);
    }
    if (deviceId.present) {
      map['device_id'] = Variable<String>(deviceId.value);
    }
    if (merchantId.present) {
      map['merchant_id'] = Variable<String>(merchantId.value);
    }
    if (storeId.present) {
      map['store_id'] = Variable<String>(storeId.value);
    }
    if (payloadJson.present) {
      map['payload_json'] = Variable<String>(payloadJson.value);
    }
    if (syncedAt.present) {
      map['synced_at'] = Variable<DateTime>(syncedAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('DeviceBootstrapCachesCompanion(')
          ..write('id: $id, ')
          ..write('apiBaseUrl: $apiBaseUrl, ')
          ..write('deviceId: $deviceId, ')
          ..write('merchantId: $merchantId, ')
          ..write('storeId: $storeId, ')
          ..write('payloadJson: $payloadJson, ')
          ..write('syncedAt: $syncedAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $SyncOutboxEntriesTable extends SyncOutboxEntries
    with TableInfo<$SyncOutboxEntriesTable, SyncOutboxEntry> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $SyncOutboxEntriesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _localEventIdMeta = const VerificationMeta(
    'localEventId',
  );
  @override
  late final GeneratedColumn<String> localEventId = GeneratedColumn<String>(
    'local_event_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _entityTypeMeta = const VerificationMeta(
    'entityType',
  );
  @override
  late final GeneratedColumn<String> entityType = GeneratedColumn<String>(
    'entity_type',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _actionMeta = const VerificationMeta('action');
  @override
  late final GeneratedColumn<String> action = GeneratedColumn<String>(
    'action',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _payloadJsonMeta = const VerificationMeta(
    'payloadJson',
  );
  @override
  late final GeneratedColumn<String> payloadJson = GeneratedColumn<String>(
    'payload_json',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _statusMeta = const VerificationMeta('status');
  @override
  late final GeneratedColumn<String> status = GeneratedColumn<String>(
    'status',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    defaultValue: const Constant('pending'),
  );
  static const VerificationMeta _attemptsMeta = const VerificationMeta(
    'attempts',
  );
  @override
  late final GeneratedColumn<int> attempts = GeneratedColumn<int>(
    'attempts',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultValue: const Constant(0),
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _availableAtMeta = const VerificationMeta(
    'availableAt',
  );
  @override
  late final GeneratedColumn<DateTime> availableAt = GeneratedColumn<DateTime>(
    'available_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    localEventId,
    entityType,
    action,
    payloadJson,
    status,
    attempts,
    createdAt,
    availableAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'sync_outbox_entries';
  @override
  VerificationContext validateIntegrity(
    Insertable<SyncOutboxEntry> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('local_event_id')) {
      context.handle(
        _localEventIdMeta,
        localEventId.isAcceptableOrUnknown(
          data['local_event_id']!,
          _localEventIdMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_localEventIdMeta);
    }
    if (data.containsKey('entity_type')) {
      context.handle(
        _entityTypeMeta,
        entityType.isAcceptableOrUnknown(data['entity_type']!, _entityTypeMeta),
      );
    } else if (isInserting) {
      context.missing(_entityTypeMeta);
    }
    if (data.containsKey('action')) {
      context.handle(
        _actionMeta,
        action.isAcceptableOrUnknown(data['action']!, _actionMeta),
      );
    } else if (isInserting) {
      context.missing(_actionMeta);
    }
    if (data.containsKey('payload_json')) {
      context.handle(
        _payloadJsonMeta,
        payloadJson.isAcceptableOrUnknown(
          data['payload_json']!,
          _payloadJsonMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_payloadJsonMeta);
    }
    if (data.containsKey('status')) {
      context.handle(
        _statusMeta,
        status.isAcceptableOrUnknown(data['status']!, _statusMeta),
      );
    }
    if (data.containsKey('attempts')) {
      context.handle(
        _attemptsMeta,
        attempts.isAcceptableOrUnknown(data['attempts']!, _attemptsMeta),
      );
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    } else if (isInserting) {
      context.missing(_createdAtMeta);
    }
    if (data.containsKey('available_at')) {
      context.handle(
        _availableAtMeta,
        availableAt.isAcceptableOrUnknown(
          data['available_at']!,
          _availableAtMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_availableAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {localEventId};
  @override
  SyncOutboxEntry map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return SyncOutboxEntry(
      localEventId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}local_event_id'],
      )!,
      entityType: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}entity_type'],
      )!,
      action: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}action'],
      )!,
      payloadJson: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}payload_json'],
      )!,
      status: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}status'],
      )!,
      attempts: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}attempts'],
      )!,
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}created_at'],
      )!,
      availableAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}available_at'],
      )!,
    );
  }

  @override
  $SyncOutboxEntriesTable createAlias(String alias) {
    return $SyncOutboxEntriesTable(attachedDatabase, alias);
  }
}

class SyncOutboxEntry extends DataClass implements Insertable<SyncOutboxEntry> {
  final String localEventId;
  final String entityType;
  final String action;
  final String payloadJson;
  final String status;
  final int attempts;
  final DateTime createdAt;
  final DateTime availableAt;
  const SyncOutboxEntry({
    required this.localEventId,
    required this.entityType,
    required this.action,
    required this.payloadJson,
    required this.status,
    required this.attempts,
    required this.createdAt,
    required this.availableAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['local_event_id'] = Variable<String>(localEventId);
    map['entity_type'] = Variable<String>(entityType);
    map['action'] = Variable<String>(action);
    map['payload_json'] = Variable<String>(payloadJson);
    map['status'] = Variable<String>(status);
    map['attempts'] = Variable<int>(attempts);
    map['created_at'] = Variable<DateTime>(createdAt);
    map['available_at'] = Variable<DateTime>(availableAt);
    return map;
  }

  SyncOutboxEntriesCompanion toCompanion(bool nullToAbsent) {
    return SyncOutboxEntriesCompanion(
      localEventId: Value(localEventId),
      entityType: Value(entityType),
      action: Value(action),
      payloadJson: Value(payloadJson),
      status: Value(status),
      attempts: Value(attempts),
      createdAt: Value(createdAt),
      availableAt: Value(availableAt),
    );
  }

  factory SyncOutboxEntry.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return SyncOutboxEntry(
      localEventId: serializer.fromJson<String>(json['localEventId']),
      entityType: serializer.fromJson<String>(json['entityType']),
      action: serializer.fromJson<String>(json['action']),
      payloadJson: serializer.fromJson<String>(json['payloadJson']),
      status: serializer.fromJson<String>(json['status']),
      attempts: serializer.fromJson<int>(json['attempts']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
      availableAt: serializer.fromJson<DateTime>(json['availableAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'localEventId': serializer.toJson<String>(localEventId),
      'entityType': serializer.toJson<String>(entityType),
      'action': serializer.toJson<String>(action),
      'payloadJson': serializer.toJson<String>(payloadJson),
      'status': serializer.toJson<String>(status),
      'attempts': serializer.toJson<int>(attempts),
      'createdAt': serializer.toJson<DateTime>(createdAt),
      'availableAt': serializer.toJson<DateTime>(availableAt),
    };
  }

  SyncOutboxEntry copyWith({
    String? localEventId,
    String? entityType,
    String? action,
    String? payloadJson,
    String? status,
    int? attempts,
    DateTime? createdAt,
    DateTime? availableAt,
  }) => SyncOutboxEntry(
    localEventId: localEventId ?? this.localEventId,
    entityType: entityType ?? this.entityType,
    action: action ?? this.action,
    payloadJson: payloadJson ?? this.payloadJson,
    status: status ?? this.status,
    attempts: attempts ?? this.attempts,
    createdAt: createdAt ?? this.createdAt,
    availableAt: availableAt ?? this.availableAt,
  );
  SyncOutboxEntry copyWithCompanion(SyncOutboxEntriesCompanion data) {
    return SyncOutboxEntry(
      localEventId: data.localEventId.present
          ? data.localEventId.value
          : this.localEventId,
      entityType: data.entityType.present
          ? data.entityType.value
          : this.entityType,
      action: data.action.present ? data.action.value : this.action,
      payloadJson: data.payloadJson.present
          ? data.payloadJson.value
          : this.payloadJson,
      status: data.status.present ? data.status.value : this.status,
      attempts: data.attempts.present ? data.attempts.value : this.attempts,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      availableAt: data.availableAt.present
          ? data.availableAt.value
          : this.availableAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('SyncOutboxEntry(')
          ..write('localEventId: $localEventId, ')
          ..write('entityType: $entityType, ')
          ..write('action: $action, ')
          ..write('payloadJson: $payloadJson, ')
          ..write('status: $status, ')
          ..write('attempts: $attempts, ')
          ..write('createdAt: $createdAt, ')
          ..write('availableAt: $availableAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    localEventId,
    entityType,
    action,
    payloadJson,
    status,
    attempts,
    createdAt,
    availableAt,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is SyncOutboxEntry &&
          other.localEventId == this.localEventId &&
          other.entityType == this.entityType &&
          other.action == this.action &&
          other.payloadJson == this.payloadJson &&
          other.status == this.status &&
          other.attempts == this.attempts &&
          other.createdAt == this.createdAt &&
          other.availableAt == this.availableAt);
}

class SyncOutboxEntriesCompanion extends UpdateCompanion<SyncOutboxEntry> {
  final Value<String> localEventId;
  final Value<String> entityType;
  final Value<String> action;
  final Value<String> payloadJson;
  final Value<String> status;
  final Value<int> attempts;
  final Value<DateTime> createdAt;
  final Value<DateTime> availableAt;
  final Value<int> rowid;
  const SyncOutboxEntriesCompanion({
    this.localEventId = const Value.absent(),
    this.entityType = const Value.absent(),
    this.action = const Value.absent(),
    this.payloadJson = const Value.absent(),
    this.status = const Value.absent(),
    this.attempts = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.availableAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  SyncOutboxEntriesCompanion.insert({
    required String localEventId,
    required String entityType,
    required String action,
    required String payloadJson,
    this.status = const Value.absent(),
    this.attempts = const Value.absent(),
    required DateTime createdAt,
    required DateTime availableAt,
    this.rowid = const Value.absent(),
  }) : localEventId = Value(localEventId),
       entityType = Value(entityType),
       action = Value(action),
       payloadJson = Value(payloadJson),
       createdAt = Value(createdAt),
       availableAt = Value(availableAt);
  static Insertable<SyncOutboxEntry> custom({
    Expression<String>? localEventId,
    Expression<String>? entityType,
    Expression<String>? action,
    Expression<String>? payloadJson,
    Expression<String>? status,
    Expression<int>? attempts,
    Expression<DateTime>? createdAt,
    Expression<DateTime>? availableAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (localEventId != null) 'local_event_id': localEventId,
      if (entityType != null) 'entity_type': entityType,
      if (action != null) 'action': action,
      if (payloadJson != null) 'payload_json': payloadJson,
      if (status != null) 'status': status,
      if (attempts != null) 'attempts': attempts,
      if (createdAt != null) 'created_at': createdAt,
      if (availableAt != null) 'available_at': availableAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  SyncOutboxEntriesCompanion copyWith({
    Value<String>? localEventId,
    Value<String>? entityType,
    Value<String>? action,
    Value<String>? payloadJson,
    Value<String>? status,
    Value<int>? attempts,
    Value<DateTime>? createdAt,
    Value<DateTime>? availableAt,
    Value<int>? rowid,
  }) {
    return SyncOutboxEntriesCompanion(
      localEventId: localEventId ?? this.localEventId,
      entityType: entityType ?? this.entityType,
      action: action ?? this.action,
      payloadJson: payloadJson ?? this.payloadJson,
      status: status ?? this.status,
      attempts: attempts ?? this.attempts,
      createdAt: createdAt ?? this.createdAt,
      availableAt: availableAt ?? this.availableAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (localEventId.present) {
      map['local_event_id'] = Variable<String>(localEventId.value);
    }
    if (entityType.present) {
      map['entity_type'] = Variable<String>(entityType.value);
    }
    if (action.present) {
      map['action'] = Variable<String>(action.value);
    }
    if (payloadJson.present) {
      map['payload_json'] = Variable<String>(payloadJson.value);
    }
    if (status.present) {
      map['status'] = Variable<String>(status.value);
    }
    if (attempts.present) {
      map['attempts'] = Variable<int>(attempts.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (availableAt.present) {
      map['available_at'] = Variable<DateTime>(availableAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SyncOutboxEntriesCompanion(')
          ..write('localEventId: $localEventId, ')
          ..write('entityType: $entityType, ')
          ..write('action: $action, ')
          ..write('payloadJson: $payloadJson, ')
          ..write('status: $status, ')
          ..write('attempts: $attempts, ')
          ..write('createdAt: $createdAt, ')
          ..write('availableAt: $availableAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $ReceiptPrintJobsTable extends ReceiptPrintJobs
    with TableInfo<$ReceiptPrintJobsTable, ReceiptPrintJob> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $ReceiptPrintJobsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _receiptIdMeta = const VerificationMeta(
    'receiptId',
  );
  @override
  late final GeneratedColumn<String> receiptId = GeneratedColumn<String>(
    'receipt_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _routeKeyMeta = const VerificationMeta(
    'routeKey',
  );
  @override
  late final GeneratedColumn<String> routeKey = GeneratedColumn<String>(
    'route_key',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _payloadJsonMeta = const VerificationMeta(
    'payloadJson',
  );
  @override
  late final GeneratedColumn<String> payloadJson = GeneratedColumn<String>(
    'payload_json',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _statusMeta = const VerificationMeta('status');
  @override
  late final GeneratedColumn<String> status = GeneratedColumn<String>(
    'status',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    defaultValue: const Constant('pending'),
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _expiresAtMeta = const VerificationMeta(
    'expiresAt',
  );
  @override
  late final GeneratedColumn<DateTime> expiresAt = GeneratedColumn<DateTime>(
    'expires_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    receiptId,
    routeKey,
    payloadJson,
    status,
    createdAt,
    expiresAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'receipt_print_jobs';
  @override
  VerificationContext validateIntegrity(
    Insertable<ReceiptPrintJob> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('receipt_id')) {
      context.handle(
        _receiptIdMeta,
        receiptId.isAcceptableOrUnknown(data['receipt_id']!, _receiptIdMeta),
      );
    }
    if (data.containsKey('route_key')) {
      context.handle(
        _routeKeyMeta,
        routeKey.isAcceptableOrUnknown(data['route_key']!, _routeKeyMeta),
      );
    } else if (isInserting) {
      context.missing(_routeKeyMeta);
    }
    if (data.containsKey('payload_json')) {
      context.handle(
        _payloadJsonMeta,
        payloadJson.isAcceptableOrUnknown(
          data['payload_json']!,
          _payloadJsonMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_payloadJsonMeta);
    }
    if (data.containsKey('status')) {
      context.handle(
        _statusMeta,
        status.isAcceptableOrUnknown(data['status']!, _statusMeta),
      );
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    } else if (isInserting) {
      context.missing(_createdAtMeta);
    }
    if (data.containsKey('expires_at')) {
      context.handle(
        _expiresAtMeta,
        expiresAt.isAcceptableOrUnknown(data['expires_at']!, _expiresAtMeta),
      );
    } else if (isInserting) {
      context.missing(_expiresAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  ReceiptPrintJob map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return ReceiptPrintJob(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      receiptId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}receipt_id'],
      ),
      routeKey: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}route_key'],
      )!,
      payloadJson: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}payload_json'],
      )!,
      status: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}status'],
      )!,
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}created_at'],
      )!,
      expiresAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}expires_at'],
      )!,
    );
  }

  @override
  $ReceiptPrintJobsTable createAlias(String alias) {
    return $ReceiptPrintJobsTable(attachedDatabase, alias);
  }
}

class ReceiptPrintJob extends DataClass implements Insertable<ReceiptPrintJob> {
  final String id;
  final String? receiptId;
  final String routeKey;
  final String payloadJson;
  final String status;
  final DateTime createdAt;
  final DateTime expiresAt;
  const ReceiptPrintJob({
    required this.id,
    this.receiptId,
    required this.routeKey,
    required this.payloadJson,
    required this.status,
    required this.createdAt,
    required this.expiresAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    if (!nullToAbsent || receiptId != null) {
      map['receipt_id'] = Variable<String>(receiptId);
    }
    map['route_key'] = Variable<String>(routeKey);
    map['payload_json'] = Variable<String>(payloadJson);
    map['status'] = Variable<String>(status);
    map['created_at'] = Variable<DateTime>(createdAt);
    map['expires_at'] = Variable<DateTime>(expiresAt);
    return map;
  }

  ReceiptPrintJobsCompanion toCompanion(bool nullToAbsent) {
    return ReceiptPrintJobsCompanion(
      id: Value(id),
      receiptId: receiptId == null && nullToAbsent
          ? const Value.absent()
          : Value(receiptId),
      routeKey: Value(routeKey),
      payloadJson: Value(payloadJson),
      status: Value(status),
      createdAt: Value(createdAt),
      expiresAt: Value(expiresAt),
    );
  }

  factory ReceiptPrintJob.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return ReceiptPrintJob(
      id: serializer.fromJson<String>(json['id']),
      receiptId: serializer.fromJson<String?>(json['receiptId']),
      routeKey: serializer.fromJson<String>(json['routeKey']),
      payloadJson: serializer.fromJson<String>(json['payloadJson']),
      status: serializer.fromJson<String>(json['status']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
      expiresAt: serializer.fromJson<DateTime>(json['expiresAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'receiptId': serializer.toJson<String?>(receiptId),
      'routeKey': serializer.toJson<String>(routeKey),
      'payloadJson': serializer.toJson<String>(payloadJson),
      'status': serializer.toJson<String>(status),
      'createdAt': serializer.toJson<DateTime>(createdAt),
      'expiresAt': serializer.toJson<DateTime>(expiresAt),
    };
  }

  ReceiptPrintJob copyWith({
    String? id,
    Value<String?> receiptId = const Value.absent(),
    String? routeKey,
    String? payloadJson,
    String? status,
    DateTime? createdAt,
    DateTime? expiresAt,
  }) => ReceiptPrintJob(
    id: id ?? this.id,
    receiptId: receiptId.present ? receiptId.value : this.receiptId,
    routeKey: routeKey ?? this.routeKey,
    payloadJson: payloadJson ?? this.payloadJson,
    status: status ?? this.status,
    createdAt: createdAt ?? this.createdAt,
    expiresAt: expiresAt ?? this.expiresAt,
  );
  ReceiptPrintJob copyWithCompanion(ReceiptPrintJobsCompanion data) {
    return ReceiptPrintJob(
      id: data.id.present ? data.id.value : this.id,
      receiptId: data.receiptId.present ? data.receiptId.value : this.receiptId,
      routeKey: data.routeKey.present ? data.routeKey.value : this.routeKey,
      payloadJson: data.payloadJson.present
          ? data.payloadJson.value
          : this.payloadJson,
      status: data.status.present ? data.status.value : this.status,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      expiresAt: data.expiresAt.present ? data.expiresAt.value : this.expiresAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('ReceiptPrintJob(')
          ..write('id: $id, ')
          ..write('receiptId: $receiptId, ')
          ..write('routeKey: $routeKey, ')
          ..write('payloadJson: $payloadJson, ')
          ..write('status: $status, ')
          ..write('createdAt: $createdAt, ')
          ..write('expiresAt: $expiresAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    receiptId,
    routeKey,
    payloadJson,
    status,
    createdAt,
    expiresAt,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is ReceiptPrintJob &&
          other.id == this.id &&
          other.receiptId == this.receiptId &&
          other.routeKey == this.routeKey &&
          other.payloadJson == this.payloadJson &&
          other.status == this.status &&
          other.createdAt == this.createdAt &&
          other.expiresAt == this.expiresAt);
}

class ReceiptPrintJobsCompanion extends UpdateCompanion<ReceiptPrintJob> {
  final Value<String> id;
  final Value<String?> receiptId;
  final Value<String> routeKey;
  final Value<String> payloadJson;
  final Value<String> status;
  final Value<DateTime> createdAt;
  final Value<DateTime> expiresAt;
  final Value<int> rowid;
  const ReceiptPrintJobsCompanion({
    this.id = const Value.absent(),
    this.receiptId = const Value.absent(),
    this.routeKey = const Value.absent(),
    this.payloadJson = const Value.absent(),
    this.status = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.expiresAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  ReceiptPrintJobsCompanion.insert({
    required String id,
    this.receiptId = const Value.absent(),
    required String routeKey,
    required String payloadJson,
    this.status = const Value.absent(),
    required DateTime createdAt,
    required DateTime expiresAt,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       routeKey = Value(routeKey),
       payloadJson = Value(payloadJson),
       createdAt = Value(createdAt),
       expiresAt = Value(expiresAt);
  static Insertable<ReceiptPrintJob> custom({
    Expression<String>? id,
    Expression<String>? receiptId,
    Expression<String>? routeKey,
    Expression<String>? payloadJson,
    Expression<String>? status,
    Expression<DateTime>? createdAt,
    Expression<DateTime>? expiresAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (receiptId != null) 'receipt_id': receiptId,
      if (routeKey != null) 'route_key': routeKey,
      if (payloadJson != null) 'payload_json': payloadJson,
      if (status != null) 'status': status,
      if (createdAt != null) 'created_at': createdAt,
      if (expiresAt != null) 'expires_at': expiresAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  ReceiptPrintJobsCompanion copyWith({
    Value<String>? id,
    Value<String?>? receiptId,
    Value<String>? routeKey,
    Value<String>? payloadJson,
    Value<String>? status,
    Value<DateTime>? createdAt,
    Value<DateTime>? expiresAt,
    Value<int>? rowid,
  }) {
    return ReceiptPrintJobsCompanion(
      id: id ?? this.id,
      receiptId: receiptId ?? this.receiptId,
      routeKey: routeKey ?? this.routeKey,
      payloadJson: payloadJson ?? this.payloadJson,
      status: status ?? this.status,
      createdAt: createdAt ?? this.createdAt,
      expiresAt: expiresAt ?? this.expiresAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (receiptId.present) {
      map['receipt_id'] = Variable<String>(receiptId.value);
    }
    if (routeKey.present) {
      map['route_key'] = Variable<String>(routeKey.value);
    }
    if (payloadJson.present) {
      map['payload_json'] = Variable<String>(payloadJson.value);
    }
    if (status.present) {
      map['status'] = Variable<String>(status.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (expiresAt.present) {
      map['expires_at'] = Variable<DateTime>(expiresAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('ReceiptPrintJobsCompanion(')
          ..write('id: $id, ')
          ..write('receiptId: $receiptId, ')
          ..write('routeKey: $routeKey, ')
          ..write('payloadJson: $payloadJson, ')
          ..write('status: $status, ')
          ..write('createdAt: $createdAt, ')
          ..write('expiresAt: $expiresAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

abstract class _$PosDatabase extends GeneratedDatabase {
  _$PosDatabase(QueryExecutor e) : super(e);
  $PosDatabaseManager get managers => $PosDatabaseManager(this);
  late final $DeviceBootstrapCachesTable deviceBootstrapCaches =
      $DeviceBootstrapCachesTable(this);
  late final $SyncOutboxEntriesTable syncOutboxEntries =
      $SyncOutboxEntriesTable(this);
  late final $ReceiptPrintJobsTable receiptPrintJobs = $ReceiptPrintJobsTable(
    this,
  );
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
    deviceBootstrapCaches,
    syncOutboxEntries,
    receiptPrintJobs,
  ];
}

typedef $$DeviceBootstrapCachesTableCreateCompanionBuilder =
    DeviceBootstrapCachesCompanion Function({
      required String id,
      Value<String?> apiBaseUrl,
      Value<String?> deviceId,
      Value<String?> merchantId,
      Value<String?> storeId,
      required String payloadJson,
      required DateTime syncedAt,
      Value<int> rowid,
    });
typedef $$DeviceBootstrapCachesTableUpdateCompanionBuilder =
    DeviceBootstrapCachesCompanion Function({
      Value<String> id,
      Value<String?> apiBaseUrl,
      Value<String?> deviceId,
      Value<String?> merchantId,
      Value<String?> storeId,
      Value<String> payloadJson,
      Value<DateTime> syncedAt,
      Value<int> rowid,
    });

class $$DeviceBootstrapCachesTableFilterComposer
    extends Composer<_$PosDatabase, $DeviceBootstrapCachesTable> {
  $$DeviceBootstrapCachesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get apiBaseUrl => $composableBuilder(
    column: $table.apiBaseUrl,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get deviceId => $composableBuilder(
    column: $table.deviceId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get merchantId => $composableBuilder(
    column: $table.merchantId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get storeId => $composableBuilder(
    column: $table.storeId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get payloadJson => $composableBuilder(
    column: $table.payloadJson,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get syncedAt => $composableBuilder(
    column: $table.syncedAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$DeviceBootstrapCachesTableOrderingComposer
    extends Composer<_$PosDatabase, $DeviceBootstrapCachesTable> {
  $$DeviceBootstrapCachesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get apiBaseUrl => $composableBuilder(
    column: $table.apiBaseUrl,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get deviceId => $composableBuilder(
    column: $table.deviceId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get merchantId => $composableBuilder(
    column: $table.merchantId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get storeId => $composableBuilder(
    column: $table.storeId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get payloadJson => $composableBuilder(
    column: $table.payloadJson,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get syncedAt => $composableBuilder(
    column: $table.syncedAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$DeviceBootstrapCachesTableAnnotationComposer
    extends Composer<_$PosDatabase, $DeviceBootstrapCachesTable> {
  $$DeviceBootstrapCachesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get apiBaseUrl => $composableBuilder(
    column: $table.apiBaseUrl,
    builder: (column) => column,
  );

  GeneratedColumn<String> get deviceId =>
      $composableBuilder(column: $table.deviceId, builder: (column) => column);

  GeneratedColumn<String> get merchantId => $composableBuilder(
    column: $table.merchantId,
    builder: (column) => column,
  );

  GeneratedColumn<String> get storeId =>
      $composableBuilder(column: $table.storeId, builder: (column) => column);

  GeneratedColumn<String> get payloadJson => $composableBuilder(
    column: $table.payloadJson,
    builder: (column) => column,
  );

  GeneratedColumn<DateTime> get syncedAt =>
      $composableBuilder(column: $table.syncedAt, builder: (column) => column);
}

class $$DeviceBootstrapCachesTableTableManager
    extends
        RootTableManager<
          _$PosDatabase,
          $DeviceBootstrapCachesTable,
          DeviceBootstrapCache,
          $$DeviceBootstrapCachesTableFilterComposer,
          $$DeviceBootstrapCachesTableOrderingComposer,
          $$DeviceBootstrapCachesTableAnnotationComposer,
          $$DeviceBootstrapCachesTableCreateCompanionBuilder,
          $$DeviceBootstrapCachesTableUpdateCompanionBuilder,
          (
            DeviceBootstrapCache,
            BaseReferences<
              _$PosDatabase,
              $DeviceBootstrapCachesTable,
              DeviceBootstrapCache
            >,
          ),
          DeviceBootstrapCache,
          PrefetchHooks Function()
        > {
  $$DeviceBootstrapCachesTableTableManager(
    _$PosDatabase db,
    $DeviceBootstrapCachesTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$DeviceBootstrapCachesTableFilterComposer(
                $db: db,
                $table: table,
              ),
          createOrderingComposer: () =>
              $$DeviceBootstrapCachesTableOrderingComposer(
                $db: db,
                $table: table,
              ),
          createComputedFieldComposer: () =>
              $$DeviceBootstrapCachesTableAnnotationComposer(
                $db: db,
                $table: table,
              ),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String?> apiBaseUrl = const Value.absent(),
                Value<String?> deviceId = const Value.absent(),
                Value<String?> merchantId = const Value.absent(),
                Value<String?> storeId = const Value.absent(),
                Value<String> payloadJson = const Value.absent(),
                Value<DateTime> syncedAt = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => DeviceBootstrapCachesCompanion(
                id: id,
                apiBaseUrl: apiBaseUrl,
                deviceId: deviceId,
                merchantId: merchantId,
                storeId: storeId,
                payloadJson: payloadJson,
                syncedAt: syncedAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                Value<String?> apiBaseUrl = const Value.absent(),
                Value<String?> deviceId = const Value.absent(),
                Value<String?> merchantId = const Value.absent(),
                Value<String?> storeId = const Value.absent(),
                required String payloadJson,
                required DateTime syncedAt,
                Value<int> rowid = const Value.absent(),
              }) => DeviceBootstrapCachesCompanion.insert(
                id: id,
                apiBaseUrl: apiBaseUrl,
                deviceId: deviceId,
                merchantId: merchantId,
                storeId: storeId,
                payloadJson: payloadJson,
                syncedAt: syncedAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$DeviceBootstrapCachesTableProcessedTableManager =
    ProcessedTableManager<
      _$PosDatabase,
      $DeviceBootstrapCachesTable,
      DeviceBootstrapCache,
      $$DeviceBootstrapCachesTableFilterComposer,
      $$DeviceBootstrapCachesTableOrderingComposer,
      $$DeviceBootstrapCachesTableAnnotationComposer,
      $$DeviceBootstrapCachesTableCreateCompanionBuilder,
      $$DeviceBootstrapCachesTableUpdateCompanionBuilder,
      (
        DeviceBootstrapCache,
        BaseReferences<
          _$PosDatabase,
          $DeviceBootstrapCachesTable,
          DeviceBootstrapCache
        >,
      ),
      DeviceBootstrapCache,
      PrefetchHooks Function()
    >;
typedef $$SyncOutboxEntriesTableCreateCompanionBuilder =
    SyncOutboxEntriesCompanion Function({
      required String localEventId,
      required String entityType,
      required String action,
      required String payloadJson,
      Value<String> status,
      Value<int> attempts,
      required DateTime createdAt,
      required DateTime availableAt,
      Value<int> rowid,
    });
typedef $$SyncOutboxEntriesTableUpdateCompanionBuilder =
    SyncOutboxEntriesCompanion Function({
      Value<String> localEventId,
      Value<String> entityType,
      Value<String> action,
      Value<String> payloadJson,
      Value<String> status,
      Value<int> attempts,
      Value<DateTime> createdAt,
      Value<DateTime> availableAt,
      Value<int> rowid,
    });

class $$SyncOutboxEntriesTableFilterComposer
    extends Composer<_$PosDatabase, $SyncOutboxEntriesTable> {
  $$SyncOutboxEntriesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get localEventId => $composableBuilder(
    column: $table.localEventId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get entityType => $composableBuilder(
    column: $table.entityType,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get action => $composableBuilder(
    column: $table.action,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get payloadJson => $composableBuilder(
    column: $table.payloadJson,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get status => $composableBuilder(
    column: $table.status,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get attempts => $composableBuilder(
    column: $table.attempts,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get availableAt => $composableBuilder(
    column: $table.availableAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$SyncOutboxEntriesTableOrderingComposer
    extends Composer<_$PosDatabase, $SyncOutboxEntriesTable> {
  $$SyncOutboxEntriesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get localEventId => $composableBuilder(
    column: $table.localEventId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get entityType => $composableBuilder(
    column: $table.entityType,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get action => $composableBuilder(
    column: $table.action,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get payloadJson => $composableBuilder(
    column: $table.payloadJson,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get status => $composableBuilder(
    column: $table.status,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get attempts => $composableBuilder(
    column: $table.attempts,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get availableAt => $composableBuilder(
    column: $table.availableAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$SyncOutboxEntriesTableAnnotationComposer
    extends Composer<_$PosDatabase, $SyncOutboxEntriesTable> {
  $$SyncOutboxEntriesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get localEventId => $composableBuilder(
    column: $table.localEventId,
    builder: (column) => column,
  );

  GeneratedColumn<String> get entityType => $composableBuilder(
    column: $table.entityType,
    builder: (column) => column,
  );

  GeneratedColumn<String> get action =>
      $composableBuilder(column: $table.action, builder: (column) => column);

  GeneratedColumn<String> get payloadJson => $composableBuilder(
    column: $table.payloadJson,
    builder: (column) => column,
  );

  GeneratedColumn<String> get status =>
      $composableBuilder(column: $table.status, builder: (column) => column);

  GeneratedColumn<int> get attempts =>
      $composableBuilder(column: $table.attempts, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<DateTime> get availableAt => $composableBuilder(
    column: $table.availableAt,
    builder: (column) => column,
  );
}

class $$SyncOutboxEntriesTableTableManager
    extends
        RootTableManager<
          _$PosDatabase,
          $SyncOutboxEntriesTable,
          SyncOutboxEntry,
          $$SyncOutboxEntriesTableFilterComposer,
          $$SyncOutboxEntriesTableOrderingComposer,
          $$SyncOutboxEntriesTableAnnotationComposer,
          $$SyncOutboxEntriesTableCreateCompanionBuilder,
          $$SyncOutboxEntriesTableUpdateCompanionBuilder,
          (
            SyncOutboxEntry,
            BaseReferences<
              _$PosDatabase,
              $SyncOutboxEntriesTable,
              SyncOutboxEntry
            >,
          ),
          SyncOutboxEntry,
          PrefetchHooks Function()
        > {
  $$SyncOutboxEntriesTableTableManager(
    _$PosDatabase db,
    $SyncOutboxEntriesTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$SyncOutboxEntriesTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$SyncOutboxEntriesTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$SyncOutboxEntriesTableAnnotationComposer(
                $db: db,
                $table: table,
              ),
          updateCompanionCallback:
              ({
                Value<String> localEventId = const Value.absent(),
                Value<String> entityType = const Value.absent(),
                Value<String> action = const Value.absent(),
                Value<String> payloadJson = const Value.absent(),
                Value<String> status = const Value.absent(),
                Value<int> attempts = const Value.absent(),
                Value<DateTime> createdAt = const Value.absent(),
                Value<DateTime> availableAt = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => SyncOutboxEntriesCompanion(
                localEventId: localEventId,
                entityType: entityType,
                action: action,
                payloadJson: payloadJson,
                status: status,
                attempts: attempts,
                createdAt: createdAt,
                availableAt: availableAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String localEventId,
                required String entityType,
                required String action,
                required String payloadJson,
                Value<String> status = const Value.absent(),
                Value<int> attempts = const Value.absent(),
                required DateTime createdAt,
                required DateTime availableAt,
                Value<int> rowid = const Value.absent(),
              }) => SyncOutboxEntriesCompanion.insert(
                localEventId: localEventId,
                entityType: entityType,
                action: action,
                payloadJson: payloadJson,
                status: status,
                attempts: attempts,
                createdAt: createdAt,
                availableAt: availableAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$SyncOutboxEntriesTableProcessedTableManager =
    ProcessedTableManager<
      _$PosDatabase,
      $SyncOutboxEntriesTable,
      SyncOutboxEntry,
      $$SyncOutboxEntriesTableFilterComposer,
      $$SyncOutboxEntriesTableOrderingComposer,
      $$SyncOutboxEntriesTableAnnotationComposer,
      $$SyncOutboxEntriesTableCreateCompanionBuilder,
      $$SyncOutboxEntriesTableUpdateCompanionBuilder,
      (
        SyncOutboxEntry,
        BaseReferences<_$PosDatabase, $SyncOutboxEntriesTable, SyncOutboxEntry>,
      ),
      SyncOutboxEntry,
      PrefetchHooks Function()
    >;
typedef $$ReceiptPrintJobsTableCreateCompanionBuilder =
    ReceiptPrintJobsCompanion Function({
      required String id,
      Value<String?> receiptId,
      required String routeKey,
      required String payloadJson,
      Value<String> status,
      required DateTime createdAt,
      required DateTime expiresAt,
      Value<int> rowid,
    });
typedef $$ReceiptPrintJobsTableUpdateCompanionBuilder =
    ReceiptPrintJobsCompanion Function({
      Value<String> id,
      Value<String?> receiptId,
      Value<String> routeKey,
      Value<String> payloadJson,
      Value<String> status,
      Value<DateTime> createdAt,
      Value<DateTime> expiresAt,
      Value<int> rowid,
    });

class $$ReceiptPrintJobsTableFilterComposer
    extends Composer<_$PosDatabase, $ReceiptPrintJobsTable> {
  $$ReceiptPrintJobsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get receiptId => $composableBuilder(
    column: $table.receiptId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get routeKey => $composableBuilder(
    column: $table.routeKey,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get payloadJson => $composableBuilder(
    column: $table.payloadJson,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get status => $composableBuilder(
    column: $table.status,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get expiresAt => $composableBuilder(
    column: $table.expiresAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$ReceiptPrintJobsTableOrderingComposer
    extends Composer<_$PosDatabase, $ReceiptPrintJobsTable> {
  $$ReceiptPrintJobsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get receiptId => $composableBuilder(
    column: $table.receiptId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get routeKey => $composableBuilder(
    column: $table.routeKey,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get payloadJson => $composableBuilder(
    column: $table.payloadJson,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get status => $composableBuilder(
    column: $table.status,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get expiresAt => $composableBuilder(
    column: $table.expiresAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$ReceiptPrintJobsTableAnnotationComposer
    extends Composer<_$PosDatabase, $ReceiptPrintJobsTable> {
  $$ReceiptPrintJobsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get receiptId =>
      $composableBuilder(column: $table.receiptId, builder: (column) => column);

  GeneratedColumn<String> get routeKey =>
      $composableBuilder(column: $table.routeKey, builder: (column) => column);

  GeneratedColumn<String> get payloadJson => $composableBuilder(
    column: $table.payloadJson,
    builder: (column) => column,
  );

  GeneratedColumn<String> get status =>
      $composableBuilder(column: $table.status, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<DateTime> get expiresAt =>
      $composableBuilder(column: $table.expiresAt, builder: (column) => column);
}

class $$ReceiptPrintJobsTableTableManager
    extends
        RootTableManager<
          _$PosDatabase,
          $ReceiptPrintJobsTable,
          ReceiptPrintJob,
          $$ReceiptPrintJobsTableFilterComposer,
          $$ReceiptPrintJobsTableOrderingComposer,
          $$ReceiptPrintJobsTableAnnotationComposer,
          $$ReceiptPrintJobsTableCreateCompanionBuilder,
          $$ReceiptPrintJobsTableUpdateCompanionBuilder,
          (
            ReceiptPrintJob,
            BaseReferences<
              _$PosDatabase,
              $ReceiptPrintJobsTable,
              ReceiptPrintJob
            >,
          ),
          ReceiptPrintJob,
          PrefetchHooks Function()
        > {
  $$ReceiptPrintJobsTableTableManager(
    _$PosDatabase db,
    $ReceiptPrintJobsTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$ReceiptPrintJobsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$ReceiptPrintJobsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$ReceiptPrintJobsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String?> receiptId = const Value.absent(),
                Value<String> routeKey = const Value.absent(),
                Value<String> payloadJson = const Value.absent(),
                Value<String> status = const Value.absent(),
                Value<DateTime> createdAt = const Value.absent(),
                Value<DateTime> expiresAt = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => ReceiptPrintJobsCompanion(
                id: id,
                receiptId: receiptId,
                routeKey: routeKey,
                payloadJson: payloadJson,
                status: status,
                createdAt: createdAt,
                expiresAt: expiresAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                Value<String?> receiptId = const Value.absent(),
                required String routeKey,
                required String payloadJson,
                Value<String> status = const Value.absent(),
                required DateTime createdAt,
                required DateTime expiresAt,
                Value<int> rowid = const Value.absent(),
              }) => ReceiptPrintJobsCompanion.insert(
                id: id,
                receiptId: receiptId,
                routeKey: routeKey,
                payloadJson: payloadJson,
                status: status,
                createdAt: createdAt,
                expiresAt: expiresAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$ReceiptPrintJobsTableProcessedTableManager =
    ProcessedTableManager<
      _$PosDatabase,
      $ReceiptPrintJobsTable,
      ReceiptPrintJob,
      $$ReceiptPrintJobsTableFilterComposer,
      $$ReceiptPrintJobsTableOrderingComposer,
      $$ReceiptPrintJobsTableAnnotationComposer,
      $$ReceiptPrintJobsTableCreateCompanionBuilder,
      $$ReceiptPrintJobsTableUpdateCompanionBuilder,
      (
        ReceiptPrintJob,
        BaseReferences<_$PosDatabase, $ReceiptPrintJobsTable, ReceiptPrintJob>,
      ),
      ReceiptPrintJob,
      PrefetchHooks Function()
    >;

class $PosDatabaseManager {
  final _$PosDatabase _db;
  $PosDatabaseManager(this._db);
  $$DeviceBootstrapCachesTableTableManager get deviceBootstrapCaches =>
      $$DeviceBootstrapCachesTableTableManager(_db, _db.deviceBootstrapCaches);
  $$SyncOutboxEntriesTableTableManager get syncOutboxEntries =>
      $$SyncOutboxEntriesTableTableManager(_db, _db.syncOutboxEntries);
  $$ReceiptPrintJobsTableTableManager get receiptPrintJobs =>
      $$ReceiptPrintJobsTableTableManager(_db, _db.receiptPrintJobs);
}
