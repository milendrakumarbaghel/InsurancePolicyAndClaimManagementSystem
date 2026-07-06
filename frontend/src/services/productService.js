import api from "./api";

export const productService = {
  create: (payload) => api.post("/products", payload).then((r) => r.data),
  update: (productId, payload) => api.put(`/products/${productId}`, payload).then((r) => r.data),
  getById: (productId) => api.get(`/products/${productId}`).then((r) => r.data),
  getAll: (params) => api.get("/products", { params }).then((r) => r.data),
  getActive: (params) => api.get("/products/active", { params }).then((r) => r.data),
  deactivate: (productId) => api.patch(`/products/${productId}/deactivate`).then((r) => r.data),
  activate: (productId) => api.patch(`/products/${productId}/activate`).then((r) => r.data),
};
