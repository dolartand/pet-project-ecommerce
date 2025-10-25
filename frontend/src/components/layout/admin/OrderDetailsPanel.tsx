import React, {useEffect, useState} from "react";
import api from "../../../api/axios";
import {formatDateFromArray} from "../../../utils/dateFormatter";

interface Item {
    productId: number;
    productName: string;
    quantity: number;
    priceAtTime: number;
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

interface OrderDetailsPanelProps {
    onClose: () => void;
    onStatusUpdate: (orderId:number, newStatus: string) => void;
    order: Order;
    isAnimating: boolean;
    isClosing: boolean;
}

function OrderDetailsPanel({ onClose, order, onStatusUpdate, isAnimating, isClosing}: OrderDetailsPanelProps) {
    const [editingStatus, setEditingStatus] = useState<string | null>(order.orderStatus);
    const [error, setError] = useState<any>({});
    const [success, setSuccess] = useState<string>('');

    useEffect(() => {
        setEditingStatus(order.orderStatus);
    }, [order.id]);

    const handlePutOrderStatus = () => {
        setSuccess('');
        if (!order) {
            setError({ putErr: 'Не удалось найти заказ для обновления.' });
            return;
        }

        api.put(`/admin/orders/${order.id}`, {status:editingStatus})
            .then(res => {
                setError({});
                onStatusUpdate(order.id, editingStatus!);
                setSuccess('Статус изменен успешно');
            }).catch(err => {
            console.log(err.message);
            setError({putErr:err.message});
        })
    }

    return (<div className={`order-details-panel ${isClosing ? 'closing' : ''} ${isAnimating && !isClosing ? 'open' : ''}`}>
                <button className='close-panel-btn' onClick={onClose}>✕</button>
                <h3>Заказ #{order.id}</h3>
                <p>Дата: {formatDateFromArray(order.createdAt)}</p>

                <div className='client-info'>
                    <h4>Клиент</h4>
                    <p>{order.userEmail} (ID: {order.userId})</p>
                </div>

                <div className='order-items'>
                    <h4>Товары в заказе</h4>
                    {order.items.map(item => (
                        <div className='item-detail' key={item.productId}>
                            <p className='item-name'>{item.productName} ({item.quantity} шт)</p>
                            <p className='item-price'>{item.priceAtTime} BYN</p>
                        </div>
                    ))}
                </div>

                <div className='shipping-info'>
                    <h4>Адрес доставки</h4>
                    <p>{order.address.shippingStreet}, {order.address.shippingCity}, {order.address.shippingPostalCode}</p>
                </div>

                <div className='status-editor'>
                    <h4>Статус заказа</h4>
                    <select name="sort" id="sort" value={editingStatus ?? 'DELIVERED'}
                            onChange={e=>setEditingStatus(e.target.value)}>
                        <option value="PENDING">PENDING</option>
                        <option value="CONFIRMED">CONFIRMED</option>
                        <option value="SHIPPED">SHIPPED</option>
                        <option value="DELIVERED">DELIVERED</option>
                        <option value="CANCELLED">CANCELLED</option>
                    </select>
                    <button className='update-btn' onClick={handlePutOrderStatus}
                            disabled={order.orderStatus === 'CANCELLED'}>Сохранить</button>
                    <button className='cancel-btn' onClick={()=> setEditingStatus(order.orderStatus)}>Отменить</button>
                    {error.putErr && <p className='error-msg'>{error.putErr}</p>}
                    {success && <p className='success-msg'>{success}</p>}
                </div>
    </div>
    )
}

export default OrderDetailsPanel;