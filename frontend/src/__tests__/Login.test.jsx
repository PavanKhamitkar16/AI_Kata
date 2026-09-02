import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Login from '../pages/Login';
import { AuthContext } from '../contexts/AuthContext';

// ------------------------------------------------------------------ //
//  Helpers
// ------------------------------------------------------------------ //

/**
 * Renders <Login> inside the minimal context providers it needs.
 * Accepts overrides for the auth context to simulate different states.
 */
function renderLogin(authOverrides = {}) {
  const defaultAuth = {
    isAuthenticated: false,
    loading: false,
    error: null,
    login: vi.fn(),
    clearError: vi.fn(),
  };

  const authValue = { ...defaultAuth, ...authOverrides };

  return render(
    <MemoryRouter initialEntries={['/login']}>
      <AuthContext.Provider value={authValue}>
        <Login />
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

// ------------------------------------------------------------------ //
//  Tests
// ------------------------------------------------------------------ //

describe('Login page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // --- Rendering ---

  it('renders username and password fields and a sign-in button', () => {
    renderLogin();
    expect(screen.getByTestId('username-input')).toBeInTheDocument();
    expect(screen.getByTestId('password-input')).toBeInTheDocument();
    expect(screen.getByTestId('login-button')).toHaveTextContent('Sign in');
  });

  it('renders the Hospital PMS heading', () => {
    renderLogin();
    expect(screen.getByText('Hospital PMS')).toBeInTheDocument();
  });

  // --- Client-side validation ---

  it('shows field-level error when submitting empty form', async () => {
    const user = userEvent.setup();
    renderLogin();

    await user.click(screen.getByTestId('login-button'));

    expect(await screen.findByText('Username is required.')).toBeInTheDocument();
    expect(await screen.findByText('Password is required.')).toBeInTheDocument();
  });

  it('shows field error for missing password only', async () => {
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByTestId('username-input'), 'dr.house');
    await user.click(screen.getByTestId('login-button'));

    expect(screen.queryByText('Username is required.')).not.toBeInTheDocument();
    expect(await screen.findByText('Password is required.')).toBeInTheDocument();
  });

  // --- Auth context interaction ---

  it('calls login with correct credentials on form submit', async () => {
    const user = userEvent.setup();
    const loginMock = vi.fn().mockResolvedValue({});
    renderLogin({ login: loginMock });

    await user.type(screen.getByTestId('username-input'), 'dr.house');
    await user.type(screen.getByTestId('password-input'), 'secret123');
    await user.click(screen.getByTestId('login-button'));

    await waitFor(() => {
      expect(loginMock).toHaveBeenCalledOnce();
      expect(loginMock).toHaveBeenCalledWith('dr.house', 'secret123');
    });
  });

  it('does NOT call login when client validation fails', async () => {
    const user = userEvent.setup();
    const loginMock = vi.fn();
    renderLogin({ login: loginMock });

    await user.click(screen.getByTestId('login-button'));

    expect(loginMock).not.toHaveBeenCalled();
  });

  // --- Error display ---

  it('displays server error from auth context', () => {
    renderLogin({ error: 'Invalid username or password.' });
    expect(screen.getByTestId('login-error')).toHaveTextContent(
      'Invalid username or password.'
    );
  });

  it('does not show error banner when error is null', () => {
    renderLogin({ error: null });
    expect(screen.queryByTestId('login-error')).not.toBeInTheDocument();
  });

  // --- Loading state ---

  it('disables the button and shows "Signing in..." during loading', () => {
    renderLogin({ loading: true });
    const btn = screen.getByTestId('login-button');
    expect(btn).toBeDisabled();
    expect(btn).toHaveTextContent('Signing in...');
  });

  // --- clearError side-effect ---

  it('calls clearError when user types in the username field', async () => {
    const user = userEvent.setup();
    const clearError = vi.fn();
    renderLogin({ clearError });

    await user.type(screen.getByTestId('username-input'), 'a');

    expect(clearError).toHaveBeenCalled();
  });
});
