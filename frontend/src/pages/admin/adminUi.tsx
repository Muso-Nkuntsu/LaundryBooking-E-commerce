// Small pieces shared by the admin pages: status labels, pop-up window, stat tiles and charts.
import { useEffect } from "react";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { formatCurrency } from "../../utilis/FormatCurrency";

// ---------- Status labels ----------

type Tone = "ok" | "warn" | "bad" | "info" | "off";

const STATUS: Record<string, { label: string; tone: Tone }> = {
  // bookings
  "booking:PENDING": { label: "Awaiting payment", tone: "warn" },
  "booking:CONFIRMED": { label: "Confirmed", tone: "info" },
  "booking:COMPLETED": { label: "Completed", tone: "ok" },
  "booking:CANCELLED": { label: "Cancelled", tone: "off" },
  // orders
  "order:PENDING": { label: "Awaiting payment", tone: "warn" },
  "order:PAID": { label: "Paid", tone: "info" },
  "order:IN_PROGRESS": { label: "Being prepared", tone: "info" },
  "order:COMPLETED": { label: "Completed", tone: "ok" },
  "order:CANCELLED": { label: "Cancelled", tone: "off" },
  // payments
  "payment:PENDING": { label: "To verify", tone: "warn" },
  "payment:COMPLETED": { label: "Received", tone: "ok" },
  "payment:FAILED": { label: "Rejected", tone: "bad" },
  // the payment summary on an order
  "orderpay:PAID": { label: "Paid", tone: "ok" },
  "orderpay:AWAITING_VERIFICATION": { label: "EFT to verify", tone: "warn" },
  "orderpay:FAILED": { label: "Payment rejected", tone: "bad" },
  "orderpay:NONE": { label: "Not paid", tone: "off" },
  // machines
  "machine:AVAILABLE": { label: "Available", tone: "ok" },
  "machine:IN_USE": { label: "In use", tone: "info" },
  "machine:OUT_OF_ORDER": { label: "Out of order", tone: "bad" },
};

export type StatusKind = "booking" | "order" | "payment" | "orderpay" | "machine";

export function statusLabel(kind: StatusKind, value: string): string {
  return STATUS[`${kind}:${value}`]?.label ?? value.replace(/_/g, " ").toLowerCase();
}

export function Status({ kind, value }: { kind: StatusKind; value: string }) {
  const found = STATUS[`${kind}:${value}`];
  return <span className={`pill pill-${found?.tone ?? "off"}`}>{found?.label ?? value}</span>;
}

/** Words for the action buttons, e.g. "Confirm" for moving a booking to CONFIRMED. */
export const ACTION_LABEL: Record<string, string> = {
  CONFIRMED: "Confirm",
  COMPLETED: "Mark completed",
  CANCELLED: "Cancel",
  PAID: "Mark as paid",
  IN_PROGRESS: "Start preparing",
};

// ---------- Formatting ----------

export const money = (amount: number | null | undefined) => formatCurrency(amount ?? 0);

export const shortTime = (time: string | null | undefined) => (time ? time.slice(0, 5) : "—");

export const shortDate = (iso: string | null | undefined) =>
  iso ? new Date(iso.length === 10 ? `${iso}T00:00:00` : iso).toLocaleDateString("en-ZA", { day: "numeric", month: "short", year: "numeric" }) : "—";

export const dateTime = (iso: string | null | undefined) =>
  iso ? new Date(iso).toLocaleString("en-ZA", { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" }) : "—";

/** Today's date as YYYY-MM-DD in the computer's own time zone. */
export const todayIso = (offsetDays = 0) => {
  const d = new Date();
  d.setDate(d.getDate() + offsetDays);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};

// ---------- Layout pieces ----------

export function PageHead({ title, intro, children }: { title: string; intro?: string; children?: ReactNode }) {
  return (
    <div className="adm-head">
      <div>
        <h1>{title}</h1>
        {intro && <p>{intro}</p>}
      </div>
      {children && <div className="btn-row">{children}</div>}
    </div>
  );
}

export function StatTile({ label, value, note, tone, to }: {
  label: string; value: ReactNode; note?: string; tone?: "warn" | "bad" | "ok"; to?: string;
}) {
  const body = (
    <>
      <span>{label}</span>
      <strong>{value}</strong>
      {note && <small>{note}</small>}
    </>
  );
  const className = `adm-stat${tone ? ` ${tone}` : ""}`;
  return to ? <Link to={to} className={className}>{body}</Link> : <div className={className}>{body}</div>;
}

/** A segmented filter, e.g. All / Pending / Confirmed. */
export function Segmented<T extends string>({ value, options, onChange, label }: {
  value: T; options: { value: T; label: string }[]; onChange: (value: T) => void; label: string;
}) {
  return (
    <div className="segmented" role="group" aria-label={label}>
      {options.map((option) => (
        <button key={option.value} type="button" aria-pressed={value === option.value} onClick={() => onChange(option.value)}>
          {option.label}
        </button>
      ))}
    </div>
  );
}

export function Modal({ title, onClose, children, narrow }: {
  title: string; onClose: () => void; children: ReactNode; narrow?: boolean;
}) {
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  return (
    <div className="scrim" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
      <div className={`adm-modal${narrow ? " narrow" : ""}`} role="dialog" aria-modal="true" aria-label={title}>
        <div className="adm-modal-head">
          <h2>{title}</h2>
          <button type="button" className="adm-close" onClick={onClose} aria-label="Close">×</button>
        </div>
        <div className="adm-modal-body">{children}</div>
      </div>
    </div>
  );
}

// ---------- Charts ----------

/** One bar per day. Hover a bar to see its exact value. */
export function DayChart({ points, mode = "count" }: {
  points: { date: string; count: number; amount: number }[]; mode?: "count" | "amount";
}) {
  const values = points.map((p) => (mode === "count" ? p.count : p.amount));
  const max = Math.max(...values, 1);
  const total = values.reduce((sum, v) => sum + v, 0);
  return (
    <figure style={{ margin: 0 }}>
      <div className="adm-chart" role="img" aria-label={`${points.length} days, total ${mode === "count" ? total : money(total)}`}>
        {points.map((point, i) => (
          <div key={point.date} title={`${shortDate(point.date)}: ${mode === "count" ? point.count : money(point.amount)}`}>
            <span style={{ height: `${(values[i] / max) * 100}%` }} />
          </div>
        ))}
      </div>
      <div className="adm-chart-axis">
        <span>{shortDate(points[0]?.date)}</span>
        <span>{shortDate(points[points.length - 1]?.date)}</span>
      </div>
    </figure>
  );
}

/** Horizontal bars, biggest first. */
export function Bars({ items, kind, empty = "Nothing yet." }: {
  items: { label: string; count: number }[]; kind?: StatusKind; empty?: string;
}) {
  if (items.length === 0) return <p className="muted">{empty}</p>;
  const max = Math.max(...items.map((i) => i.count), 1);
  return (
    <ul className="adm-bars">
      {items.map((item) => (
        <li key={item.label}>
          <span>{kind ? statusLabel(kind, item.label) : item.label}</span>
          <i><b style={{ width: `${(item.count / max) * 100}%` }} /></i>
          <span>{item.count}</span>
        </li>
      ))}
    </ul>
  );
}
