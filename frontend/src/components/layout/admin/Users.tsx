import React, {useEffect, useState} from "react";
import api from "../../../api/axios";
import '../../../styles/admin/users.css';
import {formatDateFromArray} from "../../../utils/dateFormatter";

interface User {
    id: number,
    email: string,
    firstName: string,
    lastName: string,
    role: string,
    createdAt: number[],
    updatedAt: number[],
}

function Users () {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string>('');

    useEffect(() => {
        api.get('/admin/users')
        .then((res) => {
            console.log(res);
            setUsers(res.data.users);
        })
            .catch((err) => {
                console.error(err);
                setError(err.response.message);
            })
            .finally(()=> setLoading(false));
    },[])

    if (loading)    return <div>Loading...</div>
    if (users.length === 0) return <div>Пользователь нет.</div>;

    return (<div className='users-page'>
        {error && <p className='error-msg'>{error}</p>}
        <div className='users-header'>
            <p className='user-cell'>ID</p>
            <p className='user-cell-client'>Клиент</p>
            <p className='user-cell-email'>Email</p>
            <p className='user-cell-role'>ROLE</p>
            <p className='user-cell-date'>Дата создания</p>
            <p className='user-cell-update'>Дата обновления</p>
        </div>
        <div className='users'>
            {users.map(user => (
                <div key={user.id} className='user-container'>
                    <p className='user-cell'>ID: {user.id}</p>
                    <p className='user-cell-client'>${user.firstName} ${user.lastName}</p>
                    <p className='user-cell-email'>{user.email}</p>
                    <p className='user-cell-role'>{user.role}</p>
                    <p className='user-cell-date'>{formatDateFromArray(user.createdAt)}</p>
                    <p className='user-cell-update'>{user.updatedAt ? formatDateFromArray(user.updatedAt): 'не был'}</p>
                </div>
            ))}
        </div>
    </div>)
}
export default Users;