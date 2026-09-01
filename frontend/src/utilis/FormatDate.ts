export const formatWeekday = (isoDate: string): string => {
  const date = new Date(`${isoDate}T00:00:00`);
  return date.toLocaleDateString("en-ZA", { weekday: "short" });
};

export const formatDayNumber = (isoDate: string): string => {
  const date = new Date(`${isoDate}T00:00:00`);
  return date.toLocaleDateString("en-ZA", { day: "2-digit" });
};


export const formatMonth = (isoDate: string): string => {
  const date = new Date(`${isoDate}T00:00:00`);
  return date.toLocaleDateString("en-ZA", { month: "short" });
};

export const formatFullDate = (isoDate: string): string => {
  const date = new Date(`${isoDate}T00:00:00`);
  return date.toLocaleDateString("en-ZA", {
    weekday: "long",
    day: "numeric",
    month: "long",
  });
};


export const formatTime = (time: string): string => {
  const [hoursStr, minutesStr] = time.split(":");
  const hours = Number(hoursStr);
  const period = hours >= 12 ? "PM" : "AM";
  const displayHours = hours % 12 === 0 ? 12 : hours % 12;
  return `${displayHours}:${minutesStr} ${period}`;
};

export const formatTimeRange = (startTime: string, endTime: string): string =>
  `${formatTime(startTime)} \u2013 ${formatTime(endTime)}`;

export const isToday = (isoDate: string): boolean => {
  const today = new Date().toISOString().slice(0, 10);
  return isoDate === today;
};