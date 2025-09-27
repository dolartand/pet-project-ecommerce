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
        const checkUserSession = async () => {
            try {
                const response = await api.post('/users/profile');
                const currentUser = response.data;
                const currentToken = getToken();
                setIsLoggedIn(true);
                setUser(currentUser);
                setAccessTokenState(currentToken);
            } catch (err){
                console.log("Пользователь не авторизован");
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

    const logOut = () => {
        setIsLoggedIn(false);
        setUser(null);
        // setAccessTokenState(null);
        setToken(null);
        api.post('/auth/logout').catch(err => console.log(err.message));
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