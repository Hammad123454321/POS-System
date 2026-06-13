import 'package:flutter/material.dart';

import '../../app/pos_theme.dart';
import '../../core/models/pos_models.dart';
import '../home/pos_home_controller.dart';

/// Appointment day-view: a date header plus per-staff columns of appointment
/// cards for the selected day. Tapping a card offers contextual check-in /
/// complete. A FAB opens the booking dialog.
class AppointmentsScreen extends StatefulWidget {
  const AppointmentsScreen({required this.controller, super.key});

  final PosHomeController controller;

  @override
  State<AppointmentsScreen> createState() => _AppointmentsScreenState();
}

class _AppointmentsScreenState extends State<AppointmentsScreen> {
  DateTime _day = DateTime.now();

  bool _sameDay(String iso) {
    final dt = DateTime.tryParse(iso)?.toLocal();
    if (dt == null) return false;
    return dt.year == _day.year && dt.month == _day.month && dt.day == _day.day;
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final staff = widget.controller.workforceStaff;
        final dayAppointments = widget.controller.appointments
            .where((a) => _sameDay(a.startsAt))
            .toList();

        return Scaffold(
          body: Column(
            children: [
              _DateHeader(
                day: _day,
                onPrev: () => setState(
                  () => _day = _day.subtract(const Duration(days: 1)),
                ),
                onNext: () =>
                    setState(() => _day = _day.add(const Duration(days: 1))),
                onToday: () => setState(() => _day = DateTime.now()),
              ),
              const Divider(height: 1),
              Expanded(
                child: staff.isEmpty
                    ? const Center(child: Text('No staff configured.'))
                    : ListView(
                        children: [
                          for (final member in staff)
                            _StaffColumn(
                              staff: member,
                              appointments: dayAppointments
                                  .where((a) => a.staffProfileId == member.id)
                                  .toList(),
                              onTap: (a) => _onAppointmentTap(context, a),
                            ),
                          _UnassignedColumn(
                            appointments: dayAppointments
                                .where((a) => a.staffProfileId == null)
                                .toList(),
                            onTap: (a) => _onAppointmentTap(context, a),
                          ),
                        ],
                      ),
              ),
            ],
          ),
          floatingActionButton: FloatingActionButton.extended(
            onPressed: () => _openBookingDialog(context),
            icon: const Icon(Icons.add),
            label: const Text('Book'),
          ),
        );
      },
    );
  }

  Future<void> _onAppointmentTap(
    BuildContext context,
    AppointmentSnapshot a,
  ) async {
    await showModalBottomSheet<void>(
      context: context,
      builder: (sheetContext) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: Text(a.customerName ?? 'Appointment'),
              subtitle: Text('Status: ${a.status}'),
            ),
            if (a.status == 'confirmed' || a.status == 'booked')
              ListTile(
                leading: const Icon(Icons.login),
                title: const Text('Check in'),
                onTap: () {
                  Navigator.pop(sheetContext);
                  widget.controller.checkInAppointment(a.id);
                },
              ),
            if (a.status == 'checked_in')
              ListTile(
                leading: const Icon(Icons.check),
                title: const Text('Complete'),
                onTap: () {
                  Navigator.pop(sheetContext);
                  widget.controller.completeAppointment(a.id);
                },
              ),
          ],
        ),
      ),
    );
  }

  Future<void> _openBookingDialog(BuildContext context) async {
    final staff = widget.controller.workforceStaff;
    if (staff.isEmpty) return;

    String? staffId = staff.first.id;
    final serviceController = TextEditingController();
    TimeOfDay start = const TimeOfDay(hour: 9, minute: 0);
    int durationMinutes = 30;

    await showDialog<void>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setLocal) => AlertDialog(
          title: const Text('Book appointment'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                DropdownButtonFormField<String>(
                  initialValue: staffId,
                  decoration: const InputDecoration(labelText: 'Staff'),
                  items: [
                    for (final s in staff)
                      DropdownMenuItem(value: s.id, child: Text(s.displayName)),
                  ],
                  onChanged: (v) => setLocal(() => staffId = v),
                ),
                TextField(
                  controller: serviceController,
                  decoration: const InputDecoration(
                    labelText: 'Service item ID',
                  ),
                ),
                Row(
                  children: [
                    Expanded(child: Text('Start: ${start.format(context)}')),
                    TextButton(
                      onPressed: () async {
                        final picked = await showTimePicker(
                          context: context,
                          initialTime: start,
                        );
                        if (picked != null) setLocal(() => start = picked);
                      },
                      child: const Text('Pick time'),
                    ),
                  ],
                ),
                DropdownButtonFormField<int>(
                  initialValue: durationMinutes,
                  decoration: const InputDecoration(labelText: 'Duration'),
                  items: const [
                    DropdownMenuItem(value: 15, child: Text('15 min')),
                    DropdownMenuItem(value: 30, child: Text('30 min')),
                    DropdownMenuItem(value: 45, child: Text('45 min')),
                    DropdownMenuItem(value: 60, child: Text('60 min')),
                  ],
                  onChanged: (v) => setLocal(() => durationMinutes = v ?? 30),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () {
                final startsAt = DateTime(
                  _day.year,
                  _day.month,
                  _day.day,
                  start.hour,
                  start.minute,
                );
                final endsAt = startsAt.add(Duration(minutes: durationMinutes));
                Navigator.pop(context);
                widget.controller.bookAppointment(
                  staffProfileId: staffId!,
                  serviceItemId: serviceController.text.trim(),
                  startsAt: startsAt.toUtc().toIso8601String(),
                  endsAt: endsAt.toUtc().toIso8601String(),
                );
              },
              child: const Text('Book'),
            ),
          ],
        ),
      ),
    );
  }
}

class _DateHeader extends StatelessWidget {
  const _DateHeader({
    required this.day,
    required this.onPrev,
    required this.onNext,
    required this.onToday,
  });

  final DateTime day;
  final VoidCallback onPrev;
  final VoidCallback onNext;
  final VoidCallback onToday;

  @override
  Widget build(BuildContext context) {
    final label =
        '${day.year}-${day.month.toString().padLeft(2, '0')}-${day.day.toString().padLeft(2, '0')}';
    return Padding(
      padding: const EdgeInsets.all(8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          IconButton(onPressed: onPrev, icon: const Icon(Icons.chevron_left)),
          TextButton(onPressed: onToday, child: Text(label)),
          IconButton(onPressed: onNext, icon: const Icon(Icons.chevron_right)),
        ],
      ),
    );
  }
}

class _StaffColumn extends StatelessWidget {
  const _StaffColumn({
    required this.staff,
    required this.appointments,
    required this.onTap,
  });

  final WorkforceStaffSnapshot staff;
  final List<AppointmentSnapshot> appointments;
  final ValueChanged<AppointmentSnapshot> onTap;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Container(
          color: PosColors.surface,
          padding: const EdgeInsets.all(8),
          child: Text(
            staff.displayName,
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
        ),
        if (appointments.isEmpty)
          const Padding(
            padding: EdgeInsets.all(8),
            child: Text(
              'No appointments',
              style: TextStyle(color: Colors.grey),
            ),
          )
        else
          for (final a in appointments)
            _AppointmentCard(appointment: a, onTap: () => onTap(a)),
      ],
    );
  }
}

class _UnassignedColumn extends StatelessWidget {
  const _UnassignedColumn({required this.appointments, required this.onTap});

  final List<AppointmentSnapshot> appointments;
  final ValueChanged<AppointmentSnapshot> onTap;

  @override
  Widget build(BuildContext context) {
    if (appointments.isEmpty) return const SizedBox.shrink();
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Container(
          color: PosColors.surface,
          padding: const EdgeInsets.all(8),
          child: const Text(
            'Unassigned',
            style: TextStyle(fontWeight: FontWeight.bold),
          ),
        ),
        for (final a in appointments)
          _AppointmentCard(appointment: a, onTap: () => onTap(a)),
      ],
    );
  }
}

class _AppointmentCard extends StatelessWidget {
  const _AppointmentCard({required this.appointment, required this.onTap});

  final AppointmentSnapshot appointment;
  final VoidCallback onTap;

  Color get _color => switch (appointment.status) {
    'checked_in' => PosColors.apptCheckedIn,
    'completed' => PosColors.apptCompleted,
    'cancelled' => PosColors.apptCancelled,
    _ => PosColors.apptBooked,
  };

  @override
  Widget build(BuildContext context) {
    final start = DateTime.tryParse(appointment.startsAt)?.toLocal();
    final timeLabel = start == null
        ? ''
        : '${start.hour.toString().padLeft(2, '0')}:${start.minute.toString().padLeft(2, '0')}';
    return Card(
      color: _color.withValues(alpha: 0.15),
      child: ListTile(
        leading: CircleAvatar(backgroundColor: _color, radius: 6),
        title: Text(appointment.customerName ?? 'Appointment'),
        subtitle: Text('$timeLabel · ${appointment.status}'),
        onTap: onTap,
      ),
    );
  }
}
