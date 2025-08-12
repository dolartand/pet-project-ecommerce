// файл для  глобального состояния аутентификации - хранилище
import React, {useContext, createContext, ReactNode, useState} from 'react';

type AuthContextType = {
    isLoggedIn: boolean,
    logIn: () => void,
    logOut: () => void,
}

const AuthContext = createContext<AuthContextType>({
    isLoggedIn: false,
    logIn: () => {},
    logOut: () => {},
});
// отдает значения всем дочерним компонентам
type AuthProviderProps = {
    children: ReactNode;
}
export function AuthProvider({ children }: AuthProviderProps) {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const logIn = () => setIsLoggedIn(true);
    const logOut = () => setIsLoggedIn(false);

    const value = {isLoggedIn, logIn, logOut};
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