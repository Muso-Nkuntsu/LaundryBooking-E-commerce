import styles from './LaundryServices.module.css';

function formatPrice(price, currency = 'ZAR') {
  return new Intl.NumberFormat(undefined, { style: 'currency', currency }).format(price);
}

/**
 * @param {{
 *   service: import('../../api/laundryService').LaundryService,
 *   isSelected: boolean,
 *   onSelect: (service: import('../../api/laundryService').LaundryService) => void,
 *   onViewDetails: (service: import('../../api/laundryService').LaundryService) => void
 * }} props
 */
export default function ServiceCard({ service, isSelected, onSelect, onViewDetails }) {
  const { name, description, price, currency, available, durationLabel } = service;

  return (
    <li className={`${styles.card} ${!available ? styles.cardUnavailable : ''}`}>
      <button
        type="button"
        className={styles.cardMain}
        onClick={() => onViewDetails(service)}
        aria-label={`View details for ${name}`}
      >
        <div className={styles.cardTop}>
          <h3 className={styles.cardName}>{name}</h3>
          <span
            className={`${styles.availabilityBadge} ${
              available ? styles.availabilityOpen : styles.availabilityClosed
            }`}
          >
            {available ? 'Available' : 'Unavailable'}
          </span>
        </div>
        <p className={styles.cardDescription}>{description}</p>
        <div className={styles.cardMeta}>
          <span className={styles.cardPrice}>{formatPrice(price, currency)}</span>
          {durationLabel && <span className={styles.cardDuration}>{durationLabel}</span>}
        </div>
      </button>

      <button
        type="button"
        className={`${styles.selectButton} ${isSelected ? styles.selectButtonActive : ''}`}
        disabled={!available}
        onClick={() => onSelect(service)}
      >
        {isSelected ? 'Added ✓' : 'Add service'}
      </button>
    </li>
  );
}