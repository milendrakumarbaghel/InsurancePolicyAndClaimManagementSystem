import api from "./api";

export const claimDocumentService = {
  upload: (claimId, file) => {
    const formData = new FormData();
    formData.append("file", file);
    return api
      .post(`/claim-documents/upload/${claimId}`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      .then((r) => r.data);
  },
  getByClaim: (claimId) => api.get(`/claim-documents/claim/${claimId}`).then((r) => r.data),
  getById: (documentId) => api.get(`/claim-documents/${documentId}`).then((r) => r.data),
  remove: (documentId) => api.delete(`/claim-documents/${documentId}`).then((r) => r.data),
};
