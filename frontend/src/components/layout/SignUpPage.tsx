import React, {useState} from "react";
import '../../styles/FormsAndModals.css';
import api from "../../api/axios";

type SignUpPageProps = {
    onSignUpSuccess: () => void;
}

function SignUpPage({ onSignUpSuccess }: SignUpPageProps) {
    const [firstName, setFirstName] = useState<string>('');
    const [lastName,  setLastName]  = useState<string>('');
    const [email,     setEmail]     = useState<string>('');
    const [password,  setPassword]  = useState<string>('');
    const [error,     setError]     = useState<any>({});
    const [successMsg, setSuccessMsg] = useState<string>('');

    const clearAllFields = () => {
        setError({});
        setEmail('');
        setFirstName('');
        setLastName('');
        setPassword('');
    }

    const validate = () =>{
      const newError: any = {};
        if(!firstName.trim())
            newError.firstName ='Укажите, пожалуйста, имя';
        if (!lastName.trim())
            newError.lastName ='Укажите, пожалуйста, фамилию';
        if(!email.trim())
            newError.email ='Пожалуйста, ведите email';
        else {
            const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailPattern.test(email)){
                newError.email = 'Некорректный формат email';
            }
        }
        if(!password){
            newError.password = 'Введите, пожалуйста, пароль';
        } else if (password.length<8){
            newError.password = 'Пароль должен содержать минимум 8 символов';
        }
        setError(newError);
        return Object.keys(newError).length === 0;
    };
    const handleSignUp = async (e : React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!validate()) return;
        setSuccessMsg('');
        setError({});
        try{
            const response = await api.post('/auth/register', {email, password, firstName, lastName});
            clearAllFields();
            setSuccessMsg(response.data.message + 'Регистрация прошла успешно');
            setTimeout(() =>{
                onSignUpSuccess();
            }, 1500);
        } catch(err: any){
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
        <form onSubmit={handleSignUp}>
            <h3>Создать профиль</h3>
            {error.form && (<div className='error-msg'>{error.form}</div>)}
            {successMsg && (<div className='success-msg'>{successMsg}</div>)}
            <label htmlFor="firstName" className='formLabel'>Имя</label>
            <input type="text" id="firstName" value={firstName} onChange={(e) => setFirstName(e.target.value)} />
            {error.firstName && (<div className='error-msg'>{error.firstName}</div>)}
            <label htmlFor="lastName" className='formLabel'>Фамилия</label>
            <input type="text" id='lastName' value={lastName} onChange={(e) => setLastName(e.target.value)} />
            {error.lastName && (<div className='error-msg'>{error.lastName}</div>)}
            <label htmlFor="email" className='formLabel'>Почта</label>
            <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)}/>
            {error.email && (<div className='error-msg'>{error.email}</div>)}
            <label htmlFor="password" className='formLabel'>Пароль</label>
            <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)}/>
            {error.password && (<div className='error-msg'>{error.password}</div>)}
            <button type='submit' className='submit-btn'>Создать</button>
        </form>
    )
}
export default SignUpPage;