import 'package:flutter_test/flutter_test.dart';
import 'package:pos_app/src/data/local/pos_database.dart';
import 'package:pos_app/src/data/repositories/sync_outbox_repository.dart';

void main() {
  test(
    'auth hold events can be counted and released back to pending',
    () async {
      final database = PosDatabase.memory();
      final repository = SyncOutboxRepository(database);

      await repository.enqueue(
        localEventId: 'event-1',
        entityType: 'order',
        action: 'created',
        payload: const {'order_id': 'order-1'},
      );
      await repository.enqueue(
        localEventId: 'event-2',
        entityType: 'order',
        action: 'paid',
        payload: const {'order_id': 'order-2'},
      );

      await repository.movePendingToAuthHold();

      expect(await repository.pendingCount(), 0);
      expect(await repository.authHoldCount(), 2);

      await repository.releaseAuthHoldToPending();

      expect(await repository.pendingCount(), 2);
      expect(await repository.authHoldCount(), 0);

      await database.close();
    },
  );
}
