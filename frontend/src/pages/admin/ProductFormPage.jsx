import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
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
import { productService } from "../../services/productService";
import { getErrorMessage } from "../../services/api";
import { patterns, required, minLength, maxLength, pattern } from "../../utils/validators";
import { PRODUCT_TYPES } from "../../utils/constants";
import { toTitleCase } from "../../utils/formatters";

const schema = {
  productName: [
    required("Product name is required"),
    minLength(2, "Must be between 2 and 100 characters"),
    maxLength(100, "Must be between 2 and 100 characters"),
    pattern(patterns.planName, "Contains invalid special characters"),
  ],
  productType: [required("Product type is required")],
  description: [
    required("Description is required"),
    minLength(10, "Must be between 10 and 1000 characters"),
    maxLength(1000, "Must be between 10 and 1000 characters"),
    pattern(patterns.noAngleBrackets, "Cannot contain < or >"),
  ],
};

export default function ProductFormPage() {
  const { productId } = useParams();
  const isEdit = !!productId;
  const navigate = useNavigate();
  const [isLoadingProduct, setIsLoadingProduct] = useState(isEdit);

  const { values, errors, setValues, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { productName: "", productType: "", description: "", active: true },
    schema,
    onSubmit: async (formValues) => {
      const payload = { ...formValues, active: !!formValues.active };
      if (isEdit) {
        await productService.update(productId, payload);
        toast.success("Product updated.");
      } else {
        await productService.create(payload);
        toast.success("Product created.");
      }
      navigate("/dashboard/products");
    },
  });

  useEffect(() => {
    if (!isEdit) return;
    productService
      .getById(productId)
      .then((data) =>
        setValues({
          productName: data.productName,
          productType: data.productType,
          description: data.description,
          active: data.active,
        })
      )
      .catch((err) => toast.error(getErrorMessage(err, "Could not load product.")))
      .finally(() => setIsLoadingProduct(false));
  }, [productId, isEdit, setValues]);

  if (isLoadingProduct) return <Spinner label="Loading product…" />;

  return (
    <div className="max-w-2xl">
      <PageHeader
        eyebrow="Catalog"
        title={isEdit ? "Edit Product" : "New Product"}
        description="Products group related plans under a coverage category (Health, Motor, Life, Travel)."
      />

      <Card>
        <form onSubmit={handleSubmit} className="space-y-5" noValidate>
          {submitError && <Alert type="error">{submitError}</Alert>}

          <Input
            label="Product name"
            name="productName"
            placeholder="Health Shield Plus"
            value={values.productName}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.productName}
            required
          />

          <Select
            label="Product type"
            name="productType"
            placeholder="Select a type"
            options={PRODUCT_TYPES.map((t) => ({ value: t, label: toTitleCase(t) }))}
            value={values.productType}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.productType}
            required
          />

          <Textarea
            label="Description"
            name="description"
            rows={5}
            placeholder="Describe what this product covers…"
            value={values.description}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.description}
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
            Active (visible to customers immediately)
          </label>

          <div className="flex gap-3 pt-2">
            <Button type="submit" isLoading={isSubmitting} icon={Save}>
              {isEdit ? "Save changes" : "Create product"}
            </Button>
            <Button type="button" variant="outline" onClick={() => navigate("/dashboard/products")}>
              Cancel
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
