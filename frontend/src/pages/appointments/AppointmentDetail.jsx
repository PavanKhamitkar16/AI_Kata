import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorComponent from '../../components/ErrorComponent';
import { useToast } from '../../components/Toast';
import { useAuth } from '../../hooks/useAuth';
import {
  cancelAppointment,
  completeAppointment,
  confirmAppointment,
  getAppointment,
  markNoShow,
  rescheduleAppointment,
} from '../../api/appointmentApi';

const STATUS_STYLES = {
  REQUESTED: { bg: '#fefcbf', color: '#744210', border: '#faf089' },
  CONFIRMED: { bg: '#c6f6d5', color: '#276749', border: '#9ae6b4' },
  CANCELLED: { bg: '#fed7d7', color: '#c53030', border: '#fc8181' },
  COMPLETED: { bg: '#bee3f8', color: '#2b6cb0', border: '#90cdf4' },
  NO_SHOW:   { bg: '#e9d8fd', color: '#6b46c1', border: '#d6bcfa' },
};

function AppointmentDetail() {
  const { id }        = useParams();
  const { user }      = useAuth();
  const { showToast } = useToast();
  const navigate      = useNavigate();
  const isAdminOrStaff = user?.role === 'ADMIN' || user?.role === 'STAFF' || user?.role === 'DOCTOR';

  const [appt,     setAppt]     = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);
  const [busy,     setBusy]     = useState(false);

  // Reschedule modal state
  const [showReschedule, setShowReschedule] = useState(false);
  const [newDate,         setNewDate]        = useState('');
  const [newTime,         setNewTime]        = useState('');

  async function fetchAppt() {
    setLoading(true);
    setError(null);
    try {
      setAppt(await getAppointment(id));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load appointment.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { fetchAppt(); }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  async function doAction(action, label) {
    setBusy(true);
    try {
      const updated = await action();
      setAppt(updated);
      showToast(`Appointment ${label}.`, 'success');
    } catch (err) {
      showToast(err.response?.data?.message || `Failed to ${label}.`, 'error');
    } finally {
      setBusy(false);
    }
  }

  async function handleReschedule(e) {
    e.preventDefault();
    if (!newDate || !newTime) { showToast('Please fill in date and time.', 'error'); return; }
    setBusy(true);
    setShowReschedule(false);
    try {
      const updated = await rescheduleAppointment(id, { newDate, newStartTime: newTime });
      setAppt(updated);
      showToast('Appointment rescheduled.', 'success');
    } catch (err) {
      showToast(err.response?.data?.message || 'Reschedule failed.', 'error');
    } finally {
      setBusy(false);
    }
  }

  function formatDate(d) {
    if (!d) return '—';
    return new Date(d + 'T00:00:00').toLocaleDateString('en-GB', {
      weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
    });
  }
  function formatTime(t) { return t ? t.substring(0, 5) : '—'; }
  function formatDateTime(iso) {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('en-GB', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  const canCancel     = appt && ['REQUESTED', 'CONFIRMED'].includes(appt.status);
  const canReschedule = appt && ['REQUESTED', 'CONFIRMED'].includes(appt.status);
  const canConfirm    = appt && appt.status === 'REQUESTED' && isAdminOrStaff;
  const canComplete   = appt && appt.status === 'CONFIRMED'  && isAdminOrStaff;
  const canNoShow     = appt && appt.status === 'CONFIRMED'  && isAdminOrStaff;
  const todayStr      = new Date().toISOString().split('T')[0];
  const ss            = appt ? (STATUS_STYLES[appt.status] ?? STATUS_STYLES.REQUESTED) : {};

  return (
    <div style={styles.layout}>
      <Sidebar />
      <div style={styles.main}>
        <Header />
        <main style={styles.content}>
          <div style={styles.breadcrumb}>
            <button onClick={() => navigate('/appointments')} style={styles.backLink}>Appointments</button>
            <span style={styles.sep}>/</span>
            <span>Detail</span>
          </div>

          {loading && <div style={{ padding: '60px', textAlign: 'center' }}><LoadingSpinner size="md" /></div>}
          {error && !loading && <ErrorComponent message={error} onRetry={fetchAppt} />}

          {!loading && !error && appt && (
            <div style={styles.card}>
              {/* Header */}
              <div style={styles.cardHeader}>
                <h2 style={styles.cardTitle}>Appointment</h2>
                <span style={{ ...styles.statusBadge, background: ss.bg, color: ss.color, border: `1px solid ${ss.border}` }}>
                  {appt.status}
                </span>
              </div>

              {/* Detail grid */}
              <div style={styles.divider} />
              <div style={styles.detailGrid}>
                <DetailItem label="Doctor"         value={appt.doctorName} />
                <DetailItem label="Specialization" value={appt.doctorSpecialization} />
                <DetailItem label="Patient"        value={appt.patientName} />
                <DetailItem label="Date"           value={formatDate(appt.appointmentDate)} />
                <DetailItem label="Time"           value={`${formatTime(appt.startTime)} – ${formatTime(appt.endTime)}`} />
                <DetailItem label="Reason"         value={appt.reason} />
                {appt.notes && <DetailItem label="Notes" value={appt.notes} />}
                <DetailItem label="Created"        value={formatDateTime(appt.createdAt)} />
                <DetailItem label="Last Updated"   value={formatDateTime(appt.updatedAt)} />
              </div>

              {/* Actions */}
              <div style={styles.divider} />
              <div style={styles.actionRow}>
                {canConfirm && (
                  <button disabled={busy} onClick={() => doAction(() => confirmAppointment(id), 'confirmed')} style={styles.confirmBtn}>
                    {busy ? '...' : 'Confirm'}
                  </button>
                )}
                {canComplete && (
                  <button disabled={busy} onClick={() => doAction(() => completeAppointment(id), 'completed')} style={styles.completeBtn}>
                    {busy ? '...' : 'Mark Complete'}
                  </button>
                )}
                {canNoShow && (
                  <button disabled={busy} onClick={() => doAction(() => markNoShow(id), 'marked no-show')} style={styles.noShowBtn}>
                    {busy ? '...' : 'No Show'}
                  </button>
                )}
                {canReschedule && (
                  <button disabled={busy} onClick={() => setShowReschedule(true)} style={styles.rescheduleBtn}>
                    Reschedule
                  </button>
                )}
                {canCancel && (
                  <button disabled={busy} onClick={() => doAction(() => cancelAppointment(id), 'cancelled')} style={styles.cancelBtn}>
                    {busy ? '...' : 'Cancel Appointment'}
                  </button>
                )}
              </div>
            </div>
          )}

          {/* Reschedule Modal */}
          {showReschedule && (
            <div style={styles.modalOverlay} onClick={() => setShowReschedule(false)}>
              <div style={styles.modal} onClick={e => e.stopPropagation()}>
                <h3 style={styles.modalTitle}>Reschedule Appointment</h3>
                <form onSubmit={handleReschedule} style={styles.modalForm}>
                  <div style={styles.field}>
                    <label style={styles.label}>New Date</label>
                    <input
                      type="date"
                      value={newDate}
                      min={todayStr}
                      onChange={e => setNewDate(e.target.value)}
                      style={styles.input}
                      required
                    />
                  </div>
                  <div style={styles.field}>
                    <label style={styles.label}>New Start Time (HH:MM)</label>
                    <input
                      type="time"
                      value={newTime}
                      onChange={e => setNewTime(e.target.value)}
                      style={styles.input}
                      required
                    />
                  </div>
                  <div style={styles.modalActions}>
                    <button type="button" onClick={() => setShowReschedule(false)} style={styles.modalCancelBtn}>
                      Back
                    </button>
                    <button type="submit" style={styles.modalSubmitBtn}>
                      Confirm Reschedule
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

function DetailItem({ label, value }) {
  return (
    <div style={styles.detailItem}>
      <span style={styles.detailLabel}>{label}</span>
      <span style={styles.detailValue}>{value || '—'}</span>
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

  card:       { background: '#fff', borderRadius: '8px', padding: '32px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)', maxWidth: '740px' },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  cardTitle:  { margin: 0, fontSize: '20px', fontWeight: 700, color: '#1a202c' },
  statusBadge:{ fontSize: '12px', fontWeight: 700, padding: '4px 14px', borderRadius: '12px' },

  divider:    { borderTop: '1px solid #e2e8f0', margin: '20px 0' },

  detailGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '18px' },
  detailItem: { display: 'flex', flexDirection: 'column', gap: '3px' },
  detailLabel:{ fontSize: '11px', fontWeight: 600, color: '#718096', textTransform: 'uppercase', letterSpacing: '0.5px' },
  detailValue:{ fontSize: '14px', color: '#2d3748', fontWeight: 500 },

  actionRow:     { display: 'flex', gap: '10px', flexWrap: 'wrap' },
  confirmBtn:    { padding: '9px 18px', borderRadius: '4px', border: '1px solid #9ae6b4', background: '#f0fff4', color: '#276749', fontSize: '13px', fontWeight: 600, cursor: 'pointer' },
  completeBtn:   { padding: '9px 18px', borderRadius: '4px', border: '1px solid #90cdf4', background: '#ebf8ff', color: '#2b6cb0', fontSize: '13px', fontWeight: 600, cursor: 'pointer' },
  noShowBtn:     { padding: '9px 18px', borderRadius: '4px', border: '1px solid #d6bcfa', background: '#faf5ff', color: '#6b46c1', fontSize: '13px', fontWeight: 600, cursor: 'pointer' },
  rescheduleBtn: { padding: '9px 18px', borderRadius: '4px', border: '1px solid #faf089', background: '#fefcbf', color: '#744210', fontSize: '13px', fontWeight: 600, cursor: 'pointer' },
  cancelBtn:     { padding: '9px 18px', borderRadius: '4px', border: '1px solid #fed7d7', background: '#fff5f5', color: '#c53030', fontSize: '13px', fontWeight: 600, cursor: 'pointer' },

  // Modal
  modalOverlay:  { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal:         { background: '#fff', borderRadius: '8px', padding: '28px', width: '380px', maxWidth: '90vw', boxShadow: '0 4px 24px rgba(0,0,0,0.18)' },
  modalTitle:    { margin: '0 0 20px', fontSize: '17px', fontWeight: 700, color: '#1a202c' },
  modalForm:     { display: 'flex', flexDirection: 'column', gap: '16px' },
  field:         { display: 'flex', flexDirection: 'column', gap: '5px' },
  label:         { fontSize: '13px', fontWeight: 600, color: '#4a5568' },
  input:         { padding: '9px 12px', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '14px' },
  modalActions:  { display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '4px' },
  modalCancelBtn:{ padding: '9px 16px', borderRadius: '4px', border: '1px solid #cbd5e0', background: '#fff', color: '#4a5568', fontSize: '13px', cursor: 'pointer' },
  modalSubmitBtn:{ padding: '9px 18px', borderRadius: '4px', border: 'none', background: '#3182ce', color: '#fff', fontSize: '13px', fontWeight: 600, cursor: 'pointer' },
};

export default AppointmentDetail;
