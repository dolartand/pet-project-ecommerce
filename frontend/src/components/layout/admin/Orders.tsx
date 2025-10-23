import React, {useEffect, useState} from "react";
import api from "../../../api/axios";
import {formatDateFromArray} from "../../../utils/dateFormatter";
import '../../../styles/admin/orders.css';

interface Item {
    productId: number;
    productName: string;
    quantity: number;
}

interface address {
    shippingStreet: string;
    shippingCity: string;
    shippingPostalCode: number;
}

interface Order {
    id: number,
    userId: number,
    userEmail: string,
    orderStatus: string,
    totalAmount: number,
    items: Item[],
    address: address,
    comment: string,
    createdAt: number[],
    updatedAt: number[],
}

type Filter = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | null;

function Orders() {
    const [orders, setOrders] = useState<Order[]>([{id:0, userId:0, userEmail: '', orderStatus: '', totalAmount:0, items:[], address: {
            shippingStreet: '', shippingCity: '', shippingPostalCode: 0,
        },comment:'', updatedAt:[],createdAt:[]}]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<any>({});
    const [filterStr, setFilterStr] = useState<Filter>(null);
    const [editingStatus, setEditingStatus] = useState<Filter>(null);
    const [orderStatusId, setOrderStatusId] = useState<number | undefined>(undefined);
    const [success, setSuccess] = useState<string>('');


    useEffect(()=>{
        api.get('/admin/orders',{params: {status:filterStr}})
            .then(res => {
                console.log(res.data);
                setOrders(res.data.content);
                setError({getErr:''});
            })
            .catch(err =>{
                console.log(err);
                setError({getErr: err.message});
            })
            .finally(()=>setLoading(false));
    }, [filterStr]);

    const handlePutOrderStatus = (id: number) => {
        const updatedOrder = orders.find(order => order.id === id);
        if (!updatedOrder) {
            setError({ putErr: 'Не удалось найти заказ для обновления.' });
            return;
        }

        api.put(`/admin/orders/${orderStatusId}`, {status:editingStatus})
            .then(res => {
                setError({});
                setSuccess('Статус успешно обновлен.');
                setOrders(currentOrders =>
                    currentOrders.map(o => o.id === id ? { ...o, status: editingStatus! } : o)
                );
                setOrderStatusId(undefined);
            }).catch(err => {
            console.log(err.message);
            setError({putErr:err.message});
        })
    }

    const handleSaveChange = (idOrder: number) => {
        handlePutOrderStatus(idOrder);
        setOrderStatusId(undefined);
    }

    if (loading)    return <div>Loading...</div>
    if (orders.length === 0) return <div>Заказов нет.</div>

    return (<div className='orders-page'>
        <div className='filter'>
            <label htmlFor="sort">Показать заказы с статусом</label>
            <select name="sort" id="sort" value={filterStr ?? 'DELIVERED'}
                    onChange={e=>setFilterStr(e.target.value as Filter)}>
                <option value="PENDING">PENDING</option>
                <option value="CONFIRMED">CONFIRMED</option>
                <option value="SHIPPED">SHIPPED</option>
                <option value="DELIVERED">DELIVERED</option>
                <option value="CANCELLED">CANCELLED</option>
            </select>
        </div>
        <div className='orders'>
            {error.getErr && <p className='error-msg'>{error.getErr}</p>}
            {orders.map(order => (
                <div key={order.id} className='order-container'>
                    <div className='order-date'>
                        <p>ID: {order.id}</p>
                        <p>Создан: {formatDateFromArray(order.createdAt)}</p>
                        <p>Обновлен: {formatDateFromArray(order.updatedAt)}</p>
                    </div>
                    <div className='main-info'>
                        <p>Клиент: {order.userId} - {order.userEmail}</p>
                        <p>Комментарий к заказу: {order.comment}</p>
                        <p className='total-amount'>Стоимость: {order.totalAmount}</p>
                    </div>
                    <div className='items'>
                        <p>Товары:</p>
                        {order.items.map(item => (
                            <p className='item-info'>{item.productName} - {item.quantity} шт.</p>
                        ))}
                        <h4 className='order-address'>Адрес заказа: {order.address.shippingStreet}, {order.address.shippingCity}, {order.address.shippingPostalCode}</h4>
                    </div>
                    <div>
                        {order.id === orderStatusId ? (<>
                                <label htmlFor="sort">Статус заказа: </label>
                                <select name="sort" id="sort" value={editingStatus ?? 'DELIVERED'}
                                        onChange={e=>setEditingStatus(e.target.value as Filter)}>
                                    <option value="PENDING">PENDING</option>
                                    <option value="CONFIRMED">CONFIRMED</option>
                                    <option value="SHIPPED">SHIPPED</option>
                                    <option value="DELIVERED">DELIVERED</option>
                                    <option value="CANCELLED">CANCELLED</option>
                                </select>
                                <button className='update-btn' onClick={() => handleSaveChange(order.id)}>Сохранить</button>
                                <button className='cancel-btn' onClick={()=> setOrderStatusId(undefined)}>Отменить</button>
                                {success && <p className='success-msg'>{success}</p>}
                                {error.deleteErr && <p className='error-msg'>{error.deleteErr}</p>}
                            </>)
                        : (<>
                                <p className='order-status'>Статус: {order.orderStatus}</p>
                                <button className='update-btn' onClick={()=> setOrderStatusId(order.id)} disabled={order.orderStatus === 'CANCELLED'}>Изменить статус</button>
                            </>)
                        }
                    </div>
                </div>
            ))}
        </div>
    </div>)
}

export default Orders;