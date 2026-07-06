import api from "./api";

export const customerService = {
  createProfile: (payload) => api.post("/customers", payload).then((r) => r.data),
  updateProfile: (customerId, payload) => api.put(`/customers/${customerId}`, payload).then((r) => r.data),
  getByUserId: (userId) => api.get(`/customers/user/${userId}`).then((r) => r.data),
  getMyProfile: () => api.get("/customers/me").then((r) => r.data),
  getById: (customerId) => api.get(`/customers/${customerId}`).then((r) => r.data),
  getAll: (params) => api.get("/customers", { params }).then((r) => r.data),
};
