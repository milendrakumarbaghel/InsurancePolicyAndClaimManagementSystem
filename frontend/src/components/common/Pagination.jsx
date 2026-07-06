import { ChevronLeft, ChevronRight } from "lucide-react";

export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, i) => i).filter(
    (p) => p === 0 || p === totalPages - 1 || Math.abs(p - page) <= 1
  );

  return (
    <div className="flex items-center justify-center gap-1.5 pt-4">
      <button
        onClick={() => onPageChange(Math.max(0, page - 1))}
        disabled={page === 0}
        className="rounded-lg p-2 text-ink-500 hover:bg-ink-100 dark:hover:bg-ink-800 disabled:opacity-40 disabled:cursor-not-allowed"
        aria-label="Previous page"
      >
        <ChevronLeft className="h-4 w-4" />
      </button>
      {pages.map((p, idx) => (
        <span key={p} className="flex items-center">
          {idx > 0 && pages[idx - 1] !== p - 1 && <span className="px-1 text-ink-400">…</span>}
          <button
            onClick={() => onPageChange(p)}
            className={`h-8 min-w-8 rounded-lg px-2 text-sm font-medium transition-colors ${
              p === page
                ? "bg-harbor-600 text-white"
                : "text-ink-600 dark:text-ink-300 hover:bg-ink-100 dark:hover:bg-ink-800"
            }`}
          >
            {p + 1}
          </button>
        </span>
      ))}
      <button
        onClick={() => onPageChange(Math.min(totalPages - 1, page + 1))}
        disabled={page === totalPages - 1}
        className="rounded-lg p-2 text-ink-500 hover:bg-ink-100 dark:hover:bg-ink-800 disabled:opacity-40 disabled:cursor-not-allowed"
        aria-label="Next page"
      >
        <ChevronRight className="h-4 w-4" />
      </button>
    </div>
  );
}
