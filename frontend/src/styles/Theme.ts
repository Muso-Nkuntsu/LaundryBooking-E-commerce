export const colors = {
  bg: "#F6F7F4",
  surface: "#FFFFFF",
  surfaceAlt: "#EFF2EE",
  text: "#1C2620",
  textMuted: "#5C665F",
  border: "#DCE3DD",

  primary: "#1F6F54",
  primaryHover: "#175941",
  primaryLight: "#E3F0EA",

  accent: "#D98E3B",
  accentLight: "#FBEFDD",

  unavailableBg: "#ECECE9",
  unavailableBorder: "#D8D8D3",
  unavailableText: "#96A199",

  danger: "#B3452F",
  dangerLight: "#F7E7E1",
  dangerBorder: "#E7C3B4",
} as const;

export const type = {
  display: "'Sora', 'Segoe UI', system-ui, sans-serif",
  body: "'Inter', 'Segoe UI', system-ui, sans-serif",
} as const;

export const radius = {
  sm: "6px",
  md: "10px",
  lg: "16px",
  pill: "999px",
} as const;

export const spacing = (multiplier: number): string => `${multiplier * 4}px`;

export const shadow = {
  card: "0 1px 2px rgba(28, 38, 32, 0.06), 0 1px 1px rgba(28, 38, 32, 0.04)",
};

export const injectKeyframesOnce = (id: string, css: string): void => {
  if (typeof document === "undefined") return;
  if (document.getElementById(id)) return;
  const styleEl = document.createElement("style");
  styleEl.id = id;
  styleEl.textContent = css;
  document.head.appendChild(styleEl);
};