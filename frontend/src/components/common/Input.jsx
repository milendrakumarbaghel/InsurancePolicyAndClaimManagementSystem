import { forwardRef, useState } from "react";
import { Eye, EyeOff } from "lucide-react";

const Input = forwardRef(function Input(
  { label, error, hint, icon: Icon, className = "", containerClassName = "", required, type, ...props },
  ref
) {
  const [showPassword, setShowPassword] = useState(false);
  const isPasswordType = type === "password";

  return (
    <div className={`flex flex-col gap-1.5 ${containerClassName}`}>
      {label && (
        <label htmlFor={props.id || props.name} className="text-sm font-medium text-ink-700 dark:text-ink-200">
          {label} {required && <span className="text-danger">*</span>}
        </label>
      )}
      <div className="relative">
        {Icon && (
          <Icon className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-400" />
        )}
        <input
          ref={ref}
          id={props.id || props.name}
          type={isPasswordType ? (showPassword ? "text" : "password") : type}
          className={`w-full rounded-lg border bg-white dark:bg-ink-900 px-3.5 py-2.5 text-sm text-ink-900 dark:text-ink-50
            placeholder:text-ink-400 dark:placeholder:text-ink-500
            border-ink-200 dark:border-ink-700
            focus:outline-none focus:ring-2 focus:ring-harbor-500 focus:border-harbor-500
            disabled:opacity-60 disabled:bg-ink-100 dark:disabled:bg-ink-800
            transition-colors duration-150
            ${Icon ? "pl-10" : ""}
            ${isPasswordType ? "pr-10" : ""}
            ${error ? "border-danger focus:ring-danger focus:border-danger" : ""}
            ${className}`}
          {...props}
        />
        {isPasswordType && (
          <button
            type="button"
            className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-400 hover:text-ink-600 dark:hover:text-ink-200 focus:outline-none"
            onClick={() => setShowPassword((prev) => !prev)}
            aria-label={showPassword ? "Hide password" : "Show password"}
          >
            {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        )}
      </div>
      {error ? (
        <p className="text-xs font-medium text-danger">{error}</p>
      ) : hint ? (
        <p className="text-xs text-ink-400">{hint}</p>
      ) : null}
    </div>
  );
});

export default Input;
