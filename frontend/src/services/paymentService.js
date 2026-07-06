import api from "./api";

export const paymentService = {
  record: (payload) => api.post("/payments", payload).then((r) => r.data),
  getById: (paymentId) => api.get(`/payments/${paymentId}`).then((r) => r.data),
  getByPolicy: (policyId) => api.get(`/payments/policy/${policyId}`).then((r) => r.data),
  getAll: (params) => api.get("/payments", { params }).then((r) => r.data),
};
