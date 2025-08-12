import React, {useState} from "react";
import '../../styles/Authentication.css';
import axios from "axios";
import {useAuth} from "../../context/AuthContext";

type LogInPageProps = {
    onShowResetPage: () => void;
    onLogInSuccess: () => void; // для закрытия окна
};

function LogInPage ({onShowResetPage, onLogInSuccess}: LogInPageProps)  {
    const [email, setEmail] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [error, setError] = useState<string>('');
    const {logIn} = useAuth();

    const clearAllFields = () => {
        setError('');
        setEmail('');
        setPassword('');
    }
    const validate = () : boolean => {
        setError('');
        if(!email.trim()){
            setError('Пожалуйста, ведите email');
            return false;
        }
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailPattern.test(email)){
            setError('Некорректный формат email');
            return false;
        }
        if(!password){
            setError('Введите, пожалуйста, пароль');
            return false;
        }
        if (password.length<8){
            setError('Пароль должен содержать минимум 8 символов');
            return false;
        }
        setError('');
        return true;
    }
    const handleLogIn = (e : React.FormEvent<HTMLFormElement>) =>{
        e.preventDefault();
       if (!validate()) return;
        axios.post('http://localhost:8080/api/auth/login', {email, password})
            .then(res => {
                alert('Успешный вход в аккаунт');
                // потом поменяю на собственный компонент для сообщений
                clearAllFields();
                logIn();
                onLogInSuccess();
            })
            .catch(err => {
                setError(err.response?.data?.message || err.response?.data?.errors );
            })
    }

    return (
        <form onSubmit={handleLogIn}>
            <h3>Вход в профиль</h3>
            <label htmlFor="email" className='formLabel'>Почта</label>
            <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)}/>
            <label htmlFor="password" className='formLabel'>Пароль</label>
            <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)}/>
            {error && (<div className='error-msg'>{error}</div>)}
            <p className='forgetPassword' onClick={onShowResetPage}>Забыли пароль?</p>
            <button type="submit" className='submit-btn'>Войти</button>
        </form>
    )
}
export default LogInPage;