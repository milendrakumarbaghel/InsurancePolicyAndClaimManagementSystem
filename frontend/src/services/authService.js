import api from "./api";

export const authService = {
  register: (payload) => api.post("/auth/register", payload).then((r) => r.data),

  login: (payload) => api.post("/auth/login", payload).then((r) => r.data),

  refreshToken: (refreshToken) =>
    api.post("/auth/refresh-token", { refreshToken }).then((r) => r.data),

  logout: (refreshToken) => api.post("/auth/logout", { refreshToken }),

  forgotPassword: (email) => api.post("/auth/forgot-password", { email }).then((r) => r.data),

  resetPassword: (payload) => api.post("/auth/reset-password", payload).then((r) => r.data),

  createAgent: (payload) => api.post("/auth/agents", payload).then((r) => r.data),

  verifyOtp: (payload) => api.post("/otp/verify", payload).then((r) => r.data),

  resendOtp: (email) => api.post("/otp/resend", { email }).then((r) => r.data),
};
