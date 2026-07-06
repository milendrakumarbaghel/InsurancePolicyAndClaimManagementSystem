export default function PageHeader({ eyebrow, title, description, actions }) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        {eyebrow && (
          <p className="mb-1.5 text-xs font-semibold uppercase tracking-[0.18em] text-harbor-500">{eyebrow}</p>
        )}
        <h1 className="font-display text-2xl sm:text-3xl font-semibold text-ink-900 dark:text-white">{title}</h1>
        {description && <p className="mt-1.5 max-w-2xl text-sm text-ink-500 dark:text-ink-400">{description}</p>}
      </div>
      {actions && <div className="flex flex-shrink-0 items-center gap-2">{actions}</div>}
    </div>
  );
}
