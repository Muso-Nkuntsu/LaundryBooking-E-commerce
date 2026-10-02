import type { ReactNode } from "react";
import Drum from "./Drum";

export function Loading({ message = "Loading..." }: { message?: string }) {
  return (
    <div className="state" role="status" aria-live="polite">
      <Drum state="spinning" size={56} />
      <p>{message}</p>
    </div>
  );
}

export function SkeletonGrid({ count = 3, height = 180 }: { count?: number; height?: number }) {
  return (
    <div className="grid grid-3" aria-hidden="true">
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className="skeleton" style={{ height }} />
      ))}
    </div>
  );
}

interface MessageStateProps {
  title: string;
  message?: string;
  action?: ReactNode;
}

export function EmptyState({ title, message, action }: MessageStateProps) {
  return (
    <div className="card state">
      <Drum state="off" size={56} />
      <h2>{title}</h2>
      {message && <p>{message}</p>}
      {action}
    </div>
  );
}

export function ErrorState({ title = "That didn't load", message, onRetry }: { title?: string; message: string; onRetry?: () => void }) {
  return (
    <div className="card state" role="alert">
      <h2>{title}</h2>
      <p>{message}</p>
      {onRetry && (
        <button type="button" className="btn btn-ghost" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  );
}
