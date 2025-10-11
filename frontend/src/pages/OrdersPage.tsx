import React, {useEffect, useState} from "react";
import api from "../api/axios";
import OrderFormModal from "../components/layout/OrderFormModal";
import OrderDetailsModal from "../components/layout/OrderDetailsModal";

interface Item {
    productName: string;
    quantity: number;
}

interface Order {
    id: number;
    totalAmount: number;
    items: Item[];
    comment: string;
}

function OrderPage() {
    const [orders, setOrders] = useState<Order[]>([]);
    const [isOrderDetailsOpen, setIsOrderDetailsOpen] = useState(false);
    const [idOrder, setIdOrder] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string>('');

    useEffect(() => {
        api.get('/orders')
            .then(res => {
                setOrders(res.data.content);
                setError('');
            })
            .catch(error=>{
                setError(error.message);
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

    if (loading)    return <div>Loading...</div>
    if (orders.length === 0) return <div>Ваша история заказов пуста</div>;
    if (error)    return <div className='error-msg'>{error}</div>;

    return (
        <div>
            {orders.map(order => (
                <div key={order.id} className='order-container'>
                    <h3 className='total-amount'>{order.totalAmount}</h3>
                    <div className='items-info'>
                        {order.items.map((item: Item) => (
                            <div className='item'>
                                <p>{item.productName}</p>
                                <p>{item.quantity}</p>
                            </div>
                        ))}
                        <p className='order-comment'>{order.comment}</p>
                        <button className='get-more-info' onClick={()=> handleOpenOrder(order.id)}>Подробнее</button>
                    </div>
                </div>
            ))}
            {isOrderDetailsOpen && (<OrderDetailsModal onClose={handleCloseOrderForm} orderId={idOrder}/>)}
        </div>
    )
}

export default OrderPage;