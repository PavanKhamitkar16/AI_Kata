import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorComponent from '../../components/ErrorComponent';
import { getDoctorById } from '../../api/doctorApi';
import { getAvailableSlots } from '../../api/appointmentApi';

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

function DoctorAvailability() {
  const { id }    = useParams();
  const navigate  = useNavigate();

  const [doctor,   setDoctor]   = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);

  const today = new Date();
  const [viewYear,  setViewYear]  = useState(today.getFullYear());
  const [viewMonth, setViewMonth] = useState(today.getMonth()); // 0-based

  const [selectedDate, setSelectedDate] = useState(null);
  const [slots,        setSlots]        = useState([]);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [slotsError,   setSlotsError]   = useState(null);

  useEffect(() => {
    getDoctorById(id)
      .then(setDoctor)
      .catch(err => setError(err.response?.data?.message || 'Failed to load doctor.'))
      .finally(() => setLoading(false));
  }, [id]);

  async function handleSelectDate(dateStr) {
    setSelectedDate(dateStr);
    setSlots([]);
    setSlotsError(null);
    setSlotsLoading(true);
    try {
      const data = await getAvailableSlots(id, dateStr);
      setSlots(data);
    } catch (err) {
      const msg = err.response?.data?.message || '';
      // If doctor has no schedule for that day, show friendly message not error
      if (msg.includes('no working schedule')) {
        setSlotsError('Doctor does not work on this day.');
      } else {
        setSlotsError(msg || 'Failed to load slots.');
      }
    } finally {
      setSlotsLoading(false);
    }
  }

  function prevMonth() {
    if (viewMonth === 0) { setViewYear(y => y - 1); setViewMonth(11); }
    else { setViewMonth(m => m - 1); }
  }
  function nextMonth() {
    if (viewMonth === 11) { setViewYear(y => y + 1); setViewMonth(0); }
    else { setViewMonth(m => m + 1); }
  }

  /** Build calendar grid: array of {dateStr, day, isPast, isToday} */
  function buildCalendar() {
    const firstDay    = new Date(viewYear, viewMonth, 1).getDay();
    const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();
    const todayStr    = formatDate(today);
    const cells = [];
    // Leading empty cells
    for (let i = 0; i < firstDay; i++) cells.push(null);
    for (let d = 1; d <= daysInMonth; d++) {
      const dt  = new Date(viewYear, viewMonth, d);
      const str = formatDate(dt);
      cells.push({
        day: d,
        dateStr: str,
        isPast: str < todayStr,
        isToday: str === todayStr,
      });
    }
    return cells;
  }

  function formatDate(dt) {
    const y = dt.getFullYear();
    const m = String(dt.getMonth() + 1).padStart(2, '0');
    const d = String(dt.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  function formatTime(t) {
    if (!t) return '';
    return t.substring(0, 5);
  }

  const cells = buildCalendar();

  return (
    <div style={styles.layout}>
      <Sidebar />
      <div style={styles.main}>
        <Header />
        <main style={styles.content}>
          <div style={styles.breadcrumb}>
            <button onClick={() => navigate('/doctors')} style={styles.backLink}>Doctors</button>
            <span style={styles.sep}>/</span>
            <button onClick={() => navigate(`/doctors/${id}`)} style={styles.backLink}>
              {doctor?.name ?? 'Profile'}
            </button>
            <span style={styles.sep}>/</span>
            <span>Availability</span>
          </div>

          {loading && <div style={{ padding: '60px', textAlign: 'center' }}><LoadingSpinner size="md" /></div>}
          {error && !loading && <ErrorComponent message={error} />}

          {!loading && !error && doctor && (
            <div style={styles.wrapper}>
              {/* Doctor header */}
              <div style={styles.doctorCard}>
                <div style={styles.avatar}>{doctor.name.charAt(0).toUpperCase()}</div>
                <div>
                  <h2 style={styles.doctorName}>{doctor.name}</h2>
                  <p style={styles.doctorSub}>{doctor.specialization} · {doctor.department}</p>
                </div>
              </div>

              <div style={styles.twoCol}>
                {/* Calendar */}
                <div style={styles.calCard}>
                  <div style={styles.calNav}>
                    <button onClick={prevMonth} style={styles.navBtn}>&#8249;</button>
                    <span style={styles.calTitle}>
                      {MONTH_NAMES[viewMonth]} {viewYear}
                    </span>
                    <button onClick={nextMonth} style={styles.navBtn}>&#8250;</button>
                  </div>

                  <div style={styles.calGrid}>
                    {DAY_NAMES.map(d => (
                      <div key={d} style={styles.dayHeader}>{d}</div>
                    ))}
                    {cells.map((cell, i) => {
                      if (!cell) return <div key={`empty-${i}`} />;
                      const isSelected = cell.dateStr === selectedDate;
                      let cellStyle = { ...styles.calCell };
                      if (cell.isPast)    cellStyle = { ...cellStyle, ...styles.calCellPast };
                      if (cell.isToday)   cellStyle = { ...cellStyle, ...styles.calCellToday };
                      if (isSelected)     cellStyle = { ...cellStyle, ...styles.calCellSelected };
                      return (
                        <button
                          key={cell.dateStr}
                          style={cellStyle}
                          onClick={() => !cell.isPast && handleSelectDate(cell.dateStr)}
                          disabled={cell.isPast}
                        >
                          {cell.day}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Slots panel */}
                <div style={styles.slotsPanel}>
                  {!selectedDate && (
                    <p style={styles.slotsHint}>Select a date to see available time slots.</p>
                  )}
                  {selectedDate && (
                    <>
                      <h3 style={styles.slotsTitle}>Slots for {selectedDate}</h3>
                      {slotsLoading && <LoadingSpinner size="sm" />}
                      {slotsError && !slotsLoading && (
                        <p style={styles.slotsError}>{slotsError}</p>
                      )}
                      {!slotsLoading && !slotsError && slots.length === 0 && (
                        <p style={styles.slotsHint}>No slots generated for this day.</p>
                      )}
                      {!slotsLoading && !slotsError && slots.length > 0 && (
                        <div style={styles.slotGrid}>
                          {slots.map((slot, i) => (
                            <div key={i} style={slot.available ? styles.slotAvail : styles.slotBooked}>
                              <span style={styles.slotTime}>
                                {formatTime(slot.startTime)} – {formatTime(slot.endTime)}
                              </span>
                              {slot.available ? (
                                <Link
                                  to={`/appointments/book?doctorId=${id}&date=${selectedDate}&time=${slot.startTime}`}
                                  style={styles.bookBtn}
                                >
                                  Book
                                </Link>
                              ) : (
                                <span style={styles.bookedLabel}>Booked</span>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                    </>
                  )}
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

const styles = {
  layout:    { display: 'flex', minHeight: '100vh', fontFamily: 'system-ui, sans-serif' },
  main:      { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' },
  content:   { flex: 1, padding: '24px', background: '#f7fafc', overflowY: 'auto' },

  breadcrumb: { display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px', fontSize: '14px', color: '#718096' },
  backLink:   { background: 'none', border: 'none', color: '#3182ce', cursor: 'pointer', fontSize: '14px', padding: 0 },
  sep:        { color: '#cbd5e0' },

  wrapper:    { maxWidth: '900px' },

  doctorCard: { display: 'flex', alignItems: 'center', gap: '16px', background: '#fff', borderRadius: '8px', padding: '20px', marginBottom: '20px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)' },
  avatar:     { width: '52px', height: '52px', borderRadius: '50%', background: '#3182ce', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '22px', fontWeight: 700, flexShrink: 0 },
  doctorName: { margin: '0 0 4px', fontSize: '18px', fontWeight: 700, color: '#1a202c' },
  doctorSub:  { margin: 0, fontSize: '13px', color: '#718096' },

  twoCol:     { display: 'grid', gridTemplateColumns: 'auto 1fr', gap: '20px', alignItems: 'start' },

  calCard:    { background: '#fff', borderRadius: '8px', padding: '20px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)', width: '280px' },
  calNav:     { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' },
  calTitle:   { fontWeight: 700, fontSize: '15px', color: '#1a202c' },
  navBtn:     { background: 'none', border: '1px solid #e2e8f0', borderRadius: '4px', width: '28px', height: '28px', cursor: 'pointer', fontSize: '18px', lineHeight: 1, color: '#4a5568', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  calGrid:    { display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '2px' },
  dayHeader:  { textAlign: 'center', fontSize: '11px', fontWeight: 600, color: '#718096', paddingBottom: '6px' },
  calCell:    { border: 'none', background: 'none', cursor: 'pointer', borderRadius: '4px', padding: '6px 0', textAlign: 'center', fontSize: '13px', color: '#2d3748', ':hover': { background: '#ebf8ff' } },
  calCellPast:     { color: '#cbd5e0', cursor: 'not-allowed' },
  calCellToday:    { background: '#ebf8ff', color: '#3182ce', fontWeight: 700 },
  calCellSelected: { background: '#3182ce', color: '#fff', fontWeight: 700 },

  slotsPanel: { background: '#fff', borderRadius: '8px', padding: '20px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)', minHeight: '200px' },
  slotsTitle: { margin: '0 0 16px', fontSize: '15px', fontWeight: 700, color: '#1a202c' },
  slotsHint:  { color: '#718096', fontSize: '14px' },
  slotsError: { color: '#c53030', fontSize: '14px' },

  slotGrid:   { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '8px' },
  slotAvail:  { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 12px', border: '1px solid #9ae6b4', borderRadius: '6px', background: '#f0fff4' },
  slotBooked: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 12px', border: '1px solid #e2e8f0', borderRadius: '6px', background: '#f7fafc' },
  slotTime:   { fontSize: '13px', fontWeight: 600, color: '#2d3748' },
  bookBtn:    { padding: '4px 12px', borderRadius: '4px', background: '#3182ce', color: '#fff', fontSize: '12px', fontWeight: 600, textDecoration: 'none', border: 'none' },
  bookedLabel:{ fontSize: '11px', color: '#a0aec0', fontWeight: 600 },
};

export default DoctorAvailability;
