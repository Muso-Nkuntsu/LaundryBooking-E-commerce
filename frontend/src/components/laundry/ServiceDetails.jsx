import { useState } from 'react';
import { useServiceDetails } from '../../hooks/useServiceDetails';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorState from '../common/ErrorState';
import styles from './ServiceDetails.module.css';

function formatPrice(price, currency = 'ZAR') {
  return new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(price);
}

/**
 * Service Details — full information about a single laundry service,
 * with the ability to add it to the booking.
 *
 * @param {{
 *   serviceId: string,
 *   isSelected?: boolean,
 *   onSelect: (service: import('../../api/laundryService').LaundryService) => void,
 *   onBack?: () => void
 * }} props
 */
export default function ServiceDetails({ serviceId, isSelected = false, onSelect, onBack }) {
  const { service, status, retry } = useServiceDetails(serviceId);
  const [justAdded, setJustAdded] = useState(false);

  function handleSelect() {
    onSelect(service);
    setJustAdded(true);
  }

  if (status === 'loading') {
    return <LoadingSpinner label="Loading service details…" />;
  }

  if (status === 'error') {
    return <ErrorState message="We couldn't load this service." onRetry={retry} />;
  }

  if (!service) return null;

  const added = isSelected || justAdded;

  return (
    <section className={styles.wrap} aria-labelledby="service-details-heading">
      {onBack && (
        <button type="button" className={styles.backLink} onClick={onBack}>
          ← Back to services
        </button>
      )}

      <header className={styles.header}>
        <div className={styles.headerTop}>
          <h2 id="service-details-heading" className={styles.name}>
            {service.name}
          </h2>
          <span
            className={`${styles.availabilityBadge} ${
              service.available ? styles.availabilityOpen : styles.availabilityClosed
            }`}
          >
            {service.available ? 'Available' : 'Unavailable'}
          </span>
        </div>
        <p className={styles.price}>{formatPrice(service.price, service.currency)}</p>
      </header>

      <p className={styles.description}>{service.description}</p>

      <dl className={styles.infoList}>
        {service.durationLabel && (
          <div className={styles.infoRow}>
            <dt>Duration</dt>
            <dd>{service.durationLabel}</dd>
          </div>
        )}
        {service.category && (
          <div className={styles.infoRow}>
            <dt>Category</dt>
            <dd className={styles.infoCapitalize}>{service.category}</dd>
          </div>
        )}
      </dl>

      <button
        type="button"
        className={`${styles.selectButton} ${added ? styles.selectButtonActive : ''}`}
        disabled={!service.available}
        onClick={handleSelect}
      >
        {added ? 'Added to booking ✓' : 'Add to booking'}
      </button>

      {!service.available && (
        <p className={styles.unavailableHint}>
          This service isn't currently available for booking.
        </p>
      )}
    </section>
  );
}