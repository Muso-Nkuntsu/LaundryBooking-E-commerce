import React, { useEffect } from "react";
import { colors, type } from "../../styles/theme";
import { injectKeyframesOnce } from "../../styles/theme";

interface LoadingProps {
  message?: string;
  
  variant?: "inline" | "page";
}

const SPIN_KEYFRAMES_ID = "loading-spin-keyframes";
const SPIN_KEYFRAMES_CSS = `
@keyframes loading-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}`;

const Loading: React.FC<LoadingProps> = ({
  message = "Loading...",
  variant = "inline",
}) => {
  useEffect(() => {
    injectKeyframesOnce(SPIN_KEYFRAMES_ID, SPIN_KEYFRAMES_CSS);
  }, []);

  const containerStyle: React.CSSProperties = {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    gap: "12px",
    padding: variant === "page" ? "64px 16px" : "32px 16px",
    width: "100%",
  };

  const spinnerStyle: React.CSSProperties = {
    width: "32px",
    height: "32px",
    borderRadius: "50%",
    border: `3px solid ${colors.primaryLight}`,
    borderTopColor: colors.primary,
    animation: "loading-spin 0.7s linear infinite",
  };

  const textStyle: React.CSSProperties = {
    fontFamily: type.body,
    fontSize: "14px",
    color: colors.textMuted,
  };

  return (
    <div style={containerStyle} role="status" aria-live="polite">
      <div style={spinnerStyle} />
      <span style={textStyle}>{message}</span>
    </div>
  );
};

export default Loading;