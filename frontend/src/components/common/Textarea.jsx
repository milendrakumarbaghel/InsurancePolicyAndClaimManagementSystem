import { forwardRef } from "react";

const Textarea = forwardRef(function Textarea(
  { label, error, hint, className = "", containerClassName = "", required, rows = 4, ...props },
  ref
) {
  return (
    <div className={`flex flex-col gap-1.5 ${containerClassName}`}>
      {label && (
        <label htmlFor={props.id || props.name} className="text-sm font-medium text-ink-700 dark:text-ink-200">
          {label} {required && <span className="text-danger">*</span>}
        </label>
      )}
      <textarea
        ref={ref}
        id={props.id || props.name}
        rows={rows}
        className={`w-full rounded-lg border bg-white dark:bg-ink-900 px-3.5 py-2.5 text-sm text-ink-900 dark:text-ink-50
          placeholder:text-ink-400 dark:placeholder:text-ink-500
          border-ink-200 dark:border-ink-700
          focus:outline-none focus:ring-2 focus:ring-harbor-500 focus:border-harbor-500
          transition-colors duration-150 resize-y
          ${error ? "border-danger focus:ring-danger focus:border-danger" : ""}
          ${className}`}
        {...props}
      />
      {error ? (
        <p className="text-xs font-medium text-danger">{error}</p>
      ) : hint ? (
        <p className="text-xs text-ink-400">{hint}</p>
      ) : null}
    </div>
  );
});

export default Textarea;
