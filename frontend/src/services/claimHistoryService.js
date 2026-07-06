import api from "./api";

export const claimHistoryService = {
  getHistory: (claimId, params) => api.get(`/claim-history/${claimId}`, { params }).then((r) => r.data),
};
