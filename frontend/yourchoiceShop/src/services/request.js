import axios from 'axios';
import { getToken, getCurrentUser } from './auth'; 

const request = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    timeout: 30000,
    withCredentials: true
});

request.interceptors.request.use(
    (config) => {
        const token = getToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`; 
        }
        
        const user = getCurrentUser();
        if (user) {
            const accountName = user.username || user.tenTaiKhoan || user.id; 
            if (accountName) {
                config.headers['X-Username'] = encodeURIComponent(accountName);
            }
        }

        if (config.data instanceof FormData) {
            delete config.headers['Content-Type'];
        } else {
            config.headers['Content-Type'] = 'application/json';
        }
        return config;
    },
    (error) => Promise.reject(error)
);

request.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            sessionStorage.clear(); 
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default request; 
