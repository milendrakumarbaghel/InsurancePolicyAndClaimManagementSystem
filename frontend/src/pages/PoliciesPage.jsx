import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { ShieldCheck, PlusCircle } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import DataTable from "../components/common/DataTable";
import Pagination from "../components/common/Pagination";
import Stamp from "../components/common/Stamp";
import Select from "../components/common/Select";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import EmptyState from "../components/common/EmptyState";
import { useAuth } from "../context/AuthContext";
import { policyService } from "../services/policyService";
import { getErrorMessage } from "../services/api";
import { usePagedResource } from "../hooks/usePagedResource";
import { ROLES, POLICY_STATUSES } from "../utils/constants";
import { formatCurrency, formatDate, toTitleCase } from "../utils/formatters";

function CustomerPolicies() {
  const [policies, setPolicies] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    policyService
      .getMy()
      .then(setPolicies)
      .catch((err) => toast.error(getErrorMessage(err, "Could not load policies.")))
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) return <Spinner label="Loading your policies…" />;
  if (policies.length === 0) {
    return (
      <EmptyState
        icon={ShieldCheck}
        title="No policies yet"
        description="Once you purchase a plan, it will appear here."
        action={<Button as={Link} to="/dashboard/products" className="mt-2">Browse products</Button>}
      />
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
      {policies.map((p) => (
        <Link
          key={p.policyId}
          to={`/dashboard/policies/${p.policyId}`}
          className="rounded-2xl border border-ink-200/70 dark:border-ink-800 bg-white dark:bg-ink-900 p-5 shadow-sm hover:-translate-y-1 hover:shadow-lg hover:border-harbor-300 dark:hover:border-harbor-700 transition-all duration-300"
        >
          <div className="flex items-start justify-between">
            <span className="rounded-lg bg-harbor-50 dark:bg-ink-800 px-2.5 py-1 text-xs font-semibold uppercase tracking-wider text-harbor-600">
              {toTitleCase(p.productType)}
            </span>
            <Stamp status={p.status} />
          </div>
          <h3 className="mt-4 font-display text-lg font-semibold text-ink-900 dark:text-white">{p.planName}</h3>
          <p className="font-mono-data text-xs text-ink-400 mt-1">{p.policyNumber}</p>
          <div className="mt-4 flex justify-between text-sm">
            <span className="text-ink-500">{formatDate(p.startDate)} – {formatDate(p.endDate)}</span>
          </div>
          <div className="mt-2 flex justify-between text-sm">
            <span className="text-ink-500">Premium paid</span>
            <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">{formatCurrency(p.totalPremiumPaid)}</span>
          </div>
        </Link>
      ))}
    </div>
  );
}

function AdminAgentPolicies() {
  const navigate = useNavigate();
  const { content, page, setPage, totalPages, isLoading, filters, setFilter } = usePagedResource(
    (params) => policyService.getAll(params),
    { size: 10, sortBy: "id", sortDir: "desc" }
  );

  const columns = [
    { key: "policyNumber", header: "Policy #", render: (r) => <span className="font-mono-data font-medium">{r.policyNumber}</span> },
    { key: "customerName", header: "Customer" },
    { key: "planName", header: "Plan" },
    { key: "productType", header: "Type", render: (r) => toTitleCase(r.productType) },
    { key: "dates", header: "Coverage", render: (r) => `${formatDate(r.startDate)} – ${formatDate(r.endDate)}` },
    { key: "totalPremiumPaid", header: "Paid", render: (r) => <span className="font-mono-data">{formatCurrency(r.totalPremiumPaid)}</span> },
    { key: "status", header: "Status", render: (r) => <Stamp status={r.status} /> },
    {
      key: "actions",
      header: "",
      render: (r) => (
        <Button size="sm" variant="outline" onClick={() => navigate(`/dashboard/policies/${r.policyId}`)}>
          View
        </Button>
      ),
    },
  ];

  return (
    <div>
      <div className="mb-4 flex flex-wrap gap-3">
        <Select
          containerClassName="w-48"
          placeholder="All statuses"
          options={POLICY_STATUSES.map((s) => ({ value: s, label: toTitleCase(s) }))}
          value={filters.status || ""}
          onChange={(e) => setFilter("status", e.target.value)}
        />
      </div>
      <DataTable columns={columns} data={content} isLoading={isLoading} keyField="policyId" emptyTitle="No policies found" />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}

export default function PoliciesPage() {
  const { role } = useAuth();
  const isCustomer = role === ROLES.CUSTOMER;
  const isPrivileged = role === ROLES.ADMIN || role === ROLES.AGENT;

  return (
    <div>
      <PageHeader
        eyebrow="Coverage"
        title={isCustomer ? "My Policies" : "Policies"}
        description={
          isCustomer
            ? "Every plan you've purchased, and where it currently stands."
            : "All issued policies across every customer."
        }
        actions={
          role === ROLES.ADMIN && (
            <Button as={Link} to="/dashboard/policies-issue" icon={PlusCircle}>
              Issue policy
            </Button>
          )
        }
      />
      {isCustomer && <CustomerPolicies />}
      {isPrivileged && <AdminAgentPolicies />}
    </div>
  );
}
