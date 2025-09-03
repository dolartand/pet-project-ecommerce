//храним токен в памяти, изолированный от localStorage
let accessToken : string | null = null;
let  subscribers: ((token: string | null) => void) [] = []; // массив функций, которые должны знать о изменении токена

export const getToken = (): string | null => {
    return accessToken;
}
export const setToken = (token: string | null) => {
    accessToken = token;
    subscribers.forEach(callback => callback(token));
}

export const subscribe = (callback: (token: string | null) => void) => {
    subscribers.push(callback);
    // отписка от уведомлений
    return () => {
        subscribers = subscribers.filter(cb => cb !== callback);
    };
}