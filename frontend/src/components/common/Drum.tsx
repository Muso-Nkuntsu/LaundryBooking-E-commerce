// The washing-machine door used across the app: as a loader, as the brand mark,
// and to show the state of each machine.
export type DrumState = "idle" | "spinning" | "off";

interface DrumProps {
  state?: DrumState;
  size?: number | string;
  slow?: boolean;
  tone?: "light" | "dark";
  label?: string;
}

function Drum({ state = "idle", size = 64, slow = false, tone = "light", label }: DrumProps) {
  const ring = tone === "dark" ? "#ffffff" : "#07424B";
  const glass = state === "off" ? "#E2EDEF" : tone === "dark" ? "#0B6B78" : "#DCEFF1";
  const water = state === "off" ? "#B9C9CC" : tone === "dark" ? "#F4B63F" : "#0B6B78";
  const spinClass = state === "spinning" ? (slow ? "drum-slow" : "drum-spin") : undefined;

  return (
    <svg
      viewBox="0 0 100 100"
      width={size}
      height={size}
      role={label ? "img" : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
    >
      <circle cx="50" cy="50" r="47" fill="none" stroke={ring} strokeWidth="6" opacity={tone === "dark" ? 0.9 : 1} />
      <circle cx="50" cy="50" r="36" fill={glass} />
      <g className={spinClass}>
        <path d="M50 22a28 28 0 0 1 26 18c-9 6-17-6-26 0s-17-6-26 0a28 28 0 0 1 26-18z" fill={water} opacity="0.9" />
        <path d="M24 60c9-6 17 6 26 0s17 6 26 0a28 28 0 0 1-52 0z" fill={water} opacity="0.55" />
        <circle cx="50" cy="50" r="5" fill={ring} opacity="0.85" />
      </g>
      <circle cx="50" cy="50" r="36" fill="none" stroke={ring} strokeWidth="2.5" opacity="0.35" />
    </svg>
  );
}

export default Drum;
