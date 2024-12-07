// src/api/axios.js
import axios from 'axios';
import router from '../router';

const api = axios.create({
    baseURL: 'http://localhost:9832/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    }
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