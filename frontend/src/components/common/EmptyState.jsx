export default function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-2xl border border-dashed border-ink-300 dark:border-ink-700 px-6 py-14 text-center">
      {Icon && (
        <div className="rounded-full bg-harbor-50 dark:bg-ink-800 p-3">
          <Icon className="h-6 w-6 text-harbor-500" />
        </div>
      )}
      <h3 className="font-display text-lg font-medium text-ink-800 dark:text-ink-100">{title}</h3>
      {description && <p className="max-w-sm text-sm text-ink-500 dark:text-ink-400">{description}</p>}
      {action}
    </div>
  );
}
