import { Loader2 } from "lucide-react";

export default function Spinner({ label = "Loading…", size = "h-6 w-6", className = "" }) {
  return (
    <div className={`flex flex-col items-center justify-center gap-3 py-10 text-ink-400 ${className}`}>
      <Loader2 className={`${size} animate-spin text-harbor-500`} />
      {label && <p className="text-sm">{label}</p>}
    </div>
  );
}
