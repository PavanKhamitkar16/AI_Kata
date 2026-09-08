import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorComponent from '../../components/ErrorComponent';
import { useToast } from '../../components/Toast';
import { useAuth } from '../../hooks/useAuth';
import { cancelAppointment, getAllAppointments, getPatientAppointments, getDoctorAppointments } from '../../api/appointmentApi';

const STATUS_STYLES = {
  REQUESTED: { bg: '#fefcbf', color: '#744210' },
  CONFIRMED: { bg: '#c6f6d5', color: '#276749' },
  CANCELLED: { bg: '#fed7d7', color: '#c53030' },
  COMPLETED: { bg: '#bee3f8', color: '#2b6cb0' },
  NO_SHOW:   { bg: '#e9d8fd', color: '#6b46c1' },
};

const CANCELLABLE = ['REQUESTED', 'CONFIRMED'];

function MyAppointments() {
  const { user }      = useAuth();
  const { showToast } = useToast();
  const navigate      = useNavigate();

  const [appointments, setAppointments] = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState(null);
  const [cancelling,   setCancelling]   = useState(null);

  const fetchAppointments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      let data = [];
      if (user?.role === 'DOCTOR') {
        // DOCTOR: show only their own appointments, identified by the doctorId
        // returned at login (matched by email against the Doctor profile).
        if (user.doctorId) {
          data = await getDoctorAppointments(user.doctorId);
        } else {
          // doctorId missing means no Doctor profile is linked to this user account yet.
          setError('Your account is not linked to a Doctor profile. Ask an admin to register your doctor profile with the same email as your user account.');
          return;
        }
      } else {
        // ADMIN / STAFF: show all appointments so they can manage the full schedule.
        data = await getAllAppointments();
      }
      setAppointments(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load appointments.');
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => { fetchAppointments(); }, [fetchAppointments]);

  async function handleCancel(appt) {
    setCancelling(appt.appointmentId);
    try {
      await cancelAppointment(appt.appointmentId);
      showToast('Appointment cancelled.', 'warning');
      fetchAppointments();
    } catch (err) {
      showToast(err.response?.data?.message || 'Cancel failed.', 'error');
    } finally {
      setCancelling(null);
    }
  }

  function formatDate(d) {
    if (!d) return '—';
    return new Date(d + 'T00:00:00').toLocaleDateString('en-GB', {
      day: '2-digit', month: 'short', year: 'numeric',
    });
  }

  function formatTime(t) {
    return t ? t.substring(0, 5) : '—';
  }

  return (
    <div style={styles.layout}>
      <Sidebar />
      <div style={styles.main}>
        <Header />
        <main style={styles.content}>
          <div style={styles.pageHeader}>
            <div>
              <h2 style={styles.pageTitle}>My Appointments</h2>
              <p style={styles.pageSubtitle}>{appointments.length} appointment{appointments.length !== 1 ? 's' : ''}</p>
            </div>
            <Link to="/appointments/book" style={styles.primaryBtn}>+ Book Appointment</Link>
          </div>

          {loading && <div style={{ padding: '40px', textAlign: 'center' }}><LoadingSpinner size="md" /></div>}
          {error && !loading && <ErrorComponent message={error} onRetry={fetchAppointments} />}

          {!loading && !error && appointments.length === 0 && (
            <div style={styles.emptyState}>
              <p>No appointments yet.</p>
              <Link to="/doctors" style={styles.primaryBtn}>Browse Doctors</Link>
            </div>
          )}

          {!loading && !error && appointments.length > 0 && (
            <div style={styles.tableWrapper}>
              <table style={styles.table}>
                <thead>
                  <tr style={styles.thead}>
                    <th style={styles.th}>Doctor</th>
                    <th style={styles.th}>Patient</th>
                    <th style={styles.th}>Date</th>
                    <th style={styles.th}>Time</th>
                    <th style={styles.th}>Reason</th>
                    <th style={styles.th}>Status</th>
                    <th style={styles.th}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((appt, i) => {
                    const ss = STATUS_STYLES[appt.status] ?? { bg: '#edf2f7', color: '#4a5568' };
                    const canCancel = CANCELLABLE.includes(appt.status);
                    return (
                      <tr key={appt.appointmentId} style={i % 2 === 0 ? styles.trEven : styles.trOdd}>
                        <td style={styles.td}>
                          <Link to={`/doctors/${appt.doctorId}`} style={styles.nameLink}>
                            {appt.doctorName}
                          </Link>
                          <div style={styles.subText}>{appt.doctorSpecialization}</div>
                        </td>
                        <td style={styles.td}>{appt.patientName}</td>
                        <td style={styles.td}>{formatDate(appt.appointmentDate)}</td>
                        <td style={styles.td}>{formatTime(appt.startTime)} – {formatTime(appt.endTime)}</td>
                        <td style={styles.td}>
                          <span title={appt.reason} style={styles.reasonCell}>
                            {appt.reason.length > 40 ? appt.reason.substring(0, 40) + '…' : appt.reason}
                          </span>
                        </td>
                        <td style={styles.td}>
                          <span style={{ ...styles.statusBadge, background: ss.bg, color: ss.color }}>
                            {appt.status}
                          </span>
                        </td>
                        <td style={styles.td}>
                          <div style={styles.actionRow}>
                            <Link to={`/appointments/${appt.appointmentId}`} style={styles.viewBtn}>View</Link>
                            {canCancel && (
                              <button
                                onClick={() => handleCancel(appt)}
                                disabled={cancelling === appt.appointmentId}
                                style={styles.cancelBtn}
                              >
                                {cancelling === appt.appointmentId ? '...' : 'Cancel'}
                              </button>
                            )}
                            {canCancel && (
                              <Link to={`/appointments/${appt.appointmentId}`} style={styles.rescheduleBtn}>
                                Reschedule
                              </Link>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

const styles = {
  layout:       { display: 'flex', minHeight: '100vh', fontFamily: 'system-ui, sans-serif' },
  main:         { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' },
  content:      { flex: 1, padding: '24px', background: '#f7fafc', overflowY: 'auto' },

  pageHeader:   { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' },
  pageTitle:    { margin: '0 0 4px', fontSize: '22px', color: '#1a202c', fontWeight: 700 },
  pageSubtitle: { margin: 0, fontSize: '13px', color: '#718096' },

  primaryBtn:   { padding: '9px 18px', borderRadius: '4px', background: '#3182ce', color: '#fff', fontSize: '14px', fontWeight: 600, textDecoration: 'none', border: 'none', cursor: 'pointer', display: 'inline-block' },

  tableWrapper: { background: '#fff', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'auto' },
  table:        { width: '100%', borderCollapse: 'collapse', fontSize: '14px', minWidth: '800px' },
  thead:        { background: '#edf2f7' },
  th:           { padding: '12px 16px', textAlign: 'left', fontSize: '12px', fontWeight: 600, color: '#4a5568', textTransform: 'uppercase', letterSpacing: '0.5px', borderBottom: '2px solid #e2e8f0' },
  trEven:       { background: '#fff' },
  trOdd:        { background: '#f7fafc' },
  td:           { padding: '12px 16px', borderBottom: '1px solid #e2e8f0', color: '#2d3748', verticalAlign: 'middle' },

  nameLink:     { fontWeight: 600, color: '#3182ce', textDecoration: 'none' },
  subText:      { fontSize: '12px', color: '#718096', marginTop: '2px' },
  reasonCell:   { fontSize: '13px', color: '#4a5568' },

  statusBadge:  { fontSize: '11px', fontWeight: 600, padding: '3px 8px', borderRadius: '12px', display: 'inline-block' },

  actionRow:    { display: 'flex', gap: '6px', flexWrap: 'wrap' },
  viewBtn:      { padding: '4px 10px', borderRadius: '4px', background: '#ebf8ff', color: '#2b6cb0', fontSize: '12px', textDecoration: 'none', border: '1px solid #bee3f8', fontWeight: 500 },
  cancelBtn:    { padding: '4px 10px', borderRadius: '4px', background: '#fff5f5', color: '#c53030', fontSize: '12px', border: '1px solid #fed7d7', cursor: 'pointer', fontWeight: 500 },
  rescheduleBtn:{ padding: '4px 10px', borderRadius: '4px', background: '#fefcbf', color: '#744210', fontSize: '12px', textDecoration: 'none', border: '1px solid #faf089', fontWeight: 500 },

  emptyState:   { textAlign: 'center', padding: '60px 24px', color: '#718096', background: '#fff', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' },
};

export default MyAppointments;
