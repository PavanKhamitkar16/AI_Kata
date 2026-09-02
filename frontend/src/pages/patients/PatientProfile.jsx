import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorComponent from '../../components/ErrorComponent';
import { useToast } from '../../components/Toast';
import { useAuth } from '../../hooks/useAuth';
import { activatePatient, deactivatePatient, getPatientById } from '../../api/patientApi';

const BG_LABELS     = { A_POS: 'A+', A_NEG: 'A−', B_POS: 'B+', B_NEG: 'B−', AB_POS: 'AB+', AB_NEG: 'AB−', O_POS: 'O+', O_NEG: 'O−' };
const GENDER_LABELS = { MALE: 'Male', FEMALE: 'Female', OTHER: 'Other' };

function PatientProfile() {
  const { id }        = useParams();
  const { user }      = useAuth();
  const { showToast } = useToast();
  const navigate      = useNavigate();
  const isAdmin       = user?.role === 'ADMIN';
  const canEdit       = user?.role === 'ADMIN' || user?.role === 'STAFF';

  const [patient,   setPatient]   = useState(null);
  const [loading,   setLoading]   = useState(true);
  const [error,     setError]     = useState(null);
  const [toggling,  setToggling]  = useState(false);

  async function fetchPatient() {
    setLoading(true);
    setError(null);
    try {
      const data = await getPatientById(id);
      setPatient(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load patient profile.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { fetchPatient(); }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  async function handleToggleActive() {
    const fullName = `${patient.firstName} ${patient.lastName}`;
    setToggling(true);
    try {
      if (patient.isActive) {
        const updated = await deactivatePatient(id);
        setPatient(updated);
        showToast(`${fullName} deactivated.`, 'warning');
      } else {
        const updated = await activatePatient(id);
        setPatient(updated);
        showToast(`${fullName} activated.`, 'success');
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
            <button onClick={() => navigate('/patients')} style={styles.backLink}>Patients</button>
            <span style={styles.sep}>/</span>
            <span>{patient ? `${patient.firstName} ${patient.lastName}` : 'Profile'}</span>
          </div>

          {loading && (
            <div style={{ padding: '60px', textAlign: 'center' }}>
              <LoadingSpinner size="md" />
            </div>
          )}

          {error && !loading && (
            <ErrorComponent message={error} onRetry={fetchPatient} />
          )}

          {!loading && !error && patient && (
            <div style={styles.card}>
              {/* Profile header */}
              <div style={styles.profileHeader}>
                <div style={styles.avatarCircle}>
                  {patient.firstName.charAt(0).toUpperCase()}
                </div>
                <div style={styles.profileMeta}>
                  <h2 style={styles.patientName}>{patient.firstName} {patient.lastName}</h2>
                  <p style={styles.patientSub}>
                    {GENDER_LABELS[patient.gender] ?? patient.gender}
                    {patient.dateOfBirth && ` · DOB: ${new Date(patient.dateOfBirth).toLocaleDateString('en-GB')}`}
                    {patient.bloodGroup && ` · Blood: ${BG_LABELS[patient.bloodGroup] ?? patient.bloodGroup}`}
                  </p>
                  <div style={styles.badgeRow}>
                    <span style={patient.isActive ? styles.activeChip : styles.inactiveChip}>
                      {patient.isActive ? 'Active' : 'Inactive'}
                    </span>
                    {patient.bloodGroup && (
                      <span style={styles.bgChip}>
                        {BG_LABELS[patient.bloodGroup]}
                      </span>
                    )}
                  </div>
                </div>
                <div style={styles.headerActions}>
                  {canEdit && (
                    <Link to={`/patients/${id}/edit`} style={styles.editBtn}>Edit</Link>
                  )}
                  {isAdmin && (
                    <button
                      onClick={handleToggleActive}
                      disabled={toggling}
                      style={patient.isActive ? styles.deactivateBtn : styles.activateBtn}
                    >
                      {toggling ? '...' : (patient.isActive ? 'Deactivate' : 'Activate')}
                    </button>
                  )}
                </div>
              </div>

              {/* Contact section */}
              <SectionTitle>Contact</SectionTitle>
              <div style={styles.detailGrid}>
                <DetailItem label="Phone"             value={patient.phone} />
                <DetailItem label="Email"             value={patient.email} />
                <DetailItem label="Address"           value={patient.address} />
                <DetailItem label="Emergency Contact" value={patient.emergencyContact} />
              </div>

              {/* Medical section */}
              <SectionTitle>Medical</SectionTitle>
              <div style={styles.detailGrid}>
                <DetailItem label="Blood Group"     value={patient.bloodGroup ? BG_LABELS[patient.bloodGroup] : null} />
                <DetailItem label="Allergies"       value={patient.allergies} />
                <DetailItem label="Medical History" value={patient.medicalHistory} wide />
              </div>

              {/* Audit section */}
              <SectionTitle>Record</SectionTitle>
              <div style={styles.detailGrid}>
                <DetailItem label="Registered"    value={formatDate(patient.createdAt)} />
                <DetailItem label="Last Updated"  value={formatDate(patient.updatedAt)} />
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

function SectionTitle({ children }) {
  return (
    <div style={{ borderBottom: '1px solid #e2e8f0', margin: '24px 0 16px', paddingBottom: '8px' }}>
      <h3 style={{ margin: 0, fontSize: '11px', fontWeight: 700, color: '#718096', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
        {children}
      </h3>
    </div>
  );
}

function DetailItem({ label, value, wide }) {
  return (
    <div style={{ ...(wide ? { gridColumn: '1 / -1' } : {}), display: 'flex', flexDirection: 'column', gap: '4px' }}>
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

  profileHeader: { display: 'flex', gap: '20px', alignItems: 'flex-start', flexWrap: 'wrap', marginBottom: '8px' },
  avatarCircle:  { width: '64px', height: '64px', borderRadius: '50%', background: '#48bb78', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '28px', fontWeight: 700, flexShrink: 0 },
  profileMeta:   { flex: 1 },
  patientName:   { margin: '0 0 4px', fontSize: '22px', fontWeight: 700, color: '#1a202c' },
  patientSub:    { margin: '0 0 10px', fontSize: '14px', color: '#718096' },
  badgeRow:      { display: 'flex', gap: '8px', flexWrap: 'wrap' },
  activeChip:    { fontSize: '11px', fontWeight: 600, padding: '3px 10px', borderRadius: '12px', background: '#c6f6d5', color: '#276749' },
  inactiveChip:  { fontSize: '11px', fontWeight: 600, padding: '3px 10px', borderRadius: '12px', background: '#fed7d7', color: '#c53030' },
  bgChip:        { fontSize: '12px', fontWeight: 700, padding: '2px 10px', borderRadius: '4px', background: '#ebf8ff', color: '#2b6cb0', border: '1px solid #bee3f8' },

  headerActions: { display: 'flex', gap: '8px', alignItems: 'flex-start', marginLeft: 'auto' },
  editBtn:       { padding: '8px 18px', borderRadius: '4px', background: '#fefcbf', color: '#744210', fontSize: '13px', textDecoration: 'none', border: '1px solid #faf089', fontWeight: 600 },
  activateBtn:   { padding: '8px 18px', borderRadius: '4px', background: '#f0fff4', color: '#276749', fontSize: '13px', border: '1px solid #9ae6b4', cursor: 'pointer', fontWeight: 600 },
  deactivateBtn: { padding: '8px 18px', borderRadius: '4px', background: '#fff5f5', color: '#c53030', fontSize: '13px', border: '1px solid #fed7d7', cursor: 'pointer', fontWeight: 600 },

  detailGrid:   { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '20px' },
  detailLabel:  { fontSize: '11px', fontWeight: 600, color: '#718096', textTransform: 'uppercase', letterSpacing: '0.5px' },
  detailValue:  { fontSize: '14px', color: '#2d3748', fontWeight: 500, whiteSpace: 'pre-wrap' },
};

export default PatientProfile;
