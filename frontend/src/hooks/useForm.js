import { useCallback, useState } from "react";
import { validateForm } from "../utils/validators";
import { getErrorMessage } from "../services/api";

/**
 * A small, dependency-free form hook.
 *
 * @param {object} initialValues
 * @param {object} schema - { field: [validatorFns] } evaluated live and on submit
 * @param {(values: object) => Promise<any>} onSubmit
 */
export function useForm({ initialValues, schema = {}, onSubmit }) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const validateField = useCallback(
    (field, value) => {
      if (!schema[field]) return null;
      const fieldErrors = validateForm({ [field]: value }, { [field]: schema[field] });
      return fieldErrors[field] || null;
    },
    [schema]
  );

  const setFieldValue = useCallback(
    (field, value) => {
      setValues((prev) => ({ ...prev, [field]: value }));
      if (touched[field]) {
        setErrors((prev) => ({ ...prev, [field]: validateField(field, value) }));
      }
    },
    [touched, validateField]
  );

  const handleChange = useCallback(
    (e) => {
      const { name, value, type, checked } = e.target;
      setFieldValue(name, type === "checkbox" ? checked : value);
    },
    [setFieldValue]
  );

  const handleBlur = useCallback(
    (e) => {
      const { name, value } = e.target;
      setTouched((prev) => ({ ...prev, [name]: true }));
      setErrors((prev) => ({ ...prev, [name]: validateField(name, value) }));
    },
    [validateField]
  );

  const validateAll = useCallback(() => {
    const nextErrors = validateForm(values, schema);
    setErrors(nextErrors);
    setTouched(Object.fromEntries(Object.keys(schema).map((k) => [k, true])));
    return Object.keys(nextErrors).length === 0;
  }, [values, schema]);

  const handleSubmit = useCallback(
    async (e) => {
      if (e?.preventDefault) e.preventDefault();
      setSubmitError("");
      const isValid = validateAll();
      if (!isValid) return;
      setIsSubmitting(true);
      try {
        await onSubmit(values);
      } catch (error) {
        setSubmitError(getErrorMessage(error));
      } finally {
        setIsSubmitting(false);
      }
    },
    [validateAll, onSubmit, values]
  );

  const reset = useCallback((next = initialValues) => {
    setValues(next);
    setErrors({});
    setTouched({});
    setSubmitError("");
  }, [initialValues]);

  return {
    values,
    errors,
    touched,
    isSubmitting,
    submitError,
    setSubmitError,
    setFieldValue,
    setValues,
    handleChange,
    handleBlur,
    handleSubmit,
    validateAll,
    reset,
  };
}
