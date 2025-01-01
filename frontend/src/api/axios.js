import axios from 'axios';
import router from '../router';

const api = axios.create({
    baseURL: 'https://olm.life/api',  // vite 프록시를 통해 요청이 전달됨
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
        'Origin': 'https://olm.life',
        'Referer': 'https://olm.life'
    },
    withCredentials: false
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        // 모든 요청에 Origin과 Referer 헤더 추가
        config.headers.Origin = 'https://olm.life';
        config.headers.Referer = 'https://olm.life';
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