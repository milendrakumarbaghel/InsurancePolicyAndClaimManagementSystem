import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { Plus, Pencil, Power, PowerOff } from "lucide-react";
import { useState } from "react";
import PageHeader from "../../components/common/PageHeader";
import DataTable from "../../components/common/DataTable";
import Button from "../../components/common/Button";
import Pagination from "../../components/common/Pagination";
import Stamp from "../../components/common/Stamp";
import { planService } from "../../services/planService";
import { getErrorMessage } from "../../services/api";
import { usePagedResource } from "../../hooks/usePagedResource";
import { formatCurrency, toTitleCase } from "../../utils/formatters";
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

  const columns = [
    { key: "planName", header: "Plan", render: (r) => <span className="font-medium text-ink-900 dark:text-white">{r.planName}</span> },
    { key: "productName", header: "Product" },
    { key: "coverageAmount", header: "Coverage", render: (r) => <span className="font-mono-data">{formatCurrency(r.coverageAmount)}</span> },
    { key: "premiumAmount", header: "Premium", render: (r) => <span className="font-mono-data">{formatCurrency(r.premiumAmount)}</span> },
    { key: "premiumType", header: "Cycle", render: (r) => toTitleCase(r.premiumType) },
    { key: "duration", header: "Duration", render: (r) => `${r.duration} mo` },
    { key: "active", header: "Status", render: (r) => <Stamp status={r.active} /> },
    {
      key: "actions",
      header: "",
      render: (r) => (
        <div className="flex items-center gap-2">
          {role === ROLES.ADMIN && (
            <>
              <Button variant="outline" size="sm" icon={Pencil} onClick={() => navigate(`/dashboard/plans/${r.PolicyPlanId}/edit`)} />
              <Button
                variant={r.active ? "danger" : "primary"}
                size="sm"
                icon={r.active ? PowerOff : Power}
                isLoading={busyId === r.PolicyPlanId}
                onClick={() => toggleActive(r)}
              />
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        eyebrow="Catalog"
        title="Plans"
        description="Every plan across every product line, in one manageable list."
        actions={role === ROLES.ADMIN && <Button icon={Plus} onClick={() => navigate("/dashboard/plans/new")}>New plan</Button>}
      />
      <DataTable columns={columns} data={content} isLoading={isLoading} keyField="PolicyPlanId" emptyTitle="No plans yet" />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
