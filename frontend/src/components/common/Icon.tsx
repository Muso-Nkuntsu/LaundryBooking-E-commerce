const PATHS = {
  home: "M3 11.5 12 4l9 7.5M5.5 10v9.5h13V10",
  calendar: "M4 6.5h16v13H4zM4 10.5h16M8.5 4v4M15.5 4v4",
  list: "M8 6.5h12M8 12h12M8 17.5h12M4 6.5h.01M4 12h.01M4 17.5h.01",
  bag: "M5 8h14l-1 12H6zM9 8V6.5a3 3 0 0 1 6 0V8",
  user: "M12 12.5a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4.5 20a7.5 7.5 0 0 1 15 0",
  bell: "M6 16.5V11a6 6 0 0 1 12 0v5.5l1.5 2h-15zM10 20.5a2 2 0 0 0 4 0",
  door: "M5 20.5V4.5h14v16M3 20.5h18M15 12.5h.01",
  spark: "M12 3.5l1.8 5.2 5.2 1.8-5.2 1.8L12 17.5l-1.8-5.2L5 10.5l5.2-1.8zM18.5 16l.8 2.2 2.2.8-2.2.8-.8 2.2-.8-2.2-2.2-.8 2.2-.8z",
  card: "M3 6.5h18v11H3zM3 10.5h18M7 14.5h4",
  receipt: "M6 3.5h12v17l-3-2-3 2-3-2-3 2zM9 8.5h6M9 12.5h6",
  star: "M12 4l2.4 5 5.6.8-4 3.9.9 5.5-4.9-2.6-4.9 2.6.9-5.5-4-3.9 5.6-.8z",
  logout: "M10 4.5H5v15h5M14 8l4 4-4 4M18 12H9",
  check: "M5 12.5l4.5 4.5L19 7.5",
  arrowLeft: "M11 6l-6 6 6 6M5 12h14",
} as const;

export type IconName = keyof typeof PATHS;

function Icon({ name, size = 22 }: { name: IconName; size?: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.9"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={PATHS[name]} />
    </svg>
  );
}

export default Icon;
