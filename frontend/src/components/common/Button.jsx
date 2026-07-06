import { Loader2 } from "lucide-react";

const variants = {
  primary:
    "bg-harbor-600 text-white hover:bg-harbor-700 focus-visible:outline-harbor-600 dark:bg-harbor-500 dark:hover:bg-harbor-400 dark:text-ink-950",
  gold:
    "bg-gold-500 text-ink-950 hover:bg-gold-400 focus-visible:outline-gold-500",
  outline:
    "border border-ink-300 text-ink-800 hover:bg-ink-100 dark:border-ink-600 dark:text-ink-100 dark:hover:bg-ink-800 bg-transparent",
  ghost:
    "text-ink-700 hover:bg-ink-100 dark:text-ink-200 dark:hover:bg-ink-800 bg-transparent",
  danger:
    "bg-danger text-white hover:bg-danger-dim focus-visible:outline-danger",
};

const sizes = {
  sm: "text-sm px-3 py-1.5 gap-1.5",
  md: "text-sm px-4 py-2.5 gap-2",
  lg: "text-base px-6 py-3 gap-2",
};

export default function Button({
  as: Component = "button",
  variant = "primary",
  size = "md",
  isLoading = false,
  disabled = false,
  className = "",
  children,
  icon: Icon,
  ...props
}) {
  return (
    <Component
      disabled={disabled || isLoading}
      className={`inline-flex items-center justify-center rounded-lg font-semibold transition-all duration-200
        focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2
        disabled:opacity-50 disabled:cursor-not-allowed active:scale-[0.98]
        ${variants[variant]} ${sizes[size]} ${className}`}
      {...props}
    >
      {isLoading && <Loader2 className="h-4 w-4 animate-spin" />}
      {!isLoading && Icon && <Icon className="h-4 w-4" />}
      {children}
    </Component>
  );
}
