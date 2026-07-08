import { Loader2 } from "lucide-react";

export default function Switch({ checked, onChange, disabled = false, isLoading = false, className = "" }) {
  return (
    <button
      type="button"
      disabled={disabled || isLoading}
      onClick={onChange}
      className={`relative inline-flex h-5 w-9 flex-shrink-0 cursor-pointer rounded-full border border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-harbor-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed ${
        checked ? "bg-emerald-500 dark:bg-emerald-600" : "bg-slate-300 dark:bg-slate-700"
      } ${className}`}
    >
      <span
        aria-hidden="true"
        className={`pointer-events-none flex items-center justify-center h-4 w-4 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out ${
          checked ? "translate-x-4" : "translate-x-0"
        }`}
      >
        {isLoading && <Loader2 className="h-2.5 w-2.5 animate-spin text-slate-400" />}
      </span>
    </button>
  );
}
