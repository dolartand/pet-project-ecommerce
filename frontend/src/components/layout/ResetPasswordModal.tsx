import React, {useState} from "react";
import api from "../../api/axios";
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
        api.post('/auth/forgot-password', email)
            .then(res => {
                alert(res.data.message);
                // потом поменяю на собственный компонент для сообщений
                setError('');
                setEmail('');
                setToken(res.data.token);
            })
        .catch(err => {
            setError(err.response?.data?.message || err.response?.data?.errors );
        })
    }

    const handleResetPassword = () => {
        api.post('/auth/reset-password',{token:token, newPassword:password, confirmPassword:confirmPassword})
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
                <label htmlFor="password">Новый пароль</label>
                <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)}/>
                <input type="text" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)}
                       placeholder="введите старый пароль"/>
                <button className='submit-btn' onClick={handleResetPassword}>Установить новый пароль</button>
            </>)}
            <p className='forgetPassword' onClick={onBackToLogIn}>Вернуться</p>
        </form>
    )
}
export default ResetPasswordModal;