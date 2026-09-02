package com.hospital.patientmanagement.service;

import com.hospital.patientmanagement.dto.PatientRequest;
import com.hospital.patientmanagement.dto.PatientResponse;
import com.hospital.patientmanagement.entity.Patient;
import com.hospital.patientmanagement.enums.BloodGroup;
import com.hospital.patientmanagement.enums.Gender;
import com.hospital.patientmanagement.exception.ResourceNotFoundException;
import com.hospital.patientmanagement.repository.PatientRepository;
import com.hospital.patientmanagement.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PatientServiceImpl}.
 *
 * <p>Plain Mockito — no Spring context. All tests run in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Unit Tests")
class PatientServiceTest {

    @Mock private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private UUID          sampleId;
    private Patient       samplePatient;
    private PatientRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();

        samplePatient = Patient.builder()
                .patientId(sampleId)
                .firstName("Alice")
                .lastName("Johnson")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .phone("9876543210")
                .email("alice.johnson@example.com")
                .address("123 Main St, Springfield")
                .emergencyContact("Bob Johnson: 1234567890")
                .bloodGroup(BloodGroup.O_POS)
                .medicalHistory("No known conditions")
                .allergies("Penicillin")
                .isActive(true)
                .build();

        sampleRequest = PatientRequest.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .phone("9876543210")
                .email("alice.johnson@example.com")
                .address("123 Main St, Springfield")
                .emergencyContact("Bob Johnson: 1234567890")
                .bloodGroup(BloodGroup.O_POS)
                .medicalHistory("No known conditions")
                .allergies("Penicillin")
                .build();
    }

    // ------------------------------------------------------------------ //
    //  createPatient()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createPatient — valid request — returns PatientResponse with all fields")
    void createPatient_validRequest_returnsPatientResponse() {
        // Arrange
        when(patientRepository.existsByEmail(sampleRequest.getEmail())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient);

        // Act
        PatientResponse response = patientService.createPatient(sampleRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getPatientId()).isEqualTo(sampleId);
        assertThat(response.getFirstName()).isEqualTo("Alice");
        assertThat(response.getLastName()).isEqualTo("Johnson");
        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getBloodGroup()).isEqualTo(BloodGroup.O_POS);
        assertThat(response.getIsActive()).isTrue();

        verify(patientRepository).existsByEmail("alice.johnson@example.com");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("createPatient — no email provided — skips uniqueness check")
    void createPatient_noEmail_skipsEmailCheck() {
        // Arrange
        sampleRequest.setEmail(null);
        Patient noEmailPatient = Patient.builder()
                .patientId(sampleId)
                .firstName("Alice")
                .lastName("Johnson")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .phone("9876543210")
                .isActive(true)
                .build();
        when(patientRepository.save(any(Patient.class))).thenReturn(noEmailPatient);

        // Act
        patientService.createPatient(sampleRequest);

        // Assert — existsByEmail should NOT be called when email is null
        verify(patientRepository, never()).existsByEmail(any());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("createPatient — duplicate email — throws IllegalArgumentException")
    void createPatient_duplicateEmail_throwsIllegalArgumentException() {
        // Arrange
        when(patientRepository.existsByEmail(sampleRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> patientService.createPatient(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alice.johnson@example.com");

        verify(patientRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    //  updatePatient()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("updatePatient — valid update — returns updated PatientResponse")
    void updatePatient_validRequest_returnsUpdatedResponse() {
        // Arrange
        Patient updated = Patient.builder()
                .patientId(sampleId)
                .firstName("Alice")
                .lastName("Williams")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .phone("1111111111")
                .email("alice.johnson@example.com") // same email
                .bloodGroup(BloodGroup.A_POS)
                .isActive(true)
                .build();

        when(patientRepository.findById(sampleId)).thenReturn(Optional.of(samplePatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updated);

        PatientRequest updateReq = PatientRequest.builder()
                .firstName("Alice")
                .lastName("Williams")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .phone("1111111111")
                .email("alice.johnson@example.com")
                .bloodGroup(BloodGroup.A_POS)
                .build();

        // Act
        PatientResponse response = patientService.updatePatient(sampleId, updateReq);

        // Assert
        assertThat(response.getLastName()).isEqualTo("Williams");
        assertThat(response.getBloodGroup()).isEqualTo(BloodGroup.A_POS);
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("updatePatient — changing to taken email — throws IllegalArgumentException")
    void updatePatient_changesEmailToTaken_throwsIllegalArgumentException() {
        // Arrange
        when(patientRepository.findById(sampleId)).thenReturn(Optional.of(samplePatient));
        when(patientRepository.existsByEmail("other@example.com")).thenReturn(true);

        PatientRequest req = PatientRequest.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .phone("9876543210")
                .email("other@example.com")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> patientService.updatePatient(sampleId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("other@example.com");
    }

    @Test
    @DisplayName("updatePatient — patient not found — throws ResourceNotFoundException")
    void updatePatient_notFound_throwsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(sampleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> patientService.updatePatient(sampleId, sampleRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient");
    }

    // ------------------------------------------------------------------ //
    //  getPatientById()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getPatientById — existing id — returns PatientResponse")
    void getPatientById_existingId_returnsPatientResponse() {
        // Arrange
        when(patientRepository.findById(sampleId)).thenReturn(Optional.of(samplePatient));

        // Act
        PatientResponse response = patientService.getPatientById(sampleId);

        // Assert
        assertThat(response.getPatientId()).isEqualTo(sampleId);
        assertThat(response.getAllergies()).isEqualTo("Penicillin");
    }

    @Test
    @DisplayName("getPatientById — unknown id — throws ResourceNotFoundException")
    void getPatientById_unknownId_throwsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(sampleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> patientService.getPatientById(sampleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient");
    }

    // ------------------------------------------------------------------ //
    //  getPatients()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getPatients — no filters — returns page of PatientResponse")
    void getPatients_noFilters_returnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Patient> patientPage = new PageImpl<>(List.of(samplePatient), pageable, 1);
        when(patientRepository.search(null, null, null, pageable)).thenReturn(patientPage);

        // Act
        Page<PatientResponse> result = patientService.getPatients(null, null, null, pageable);

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("alice.johnson@example.com");
    }

    // ------------------------------------------------------------------ //
    //  activatePatient() / deactivatePatient()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("activatePatient — inactive patient — sets isActive to true")
    void activatePatient_inactivePatient_setsActiveTrue() {
        // Arrange
        samplePatient.setIsActive(false);
        when(patientRepository.findById(sampleId)).thenReturn(Optional.of(samplePatient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PatientResponse response = patientService.activatePatient(sampleId);

        // Assert
        assertThat(response.getIsActive()).isTrue();
        verify(patientRepository).save(argThat(Patient::getIsActive));
    }

    @Test
    @DisplayName("deactivatePatient — active patient — sets isActive to false")
    void deactivatePatient_activePatient_setsActiveFalse() {
        // Arrange
        when(patientRepository.findById(sampleId)).thenReturn(Optional.of(samplePatient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PatientResponse response = patientService.deactivatePatient(sampleId);

        // Assert
        assertThat(response.getIsActive()).isFalse();
        verify(patientRepository).save(argThat(p -> !p.getIsActive()));
    }

    @Test
    @DisplayName("deactivatePatient — patient not found — throws ResourceNotFoundException")
    void deactivatePatient_notFound_throwsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(sampleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> patientService.deactivatePatient(sampleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
