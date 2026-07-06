import { useCallback, useEffect, useState } from "react";
import { getErrorMessage } from "../services/api";

/**
 * Wraps a Spring Data `Page<T>` endpoint (content, totalPages, totalElements, number)
 * with page/sort/filter state and refetching.
 *
 * @param {(params: object) => Promise<any>} fetcher - service call accepting { page, size, sortBy, sortDir, ...filters }
 * @param {object} options
 */
export function usePagedResource(fetcher, { size = 10, sortBy = "id", sortDir = "desc", filters = {} } = []) {
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [activeFilters, setActiveFilters] = useState(filters);
  const [refreshKey, setRefreshKey] = useState(0);

  const refresh = useCallback(() => setRefreshKey((k) => k + 1), []);

  useEffect(() => {
    let cancelled = false;
    setIsLoading(true);
    setError("");

    const cleanFilters = Object.fromEntries(
      Object.entries(activeFilters).filter(([, v]) => v !== "" && v !== null && v !== undefined)
    );

    fetcher({ page, size, sortBy, sortDir, ...cleanFilters })
      .then((result) => {
        if (cancelled) return;
        // Spring Data Page shape: { content, totalPages, totalElements, number, ... }
        setData({
          content: result?.content ?? [],
          totalPages: result?.totalPages ?? 0,
          totalElements: result?.totalElements ?? 0,
        });
      })
      .catch((err) => {
        if (cancelled) return;
        setError(getErrorMessage(err, "Could not load this list."));
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size, sortBy, sortDir, JSON.stringify(activeFilters), refreshKey]);

  const setFilter = useCallback((key, value) => {
    setPage(0);
    setActiveFilters((prev) => ({ ...prev, [key]: value }));
  }, []);

  return {
    page,
    setPage,
    ...data,
    isLoading,
    error,
    filters: activeFilters,
    setFilter,
    refresh,
  };
}
