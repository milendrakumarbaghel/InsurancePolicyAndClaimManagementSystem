import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { authService } from "../services/authService";
import { tokenStorage, userStorage } from "../utils/storage";
import { getErrorMessage } from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => userStorage.getUser());
  const [isLoading, setIsLoading] = useState(false);
  const [isBootstrapping, setIsBootstrapping] = useState(true);

  useEffect(() => {
    // Session is restored purely from localStorage — the backend is stateless (JWT),
    // so there's no "whoami" endpoint to re-validate against on refresh.
    const storedUser = userStorage.getUser();
    const hasToken = tokenStorage.getAccessToken();
    if (storedUser && hasToken) {
      setUser(storedUser);
    } else {
      setUser(null);
    }
    setIsBootstrapping(false);
  }, []);

  const login = useCallback(async (credentials) => {
    setIsLoading(true);
    try {
      const data = await authService.login(credentials);
      tokenStorage.setTokens(data.accessToken, data.refreshToken);
      const nextUser = {
        email: data.email,
        firstName: data.firstName,
        middleName: data.middleName,
        lastName: data.lastName,
        role: data.role,
      };
      userStorage.setUser(nextUser);
      setUser(nextUser);
      return { success: true, user: nextUser };
    } catch (error) {
      return { success: false, message: getErrorMessage(error, "Invalid email or password.") };
    } finally {
      setIsLoading(false);
    }
  }, []);

  const register = useCallback(async (payload) => {
    setIsLoading(true);
    try {
      const data = await authService.register(payload);
      return { success: true, data };
    } catch (error) {
      return { success: false, message: getErrorMessage(error, "Registration failed.") };
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = tokenStorage.getRefreshToken();
    try {
      await authService.logout(refreshToken);
    } catch {
      // Ignore network errors on logout — we clear local state regardless.
    }
    tokenStorage.clearTokens();
    userStorage.clearUser();
    setUser(null);
  }, []);

  const value = {
    user,
    role: user?.role || null,
    isAuthenticated: !!user,
    isLoading,
    isBootstrapping,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
