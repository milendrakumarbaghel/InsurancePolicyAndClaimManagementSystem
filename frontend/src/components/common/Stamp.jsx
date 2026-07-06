import { toTitleCase } from "../../utils/formatters";
import { STATUS_STYLES } from "../../utils/constants";

/**
 * The app's signature status indicator — styled like a ledger rubber-stamp,
 * used everywhere a claim / policy / payment / active status is shown.
 */
export default function Stamp({ status, className = "" }) {
  const key = typeof status === "boolean" ? String(status) : status;
  const colorClasses = STATUS_STYLES[key] || "text-ink-500 border-ink-400";
  const label = typeof status === "boolean" ? (status ? "Active" : "Inactive") : toTitleCase(status);

  return (
    <span className={`stamp text-[0.65rem] ${colorClasses} ${className}`}>
      {label}
    </span>
  );
}
