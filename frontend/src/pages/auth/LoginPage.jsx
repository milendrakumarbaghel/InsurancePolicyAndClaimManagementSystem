import { Link, useLocation, useNavigate } from "react-router-dom";
import { Mail, Lock, ArrowRight } from "lucide-react";
import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import { useForm } from "../../hooks/useForm";
import { useAuth } from "../../context/AuthContext";
import { required, email } from "../../utils/validators";
import toast from "react-hot-toast";

const schema = {
  email: [required("Email is required"), email("Invalid email format")],
  password: [required("Password is required")],
};

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { email: "", password: "" },
    schema,
    onSubmit: async (formValues) => {
      const result = await login(formValues);
      if (!result.success) throw new Error(result.message);
      toast.success(`Welcome back, ${result.user.name?.split(" ")[0] || "there"}!`);
      const redirectTo = location.state?.from?.pathname || "/dashboard";
      navigate(redirectTo, { replace: true });
    },
  });

  return (
    <div className="animate-fade-in-up">
      <h1 className="font-display text-2xl font-semibold text-ink-900 dark:text-white">Welcome back</h1>
      <p className="mt-1.5 text-sm text-ink-500 dark:text-ink-400">Sign in to manage your policies and claims.</p>

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
        <div>
          <Input
            label="Password"
            name="password"
            type="password"
            icon={Lock}
            placeholder="••••••••"
            value={values.password}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.password}
            required
          />
          <div className="mt-2 text-right">
            <Link to="/forgot-password" className="text-xs font-medium text-harbor-600 dark:text-harbor-400 hover:underline">
              Forgot password?
            </Link>
          </div>
        </div>

        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting} icon={ArrowRight}>
          Sign in
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-ink-500 dark:text-ink-400">
        New to Assurly?{" "}
        <Link to="/register" className="font-semibold text-harbor-600 dark:text-harbor-400 hover:underline">
          Create an account
        </Link>
      </p>
    </div>
  );
}
