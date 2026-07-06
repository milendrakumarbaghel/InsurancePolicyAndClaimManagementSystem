import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import toast from "react-hot-toast";
import { ArrowLeft, FilePlus2, Trash2, Send, UploadCloud } from "lucide-react";
import Card from "../../components/common/Card";
import PageHeader from "../../components/common/PageHeader";
import Select from "../../components/common/Select";
import Input from "../../components/common/Input";
import Textarea from "../../components/common/Textarea";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useForm } from "../../hooks/useForm";
import { policyService } from "../../services/policyService";
import { claimService } from "../../services/claimService";
import { claimDocumentService } from "../../services/claimDocumentService";
import { getErrorMessage } from "../../services/api";
import {
  patterns, required, minLength, maxLength, pattern, positive, max, pastOrPresent,
} from "../../utils/validators";
import { DOCUMENT_TYPE_SUGGESTIONS } from "../../utils/constants";

const schema = {
  policyId: [required("Please select a policy")],
  claimAmount: [required("Claim amount is required"), positive(), max(99999999, "Exceeds the maximum allowable limit")],
  claimReason: [
    required("Please describe the reason for your claim"),
    minLength(10, "Must be between 10 and 1000 characters"),
    maxLength(1000, "Must be between 10 and 1000 characters"),
    pattern(patterns.noAngleBrackets, "Cannot contain < or >"),
  ],
  incidentDate: [required("Incident date is required"), pastOrPresent("Cannot be a future date")],
};

const emptyDocument = () => ({ documentName: "", documentType: "", documentReference: "", file: null });

export default function RaiseClaimPage() {
  const navigate = useNavigate();
  const [policies, setPolicies] = useState([]);
  const [isLoadingPolicies, setIsLoadingPolicies] = useState(true);
  const [documents, setDocuments] = useState([emptyDocument()]);
  const [documentErrors, setDocumentErrors] = useState([]);

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { policyId: "", claimAmount: "", claimReason: "", incidentDate: "" },
    schema,
    onSubmit: async (formValues) => {
      const docErrors = documents.map((doc) => {
        const e = {};
        if (!doc.documentName || doc.documentName.length < 3) e.documentName = "Min 3 characters";
        else if (!patterns.documentName.test(doc.documentName)) e.documentName = "Invalid characters";
        if (!doc.documentType || doc.documentType.length < 2) e.documentType = "Min 2 characters";
        else if (!patterns.documentType.test(doc.documentType)) e.documentType = "Letters, spaces, / or - only";
        if (!doc.documentReference || doc.documentReference.length < 5) e.documentReference = "Min 5 characters";
        if (!doc.file) e.file = "Please upload a file";
        return e;
      });
      setDocumentErrors(docErrors);
      const hasDocErrors = docErrors.some((e) => Object.keys(e).length > 0);
      if (hasDocErrors) throw new Error("Please fix the highlighted document fields.");
      if (documents.length === 0) throw new Error("At least one supporting document is required.");

      const claim = await claimService.raise({
        policyId: Number(formValues.policyId),
        claimAmount: Number(formValues.claimAmount),
        claimReason: formValues.claimReason,
        incidentDate: formValues.incidentDate,
        documents: documents.map(({ documentName, documentType, documentReference }) => ({
          documentName,
          documentType,
          documentReference,
        })),
      });

      const uploadPromises = documents
        .filter((doc) => doc.file)
        .map((doc) => claimDocumentService.upload(claim.claimId, doc.file));

      if (uploadPromises.length > 0) {
        await Promise.all(uploadPromises);
      }

      toast.success(`Claim ${claim.claimNumber} submitted.`);
      navigate(`/dashboard/claims/${claim.claimId}`);
    },
  });

  useEffect(() => {
    policyService
      .getMy()
      .then((data) => setPolicies(data.filter((p) => p.status === "ACTIVE")))
      .catch((err) => toast.error(getErrorMessage(err, "Could not load your policies.")))
      .finally(() => setIsLoadingPolicies(false));
  }, []);

  const updateDocument = (index, field, value) => {
    setDocuments((prev) => prev.map((d, i) => (i === index ? { ...d, [field]: value } : d)));
  };

  const addDocument = () => {
    if (documents.length >= 10) return;
    setDocuments((prev) => [...prev, emptyDocument()]);
  };

  const removeDocument = (index) => {
    if (documents.length <= 1) return;
    setDocuments((prev) => prev.filter((_, i) => i !== index));
  };

  return (
    <div className="max-w-2xl">
      <Link to="/dashboard/claims" className="mb-4 flex items-center gap-1.5 text-sm font-medium text-ink-500 hover:text-harbor-600 dark:hover:text-harbor-400 w-fit">
        <ArrowLeft className="h-4 w-4" /> Back to claims
      </Link>

      <PageHeader eyebrow="Claims" title="Raise a Claim" description="Provide the incident details and supporting documents for your claim." />

      <Card>
        {isLoadingPolicies ? (
          <Spinner label="Loading your policies…" />
        ) : policies.length === 0 ? (
          <Alert type="warning">You don't have any active policies to claim against yet.</Alert>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-5" noValidate>
            {submitError && <Alert type="error">{submitError}</Alert>}

            <Select
              label="Policy"
              name="policyId"
              placeholder="Select an active policy"
              options={policies.map((p) => ({ value: p.policyId, label: `${p.policyNumber} — ${p.planName}` }))}
              value={values.policyId}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.policyId}
              required
            />

            <Input
              label="Claim amount (₹)"
              name="claimAmount"
              type="number"
              min="1"
              value={values.claimAmount}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.claimAmount}
              required
            />

            <Input
              label="Incident date"
              name="incidentDate"
              type="date"
              max={new Date().toISOString().split("T")[0]}
              value={values.incidentDate}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.incidentDate}
              required
            />

            <Textarea
              label="Claim reason"
              name="claimReason"
              rows={4}
              placeholder="Describe what happened, when, and what you're claiming for…"
              value={values.claimReason}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.claimReason}
              required
            />

            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-sm font-medium text-ink-700 dark:text-ink-200">
                  Supporting documents <span className="text-danger">*</span>
                </label>
                <Button type="button" size="sm" variant="outline" icon={FilePlus2} onClick={addDocument} disabled={documents.length >= 10}>
                  Add document
                </Button>
              </div>

              <div className="space-y-4">
                {documents.map((doc, index) => (
                  <div key={index} className="rounded-xl border border-ink-200 dark:border-ink-700 p-4 space-y-3 relative">
                    {documents.length > 1 && (
                      <button
                        type="button"
                        onClick={() => removeDocument(index)}
                        className="absolute top-3 right-3 text-ink-400 hover:text-danger transition-colors"
                        aria-label="Remove document"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                    <Input
                      label="Document name"
                      placeholder="Hospital Bill.pdf"
                      value={doc.documentName}
                      onChange={(e) => updateDocument(index, "documentName", e.target.value)}
                      error={documentErrors[index]?.documentName}
                    />
                    <Input
                      label="Document type"
                      placeholder="Medical Bill"
                      list={`doc-type-suggestions-${index}`}
                      value={doc.documentType}
                      onChange={(e) => updateDocument(index, "documentType", e.target.value)}
                      error={documentErrors[index]?.documentType}
                    />
                    <datalist id={`doc-type-suggestions-${index}`}>
                      {DOCUMENT_TYPE_SUGGESTIONS.map((s) => <option key={s} value={s} />)}
                    </datalist>
                    <Input
                      label="Original document"
                      placeholder="Invoice #4521 from City Hospital dated 12 June 2026"
                      value={doc.documentReference}
                      onChange={(e) => updateDocument(index, "documentReference", e.target.value)}
                      error={documentErrors[index]?.documentReference}
                    />

                    <div className="flex flex-col gap-1.5">
                      <span className="text-sm font-medium text-ink-700 dark:text-ink-200">
                        Upload File <span className="text-danger">*</span>
                      </span>
                      <div className="flex items-center gap-3">
                        <label className="flex items-center gap-2 cursor-pointer rounded-lg border border-ink-300 dark:border-ink-700 bg-white dark:bg-ink-900 px-4 py-2 text-sm font-medium text-ink-700 dark:text-ink-200 hover:bg-ink-50 dark:hover:bg-ink-800 transition-colors">
                          <UploadCloud className="h-4 w-4 text-ink-400" />
                          <span>{doc.file ? "Change File" : "Choose File"}</span>
                          <input
                            type="file"
                            accept=".pdf,.jpg,.jpeg,.png"
                            className="hidden"
                            onChange={(e) => {
                              const file = e.target.files?.[0];
                              if (file) {
                                updateDocument(index, "file", file);
                                updateDocument(index, "documentName", file.name);
                                let docType = "Medical Bill";
                                if (file.type.includes("pdf")) docType = "PDF Document";
                                else if (file.type.includes("image")) docType = "Image Scan";
                                updateDocument(index, "documentType", docType);
                              }
                            }}
                          />
                        </label>
                        {doc.file && (
                          <span className="text-xs text-ink-500 font-mono-data truncate max-w-[200px]" title={doc.file.name}>
                            {doc.file.name}
                          </span>
                        )}
                      </div>
                      {documentErrors[index]?.file && (
                        <p className="text-xs font-medium text-danger mt-1">{documentErrors[index].file}</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex gap-3 pt-2">
              <Button type="submit" isLoading={isSubmitting} icon={Send}>
                Submit claim
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate("/dashboard/claims")}>
                Cancel
              </Button>
            </div>
          </form>
        )}
      </Card>
    </div>
  );
}
