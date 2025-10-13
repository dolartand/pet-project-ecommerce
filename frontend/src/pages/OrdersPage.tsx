import React, {useEffect, useState} from "react";
import api from "../api/axios";
import OrderDetailsModal from "../components/layout/OrderDetailsModal";
import '../styles/OrdersPage.css';

interface Item {
    productName: string;
    quantity: number;
}

interface Order {
    id: number;
    orderStatus: string;
    totalAmount: number;
    items: Item[];
    comment: string;
}

function OrderPage() {
    const [orders, setOrders] = useState<Order[]>([]);
    const [isOrderDetailsOpen, setIsOrderDetailsOpen] = useState(false);
    const [idOrder, setIdOrder] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<any>({});
    const [deletingOrderId, setDeletingOrderId] = useState<number>();

    useEffect(() => {
        api.get('/orders')
            .then(res => {
                setOrders(res.data.content);
                setError('');
            })
            .catch(error=>{
                setError({getOrdersErr:error.message});
                console.log('Ошибка получения заказов ', error);
                setOrders([]);
            })
            .finally(()=> setLoading(false));
    }, []);

    const handleOpenOrder = (id: number) => {
        setIdOrder(id);
        setIsOrderDetailsOpen(true);
    }
    const handleCloseOrderForm = () => {setIsOrderDetailsOpen(false);}

    const handleCancelOrder = (id: number) => {
        setDeletingOrderId(id);
        setTimeout(()=> {
            setOrders(current => current.filter(item => item.id !== id));
            api.put(`/orders/${id}/cancel`)
                .then(res => {
                    setOrders(currentOrders =>
                        currentOrders.map(order => {
                            if (order.id === id) {
                                return { ...order, orderStatus: 'CANCELLED' };
                            }
                            return order;
                        })
                    );
                })
                .catch(error=>{
                    console.log(error);
                    setError({cancelOrderErr:error.message});
                })
        }, 300);
    }

    if (loading)    return <div>Loading...</div>
    if (orders.length === 0) return <div>Ваша история заказов пуста</div>;
    if (error.getOrdersErr)    return <div className='error-msg'>{error}</div>;

    const sortedOrders = [...orders].sort(function (a,b){
        if (a.orderStatus === 'CANCELLED' && b.orderStatus !== "CANCELLED") return 1;
        if (a.orderStatus !== 'CANCELLED' && b.orderStatus === "CANCELLED") return -1;
        return 0;
    });

    return (
        <div className="orders-list">
            {sortedOrders.map(order => (
                <div key={order.id} className={`order-container ${order.id===deletingOrderId ? 'order-hiding' : ''}`}>
                    <div className='order-info'>
                        <div className='items-info'>
                            {order.items.map((item: Item) => (
                                <div className='item'>
                                    <p>{item.productName}</p>
                                    <p>(кол-во: {item.quantity})</p>
                                </div>
                            ))}
                            <p>Комментарий к заказу:</p>
                            <p className='order-comment'>{order.comment}</p>
                        </div>
                        <div className='btn-section'>
                            <button className='get-more-info' onClick={()=> handleOpenOrder(order.id)}>Подробнее</button>
                            <button type='button' className='cancel-btn' onClick={()=> handleCancelOrder(order.id)}
                                    disabled={order.orderStatus === 'CANCELLED'}>Отменить заказ</button>
                            {error.cancelOrderErr && (<p className='error-msg'>{error.cancelOrderErr}</p>)}
                            {order.orderStatus === 'CANCELLED' && (<p className='cancelledStatus'>Заказ отменен</p>)}
                        </div>
                    </div>
                    <h3 className='total-amount'>К оплате: <strong>{order.totalAmount}</strong> BYN</h3>
                </div>
            ))}
            {isOrderDetailsOpen && (<OrderDetailsModal onClose={handleCloseOrderForm} orderId={idOrder}/>)}
        </div>
    )
}

export default OrderPage;