import axios from "axios";
import { tokenStorage, userStorage } from "../utils/storage";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

export const api = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// Plain axios instance (no interceptors) used only for the refresh-token call itself,
// to avoid infinite refresh loops.
const rawApi = axios.create({ baseURL: BASE_URL });

api.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let pendingQueue = [];

function resolvePendingQueue(error, token = null) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else resolve(token);
  });
  pendingQueue = [];
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;
    const isAuthEndpoint = originalRequest?.url?.includes("/auth/login") ||
      originalRequest?.url?.includes("/auth/register") ||
      originalRequest?.url?.includes("/auth/refresh-token");

    if (status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      const refreshToken = tokenStorage.getRefreshToken();

      if (!refreshToken) {
        tokenStorage.clearTokens();
        userStorage.clearUser();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const { data } = await rawApi.post("/auth/refresh-token", { refreshToken });
        tokenStorage.setTokens(data.accessToken, data.refreshToken);
        resolvePendingQueue(null, data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        resolvePendingQueue(refreshError, null);
        tokenStorage.clearTokens();
        userStorage.clearUser();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

/**
 * Extracts a human-readable message from an Axios/Spring error response.
 * The backend's GlobalExceptionHandler and field-validation errors can take
 * a few different shapes, so we check them in order of specificity.
 */
export function getErrorMessage(error, fallback = "Something went wrong. Please try again.") {
  const data = error?.response?.data;
  if (!data) return error?.message || fallback;

  // 1. Check for standard structured backend exception fields first (e.g., HTTP 413, BusinessException)
  if (data.message && typeof data.message === "string") return data.message;
  if (data.error && typeof data.error === "string") return data.error;

  // 2. Handle specific policy sum insured coverage exhaustions
  if (data.remainingCoverage !== undefined) {
    if (data.remainingCoverage <= 0) {
      return "This policy has exhausted its Sum Insured.\nNo further claims can be processed.";
    }
    
    const fmt = (val) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(val);
    return `${data.message}\n\nCoverage Amount: ${fmt(data.coverageAmount)}\nAlready Approved: ${fmt(data.approvedClaimAmount)}\nRemaining Coverage: ${fmt(data.remainingCoverage)}`;
  }
  
  // 3. Handle loose strings returned by endpoints
  if (typeof data === "string") return data;

  // 4. Handle Spring validation MethodArgumentNotValidException map tracking nested attributes (e.g., nominee fields, city, state)
  if (data.errors && typeof data.errors === "object") {
    const firstKey = Object.keys(data.errors)[0];
    return data.errors[firstKey] || fallback;
  }

  return fallback;
}

export default api;
