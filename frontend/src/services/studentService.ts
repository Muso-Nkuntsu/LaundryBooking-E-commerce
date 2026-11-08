import { apiGet, apiPost } from "./Api";
import type { Student } from "../types/Student";

export interface NewStudent {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
}

// The backend never sends the password back.
export type StudentAccount = Omit<Student, "password">;

export const createStudent = (student: NewStudent): Promise<StudentAccount> =>
  apiPost<StudentAccount>("/student/create", student);

export interface LoginResponse {
  /** Sent as "Authorization: Bearer <token>" on every request (Api.ts does this). */
  token: string;
  expiresInMs: number;
  student: StudentAccount;
}

/** Fails with status 401 when the email or password is wrong, 403 when the email is not verified yet. */
export const loginStudent = (email: string, password: string): Promise<LoginResponse> =>
  apiPost<LoginResponse>("/student/login", { email, password });

/** The logged-in student, according to the backend. */
export const getCurrentStudent = (): Promise<StudentAccount> => apiGet<StudentAccount>("/student/me");

export const updateStudent = (student: {
  studentId: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}): Promise<StudentAccount> => apiPost<StudentAccount>("/student/update", student);

export interface MessageResponse {
  message: string;
}

/** Confirms the email address using the token from the emailed link. Fails with status 400 when it is invalid or expired. */
export const verifyEmail = (token: string): Promise<MessageResponse> =>
  apiPost<MessageResponse>("/student/verify-email", { token });

/** Asks for a new verification email. Fails with status 429 when asked again too soon. */
export const resendVerificationEmail = (email: string): Promise<MessageResponse> =>
  apiPost<MessageResponse>("/student/resend-verification", { email });

/** Emails a password reset link. The reply is the same whether or not the email has an account. */
export const requestPasswordReset = (email: string): Promise<MessageResponse> =>
  apiPost<MessageResponse>("/student/forgot-password", { email });

/** Sets a new password with the token from the emailed link. Fails with status 400 when the link is invalid or expired. */
export const resetPassword = (token: string, newPassword: string): Promise<MessageResponse> =>
  apiPost<MessageResponse>("/student/reset-password", { token, newPassword });
