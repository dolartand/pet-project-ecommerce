// файл для  глобального состояния аутентификации - хранилище
import React, {useContext, createContext, ReactNode, useState, useEffect} from 'react';
import api from "../api/axios";
import {getToken, setToken, subscribe} from "../api/tokenStore";

interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: 'USER' | 'ADMIN';
}

interface AuthData {
    accessToken: string;
    //refreshToken: string;
    user: User;
}

interface AuthContextType  {
    isLoggedIn: boolean,
    user: User | null,
    accessToken: string | null,
    logIn: (authData: AuthData) => void,
    logOut: () => Promise<void>,
    setAccessToken: (token: string | null) => void,
}

const AuthContext = createContext<AuthContextType>({
    isLoggedIn: false,
    user: null,
    accessToken: null,
    logIn: () => {},
    logOut: async () => {},
    setAccessToken: () => {},
});
// отдает значения всем дочерним компонентам
type AuthProviderProps = {
    children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [user, setUser] = useState<User | null>(null);
    const [accessToken, setAccessTokenState] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const unsubscribe = subscribe((token) => {
            setAccessTokenState(token);
            // Если токен был очищен (null), значит пользователь разлогинен
            if (token === null && isLoggedIn) {
                setIsLoggedIn(false);
                setUser(null);
            }
        });
        return () => {
            unsubscribe();
        }
    }, [isLoggedIn]);

    useEffect(() =>{
        const checkUserSession = async () => {
            try {
                // Проверяем, есть ли уже токен в памяти
                const currentToken = getToken();
                if (currentToken) {
                    // Если токен есть, пытаемся получить данные пользователя
                    try {
                        const response = await api.get('/users/profile');
                        const currentUser = response.data;
                        setIsLoggedIn(true);
                        setUser(currentUser);
                        setAccessTokenState(currentToken);
                        setLoading(false);
                        return;
                    } catch (profileError) {
                        // Если запрос с текущим токеном не прошел, попробуем refresh
                        console.log("Токен истек, пытаемся обновить...");
                    }
                }
                // Если токена нет или он недействителен, пытаемся refresh
                const refreshResponse = await api.post('/auth/refresh');
                const { accessToken } = refreshResponse.data;
                setToken(accessToken);
                
                // Получаем данные пользователя с новым токеном
                const response = await api.get('/users/profile');
                const currentUser = response.data;
                
                setIsLoggedIn(true);
                setUser(currentUser);
                setAccessTokenState(accessToken);
            } catch (err){
                console.log("Пользователь не авторизован или сессия истекла");
                // Очищаем состояние только если refresh тоже не удался
                logOut();
            } finally {
                setLoading(false);
            }
        };
        checkUserSession();
    }, []);

    const logIn = (authData: AuthData) => {
        setIsLoggedIn(true);
        setUser(authData.user);
        // setAccessTokenState(authData.accessToken);
        setToken(authData.accessToken);
    }

    const logOut = async () => {
        try {
            await api.post('/auth/logout');
        } catch (err) {
            console.log('Logout request failed:', err);
        } finally {
            // Очищаем состояние только после запроса (или если он не удался)
            setIsLoggedIn(false);
            setUser(null);
            setToken(null);
        }
    }

    const value = {isLoggedIn, user, accessToken, logIn, logOut, setAccessToken: setAccessTokenState};
    if (loading) {return <div>Загрузка...</div>}
    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}
export default AuthContext;