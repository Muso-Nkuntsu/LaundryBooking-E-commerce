import styles from './LoadingSpinner.module.css';

/**
 * @param {{ label?: string }} props
 */
export default function LoadingSpinner({ label = 'Loading…' }) {
  return (
    <div className={styles.wrap} role="status" aria-live="polite">
      <span className={styles.spinner} aria-hidden="true" />
      <span className={styles.label}>{label}</span>
    </div>
  );
}