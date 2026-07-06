import api from "./api";

export const userService = {
  getCustomers: () => api.get("/users/customers").then((r) => r.data),
  getInsuranceOperationsOfficers: () => api.get("/users/insurance-operations-officers").then((r) => r.data),
  getAll: (params) => api.get("/users", { params }).then((r) => r.data),
  getById: (id) => api.get(`/users/${id}`).then((r) => r.data),
  activate: (id) => api.patch(`/users/${id}/activate`).then((r) => r.data),
  deactivate: (id) => api.patch(`/users/${id}/deactivate`).then((r) => r.data),
};