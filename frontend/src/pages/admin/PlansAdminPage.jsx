import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { Plus, Pencil, Download } from "lucide-react";
import { useState } from "react";
import PageHeader from "../../components/common/PageHeader";
import DataTable from "../../components/common/DataTable";
import Button from "../../components/common/Button";
import Switch from "../../components/common/Switch";
import Pagination from "../../components/common/Pagination";
import Stamp from "../../components/common/Stamp";
import { planService } from "../../services/planService";
import { getErrorMessage } from "../../services/api";
import { usePagedResource } from "../../hooks/usePagedResource";
import { formatCurrency, toTitleCase } from "../../utils/formatters";
import { exportToCSV } from "../../utils/exportCsv";
import { useAuth } from "../../context/AuthContext";
import { ROLES } from "../../utils/constants";

export default function PlansAdminPage() {
  const navigate = useNavigate();
  const { role } = useAuth();
  const [busyId, setBusyId] = useState(null);
  const { content, page, setPage, totalPages, isLoading, refresh } = usePagedResource(
    (params) => planService.getAll(params),
    { size: 10, sortBy: "id", sortDir: "desc" }
  );

  const toggleActive = async (plan) => {
    setBusyId(plan.PolicyPlanId);
    try {
      if (plan.active) {
        await planService.deactivate(plan.PolicyPlanId);
        toast.success(`${plan.planName} deactivated.`);
      } else {
        await planService.activate(plan.PolicyPlanId);
        toast.success(`${plan.planName} activated.`);
      }
      refresh();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setBusyId(null);
    }
  };

  const handleExport = () => {
    if (!content || content.length === 0) {
      toast.error("No data to export.");
      return;
    }
    exportToCSV("plans", content, [
      { key: "PolicyPlanId", header: "Plan ID" },
      { key: "planName", header: "Plan Name" },
      { key: "productName", header: "Product" },
      { key: "minCoverageAmount", header: "Min Coverage (\u20b9)", format: (v) => formatCurrency(v) },
      { key: "maxCoverageAmount", header: "Max Coverage (\u20b9)", format: (v) => formatCurrency(v) },
      { key: "premiumType", header: "Premium Cycle", format: (v) => toTitleCase(v) },
      { key: "minDuration", header: "Min Duration (mo)" },
      { key: "maxDuration", header: "Max Duration (mo)" },
      { key: "active", header: "Status", format: (v) => (v ? "Active" : "Inactive") },
    ]);
    toast.success("Plans exported successfully.");
  };

  const baseColumns = [
    { key: "planName", header: "Plan", render: (r) => <span className="font-medium text-ink-900 dark:text-white">{r.planName}</span> },
    { key: "productName", header: "Product" },
    {
      key: "coverageRange",
      header: "Coverage Range",
      render: (r) => (
        <span className="font-mono-data">
          {formatCurrency(r.minCoverageAmount)} – {formatCurrency(r.maxCoverageAmount)}
        </span>
      ),
    },
    { key: "premiumType", header: "Cycle", render: (r) => toTitleCase(r.premiumType) },
    {
      key: "durationRange",
      header: "Duration Range",
      render: (r) => `${r.minDuration} – ${r.maxDuration} mo`,
    },
    { key: "active", header: "Status", render: (r) => <Stamp status={r.active} /> },
  ];

  const columns = role === ROLES.ADMIN
    ? [
        ...baseColumns,
        {
          key: "actions",
          header: "Actions",
          render: (r) => (
            <div className="flex items-center gap-3">
              <Button variant="outline" size="sm" icon={Pencil} onClick={() => navigate(`/dashboard/plans/${r.PolicyPlanId}/edit`)} />
              <Switch
                checked={r.active}
                isLoading={busyId === r.PolicyPlanId}
                onChange={() => toggleActive(r)}
              />
            </div>
          ),
        },
      ]
    : baseColumns;

  return (
    <div>
      <PageHeader
        eyebrow="Catalog"
        title="Plans"
        description="Every plan across every product line, in one manageable list."
        actions={
          <>
            <Button icon={Download} variant="outline" onClick={handleExport}>Export CSV</Button>
            {role === ROLES.ADMIN && <Button icon={Plus} onClick={() => navigate("/dashboard/plans/new")}>New plan</Button>}
          </>
        }
      />
      <DataTable columns={columns} data={content} isLoading={isLoading} keyField="PolicyPlanId" emptyTitle="No plans yet" />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
