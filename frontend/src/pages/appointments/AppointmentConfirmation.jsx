import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorComponent from '../../components/ErrorComponent';
import { getAppointment } from '../../api/appointmentApi';

const STATUS_COLORS = {
  REQUESTED:  { bg: '#fefcbf', color: '#744210', border: '#faf089' },
  CONFIRMED:  { bg: '#c6f6d5', color: '#276749', border: '#9ae6b4' },
  CANCELLED:  { bg: '#fed7d7', color: '#c53030', border: '#fc8181' },
  COMPLETED:  { bg: '#bee3f8', color: '#2b6cb0', border: '#90cdf4' },
  NO_SHOW:    { bg: '#e9d8fd', color: '#6b46c1', border: '#d6bcfa' },
};

function AppointmentConfirmation() {
  const { id }   = useParams();
  const navigate = useNavigate();

  const [appt,    setAppt]    = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    getAppointment(id)
      .then(setAppt)
      .catch(err => setError(err.response?.data?.message || 'Failed to load appointment.'))
      .finally(() => setLoading(false));
  }, [id]);

  function formatDate(d) {
    if (!d) return '—';
    return new Date(d + 'T00:00:00').toLocaleDateString('en-GB', {
      weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
    });
  }

  function formatTime(t) {
    if (!t) return '—';
    return t.substring(0, 5);
  }

  const statusStyle = appt ? (STATUS_COLORS[appt.status] ?? STATUS_COLORS.REQUESTED) : {};

  return (
    <div style={styles.layout}>
      <Sidebar />
      <div style={styles.main}>
        <Header />
        <main style={styles.content}>
          {loading && <div style={{ padding: '60px', textAlign: 'center' }}><LoadingSpinner size="md" /></div>}
          {error && !loading && <ErrorComponent message={error} />}

          {!loading && !error && appt && (
            <div style={styles.wrapper}>
              {/* Success banner */}
              <div style={styles.successBanner}>
                <span style={styles.checkmark}>&#10003;</span>
                <div>
                  <h2 style={styles.successTitle}>Appointment Booked!</h2>
                  <p style={styles.successSub}>Your appointment has been submitted and is pending confirmation.</p>
                </div>
              </div>

              {/* Appointment card */}
              <div style={styles.card}>
                <div style={styles.cardHeader}>
                  <h3 style={styles.cardTitle}>Appointment Details</h3>
                  <span style={{ ...styles.statusBadge, background: statusStyle.bg, color: statusStyle.color, border: `1px solid ${statusStyle.border}` }}>
                    {appt.status}
                  </span>
                </div>

                <div style={styles.detailGrid}>
                  <DetailItem label="Doctor"          value={appt.doctorName} />
                  <DetailItem label="Specialization"  value={appt.doctorSpecialization} />
                  <DetailItem label="Patient"         value={appt.patientName} />
                  <DetailItem label="Date"            value={formatDate(appt.appointmentDate)} />
                  <DetailItem label="Time"            value={`${formatTime(appt.startTime)} – ${formatTime(appt.endTime)}`} />
                  <DetailItem label="Reason"          value={appt.reason} />
                  {appt.notes && <DetailItem label="Notes" value={appt.notes} />}
                </div>

                <div style={styles.actions}>
                  <button
                    onClick={() => navigate('/appointments')}
                    style={styles.primaryBtn}
                  >
                    View My Appointments
                  </button>
                  <Link
                    to={`/doctors/${appt.doctorId}/availability`}
                    style={styles.secondaryBtn}
                  >
                    Book Another
                  </Link>
                </div>
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

  wrapper:        { maxWidth: '600px' },
  successBanner:  { display: 'flex', alignItems: 'flex-start', gap: '16px', background: '#f0fff4', border: '1px solid #9ae6b4', borderRadius: '8px', padding: '20px', marginBottom: '20px' },
  checkmark:      { width: '40px', height: '40px', borderRadius: '50%', background: '#38a169', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '20px', fontWeight: 700, flexShrink: 0 },
  successTitle:   { margin: '0 0 4px', fontSize: '18px', fontWeight: 700, color: '#276749' },
  successSub:     { margin: 0, fontSize: '13px', color: '#4a5568' },

  card:        { background: '#fff', borderRadius: '8px', padding: '24px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)' },
  cardHeader:  { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
  cardTitle:   { margin: 0, fontSize: '16px', fontWeight: 700, color: '#1a202c' },
  statusBadge: { fontSize: '12px', fontWeight: 700, padding: '4px 12px', borderRadius: '12px' },

  detailGrid:  { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '24px' },
  detailItem:  { display: 'flex', flexDirection: 'column', gap: '3px' },
  detailLabel: { fontSize: '11px', fontWeight: 600, color: '#718096', textTransform: 'uppercase', letterSpacing: '0.5px' },
  detailValue: { fontSize: '14px', color: '#2d3748', fontWeight: 500 },

  actions:     { display: 'flex', gap: '12px' },
  primaryBtn:  { padding: '10px 20px', borderRadius: '4px', border: 'none', background: '#3182ce', color: '#fff', fontSize: '14px', fontWeight: 600, cursor: 'pointer' },
  secondaryBtn:{ padding: '10px 20px', borderRadius: '4px', border: '1px solid #cbd5e0', background: '#fff', color: '#4a5568', fontSize: '14px', fontWeight: 500, textDecoration: 'none', display: 'inline-block' },
};

export default AppointmentConfirmation;
