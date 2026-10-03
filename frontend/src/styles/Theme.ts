// Shared design tokens for components that use inline styles.
// Keep these in step with the CSS variables in index.css.
export const colors = {
  bg: "#EEF4F5",
  surface: "#FFFFFF",
  surfaceAlt: "#E2EDEF",
  text: "#10262C",
  textMuted: "#4D6369",
  border: "#D3E0E2",

  primary: "#0B6B78",
  primaryHover: "#07424B",
  primaryLight: "#DCEFF1",

  accentLight: "#FDF1D6",

  unavailableBg: "#E2EDEF",
  unavailableBorder: "#D3E0E2",
  unavailableText: "#8FA3A8",

  danger: "#C4412B",
  dangerLight: "#FBE9E5",
  dangerBorder: "#EFC2B8",
} as const;

export const type = {
  display: "'Bricolage Grotesque', 'Segoe UI', system-ui, sans-serif",
  body: "'Figtree', 'Segoe UI', system-ui, sans-serif",
} as const;

export const radius = {
  sm: "8px",
  md: "14px",
  lg: "22px",
  pill: "999px",
} as const;

export const shadow = {
  card: "0 10px 28px -18px rgba(7, 66, 75, 0.35)",
};
