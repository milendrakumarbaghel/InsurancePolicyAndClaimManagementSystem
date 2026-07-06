import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import {
  ArrowLeft, FileText, UploadCloud, CheckCircle2, XCircle, UserPlus, Trash2, History,
} from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import Card from "../components/common/Card";
import Stamp from "../components/common/Stamp";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import Alert from "../components/common/Alert";
import Modal from "../components/common/Modal";
import Select from "../components/common/Select";
import Textarea from "../components/common/Textarea";
import FileUpload from "../components/common/FileUpload";

import { useAuth } from "../context/AuthContext";
import { claimService } from "../services/claimService";
import { claimDocumentService } from "../services/claimDocumentService";
import { claimHistoryService } from "../services/claimHistoryService";
import { userService } from "../services/userService";
import { getErrorMessage } from "../services/api";
import { ROLES } from "../utils/constants";
import { formatCurrency, formatDate, formatDateTime, toTitleCase } from "../utils/formatters";

function ReviewForm({ claim, onDone }) {
  const [recommended, setRecommended] = useState(null);
  const [remarks, setRemarks] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const submit = async () => {
    setError("");
    if (recommended === null) return setError("Choose whether you recommend approval or rejection.");
    if (remarks.trim().length < 5) return setError("Remarks must be at least 5 characters.");

    setIsSubmitting(true);
    try {
      await claimService.review(claim.claimId, { recommended, remarks });
      toast.success("Review submitted.");
      onDone();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card>
      <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white mb-4">Submit your review</h3>
      {error && <Alert type="error" className="mb-4">{error}</Alert>}

      <div className="flex gap-3 mb-4">
        <button
          onClick={() => setRecommended(true)}
          className={`flex-1 rounded-lg border-2 px-4 py-3 text-sm font-semibold transition-colors ${
            recommended === true ? "border-success bg-success/10 text-success" : "border-ink-200 dark:border-ink-700 text-ink-500"
          }`}
        >
          Recommend Approval
        </button>
        <button
          onClick={() => setRecommended(false)}
          className={`flex-1 rounded-lg border-2 px-4 py-3 text-sm font-semibold transition-colors ${
            recommended === false ? "border-danger bg-danger/10 text-danger" : "border-ink-200 dark:border-ink-700 text-ink-500"
          }`}
        >
          Recommend Rejection
        </button>
      </div>

      <Textarea
        label="Remarks"
        rows={3}
        placeholder="Explain your recommendation"
        value={remarks}
        onChange={(e) => setRemarks(e.target.value)}
        required
      />

      <Button className="mt-4" isLoading={isSubmitting} onClick={submit}>
        Submit review
      </Button>
    </Card>
  );
}

function AssignForm({ claim, onDone }) {
  const [insuranceOperationsOfficers, setInsuranceOperationsOfficers] = useState([]);
  const [insuranceOperationsOfficerId, setInsuranceOperationsOfficerId] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    userService.getInsuranceOperationsOfficers().then(setInsuranceOperationsOfficers).catch(() => {});
  }, []);

  const submit = async () => {
    setError("");
    if (!insuranceOperationsOfficerId) return setError("Select an insurance operations officer to assign.");

    setIsSubmitting(true);
    try {
      await claimService.assign(claim.claimId, Number(insuranceOperationsOfficerId));
      toast.success("Claim assigned.");
      onDone();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card>
      <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white mb-4">Assign to an insurance operations officer</h3>
      {error && <Alert type="error" className="mb-4">{error}</Alert>}

      <Select
        placeholder="Select an insurance operations officer"
        options={insuranceOperationsOfficers.map((a) => ({ value: a.userId, label: `${a.fullName} - ${a.email}` }))}
        value={insuranceOperationsOfficerId}
        onChange={(e) => setInsuranceOperationsOfficerId(e.target.value)}
      />

      <Button className="mt-4" icon={UserPlus} isLoading={isSubmitting} onClick={submit}>
        Assign claim
      </Button>
    </Card>
  );
}

function DecisionForm({ claim, onDone }) {
  const [remarks, setRemarks] = useState("");
  const [error, setError] = useState("");
  const [pendingAction, setPendingAction] = useState(null);

  const submit = async (action) => {
    setError("");
    if (remarks.trim().length < 5) return setError("Remarks must be at least 5 characters.");

    setPendingAction(action);
    try {
      if (action === "approve") await claimService.approve(claim.claimId, remarks);
      else await claimService.reject(claim.claimId, remarks);
      toast.success(`Claim ${action === "approve" ? "approved" : "rejected"}.`);
      onDone();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setPendingAction(null);
    }
  };

  return (
    <Card>
      <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white mb-4">Final decision</h3>
      {error && <Alert type="error" className="mb-4">{error}</Alert>}

      <Textarea
        label="Decision remarks"
        rows={3}
        placeholder="Explain the final decision"
        value={remarks}
        onChange={(e) => setRemarks(e.target.value)}
        required
      />

      <div className="mt-4 flex gap-3">
        <Button variant="primary" className="flex-1" icon={CheckCircle2} isLoading={pendingAction === "approve"} onClick={() => submit("approve")}>
          Approve
        </Button>
        <Button variant="danger" className="flex-1" icon={XCircle} isLoading={pendingAction === "reject"} onClick={() => submit("reject")}>
          Reject
        </Button>
      </div>
    </Card>
  );
}

export default function ClaimDetailPage() {
  const { claimId } = useParams();
  const { role, user } = useAuth();
  const navigate = useNavigate();

  const [claim, setClaim] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [history, setHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const [uploadOpen, setUploadOpen] = useState(false);
  const [uploadFile, setUploadFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);

  const load = () => {
    setIsLoading(true);
    Promise.all([
      claimService.getById(claimId),
      claimDocumentService.getByClaim(claimId),
      claimHistoryService.getHistory(claimId, { page: 0, size: 50 }),
    ])
      .then(([claimData, docData, historyData]) => {
        setClaim(claimData);
        setDocuments(docData || []);
        setHistory(historyData?.content ?? []);
      })
      .catch((err) => toast.error(getErrorMessage(err, "Could not load this claim.")))
      .finally(() => setIsLoading(false));
  };

  useEffect(load, [claimId]);

  const handleUpload = async () => {
    if (!uploadFile) return;
    setIsUploading(true);
    try {
      await claimDocumentService.upload(claimId, uploadFile);
      toast.success("Document uploaded.");
      setUploadOpen(false);
      setUploadFile(null);
      load();
    } catch (error) {
      toast.error(getErrorMessage(error, "Upload failed."));
    } finally {
      setIsUploading(false);
    }
  };

  const handleDeleteDocument = async (documentId) => {
    try {
      await claimDocumentService.remove(documentId);
      toast.success("Document removed.");
      load();
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  };

  if (isLoading) return <Spinner label="Loading claim..." />;
  if (!claim) return null;

  const isOwnAssignedInsuranceOperationsOfficer = role === ROLES.INSURANCE_OPERATIONS_OFFICER && claim.assignedInsuranceOperationsOfficerName === user?.name;
  const canReview = isOwnAssignedInsuranceOperationsOfficer && ["ASSIGNED", "UNDER_REVIEW"].includes(claim.claimStatus);
  const canAssign = role === ROLES.ADMIN && claim.claimStatus === "SUBMITTED";
  const canDecide = role === ROLES.ADMIN && ["RECOMMENDED_APPROVAL", "RECOMMENDED_REJECTION", "UNDER_REVIEW"].includes(claim.claimStatus);

  return (
    <div>
      <button
        onClick={() => navigate(-1)}
        className="mb-4 flex items-center gap-1.5 text-sm font-medium text-ink-500 hover:text-harbor-600 dark:hover:text-harbor-400"
      >
        <ArrowLeft className="h-4 w-4" /> Back
      </button>

      <PageHeader
        eyebrow={claim.claimNumber}
        title={formatCurrency(claim.claimAmount)}
        description={`Filed by ${claim.customerName} against policy ${claim.policyNumber}`}
        actions={<Stamp status={claim.claimStatus} className="text-sm" />}
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white mb-3">Claim details</h3>
            <dl className="grid grid-cols-2 gap-4 text-sm">
              <div><dt className="text-ink-400">Incident date</dt><dd className="font-medium text-ink-800 dark:text-ink-100">{formatDate(claim.incidentDate)}</dd></div>
              <div><dt className="text-ink-400">Assigned insurance operations officer</dt><dd className="font-medium text-ink-800 dark:text-ink-100">{claim.assignedInsuranceOperationsOfficerName || "Unassigned"}</dd></div>
              <div className="col-span-2"><dt className="text-ink-400">Reason</dt><dd className="mt-1 text-ink-700 dark:text-ink-200">{claim.claimReason}</dd></div>
              {claim.insuranceOperationsOfficerRemarks && <div className="col-span-2"><dt className="text-ink-400">Insurance Operations Officer remarks</dt><dd className="mt-1 text-ink-700 dark:text-ink-200">{claim.insuranceOperationsOfficerRemarks}</dd></div>}
              {claim.adminRemarks && <div className="col-span-2"><dt className="text-ink-400">Admin remarks</dt><dd className="mt-1 text-ink-700 dark:text-ink-200">{claim.adminRemarks}</dd></div>}
            </dl>

            {claim.planDetails && (
              <div className="mt-5 pt-5 border-t border-ink-100 dark:border-ink-800 grid grid-cols-2 gap-4 text-sm">
                <div><dt className="text-ink-400">Plan</dt><dd className="font-medium text-ink-800 dark:text-ink-100">{claim.planDetails.planName}</dd></div>
                <div><dt className="text-ink-400">Coverage</dt><dd className="font-mono-data font-medium text-ink-800 dark:text-ink-100">{formatCurrency(claim.planDetails.coverageAmount)}</dd></div>
              </div>
            )}

            {claim.planSummary && (
              <div className="mt-5 pt-5 border-t border-ink-100 dark:border-ink-800 grid grid-cols-3 gap-4 text-sm">
                <div><dt className="text-ink-400">Previous claims</dt><dd className="font-medium text-ink-800 dark:text-ink-100">{claim.planSummary.totalPreviousClaims}</dd></div>
                <div><dt className="text-ink-400">Previously claimed</dt><dd className="font-mono-data font-medium text-ink-800 dark:text-ink-100">{formatCurrency(claim.planSummary.totalPreviousClaimAmount)}</dd></div>
                <div><dt className="text-ink-400">Remaining coverage</dt><dd className="font-mono-data font-medium text-success">{formatCurrency(claim.planSummary.remainingCoverage)}</dd></div>
              </div>
            )}
          </Card>

          <Card>
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white">Documents</h3>
              {role === ROLES.CUSTOMER && (
                <Button size="sm" variant="outline" icon={UploadCloud} onClick={() => setUploadOpen(true)}>
                  Upload file
                </Button>
              )}
            </div>

            {documents.length === 0 ? (
              <p className="text-sm text-ink-500">No documents on file.</p>
            ) : (
              <div className="space-y-2">
                {documents.map((doc) => (
                  <div key={doc.claimDocumentId} className="flex items-center justify-between rounded-lg border border-ink-100 dark:border-ink-800 px-3.5 py-2.5">
                    <div className="flex items-center gap-3 min-w-0">
                      <FileText className="h-4 w-4 text-harbor-500 flex-shrink-0" />
                      <div className="min-w-0">
                        <a
                          href={doc.documentReference}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-sm font-medium text-harbor-600 dark:text-harbor-400 hover:underline truncate block"
                          title="Click to view document in new tab"
                        >
                          {doc.documentName}
                        </a>
                        <p className="text-xs text-ink-400">{doc.documentType} - {formatDateTime(doc.uploadedDate)}</p>
                      </div>
                    </div>
                    {role === ROLES.CUSTOMER && (
                      <button onClick={() => handleDeleteDocument(doc.claimDocumentId)} className="text-ink-400 hover:text-danger transition-colors flex-shrink-0">
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </Card>

          {claim.customerClaimHistory?.length > 0 && (
            <Card>
              <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white mb-4">Customer's claim history</h3>
              <div className="space-y-2">
                {claim.customerClaimHistory.map((h, i) => (
                  <div key={i} className="flex items-center justify-between rounded-lg border border-ink-100 dark:border-ink-800 px-3.5 py-2.5 text-sm">
                    <span className="font-mono-data text-xs">{h.claimNumber}</span>
                    <span className="text-ink-500">{h.planName}</span>
                    <span className="font-mono-data">{formatCurrency(h.claimedAmount)}</span>
                    <Stamp status={h.claimStatus} />
                  </div>
                ))}
              </div>
            </Card>
          )}

          <Card>
            <div className="flex items-center gap-2 mb-4">
              <History className="h-4 w-4 text-harbor-500" />
              <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white">Status history</h3>
            </div>
            {history.length === 0 ? (
              <p className="text-sm text-ink-500">No status changes recorded yet.</p>
            ) : (
              <ol className="space-y-4 border-l-2 border-ink-100 dark:border-ink-800 pl-5">
                {history.map((h) => (
                  <li key={h.historyId} className="relative">
                    <span className="absolute -left-[1.65rem] top-1 h-2.5 w-2.5 rounded-full bg-harbor-500" />
                    <p className="text-sm font-medium text-ink-800 dark:text-ink-100">
                      {toTitleCase(h.previousStatus) || "---"} <span className="text-ink-400">→</span> {toTitleCase(h.newStatus)}
                    </p>
                    <p className="text-xs text-ink-400 mt-0.5">{h.updatedBy} ({toTitleCase(h.updatedByRole)}) - {formatDateTime(h.updatedAt)}</p>
                    {h.remarks && <p className="text-xs text-ink-500 mt-1">{h.remarks}</p>}
                  </li>
                ))}
              </ol>
            )}
          </Card>
        </div>

        <div className="space-y-6">
          {canAssign && <AssignForm claim={claim} onDone={load} />}
          {canReview && <ReviewForm claim={claim} onDone={load} />}
          {canDecide && <DecisionForm claim={claim} onDone={load} />}

          {!canAssign && !canReview && !canDecide && (
            <Card>
              <p className="text-sm text-ink-500">No actions available for this claim right now.</p>
            </Card>
          )}
        </div>
      </div>

      <Modal
        open={uploadOpen}
        onClose={() => { setUploadOpen(false); setUploadFile(null); }}
        title="Upload supporting document"
        footer={
          <>
            <Button variant="outline" onClick={() => { setUploadOpen(false); setUploadFile(null); }}>Cancel</Button>
            <Button isLoading={isUploading} disabled={!uploadFile} onClick={handleUpload}>Upload</Button>
          </>
        }
      >
        <FileUpload onFileSelected={setUploadFile} />
      </Modal>
    </div>
  );
}