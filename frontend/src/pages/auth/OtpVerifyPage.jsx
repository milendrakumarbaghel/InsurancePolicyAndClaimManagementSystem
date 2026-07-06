import { useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import { ShieldCheck, RefreshCw } from "lucide-react";
import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import { authService } from "../../services/authService";
import { getErrorMessage } from "../../services/api";
import { useForm } from "../../hooks/useForm";
import { required, pattern, patterns } from "../../utils/validators";
import toast from "react-hot-toast";

const schema = {
  emailOtp: [required("Email OTP is required"), pattern(patterns.otp6, "OTP must be a 6-digit number")],
  phoneOtp: [required("Phone OTP is required"), pattern(patterns.otp6, "OTP must be a 6-digit number")],
};

export default function OtpVerifyPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const emailFromState = location.state?.email || "";
  const [email, setEmail] = useState(emailFromState);
  const [isResending, setIsResending] = useState(false);

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: { emailOtp: "", phoneOtp: "" },
    schema,
    onSubmit: async (formValues) => {
      if (!email) throw new Error("Enter the email you registered with.");
      await authService.verifyOtp({ email, ...formValues });
      toast.success("Account verified — you can sign in now.");
      navigate("/login");
    },
  });

  const handleResend = async () => {
    if (!email) {
      toast.error("Enter your email first.");
      return;
    }
    setIsResending(true);
    try {
      await authService.resendOtp(email);
      toast.success("A new OTP has been sent.");
    } catch (error) {
      toast.error(getErrorMessage(error, "Could not resend OTP."));
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="animate-fade-in-up">
      <div className="mb-2 flex h-11 w-11 items-center justify-center rounded-xl bg-harbor-50 dark:bg-ink-800 text-harbor-600">
        <ShieldCheck className="h-5 w-5" />
      </div>
      <h1 className="font-display text-2xl font-semibold text-ink-900 dark:text-white">Verify your account</h1>
      <p className="mt-1.5 text-sm text-ink-500 dark:text-ink-400">
        We sent a one-time code to your email and mobile number. Enter both below to activate your account.
      </p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-4" noValidate>
        {submitError && <Alert type="error">{submitError}</Alert>}

        <Input
          label="Email"
          name="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
          required
        />
        <Input
          label="Email OTP"
          name="emailOtp"
          inputMode="numeric"
          maxLength={6}
          placeholder="6-digit code"
          value={values.emailOtp}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.emailOtp}
          required
        />
        <Input
          label="Mobile OTP"
          name="phoneOtp"
          inputMode="numeric"
          maxLength={6}
          placeholder="6-digit code"
          value={values.phoneOtp}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.phoneOtp}
          required
        />

        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting}>
          Verify account
        </Button>

        <button
          type="button"
          onClick={handleResend}
          disabled={isResending}
          className="mx-auto flex items-center gap-1.5 text-sm font-medium text-harbor-600 dark:text-harbor-400 hover:underline disabled:opacity-50"
        >
          <RefreshCw className={`h-3.5 w-3.5 ${isResending ? "animate-spin" : ""}`} /> Resend OTP
        </button>
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
