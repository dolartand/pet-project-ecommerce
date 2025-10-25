import React, {useEffect, useState} from "react";
import api from "../../../api/axios";
import {formatDateFromArray} from "../../../utils/dateFormatter";
import '../../../styles/admin/orders.css';
import OrderDetailsPanel from "./OrderDetailsPanel";

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

type Filter = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | null;

function Orders() {
    const [orders, setOrders] = useState<Order[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<any>({});
    const [filterStr, setFilterStr] = useState<Filter>(null);
    const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
    const [isAnimating, setIsAnimating] = useState<boolean>(false);
    const [isClosing, setIsClosing] = useState(false);

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

    useEffect(() => {
        if (selectedOrderId) {
            const timer = setTimeout(()=>{setIsAnimating(true)},10);
            return () => clearTimeout(timer);
        } else  setIsAnimating(false);
    },[selectedOrderId]);

    useEffect(()=>{
        if (!isClosing) return;
        const timer = setTimeout(()=>{
            setIsClosing(false);
            setSelectedOrderId(null);
        },300);
        return () => {clearTimeout(timer);};
    },[isClosing]);

    const handleOrderStatusUpdate = (orderId: number, newStatus:string) => {
        setOrders(currentOrder =>
        currentOrder.map(order => {
            if (order.id === orderId)   return {...order, orderStatus: newStatus};
            return order;
        }))
    }

    const handleToggleOpenOrder = (id: number) => {
        if (selectedOrderId === id) {handleCloseDetailsPanel()}
        else    setSelectedOrderId(id);
    }
    const handleCloseDetailsPanel = () => {setIsClosing(true);}

    if (loading)    return <div>Loading...</div>
    if (!loading && orders.length === 0) return <div>Заказов нет.</div>

    const selectedOrder = orders.find(o => o.id === selectedOrderId);

    return (<div className='orders-page'>
        <div className='orders-list-container '>
            <div className='filter'>
                <label htmlFor="sort">Показать заказы с статусом</label>
                <select name="sort" id="sort" value={filterStr ?? ''}
                        onChange={e=>setFilterStr(e.target.value as Filter)}>
                    <option value=''>ALL</option>
                    <option value="PENDING">PENDING</option>
                    <option value="CONFIRMED">CONFIRMED</option>
                    <option value="SHIPPED">SHIPPED</option>
                    <option value="DELIVERED">DELIVERED</option>
                    <option value="CANCELLED">CANCELLED</option>
                </select>
            </div>

            {error.getErr && <p className='error-msg'>{error.getErr}</p>}

            <div className='orders-table'>
                <div className='order-header'>
                    <p className='order-cell'>Заказ</p>
                    <p className='order-cell-client'>Клиент</p>
                    <p className='order-cell-status'>Статус</p>
                    <p className='order-cell-total'>Итого</p>
                    <p className='order-cell-date'>Дата</p>
                </div>
                {orders.map(order => (
                    <div key={order.id} className={`order-row ${order.id === selectedOrderId ? 'active' : ''}`}
                         onClick={() => handleToggleOpenOrder(order.id)}>
                        <p className='order-cell'>#{order.id}</p>
                        <p className='order-cell-client'>{order.userEmail}</p>
                        <div className='order-cell-status'>
                            <span className={`status-badge status-${order.orderStatus.toLowerCase()}`}>{order.orderStatus}</span>
                        </div>
                        <p className='order-cell-total'>{order.totalAmount.toFixed(2)} BYN</p>
                        <p className='order-cell-date'>{formatDateFromArray(order.createdAt)}</p>
                    </div>
                ))}
            </div>
        </div>
        {selectedOrder && (<OrderDetailsPanel onClose={handleCloseDetailsPanel}
        order={selectedOrder} onStatusUpdate={handleOrderStatusUpdate}
                                              isClosing={isClosing} isAnimating={isAnimating}/>)}
    </div>)
}

export default Orders;