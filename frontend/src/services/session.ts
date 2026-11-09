// Remembers who is logged in, in the browser's localStorage.
// /student/login returns a token and the student; the token is sent with every request (see Api.ts).

const KEY = "laundry.student";

export type Role = "USER" | "ADMIN";

export interface SessionStudent {
  studentId: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: Role;
  token: string;
}

interface StudentDetails {
  studentId?: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role?: Role;
}

export function getSession(): SessionStudent | null {
  try {
    const raw = localStorage.getItem(KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as Partial<SessionStudent>;
    // A session saved before tokens were added has no token, so the student logs in again.
    if (typeof parsed.studentId !== "number" || typeof parsed.token !== "string") return null;
    return parsed as SessionStudent;
  } catch {
    return null;
  }
}

/** Saves the student after logging in, or after a profile update (then the existing token is kept). */
export function saveSession(student: StudentDetails, token?: string): void {
  const savedToken = token ?? getSession()?.token;
  if (typeof student.studentId !== "number" || !savedToken) return;
  const session: SessionStudent = {
    studentId: student.studentId,
    firstName: student.firstName,
    lastName: student.lastName,
    email: student.email,
    phoneNumber: student.phoneNumber,
    role: student.role ?? getSession()?.role ?? "USER",
    token: savedToken,
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

export function getToken(): string | null {
  return getSession()?.token ?? null;
}

export function isLoggedIn(): boolean {
  return getSession() !== null;
}

/** Only for showing or hiding admin links. The backend checks the role on every admin request. */
export function isAdmin(): boolean {
  return getSession()?.role === "ADMIN";
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
