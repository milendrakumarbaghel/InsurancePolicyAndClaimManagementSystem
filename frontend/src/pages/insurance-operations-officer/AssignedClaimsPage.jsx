import { useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { FileDown } from "lucide-react";
import PageHeader from "../../components/common/PageHeader";
import DataTable from "../../components/common/DataTable";
import Pagination from "../../components/common/Pagination";
import Stamp from "../../components/common/Stamp";
import Button from "../../components/common/Button";
import { claimService } from "../../services/claimService";
import { usePagedResource } from "../../hooks/usePagedResource";
import { formatCurrency, formatDate } from "../../utils/formatters";
import { downloadClaimSummaryPdf, downloadClaimsListPdf } from "../../utils/claimPdfGenerator";

export default function AssignedClaimsPage() {
  const navigate = useNavigate();
  const [downloadingId, setDownloadingId] = useState(null);

  const { content, page, setPage, totalPages, isLoading } = usePagedResource(
    (params) => claimService.getAssigned(params),
    { size: 10, sortBy: "assignedAt", sortDir: "desc" }
  );

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

  const handleBulkPdf = () => {
    if (!content || content.length === 0) {
      toast.error("No data to export.");
      return;
    }
    try {
      downloadClaimsListPdf(content, "Assigned Claims Report");
      toast.success("Claims PDF downloaded.");
    } catch {
      toast.error("Could not generate PDF.");
    }
  };

  const columns = [
    { key: "claimNumber", header: "Claim #", render: (r) => <span className="font-mono-data font-medium">{r.claimNumber}</span> },
    { key: "customerName", header: "Customer" },
    { key: "claimAmount", header: "Amount", render: (r) => <span className="font-mono-data">{formatCurrency(r.claimAmount)}</span> },
    { key: "assignedAt", header: "Assigned on", render: (r) => formatDate(r.assignedAt) },
    { key: "claimStatus", header: "Status", render: (r) => <Stamp status={r.claimStatus} /> },
    {
      key: "actions",
      header: "",
      render: (r) => (
        <div className="flex items-center gap-2">
          <Button size="sm" onClick={() => navigate(`/dashboard/claims/${r.claimId}`)}>
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
      <PageHeader
        eyebrow="Queue"
        title="Assigned Claims"
        description="Claims routed to you for review — recommend approval or rejection."
        actions={
          <Button icon={FileDown} variant="outline" size="sm" onClick={handleBulkPdf}>
            Download PDF
          </Button>
        }
      />
      <DataTable
        columns={columns}
        data={content}
        isLoading={isLoading}
        keyField="claimId"
        emptyTitle="Nothing assigned to you yet"
        emptyDescription="New claims will appear here once an administrator assigns them to you."
      />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
