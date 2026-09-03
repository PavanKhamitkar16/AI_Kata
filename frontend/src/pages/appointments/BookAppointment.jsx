import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorComponent from '../../components/ErrorComponent';
import { useToast } from '../../components/Toast';
import { useAuth } from '../../hooks/useAuth';
import { getDoctorById } from '../../api/doctorApi';
import { getAvailableSlots, createAppointment } from '../../api/appointmentApi';

function BookAppointment() {
  const navigate              = useNavigate();
  const [searchParams]        = useSearchParams();
  const { user }              = useAuth();
  const { showToast }         = useToast();

  const doctorId  = searchParams.get('doctorId') || '';
  const dateParam = searchParams.get('date') || '';
  const timeParam = searchParams.get('time') || '';

  const [doctor,   setDoctor]   = useState(null);
  const [docLoad,  setDocLoad]  = useState(!!doctorId);
  const [docError, setDocError] = useState(null);

  const [date,       setDate]       = useState(dateParam);
  const [slots,      setSlots]      = useState([]);
  const [slotsLoad,  setSlotsLoad]  = useState(false);
  const [selectedSlot, setSelectedSlot] = useState(timeParam || '');
  const [reason,     setReason]     = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Load doctor info
  useEffect(() => {
    if (!doctorId) return;
    getDoctorById(doctorId)
      .then(setDoctor)
      .catch(err => setDocError(err.response?.data?.message || 'Failed to load doctor.'))
      .finally(() => setDocLoad(false));
  }, [doctorId]);

  // Load slots whenever date changes
  useEffect(() => {
    if (!doctorId || !date) return;
    setSlotsLoad(true);
    setSlots([]);
    setSelectedSlot(timeParam || '');
    getAvailableSlots(doctorId, date)
      .then(data => setSlots(data.filter(s => s.available)))
      .catch(() => setSlots([]))
      .finally(() => setSlotsLoad(false));
  }, [doctorId, date]); // eslint-disable-line react-hooks/exhaustive-deps

  async function handleSubmit(e) {
    e.preventDefault();
    if (!selectedSlot) { showToast('Please select a time slot.', 'error'); return; }
    if (!reason.trim()) { showToast('Please enter a reason.', 'error'); return; }

    setSubmitting(true);
    try {
      // patientId: in a real system this comes from the logged-in patient's profile.
      // For now we use the user's id field (staff/admin can book on behalf of a patient
      // via the patientId they'd pass; here we fall back to user.id).
      const appointment = await createAppointment({
        patientId: user?.patientId || user?.id,
        doctorId,
        date,
        startTime: selectedSlot,
        reason: reason.trim(),
      });
      showToast('Appointment booked!', 'success');
      navigate(`/appointments/${appointment.appointmentId}/confirmation`);
    } catch (err) {
      showToast(err.response?.data?.message || 'Booking failed. Try a different slot.', 'error');
    } finally {
      setSubmitting(false);
    }
  }

  function formatTime(t) {
    if (!t) return '';
    return t.substring(0, 5);
  }

  // Minimum date for the date picker
  const todayStr = new Date().toISOString().split('T')[0];

  return (
    <div style={styles.layout}>
      <Sidebar />
      <div style={styles.main}>
        <Header />
        <main style={styles.content}>
          <div style={styles.breadcrumb}>
            <button onClick={() => navigate('/appointments')} style={styles.backLink}>Appointments</button>
            <span style={styles.sep}>/</span>
            <span>Book Appointment</span>
          </div>

          <div style={styles.card}>
            <h2 style={styles.pageTitle}>Book an Appointment</h2>

            {docLoad && <LoadingSpinner size="sm" />}
            {docError && <ErrorComponent message={docError} />}

            {!docLoad && doctor && (
              <div style={styles.doctorBanner}>
                <div style={styles.avatar}>{doctor.name.charAt(0).toUpperCase()}</div>
                <div>
                  <p style={styles.docName}>{doctor.name}</p>
                  <p style={styles.docSub}>{doctor.specialization} · {doctor.department}</p>
                </div>
                <button
                  type="button"
                  onClick={() => navigate(`/doctors/${doctorId}/availability`)}
                  style={styles.changeDoctorBtn}
                >
                  Change date
                </button>
              </div>
            )}

            <form onSubmit={handleSubmit} style={styles.form}>
              {/* Date picker */}
              <div style={styles.field}>
                <label style={styles.label}>Appointment Date</label>
                <input
                  type="date"
                  value={date}
                  min={todayStr}
                  onChange={e => setDate(e.target.value)}
                  style={styles.input}
                  required
                />
              </div>

              {/* Slot selector */}
              <div style={styles.field}>
                <label style={styles.label}>Available Time Slots</label>
                {slotsLoad && <LoadingSpinner size="sm" />}
                {!slotsLoad && date && slots.length === 0 && (
                  <p style={styles.noSlots}>No available slots for this date. Try another day.</p>
                )}
                {!slotsLoad && slots.length > 0 && (
                  <div style={styles.slotGrid}>
                    {slots.map(slot => {
                      const isSelected = selectedSlot === slot.startTime;
                      return (
                        <button
                          key={slot.startTime}
                          type="button"
                          onClick={() => setSelectedSlot(slot.startTime)}
                          style={isSelected ? styles.slotSelected : styles.slotBtn}
                        >
                          {formatTime(slot.startTime)} – {formatTime(slot.endTime)}
                        </button>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* Reason */}
              <div style={styles.field}>
                <label style={styles.label}>Reason for Visit</label>
                <textarea
                  value={reason}
                  onChange={e => setReason(e.target.value)}
                  rows={3}
                  placeholder="Describe your symptoms or reason for the appointment..."
                  style={styles.textarea}
                  required
                  maxLength={500}
                />
                <p style={styles.charCount}>{reason.length}/500</p>
              </div>

              {/* Summary */}
              {selectedSlot && date && (
                <div style={styles.summary}>
                  <strong>Booking summary:</strong> {doctor?.name} on {date} at {formatTime(selectedSlot)}
                </div>
              )}

              <div style={styles.actions}>
                <button type="button" onClick={() => navigate(-1)} style={styles.cancelBtn}>
                  Back
                </button>
                <button
                  type="submit"
                  disabled={submitting || !selectedSlot}
                  style={submitting || !selectedSlot ? styles.submitBtnDisabled : styles.submitBtn}
                >
                  {submitting ? 'Booking...' : 'Confirm Booking'}
                </button>
              </div>
            </form>
          </div>
        </main>
      </div>
    </div>
  );
}

const styles = {
  layout:   { display: 'flex', minHeight: '100vh', fontFamily: 'system-ui, sans-serif' },
  main:     { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' },
  content:  { flex: 1, padding: '24px', background: '#f7fafc', overflowY: 'auto' },

  breadcrumb: { display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px', fontSize: '14px', color: '#718096' },
  backLink:   { background: 'none', border: 'none', color: '#3182ce', cursor: 'pointer', fontSize: '14px', padding: 0 },
  sep:        { color: '#cbd5e0' },

  card:      { background: '#fff', borderRadius: '8px', padding: '32px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)', maxWidth: '640px' },
  pageTitle: { margin: '0 0 24px', fontSize: '20px', fontWeight: 700, color: '#1a202c' },

  doctorBanner: { display: 'flex', alignItems: 'center', gap: '14px', background: '#ebf8ff', border: '1px solid #bee3f8', borderRadius: '8px', padding: '14px', marginBottom: '24px' },
  avatar:       { width: '44px', height: '44px', borderRadius: '50%', background: '#3182ce', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '18px', fontWeight: 700, flexShrink: 0 },
  docName:      { margin: 0, fontWeight: 700, fontSize: '15px', color: '#1a202c' },
  docSub:       { margin: 0, fontSize: '12px', color: '#718096' },
  changeDoctorBtn: { marginLeft: 'auto', padding: '5px 12px', borderRadius: '4px', border: '1px solid #bee3f8', background: '#fff', color: '#2b6cb0', fontSize: '12px', cursor: 'pointer', fontWeight: 500 },

  form:     { display: 'flex', flexDirection: 'column', gap: '20px' },
  field:    { display: 'flex', flexDirection: 'column', gap: '6px' },
  label:    { fontSize: '13px', fontWeight: 600, color: '#4a5568' },
  input:    { padding: '9px 12px', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '14px', color: '#2d3748' },
  textarea: { padding: '9px 12px', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '14px', color: '#2d3748', resize: 'vertical' },
  charCount:{ margin: '2px 0 0', fontSize: '11px', color: '#a0aec0', textAlign: 'right' },

  noSlots:  { fontSize: '13px', color: '#718096' },
  slotGrid: { display: 'flex', flexWrap: 'wrap', gap: '8px' },
  slotBtn:  { padding: '8px 14px', borderRadius: '6px', border: '1px solid #9ae6b4', background: '#f0fff4', color: '#276749', fontSize: '13px', cursor: 'pointer', fontWeight: 500 },
  slotSelected: { padding: '8px 14px', borderRadius: '6px', border: '1px solid #3182ce', background: '#3182ce', color: '#fff', fontSize: '13px', cursor: 'pointer', fontWeight: 600 },

  summary:  { background: '#fefcbf', border: '1px solid #faf089', borderRadius: '6px', padding: '10px 14px', fontSize: '13px', color: '#744210' },

  actions:         { display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '4px' },
  cancelBtn:       { padding: '10px 20px', borderRadius: '4px', border: '1px solid #cbd5e0', background: '#fff', color: '#4a5568', fontSize: '14px', cursor: 'pointer' },
  submitBtn:       { padding: '10px 24px', borderRadius: '4px', border: 'none', background: '#3182ce', color: '#fff', fontSize: '14px', fontWeight: 600, cursor: 'pointer' },
  submitBtnDisabled: { padding: '10px 24px', borderRadius: '4px', border: 'none', background: '#a0aec0', color: '#fff', fontSize: '14px', fontWeight: 600, cursor: 'not-allowed' },
};

export default BookAppointment;
