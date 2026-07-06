import { Link, useNavigate } from "react-router-dom";
import { Mail, User, Phone, Lock, ArrowRight } from "lucide-react";
import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import { useForm } from "../../hooks/useForm";
import { useAuth } from "../../context/AuthContext";
import { patterns, required, minLength, maxLength, pattern, email } from "../../utils/validators";
import toast from "react-hot-toast";

const schema = {
  fullName: [
    required("Full name is required"),
    minLength(3, "Full name must be between 3 and 100 characters"),
    maxLength(100, "Full name must be between 3 and 100 characters"),
    pattern(patterns.fullName, "Full name must contain only letters and spaces"),
  ],
  email: [required("Email is required"), email("Invalid email format")],
  mobileNumber: [
    required("Mobile number is required"),
    pattern(patterns.mobileNumber, "Must be a 10-digit number starting with 6, 7, 8, or 9"),
  ],
  password: [
    required("Password is required"),
    minLength(8, "Password must be between 8 and 20 characters"),
    maxLength(20, "Password must be between 8 and 20 characters"),
    pattern(
      patterns.password,
      "Must include an uppercase letter, lowercase letter, digit, and special character (@#$%^&+=!)"
    ),
  ],
};

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { fullName: "", email: "", mobileNumber: "", password: "" },
    schema,
    onSubmit: async (formValues) => {
      const result = await register(formValues);
      if (!result.success) throw new Error(result.message);
      toast.success("Account created — check your email and phone for an OTP.");
      navigate("/verify-otp", { state: { email: formValues.email } });
    },
  });

  return (
    <div className="animate-fade-in-up">
      <h1 className="font-display text-2xl font-semibold text-ink-900 dark:text-white">Create your account</h1>
      <p className="mt-1.5 text-sm text-ink-500 dark:text-ink-400">
        Start as a customer — purchase plans, track policies, and raise claims.
      </p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-4" noValidate>
        {submitError && <Alert type="error">{submitError}</Alert>}

        <Input
          label="Full name"
          name="fullName"
          icon={User}
          placeholder="Aditi Sharma"
          value={values.fullName}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.fullName}
          required
        />
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
        <Input
          label="Mobile number"
          name="mobileNumber"
          icon={Phone}
          placeholder="9876543210"
          value={values.mobileNumber}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.mobileNumber}
          required
        />
        <Input
          label="Password"
          name="password"
          type="password"
          icon={Lock}
          placeholder="At least 8 characters"
          value={values.password}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.password}
          hint="One uppercase, one lowercase, one digit, one special character (@#$%^&+=!)"
          required
        />

        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting} icon={ArrowRight}>
          Create account
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-ink-500 dark:text-ink-400">
        Already have an account?{" "}
        <Link to="/login" className="font-semibold text-harbor-600 dark:text-harbor-400 hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}
