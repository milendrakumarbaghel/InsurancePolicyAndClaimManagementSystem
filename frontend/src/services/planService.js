import api from "./api";

export const planService = {
  create: (payload) => api.post("/plans", payload).then((r) => r.data),
  update: (planId, payload) => api.put(`/plans/${planId}`, payload).then((r) => r.data),
  getById: (planId) => api.get(`/plans/${planId}`).then((r) => r.data),
  getByProduct: (productId) => api.get(`/plans/product/${productId}`).then((r) => r.data),
  getAll: (params) => api.get("/plans", { params }).then((r) => r.data),
  deactivate: (planId) => api.patch(`/plans/${planId}/deactivate`).then((r) => r.data),
  activate: (planId) => api.patch(`/plans/${planId}/activate`).then((r) => r.data),
};
