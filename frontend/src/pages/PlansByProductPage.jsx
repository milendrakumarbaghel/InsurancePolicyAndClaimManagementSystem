import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";
import { ArrowLeft, ShieldCheck, Pencil, Plus, Calculator } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import Card from "../components/common/Card";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import EmptyState from "../components/common/EmptyState";
import Stamp from "../components/common/Stamp";
import Modal from "../components/common/Modal";
import Input from "../components/common/Input";
import Select from "../components/common/Select";
import Alert from "../components/common/Alert";
import { useAuth } from "../context/AuthContext";
import { planService } from "../services/planService";
import { productService } from "../services/productService";
import { policyService } from "../services/policyService";
import { getErrorMessage } from "../services/api";
import { ROLES, PREMIUM_TYPES } from "../utils/constants";
import { formatCurrency, toTitleCase } from "../utils/formatters";

// ── Premium Preview Hook ──────────────────────────────────────────────────────
function usePremiumPreview(planId, coverage, duration, premiumType) {
  const [preview, setPreview] = useState(null);
  const [isCalculating, setIsCalculating] = useState(false);
  const debounceRef = useRef(null);

  const recalculate = useCallback(() => {
    if (!planId || !coverage || !duration || !premiumType) {
      setPreview(null);
      return;
    }
    setIsCalculating(true);
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      planService
        .calculatePremium(planId, { coverage, duration, premiumType })
        .then((val) => setPreview(val))
        .catch(() => setPreview(null))
        .finally(() => setIsCalculating(false));
    }, 400); // debounce 400 ms
  }, [planId, coverage, duration, premiumType]);

  useEffect(() => {
    recalculate();
    return () => clearTimeout(debounceRef.current);
  }, [recalculate]);

  return { preview, isCalculating };
}

// ── Purchase Configuration Modal ─────────────────────────────────────────────
function PurchaseModal({ plan, open, onClose, onConfirm, isPurchasing }) {
  const [coverage, setCoverage] = useState("");
  const [duration, setDuration] = useState("");
  const [premiumType, setPremiumType] = useState("");

  // Reset when plan changes – pre-select the plan's default cycle if set
  useEffect(() => {
    if (plan) {
      setCoverage(plan.minCoverageAmount ?? "");
      setDuration(plan.minDuration ?? "");
      setPremiumType(plan.premiumType ?? "");
    }
  }, [plan]);

  const coverageNum = Number(coverage);
  const durationNum = Number(duration);

  const coverageErr =
    !coverage
      ? null
      : coverageNum < (plan?.minCoverageAmount ?? 0)
      ? `Min ₹${formatCurrency(plan?.minCoverageAmount)}`
      : coverageNum > (plan?.maxCoverageAmount ?? Infinity)
      ? `Max ₹${formatCurrency(plan?.maxCoverageAmount)}`
      : null;

  const durationErr =
    !duration
      ? null
      : durationNum < (plan?.minDuration ?? 0)
      ? `Min ${plan?.minDuration} months`
      : durationNum > (plan?.maxDuration ?? Infinity)
      ? `Max ${plan?.maxDuration} months`
      : null;

  const isValid =
    coverage &&
    duration &&
    premiumType &&
    !coverageErr &&
    !durationErr &&
    coverageNum > 0 &&
    durationNum > 0;

  const { preview, isCalculating } = usePremiumPreview(
    plan?.PolicyPlanId,
    isValid ? coverageNum : null,
    isValid ? durationNum : null,
    isValid ? premiumType : null
  );

  const handleConfirm = () => {
    if (!isValid) return;
    onConfirm({ coverage: coverageNum, duration: durationNum, premiumType });
  };

  if (!plan) return null;

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={`Configure: ${plan.planName}`}
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={isPurchasing}>
            Cancel
          </Button>
          <Button
            isLoading={isPurchasing}
            disabled={!isValid || isCalculating || preview == null}
            onClick={handleConfirm}
          >
            Confirm &amp; Issue Policy
          </Button>
        </>
      }
    >
      <div className="space-y-5">
        {/* Plan limits info */}
        <div className="rounded-lg border border-ink-200 dark:border-ink-700 p-4 text-sm space-y-1.5 bg-ink-50 dark:bg-ink-800/40">
          <p className="font-semibold text-ink-700 dark:text-ink-200 mb-2">Plan Limits</p>
          <div className="flex justify-between">
            <span className="text-ink-500">Coverage range</span>
            <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
              {formatCurrency(plan.minCoverageAmount)} – {formatCurrency(plan.maxCoverageAmount)}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-ink-500">Duration range</span>
            <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
              {plan.minDuration} – {plan.maxDuration} months
            </span>
          </div>
        </div>

        {/* Inputs */}
        <Input
          label="Your coverage amount (₹)"
          type="number"
          min={plan.minCoverageAmount}
          max={plan.maxCoverageAmount}
          value={coverage}
          onChange={(e) => setCoverage(e.target.value)}
          error={coverageErr}
          hint={`Choose between ${formatCurrency(plan.minCoverageAmount)} and ${formatCurrency(plan.maxCoverageAmount)}`}
          required
        />

        <Input
          label="Policy duration (months)"
          type="number"
          min={plan.minDuration}
          max={plan.maxDuration}
          value={duration}
          onChange={(e) => setDuration(e.target.value)}
          error={durationErr}
          hint={`Choose between ${plan.minDuration} and ${plan.maxDuration} months`}
          required
        />

        <Select
          label="Premium payment cycle"
          placeholder="Select a payment cycle"
          options={PREMIUM_TYPES.map((t) => ({ value: t, label: toTitleCase(t) }))}
          value={premiumType}
          onChange={(e) => setPremiumType(e.target.value)}
          required
        />

        {/* Live premium display */}
        {isCalculating && (
          <div className="flex items-center gap-2 text-sm text-ink-400">
            <Calculator className="h-4 w-4 animate-spin" />
            Calculating premium…
          </div>
        )}

        {!isCalculating && isValid && preview != null && (
          <Alert type="info">
            <span className="flex items-center gap-2">
              <Calculator className="h-4 w-4 flex-shrink-0" />
              <span>
                Your{" "}
                <strong>{toTitleCase(premiumType).toLowerCase()}</strong> premium:{" "}
                <strong className="text-lg">{formatCurrency(preview)}</strong>
              </span>
            </span>
          </Alert>
        )}

        {!isCalculating && isValid && preview == null && (
          <Alert type="warning">
            Could not calculate premium for these values. Please adjust your inputs.
          </Alert>
        )}
      </div>
    </Modal>
  );
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function PlansByProductPage() {
  const { productId } = useParams();
  const { role } = useAuth();
  const isAdmin = role === ROLES.ADMIN;
  const isCustomer = role === ROLES.CUSTOMER;
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [plans, setPlans] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [configuringPlan, setConfiguringPlan] = useState(null);
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

  const handlePurchase = async ({ coverage, duration, premiumType }) => {
    if (!configuringPlan) return;
    setIsPurchasing(true);
    try {
      const policy = await policyService.purchase(configuringPlan.PolicyPlanId, {
        coverage,
        duration,
        premiumType,
      });
      toast.success("Policy issued! Complete payment to activate it.");
      setConfiguringPlan(null);
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
        <EmptyState
          icon={ShieldCheck}
          title="No plans available"
          description="Check back soon, or add one if you manage this product."
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {plans.map((plan) => (
            <Card key={plan.PolicyPlanId} className="flex flex-col">
              <div className="flex items-start justify-between">
                <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white">
                  {plan.planName}
                </h3>
                <Stamp status={plan.active} />
              </div>

              <div className="mt-4 space-y-2 text-sm flex-1">
                <div className="flex justify-between">
                  <span className="text-ink-500">Coverage range</span>
                  <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                    {formatCurrency(plan.minCoverageAmount)} – {formatCurrency(plan.maxCoverageAmount)}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-ink-500">Duration range</span>
                  <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                    {plan.minDuration} – {plan.maxDuration} months
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-ink-500">Premium cycle</span>
                  <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                    {toTitleCase(plan.premiumType)}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-ink-500">Premium</span>
                  <span className="text-xs bg-harbor-50 dark:bg-harbor-900/30 text-harbor-700 dark:text-harbor-300 border border-harbor-200 dark:border-harbor-800/50 rounded px-2 py-0.5 font-medium">
                    Dynamic
                  </span>
                </div>
              </div>

              <p className="mt-3 text-xs text-ink-400 line-clamp-2">{plan.termsAndConditions}</p>

              <div className="mt-5 flex gap-2">
                {isCustomer && plan.active && (
                  <Button
                    className="flex-1"
                    icon={Calculator}
                    onClick={() => setConfiguringPlan(plan)}
                  >
                    Configure &amp; Buy
                  </Button>
                )}
                {role === ROLES.AGENT && (
                  <Button className="flex-1" disabled title="Agents are not authorized to purchase plans">
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

      <PurchaseModal
        plan={configuringPlan}
        open={!!configuringPlan}
        onClose={() => !isPurchasing && setConfiguringPlan(null)}
        onConfirm={handlePurchase}
        isPurchasing={isPurchasing}
      />
    </div>
  );
}
