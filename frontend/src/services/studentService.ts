import { apiPost } from "./Api";
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

/** Fails with status 401 when the email or password is wrong. */
export const loginStudent = (email: string, password: string): Promise<StudentAccount> =>
  apiPost<StudentAccount>("/student/login", { email, password });

export const updateStudent = (student: {
  studentId: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}): Promise<StudentAccount> => apiPost<StudentAccount>("/student/update", student);
