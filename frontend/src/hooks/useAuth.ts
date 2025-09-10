import {useContext} from "react";
import AuthContext from "../context/AuthContext";

// custom hook чтобы использовать контекст в других компонентах
export function useAuth (){
    return useContext(AuthContext);
}