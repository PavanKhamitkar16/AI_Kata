import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorComponent from '../../components/ErrorComponent';
import { useToast } from '../../components/Toast';
import { useAuth } from '../../hooks/useAuth';
import { activateDoctor, deactivateDoctor, getDoctorById } from '../../api/doctorApi';

const STATUS_LABELS = { AVAILABLE: 'Available', UNAVAILABLE: 'Unavailable', ON_LEAVE: 'On Leave' };
const STATUS_COLORS = { AVAILABLE: '#276749', UNAVAILABLE: '#c53030', ON_LEAVE: '#744210' };

function DoctorProfile() {
  const { id }        = useParams();
  const { user }      = useAuth();
  const { showToast } = useToast();
  const navigate      = useNavigate();
  const isAdmin       = user?.role === 'ADMIN';

  const [doctor,  setDoctor]  = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);
  const [toggling, setToggling] = useState(false);

  async function fetchDoctor() {
    setLoading(true);
    setError(null);
    try {
      const data = await getDoctorById(id);
      setDoctor(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load doctor profile.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { fetchDoctor(); }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  async function handleToggleActive() {
    setToggling(true);
    try {
      if (doctor.isActive) {
        const updated = await deactivateDoctor(id);
        setDoctor(updated);
        showToast(`${doctor.name} deactivated.`, 'warning');
      } else {
        const updated = await activateDoctor(id);
        setDoctor(updated);
        showToast(`${doctor.name} activated.`, 'success');
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Action failed.', 'error');
    } finally {
      setToggling(false);
    }
  }

  return (
    <div style={styles.layout}>
      <Sidebar />
      <div style={styles.main}>
        <Header />
        <main style={styles.content}>
          <div style={styles.breadcrumb}>
            <button onClick={() => navigate('/doctors')} style={styles.backLink}>Doctors</button>
            <span style={styles.sep}>/</span>
            <span>{doctor?.name ?? 'Profile'}</span>
          </div>

          {loading && (
            <div style={{ padding: '60px', textAlign: 'center' }}>
              <LoadingSpinner size="md" />
            </div>
          )}

          {error && !loading && (
            <ErrorComponent message={error} onRetry={fetchDoctor} />
          )}

          {!loading && !error && doctor && (
            <div style={styles.card}>
              {/* Profile header */}
              <div style={styles.profileHeader}>
                <div style={styles.avatarCircle}>
                  {doctor.name.charAt(0).toUpperCase()}
                </div>
                <div style={styles.profileMeta}>
                  <h2 style={styles.doctorName}>{doctor.name}</h2>
                  <p style={styles.doctorSub}>{doctor.specialization} · {doctor.department}</p>
                  <div style={styles.badgeRow}>
                    <span style={{ ...styles.statusBadge, background: STATUS_COLORS[doctor.availabilityStatus] ?? '#4a5568' }}>
                      {STATUS_LABELS[doctor.availabilityStatus] ?? doctor.availabilityStatus}
                    </span>
                    <span style={doctor.isActive ? styles.activeChip : styles.inactiveChip}>
                      {doctor.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                </div>
                {isAdmin && (
                  <div style={styles.headerActions}>
                    <Link to={`/doctors/${id}/edit`} style={styles.editBtn}>Edit</Link>
                    <button
                      onClick={handleToggleActive}
                      disabled={toggling}
                      style={doctor.isActive ? styles.deactivateBtn : styles.activateBtn}
                    >
                      {toggling ? '...' : (doctor.isActive ? 'Deactivate' : 'Activate')}
                    </button>
                  </div>
                )}
              </div>

              {/* Detail grid */}
              <div style={styles.divider} />
              <div style={styles.detailGrid}>
                <DetailItem label="Email"          value={doctor.email} />
                <DetailItem label="Phone"          value={doctor.phone} />
                <DetailItem label="Qualification"  value={doctor.qualification} />
                <DetailItem label="Experience"     value={doctor.experienceYears != null ? `${doctor.experienceYears} years` : '—'} />
                <DetailItem label="Department"     value={doctor.department} />
                <DetailItem label="Specialization" value={doctor.specialization} />
                <DetailItem label="Created"        value={formatDate(doctor.createdAt)} />
                <DetailItem label="Last Updated"   value={formatDate(doctor.updatedAt)} />
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

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
}

const styles = {
  layout:    { display: 'flex', minHeight: '100vh', fontFamily: 'system-ui, sans-serif' },
  main:      { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' },
  content:   { flex: 1, padding: '24px', background: '#f7fafc', overflowY: 'auto' },

  breadcrumb:{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px', fontSize: '14px', color: '#718096' },
  backLink:  { background: 'none', border: 'none', color: '#3182ce', cursor: 'pointer', fontSize: '14px', padding: 0 },
  sep:       { color: '#cbd5e0' },

  card:      { background: '#fff', borderRadius: '8px', padding: '32px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)', maxWidth: '820px' },

  profileHeader: { display: 'flex', gap: '20px', alignItems: 'flex-start', flexWrap: 'wrap' },
  avatarCircle:  { width: '64px', height: '64px', borderRadius: '50%', background: '#3182ce', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '28px', fontWeight: 700, flexShrink: 0 },
  profileMeta:   { flex: 1 },
  doctorName:    { margin: '0 0 4px', fontSize: '22px', fontWeight: 700, color: '#1a202c' },
  doctorSub:     { margin: '0 0 10px', fontSize: '14px', color: '#718096' },
  badgeRow:      { display: 'flex', gap: '8px', flexWrap: 'wrap' },
  statusBadge:   { fontSize: '11px', fontWeight: 600, padding: '3px 10px', borderRadius: '12px', color: '#fff' },
  activeChip:    { fontSize: '11px', fontWeight: 600, padding: '3px 10px', borderRadius: '12px', background: '#c6f6d5', color: '#276749' },
  inactiveChip:  { fontSize: '11px', fontWeight: 600, padding: '3px 10px', borderRadius: '12px', background: '#fed7d7', color: '#c53030' },

  headerActions: { display: 'flex', gap: '8px', alignItems: 'flex-start', marginLeft: 'auto' },
  editBtn:       { padding: '8px 18px', borderRadius: '4px', background: '#fefcbf', color: '#744210', fontSize: '13px', textDecoration: 'none', border: '1px solid #faf089', fontWeight: 600 },
  activateBtn:   { padding: '8px 18px', borderRadius: '4px', background: '#f0fff4', color: '#276749', fontSize: '13px', border: '1px solid #9ae6b4', cursor: 'pointer', fontWeight: 600 },
  deactivateBtn: { padding: '8px 18px', borderRadius: '4px', background: '#fff5f5', color: '#c53030', fontSize: '13px', border: '1px solid #fed7d7', cursor: 'pointer', fontWeight: 600 },

  divider:       { borderTop: '1px solid #e2e8f0', margin: '24px 0' },

  detailGrid:    { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '20px' },
  detailItem:    { display: 'flex', flexDirection: 'column', gap: '4px' },
  detailLabel:   { fontSize: '11px', fontWeight: 600, color: '#718096', textTransform: 'uppercase', letterSpacing: '0.5px' },
  detailValue:   { fontSize: '14px', color: '#2d3748', fontWeight: 500 },
};

export default DoctorProfile;
