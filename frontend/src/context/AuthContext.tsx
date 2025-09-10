// файл для  глобального состояния аутентификации - хранилище
import React, {useContext, createContext, ReactNode, useState, useEffect} from 'react';
import api from "../api/axios";
import {setToken, subscribe} from "../api/tokenStore";

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
    logOut: () => void,
    setAccessToken: (token: string | null) => void,
}

const AuthContext = createContext<AuthContextType>({
    isLoggedIn: false,
    user: null,
    accessToken: null,
    logIn: () => {},
    logOut: () => {},
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
            setAccessTokenState(token)
        });
        return () => {
            unsubscribe();
        }
    }, []);

    useEffect(() =>{
        const checkAuth = async () => {
            try {
                const response = await api.post('/auth/refresh');
                logIn(response.data);
            } catch (err){
                console.log("Пользователь не авторизован");
            } finally {
                setLoading(false);
            }
        };
        checkAuth();
    }, []);

    const logIn = (authData: AuthData) => {
        setIsLoggedIn(true);
        setUser(authData.user);
        setAccessTokenState(authData.accessToken);
        setToken(authData.accessToken);
    }

    const logOut = () => {
        setIsLoggedIn(false);
        setUser(null);
        setAccessTokenState(null);
        setToken(null);
        api.post('/auth/logout')
            .then(res =>
                console.log(res.data.message))
            .catch(err => alert(err.message));
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