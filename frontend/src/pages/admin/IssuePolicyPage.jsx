import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { ShieldPlus } from "lucide-react";
import Card from "../../components/common/Card";
import PageHeader from "../../components/common/PageHeader";
import Select from "../../components/common/Select";
import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import { useForm } from "../../hooks/useForm";
import { policyService } from "../../services/policyService";
import { customerService } from "../../services/customerService";
import { planService } from "../../services/planService";
import { getErrorMessage } from "../../services/api";
import { required, futureOrPresent } from "../../utils/validators";
import { formatCurrency } from "../../utils/formatters";

const schema = {
  customerId: [required("Please select a customer")],
  planId: [required("Please select a plan")],
  startDate: [required("Start date is required"), futureOrPresent("Start date cannot be in the past")],
};

export default function IssuePolicyPage() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [plans, setPlans] = useState([]);
  const [isLoadingOptions, setIsLoadingOptions] = useState(true);

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { customerId: "", planId: "", startDate: "" },
    schema,
    onSubmit: async (formValues) => {
      const policy = await policyService.issue({
        customerId: Number(formValues.customerId),
        planId: Number(formValues.planId),
        startDate: formValues.startDate,
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

  return (
    <div className="max-w-2xl">
      <PageHeader
        eyebrow="Coverage"
        title="Issue Policy"
        description="Manually issue a policy on behalf of a customer — useful for offline or assisted sign-ups."
      />

      <Card>
        <form onSubmit={handleSubmit} className="space-y-5" noValidate>
          {submitError && <Alert type="error">{submitError}</Alert>}

          <Select
            label="Customer"
            name="customerId"
            placeholder={isLoadingOptions ? "Loading customers…" : "Select a customer"}
            options={customers.map((c) => ({ value: c.customerId, label: `${[c.firstName, c.middleName, c.lastName].filter(Boolean).join(" ")} — ${c.email}` }))}
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
            options={plans.map((p) => ({ value: p.PolicyPlanId, label: `${p.planName} (${p.productName})` }))}
            value={values.planId}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.planId}
            disabled={isLoadingOptions}
            required
          />

          {selectedPlan && (
            <Alert type="info">
              Coverage {formatCurrency(selectedPlan.coverageAmount)} · Premium{" "}
              {formatCurrency(selectedPlan.premiumAmount)} per cycle · {selectedPlan.duration} months
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
