import React, {useEffect, useState} from "react";
import LogInModal from "./LogInModal";
import SignUpModal from "./SignUpModal";
import '../../styles/FormsAndModals.css';
import ResetPasswordModal from "./ResetPasswordModal";

type AuthModeProps = {
    mode : 'login' | 'signup' | 'reset';
    onClose: () => void;
    onSwitchMode: () => void;
    onResetMode: () => void;
    onBackToLogIn: () => void;
}

function AuthMode({ mode, onClose, onSwitchMode, onResetMode, onBackToLogIn }: AuthModeProps) {
    const [isClosing, setIsClosing] = useState(false);
    const [isAnimating, setIsAnimating] = useState<boolean>(false);

    useEffect(()=>{
        if (!isClosing) return;
        const timer = setTimeout(()=>{
            setIsClosing(false);
            onClose();
        },300);
        return () => {clearTimeout(timer);};
    },[isClosing, onClose]);

    useEffect(() => {
        if (mode) {
            const timer = setTimeout(()=>{setIsAnimating(true)},10);
            return () => clearTimeout(timer);
        } else  setIsAnimating(false);
    },[mode]);

    const handleClose = () => setIsClosing(true);

    return (
        <div className={`modal-backdrop${isClosing ? ' closing' : ''}`} onClick={handleClose}>
            <div className={`page-content ${isAnimating && !isClosing ? 'open' : ''} ${isClosing ? ' closing' : ''}`} onClick={e => e.stopPropagation()}>
                <button type='button' aria-label='Закрыть форму' className="onClose" onClick={handleClose}>✕</button>
                {mode === 'login' && (
                    <LogInModal onShowResetPage={onResetMode} onLogInSuccess={handleClose}/>
                )}
                {mode === 'signup' && (
                    <SignUpModal onSignUpSuccess={onSwitchMode}/>
                )}
                <p className='switch-link' onClick={onSwitchMode}>
                    {mode === 'login' && ('Нет аккаунта? Создать')}
                    {mode === 'signup' && ('Уже есть аккаунт? Войти')}
                </p>
                {mode === 'reset' && (
                    <ResetPasswordModal onBackToLogIn={onBackToLogIn}/>
                )}
            </div>
        </div>
    )
}
export default AuthMode;