import axios from 'axios';

const request = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    timeout: 30000 
});

request.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token'); 
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
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default request;