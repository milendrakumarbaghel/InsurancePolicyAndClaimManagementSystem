import { useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import { Lock, KeyRound } from "lucide-react";
import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import { authService } from "../../services/authService";
import { useForm } from "../../hooks/useForm";
import { patterns, required, minLength, maxLength, pattern } from "../../utils/validators";
import toast from "react-hot-toast";

export default function ResetPasswordPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [email, setEmail] = useState(location.state?.email || "");

  const schema = {
    otp: [required("OTP is required"), pattern(patterns.otp6, "OTP must be a 6-digit number")],
    newPassword: [
      required("New password is required"),
      minLength(8, "Password must be between 8 and 20 characters"),
      maxLength(20, "Password must be between 8 and 20 characters"),
      pattern(
        patterns.password,
        "Must include an uppercase letter, lowercase letter, digit, and special character (@#$%^&+=!)"
      ),
    ],
    confirmPassword: [required("Please confirm your new password")],
  };

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { otp: "", newPassword: "", confirmPassword: "" },
    schema,
    onSubmit: async (formValues) => {
      if (!email) throw new Error("Enter the email tied to your account.");
      if (formValues.newPassword !== formValues.confirmPassword) {
        throw new Error("Passwords do not match.");
      }
      await authService.resetPassword({ email, ...formValues });
      toast.success("Password reset — sign in with your new password.");
      navigate("/login");
    },
  });

  return (
    <div className="animate-fade-in-up">
      <div className="mb-2 flex h-11 w-11 items-center justify-center rounded-xl bg-harbor-50 dark:bg-ink-800 text-harbor-600">
        <KeyRound className="h-5 w-5" />
      </div>
      <h1 className="font-display text-2xl font-semibold text-ink-900 dark:text-white">Reset your password</h1>
      <p className="mt-1.5 text-sm text-ink-500 dark:text-ink-400">
        Enter the OTP we sent you along with your new password.
      </p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-4" noValidate>
        {submitError && <Alert type="error">{submitError}</Alert>}

        <Input label="Email" name="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <Input
          label="OTP"
          name="otp"
          inputMode="numeric"
          maxLength={6}
          placeholder="6-digit code"
          value={values.otp}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.otp}
          required
        />
        <Input
          label="New password"
          name="newPassword"
          type="password"
          icon={Lock}
          value={values.newPassword}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.newPassword}
          hint="One uppercase, one lowercase, one digit, one special character (@#$%^&+=!)"
          required
        />
        <Input
          label="Confirm new password"
          name="confirmPassword"
          type="password"
          icon={Lock}
          value={values.confirmPassword}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.confirmPassword}
          required
        />

        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting}>
          Reset password
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-ink-500 dark:text-ink-400">
        Back to{" "}
        <Link to="/login" className="font-semibold text-harbor-600 dark:text-harbor-400 hover:underline">
          sign in
        </Link>
      </p>
    </div>
  );
}
