import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import toast from "react-hot-toast";
import { Save } from "lucide-react";
import Card from "../../components/common/Card";
import PageHeader from "../../components/common/PageHeader";
import Input from "../../components/common/Input";
import Select from "../../components/common/Select";
import Textarea from "../../components/common/Textarea";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useForm } from "../../hooks/useForm";
import { planService } from "../../services/planService";
import { productService } from "../../services/productService";
import { getErrorMessage } from "../../services/api";
import {
  patterns, required, minLength, maxLength, pattern, positive, max, min,
} from "../../utils/validators";
import { PREMIUM_TYPES } from "../../utils/constants";
import { toTitleCase } from "../../utils/formatters";

const schema = {
  productId: [required("Product is required")],
  planName: [
    required("Plan name is required"),
    minLength(3, "Must be between 3 and 100 characters"),
    maxLength(100, "Must be between 3 and 100 characters"),
    pattern(patterns.planName, "Contains invalid special characters"),
  ],
  maxCoverageAmount: [
    required("Maximum coverage amount is required"),
    positive(),
    max(999999999, "Exceeds maximum allowable limit"),
  ],
  minCoverageAmount: [
    required("Minimum coverage amount is required"),
    positive(),
    max(999999999, "Exceeds maximum allowable limit"),
  ],
  premiumType: [required("Premium cycle is required")],
  maxDuration: [
    required("Maximum duration is required"),
    positive(),
    max(120, "Cannot exceed 120 months"),
  ],
  minDuration: [
    required("Minimum duration is required"),
    positive(),
    max(120, "Cannot exceed 120 months"),
  ],
  termsAndConditions: [
    required("Terms and conditions are required"),
    minLength(10, "Must be between 10 and 2000 characters"),
    maxLength(2000, "Must be between 10 and 2000 characters"),
    pattern(patterns.noAngleBrackets, "Cannot contain < or >"),
  ],
};

export default function PlanFormPage() {
  const { planId } = useParams();
  const [searchParams] = useSearchParams();
  const isEdit = !!planId;
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [isLoadingPlan, setIsLoadingPlan] = useState(isEdit);

  const { values, errors, setValues, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: {
      productId: searchParams.get("productId") || "",
      planName: "",
      maxCoverageAmount: "",
      minCoverageAmount: "",
      premiumType: "",
      maxDuration: "",
      minDuration: "",
      termsAndConditions: "",
      active: true,
    },
    schema,
    onSubmit: async (formValues) => {
      // Cross-field validation
      if (Number(formValues.minCoverageAmount) >= Number(formValues.maxCoverageAmount)) {
        throw new Error("Minimum coverage must be less than maximum coverage.");
      }
      if (Number(formValues.minDuration) >= Number(formValues.maxDuration)) {
        throw new Error("Minimum duration must be less than maximum duration.");
      }

      const payload = {
        ...formValues,
        productId: Number(formValues.productId),
        maxCoverageAmount: Number(formValues.maxCoverageAmount),
        minCoverageAmount: Number(formValues.minCoverageAmount),
        maxDuration: Number(formValues.maxDuration),
        minDuration: Number(formValues.minDuration),
        active: !!formValues.active,
      };
      if (isEdit) {
        await planService.update(planId, payload);
        toast.success("Plan updated.");
      } else {
        await planService.create(payload);
        toast.success("Plan created.");
      }
      navigate("/dashboard/plans");
    },
  });

  useEffect(() => {
    productService.getAll({ page: 0, size: 200 }).then((data) => setProducts(data?.content ?? []));
  }, []);

  useEffect(() => {
    if (!isEdit) return;
    planService
      .getById(planId)
      .then((data) =>
        setValues((prev) => ({
          ...prev,
          planName: data.planName,
          maxCoverageAmount: data.maxCoverageAmount,
          minCoverageAmount: data.minCoverageAmount,
          premiumType: data.premiumType,
          maxDuration: data.maxDuration,
          minDuration: data.minDuration,
          active: data.active,
        }))
      )
      .catch((err) => toast.error(getErrorMessage(err, "Could not load plan.")))
      .finally(() => setIsLoadingPlan(false));
  }, [planId, isEdit, setValues]);

  if (isLoadingPlan) return <Spinner label="Loading plan…" />;

  return (
    <div className="max-w-2xl">
      <PageHeader
        eyebrow="Catalog"
        title={isEdit ? "Edit Plan" : "New Plan"}
        description="Plans define the coverage range and duration range. Customers choose their exact values; the premium is calculated dynamically at purchase."
      />

      <Card>
        <form onSubmit={handleSubmit} className="space-y-5" noValidate>
          {submitError && <Alert type="error">{submitError}</Alert>}

          <Select
            label="Product"
            name="productId"
            placeholder="Select a product"
            options={products.map((p) => ({ value: p.productId, label: p.productName }))}
            value={values.productId}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.productId}
            disabled={isEdit}
            required
          />

          <Input
            label="Plan name"
            name="planName"
            placeholder="Health Shield Plus — Silver"
            value={values.planName}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.planName}
            required
          />

          {/* Coverage Range */}
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-ink-400">
              Coverage Amount Range (₹)
            </p>
            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Minimum coverage (₹)"
                name="minCoverageAmount"
                type="number"
                min="1"
                value={values.minCoverageAmount}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.minCoverageAmount}
                required
              />
              <Input
                label="Maximum coverage (₹)"
                name="maxCoverageAmount"
                type="number"
                min="1"
                value={values.maxCoverageAmount}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.maxCoverageAmount}
                required
              />
            </div>
          </div>

          {/* Premium Cycle + Duration Range */}
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-ink-400">
              Policy Duration Range (Months)
            </p>
            <div className="grid grid-cols-3 gap-4">
              <Select
                label="Premium cycle"
                name="premiumType"
                placeholder="Select cycle"
                options={PREMIUM_TYPES.map((t) => ({ value: t, label: toTitleCase(t) }))}
                value={values.premiumType}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.premiumType}
                required
              />
              <Input
                label="Min duration (months)"
                name="minDuration"
                type="number"
                min="1"
                max="120"
                value={values.minDuration}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.minDuration}
                required
              />
              <Input
                label="Max duration (months)"
                name="maxDuration"
                type="number"
                min="1"
                max="120"
                value={values.maxDuration}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.maxDuration}
                required
              />
            </div>
          </div>

          <Textarea
            label="Terms and conditions"
            name="termsAndConditions"
            rows={5}
            placeholder="Outline exclusions, waiting periods, and claim conditions…"
            value={values.termsAndConditions}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.termsAndConditions}
            required
          />

          <label className="flex items-center gap-2.5 text-sm font-medium text-ink-700 dark:text-ink-200">
            <input
              type="checkbox"
              name="active"
              checked={!!values.active}
              onChange={handleChange}
              className="h-4 w-4 rounded border-ink-300 text-harbor-600 focus:ring-harbor-500"
            />
            Active (purchasable by customers immediately)
          </label>

          <div className="flex gap-3 pt-2">
            <Button type="submit" isLoading={isSubmitting} icon={Save}>
              {isEdit ? "Save changes" : "Create plan"}
            </Button>
            <Button type="button" variant="outline" onClick={() => navigate("/dashboard/plans")}>
              Cancel
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
