import { useCallback, useEffect, useState } from 'react';
import { getLaundryServices } from '../api/laundryService';

/**
 * @param {string} residenceId
 */
export function useLaundryServices(residenceId) {
  const [services, setServices] = useState([]);
  const [status, setStatus] = useState('idle'); // idle | loading | success | error
  const [error, setError] = useState(null);

  const load = useCallback(() => {
    if (!residenceId) return;
    const controller = new AbortController();
    setStatus('loading');
    setError(null);

    getLaundryServices(residenceId, { signal: controller.signal })
      .then((result) => {
        setServices(result);
        setStatus('success');
      })
      .catch((err) => {
        if (err.name === 'AbortError') return;
        setError(err);
        setStatus('error');
      });

    return () => controller.abort();
  }, [residenceId]);

  useEffect(() => {
    const cleanup = load();
    return cleanup;
  }, [load]);

  return { services, status, error, retry: load };
}