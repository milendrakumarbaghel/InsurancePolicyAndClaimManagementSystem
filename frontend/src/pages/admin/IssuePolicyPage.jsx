import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { ShieldPlus, Calculator } from "lucide-react";
import Card from "../../components/common/Card";
import PageHeader from "../../components/common/PageHeader";
import Select from "../../components/common/Select";
import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import { useForm } from "../../hooks/useForm";
import { policyService } from "../../services/policyService";
import { planService } from "../../services/planService";
import { customerService } from "../../services/customerService";
import { getErrorMessage } from "../../services/api";
import { required, futureOrPresent, positive, max } from "../../utils/validators";
import { formatCurrency, toTitleCase } from "../../utils/formatters";
import { PREMIUM_TYPES } from "../../utils/constants";

const schema = {
  customerId: [required("Please select a customer")],
  planId: [required("Please select a plan")],
  startDate: [required("Start date is required"), futureOrPresent("Start date cannot be in the past")],
  selectedCoverageAmount: [required("Coverage amount is required"), positive()],
  selectedDuration: [required("Duration is required"), positive(), max(120, "Cannot exceed 120 months")],
  selectedPremiumType: [required("Premium cycle is required")],
};

export default function IssuePolicyPage() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [plans, setPlans] = useState([]);
  const [isLoadingOptions, setIsLoadingOptions] = useState(true);
  const [premiumPreview, setPremiumPreview] = useState(null);
  const [isCalculating, setIsCalculating] = useState(false);
  const debounceRef = useRef(null);

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } =
    useForm({
      initialValues: {
        customerId: "",
        planId: "",
        startDate: "",
        selectedCoverageAmount: "",
        selectedDuration: "",
        selectedPremiumType: "",
      },
      schema,
      onSubmit: async (formValues) => {
        const policy = await policyService.issue({
          customerId: Number(formValues.customerId),
          planId: Number(formValues.planId),
          startDate: formValues.startDate,
          selectedCoverageAmount: Number(formValues.selectedCoverageAmount),
          selectedDuration: Number(formValues.selectedDuration),
          selectedPremiumType: formValues.selectedPremiumType,
        });
        toast.success(`Policy ${policy.policyNumber} issued.`);
        navigate(`/dashboard/policies/${policy.policyId}`);
      },
    });

  useEffect(() => {
    Promise.all([
      customerService.getAll({ page: 0, size: 200 }),
      planService.getAll({ page: 0, size: 200 }),
    ])
      .then(([customerPage, planPage]) => {
        setCustomers(customerPage?.content ?? []);
        setPlans((planPage?.content ?? []).filter((p) => p.active));
      })
      .catch((err) => toast.error(getErrorMessage(err, "Could not load form options.")))
      .finally(() => setIsLoadingOptions(false));
  }, []);

  const selectedPlan = plans.find((p) => String(p.PolicyPlanId) === String(values.planId));

  // Live premium preview (debounced)
  const recalculate = useCallback(() => {
    const cov = Number(values.selectedCoverageAmount);
    const dur = Number(values.selectedDuration);
    const pType = values.selectedPremiumType;
    if (!selectedPlan || !cov || !dur || !pType || cov <= 0 || dur <= 0) {
      setPremiumPreview(null);
      return;
    }
    setIsCalculating(true);
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      planService
        .calculatePremium(selectedPlan.PolicyPlanId, {
          coverage: cov,
          duration: dur,
          premiumType: pType,
        })
        .then((val) => setPremiumPreview(val))
        .catch(() => setPremiumPreview(null))
        .finally(() => setIsCalculating(false));
    }, 400);
  }, [selectedPlan, values.selectedCoverageAmount, values.selectedDuration, values.selectedPremiumType]);

  useEffect(() => {
    recalculate();
    return () => clearTimeout(debounceRef.current);
  }, [recalculate]);

  // Reset coverage/duration when plan changes
  useEffect(() => {
    setPremiumPreview(null);
  }, [values.planId]);

  return (
    <div className="max-w-2xl">
      <PageHeader
        eyebrow="Coverage"
        title="Issue Policy"
        description="Manually issue a policy on behalf of a customer — useful for offline or assisted sign-ups. The premium is calculated dynamically based on the chosen coverage and duration."
      />

      <Card>
        <form onSubmit={handleSubmit} className="space-y-5" noValidate>
          {submitError && <Alert type="error">{submitError}</Alert>}

          <Select
            label="Customer"
            name="customerId"
            placeholder={isLoadingOptions ? "Loading customers…" : "Select a customer"}
            options={customers.map((c) => ({
              value: c.customerId,
              label: `${[c.firstName, c.middleName, c.lastName].filter(Boolean).join(" ")} — ${c.email}`,
            }))}
            value={values.customerId}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.customerId}
            disabled={isLoadingOptions}
            required
          />

          <Select
            label="Plan"
            name="planId"
            placeholder={isLoadingOptions ? "Loading plans…" : "Select a plan"}
            options={plans.map((p) => ({
              value: p.PolicyPlanId,
              label: `${p.planName} (${p.productName})`,
            }))}
            value={values.planId}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.planId}
            disabled={isLoadingOptions}
            required
          />

          {selectedPlan && (
            <div className="rounded-lg border border-ink-200 dark:border-ink-700 p-4 text-sm space-y-1.5 bg-ink-50 dark:bg-ink-800/40">
              <p className="font-semibold text-ink-700 dark:text-ink-200 mb-2">Plan Limits</p>
              <div className="flex justify-between">
                <span className="text-ink-500">Coverage range</span>
                <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                  {formatCurrency(selectedPlan.minCoverageAmount)} –{" "}
                  {formatCurrency(selectedPlan.maxCoverageAmount)}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-ink-500">Duration range</span>
                <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                  {selectedPlan.minDuration} – {selectedPlan.maxDuration} months
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-ink-500">Premium cycle</span>
                <span className="font-mono-data font-semibold text-ink-800 dark:text-ink-100">
                  {toTitleCase(selectedPlan.premiumType)}
                </span>
              </div>
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Coverage amount (₹)"
              name="selectedCoverageAmount"
              type="number"
              min={selectedPlan?.minCoverageAmount ?? 1}
              max={selectedPlan?.maxCoverageAmount}
              value={values.selectedCoverageAmount}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.selectedCoverageAmount}
              disabled={!selectedPlan}
              hint={
                selectedPlan
                  ? `${formatCurrency(selectedPlan.minCoverageAmount)} – ${formatCurrency(selectedPlan.maxCoverageAmount)}`
                  : "Select a plan first"
              }
              required
            />
            <Input
              label="Duration (months)"
              name="selectedDuration"
              type="number"
              min={selectedPlan?.minDuration ?? 1}
              max={selectedPlan?.maxDuration ?? 120}
              value={values.selectedDuration}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.selectedDuration}
              disabled={!selectedPlan}
              hint={
                selectedPlan
                  ? `${selectedPlan.minDuration} – ${selectedPlan.maxDuration} months`
                  : "Select a plan first"
              }
              required
            />
          </div>

          <Select
            label="Premium payment cycle"
            name="selectedPremiumType"
            placeholder="Select a payment cycle"
            options={PREMIUM_TYPES.map((t) => ({ value: t, label: toTitleCase(t) }))}
            value={values.selectedPremiumType}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.selectedPremiumType}
            disabled={!selectedPlan}
            required
          />

          {/* Live premium preview */}
          {isCalculating && (
            <div className="flex items-center gap-2 text-sm text-ink-400">
              <Calculator className="h-4 w-4 animate-spin" />
              Calculating premium…
            </div>
          )}
          {!isCalculating && premiumPreview != null && (
            <Alert type="info">
              <span className="flex items-center gap-2">
                <Calculator className="h-4 w-4 flex-shrink-0" />
                <span>
                  Calculated{" "}
                  <strong>{toTitleCase(values.selectedPremiumType).toLowerCase()}</strong> premium:{" "}
                  <strong className="text-lg">{formatCurrency(premiumPreview)}</strong>
                </span>
              </span>
            </Alert>
          )}

          <Input
            label="Start date"
            name="startDate"
            type="date"
            value={values.startDate}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.startDate}
            required
          />

          <div className="flex gap-3 pt-2">
            <Button type="submit" isLoading={isSubmitting} icon={ShieldPlus}>
              Issue policy
            </Button>
            <Button type="button" variant="outline" onClick={() => navigate("/dashboard/policies")}>
              Cancel
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
