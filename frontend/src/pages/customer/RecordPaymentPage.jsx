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
import { paymentService } from "../../services/paymentService";
import { getErrorMessage } from "../../services/api";
import { required } from "../../utils/validators";
import { PAYMENT_MODES } from "../../utils/constants";
import { formatCurrency, toTitleCase } from "../../utils/formatters";
import { hasPaymentInCurrentPeriod } from "../../utils/installmentUtils";

const schema = {
  policyId: [required("Please select a policy")],
  paymentMode: [required("Please select a payment method")],
};

export default function RecordPaymentPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preselectedPolicyId = searchParams.get("policyId") || "";

  const [policies, setPolicies] = useState([]);
  const [isLoadingOptions, setIsLoadingOptions] = useState(true);
  const [installmentPaid, setInstallmentPaid] = useState(false);
  const [isCheckingPayments, setIsCheckingPayments] = useState(false);

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { policyId: preselectedPolicyId, paymentMode: "" },
    schema,
    onSubmit: async (formValues) => {
      const policy = policies.find((p) => String(p.policyId) === String(formValues.policyId));
      if (!policy) {
        throw new Error("Could not find the selected policy. Please refresh and try again.");
      }
      if (!policy.calculatedPremiumAmount) {
        throw new Error(
          "This policy does not have a calculated premium. Please contact support."
        );
      }
      await paymentService.record({
        amount: policy.calculatedPremiumAmount,
        policyNumber: policy.policyNumber,
        paymentMode: formValues.paymentMode,
      });
      toast.success("Payment recorded successfully.");
      navigate(`/dashboard/policies/${policy.policyId}`);
    },
  });

  // Load only payable policies (no need to fetch all plans anymore)
  useEffect(() => {
    policyService
      .getMy()
      .then((myPolicies) => {
        const payable = myPolicies.filter(
          (p) => p.status === "PENDING_PAYMENT" || p.status === "ACTIVE"
        );
        setPolicies(payable);
      })
      .catch((err) => toast.error(getErrorMessage(err, "Could not load your policies.")))
      .finally(() => setIsLoadingOptions(false));
  }, []);

  // Check whether the current installment has already been paid
  useEffect(() => {
    if (!values.policyId || isLoadingOptions) {
      setInstallmentPaid(false);
      return;
    }
    const policy = policies.find((p) => String(p.policyId) === String(values.policyId));
    if (!policy) {
      setInstallmentPaid(false);
      return;
    }
    setIsCheckingPayments(true);
    paymentService
      .getByPolicy(policy.policyId)
      .then((payments) => {
        setInstallmentPaid(hasPaymentInCurrentPeriod(payments, policy.planPremiumType));
      })
      .catch(() => setInstallmentPaid(false))
      .finally(() => setIsCheckingPayments(false));
  }, [values.policyId, policies, isLoadingOptions]);

  const selectedPolicy = policies.find((p) => String(p.policyId) === String(values.policyId));

  return (
    <div className="max-w-2xl">
      <Link
        to="/dashboard/policies"
        className="mb-4 flex items-center gap-1.5 text-sm font-medium text-ink-500 hover:text-harbor-600 dark:hover:text-harbor-400 w-fit"
      >
        <ArrowLeft className="h-4 w-4" /> Back to policies
      </Link>

      <PageHeader
        eyebrow="Payments"
        title="Pay a Premium"
        description="Settle a premium payment against one of your policies."
      />

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
              options={policies.map((p) => ({
                value: p.policyId,
                label: `${p.policyNumber} — ${p.planName}`,
              }))}
              value={values.policyId}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.policyId}
              required
            />

            {installmentPaid && (
              <Alert type="warning">
                A premium payment for the current{" "}
                {toTitleCase(selectedPolicy?.planPremiumType)} installment has already been
                recorded for this policy. You cannot pay again until the next billing cycle.
              </Alert>
            )}

            {selectedPolicy && !installmentPaid && (
              <>
                {/* Coverage & duration summary */}
                <div className="rounded-lg border border-ink-200 dark:border-ink-700 p-4 text-sm space-y-1.5 bg-ink-50 dark:bg-ink-800/40">
                  <p className="font-semibold text-ink-700 dark:text-ink-200 mb-2">
                    Your Policy Details
                  </p>
                  <div className="flex justify-between">
                    <span className="text-ink-500">Coverage amount</span>
                    <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                      {formatCurrency(selectedPolicy.selectedCoverageAmount)}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-ink-500">Duration</span>
                    <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                      {selectedPolicy.selectedDuration} months
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-ink-500">Premium cycle</span>
                    <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                      {toTitleCase(selectedPolicy.planPremiumType)}
                    </span>
                  </div>
                </div>

                <Alert type="info">
                  Premium due:{" "}
                  <strong>{formatCurrency(selectedPolicy.calculatedPremiumAmount)}</strong> (
                  {toTitleCase(selectedPolicy.planPremiumType)})
                </Alert>
              </>
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
              value={
                selectedPolicy?.calculatedPremiumAmount
                  ? formatCurrency(selectedPolicy.calculatedPremiumAmount)
                  : ""
              }
              disabled
              hint="Amount is calculated based on your selected coverage and duration."
            />

            <div className="flex gap-3 pt-2">
              <Button
                type="submit"
                isLoading={isSubmitting || isCheckingPayments}
                icon={Wallet}
                disabled={!selectedPolicy || installmentPaid}
              >
                Pay now
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate("/dashboard/policies")}
              >
                Cancel
              </Button>
            </div>
          </form>
        )}
      </Card>
    </div>
  );
}
