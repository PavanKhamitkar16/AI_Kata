import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import { useToast } from '../../components/Toast';
import { createDoctor, getDoctorById, updateDoctor } from '../../api/doctorApi';

const EMPTY_FORM = {
  name: '',
  email: '',
  phone: '',
  specialization: '',
  qualification: '',
  experienceYears: '',
  department: '',
  availabilityStatus: 'AVAILABLE',
};

const SPECIALIZATIONS = ['Cardiology', 'Neurology', 'Orthopedics', 'Pediatrics', 'Oncology', 'Radiology', 'Surgery', 'Dermatology', 'Psychiatry', 'General Medicine'];
const DEPARTMENTS     = ['Cardiac Care', 'Neurology Unit', 'Orthopedic Ward', 'Pediatric Ward', 'Oncology Unit', 'Radiology', 'Surgical Unit', 'Dermatology', 'Psychiatry', 'General'];
const PHONE_RE        = /^\+?[0-9]{7,15}$/;
const EMAIL_RE        = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Doctor create / edit form.
 * Create mode: /doctors/new
 * Edit mode:   /doctors/:id/edit
 */
function DoctorForm() {
  const { id }        = useParams();
  const isEdit        = Boolean(id);
  const navigate      = useNavigate();
  const { showToast } = useToast();

  const [formData,    setFormData]    = useState(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading,     setLoading]     = useState(false);
  const [fetching,    setFetching]    = useState(isEdit);
  const [serverError, setServerError] = useState(null);

  // In edit mode, load existing data
  useEffect(() => {
    if (!isEdit) return;
    setFetching(true);
    getDoctorById(id)
      .then(data => {
        setFormData({
          name:               data.name            ?? '',
          email:              data.email           ?? '',
          phone:              data.phone           ?? '',
          specialization:     data.specialization  ?? '',
          qualification:      data.qualification   ?? '',
          experienceYears:    data.experienceYears != null ? String(data.experienceYears) : '',
          department:         data.department      ?? '',
          availabilityStatus: data.availabilityStatus ?? 'AVAILABLE',
        });
      })
      .catch(() => setServerError('Failed to load doctor data.'))
      .finally(() => setFetching(false));
  }, [id, isEdit]);

  function handleChange(e) {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setFieldErrors(prev => ({ ...prev, [name]: '' }));
    setServerError(null);
  }

  function validate() {
    const errors = {};
    if (!formData.name.trim())           errors.name           = 'Name is required.';
    if (!formData.email.trim())          errors.email          = 'Email is required.';
    else if (!EMAIL_RE.test(formData.email)) errors.email      = 'Enter a valid email address.';
    if (!formData.phone.trim())          errors.phone          = 'Phone is required.';
    else if (!PHONE_RE.test(formData.phone)) errors.phone      = 'Phone must be 7-15 digits.';
    if (!formData.specialization.trim()) errors.specialization = 'Specialization is required.';
    if (!formData.qualification.trim())  errors.qualification  = 'Qualification is required.';
    if (!formData.department.trim())     errors.department     = 'Department is required.';
    if (formData.experienceYears !== '') {
      const yr = Number(formData.experienceYears);
      if (!Number.isInteger(yr) || yr < 0 || yr > 60)
        errors.experienceYears = 'Experience must be 0-60 years.';
    }
    return errors;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length) {
      setFieldErrors(errors);
      return;
    }

    const payload = {
      ...formData,
      experienceYears: formData.experienceYears !== '' ? Number(formData.experienceYears) : null,
    };

    setLoading(true);
    setServerError(null);
    try {
      if (isEdit) {
        await updateDoctor(id, payload);
        showToast('Doctor updated successfully.', 'success');
        navigate(`/doctors/${id}`);
      } else {
        const created = await createDoctor(payload);
        showToast('Doctor registered successfully.', 'success');
        navigate(`/doctors/${created.doctorId}`);
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Something went wrong. Please try again.';
      setServerError(msg);
    } finally {
      setLoading(false);
    }
  }

  if (fetching) {
    return (
      <div style={styles.layout}>
        <Sidebar />
        <div style={styles.main}>
          <Header />
          <main style={styles.content}>
            <LoadingSpinner fullPage />
          </main>
        </div>
      </div>
    );
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
            <span>{isEdit ? 'Edit Doctor' : 'New Doctor'}</span>
          </div>

          <div style={styles.card}>
            <h2 style={styles.cardTitle}>{isEdit ? 'Edit Doctor Profile' : 'Register New Doctor'}</h2>

            {serverError && (
              <div role="alert" style={styles.errorBanner}>{serverError}</div>
            )}

            <form onSubmit={handleSubmit} noValidate style={styles.form}>
              {/* Row 1: Name + Email */}
              <div style={styles.row}>
                <Field label="Full Name" required error={fieldErrors.name}>
                  <input name="name" value={formData.name} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.name)}
                         placeholder="Dr. Jane Smith" />
                </Field>
                <Field label="Email" required error={fieldErrors.email}>
                  <input name="email" type="email" value={formData.email} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.email)}
                         placeholder="jane.smith@hospital.com" />
                </Field>
              </div>

              {/* Row 2: Phone + Experience */}
              <div style={styles.row}>
                <Field label="Phone" required error={fieldErrors.phone}>
                  <input name="phone" value={formData.phone} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.phone)}
                         placeholder="1234567890" />
                </Field>
                <Field label="Experience (years)" error={fieldErrors.experienceYears}>
                  <input name="experienceYears" type="number" min="0" max="60"
                         value={formData.experienceYears} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.experienceYears)}
                         placeholder="10" />
                </Field>
              </div>

              {/* Row 3: Specialization + Department */}
              <div style={styles.row}>
                <Field label="Specialization" required error={fieldErrors.specialization}>
                  <select name="specialization" value={formData.specialization}
                          onChange={handleChange} disabled={loading}
                          style={fieldStyle(fieldErrors.specialization)}>
                    <option value="">Select specialization</option>
                    {SPECIALIZATIONS.map(s => <option key={s} value={s}>{s}</option>)}
                  </select>
                </Field>
                <Field label="Department" required error={fieldErrors.department}>
                  <select name="department" value={formData.department}
                          onChange={handleChange} disabled={loading}
                          style={fieldStyle(fieldErrors.department)}>
                    <option value="">Select department</option>
                    {DEPARTMENTS.map(d => <option key={d} value={d}>{d}</option>)}
                  </select>
                </Field>
              </div>

              {/* Row 4: Qualification + Status */}
              <div style={styles.row}>
                <Field label="Qualification" required error={fieldErrors.qualification}>
                  <input name="qualification" value={formData.qualification} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.qualification)}
                         placeholder="MD, MBBS" />
                </Field>
                <Field label="Availability Status" error={fieldErrors.availabilityStatus}>
                  <select name="availabilityStatus" value={formData.availabilityStatus}
                          onChange={handleChange} disabled={loading} style={fieldStyle()}>
                    <option value="AVAILABLE">Available</option>
                    <option value="UNAVAILABLE">Unavailable</option>
                    <option value="ON_LEAVE">On Leave</option>
                  </select>
                </Field>
              </div>

              <div style={styles.formActions}>
                <button type="button" onClick={() => navigate(isEdit ? `/doctors/${id}` : '/doctors')}
                        style={styles.cancelBtn} disabled={loading}>
                  Cancel
                </button>
                <button type="submit" style={loading ? { ...styles.submitBtn, ...styles.disabledBtn } : styles.submitBtn}
                        disabled={loading}>
                  {loading ? (isEdit ? 'Saving...' : 'Registering...') : (isEdit ? 'Save Changes' : 'Register Doctor')}
                </button>
              </div>
            </form>
          </div>
        </main>
      </div>
    </div>
  );
}

/** Reusable labelled field wrapper */
function Field({ label, required, error, children }) {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '4px', minWidth: '200px' }}>
      <label style={styles.label}>
        {label}{required && <span style={{ color: '#e53e3e' }}> *</span>}
      </label>
      {children}
      {error && <span role="alert" style={styles.fieldError}>{error}</span>}
    </div>
  );
}

function fieldStyle(error) {
  return {
    padding: '9px 12px',
    borderRadius: '4px',
    border: `1px solid ${error ? '#fc8181' : '#cbd5e0'}`,
    fontSize: '14px',
    outline: 'none',
    width: '100%',
    boxSizing: 'border-box',
    background: '#fff',
    color: '#2d3748',
  };
}

const styles = {
  layout:    { display: 'flex', minHeight: '100vh', fontFamily: 'system-ui, sans-serif' },
  main:      { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' },
  content:   { flex: 1, padding: '24px', background: '#f7fafc', overflowY: 'auto' },

  breadcrumb:{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px', fontSize: '14px', color: '#718096' },
  backLink:  { background: 'none', border: 'none', color: '#3182ce', cursor: 'pointer', fontSize: '14px', padding: 0 },
  sep:       { color: '#cbd5e0' },

  card:      { background: '#fff', borderRadius: '8px', padding: '32px', boxShadow: '0 1px 3px rgba(0,0,0,0.08)', maxWidth: '820px' },
  cardTitle: { margin: '0 0 24px', fontSize: '20px', fontWeight: 700, color: '#1a202c' },

  errorBanner: { background: '#fff5f5', border: '1px solid #fc8181', borderRadius: '4px', color: '#c53030', padding: '10px 14px', marginBottom: '20px', fontSize: '14px' },

  form:      { display: 'flex', flexDirection: 'column', gap: '20px' },
  row:       { display: 'flex', gap: '20px', flexWrap: 'wrap' },
  label:     { fontSize: '13px', fontWeight: 600, color: '#4a5568' },
  fieldError:{ fontSize: '12px', color: '#c53030' },

  formActions: { display: 'flex', gap: '12px', justifyContent: 'flex-end', paddingTop: '8px' },
  cancelBtn:   { padding: '10px 20px', borderRadius: '4px', border: '1px solid #cbd5e0', background: '#fff', color: '#4a5568', fontSize: '14px', cursor: 'pointer' },
  submitBtn:   { padding: '10px 24px', borderRadius: '4px', border: 'none', background: '#3182ce', color: '#fff', fontSize: '14px', fontWeight: 600, cursor: 'pointer' },
  disabledBtn: { background: '#a0aec0', cursor: 'not-allowed' },
};

export default DoctorForm;
