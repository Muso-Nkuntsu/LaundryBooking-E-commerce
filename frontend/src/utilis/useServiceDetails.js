import { useCallback, useEffect, useState } from 'react';
import { getLaundryServiceById } from '../api/laundryService';

/**
 * @param {string} serviceId
 */
export function useServiceDetails(serviceId) {
  const [service, setService] = useState(null);
  const [status, setStatus] = useState('idle'); // idle | loading | success | error
  const [error, setError] = useState(null);

  const load = useCallback(() => {
    if (!serviceId) return;
    const controller = new AbortController();
    setStatus('loading');
    setError(null);

    getLaundryServiceById(serviceId, { signal: controller.signal })
      .then((result) => {
        setService(result);
        setStatus('success');
      })
      .catch((err) => {
        if (err.name === 'AbortError') return;
        setError(err);
        setStatus('error');
      });

    return () => controller.abort();
  }, [serviceId]);

  useEffect(() => {
    const cleanup = load();
    return cleanup;
  }, [load]);

  return { service, status, error, retry: load };
}