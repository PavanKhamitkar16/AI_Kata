import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Header from '../../components/Header';
import Sidebar from '../../components/Sidebar';
import LoadingSpinner from '../../components/LoadingSpinner';
import { useToast } from '../../components/Toast';
import { createPatient, getPatientById, updatePatient } from '../../api/patientApi';

const EMPTY_FORM = {
  firstName: '',
  lastName: '',
  dateOfBirth: '',
  gender: '',
  phone: '',
  email: '',
  address: '',
  emergencyContact: '',
  bloodGroup: '',
  medicalHistory: '',
  allergies: '',
};

const BLOOD_GROUPS = ['A_POS', 'A_NEG', 'B_POS', 'B_NEG', 'AB_POS', 'AB_NEG', 'O_POS', 'O_NEG'];
const BG_LABELS    = { A_POS: 'A+', A_NEG: 'A−', B_POS: 'B+', B_NEG: 'B−', AB_POS: 'AB+', AB_NEG: 'AB−', O_POS: 'O+', O_NEG: 'O−' };
const PHONE_RE     = /^\+?[0-9]{7,15}$/;
const EMAIL_RE     = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Patient register / edit form.
 * Register mode: /patients/new
 * Edit mode:     /patients/:id/edit
 */
function PatientForm() {
  const { id }        = useParams();
  const isEdit        = Boolean(id);
  const navigate      = useNavigate();
  const { showToast } = useToast();

  const [formData,    setFormData]    = useState(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading,     setLoading]     = useState(false);
  const [fetching,    setFetching]    = useState(isEdit);
  const [serverError, setServerError] = useState(null);

  useEffect(() => {
    if (!isEdit) return;
    setFetching(true);
    getPatientById(id)
      .then(data => {
        setFormData({
          firstName:        data.firstName        ?? '',
          lastName:         data.lastName         ?? '',
          dateOfBirth:      data.dateOfBirth      ?? '',
          gender:           data.gender           ?? '',
          phone:            data.phone            ?? '',
          email:            data.email            ?? '',
          address:          data.address          ?? '',
          emergencyContact: data.emergencyContact ?? '',
          bloodGroup:       data.bloodGroup       ?? '',
          medicalHistory:   data.medicalHistory   ?? '',
          allergies:        data.allergies        ?? '',
        });
      })
      .catch(() => setServerError('Failed to load patient data.'))
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
    if (!formData.firstName.trim())   errors.firstName   = 'First name is required.';
    if (!formData.lastName.trim())    errors.lastName    = 'Last name is required.';
    if (!formData.dateOfBirth)        errors.dateOfBirth = 'Date of birth is required.';
    else if (new Date(formData.dateOfBirth) >= new Date()) errors.dateOfBirth = 'Date of birth must be in the past.';
    if (!formData.gender)             errors.gender      = 'Gender is required.';
    if (!formData.phone.trim())       errors.phone       = 'Phone is required.';
    else if (!PHONE_RE.test(formData.phone)) errors.phone = 'Phone must be 7-15 digits.';
    if (formData.email && !EMAIL_RE.test(formData.email)) errors.email = 'Enter a valid email address.';
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
      dateOfBirth:  formData.dateOfBirth  || null,
      gender:       formData.gender       || null,
      email:        formData.email        || null,
      bloodGroup:   formData.bloodGroup   || null,
      medicalHistory: formData.medicalHistory || null,
      allergies:    formData.allergies    || null,
      address:      formData.address      || null,
      emergencyContact: formData.emergencyContact || null,
    };

    setLoading(true);
    setServerError(null);
    try {
      if (isEdit) {
        await updatePatient(id, payload);
        showToast('Patient updated successfully.', 'success');
        navigate(`/patients/${id}`);
      } else {
        const created = await createPatient(payload);
        showToast('Patient registered successfully.', 'success');
        navigate(`/patients/${created.patientId}`);
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
            <button onClick={() => navigate('/patients')} style={styles.backLink}>Patients</button>
            <span style={styles.sep}>/</span>
            <span>{isEdit ? 'Edit Patient' : 'New Patient'}</span>
          </div>

          <div style={styles.card}>
            <h2 style={styles.cardTitle}>{isEdit ? 'Edit Patient Profile' : 'Register New Patient'}</h2>

            {serverError && (
              <div role="alert" style={styles.errorBanner}>{serverError}</div>
            )}

            <form onSubmit={handleSubmit} noValidate style={styles.form}>
              {/* Section: Personal Information */}
              <SectionTitle>Personal Information</SectionTitle>

              <div style={styles.row}>
                <Field label="First Name" required error={fieldErrors.firstName}>
                  <input name="firstName" value={formData.firstName} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.firstName)}
                         placeholder="Alice" />
                </Field>
                <Field label="Last Name" required error={fieldErrors.lastName}>
                  <input name="lastName" value={formData.lastName} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.lastName)}
                         placeholder="Johnson" />
                </Field>
              </div>

              <div style={styles.row}>
                <Field label="Date of Birth" required error={fieldErrors.dateOfBirth}>
                  <input name="dateOfBirth" type="date" value={formData.dateOfBirth} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.dateOfBirth)}
                         max={new Date().toISOString().split('T')[0]} />
                </Field>
                <Field label="Gender" required error={fieldErrors.gender}>
                  <select name="gender" value={formData.gender} onChange={handleChange}
                          disabled={loading} style={fieldStyle(fieldErrors.gender)}>
                    <option value="">Select gender</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </Field>
              </div>

              {/* Section: Contact */}
              <SectionTitle>Contact</SectionTitle>

              <div style={styles.row}>
                <Field label="Phone" required error={fieldErrors.phone}>
                  <input name="phone" value={formData.phone} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.phone)}
                         placeholder="9876543210" />
                </Field>
                <Field label="Email" error={fieldErrors.email}>
                  <input name="email" type="email" value={formData.email} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.email)}
                         placeholder="alice@example.com (optional)" />
                </Field>
              </div>

              <div style={styles.row}>
                <Field label="Address" error={fieldErrors.address}>
                  <textarea name="address" value={formData.address} onChange={handleChange}
                            disabled={loading}
                            style={{ ...fieldStyle(fieldErrors.address), resize: 'vertical', minHeight: '72px' }}
                            placeholder="123 Main St, Springfield" />
                </Field>
                <Field label="Emergency Contact" error={fieldErrors.emergencyContact}>
                  <input name="emergencyContact" value={formData.emergencyContact} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.emergencyContact)}
                         placeholder="Bob Johnson: 1234567890" />
                </Field>
              </div>

              {/* Section: Medical */}
              <SectionTitle>Medical</SectionTitle>

              <div style={styles.row}>
                <Field label="Blood Group" error={fieldErrors.bloodGroup}>
                  <select name="bloodGroup" value={formData.bloodGroup} onChange={handleChange}
                          disabled={loading} style={fieldStyle()}>
                    <option value="">Unknown</option>
                    {BLOOD_GROUPS.map(bg => (
                      <option key={bg} value={bg}>{BG_LABELS[bg]}</option>
                    ))}
                  </select>
                </Field>
                <Field label="Allergies" error={fieldErrors.allergies}>
                  <input name="allergies" value={formData.allergies} onChange={handleChange}
                         disabled={loading} style={fieldStyle(fieldErrors.allergies)}
                         placeholder="Penicillin, Pollen..." />
                </Field>
              </div>

              <div style={styles.row}>
                <Field label="Medical History" error={fieldErrors.medicalHistory}>
                  <textarea name="medicalHistory" value={formData.medicalHistory} onChange={handleChange}
                            disabled={loading}
                            style={{ ...fieldStyle(fieldErrors.medicalHistory), resize: 'vertical', minHeight: '88px', flex: 1 }}
                            placeholder="Known conditions, past procedures..." />
                </Field>
              </div>

              <div style={styles.formActions}>
                <button type="button" onClick={() => navigate(isEdit ? `/patients/${id}` : '/patients')}
                        style={styles.cancelBtn} disabled={loading}>
                  Cancel
                </button>
                <button type="submit" style={loading ? { ...styles.submitBtn, ...styles.disabledBtn } : styles.submitBtn}
                        disabled={loading}>
                  {loading ? (isEdit ? 'Saving...' : 'Registering...') : (isEdit ? 'Save Changes' : 'Register Patient')}
                </button>
              </div>
            </form>
          </div>
        </main>
      </div>
    </div>
  );
}

function SectionTitle({ children }) {
  return (
    <div style={{ borderBottom: '1px solid #e2e8f0', paddingBottom: '8px', marginBottom: '4px' }}>
      <h3 style={{ margin: 0, fontSize: '13px', fontWeight: 700, color: '#4a5568', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
        {children}
      </h3>
    </div>
  );
}

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

  form:      { display: 'flex', flexDirection: 'column', gap: '16px' },
  row:       { display: 'flex', gap: '20px', flexWrap: 'wrap' },
  label:     { fontSize: '13px', fontWeight: 600, color: '#4a5568' },
  fieldError:{ fontSize: '12px', color: '#c53030' },

  formActions: { display: 'flex', gap: '12px', justifyContent: 'flex-end', paddingTop: '8px' },
  cancelBtn:   { padding: '10px 20px', borderRadius: '4px', border: '1px solid #cbd5e0', background: '#fff', color: '#4a5568', fontSize: '14px', cursor: 'pointer' },
  submitBtn:   { padding: '10px 24px', borderRadius: '4px', border: 'none', background: '#3182ce', color: '#fff', fontSize: '14px', fontWeight: 600, cursor: 'pointer' },
  disabledBtn: { background: '#a0aec0', cursor: 'not-allowed' },
};

export default PatientForm;
