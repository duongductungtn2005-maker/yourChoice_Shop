import axios from 'axios';
// ADDED: Imported 'logout' from your auth file
import { getToken, getCurrentUser, logout, isAuthenticated, touchSession } from './auth'; 

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
        // Gia hạn phiên mỗi khi có response thành công
        touchSession();
        return response;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            // Xác định role để redirect về đúng trang login
            const role = sessionStorage.getItem('userRole');
            logout(); // Now this will work correctly!
            if (role === 'ADMIN' || role === 'STAFF') {
                window.location.href = '/admin/login';
            } else {
                window.location.href = '/client/login';
            }
        }
        return Promise.reject(error);
    }
);

export default request; 
