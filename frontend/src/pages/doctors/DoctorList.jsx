import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorComponent from '../../components/ErrorComponent';
import { useToast } from '../../components/Toast';
import { useAuth } from '../../hooks/useAuth';
import { activateDoctor, deactivateDoctor, getDoctors } from '../../api/doctorApi';

const SPECIALIZATIONS = ['Cardiology', 'Neurology', 'Orthopedics', 'Pediatrics', 'Oncology', 'Radiology', 'Surgery', 'Dermatology', 'Psychiatry', 'General Medicine'];
const DEPARTMENTS     = ['Cardiac Care', 'Neurology Unit', 'Orthopedic Ward', 'Pediatric Ward', 'Oncology Unit', 'Radiology', 'Surgical Unit', 'Dermatology', 'Psychiatry', 'General'];
const STATUSES        = ['AVAILABLE', 'UNAVAILABLE', 'ON_LEAVE'];
const STATUS_LABELS   = { AVAILABLE: 'Available', UNAVAILABLE: 'Unavailable', ON_LEAVE: 'On Leave' };
const STATUS_COLORS   = { AVAILABLE: '#276749', UNAVAILABLE: '#c53030', ON_LEAVE: '#744210' };

function DoctorList() {
  const { user }       = useAuth();
  const { showToast }  = useToast();
  const navigate       = useNavigate();
  const isAdmin        = user?.role === 'ADMIN';

  const [doctors,   setDoctors]   = useState([]);
  const [loading,   setLoading]   = useState(false);
  const [error,     setError]     = useState(null);
  const [page,      setPage]      = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [filters, setFilters] = useState({
    specialization: '',
    department: '',
    status: '',
    activeOnly: '',
  });

  const fetchDoctors = useCallback(async (currentPage = 0, currentFilters = filters) => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: currentPage, size: 10, sort: 'name' };
      if (currentFilters.specialization) params.specialization = currentFilters.specialization;
      if (currentFilters.department)     params.department     = currentFilters.department;
      if (currentFilters.status)         params.status         = currentFilters.status;
      if (currentFilters.activeOnly !== '') params.activeOnly  = currentFilters.activeOnly;

      const data = await getDoctors(params);
      setDoctors(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      setPage(data.number);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load doctors.');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    fetchDoctors(0, filters);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  function handleFilterChange(e) {
    setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleSearch(e) {
    e.preventDefault();
    fetchDoctors(0, filters);
  }

  function handleReset() {
    const cleared = { specialization: '', department: '', status: '', activeOnly: '' };
    setFilters(cleared);
    fetchDoctors(0, cleared);
  }

  async function handleToggleActive(doctor) {
    try {
      if (doctor.isActive) {
        await deactivateDoctor(doctor.doctorId);
        showToast(`${doctor.name} deactivated.`, 'warning');
      } else {
        await activateDoctor(doctor.doctorId);
        showToast(`${doctor.name} activated.`, 'success');
      }
      fetchDoctors(page, filters);
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
              <h2 style={styles.pageTitle}>Doctors</h2>
              <p style={styles.pageSubtitle}>{totalElements} doctor{totalElements !== 1 ? 's' : ''} found</p>
            </div>
            {isAdmin && (
              <Link to="/doctors/new" style={styles.primaryBtn}>
                + New Doctor
              </Link>
            )}
          </div>

          {/* Filters */}
          <div style={styles.filterCard}>
            <form onSubmit={handleSearch} style={styles.filterRow}>
              <select name="specialization" value={filters.specialization} onChange={handleFilterChange} style={styles.select}>
                <option value="">All Specializations</option>
                {SPECIALIZATIONS.map(s => <option key={s} value={s}>{s}</option>)}
              </select>

              <select name="department" value={filters.department} onChange={handleFilterChange} style={styles.select}>
                <option value="">All Departments</option>
                {DEPARTMENTS.map(d => <option key={d} value={d}>{d}</option>)}
              </select>

              <select name="status" value={filters.status} onChange={handleFilterChange} style={styles.select}>
                <option value="">All Statuses</option>
                {STATUSES.map(s => <option key={s} value={s}>{STATUS_LABELS[s]}</option>)}
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
            <ErrorComponent message={error} onRetry={() => fetchDoctors(page, filters)} />
          )}

          {!loading && !error && (
            <>
              {doctors.length === 0 ? (
                <div style={styles.emptyState}>
                  <p>No doctors found. Try adjusting your filters.</p>
                  {isAdmin && <Link to="/doctors/new" style={styles.primaryBtn}>Register first doctor</Link>}
                </div>
              ) : (
                <div style={styles.tableWrapper}>
                  <table style={styles.table}>
                    <thead>
                      <tr style={styles.thead}>
                        <th style={styles.th}>Name</th>
                        <th style={styles.th}>Specialization</th>
                        <th style={styles.th}>Department</th>
                        <th style={styles.th}>Experience</th>
                        <th style={styles.th}>Availability</th>
                        <th style={styles.th}>Status</th>
                        <th style={styles.th}>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {doctors.map((doc, i) => (
                        <tr key={doc.doctorId} style={i % 2 === 0 ? styles.trEven : styles.trOdd}>
                          <td style={styles.td}>
                            <Link to={`/doctors/${doc.doctorId}`} style={styles.nameLink}>
                              {doc.name}
                            </Link>
                            <div style={styles.emailText}>{doc.email}</div>
                          </td>
                          <td style={styles.td}>{doc.specialization}</td>
                          <td style={styles.td}>{doc.department}</td>
                          <td style={styles.td}>{doc.experienceYears != null ? `${doc.experienceYears} yr` : '—'}</td>
                          <td style={styles.td}>
                            <StatusBadge status={doc.availabilityStatus} />
                          </td>
                          <td style={styles.td}>
                            <span style={doc.isActive ? styles.activeChip : styles.inactiveChip}>
                              {doc.isActive ? 'Active' : 'Inactive'}
                            </span>
                          </td>
                          <td style={styles.td}>
                            <div style={styles.actionRow}>
                              <Link to={`/doctors/${doc.doctorId}`} style={styles.viewBtn}>View</Link>
                              {isAdmin && (
                                <>
                                  <Link to={`/doctors/${doc.doctorId}/edit`} style={styles.editBtn}>Edit</Link>
                                  <button
                                    onClick={() => handleToggleActive(doc)}
                                    style={doc.isActive ? styles.deactivateBtn : styles.activateBtn}
                                  >
                                    {doc.isActive ? 'Deactivate' : 'Activate'}
                                  </button>
                                </>
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
                    onClick={() => fetchDoctors(page - 1, filters)}
                    disabled={page === 0}
                    style={page === 0 ? styles.pageBtn_disabled : styles.pageBtn}
                  >
                    Previous
                  </button>
                  <span style={styles.pageInfo}>
                    Page {page + 1} of {totalPages}
                  </span>
                  <button
                    onClick={() => fetchDoctors(page + 1, filters)}
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

function StatusBadge({ status }) {
  const bg    = STATUS_COLORS[status] ?? '#4a5568';
  const label = STATUS_LABELS[status] ?? status;
  return (
    <span style={{ ...styles.statusBadge, background: bg }}>
      {label}
    </span>
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

  statusBadge:  { fontSize: '11px', fontWeight: 600, padding: '3px 8px', borderRadius: '12px', color: '#fff', display: 'inline-block' },
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

export default DoctorList;
