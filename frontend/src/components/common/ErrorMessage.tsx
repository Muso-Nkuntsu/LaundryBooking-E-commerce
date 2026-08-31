import React from "react";
import { colors, radius, type } from "../../styles/theme";

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

const ErrorMessage: React.FC<ErrorMessageProps> = ({ message, onRetry }) => {
  const containerStyle: React.CSSProperties = {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    textAlign: "center",
    gap: "10px",
    padding: "28px 20px",
    backgroundColor: colors.dangerLight,
    border: `1px solid ${colors.dangerBorder}`,
    borderRadius: radius.md,
    width: "100%",
    boxSizing: "border-box",
  };

  const iconStyle: React.CSSProperties = {
    width: "28px",
    height: "28px",
    borderRadius: "50%",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.danger,
    color: colors.surface,
    fontFamily: type.body,
    fontWeight: 700,
    fontSize: "15px",
  };

  const textStyle: React.CSSProperties = {
    fontFamily: type.body,
    fontSize: "14px",
    color: colors.text,
    maxWidth: "360px",
  };

  const buttonStyle: React.CSSProperties = {
    marginTop: "4px",
    padding: "8px 18px",
    borderRadius: radius.pill,
    border: `1px solid ${colors.danger}`,
    backgroundColor: "transparent",
    color: colors.danger,
    fontFamily: type.body,
    fontSize: "13px",
    fontWeight: 600,
    cursor: "pointer",
  };

  return (
    <div style={containerStyle} role="alert">
      <div style={iconStyle}>!</div>
      <p style={textStyle}>{message}</p>
      {onRetry && (
        <button type="button" style={buttonStyle} onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  );
};

export default ErrorMessage;