import axios from 'axios';
import { getToken, logout, isAuthenticated, touchSession } from './auth';

const request = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    timeout: 30000 
});

request.interceptors.request.use(
    (config) => {
        // Kiểm tra phiên còn hiệu lực trước mỗi request
        const token = getToken();
        if (token) config.headers.Authorization = `Bearer ${token}`;

        // Xóa Content-Type nếu là FormData để browser tự xử lý
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
        // Gia hạn phiên mỗi khi có response thành công
        touchSession();
        return response;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            // Xoá toàn bộ auth state (token + role + user + loginTime)
            logout();
            window.location.href = '/client/login';
        }
        return Promise.reject(error);
    }
);

export default request;