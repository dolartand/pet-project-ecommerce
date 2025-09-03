// файл для  глобального состояния аутентификации - хранилище
import React, {useContext, createContext, ReactNode, useState, useEffect} from 'react';
import api from "../api/axios";

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
}

const AuthContext = createContext<AuthContextType>({
    isLoggedIn: false,
    user: null,
    accessToken: null,
    logIn: () => {},
    logOut: () => {},
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
        const storedUser = localStorage.getItem('user');
        const storedAccessToken = localStorage.getItem('accessToken');
        if(storedAccessToken && storedUser) {
            setUser(JSON.parse(storedUser));
            setAccessToken(storedAccessToken);
            setIsLoggedIn(true);
        }
    }, []);

    const logIn = (authData: AuthData) => {
        setIsLoggedIn(true);
        setUser(authData.user);
        setAccessToken(authData.accessToken);
        localStorage.setItem('accessToken', authData.accessToken);
        // localStorage.setItem('refreshToken', authData.refreshToken);
        localStorage.setItem('user', JSON.stringify(authData.user));
    }

    const logOut = () => {
        setIsLoggedIn(false);
        setUser(null);
        setAccessToken(null);
        localStorage.removeItem('accessToken');
        // localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        api.post('/auth/logout')
            .then(res =>
                alert(res.data.message))
            .catch(err => alert(err.message));
    }

    const value = {isLoggedIn, user, accessToken, logIn, logOut};
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