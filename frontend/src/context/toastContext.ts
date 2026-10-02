import { createContext } from "react";

export type ToastKind = "ok" | "error";

export interface ToastApi {
  success: (message: string) => void;
  error: (message: string) => void;
}

export const ToastContext = createContext<ToastApi | null>(null);
