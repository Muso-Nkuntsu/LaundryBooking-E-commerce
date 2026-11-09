import { useCallback, useEffect, useState } from "react";
import type { ReactNode } from "react";

import { getApiErrorMessage } from "../../services/Api";

export function StatCard(props: { label: string; value: string | number; hint?: string; tone?: "ok" | "warn" | "bad" }) {
    return (
        <div className={`adm-stat ${props.tone ?? ""}`}>
            <div className="v">{props.value}</div>
            <div className="l">{props.label}</div>
            {props.hint && <div className="h">{props.hint}</div>}
        </div>
    );
}

const TONES: Record<string, string> = {
    CONFIRMED: "info", PENDING: "warn", COMPLETED: "ok", CANCELLED: "bad",
    PAID: "ok", IN_PROGRESS: "info",
    AVAILABLE: "ok", IN_USE: "warn", OUT_OF_ORDER: "bad",
    Completed: "ok", Pending: "warn", Failed: "bad", Unpaid: "",
    OK: "ok", LOW: "warn", OUT: "bad",
    ADMIN: "info", USER: "",
    Active: "ok", Inactive: "bad",
};

export function Badge({ text }: { text: string }) {
    return <span className={`adm-badge ${TONES[text] ?? ""}`}>{text.replace(/_/g, " ")}</span>;
}

export function Notice(props: { kind: "err" | "okk" | "inf"; children: ReactNode }) {
    return <div className={`adm-note ${props.kind}`} role={props.kind === "err" ? "alert" : "status"}>{props.children}</div>;
}

export const money = (value: number | null | undefined): string => `R${(value ?? 0).toFixed(2)}`;

export const fmtDateTime = (iso: string | null | undefined): string => {
    if (!iso) return "-";
    const d = new Date(iso);
    return Number.isNaN(d.getTime()) ? iso : d.toLocaleString();
};

/** Loads data from the API and exposes loading / error / reload. */
export function useLoad<T>(loader: () => Promise<T>) {
    const [data, setData] = useState<T | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const reload = useCallback(async () => {
        setLoading(true);
        setError("");
        try {
            setData(await loader());
        } catch (err) {
            setError(getApiErrorMessage(err));
        } finally {
            setLoading(false);
        }
    }, [loader]);

    useEffect(() => {
        void reload();
    }, [reload]);

    return { data, loading, error, reload, setData };
}

export function LoadState(props: { loading: boolean; error: string; onRetry?: () => void }) {
    if (props.loading) return <p className="adm-muted">Loading...</p>;
    if (props.error) {
        return (
            <Notice kind="err">
                {props.error} {props.onRetry && <button onClick={props.onRetry}>Retry</button>}
            </Notice>
        );
    }
    return null;
}