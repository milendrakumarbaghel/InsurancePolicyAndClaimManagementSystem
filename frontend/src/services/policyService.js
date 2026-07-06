import api from "./api";

export const policyService = {
  purchase: (planId) => api.post(`/policies/purchase/${planId}`).then((r) => r.data),
  issue: (payload) => api.post("/policies/issue", payload).then((r) => r.data),
  getById: (policyId) => api.get(`/policies/${policyId}`).then((r) => r.data),
  getByNumber: (policyNumber) => api.get(`/policies/number/${policyNumber}`).then((r) => r.data),
  getMy: () => api.get("/policies/my").then((r) => r.data),
  getAll: (params) => api.get("/policies", { params }).then((r) => r.data),
  cancel: (policyId) => api.patch(`/policies/${policyId}/cancel`).then((r) => r.data),
};
