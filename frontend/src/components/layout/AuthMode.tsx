import React, {useEffect, useState} from "react";
import LogInPage from "./LogInPage";
import SignUpPage from "./SignUpPage";
import '../../styles/Authentication.css';

type AuthModeProps = {
    mode : 'login' | 'signup';
    onClose: () => void;
    onSwitchMode: () => void;
}

function AuthMode({ mode, onClose, onSwitchMode }: AuthModeProps) {
    const [isClosing, setIsClosing] = useState(false);
    useEffect(()=>{
        if (!isClosing) return;
        const timer = setTimeout(()=>{
            setIsClosing(false);
            onClose();
        },300);
        return () => {clearTimeout(timer);};
    },[isClosing, onClose]);

    const handleClose = () => setIsClosing(true);

    return (
        <div className={`modal-backdrop${isClosing ? ' closing' : ''}`} onClick={handleClose}>
            <div className={`page-content${isClosing ? ' closing' : ''}`} onClick={e => e.stopPropagation()}>
                <button type='button' aria-label='Закрыть форму' className="onClose" onClick={handleClose}>✕</button>
                {mode === 'login' ? (
                    <LogInPage/>
                ):(
                    <SignUpPage/>
                )}
                <p className='switch-link' onClick={onSwitchMode}>
                    {mode === 'login' ?
                        'Нет аккаунта? Создать' :
                        'Уже есть аккаунт? Войти'}
                </p>
            </div>
        </div>
    )
}
export default AuthMode;