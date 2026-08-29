import styles from './ErrorState.module.css';

/**
 * @param {{ message?: string, onRetry?: () => void }} props
 */
export default function ErrorState({
  message = "We couldn't load this. Check your connection and try again.",
  onRetry,
}) {
  return (
    <div className={styles.wrap} role="alert">
      <p className={styles.message}>{message}</p>
      {onRetry && (
        <button type="button" className={styles.retryButton} onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  );
}