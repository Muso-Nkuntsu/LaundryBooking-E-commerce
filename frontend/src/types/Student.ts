export interface Student{
    studentId?: number;
    firstName : string;
    lastName: string;
    email: string;
    phoneNumber: string;
    password: string;
    createdAt?: string;
    emailVerified?: boolean;
    emailVerifiedAt?: string;
}