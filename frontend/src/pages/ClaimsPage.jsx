import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { FileWarning, PlusCircle, Download, FileDown } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import DataTable from "../components/common/DataTable";
import Pagination from "../components/common/Pagination";
import Stamp from "../components/common/Stamp";
import Select from "../components/common/Select";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import EmptyState from "../components/common/EmptyState";

import { useAuth } from "../context/AuthContext";
import { claimService } from "../services/claimService";
import { getErrorMessage } from "../services/api";
import { usePagedResource } from "../hooks/usePagedResource";
import { ROLES, CLAIM_STATUSES } from "../utils/constants";
import { formatCurrency, formatDate } from "../utils/formatters";
import { exportToCSV } from "../utils/exportCsv";
import { downloadClaimSummaryPdf, downloadClaimsListPdf } from "../utils/claimPdfGenerator";

function CustomerClaims() {
  const [claims, setClaims] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState(null);

  useEffect(() => {
    claimService
      .getMy()
      .then(setClaims)
      .catch((err) => toast.error(getErrorMessage(err, "Could not load claims.")))
      .finally(() => setIsLoading(false));
  }, []);

  const handleDownload = async (e, claim) => {
    e.preventDefault();
    e.stopPropagation();
    setDownloadingId(claim.claimId);
    try {
      downloadClaimSummaryPdf(claim);
      toast.success("PDF downloaded.");
    } catch {
      toast.error("Could not generate PDF.");
    } finally {
      setDownloadingId(null);
    }
  };

  if (isLoading) return <Spinner label="Loading your claims..." />;

  if (claims.length === 0) {
    return (
      <EmptyState
        icon={FileWarning}
        title="No claims raised yet"
        description="If you need to make a claim against an active policy, you can start one here."
        action={<Button as={Link} to="/dashboard/claims/new" className="mt-2">Raise a claim</Button>}
      />
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
      {claims.map((c) => (
        <Link
          key={c.claimId}
          to={`/dashboard/claims/${c.claimId}`}
          className="relative rounded-2xl border border-ink-200/70 dark:border-ink-800 bg-white dark:bg-ink-900 p-5 shadow-sm hover:-translate-y-1 hover:shadow-lg hover:border-harbor-300 dark:hover:border-harbor-700 transition-all duration-300"
        >
          <div className="flex items-start justify-between">
            <p className="font-mono-data text-xs text-ink-400">{c.claimNumber}</p>
            <Stamp status={c.claimStatus} />
          </div>
          <p className="mt-3 font-display text-lg font-semibold text-ink-900 dark:text-white">{formatCurrency(c.claimAmount)}</p>
          <p className="mt-1 text-sm text-ink-500 line-clamp-2">{c.claimReason}</p>
          <p className="mt-3 text-xs text-ink-400">Incident on {formatDate(c.incidentDate)}</p>

          {/* Download PDF button */}
          <button
            onClick={(e) => handleDownload(e, c)}
            disabled={downloadingId === c.claimId}
            title="Download PDF"
            className="absolute bottom-4 right-4 flex items-center gap-1 text-xs font-semibold text-harbor-600 dark:text-harbor-400 hover:text-harbor-800 dark:hover:text-harbor-200 transition-colors disabled:opacity-50"
          >
            <FileDown className="h-4 w-4" />
            {downloadingId === c.claimId ? "..." : "PDF"}
          </button>
        </Link>
      ))}
    </div>
  );
}

function PrivilegedClaims() {
  const navigate = useNavigate();
  const [downloadingId, setDownloadingId] = useState(null);
  const { content, page, setPage, totalPages, isLoading, filters, setFilter } = usePagedResource(
    (params) => claimService.getAll(params),
    { size: 10, sortBy: "id", sortDir: "desc" }
  );

  const handleExport = () => {
    if (!content || content.length === 0) {
      toast.error("No data to export.");
      return;
    }
    exportToCSV("claims", content, [
      { key: "claimId", header: "Claim ID" },
      { key: "claimNumber", header: "Claim #" },
      { key: "customerName", header: "Customer" },
      { key: "policyNumber", header: "Policy #" },
      { key: "claimAmount", header: "Amount", format: (v) => formatCurrency(v) },
      { key: "claimReason", header: "Reason" },
      { key: "incidentDate", header: "Incident Date", format: (v) => formatDate(v) },
      { key: "assignedInsuranceOperationsOfficerName", header: "Assigned IOO", format: (v) => v || "Unassigned" },
      { key: "claimStatus", header: "Status" },
    ]);
    toast.success("Claims exported successfully.");
  };

  const handleBulkPdf = () => {
    if (!content || content.length === 0) {
      toast.error("No data to export.");
      return;
    }
    try {
      downloadClaimsListPdf(content, "Claims Report");
      toast.success("Claims PDF downloaded.");
    } catch {
      toast.error("Could not generate PDF.");
    }
  };

  const handleRowPdf = async (e, claim) => {
    e.stopPropagation();
    setDownloadingId(claim.claimId);
    try {
      downloadClaimSummaryPdf(claim);
      toast.success("PDF downloaded.");
    } catch {
      toast.error("Could not generate PDF.");
    } finally {
      setDownloadingId(null);
    }
  };

  const columns = [
    { key: "claimNumber", header: "Claim #", render: (r) => <span className="font-mono-data font-medium">{r.claimNumber}</span> },
    { key: "customerName", header: "Customer" },
    { key: "policyNumber", header: "Policy #", render: (r) => <span className="font-mono-data text-xs">{r.policyNumber}</span> },
    { key: "claimAmount", header: "Amount", render: (r) => <span className="font-mono-data">{formatCurrency(r.claimAmount)}</span> },
    { key: "assignedInsuranceOperationsOfficerName", header: "Insurance Operations Officer", render: (r) => r.assignedInsuranceOperationsOfficerName || "--- Unassigned ---" },
    { key: "claimStatus", header: "Status", render: (r) => <Stamp status={r.claimStatus} /> },
    {
      key: "actions",
      header: "",
      render: (r) => (
        <div className="flex items-center gap-2">
          <Button size="sm" variant="outline" onClick={() => navigate(`/dashboard/claims/${r.claimId}`)}>
            Review
          </Button>
          <button
            onClick={(e) => handleRowPdf(e, r)}
            disabled={downloadingId === r.claimId}
            title="Download PDF"
            className="flex items-center gap-1 rounded-lg border border-ink-300 dark:border-ink-600 px-2.5 py-1.5 text-xs font-semibold text-ink-700 dark:text-ink-200 hover:bg-ink-100 dark:hover:bg-ink-800 transition-colors disabled:opacity-50"
          >
            <FileDown className="h-3.5 w-3.5" />
            {downloadingId === r.claimId ? "..." : "PDF"}
          </button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div className="mb-4 flex flex-wrap items-center gap-3">
        <Select
          containerClassName="w-56"
          placeholder="All statuses"
          options={CLAIM_STATUSES.map((s) => ({ value: s, label: s.replaceAll("_", " ") }))}
          value={filters.status || ""}
          onChange={(e) => setFilter("status", e.target.value)}
        />
        <Button icon={Download} variant="outline" size="sm" onClick={handleExport}>Export CSV</Button>
        <Button icon={FileDown} variant="outline" size="sm" onClick={handleBulkPdf}>Download PDF</Button>
      </div>
      <DataTable columns={columns} data={content} isLoading={isLoading} keyField="claimId" emptyTitle="No claims found" />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}

export default function ClaimsPage() {
  const { role } = useAuth();
  const isCustomer = role === ROLES.CUSTOMER;

  return (
    <div>
      <PageHeader
        eyebrow="Claims"
        title={isCustomer ? "My Claims" : "Claims"}
        description={
          isCustomer
            ? "Track every claim you've raised, start to settlement."
            : "Every claim in the system - assign, review, and decide."
        }
        actions={
          isCustomer && (
            <Button as={Link} to="/dashboard/claims/new" icon={PlusCircle}>
              Raise a claim
            </Button>
          )
        }
      />
      {isCustomer ? <CustomerClaims /> : <PrivilegedClaims />}
    </div>
  );
}