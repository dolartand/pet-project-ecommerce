import React, {useEffect, useState} from 'react';
import api from '../../api/axios';
import '../../styles/FormsAndModals.css';

interface Item {
    productId: number;
    productName: string;
    quantity: number;
    priceAtTime: number;
}

interface ShippingAddress {
    shippingStreet: string;
    shippingCity: string;
    shippingPostalCode: string;
}

interface OrderDetails {
    id: number;
    status: string;
    totalAmount: number;
    items: Item[];
    address: ShippingAddress;
    comment: string;
}

type OrderDetailsModalProps = {
    orderId: number;
    onClose: () => void;
}

function OrderDetailsModal({ orderId, onClose,}: OrderDetailsModalProps) {
    const [orderDetails, setOrderDetails] = useState<OrderDetails>({
        id: 0,
        status: '',
        totalAmount: 0,
        items: [{
            productId:0,
            productName: '',
            quantity: 0,
            priceAtTime: 0
        }],
        address: {shippingStreet: '', shippingCity: '', shippingPostalCode: ''},
        comment: ''
    });
    const [error, setError] = useState<any>({});
    const [isClosing, setIsClosing] = useState<boolean>(false);
    const [successMsg, setSuccessMsg] = useState<string>('');

    useEffect(() => {
        api.get(`/orders/${orderId}`)
            .then((response) => {
            setOrderDetails(response.data);
        })
            .catch((error) => {
                console.log(error);
                setError({getOrderDetails: error.message});
            })
    }, []);
    useEffect(()=>{
        if (!isClosing) return;
        const timer = setTimeout(()=>{
            setIsClosing(false);
            onClose();
        },300);
        return () => {clearTimeout(timer);};
    },[isClosing, onClose]);

    const handleClose = () => setIsClosing(true);

    const handleCancelOrder = (orderId: number) => {

        setSuccessMsg('Заказ успешно отменен.');
    }

    return (
        <div className={`modal-backdrop${isClosing ? ' closing' : ''}`} onClick={handleClose}>
            <div className={`page-content${isClosing ? ' closing' : ''}`} onClick={e => e.stopPropagation()}>
                <button type='button' aria-label='Закрыть форму' className="onClose" onClick={handleClose}>✕</button>
                <h3>Детали заказа</h3>
                {error.getOrderDetails && (<div className='error-msg'>{error.getOrderDetails}</div>)}

                <label htmlFor="totalAmount" className='totalAmount'>Общая стоимость заказа</label>
                <input type="text" id="totalAmount" value={orderDetails.totalAmount} readOnly={true}/>

                {orderDetails.items.map(item =>(
                    <div className='item-info-detail' key={item.productId}>
                        <p>{item.productName} - </p>
                        <p>{item.priceAtTime}</p>
                        <p>кол-во: {item.quantity}</p>
                    </div>
                ))}

                <label htmlFor="street" className='street'>Полный адрес доставки</label>
                <input type="text" id="street" readOnly={true}
                       value={`${orderDetails.address.shippingStreet}, ${orderDetails.address.shippingCity}, ${orderDetails.address.shippingPostalCode}`}/>

                <label htmlFor="comment" className='comment'>Комментарий к заказу</label>
                <input type="text" id='comment' value={orderDetails.comment} readOnly={true} />

                <button type='button' className='cancel-btn' onClick={()=> handleCancelOrder(orderId)}>Отменить заказ</button>
                {error.cancelErr && (<div className='error-msg'>{error.cancelErr}</div>)}
                {!error.postErr && (<p className='success-msg'>{successMsg}</p>)}
            </div>
        </div>
    )
}
export default OrderDetailsModal;