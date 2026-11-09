import React from "react";

interface RatingProps {
  value: number;
  onChange?: (value: number) => void;
  readOnly?: boolean;
  size?: "sm" | "md" | "lg";
}

const FONT_SIZES = { sm: "16px", md: "22px", lg: "28px" } as const;

export const Rating: React.FC<RatingProps> = ({
  value,
  onChange,
  readOnly = false,
  size = "md",
}) => {
  const interactive = !readOnly && Boolean(onChange);

  return (
    <div
      role={interactive ? "radiogroup" : "img"}
      aria-label={`Rating: ${value} out of 5`}
      style={{ display: "inline-flex", gap: "2px" }}
    >
      {[1, 2, 3, 4, 5].map((star) => {
        const filled = star <= value;
        const style = {
          fontSize: FONT_SIZES[size],
          lineHeight: 1,
          color: filled ? "#F4B63F" : "#C5D3D6",
        };

        if (!interactive) {
          return (
            <span key={star} style={style} aria-hidden="true">
              ★
            </span>
          );
        }

        return (
          <button
            key={star}
            type="button"
            role="radio"
            aria-checked={star === value}
            aria-label={`${star} star${star > 1 ? "s" : ""}`}
            onClick={() => onChange?.(star)}
            style={{
              ...style,
              background: "none",
              border: "none",
              padding: 0,
              cursor: "pointer",
            }}
          >
            ★
          </button>
        );
      })}
    </div>
  );
};

export default Rating;
