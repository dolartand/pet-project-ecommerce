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
    refreshToken: string;
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
    const [accessToken, setAccessToken] = useState<string | null>(null);

    useEffect(() => {
        const unsubscribe = subscribe((token) => setAccessToken(token));
        return () => {
            unsubscribe();
        }
    }, []);

    const logIn = (authData: AuthData) => {
        setIsLoggedIn(true);
        setUser(authData.user);
        setAccessToken(authData.accessToken);
        setToken(authData.accessToken);
    }

    const logOut = () => {
        setIsLoggedIn(false);
        setUser(null);
        setAccessToken(null);
        setToken(null);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        api.post('/auth/logout')
            .then(res =>
                alert(res.data.message))
            .catch(err => alert(err.message));
    }

    const value = {isLoggedIn, user, accessToken, logIn, logOut, setAccessToken};
    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}
// custom hook чтобы использовать контекст в других компонентах
export function useAuth (){
    return useContext(AuthContext);
}