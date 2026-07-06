import { useNavigate } from "react-router-dom";
import PageHeader from "../../components/common/PageHeader";
import DataTable from "../../components/common/DataTable";
import Pagination from "../../components/common/Pagination";
import Stamp from "../../components/common/Stamp";
import Button from "../../components/common/Button";
import { claimService } from "../../services/claimService";
import { usePagedResource } from "../../hooks/usePagedResource";
import { formatCurrency, formatDate } from "../../utils/formatters";

export default function AssignedClaimsPage() {
  const navigate = useNavigate();
  const { content, page, setPage, totalPages, isLoading } = usePagedResource(
    (params) => claimService.getAssigned(params),
    { size: 10, sortBy: "assignedAt", sortDir: "desc" }
  );

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
        <Button size="sm" onClick={() => navigate(`/dashboard/claims/${r.claimId}`)}>
          Review
        </Button>
      ),
    },
  ];

  return (
    <div>
      <PageHeader eyebrow="Queue" title="Assigned Claims" description="Claims routed to you for review — recommend approval or rejection." />
      <DataTable columns={columns} data={content} isLoading={isLoading} keyField="claimId" emptyTitle="Nothing assigned to you yet" emptyDescription="New claims will appear here once an administrator assigns them to you." />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
