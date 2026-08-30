/**
 * MediTime Real-Time Medicine Reminder & Alarm Polling System
 */

class MedicineReminderSystem {
    constructor() {
        this.activeAlarmSchedule = null;
        this.acknowledgedAlarms = new Set();
        this.pollingInterval = null;
        this.countdownInterval = null;
        this.nextDoseTarget = null;
        this.soundSettings = {
            soundEnabled: true,
            volume: 80,
            snoozeMinutes: 10,
            alarmSound: 'medical_beep'
        };

        this.init();
    }

    init() {
        // Guard check: only run on patient pages
        if (!window.location.pathname.startsWith('/patient')) {
            return;
        }

        this.loadSettings();
        this.startSchedulePolling();
        this.initNextDoseCountdown();
        this.bindGlobalActions();
    }

    loadSettings() {
        fetch('/api/settings/reminder')
            .then(res => {
                if (res.status === 401 || res.status === 403) return null;
                return res.json();
            })
            .then(data => {
                if (data && data.success && data.data) {
                    this.soundSettings = data.data;
                    if (window.soundEngine) {
                        window.soundEngine.setVolume(this.soundSettings.volume);
                        window.soundEngine.setSoundType(this.soundSettings.alarmSound);
                    }
                }
            })
            .catch(err => console.debug("Settings load note:", err));
    }

    startSchedulePolling() {
        this.checkDueMedicines();
        this.pollingInterval = setInterval(() => this.checkDueMedicines(), 5000);
    }

    checkDueMedicines() {
        fetch('/api/schedules/active-alarms')
            .then(res => {
                if (res.status === 401 || res.status === 403) return null;
                return res.json();
            })
            .then(response => {
                if (response && response.success && response.data && response.data.length > 0) {
                    const dueSchedule = response.data.find(s => !this.acknowledgedAlarms.has(s.id));
                    if (dueSchedule) {
                        this.triggerAlarm(dueSchedule);
                    }
                }
            })
            .catch(err => console.debug("Alarm check error:", err));
    }

    triggerAlarm(schedule) {
        if (this.activeAlarmSchedule && this.activeAlarmSchedule.id === schedule.id) {
            return;
        }

        this.activeAlarmSchedule = schedule;

        if (this.soundSettings.soundEnabled && window.soundEngine) {
            window.soundEngine.setSoundType(this.soundSettings.alarmSound);
            window.soundEngine.setVolume(this.soundSettings.volume);
            window.soundEngine.startAlarm();
        }

        const modalEl = document.getElementById('medicineAlarmModal');
        if (modalEl) {
            document.getElementById('modalMedName').textContent = schedule.medicineName || 'Medicine';
            document.getElementById('modalMedType').textContent = schedule.medicineType || 'Tablet';
            document.getElementById('modalMedDosage').textContent = schedule.dosage || '';
            document.getElementById('modalScheduledTime').textContent = schedule.scheduledTimeFormatted || schedule.scheduledTime;
            document.getElementById('modalMealInstruction').textContent = schedule.mealInstruction || 'As directed';
            document.getElementById('modalSpecialInstruction').textContent = schedule.specialInstruction || 'Take with water';

            const defaultSnoozeBtn = document.getElementById('btnModalSnoozeDefault');
            if (defaultSnoozeBtn) {
                defaultSnoozeBtn.innerHTML = `<i class="fa-regular fa-clock me-1"></i> Snooze (${this.soundSettings.snoozeMinutes}m)`;
            }

            const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            modal.show();
        }
    }

    stopAndAcknowledge(scheduleId) {
        if (window.soundEngine) {
            window.soundEngine.stopAlarm();
        }
        if (scheduleId) {
            this.acknowledgedAlarms.add(scheduleId);
        }
        this.activeAlarmSchedule = null;

        const modalEl = document.getElementById('medicineAlarmModal');
        if (modalEl) {
            const modal = bootstrap.Modal.getInstance(modalEl);
            if (modal) modal.hide();
        }
    }

    markTaken(scheduleId) {
        this.stopAndAcknowledge(scheduleId);
        fetch(`/api/schedules/${scheduleId}/taken`, { method: 'PUT' })
            .then(res => res.json())
            .then(res => {
                this.showToast('success', 'Medicine marked as TAKEN ✅');
                this.refreshCurrentView();
            })
            .catch(err => this.showToast('danger', 'Failed to update status'));
    }

    markMissed(scheduleId) {
        this.stopAndAcknowledge(scheduleId);
        fetch(`/api/schedules/${scheduleId}/missed`, { method: 'PUT' })
            .then(res => res.json())
            .then(res => {
                this.showToast('warning', 'Medicine marked as MISSED ❌');
                this.refreshCurrentView();
            })
            .catch(err => this.showToast('danger', 'Failed to update status'));
    }

    snooze(scheduleId, minutes) {
        const mins = minutes || this.soundSettings.snoozeMinutes || 10;
        this.stopAndAcknowledge(scheduleId);
        fetch(`/api/schedules/${scheduleId}/snooze`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ minutes: mins })
        })
            .then(res => res.json())
            .then(res => {
                this.showToast('info', `Reminder snoozed for ${mins} minutes ⏰`);
                this.refreshCurrentView();
            })
            .catch(err => this.showToast('danger', 'Failed to snooze reminder'));
    }

    initNextDoseCountdown() {
        fetch('/api/schedules/next-dose')
            .then(res => {
                if (res.status === 401 || res.status === 403) return null;
                return res.json();
            })
            .then(response => {
                if (response && response.success && response.data) {
                    const next = response.data;
                    const datePart = next.scheduledDate;
                    const timePart = next.scheduledTime;
                    const targetStr = `${datePart}T${timePart}:00`;
                    this.nextDoseTarget = new Date(targetStr);
                    this.startCountdownClock();
                } else {
                    const countdownEl = document.getElementById('nextDoseCountdown');
                    if (countdownEl) countdownEl.textContent = "All Done Today!";
                }
            })
            .catch(err => console.debug("Next dose fetch note:", err));
    }

    startCountdownClock() {
        if (this.countdownInterval) clearInterval(this.countdownInterval);

        const updateClock = () => {
            const countdownEl = document.getElementById('nextDoseCountdown');
            if (!countdownEl || !this.nextDoseTarget) return;

            const now = new Date();
            const diffMs = this.nextDoseTarget - now;

            if (diffMs <= 0) {
                countdownEl.textContent = "Dose Due Now!";
                countdownEl.classList.add('text-warning');
                return;
            }

            const totalSec = Math.floor(diffMs / 1000);
            const hrs = Math.floor(totalSec / 3600);
            const mins = Math.floor((totalSec % 3600) / 60);
            const secs = totalSec % 60;

            const pad = (n) => String(n).padStart(2, '0');
            countdownEl.textContent = `${pad(hrs)}:${pad(mins)}:${pad(secs)}`;
        };

        updateClock();
        this.countdownInterval = setInterval(updateClock, 1000);
    }

    bindGlobalActions() {
        const btnTaken = document.getElementById('btnModalTaken');
        if (btnTaken) {
            btnTaken.addEventListener('click', () => {
                if (this.activeAlarmSchedule) {
                    this.markTaken(this.activeAlarmSchedule.id);
                }
            });
        }

        const btnMissed = document.getElementById('btnModalMissed');
        if (btnMissed) {
            btnMissed.addEventListener('click', () => {
                if (this.activeAlarmSchedule) {
                    this.markMissed(this.activeAlarmSchedule.id);
                }
            });
        }

        const btnSnoozeDefault = document.getElementById('btnModalSnoozeDefault');
        if (btnSnoozeDefault) {
            btnSnoozeDefault.addEventListener('click', () => {
                if (this.activeAlarmSchedule) {
                    this.snooze(this.activeAlarmSchedule.id, this.soundSettings.snoozeMinutes);
                }
            });
        }

        const enableAudioBtn = document.getElementById('btnEnableAudio');
        if (enableAudioBtn) {
            enableAudioBtn.addEventListener('click', () => {
                if (window.soundEngine) {
                    window.soundEngine.unlockAudio();
                    window.soundEngine.testSound(this.soundSettings.alarmSound, this.soundSettings.volume);
                    this.showToast('success', 'Audio Reminders Enabled and Tested!');
                }
            });
        }
    }

    refreshCurrentView() {
        setTimeout(() => {
            if (window.location.pathname.includes('/patient/dashboard') ||
                window.location.pathname.includes('/patient/timetable') ||
                window.location.pathname.includes('/patient/history')) {
                window.location.reload();
            }
        }, 1200);
    }

    showToast(type, message) {
        let container = document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            container.style.zIndex = '9999';
            document.body.appendChild(container);
        }

        const toastEl = document.createElement('div');
        toastEl.className = `toast align-items-center text-bg-${type} border-0 show shadow`;
        toastEl.setAttribute('role', 'alert');
        toastEl.innerHTML = `
            <div class="d-flex">
                <div class="toast-body fw-semibold">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        container.appendChild(toastEl);
        setTimeout(() => toastEl.remove(), 4000);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.reminderSystem = new MedicineReminderSystem();
});
