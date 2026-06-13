import 'package:flutter/material.dart';

import '../../app/pos_theme.dart';
import '../../core/models/pos_models.dart';
import '../../core/support/money_format.dart';
import '../home/pos_home_controller.dart';

/// Cashier register: a category-filtered product grid on the left and a cart
/// panel on the right (wide) or a bottom total bar opening a cart sheet (narrow).
class RegisterScreen extends StatefulWidget {
  const RegisterScreen({
    required this.controller,
    this.onCharge,
    super.key,
  });

  final PosHomeController controller;

  /// Invoked when the cashier taps Charge with a non-empty cart and an open
  /// register. The shell wires this to push the tender flow.
  final VoidCallback? onCharge;

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  String _category = 'All';
  String _search = '';

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final isWide = MediaQuery.sizeOf(context).width >= 840;
        final productArea = _ProductArea(
          controller: widget.controller,
          category: _category,
          search: _search,
          onCategory: (c) => setState(() => _category = c),
          onSearch: (s) => setState(() => _search = s),
        );

        if (isWide) {
          return Row(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Expanded(flex: 3, child: productArea),
              SizedBox(
                width: 380,
                child: _CartPanel(
                  controller: widget.controller,
                  onCharge: widget.onCharge,
                ),
              ),
            ],
          );
        }

        return Column(
          children: [
            Expanded(child: productArea),
            _TotalBar(
              controller: widget.controller,
              onOpenCart: () => _openCartSheet(context),
              onCharge: widget.onCharge,
            ),
          ],
        );
      },
    );
  }

  void _openCartSheet(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (_) => SizedBox(
        height: MediaQuery.sizeOf(context).height * 0.85,
        child: _CartPanel(
          controller: widget.controller,
          onCharge: widget.onCharge,
        ),
      ),
    );
  }
}

class _ProductArea extends StatelessWidget {
  const _ProductArea({
    required this.controller,
    required this.category,
    required this.search,
    required this.onCategory,
    required this.onSearch,
  });

  final PosHomeController controller;
  final String category;
  final String search;
  final ValueChanged<String> onCategory;
  final ValueChanged<String> onSearch;

  @override
  Widget build(BuildContext context) {
    final categories = ['All', ...controller.catalogCategories];
    final term = search.trim().toLowerCase();
    final items = controller.catalogItems.where((item) {
      final matchesCategory = category == 'All' || item.categoryName == category;
      final matchesSearch =
          term.isEmpty || item.name.toLowerCase().contains(term);
      return matchesCategory && matchesSearch;
    }).toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Padding(
          padding: const EdgeInsets.all(12),
          child: TextField(
            decoration: const InputDecoration(
              prefixIcon: Icon(Icons.search),
              hintText: 'Search items',
              isDense: true,
              border: OutlineInputBorder(),
            ),
            onChanged: onSearch,
          ),
        ),
        SizedBox(
          height: 44,
          child: ListView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 12),
            children: [
              for (final c in categories)
                Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: ChoiceChip(
                    label: Text(c),
                    selected: c == category,
                    onSelected: (_) => onCategory(c),
                  ),
                ),
            ],
          ),
        ),
        Expanded(
          child: items.isEmpty
              ? const Center(child: Text('No items'))
              : GridView.builder(
                  padding: const EdgeInsets.all(12),
                  gridDelegate:
                      const SliverGridDelegateWithMaxCrossAxisExtent(
                    maxCrossAxisExtent: 180,
                    childAspectRatio: 1.1,
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                  ),
                  itemCount: items.length,
                  itemBuilder: (context, i) => _ProductTile(
                    item: items[i],
                    quantity: controller.quantityOf(items[i].id),
                    onTap: () => controller.addItem(items[i]),
                  ),
                ),
        ),
      ],
    );
  }
}

class _ProductTile extends StatelessWidget {
  const _ProductTile({
    required this.item,
    required this.quantity,
    required this.onTap,
  });

  final CatalogItemSnapshot item;
  final int quantity;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: item.soldOut ? null : onTap,
        child: Stack(
          children: [
            Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Flexible(
                    child: Text(
                      item.name,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(fontWeight: FontWeight.w600),
                    ),
                  ),
                  Text(
                    formatMinor(item.effectivePriceMinor,
                        currency: item.currency),
                    style: const TextStyle(color: PosColors.forest),
                  ),
                ],
              ),
            ),
            if (item.soldOut)
              Positioned.fill(
                child: Container(
                  color: Colors.black.withValues(alpha: 0.45),
                  alignment: Alignment.center,
                  child: const Text('SOLD OUT',
                      style: TextStyle(color: Colors.white)),
                ),
              ),
            if (quantity > 0)
              Positioned(
                top: 6,
                right: 6,
                child: CircleAvatar(
                  radius: 12,
                  backgroundColor: PosColors.forest,
                  child: Text('$quantity',
                      style: const TextStyle(
                          color: Colors.white, fontSize: 12)),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _CartPanel extends StatelessWidget {
  const _CartPanel({required this.controller, this.onCharge});

  final PosHomeController controller;
  final VoidCallback? onCharge;

  @override
  Widget build(BuildContext context) {
    final lines = controller.cartLines;
    final hasOpenRegister = controller.activeRegisterSession != null;
    final total = controller.cartEstimatedTotalMinor;

    return Material(
      color: Theme.of(context).colorScheme.surface,
      child: Column(
        children: [
          ListTile(
            title: const Text('Cart'),
            trailing: lines.isEmpty
                ? null
                : TextButton(
                    onPressed: controller.clearCart,
                    child: const Text('Clear'),
                  ),
          ),
          const Divider(height: 1),
          Expanded(
            child: lines.isEmpty
                ? const Center(child: Text('Cart is empty'))
                : ListView.separated(
                    itemCount: lines.length,
                    separatorBuilder: (_, _) => const Divider(height: 1),
                    itemBuilder: (context, i) =>
                        _CartLineTile(controller: controller, line: lines[i]),
                  ),
          ),
          const Divider(height: 1),
          _TotalsBlock(controller: controller),
          Padding(
            padding: const EdgeInsets.all(12),
            child: SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: (lines.isEmpty || controller.isBusy)
                    ? null
                    : onCharge,
                child: Text(
                  hasOpenRegister
                      ? 'Charge ${formatMinor(total)}'
                      : 'Open Register',
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _CartLineTile extends StatelessWidget {
  const _CartLineTile({required this.controller, required this.line});

  final PosHomeController controller;
  final CartLineSnapshot line;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      dense: true,
      title: Text(line.item.name),
      subtitle: Text(formatMinor(line.lineSubtotalMinor,
          currency: line.item.currency)),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          IconButton(
            icon: const Icon(Icons.remove_circle_outline),
            onPressed: () => controller.removeItem(line.item),
          ),
          Text('${line.quantity}'),
          IconButton(
            icon: const Icon(Icons.add_circle_outline),
            onPressed: () => controller.addItem(line.item),
          ),
          IconButton(
            icon: const Icon(Icons.delete_outline),
            onPressed: () => controller.removeLine(line.item),
          ),
        ],
      ),
    );
  }
}

class _TotalsBlock extends StatelessWidget {
  const _TotalsBlock({required this.controller});

  final PosHomeController controller;

  @override
  Widget build(BuildContext context) {
    Widget row(String label, int minor, {bool bold = false}) {
      final style = bold
          ? const TextStyle(fontWeight: FontWeight.bold)
          : const TextStyle();
      return Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 2),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: style),
            Text(formatMinor(minor), style: style),
          ],
        ),
      );
    }

    return Column(
      children: [
        const SizedBox(height: 8),
        row('Subtotal', controller.cartEstimatedSubtotalMinor),
        row('Discount', -controller.cartEstimatedDiscountMinor),
        row('Tax', controller.cartEstimatedTaxMinor),
        row('Total', controller.cartEstimatedTotalMinor, bold: true),
        const SizedBox(height: 8),
      ],
    );
  }
}

class _TotalBar extends StatelessWidget {
  const _TotalBar({
    required this.controller,
    required this.onOpenCart,
    this.onCharge,
  });

  final PosHomeController controller;
  final VoidCallback onOpenCart;
  final VoidCallback? onCharge;

  @override
  Widget build(BuildContext context) {
    final lines = controller.cartLines;
    final hasOpenRegister = controller.activeRegisterSession != null;

    return Material(
      elevation: 8,
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            Expanded(
              child: TextButton.icon(
                onPressed: onOpenCart,
                icon: const Icon(Icons.shopping_cart_outlined),
                label: Text('${lines.length} items · '
                    '${formatMinor(controller.cartEstimatedTotalMinor)}'),
              ),
            ),
            FilledButton(
              onPressed:
                  (lines.isEmpty || controller.isBusy) ? null : onCharge,
              child: Text(hasOpenRegister ? 'Charge' : 'Open Register'),
            ),
          ],
        ),
      ),
    );
  }
}
