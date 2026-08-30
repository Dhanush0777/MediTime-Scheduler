/**
 * MediTime Doctor Prescription Form Handler
 * Multi-medicine dynamic rows, frequency-time preset calculators, live timetable preview, and AJAX submission.
 */

let medicineCount = 0;

function getFrequencyDefaultTimes(freq) {
    const lower = (freq || '').toLowerCase();
    if (lower.includes('once') || lower.startsWith('1')) return ['08:00'];
    if (lower.includes('twice') || lower.startsWith('2')) return ['08:00', '20:00'];
    if (lower.includes('3') || lower.includes('thrice')) return ['08:00', '14:00', '20:00'];
    if (lower.includes('4')) return ['08:00', '12:00', '16:00', '20:00'];
    if (lower.includes('6 hours')) return ['06:00', '12:00', '18:00', '00:00'];
    if (lower.includes('8 hours')) return ['08:00', '16:00', '00:00'];
    return ['08:00'];
}

function addMedicineRow(initialData = null) {
    medicineCount++;
    const idx = medicineCount;
    const container = document.getElementById('medicineRowsContainer');
    if (!container) return;

    const row = document.createElement('div');
    row.className = 'card mb-3 medicine-row border-primary-subtle';
    row.id = `medRow_${idx}`;

    const medName = initialData ? initialData.name : '';
    const dosage = initialData ? initialData.dosage : '500 mg';
    const medType = initialData ? initialData.type : 'Tablet';
    const freq = initialData ? initialData.frequency : '3 times per day';
    const duration = initialData ? initialData.duration : 5;
    const meal = initialData ? initialData.meal : 'After food';
    const special = initialData ? initialData.special : '';
    const times = initialData && initialData.times ? initialData.times : getFrequencyDefaultTimes(freq);

    row.innerHTML = `
        <div class="card-header bg-light d-flex justify-content-between align-items-center py-2">
            <span class="fw-bold text-primary"><i class="fa-solid fa-pills me-2"></i>Medicine #${idx}</span>
            <button type="button" class="btn btn-sm btn-outline-danger" onclick="removeMedicineRow(${idx})">
                <i class="fa-solid fa-trash-can me-1"></i> Remove
            </button>
        </div>
        <div class="card-body">
            <div class="row g-3">
                <div class="col-md-5">
                    <label class="form-label small fw-bold">Medicine Name *</label>
                    <input type="text" class="form-control med-name" placeholder="e.g. Paracetamol, Amoxicillin" value="${medName}" required oninput="updateLiveTimetablePreview()">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-bold">Dosage *</label>
                    <input type="text" class="form-control med-dosage" placeholder="e.g. 500 mg, 10 ml" value="${dosage}" required oninput="updateLiveTimetablePreview()">
                </div>
                <div class="col-md-4">
                    <label class="form-label small fw-bold">Type</label>
                    <select class="form-select med-type">
                        <option value="Tablet" ${medType === 'Tablet' ? 'selected' : ''}>Tablet</option>
                        <option value="Capsule" ${medType === 'Capsule' ? 'selected' : ''}>Capsule</option>
                        <option value="Syrup" ${medType === 'Syrup' ? 'selected' : ''}>Syrup</option>
                        <option value="Injection" ${medType === 'Injection' ? 'selected' : ''}>Injection</option>
                        <option value="Drops" ${medType === 'Drops' ? 'selected' : ''}>Drops</option>
                        <option value="Inhaler" ${medType === 'Inhaler' ? 'selected' : ''}>Inhaler</option>
                        <option value="Ointment" ${medType === 'Ointment' ? 'selected' : ''}>Ointment</option>
                    </select>
                </div>

                <div class="col-md-4">
                    <label class="form-label small fw-bold">Frequency *</label>
                    <select class="form-select med-freq" onchange="handleFrequencyChange(${idx}, this.value)">
                        <option value="Once daily" ${freq.includes('Once') ? 'selected' : ''}>Once daily (1x)</option>
                        <option value="2 times per day" ${freq.includes('2') || freq.includes('Twice') ? 'selected' : ''}>Twice daily (2x)</option>
                        <option value="3 times per day" ${freq.includes('3') || freq.includes('Thrice') ? 'selected' : ''}>3 times per day (3x)</option>
                        <option value="4 times per day" ${freq.includes('4') ? 'selected' : ''}>4 times per day (4x)</option>
                        <option value="Every 6 hours" ${freq.includes('6') ? 'selected' : ''}>Every 6 hours</option>
                        <option value="Every 8 hours" ${freq.includes('8') ? 'selected' : ''}>Every 8 hours</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label small fw-bold">Duration (Days) *</label>
                    <input type="number" class="form-control med-duration" min="1" max="90" value="${duration}" required oninput="updateLiveTimetablePreview()">
                </div>
                <div class="col-md-4">
                    <label class="form-label small fw-bold">Meal Relation</label>
                    <select class="form-select med-meal" onchange="updateLiveTimetablePreview()">
                        <option value="After food" ${meal === 'After food' ? 'selected' : ''}>After food</option>
                        <option value="Before food" ${meal === 'Before food' ? 'selected' : ''}>Before food</option>
                        <option value="With food" ${meal === 'With food' ? 'selected' : ''}>With food</option>
                        <option value="Empty stomach" ${meal === 'Empty stomach' ? 'selected' : ''}>Empty stomach</option>
                        <option value="No relation" ${meal === 'No relation' ? 'selected' : ''}>No specific relation</option>
                    </select>
                </div>

                <div class="col-12">
                    <label class="form-label small fw-bold">Reminder Times (24-Hour HH:mm)</label>
                    <div id="timesContainer_${idx}" class="d-flex flex-wrap gap-2 align-items-center">
                        <!-- Dynamic time badges -->
                    </div>
                    <div class="mt-2 d-flex gap-2 align-items-center">
                        <input type="time" class="form-control form-control-sm" style="max-width: 140px;" id="newTimeInput_${idx}" value="12:00">
                        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="addCustomTimeToRow(${idx})">
                            <i class="fa-solid fa-plus me-1"></i> Add Time
                        </button>
                    </div>
                </div>

                <div class="col-12">
                    <label class="form-label small fw-bold">Special Instructions</label>
                    <input type="text" class="form-control med-special" placeholder="e.g. Drink plenty of warm water, do not crush" value="${special}">
                </div>
            </div>
        </div>
    `;

    container.appendChild(row);
    renderTimesBadges(idx, times);
    updateLiveTimetablePreview();
}

function removeMedicineRow(idx) {
    const row = document.getElementById(`medRow_${idx}`);
    if (row) {
        row.remove();
        updateLiveTimetablePreview();
    }
}

function handleFrequencyChange(idx, freq) {
    const times = getFrequencyDefaultTimes(freq);
    renderTimesBadges(idx, times);
    updateLiveTimetablePreview();
}

function renderTimesBadges(idx, times) {
    const container = document.getElementById(`timesContainer_${idx}`);
    if (!container) return;
    container.innerHTML = '';

    times.forEach(t => {
        const badge = document.createElement('span');
        badge.className = 'badge bg-teal-subtle text-primary border border-primary-subtle d-inline-flex align-items-center p-2 fs-6';
        badge.dataset.time = t;
        badge.innerHTML = `
            <i class="fa-regular fa-clock me-1"></i> ${t}
            <button type="button" class="btn-close btn-close-sm ms-2" style="font-size: 0.6rem;" onclick="removeTimeFromRow(${idx}, '${t}')"></button>
        `;
        container.appendChild(badge);
    });
}

function addCustomTimeToRow(idx) {
    const input = document.getElementById(`newTimeInput_${idx}`);
    if (!input || !input.value) return;

    const time = input.value;
    const currentTimes = getCurrentTimes(idx);
    if (!currentTimes.includes(time)) {
        currentTimes.push(time);
        currentTimes.sort();
        renderTimesBadges(idx, currentTimes);
        updateLiveTimetablePreview();
    }
}

function removeTimeFromRow(idx, timeToRemove) {
    let currentTimes = getCurrentTimes(idx);
    currentTimes = currentTimes.filter(t => t !== timeToRemove);
    renderTimesBadges(idx, currentTimes);
    updateLiveTimetablePreview();
}

function getCurrentTimes(idx) {
    const container = document.getElementById(`timesContainer_${idx}`);
    if (!container) return [];
    const badges = container.querySelectorAll('[data-time]');
    const times = [];
    badges.forEach(b => times.push(b.dataset.time));
    return times;
}

function updateLiveTimetablePreview() {
    const previewContainer = document.getElementById('timetablePreviewTableBody');
    if (!previewContainer) return;

    const rows = document.querySelectorAll('.medicine-row');
    const scheduleItems = [];

    rows.forEach(row => {
        const idx = row.id.split('_')[1];
        const name = row.querySelector('.med-name')?.value || 'Unnamed Medicine';
        const dosage = row.querySelector('.med-dosage')?.value || '';
        const meal = row.querySelector('.med-meal')?.value || '';
        const times = getCurrentTimes(idx);

        times.forEach(t => {
            scheduleItems.push({
                time: t,
                name: name,
                dosage: dosage,
                meal: meal
            });
        });
    });

    scheduleItems.sort((a, b) => a.time.localeCompare(b.time));

    if (scheduleItems.length === 0) {
        previewContainer.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-3">Add medicines above to generate timetable preview</td></tr>`;
        return;
    }

    previewContainer.innerHTML = scheduleItems.map(item => `
        <tr>
            <td class="fw-bold text-primary"><i class="fa-regular fa-clock me-1"></i>${item.time}</td>
            <td class="fw-semibold">${item.name}</td>
            <td><span class="badge bg-light text-dark border">${item.dosage}</span></td>
            <td><span class="text-muted small">${item.meal}</span></td>
            <td><span class="badge badge-status badge-status-pending">Pending</span></td>
        </tr>
    `).join('');
}

function submitPrescriptionForm(event) {
    event.preventDefault();

    const form = document.getElementById('prescriptionForm');
    const patientId = document.getElementById('patientSelect').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const diagnosis = document.getElementById('diagnosis').value;
    const notes = document.getElementById('notes').value;

    const medicineRows = document.querySelectorAll('.medicine-row');
    if (medicineRows.length === 0) {
        alert('Please add at least one medicine to the prescription.');
        return;
    }

    const medicines = [];
    medicineRows.forEach(row => {
        const idx = row.id.split('_')[1];
        const name = row.querySelector('.med-name').value.trim();
        const dosage = row.querySelector('.med-dosage').value.trim();
        const type = row.querySelector('.med-type').value;
        const freq = row.querySelector('.med-freq').value;
        const duration = parseInt(row.querySelector('.med-duration').value, 10) || 5;
        const meal = row.querySelector('.med-meal').value;
        const special = row.querySelector('.med-special').value.trim();
        const times = getCurrentTimes(idx);

        if (name && dosage) {
            medicines.push({
                medicineName: name,
                dosage: dosage,
                medicineType: type,
                frequency: freq,
                durationDays: duration,
                mealInstruction: meal,
                specialInstruction: special,
                reminderTimes: times.length > 0 ? times : getFrequencyDefaultTimes(freq)
            });
        }
    });

    if (medicines.length === 0) {
        alert('Please fill in medicine name and dosage for at least one medicine.');
        return;
    }

    const payload = {
        patientId: parseInt(patientId, 10),
        startDate: startDate,
        endDate: endDate,
        diagnosis: diagnosis,
        notes: notes,
        medicines: medicines
    };

    const submitBtn = document.getElementById('btnSubmitPrescription');
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span> Generating Timetable...`;

    fetch('/api/prescriptions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.success && data.data) {
            window.location.href = `/doctor/prescriptions/${data.data.id}`;
        } else {
            alert('Error creating prescription: ' + (data.message || 'Unknown error'));
            submitBtn.disabled = false;
            submitBtn.innerHTML = `<i class="fa-solid fa-check me-2"></i> Save & Generate Timetable`;
        }
    })
    .catch(err => {
        alert('Failed to connect to server: ' + err.message);
        submitBtn.disabled = false;
        submitBtn.innerHTML = `<i class="fa-solid fa-check me-2"></i> Save & Generate Timetable`;
    });
}
