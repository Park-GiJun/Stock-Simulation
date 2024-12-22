// src/api/axios.js
import axios from 'axios';
import router from '../router';

// const api = axios.create({
//     // baseURL: 'http://localhost:9832/api',
//     baseURL: 'http://15.165.163.233:9832/api',
//     timeout: 10000,
//     headers: {
//         'Content-Type': 'application/json',
//     }
// });

const api = axios.create({
    // baseURL을 HTTPS로 수정
    baseURL: 'https://olm.life/api',  // 변경: https로 접속
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true  // 쿠키와 인증 정보를 포함하여 요청 보내기
});


api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => {
        return response.data;
    },
    async (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('accessToken');
            router.push('/login');
        }
        return Promise.reject(error);
    }
);

export default api;