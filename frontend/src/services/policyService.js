import api from "./api";

export const policyService = {
  /**
   * Customer purchases a plan with their chosen coverage and duration.
   * The backend calculates and stores the premium at this point.
   */
  purchase: (planId, { coverage, duration, premiumType }) =>
    api
      .post(`/policies/purchase/${planId}`, null, {
        params: { coverage, duration, premiumType },
      })
      .then((r) => r.data),

  issue: (payload) => api.post("/policies/issue", payload).then((r) => r.data),
  getById: (policyId) => api.get(`/policies/${policyId}`).then((r) => r.data),
  getByNumber: (policyNumber) =>
    api.get(`/policies/number/${policyNumber}`).then((r) => r.data),
  getMy: () => api.get("/policies/my").then((r) => r.data),
  getAll: (params) => api.get("/policies", { params }).then((r) => r.data),
  cancel: (policyId) =>
    api.patch(`/policies/${policyId}/cancel`).then((r) => r.data),
};
