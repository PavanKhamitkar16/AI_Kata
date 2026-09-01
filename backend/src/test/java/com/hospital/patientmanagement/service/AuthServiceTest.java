package com.hospital.patientmanagement.service;

import com.hospital.patientmanagement.dto.LoginRequest;
import com.hospital.patientmanagement.dto.LoginResponse;
import com.hospital.patientmanagement.entity.User;
import com.hospital.patientmanagement.enums.Role;
import com.hospital.patientmanagement.security.CustomUserDetails;
import com.hospital.patientmanagement.security.JwtTokenProvider;
import com.hospital.patientmanagement.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 *
 * <p>Uses plain Mockito — no Spring context is loaded, so tests run in
 * milliseconds. All collaborators (AuthenticationManager, JwtTokenProvider)
 * are mocked to isolate the service logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider      jwtTokenProvider;
    @Mock private Authentication        authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User        sampleUser;
    private CustomUserDetails samplePrincipal;

    @BeforeEach
    void setUp() {
        // Inject @Value field manually (Spring not running)
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 86400000L);

        sampleUser = User.builder()
                .id(1L)
                .username("dr.house")
                .email("house@hospital.com")
                .password("$2a$12$encodedPassword")
                .role(Role.DOCTOR)
                .firstName("Gregory")
                .lastName("House")
                .build();

        samplePrincipal = new CustomUserDetails(sampleUser);
    }

    // ------------------------------------------------------------------ //
    //  login()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("login — valid credentials — returns LoginResponse with token")
    void login_validCredentials_returnsLoginResponse() {
        // Arrange
        LoginRequest request = new LoginRequest("dr.house", "correct-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(samplePrincipal);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("mocked-jwt-token");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUsername()).isEqualTo("dr.house");
        assertThat(response.getEmail()).isEqualTo("house@hospital.com");
        assertThat(response.getRole()).isEqualTo("DOCTOR");
        assertThat(response.getExpiresIn()).isEqualTo(86400000L);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    @DisplayName("login — invalid password — throws BadCredentialsException")
    void login_invalidPassword_throwsBadCredentialsException() {
        // Arrange
        LoginRequest request = new LoginRequest("dr.house", "wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("login — unknown username — throws BadCredentialsException")
    void login_unknownUsername_throwsBadCredentialsException() {
        // Arrange
        LoginRequest request = new LoginRequest("nobody", "any-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("User not found"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login — authentication manager is called with correct credentials")
    void login_correctCredentials_authManagerCalledWithUsernameAndPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("dr.house", "correct-password");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(samplePrincipal);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("token");

        // Act
        authService.login(request);

        // Assert — verify exact token passed to AuthenticationManager
        verify(authenticationManager).authenticate(
                argThat(token ->
                    token instanceof UsernamePasswordAuthenticationToken upt
                    && "dr.house".equals(upt.getName())
                    && "correct-password".equals(upt.getCredentials().toString())
                )
        );
    }

    // ------------------------------------------------------------------ //
    //  logout()
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("logout — valid Bearer header — token is blacklisted")
    void logout_validBearerHeader_blacklistsToken() {
        // Act
        authService.logout("Bearer some-valid-jwt-token");

        // Assert
        verify(jwtTokenProvider).blacklistToken("some-valid-jwt-token");
    }

    @Test
    @DisplayName("logout — null header — no exception, no blacklist call")
    void logout_nullHeader_doesNothing() {
        // Act & Assert — must not throw
        assertThatCode(() -> authService.logout(null))
                .doesNotThrowAnyException();

        verify(jwtTokenProvider, never()).blacklistToken(any());
    }

    @Test
    @DisplayName("logout — malformed header (no Bearer prefix) — token not blacklisted")
    void logout_missingBearerPrefix_tokenNotBlacklisted() {
        // Act
        authService.logout("just-a-raw-token");

        // Assert
        verify(jwtTokenProvider, never()).blacklistToken(any());
    }
}
