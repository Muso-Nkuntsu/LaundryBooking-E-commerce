import { useState } from 'react';
import { useLaundryServices } from '../../hooks/useLaundryServices';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorState from '../common/ErrorState';
import ServiceCard from './ServiceCard';
import styles from './LaundryServices.module.css';

/**
 * Laundry Services — browse services offered by the residence.
 *
 * @param {{
 *   residenceId: string,
 *   selectedServiceIds?: string[],
 *   onSelectService: (service: import('../../api/laundryService').LaundryService) => void,
 *   onViewDetails: (service: import('../../api/laundryService').LaundryService) => void
 * }} props
 */
export default function LaundryServices({
  residenceId,
  selectedServiceIds = [],
  onSelectService,
  onViewDetails,
}) {
  const { services, status, retry } = useLaundryServices(residenceId);
  const [localSelectedIds, setLocalSelectedIds] = useState(new Set(selectedServiceIds));

  function handleSelect(service) {
    setLocalSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(service.id)) {
        next.delete(service.id);
      } else {
        next.add(service.id);
      }
      return next;
    });
    onSelectService(service);
  }

  return (
    <section className={styles.wrap} aria-labelledby="laundry-services-heading">
      <header className={styles.header}>
        <h2 id="laundry-services-heading" className={styles.heading}>
          Laundry services
        </h2>
        <p className={styles.subheading}>
          Add wash, dry, or extra services to your booking. Tap a service to see more.
        </p>
      </header>

      {status === 'loading' && <LoadingSpinner label="Loading laundry services…" />}

      {status === 'error' && (
        <ErrorState message="We couldn't load laundry services." onRetry={retry} />
      )}

      {status === 'success' && services.length === 0 && (
        <div className={styles.empty}>
          <p>No laundry services are set up for this residence yet.</p>
        </div>
      )}

      {status === 'success' && services.length > 0 && (
        <ul className={styles.list}>
          {services.map((service) => (
            <ServiceCard
              key={service.id}
              service={service}
              isSelected={localSelectedIds.has(service.id)}
              onSelect={handleSelect}
              onViewDetails={onViewDetails}
            />
          ))}
        </ul>
      )}
    </section>
  );
}