// Remembers who is logged in, in the browser's localStorage.
// The student comes from the backend's /student/login response.

const KEY = "laundry.student";

export interface SessionStudent {
  studentId: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}

export function getSession(): SessionStudent | null {
  try {
    const raw = localStorage.getItem(KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as Partial<SessionStudent>;
    return typeof parsed.studentId === "number" ? (parsed as SessionStudent) : null;
  } catch {
    return null;
  }
}

export function saveSession(student: { studentId?: number; firstName: string; lastName: string; email: string; phoneNumber: string }): void {
  if (typeof student.studentId !== "number") return;
  const session: SessionStudent = {
    studentId: student.studentId,
    firstName: student.firstName,
    lastName: student.lastName,
    email: student.email,
    phoneNumber: student.phoneNumber,
  };
  try {
    localStorage.setItem(KEY, JSON.stringify(session));
  } catch {
    // Storage can be unavailable in private windows; the student will need to log in again.
  }
}

export function clearSession(): void {
  try {
    localStorage.removeItem(KEY);
  } catch {
    // Nothing to clear.
  }
}

export function isLoggedIn(): boolean {
  return getSession() !== null;
}

/** Only call on pages behind RequireLogin, where a session always exists. */
export function getStudentId(): number {
  return getSession()?.studentId ?? 0;
}

export function getDisplayName(): string {
  return getSession()?.firstName || "there";
}

export function getInitials(): string {
  const session = getSession();
  const first = session?.firstName?.[0] ?? "S";
  const last = session?.lastName?.[0] ?? "";
  return `${first}${last}`.toUpperCase();
}
