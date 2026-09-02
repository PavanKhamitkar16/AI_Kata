import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorComponent from '../../components/ErrorComponent';
import { useToast } from '../../components/Toast';
import { useAuth } from '../../hooks/useAuth';
import { activatePatient, deactivatePatient, getPatients } from '../../api/patientApi';

const BLOOD_GROUPS = ['A_POS', 'A_NEG', 'B_POS', 'B_NEG', 'AB_POS', 'AB_NEG', 'O_POS', 'O_NEG'];
const BG_LABELS    = { A_POS: 'A+', A_NEG: 'A−', B_POS: 'B+', B_NEG: 'B−', AB_POS: 'AB+', AB_NEG: 'AB−', O_POS: 'O+', O_NEG: 'O−' };
const GENDER_LABELS = { MALE: 'Male', FEMALE: 'Female', OTHER: 'Other' };

function PatientList() {
  const { user }      = useAuth();
  const { showToast } = useToast();
  const navigate      = useNavigate();
  const isAdmin       = user?.role === 'ADMIN';
  const canEdit       = user?.role === 'ADMIN' || user?.role === 'STAFF';

  const [patients,      setPatients]      = useState([]);
  const [loading,       setLoading]       = useState(false);
  const [error,         setError]         = useState(null);
  const [page,          setPage]          = useState(0);
  const [totalPages,    setTotalPages]    = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [filters, setFilters] = useState({ name: '', bloodGroup: '', activeOnly: '' });

  const fetchPatients = useCallback(async (currentPage = 0, currentFilters = filters) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: currentPage, size: 10, sort: 'lastName' };
      if (currentFilters.name)        params.name       = currentFilters.name;
      if (currentFilters.bloodGroup)  params.bloodGroup = currentFilters.bloodGroup;
      if (currentFilters.activeOnly !== '') params.activeOnly = currentFilters.activeOnly;

      const data = await getPatients(params);
      setPatients(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      setPage(data.number);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load patients.');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    fetchPatients(0, filters);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  function handleFilterChange(e) {
    setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleSearch(e) {
    e.preventDefault();
    fetchPatients(0, filters);
  }

  function handleReset() {
    const cleared = { name: '', bloodGroup: '', activeOnly: '' };
    setFilters(cleared);
    fetchPatients(0, cleared);
  }

  async function handleToggleActive(patient) {
    const fullName = `${patient.firstName} ${patient.lastName}`;
    try {
      if (patient.isActive) {
        await deactivatePatient(patient.patientId);
        showToast(`${fullName} deactivated.`, 'warning');
      } else {
        await activatePatient(patient.patientId);
        showToast(`${fullName} activated.`, 'success');
      }
      fetchPatients(page, filters);
    } catch (err) {
      showToast(err.response?.data?.message || 'Action failed.', 'error');
    }
  }

  return (
    <div style={styles.layout}>
      <Sidebar />
      <div style={styles.main}>
        <Header />
        <main style={styles.content}>
          {/* Page header */}
          <div style={styles.pageHeader}>
            <div>
              <h2 style={styles.pageTitle}>Patients</h2>
              <p style={styles.pageSubtitle}>{totalElements} patient{totalElements !== 1 ? 's' : ''} found</p>
            </div>
            {canEdit && (
              <Link to="/patients/new" style={styles.primaryBtn}>
                + New Patient
              </Link>
            )}
          </div>

          {/* Filters */}
          <div style={styles.filterCard}>
            <form onSubmit={handleSearch} style={styles.filterRow}>
              <input
                name="name"
                value={filters.name}
                onChange={handleFilterChange}
                placeholder="Search by name..."
                style={styles.searchInput}
              />

              <select name="bloodGroup" value={filters.bloodGroup} onChange={handleFilterChange} style={styles.select}>
                <option value="">All Blood Groups</option>
                {BLOOD_GROUPS.map(bg => (
                  <option key={bg} value={bg}>{BG_LABELS[bg]}</option>
                ))}
              </select>

              <select name="activeOnly" value={filters.activeOnly} onChange={handleFilterChange} style={styles.select}>
                <option value="">Active + Inactive</option>
                <option value="true">Active only</option>
                <option value="false">Inactive only</option>
              </select>

              <button type="submit" style={styles.searchBtn}>Search</button>
              <button type="button" onClick={handleReset} style={styles.resetBtn}>Reset</button>
            </form>
          </div>

          {/* Content */}
          {loading && (
            <div style={{ padding: '40px', textAlign: 'center' }}>
              <LoadingSpinner size="md" />
            </div>
          )}

          {error && !loading && (
            <ErrorComponent message={error} onRetry={() => fetchPatients(page, filters)} />
          )}

          {!loading && !error && (
            <>
              {patients.length === 0 ? (
                <div style={styles.emptyState}>
                  <p>No patients found. Try adjusting your filters.</p>
                  {canEdit && <Link to="/patients/new" style={styles.primaryBtn}>Register first patient</Link>}
                </div>
              ) : (
                <div style={styles.tableWrapper}>
                  <table style={styles.table}>
                    <thead>
                      <tr style={styles.thead}>
                        <th style={styles.th}>Name</th>
                        <th style={styles.th}>Gender</th>
                        <th style={styles.th}>Date of Birth</th>
                        <th style={styles.th}>Phone</th>
                        <th style={styles.th}>Blood Group</th>
                        <th style={styles.th}>Status</th>
                        <th style={styles.th}>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {patients.map((pt, i) => (
                        <tr key={pt.patientId} style={i % 2 === 0 ? styles.trEven : styles.trOdd}>
                          <td style={styles.td}>
                            <Link to={`/patients/${pt.patientId}`} style={styles.nameLink}>
                              {pt.firstName} {pt.lastName}
                            </Link>
                            {pt.email && <div style={styles.emailText}>{pt.email}</div>}
                          </td>
                          <td style={styles.td}>{GENDER_LABELS[pt.gender] ?? pt.gender}</td>
                          <td style={styles.td}>{pt.dateOfBirth ? new Date(pt.dateOfBirth).toLocaleDateString('en-GB') : '—'}</td>
                          <td style={styles.td}>{pt.phone}</td>
                          <td style={styles.td}>
                            {pt.bloodGroup
                              ? <span style={styles.bgChip}>{BG_LABELS[pt.bloodGroup] ?? pt.bloodGroup}</span>
                              : '—'}
                          </td>
                          <td style={styles.td}>
                            <span style={pt.isActive ? styles.activeChip : styles.inactiveChip}>
                              {pt.isActive ? 'Active' : 'Inactive'}
                            </span>
                          </td>
                          <td style={styles.td}>
                            <div style={styles.actionRow}>
                              <Link to={`/patients/${pt.patientId}`} style={styles.viewBtn}>View</Link>
                              {canEdit && (
                                <Link to={`/patients/${pt.patientId}/edit`} style={styles.editBtn}>Edit</Link>
                              )}
                              {isAdmin && (
                                <button
                                  onClick={() => handleToggleActive(pt)}
                                  style={pt.isActive ? styles.deactivateBtn : styles.activateBtn}
                                >
                                  {pt.isActive ? 'Deactivate' : 'Activate'}
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              {/* Pagination */}
              {totalPages > 1 && (
                <div style={styles.pagination}>
                  <button
                    onClick={() => fetchPatients(page - 1, filters)}
                    disabled={page === 0}
                    style={page === 0 ? styles.pageBtn_disabled : styles.pageBtn}
                  >
                    Previous
                  </button>
                  <span style={styles.pageInfo}>
                    Page {page + 1} of {totalPages}
                  </span>
                  <button
                    onClick={() => fetchPatients(page + 1, filters)}
                    disabled={page >= totalPages - 1}
                    style={page >= totalPages - 1 ? styles.pageBtn_disabled : styles.pageBtn}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
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

  filterCard:   { background: '#fff', borderRadius: '8px', padding: '16px', marginBottom: '20px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)' },
  filterRow:    { display: 'flex', gap: '10px', flexWrap: 'wrap', alignItems: 'center' },
  searchInput:  { padding: '8px 12px', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '13px', minWidth: '200px', flex: 1 },
  select:       { padding: '8px 10px', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '13px', background: '#fff', color: '#2d3748', minWidth: '160px' },

  tableWrapper: { background: '#fff', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' },
  table:        { width: '100%', borderCollapse: 'collapse', fontSize: '14px' },
  thead:        { background: '#edf2f7' },
  th:           { padding: '12px 16px', textAlign: 'left', fontSize: '12px', fontWeight: 600, color: '#4a5568', textTransform: 'uppercase', letterSpacing: '0.5px', borderBottom: '2px solid #e2e8f0' },
  trEven:       { background: '#fff' },
  trOdd:        { background: '#f7fafc' },
  td:           { padding: '12px 16px', borderBottom: '1px solid #e2e8f0', color: '#2d3748', verticalAlign: 'middle' },

  nameLink:     { fontWeight: 600, color: '#3182ce', textDecoration: 'none' },
  emailText:    { fontSize: '12px', color: '#718096', marginTop: '2px' },

  bgChip:       { fontSize: '12px', fontWeight: 700, padding: '2px 8px', borderRadius: '4px', background: '#ebf8ff', color: '#2b6cb0', border: '1px solid #bee3f8' },
  activeChip:   { fontSize: '11px', fontWeight: 600, padding: '3px 8px', borderRadius: '12px', background: '#c6f6d5', color: '#276749' },
  inactiveChip: { fontSize: '11px', fontWeight: 600, padding: '3px 8px', borderRadius: '12px', background: '#fed7d7', color: '#c53030' },

  actionRow:    { display: 'flex', gap: '6px', flexWrap: 'wrap' },
  viewBtn:      { padding: '4px 10px', borderRadius: '4px', background: '#ebf8ff', color: '#2b6cb0', fontSize: '12px', textDecoration: 'none', border: '1px solid #bee3f8', fontWeight: 500 },
  editBtn:      { padding: '4px 10px', borderRadius: '4px', background: '#fefcbf', color: '#744210', fontSize: '12px', textDecoration: 'none', border: '1px solid #faf089', fontWeight: 500 },
  activateBtn:  { padding: '4px 10px', borderRadius: '4px', background: '#f0fff4', color: '#276749', fontSize: '12px', border: '1px solid #9ae6b4', cursor: 'pointer', fontWeight: 500 },
  deactivateBtn:{ padding: '4px 10px', borderRadius: '4px', background: '#fff5f5', color: '#c53030', fontSize: '12px', border: '1px solid #fed7d7', cursor: 'pointer', fontWeight: 500 },

  primaryBtn:   { padding: '9px 18px', borderRadius: '4px', background: '#3182ce', color: '#fff', fontSize: '14px', fontWeight: 600, textDecoration: 'none', border: 'none', cursor: 'pointer', display: 'inline-block' },
  searchBtn:    { padding: '8px 18px', borderRadius: '4px', background: '#3182ce', color: '#fff', fontSize: '13px', fontWeight: 600, border: 'none', cursor: 'pointer' },
  resetBtn:     { padding: '8px 14px', borderRadius: '4px', background: '#edf2f7', color: '#4a5568', fontSize: '13px', border: '1px solid #cbd5e0', cursor: 'pointer' },

  emptyState:   { textAlign: 'center', padding: '60px 24px', color: '#718096', background: '#fff', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)' },

  pagination:   { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '16px', marginTop: '20px' },
  pageBtn:      { padding: '7px 16px', borderRadius: '4px', border: '1px solid #cbd5e0', background: '#fff', color: '#2d3748', fontSize: '13px', cursor: 'pointer' },
  pageBtn_disabled: { padding: '7px 16px', borderRadius: '4px', border: '1px solid #e2e8f0', background: '#f7fafc', color: '#a0aec0', fontSize: '13px', cursor: 'not-allowed' },
  pageInfo:     { fontSize: '13px', color: '#718096' },
};

export default PatientList;
