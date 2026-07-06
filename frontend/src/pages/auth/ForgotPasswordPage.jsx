import { useNavigate, Link } from "react-router-dom";
import { Mail, ArrowRight } from "lucide-react";
import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import { authService } from "../../services/authService";
import { useForm } from "../../hooks/useForm";
import { required, email } from "../../utils/validators";
import toast from "react-hot-toast";

const schema = { email: [required("Email is required"), email("Invalid email format")] };

export default function ForgotPasswordPage() {
  const navigate = useNavigate();

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { email: "" },
    schema,
    onSubmit: async (formValues) => {
      await authService.forgotPassword(formValues.email);
      toast.success("If that email exists, an OTP has been sent.");
      navigate("/reset-password", { state: { email: formValues.email } });
    },
  });

  return (
    <div className="animate-fade-in-up">
      <h1 className="font-display text-2xl font-semibold text-ink-900 dark:text-white">Forgot your password?</h1>
      <p className="mt-1.5 text-sm text-ink-500 dark:text-ink-400">
        Enter your account email and we'll send a one-time reset code.
      </p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-4" noValidate>
        {submitError && <Alert type="error">{submitError}</Alert>}

        <Input
          label="Email"
          name="email"
          type="email"
          icon={Mail}
          placeholder="you@example.com"
          value={values.email}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.email}
          required
        />

        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting} icon={ArrowRight}>
          Send reset code
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-ink-500 dark:text-ink-400">
        Remembered it after all?{" "}
        <Link to="/login" className="font-semibold text-harbor-600 dark:text-harbor-400 hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}
