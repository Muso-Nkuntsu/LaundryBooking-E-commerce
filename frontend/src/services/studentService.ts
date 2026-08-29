import type { Student } from "../types/Student";

const API_URL = "http://localhost:8080/student";

export const createStudent = async (student: Student): Promise<Student> => {
  const response = await fetch(`${API_URL}/create`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(student),
  });

  if (!response.ok) {
    throw new Error("Failed to register student.");
  }

  return response.json();
};

export const getStudentById = async (studentId: number): Promise<Student> => {
  const response = await fetch(`${API_URL}/read/${studentId}`);

  if (!response.ok) {
    throw new Error("Student not found.");
  }

  return response.json();
};

export const updateStudent = async (student: Student): Promise<Student> => {
  const response = await fetch(`${API_URL}/update`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(student),
  });

  if (!response.ok) {
    throw new Error("Failed to update student.");
  }

  return response.json();
};

export const deleteStudent = async (studentId: number): Promise<boolean> => {
  const response = await fetch(`${API_URL}/delete/${studentId}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error("Failed to delete student.");
  }

  return true;
};

export const getAllStudents = async (): Promise<Student[]> => {
  const response = await fetch(`${API_URL}/getall`);

  if (!response.ok) {
    throw new Error("Failed to retrieve students.");
  }

  return response.json();
};