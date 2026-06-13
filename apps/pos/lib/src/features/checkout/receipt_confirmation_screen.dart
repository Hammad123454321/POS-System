import 'package:flutter/material.dart';

import '../../core/support/money_format.dart';
import '../home/pos_home_controller.dart';

/// Shown after a successful tender. Offers receipt options (print again, email,
/// none) and a New Sale action. The cart is already cleared by the controller.
class ReceiptConfirmationScreen extends StatefulWidget {
  const ReceiptConfirmationScreen({required this.controller, super.key});

  final PosHomeController controller;

  @override
  State<ReceiptConfirmationScreen> createState() =>
      _ReceiptConfirmationScreenState();
}

class _ReceiptConfirmationScreenState extends State<ReceiptConfirmationScreen> {
  final _emailController = TextEditingController();

  @override
  void dispose() {
    _emailController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final receipt = widget.controller.lastReceipt;
    final total = (receipt?.payload['total_minor'] as int?) ?? 0;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Sale complete'),
        automaticallyImplyLeading: false,
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const Icon(Icons.check_circle, color: Colors.green, size: 64),
          const SizedBox(height: 12),
          Center(
            child: Text(
              receipt?.receiptNumber ?? 'Receipt',
              style: Theme.of(context).textTheme.titleLarge,
            ),
          ),
          if (total > 0) ...[
            const SizedBox(height: 4),
            Center(child: Text('Total ${formatMinor(total)}')),
          ],
          const SizedBox(height: 24),
          OutlinedButton.icon(
            onPressed: () => widget.controller.printPendingReceipts(),
            icon: const Icon(Icons.print),
            label: const Text('Print again'),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _emailController,
            keyboardType: TextInputType.emailAddress,
            decoration: const InputDecoration(
              labelText: 'Email receipt to…',
              border: OutlineInputBorder(),
              suffixIcon: Icon(Icons.email_outlined),
            ),
          ),
          const SizedBox(height: 8),
          OutlinedButton(
            onPressed: () {
              final email = _emailController.text.trim();
              if (email.isNotEmpty && receipt != null) {
                widget.controller.emailReceipt(receipt.receiptId, email);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Receipt emailed to $email')),
                );
              }
            },
            child: const Text('Email receipt'),
          ),
          const SizedBox(height: 24),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('New sale'),
          ),
        ],
      ),
    );
  }
}
