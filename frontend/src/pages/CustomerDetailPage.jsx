import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import { ArrowLeft } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import Card from "../components/common/Card";
import DataTable from "../components/common/DataTable";
import Stamp from "../components/common/Stamp";
import Spinner from "../components/common/Spinner";
import Button from "../components/common/Button";
import { customerService } from "../services/customerService";
import { policyService } from "../services/policyService";
import { getErrorMessage } from "../services/api";
import { formatCurrency, formatDate, toTitleCase } from "../utils/formatters";

export default function CustomerDetailPage() {
  const { customerId } = useParams();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [policies, setPolicies] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    setIsLoading(true);
    Promise.all([
      customerService.getById(customerId),
      policyService.getAll({ page: 0, size: 50, customerId }),
    ])
      .then(([customerData, policyData]) => {
        setCustomer(customerData);
        setPolicies(policyData?.content ?? []);
      })
      .catch((err) => toast.error(getErrorMessage(err, "Could not load this customer.")))
      .finally(() => setIsLoading(false));
  }, [customerId]);

  if (isLoading) return <Spinner label="Loading customer…" />;
  if (!customer) return null;

  const columns = [
    { key: "policyNumber", header: "Policy #", render: (r) => <span className="font-mono-data font-medium">{r.policyNumber}</span> },
    { key: "planName", header: "Plan" },
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
      <button
        onClick={() => navigate(-1)}
        className="mb-4 flex items-center gap-1.5 text-sm font-medium text-ink-500 hover:text-harbor-600 dark:hover:text-harbor-400"
      >
        <ArrowLeft className="h-4 w-4" /> Back to customers
      </button>

      <PageHeader eyebrow="Customer" title={customer.fullName} description={customer.email} />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5 mb-8">
        <Card>
          <p className="text-xs uppercase tracking-wider text-ink-400 font-semibold mb-3">Contact</p>
          <p className="text-sm text-ink-800 dark:text-ink-100"> PHONE:{customer.mobileNumber}</p>
          <p className="text-sm text-ink-800 dark:text-ink-100"> EMAIL:{customer.email}</p>
        </Card>
        <Card>
          <p className="text-xs uppercase tracking-wider text-ink-400 font-semibold mb-3">Address</p>
          <p className="text-sm text-ink-800 dark:text-ink-100">{customer.address}</p>
          <p className="text-sm text-ink-800 dark:text-ink-100">{customer.city}, {customer.state} — {customer.pinCode}</p>
        </Card>
        <Card>
          <p className="text-xs uppercase tracking-wider text-ink-400 font-semibold mb-3">
            Nominee Directory ({customer.nominees?.length || 0})
          </p>
          {!customer.nominees || customer.nominees.length === 0 ? (
            <p className="text-sm text-ink-500 italic">No nominees assigned.</p>
          ) : (
            <div className="space-y-2.5">
              {customer.nominees.map((nom, idx) => (
                <div key={idx} className="text-sm border-b border-ink-100 dark:border-ink-800 last:border-none pb-2 last:pb-0">
                  <p className="font-medium text-ink-800 dark:text-ink-100">{nom.name}</p>
                  <p className="text-xs text-harbor-600 dark:text-harbor-400 font-semibold mt-0.5">
                    {toTitleCase(nom.relation)}
                  </p>
                </div>
              ))}
            </div>
          )}
          <p className="text-xs text-ink-400 mt-3 pt-2 border-t border-ink-100 dark:border-ink-800">
            Customer DOB: {formatDate(customer.dateOfBirth)}
          </p>
        </Card>
      </div>

      <Card padded={false} className="overflow-hidden">
        <div className="px-5 sm:px-6 pt-5">
          <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white mb-4">Policies</h3>
        </div>
        <div className="px-5 sm:px-6 pb-5">
          <DataTable columns={columns} data={policies} keyField="policyId" emptyTitle="No policies for this customer" />
        </div>
      </Card>
    </div>
  );
}
