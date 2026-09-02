package com.hospital.patientmanagement.service;

import com.hospital.patientmanagement.dto.DoctorRequest;
import com.hospital.patientmanagement.dto.DoctorResponse;
import com.hospital.patientmanagement.entity.Doctor;
import com.hospital.patientmanagement.enums.AvailabilityStatus;
import com.hospital.patientmanagement.exception.ResourceNotFoundException;
import com.hospital.patientmanagement.repository.DoctorRepository;
import com.hospital.patientmanagement.service.impl.DoctorServiceImpl;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DoctorServiceImpl}.
 *
 * <p>Plain Mockito — no Spring context. All tests run in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorService Unit Tests")
class DoctorServiceTest {

    @Mock private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private UUID       sampleId;
    private Doctor     sampleDoctor;
    private DoctorRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();

        sampleDoctor = Doctor.builder()
                .doctorId(sampleId)
                .name("Dr. Jane Smith")
                .email("jane.smith@hospital.com")
                .phone("1234567890")
                .specialization("Cardiology")
                .qualification("MD, MBBS")
                .experienceYears(10)
                .department("Cardiac Care")
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .isActive(true)
                .build();

        sampleRequest = DoctorRequest.builder()
                .name("Dr. Jane Smith")
                .email("jane.smith@hospital.com")
                .phone("1234567890")
                .specialization("Cardiology")
                .qualification("MD, MBBS")
                .experienceYears(10)
                .department("Cardiac Care")
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .build();
    }

    // ------------------------------------------------------------------ //
    //  createDoctor()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("createDoctor — valid request — returns DoctorResponse with all fields")
    void createDoctor_validRequest_returnsDoctorResponse() {
        // Arrange
        when(doctorRepository.existsByEmail(sampleRequest.getEmail())).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(sampleDoctor);

        // Act
        DoctorResponse response = doctorService.createDoctor(sampleRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDoctorId()).isEqualTo(sampleId);
        assertThat(response.getName()).isEqualTo("Dr. Jane Smith");
        assertThat(response.getEmail()).isEqualTo("jane.smith@hospital.com");
        assertThat(response.getSpecialization()).isEqualTo("Cardiology");
        assertThat(response.getDepartment()).isEqualTo("Cardiac Care");
        assertThat(response.getAvailabilityStatus()).isEqualTo(AvailabilityStatus.AVAILABLE);
        assertThat(response.getIsActive()).isTrue();

        verify(doctorRepository).existsByEmail("jane.smith@hospital.com");
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    @DisplayName("createDoctor — null availabilityStatus — defaults to AVAILABLE")
    void createDoctor_nullAvailabilityStatus_defaultsToAvailable() {
        // Arrange
        sampleRequest.setAvailabilityStatus(null);
        when(doctorRepository.existsByEmail(anyString())).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(sampleDoctor);

        // Act
        doctorService.createDoctor(sampleRequest);

        // Assert — the entity saved must have AVAILABLE status
        verify(doctorRepository).save(argThat(d ->
                d.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE));
    }

    @Test
    @DisplayName("createDoctor — duplicate email — throws IllegalArgumentException")
    void createDoctor_duplicateEmail_throwsIllegalArgumentException() {
        // Arrange
        when(doctorRepository.existsByEmail(sampleRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> doctorService.createDoctor(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jane.smith@hospital.com");

        verify(doctorRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    //  updateDoctor()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("updateDoctor — valid update — returns updated DoctorResponse")
    void updateDoctor_validRequest_returnsUpdatedResponse() {
        // Arrange
        Doctor updated = Doctor.builder()
                .doctorId(sampleId)
                .name("Dr. Jane Smith Updated")
                .email("jane.smith@hospital.com")
                .phone("0987654321")
                .specialization("Neurology")
                .qualification("MD, PhD")
                .experienceYears(12)
                .department("Neurology Unit")
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .isActive(true)
                .build();

        when(doctorRepository.findById(sampleId)).thenReturn(Optional.of(sampleDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(updated);

        DoctorRequest updateRequest = DoctorRequest.builder()
                .name("Dr. Jane Smith Updated")
                .email("jane.smith@hospital.com") // same email — no uniqueness check needed
                .phone("0987654321")
                .specialization("Neurology")
                .qualification("MD, PhD")
                .experienceYears(12)
                .department("Neurology Unit")
                .build();

        // Act
        DoctorResponse response = doctorService.updateDoctor(sampleId, updateRequest);

        // Assert
        assertThat(response.getName()).isEqualTo("Dr. Jane Smith Updated");
        assertThat(response.getSpecialization()).isEqualTo("Neurology");
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    @DisplayName("updateDoctor — changing to taken email — throws IllegalArgumentException")
    void updateDoctor_changesEmailToTaken_throwsIllegalArgumentException() {
        // Arrange
        when(doctorRepository.findById(sampleId)).thenReturn(Optional.of(sampleDoctor));
        when(doctorRepository.existsByEmail("other@hospital.com")).thenReturn(true);

        DoctorRequest req = DoctorRequest.builder()
                .name("Dr. Jane")
                .email("other@hospital.com")
                .phone("111")
                .specialization("X")
                .qualification("Y")
                .department("Z")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> doctorService.updateDoctor(sampleId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("other@hospital.com");
    }

    @Test
    @DisplayName("updateDoctor — doctor not found — throws ResourceNotFoundException")
    void updateDoctor_notFound_throwsResourceNotFoundException() {
        // Arrange
        when(doctorRepository.findById(sampleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> doctorService.updateDoctor(sampleId, sampleRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor");
    }

    // ------------------------------------------------------------------ //
    //  getDoctorById()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getDoctorById — existing id — returns DoctorResponse")
    void getDoctorById_existingId_returnsDoctorResponse() {
        // Arrange
        when(doctorRepository.findById(sampleId)).thenReturn(Optional.of(sampleDoctor));

        // Act
        DoctorResponse response = doctorService.getDoctorById(sampleId);

        // Assert
        assertThat(response.getDoctorId()).isEqualTo(sampleId);
    }

    @Test
    @DisplayName("getDoctorById — unknown id — throws ResourceNotFoundException")
    void getDoctorById_unknownId_throwsResourceNotFoundException() {
        // Arrange
        when(doctorRepository.findById(sampleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> doctorService.getDoctorById(sampleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor");
    }

    // ------------------------------------------------------------------ //
    //  getDoctors()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getDoctors — no filters — returns page of DoctorResponse")
    void getDoctors_noFilters_returnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Doctor> doctorPage = new PageImpl<>(List.of(sampleDoctor), pageable, 1);
        when(doctorRepository.search(null, null, null, null, pageable)).thenReturn(doctorPage);

        // Act
        Page<DoctorResponse> result = doctorService.getDoctors(null, null, null, null, pageable);

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("jane.smith@hospital.com");
    }

    // ------------------------------------------------------------------ //
    //  activateDoctor() / deactivateDoctor()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("activateDoctor — inactive doctor — sets isActive to true")
    void activateDoctor_inactiveDoctor_setsActiveTrue() {
        // Arrange
        sampleDoctor.setIsActive(false);
        when(doctorRepository.findById(sampleId)).thenReturn(Optional.of(sampleDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DoctorResponse response = doctorService.activateDoctor(sampleId);

        // Assert
        assertThat(response.getIsActive()).isTrue();
        verify(doctorRepository).save(argThat(Doctor::getIsActive));
    }

    @Test
    @DisplayName("deactivateDoctor — active doctor — sets isActive to false")
    void deactivateDoctor_activeDoctor_setsActiveFalse() {
        // Arrange
        when(doctorRepository.findById(sampleId)).thenReturn(Optional.of(sampleDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DoctorResponse response = doctorService.deactivateDoctor(sampleId);

        // Assert
        assertThat(response.getIsActive()).isFalse();
        verify(doctorRepository).save(argThat(d -> !d.getIsActive()));
    }

    @Test
    @DisplayName("activateDoctor — doctor not found — throws ResourceNotFoundException")
    void activateDoctor_notFound_throwsResourceNotFoundException() {
        // Arrange
        when(doctorRepository.findById(sampleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> doctorService.activateDoctor(sampleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
