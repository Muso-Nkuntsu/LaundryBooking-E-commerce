import axios from "axios";

const API_URL = "http://localhost:8080/student";

export const createStudent = async(studentData: 
    {
        firstName: string;
        lastName:string;
        email: string;
        phoneNumber: string;
        password: string;

    }) =>{
         const response = await axios.post(
            '${API_URL}/create', 
            studentData
        );
    return response.data;
};