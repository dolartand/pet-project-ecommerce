import axios from "axios";
import {getToken, setToken} from "./tokenStore";

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

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        if (error.response.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            try {
                const response = await api.post('/auth/refresh');
                const { accessToken } = response.data;
                setToken(accessToken);
                originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                return api(originalRequest);
            } catch (refreshError) {
                setToken(null);
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

export default api;