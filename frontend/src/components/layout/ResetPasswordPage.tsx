import React, {useState} from "react";
import api from "../../api/axios";
import '../../styles/FormsAndModals.css';

type ResetPasswordPageProps = {
    onBackToLogIn: () => void;
}

function ResetPasswordPage ({onBackToLogIn}: ResetPasswordPageProps) {
    const [email, setEmail] = useState<string>('');
    const [error, setError] = useState<string>('');

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

    const handleResetPassword = (e : React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!validate()) return;
        api.post('/auth/forgot-password', email)
            .then(res => {
                alert(res.data.message);
                // потом поменяю на собственный компонент для сообщений
                setError('');
                setEmail('');
            })
        .catch(err => {
            setError(err.response?.data?.message || err.response?.data?.errors );
        })
    }
    return (
        <form onSubmit={handleResetPassword}>
            <h3>Сброс пароль</h3>
            <label htmlFor="email" className='formLabel'>Почта</label>
            <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)}/>
            {error && (<div className='error-msg'>{error}</div>)}
            <button type='submit' className='submit-btn'>Сбросить</button>
            <p className='forgetPassword' onClick={onBackToLogIn}>Вернуться</p>
        </form>
    )
}
export default ResetPasswordPage;