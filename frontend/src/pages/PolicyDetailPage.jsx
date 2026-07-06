import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import { ArrowLeft, Ban, Wallet } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import Card from "../components/common/Card";
import Stamp from "../components/common/Stamp";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import DataTable from "../components/common/DataTable";
import Modal from "../components/common/Modal";
import { useAuth } from "../context/AuthContext";
import { policyService } from "../services/policyService";
import { paymentService } from "../services/paymentService";
import { getErrorMessage } from "../services/api";
import { ROLES } from "../utils/constants";
import { formatCurrency, formatDate, formatDateTime, toTitleCase } from "../utils/formatters";

export default function PolicyDetailPage() {
  const { policyId } = useParams();
  const { role } = useAuth();
  const navigate = useNavigate();

  const [policy, setPolicy] = useState(null);
  const [payments, setPayments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [isCancelling, setIsCancelling] = useState(false);

  const load = () => {
    setIsLoading(true);
    Promise.all([policyService.getById(policyId), paymentService.getByPolicy(policyId)])
      .then(([policyData, paymentData]) => {
        setPolicy(policyData);
        setPayments(paymentData || []);
      })
      .catch((err) => toast.error(getErrorMessage(err, "Could not load this policy.")))
      .finally(() => setIsLoading(false));
  };

  useEffect(load, [policyId]);

  const handleCancel = async () => {
    setIsCancelling(true);
    try {
      await policyService.cancel(policyId);
      toast.success("Policy cancelled.");
      setShowCancelModal(false);
      load();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsCancelling(false);
    }
  };

  if (isLoading) return <Spinner label="Loading policy…" />;
  if (!policy) return null;

  const columns = [
    { key: "transactionReference", header: "Reference", render: (r) => <span className="font-mono-data text-xs">{r.transactionReference}</span> },
    { key: "amount", header: "Amount", render: (r) => <span className="font-mono-data">{formatCurrency(r.amount)}</span> },
    { key: "paymentMode", header: "Mode", render: (r) => toTitleCase(r.paymentMode) },
    { key: "paymentDate", header: "Date", render: (r) => formatDateTime(r.paymentDate) },
    { key: "status", header: "Status", render: (r) => <Stamp status={r.status} /> },
  ];

  return (
    <div>
      <button
        onClick={() => navigate(-1)}
        className="mb-4 flex items-center gap-1.5 text-sm font-medium text-ink-500 hover:text-harbor-600 dark:hover:text-harbor-400"
      >
        <ArrowLeft className="h-4 w-4" /> Back
      </button>

      <PageHeader
        eyebrow={policy.policyNumber}
        title={policy.planName}
        description={`${toTitleCase(policy.productType)} coverage for ${policy.customerName}`}
        actions={
          <div className="flex items-center gap-2">
            {role === ROLES.CUSTOMER && policy.status === "PENDING_PAYMENT" && (
              <Button icon={Wallet} onClick={() => navigate(`/dashboard/payments/new?policyId=${policy.policyId}`)}>
                Pay premium
              </Button>
            )}
            {(role === ROLES.ADMIN || role === ROLES.AGENT) && policy.status !== "CANCELLED" && (
              <Button variant="danger" icon={Ban} onClick={() => setShowCancelModal(true)}>
                Cancel policy
              </Button>
            )}
          </div>
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5 mb-8">
        <Card>
          <p className="text-xs uppercase tracking-wider text-ink-400 font-semibold">Status</p>
          <div className="mt-2"><Stamp status={policy.status} /></div>
        </Card>
        <Card>
          <p className="text-xs uppercase tracking-wider text-ink-400 font-semibold">Coverage period</p>
          <p className="mt-2 font-mono-data text-sm font-semibold text-ink-800 dark:text-ink-100">
            {formatDate(policy.startDate)} – {formatDate(policy.endDate)}
          </p>
        </Card>
        <Card>
          <p className="text-xs uppercase tracking-wider text-ink-400 font-semibold">Total premium paid</p>
          <p className="mt-2 font-mono-data text-lg font-semibold text-ink-900 dark:text-white">
            {formatCurrency(policy.totalPremiumPaid)}
          </p>
        </Card>
      </div>

      <Card padded={false} className="overflow-hidden">
        <div className="px-5 sm:px-6 pt-5">
          <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white mb-4">Payment history</h3>
        </div>
        <div className="px-5 sm:px-6 pb-5">
          <DataTable columns={columns} data={payments} keyField="paymentId" emptyTitle="No payments recorded yet" />
        </div>
      </Card>

      <Modal
        open={showCancelModal}
        onClose={() => setShowCancelModal(false)}
        title="Cancel this policy?"
        footer={
          <>
            <Button variant="outline" onClick={() => setShowCancelModal(false)}>Keep policy</Button>
            <Button variant="danger" isLoading={isCancelling} onClick={handleCancel}>Yes, cancel</Button>
          </>
        }
      >
        <p className="text-sm text-ink-600 dark:text-ink-300">
          This will immediately stop coverage under policy <strong>{policy.policyNumber}</strong>. This action cannot be undone.
        </p>
      </Modal>
    </div>
  );
}
