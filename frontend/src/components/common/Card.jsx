export default function Card({ children, className = "", padded = true, ...props }) {
  return (
    <div
      className={`rounded-2xl border border-ink-200/70 dark:border-ink-800 bg-white dark:bg-ink-900
        shadow-sm shadow-ink-900/5 dark:shadow-black/20
        ${padded ? "p-5 sm:p-6" : ""} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}
