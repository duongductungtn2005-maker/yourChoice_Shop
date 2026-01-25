import axios from 'axios';

// Cấu hình base URL khớp với cổng Backend Spring Boot của bạn
const request = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    headers: {
        'Content-Type': 'application/json'
    }
});

export default request;