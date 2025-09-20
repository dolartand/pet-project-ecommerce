import React, {useState} from "react";
import '../../styles/FormsAndModals.css';
import api from "../../api/axios";
import {useAuth} from "../../hooks/useAuth";

type LogInPageProps = {
    onShowResetPage: () => void;
    onLogInSuccess: () => void; // для закрытия окна
};

function LogInPage ({onShowResetPage, onLogInSuccess}: LogInPageProps)  {
    const [email, setEmail] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [error, setError] = useState<any>({});
    const {logIn} = useAuth();

    const clearAllFields = () => {
        setError({});
        setEmail('');
        setPassword('');
    }
    const validate = () : boolean => {
        const newError: any = {};
        if(!email.trim())
            newError.email = 'Пожалуйста, введите email';
        else{
            const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailPattern.test(email)){
                newError.email ='Некорректный формат email';
            }
        }
        if(!password)
            newError.password = 'Введите, пожалуйста, пароль';
        else if (password.length<8)
            newError.password ='Пароль должен содержать минимум 8 символов';

        setError(newError);
        return Object.keys(newError).length === 0;
    }
    const handleLogIn = async (e : React.FormEvent<HTMLFormElement>) =>{
        e.preventDefault();
       if (!validate()) return;
       try{
            const response = await api.post('auth/login', {email, password});
           clearAllFields();
           logIn(response.data);
           onLogInSuccess();
       } catch (err: any) {
           const errorData = err.response?.data;
           if(errorData?.validationErrors)
               setError(errorData?.validationErrors);
           else if (errorData?.message)
               setError({form: errorData .message});
           else
               setError({form: 'Ошибка. Попробуйте снова'});
       }
    }

    return (
        <form onSubmit={handleLogIn}>
            <h3>Вход в профиль</h3>
            {error.form && (<div className='error-msg'>{error.form}</div>)}
            <label htmlFor="email" className='formLabel'>Почта</label>
            <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)}/>
            {error.email && (<div className='error-msg'>{error.email}</div>)}
            <label htmlFor="password" className='formLabel'>Пароль</label>
            <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)}/>
            {error.password && (<div className='error-msg'>{error.password}</div>)}
            <p className='forgetPassword' onClick={onShowResetPage}>Забыли пароль?</p>
            <button type="submit" className='submit-btn'>Войти</button>
        </form>
    )
}
export default LogInPage;