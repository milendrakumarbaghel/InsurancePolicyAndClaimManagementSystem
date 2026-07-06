import { AlertTriangle, CheckCircle2, Info, XCircle } from "lucide-react";

const styles = {
  error: {
    wrap: "bg-danger/10 border-danger/30 text-danger-dim dark:text-red-300",
    Icon: XCircle,
  },
  success: {
    wrap: "bg-success/10 border-success/30 text-success-dim dark:text-emerald-300",
    Icon: CheckCircle2,
  },
  warning: {
    wrap: "bg-gold-500/10 border-gold-500/30 text-gold-700 dark:text-gold-300",
    Icon: AlertTriangle,
  },
  info: {
    wrap: "bg-info/10 border-info/30 text-info dark:text-sky-300",
    Icon: Info,
  },
};

export default function Alert({ type = "info", children, className = "" }) {
  const { wrap, Icon } = styles[type] || styles.info;
  return (
    <div className={`flex items-start gap-2.5 rounded-lg border px-4 py-3 text-sm font-medium ${wrap} ${className}`}>
      <Icon className="mt-0.5 h-4 w-4 flex-shrink-0" />
      <span>{children}</span>
    </div>
  );
}
