import React, {useEffect, useState} from "react";
import {useAuth} from "../../hooks/useAuth";
import api from "../../api/axios";
import '../../styles/FormsAndModals.css';

type ProfileModalProps = {
    handleClose: () => void;
}

function ProfileModal({handleClose}: ProfileModalProps) {
    const [firstName, setFirstName] = useState<string>('');
    const [lastName,  setLastName]  = useState<string>('');
    const [error, setError] = useState<any>({});
    const [readOnly, setReadOnly] = useState<boolean>(true);
    const [editBtnLabel, setEditBtnLabel] = useState<string>('Редактировать');
    const [unauthorized, setUnauthorized] = useState<boolean>(false);
    const {logOut,isLoggedIn} = useAuth();

    useEffect(() => {
        api.get('/users/profile')
            .then(res=>{
                setFirstName(res.data.firstName);
                setLastName(res.data.lastName);
                setError({});
            })
            .catch(error =>{
                console.log('Ошибка получения профиля ', error);
                if (error.response && (error.response.status === 401 || error.response.status === 403)) {
                    logOut().catch(console.error);
                } else {
                    setError({ form: 'Не удалось загрузить профиль. Попробуйте позже.'});
                }
            })
    },[isLoggedIn, logOut]);

    const validate = (): boolean => {
        const newError: any = {};
        if (!firstName.trim())  newError.firstName = 'Введите имя';
        if (!lastName.trim())   newError.lastName = 'Введите фамилию';
        setError(newError);

        return Object.keys(newError).length === 0;
    }

    const handleEditProfile = async() => {
        if(editBtnLabel==='Редактировать'){
            setReadOnly(false);
            setEditBtnLabel('Сохранить');
        } else{
            if(!validate()) return;
            try{
                await api.put('/users/profile',{firstName, lastName});
                setReadOnly(true);
                setEditBtnLabel('Редактировать');
                setError({});
            }catch(e){setError({form: 'Не удалось сохранить изменения. Попробуйте позже.'});}
        }
    }

    const handleLogOut = async () => {
        try {
            await logOut();
            setUnauthorized(true);
        }catch(e){ setError({form:'Не удалось выйти из профиля. Попробуйте позже'})}
    }

    return (
        <div>
            <button type='button' aria-label='Закрыть форму' className="onClose" onClick={handleClose}>✕</button>
            <h3>Профиль</h3>
            {error.form && (<div className='error-msg'>{error.form}</div>)}
            <label htmlFor="firstName" className='formLabel'>Имя</label>
            <input type="text" id="firstName" value={firstName} aria-readonly={readOnly} readOnly={readOnly}
                   onChange={(e) => setFirstName(e.target.value)} />
            {error.firstName && (<div className='error-msg'>{error.firstName}</div>)}
            <label htmlFor="lastName" className='formLabel'>Фамилия</label>
            <input type="text" id='lastName' value={lastName} aria-readonly={readOnly} readOnly={readOnly}
                   onChange={(e) => setLastName(e.target.value)} />
            {error.lastName && (<div className='error-msg'>{error.lastName}</div>)}
            <button type='button' className='edit-btn' onClick={handleEditProfile}>{editBtnLabel}</button>
            <button type='button' className='submit-btn' onClick={handleLogOut}>Выйти из аккаунта</button>
        </div>
    )
}
export default ProfileModal;