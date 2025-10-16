import React, {useState} from "react";
import {apiPublic} from "../../api/axios";
import '../../styles/FormsAndModals.css';

type ResetPasswordPageProps = {
    onBackToLogIn: () => void;
}

function ResetPasswordModal ({onBackToLogIn}: ResetPasswordPageProps) {
    const [email, setEmail] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [confirmPassword, setConfirmPassword] = useState<string>('');
    const [error, setError] = useState<string>('');
    const [token, setToken] = useState<string>('');
    const [success, setSuccess] = useState<string>('');

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
        setError('');
        return true;
    }

    const handleForgotPassword = (e : React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!validate()) return;
        apiPublic.post('/auth/forgot-password', {email})
            .then(res => {
                // alert(res.data.message);
                // потом поменяю на собственный компонент для сообщений
                setError('');
                setToken(res.data.token);
            })
        .catch(err => {
            setError(err.response?.data?.message || err.response?.data?.errors );
        })
    }

    const handleResetPassword = () => {
        const requestBody = {
            token: token,
            newPassword: password,
            confirmPassword: confirmPassword
        };
        apiPublic.post('/auth/reset-password', requestBody)
            .then((res) => {
                setSuccess(res.data.message);
                setError('');
        })
        .catch(err => {
            setError(err.response?.data?.message);
            console.log(err.response?.data?.message);
        })
    }

    return (
        <form onSubmit={handleForgotPassword}>
            <h3>Сброс пароль</h3>
            <label htmlFor="email" className='formLabel'>Почта</label>
            <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)}/>
            {error && (<div className='error-msg'>{error}</div>)}
            <button type='submit' className='submit-btn'>Сбросить</button>
            {token && (<>
                <label htmlFor="password" className='formLabel'>Новый пароль</label>
                <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)}/>
                <input type="text" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)}
                       placeholder="повторите пароль"/>
                <button className='submit-btn' onClick={handleResetPassword} type='button'>Установить новый пароль</button>
            </>)}
            <p className='forgetPassword' onClick={onBackToLogIn}>Вернуться</p>
        </form>
    )
}
export default ResetPasswordModal;