import { useCallback, useEffect, useState } from "react";

interface FetchState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

/**
 * Loads data when the component mounts and whenever `key` changes.
 * Returns the data, loading and error state, plus `reload` and `setData`.
 */
export function useFetch<T>(loader: () => Promise<T>, key: string | number = "", fallbackError = "Something went wrong. Try again.") {
  const [state, setState] = useState<FetchState<T>>({ data: null, loading: true, error: null });
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let active = true;

    loader()
      .then((data) => {
        if (active) setState({ data, loading: false, error: null });
      })
      .catch((error: unknown) => {
        if (!active) return;
        const message = error instanceof Error && error.message ? error.message : fallbackError;
        setState((prev) => ({ ...prev, loading: false, error: message }));
      });

    return () => {
      active = false;
    };
    // The loader is recreated on every render, so `key` decides when to refetch.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key, attempt]);

  const reload = useCallback(() => {
    setState((prev) => ({ ...prev, loading: true, error: null }));
    setAttempt((n) => n + 1);
  }, []);

  const setData = useCallback((update: (current: T | null) => T | null) => {
    setState((prev) => ({ ...prev, data: update(prev.data) }));
  }, []);

  return { ...state, reload, setData };
}
