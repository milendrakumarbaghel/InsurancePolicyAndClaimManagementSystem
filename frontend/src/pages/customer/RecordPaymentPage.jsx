import { useEffect, useState } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import toast from "react-hot-toast";
import { Wallet, ArrowLeft } from "lucide-react";
import Card from "../../components/common/Card";
import PageHeader from "../../components/common/PageHeader";
import Select from "../../components/common/Select";
import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useForm } from "../../hooks/useForm";
import { policyService } from "../../services/policyService";
import { planService } from "../../services/planService";
import { paymentService } from "../../services/paymentService";
import { getErrorMessage } from "../../services/api";
import { required } from "../../utils/validators";
import { PAYMENT_MODES } from "../../utils/constants";
import { formatCurrency, toTitleCase } from "../../utils/formatters";

const schema = {
  policyId: [required("Please select a policy")],
  paymentMode: [required("Please select a payment method")],
};

export default function RecordPaymentPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preselectedPolicyId = searchParams.get("policyId") || "";

  const [policies, setPolicies] = useState([]);
  const [plansById, setPlansById] = useState({});
  const [isLoadingOptions, setIsLoadingOptions] = useState(true);
  const [matchError, setMatchError] = useState("");

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { policyId: preselectedPolicyId, paymentMode: "" },
    schema,
    onSubmit: async (formValues) => {
      const policy = policies.find((p) => String(p.policyId) === String(formValues.policyId));
      const plan = plansById[formValues.policyId];
      if (!policy || !plan) {
        throw new Error("Could not determine the premium amount for this policy. Please contact support.");
      }
      await paymentService.record({
        policyPlanId: plan.PolicyPlanId,
        amount: plan.premiumAmount,
        policyNumber: policy.policyNumber,
        paymentMode: formValues.paymentMode,
      });
      toast.success("Payment recorded successfully.");
      navigate(`/dashboard/policies/${policy.policyId}`);
    },
  });

  useEffect(() => {
    Promise.all([policyService.getMy(), planService.getAll({ page: 0, size: 300 })])
      .then(([myPolicies, planPage]) => {
        const payable = myPolicies.filter((p) => p.status === "PENDING_PAYMENT" || p.status === "ACTIVE");
        setPolicies(payable);

        // The policy response doesn't expose its plan id directly, so we correlate
        // by plan name (unique per product in practice) to recover the premium amount.
        const allPlans = planPage?.content ?? [];
        const map = {};
        payable.forEach((policy) => {
          const match = allPlans.find((plan) => plan.planName === policy.planName);
          if (match) map[policy.policyId] = match;
        });
        setPlansById(map);
      })
      .catch((err) => toast.error(getErrorMessage(err, "Could not load your policies.")))
      .finally(() => setIsLoadingOptions(false));
  }, []);

  useEffect(() => {
    if (!values.policyId || isLoadingOptions) return;
    setMatchError(plansById[values.policyId] ? "" : "We couldn't automatically match this policy's plan. Please contact support to complete this payment.");
  }, [values.policyId, plansById, isLoadingOptions]);

  const selectedPolicy = policies.find((p) => String(p.policyId) === String(values.policyId));
  const selectedPlan = plansById[values.policyId];

  return (
    <div className="max-w-2xl">
      <Link to="/dashboard/policies" className="mb-4 flex items-center gap-1.5 text-sm font-medium text-ink-500 hover:text-harbor-600 dark:hover:text-harbor-400 w-fit">
        <ArrowLeft className="h-4 w-4" /> Back to policies
      </Link>

      <PageHeader eyebrow="Payments" title="Pay a Premium" description="Settle a premium payment against one of your policies." />

      <Card>
        {isLoadingOptions ? (
          <Spinner label="Loading your policies…" />
        ) : (
          <form onSubmit={handleSubmit} className="space-y-5" noValidate>
            {submitError && <Alert type="error">{submitError}</Alert>}

            <Select
              label="Policy"
              name="policyId"
              placeholder="Select a policy"
              options={policies.map((p) => ({ value: p.policyId, label: `${p.policyNumber} — ${p.planName}` }))}
              value={values.policyId}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.policyId}
              required
            />

            {matchError && <Alert type="warning">{matchError}</Alert>}

            {selectedPlan && (
              <Alert type="info">
                Premium due: <strong>{formatCurrency(selectedPlan.premiumAmount)}</strong> ({toTitleCase(selectedPlan.premiumType)})
              </Alert>
            )}

            <Select
              label="Payment method"
              name="paymentMode"
              placeholder="Select a method"
              options={PAYMENT_MODES.map((m) => ({ value: m, label: toTitleCase(m) }))}
              value={values.paymentMode}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.paymentMode}
              required
            />

            <Input
              label="Amount"
              value={selectedPlan ? formatCurrency(selectedPlan.premiumAmount) : ""}
              disabled
              hint="Amount is fixed to the plan's premium and cannot be edited."
            />

            <div className="flex gap-3 pt-2">
              <Button type="submit" isLoading={isSubmitting} icon={Wallet} disabled={!selectedPolicy || !!matchError}>
                Pay now
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate("/dashboard/policies")}>
                Cancel
              </Button>
            </div>
          </form>
        )}
      </Card>
    </div>
  );
}
