import api from "./api";

export const claimService = {
  raise: (payload) => api.post("/claims", payload).then((r) => r.data),
  review: (claimId, payload) => api.put(`/claims/${claimId}/review`, payload).then((r) => r.data),
  assign: (claimId, agentId) => api.put(`/claims/${claimId}/assign`, { agentId }).then((r) => r.data),
  approve: (claimId, remarks) => api.put(`/claims/${claimId}/approve`, { remarks }).then((r) => r.data),
  reject: (claimId, remarks) => api.put(`/claims/${claimId}/reject`, { remarks }).then((r) => r.data),
  getById: (claimId) => api.get(`/claims/${claimId}`).then((r) => r.data),
  getByNumber: (claimNumber) => api.get(`/claims/number/${claimNumber}`).then((r) => r.data),
  getMy: () => api.get("/claims/my").then((r) => r.data),
  getAll: (params) => api.get("/claims", { params }).then((r) => r.data),
  getAssigned: (params) => api.get("/claims/assigned", { params }).then((r) => r.data),
};
