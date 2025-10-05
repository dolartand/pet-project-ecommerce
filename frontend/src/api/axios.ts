import axios from "axios";
import {getToken, setToken, notifyTokenRefresh, hasSession} from "./tokenStore";

const api = axios.create({
    baseURL: process.env.REACT_APP_API_URL,
    withCredentials: true,
});

api.interceptors.request.use((config) => {
    const token = getToken();
    if (token)
        config.headers.Authorization = `Bearer ${token}`;
    return config;
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: (value: any) => void, reject: (reason?: any) => void }> = [];

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        const publicEndpoints = ['/auth/login', '/auth/register', '/auth/refresh'];
        if (error.response.status === 401 && !originalRequest._retry && !publicEndpoints.includes(originalRequest.url)) {
            // Если не знаем про серверную сессию (куки очищены), не пытаемся рефрешить
            if (!hasSession()) {
                return Promise.reject(error);
            }
            // если запрос уже 1 отправлен, остальные ставим в очередь
            if (isRefreshing) {
                return new Promise(function (resolve, reject) {
                    failedQueue.push({resolve, reject});
                }).then(token => {
                    originalRequest.headers['Authorization'] = 'Bearer ' + token;
                    return api(originalRequest);
                });
            }
            originalRequest._retry = true; // говорит о том что была попытка обновить токен
            isRefreshing = true;
            try{
                const response = await api.post('/auth/refresh');
                const { accessToken } = response.data;
                // Используем notifyTokenRefresh для уведомления всех подписчиков
                notifyTokenRefresh(accessToken);
                originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                processQueue(null, accessToken);
                return api(originalRequest);
            }catch(refreshError) {
                // Если refresh не удался, очищаем токен и уведомляем подписчиков
                setToken(null);
                processQueue(refreshError, null);
                return Promise.reject(refreshError);
            }finally {
                isRefreshing = false;
            }
        }
        return Promise.reject(error);
    }
);

export default api;