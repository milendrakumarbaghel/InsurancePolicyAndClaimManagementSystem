import Spinner from "./Spinner";
import EmptyState from "./EmptyState";
import { Inbox } from "lucide-react";

/**
 * Generic responsive data table.
 * columns: [{ key, header, render?(row), className? }]
 */
export default function DataTable({ columns, data, isLoading, emptyTitle = "Nothing here yet", emptyDescription, keyField = "id" }) {
  if (isLoading) return <Spinner label="Fetching records…" />;

  if (!data || data.length === 0) {
    return <EmptyState icon={Inbox} title={emptyTitle} description={emptyDescription} />;
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-ink-200/70 dark:border-ink-800">
      <table className="w-full min-w-160 text-left text-sm">
        <thead>
          <tr className="border-b border-ink-200/70 dark:border-ink-800 bg-ink-50/70 dark:bg-ink-800/40">
            {columns.map((col) => (
              <th
                key={col.key}
                className={`px-4 py-3 text-xs font-semibold uppercase tracking-wider text-ink-500 dark:text-ink-400 ${col.className || ""}`}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-ink-100 dark:divide-ink-800">
          {data.map((row) => (
            <tr
              key={row[keyField]}
              className="bg-white dark:bg-ink-900 hover:bg-harbor-50/60 dark:hover:bg-ink-800/60 transition-colors"
            >
              {columns.map((col) => (
                <td key={col.key} className={`px-4 py-3.5 text-ink-700 dark:text-ink-200 ${col.className || ""}`}>
                  {col.render ? col.render(row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
