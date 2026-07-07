import { Link } from "react-router-dom";
import toast from "react-hot-toast";
import { PlusCircle, Download } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import DataTable from "../components/common/DataTable";
import Pagination from "../components/common/Pagination";
import Stamp from "../components/common/Stamp";
import Button from "../components/common/Button";
import { useAuth } from "../context/AuthContext";
import { paymentService } from "../services/paymentService";
import { usePagedResource } from "../hooks/usePagedResource";
import { ROLES } from "../utils/constants";
import { formatCurrency, formatDateTime, toTitleCase } from "../utils/formatters";
import { exportToCSV } from "../utils/exportCsv";

export default function PaymentsPage() {
  const { role } = useAuth();
  const { content, page, setPage, totalPages, isLoading } = usePagedResource(
    (params) => paymentService.getAll(params),
    { size: 10, sortBy: "id", sortDir: "desc" }
  );

  const handleExport = () => {
    if (!content || content.length === 0) {
      toast.error("No data to export.");
      return;
    }
    exportToCSV("payments", content, [
      { key: "paymentId", header: "Payment ID" },
      { key: "transactionReference", header: "Transaction Reference" },
      { key: "policyNumber", header: "Policy #" },
      { key: "amount", header: "Amount", format: (v) => formatCurrency(v) },
      { key: "paymentMode", header: "Payment Mode", format: (v) => toTitleCase(v) },
      { key: "paymentDate", header: "Payment Date", format: (v) => formatDateTime(v) },
      { key: "status", header: "Status" },
    ]);
    toast.success("Payments exported successfully.");
  };

  const columns = [
    { key: "transactionReference", header: "Reference", render: (r) => <span className="font-mono-data text-xs">{r.transactionReference}</span> },
    { key: "policyNumber", header: "Policy #", render: (r) => <span className="font-mono-data font-medium">{r.policyNumber}</span> },
    { key: "amount", header: "Amount", render: (r) => <span className="font-mono-data">{formatCurrency(r.amount)}</span> },
    { key: "paymentMode", header: "Mode", render: (r) => toTitleCase(r.paymentMode) },
    { key: "paymentDate", header: "Date", render: (r) => formatDateTime(r.paymentDate) },
    { key: "status", header: "Status", render: (r) => <Stamp status={r.status} /> },
  ];

  return (
    <div>
      <PageHeader
        eyebrow="Ledger"
        title={role === ROLES.CUSTOMER ? "My Payments" : "Payments"}
        description={
          role === ROLES.CUSTOMER
            ? "Every premium you've paid, with its transaction reference."
            : "Every premium payment recorded across all policies."
        }
        actions={
          <>
            <Button icon={Download} variant="outline" onClick={handleExport}>Export CSV</Button>
            {role === ROLES.CUSTOMER && (
              <Button as={Link} to="/dashboard/payments/new" icon={PlusCircle}>
                Pay a premium
              </Button>
            )}
          </>
        }
      />
      <DataTable columns={columns} data={content} isLoading={isLoading} keyField="paymentId" emptyTitle="No payments recorded" emptyDescription="Payments will show up here once made." />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
