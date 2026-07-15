import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import { ArrowLeft, ShieldCheck, Pencil, Plus } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import Card from "../components/common/Card";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import EmptyState from "../components/common/EmptyState";
import Stamp from "../components/common/Stamp";
import Modal from "../components/common/Modal";
import { useAuth } from "../context/AuthContext";
import { planService } from "../services/planService";
import { productService } from "../services/productService";
import { policyService } from "../services/policyService";
import { getErrorMessage } from "../services/api";
import { ROLES } from "../utils/constants";
import { formatCurrency, toTitleCase } from "../utils/formatters";

export default function PlansByProductPage() {
  const { productId } = useParams();
  const { role } = useAuth();
  const isAdmin = role === ROLES.ADMIN;
  const isCustomer = role === ROLES.CUSTOMER;
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [plans, setPlans] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [confirmPlan, setConfirmPlan] = useState(null);
  const [isPurchasing, setIsPurchasing] = useState(false);

  const load = () => {
    setIsLoading(true);
    Promise.all([productService.getById(productId), planService.getByProduct(productId)])
      .then(([productData, planData]) => {
        setProduct(productData);
        setPlans((planData || []).filter((p) => isAdmin || role === ROLES.AGENT || p.active));
      })
      .catch((err) => toast.error(getErrorMessage(err, "Could not load plans.")))
      .finally(() => setIsLoading(false));
  };

  useEffect(load, [productId, isAdmin]);

  const handlePurchase = async () => {
    if (!confirmPlan) return;
    setIsPurchasing(true);
    try {
      const policy = await policyService.purchase(confirmPlan.PolicyPlanId);
      toast.success("Policy issued! Complete payment to activate it.");
      setConfirmPlan(null);
      navigate(`/dashboard/policies/${policy.policyId}`);
    } catch (error) {
      toast.error(getErrorMessage(error, "Could not purchase this plan."));
    } finally {
      setIsPurchasing(false);
    }
  };

  if (isLoading) return <Spinner label="Loading plans…" />;

  return (
    <div>
      <button
        onClick={() => navigate("/dashboard/products")}
        className="mb-4 flex items-center gap-1.5 text-sm font-medium text-ink-500 hover:text-harbor-600 dark:hover:text-harbor-400"
      >
        <ArrowLeft className="h-4 w-4" /> Back to products
      </button>

      <PageHeader
        eyebrow={toTitleCase(product?.productType)}
        title={product?.productName}
        description={product?.description}
        actions={
          isAdmin && (
            <Button icon={Plus} onClick={() => navigate(`/dashboard/plans/new?productId=${productId}`)}>
              New plan
            </Button>
          )
        }
      />

      {plans.length === 0 ? (
        <EmptyState icon={ShieldCheck} title="No plans available" description="Check back soon, or add one if you manage this product." />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {plans.map((plan) => (
            <Card key={plan.PolicyPlanId} className="flex flex-col">
              <div className="flex items-start justify-between">
                <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white">{plan.planName}</h3>
                <Stamp status={plan.active} />
              </div>
              <div className="mt-4 space-y-2 text-sm flex-1">
                <div className="flex justify-between">
                  <span className="text-ink-500">Maximum Coverage Amount</span>
                  <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">{formatCurrency(plan.coverageAmount)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-ink-500">Premium Amount</span>
                  <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                    {formatCurrency(plan.premiumAmount)} / {toTitleCase(plan.premiumType)}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-ink-500">Maximum Duration (Months)</span>
                  <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">{plan.duration} months</span>
                </div>
              </div>
              <p className="mt-3 text-xs text-ink-400 line-clamp-2">{plan.termsAndConditions}</p>

              <div className="mt-5 flex gap-2">
                {isCustomer && plan.active && (
                  <Button className="flex-1" onClick={() => setConfirmPlan(plan)}>
                    Purchase plan
                  </Button>
                )}
                {role === ROLES.AGENT && (
                  <Button className="flex-1" disabled={true} title="Agents are not authorized to purchase plans">
                    Purchase plan
                  </Button>
                )}
                {isAdmin && (
                  <Button
                    variant="outline"
                    className="flex-1"
                    icon={Pencil}
                    onClick={() => navigate(`/dashboard/plans/${plan.PolicyPlanId}/edit`)}
                  >
                    Edit
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal
        open={!!confirmPlan}
        onClose={() => setConfirmPlan(null)}
        title="Confirm purchase"
        footer={
          <>
            <Button variant="outline" onClick={() => setConfirmPlan(null)}>Cancel</Button>
            <Button isLoading={isPurchasing} onClick={handlePurchase}>Confirm & issue policy</Button>
          </>
        }
      >
        <p className="text-sm text-ink-600 dark:text-ink-300">
          You're about to purchase <strong>{confirmPlan?.planName}</strong> for{" "}
          <strong>{formatCurrency(confirmPlan?.premiumAmount)}</strong> per {confirmPlan && toTitleCase(confirmPlan.premiumType).toLowerCase()} cycle.
          A policy will be issued in <em>pending payment</em> status — you'll pay the first premium next.
        </p>
      </Modal>
    </div>
  );
}
