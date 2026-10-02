// Remembers who is signed in, in the browser's localStorage.
// NOTE: the backend has no login endpoint yet, so this is a stand-in.
// Replace it with real authentication (and remove FALLBACK_STUDENT_ID) once that exists.

const KEY = "laundry.student";
const FALLBACK_STUDENT_ID = 1;

export interface SessionStudent {
  studentId?: number;
  firstName?: string;
  lastName?: string;
  email: string;
  phoneNumber?: string;
}

export function getSession(): SessionStudent | null {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as SessionStudent) : null;
  } catch {
    return null;
  }
}

export function saveSession(student: SessionStudent): void {
  try {
    localStorage.setItem(KEY, JSON.stringify(student));
  } catch {
    // Storage can be unavailable in private windows; the app still works without it.
  }
}

export function clearSession(): void {
  try {
    localStorage.removeItem(KEY);
  } catch {
    // Nothing to clear.
  }
}

export function getStudentId(): number {
  return getSession()?.studentId ?? FALLBACK_STUDENT_ID;
}

export function getDisplayName(): string {
  const session = getSession();
  if (session?.firstName) return session.firstName;
  if (session?.email) return session.email.split("@")[0];
  return "there";
}

export function getInitials(): string {
  const session = getSession();
  const first = session?.firstName?.[0] ?? session?.email?.[0] ?? "S";
  const last = session?.lastName?.[0] ?? "";
  return `${first}${last}`.toUpperCase();
}
