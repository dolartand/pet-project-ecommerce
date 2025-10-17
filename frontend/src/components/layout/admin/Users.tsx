import React, {useEffect, useState} from "react";
import api from "axios";

interface User {
    id: number,
    email: string,
    firstName: string,
    lastName: string,
    role: string,
    createdAt: string,
    updatedAt: string
}

function Users () {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string>('');

    useEffect(() => {
        api.get('/admin/users')
        .then((res) => {
            setUsers(res.data.content);
            alert('Got all users');
        })
            .catch((err) => {
                console.error(err);
                setError(err.response.message);
            })
            .finally(()=> setLoading(false));
    },[])

    if (loading)    return <div>Loading...</div>

    return (<div className='users-page'>
        {error && <p>{error}</p>}
        {users.map(user => (
            <div key={user.id} className='user-container'>
                <p>ID: {user.id}</p>
                <div className='user-info'>
                    <p>${user.firstName} ${user.lastName}</p>
                    <p>{user.email}</p>
                </div>
                <div className='account-info'>
                    <p>Создан: {user.createdAt}</p>
                    <p>Последнее обновление: {user.updatedAt}</p>
                </div>
            </div>
        ))}
    </div>)
}
export default Users;