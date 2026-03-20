import axios from 'axios';
import { getToken, getCurrentUser } from './auth'; // IMPORT THÊM getCurrentUserName

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
        
        // --- SỬA ĐOẠN LẤY USERNAME Ở ĐÂY ---
        const user = getCurrentUser();
        if (user) {
            // Chỉ lấy tên tài khoản (thường là username hoặc tenTaiKhoan tùy Backend của bạn trả về)
            // Dùng encodeURIComponent để phòng hờ trường hợp có ký tự lạ
            const accountName = user.username || user.tenTaiKhoan || user.id; 
            if (accountName) {
                config.headers['X-Username'] = encodeURIComponent(accountName);
            }
        }
        // -----------------------------------

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
        // Xử lý khi bị lỗi 401 (Hết hạn token hoặc token sai)
        if (error.response && error.response.status === 401) {
            // Sửa lại thành sessionStorage cho đồng bộ với auth.js của bạn
            sessionStorage.clear(); 
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default request;