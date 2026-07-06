import { forwardRef } from "react";
import { ChevronDown } from "lucide-react";

const Select = forwardRef(function Select(
  { label, error, hint, options = [], placeholder, className = "", containerClassName = "", required, ...props },
  ref
) {
  return (
    <div className={`flex flex-col gap-1.5 ${containerClassName}`}>
      {label && (
        <label htmlFor={props.id || props.name} className="text-sm font-medium text-ink-700 dark:text-ink-200">
          {label} {required && <span className="text-danger">*</span>}
        </label>
      )}
      <div className="relative">
        <select
          ref={ref}
          id={props.id || props.name}
          className={`w-full appearance-none rounded-lg border bg-white dark:bg-ink-900 px-3.5 py-2.5 pr-9 text-sm text-ink-900 dark:text-ink-50
            border-ink-200 dark:border-ink-700
            focus:outline-none focus:ring-2 focus:ring-harbor-500 focus:border-harbor-500
            disabled:opacity-60
            transition-colors duration-150
            ${error ? "border-danger focus:ring-danger focus:border-danger" : ""}
            ${className}`}
          {...props}
        >
          {placeholder && <option value="">{placeholder}</option>}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <ChevronDown className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-400" />
      </div>
      {error ? (
        <p className="text-xs font-medium text-danger">{error}</p>
      ) : hint ? (
        <p className="text-xs text-ink-400">{hint}</p>
      ) : null}
    </div>
  );
});

export default Select;
