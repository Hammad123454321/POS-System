import 'package:flutter/material.dart';

import '../../core/support/money_format.dart';
import '../home/pos_home_controller.dart';
import 'receipt_confirmation_screen.dart';

enum _TenderKind { cash, card, split, giftCard }

/// Multi-tender checkout. Filters available methods by the store's payment
/// capabilities. On success (detected by a new lastReceipt id) it replaces
/// itself with the receipt confirmation screen.
class TenderScreen extends StatefulWidget {
  const TenderScreen({required this.controller, super.key});

  final PosHomeController controller;

  @override
  State<TenderScreen> createState() => _TenderScreenState();
}

class _TenderScreenState extends State<TenderScreen> {
  late _TenderKind _kind;
  final _cashController = TextEditingController();
  final _cardTipController = TextEditingController();
  final _giftController = TextEditingController();
  int _tipPercent = 0;

  @override
  void initState() {
    super.initState();
    _kind = _available().first;
  }

  @override
  void dispose() {
    _cashController.dispose();
    _cardTipController.dispose();
    _giftController.dispose();
    super.dispose();
  }

  List<_TenderKind> _available() {
    final caps = widget.controller.paymentCapabilities;
    final supported = caps?.supportedTenders ?? const ['cash'];
    final kinds = <_TenderKind>[];
    if (supported.contains('cash')) kinds.add(_TenderKind.cash);
    if (supported.contains('card')) kinds.add(_TenderKind.card);
    if (supported.contains('cash') && supported.contains('card')) {
      kinds.add(_TenderKind.split);
    }
    if (supported.contains('gift_card')) kinds.add(_TenderKind.giftCard);
    return kinds.isEmpty ? [_TenderKind.cash] : kinds;
  }

  Future<void> _run(Future<void> Function() action) async {
    final before = widget.controller.lastReceipt?.receiptId;
    await action();
    final after = widget.controller.lastReceipt?.receiptId;

    if (!mounted) return;

    if (after != null && after != before) {
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(
          builder: (_) =>
              ReceiptConfirmationScreen(controller: widget.controller),
        ),
      );
    } else if (widget.controller.errorMessage != null) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(widget.controller.errorMessage!)));
    }
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final total = widget.controller.cartEstimatedTotalMinor;
        final available = _available();
        if (!available.contains(_kind)) _kind = available.first;

        return Scaffold(
          appBar: AppBar(title: Text('Charge ${formatMinor(total)}')),
          body: AbsorbPointer(
            absorbing: widget.controller.isBusy,
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                SegmentedButton<_TenderKind>(
                  segments: [
                    for (final k in available)
                      ButtonSegment(value: k, label: Text(_label(k))),
                  ],
                  selected: {_kind},
                  onSelectionChanged: (s) => setState(() => _kind = s.first),
                ),
                const SizedBox(height: 20),
                _body(total),
                if (widget.controller.isBusy) ...[
                  const SizedBox(height: 24),
                  const Center(child: CircularProgressIndicator()),
                  const SizedBox(height: 8),
                  const Center(child: Text('Follow prompts on the terminal…')),
                ],
                if (widget.controller.inDoubtOrderId != null) ...[
                  const SizedBox(height: 24),
                  Card(
                    color: Theme.of(context).colorScheme.errorContainer,
                    child: const Padding(
                      padding: EdgeInsets.all(16),
                      child: Text(
                        'Card transaction in doubt. A manager must run terminal '
                        'recovery before retrying. Retry is blocked.',
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ),
        );
      },
    );
  }

  String _label(_TenderKind k) => switch (k) {
    _TenderKind.cash => 'Cash',
    _TenderKind.card => 'Card',
    _TenderKind.split => 'Split',
    _TenderKind.giftCard => 'Gift Card',
  };

  Widget _body(int total) {
    switch (_kind) {
      case _TenderKind.cash:
        return _cashBody(total);
      case _TenderKind.card:
        return _cardBody(total);
      case _TenderKind.split:
        return _splitBody(total);
      case _TenderKind.giftCard:
        return _giftBody();
    }
  }

  Widget _cashBody(int total) {
    final quick = _quickAmounts(total);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Wrap(
          spacing: 8,
          children: [
            for (final amount in quick)
              OutlinedButton(
                onPressed: () => _cashController.text = amount.toString(),
                child: Text(formatMinor(amount)),
              ),
          ],
        ),
        const SizedBox(height: 12),
        TextField(
          controller: _cashController,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(
            labelText: 'Cash tendered (minor units)',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 16),
        FilledButton(
          onPressed: () {
            final tendered = int.tryParse(_cashController.text.trim()) ?? 0;
            _run(() => widget.controller.checkoutCash(tendered));
          },
          child: const Text('Complete cash sale'),
        ),
      ],
    );
  }

  Widget _cardBody(int total) {
    final tip = _tipPercent == 0 ? 0 : (total * _tipPercent) ~/ 100;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const Text('Tip'),
        Wrap(
          spacing: 8,
          children: [
            for (final p in [0, 10, 15, 20])
              ChoiceChip(
                label: Text(p == 0 ? 'No tip' : '$p%'),
                selected: _tipPercent == p,
                onSelected: (_) => setState(() => _tipPercent = p),
              ),
          ],
        ),
        const SizedBox(height: 8),
        Text('Tip: ${formatMinor(tip)}'),
        const SizedBox(height: 16),
        FilledButton(
          onPressed: () => _run(() => widget.controller.checkoutCard(tip)),
          child: Text('Charge card ${formatMinor(total + tip)}'),
        ),
      ],
    );
  }

  Widget _splitBody(int total) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: _cashController,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(
            labelText: 'Cash applied (minor units)',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: _cardTipController,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(
            labelText: 'Card tip (minor units)',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 16),
        FilledButton(
          onPressed: () {
            final cash = int.tryParse(_cashController.text.trim()) ?? 0;
            final tip = int.tryParse(_cardTipController.text.trim()) ?? 0;
            _run(
              () => widget.controller.checkoutSplitCashCard(
                cashAppliedMinor: cash,
                cashTenderedMinor: cash,
                cardTipMinor: tip,
              ),
            );
          },
          child: const Text('Complete split sale'),
        ),
      ],
    );
  }

  Widget _giftBody() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: _giftController,
          decoration: const InputDecoration(
            labelText: 'Gift card code',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 16),
        FilledButton(
          onPressed: () => _run(
            () =>
                widget.controller.checkoutGiftCard(_giftController.text.trim()),
          ),
          child: const Text('Redeem gift card'),
        ),
      ],
    );
  }

  /// Exact total, then the next whole 1/5/10/20 major-unit amounts above it.
  List<int> _quickAmounts(int total) {
    final amounts = <int>{total};
    for (final step in [100, 500, 1000, 2000]) {
      amounts.add(((total / step).ceil()) * step);
    }
    final sorted = amounts.toList()..sort();
    return sorted;
  }
}
