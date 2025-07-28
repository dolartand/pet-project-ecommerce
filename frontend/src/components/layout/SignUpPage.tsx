import React, {useState} from "react";
import '../../styles/Authentication.css';
import axios from 'axios';

function SignUpPage() {
    const [firstName, setFirstName] = useState<string>('');
    const [lastName,  setLastName]  = useState<string>('');
    const [email,     setEmail]     = useState<string>('');
    const [password,  setPassword]  = useState<string>('');
    const [error,     setError]     = useState<string>('');

    const clearAllFields = () => {
        setError('');
        setEmail('');
        setFirstName('');
        setLastName('');
        setPassword('');
    }

    const validate = () =>{
      setError('');
        if(!firstName.trim()){
            setError('Укажите, пожалуйста, имя');
            return false;
        }
        if (!lastName.trim()){
            setError('Укажите, пожалуйста, фамилию');
            return false;
        }
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
    };
    const handleSignUp = (e : React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!validate()) return;
        // axios.post(); добавление юзера
        clearAllFields();
    }
    return (
        <form onSubmit={handleSignUp}>
            <h3>Создать профиль</h3>
            <label htmlFor="firstName" className='formLabel'>Имя</label>
            <input type="text" id="firstName" value={firstName} onChange={(e) => setFirstName(e.target.value)} />
            <label htmlFor="lastName" className='formLabel'>Фамилия</label>
            <input type="text" id='lastName' value={lastName} onChange={(e) => setLastName(e.target.value)} />
            <label htmlFor="email" className='formLabel'>Почта</label>
            <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)}/>
            <label htmlFor="password" className='formLabel'>Пароль</label>
            <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)}/>
            {error && (<div className='error-msg'>{error}</div>)}
            <button type='submit' className='submit-btn'>Создать</button>
        </form>
    )
}
export default SignUpPage;