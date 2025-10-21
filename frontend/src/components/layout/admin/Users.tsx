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
        {users.map(user => (
            <div key={user.id} className='user-container'>
                <div className='user-info'>
                    <p>ID: {user.id}</p>
                    <p>${user.firstName} ${user.lastName}</p>
                    <p>ROLE: {user.role}</p>
                </div>
                <p>Почта: {user.email}</p>
                <div className='account-info'>
                    <p>Создан: {formatDateFromArray(user.createdAt)}</p>
                    <p>Обновлен: {user.updatedAt ? formatDateFromArray(user.updatedAt): 'не был'}</p>
                </div>
            </div>
        ))}
    </div>)
}
export default Users;