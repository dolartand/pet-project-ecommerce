import React, {useEffect, useState} from "react";
import api from '../../api/axios';
import '../../styles/FormsAndModals.css'

interface ShippingAddress {
    shippingStreet: string;
    shippingCity: string;
    shippingPostalCode: string;
}
interface OrderInfo{
    address: ShippingAddress;
    comment: string;
}

type OrderFormModalProps = {
    onClose: () => void;
    onOrderSuccess: () => void;
    isOpen: boolean;
}

function OrderFormModal({onClose, onOrderSuccess, isOpen}: OrderFormModalProps) {
    const [orderInfo, setOrderInfo] = useState<OrderInfo>({
        address: {shippingStreet: '', shippingCity: '', shippingPostalCode: ''},
        comment: ''});
    const [error, setError] = useState<any>({});
    const [isClosing, setIsClosing] = useState<boolean>(false);
    const [successMsg, setSuccessMsg] = useState<string>('');
    const [isAnimating, setIsAnimating] = useState<boolean>(false);

    useEffect(()=>{
        if (!isClosing) return;
        const timer = setTimeout(()=>{
            setIsClosing(false);
            onClose();
        },300);
        return () => {clearTimeout(timer);};
    },[isClosing, onClose]);

    useEffect(() => {
        if (isOpen) {
            const timer = setTimeout(()=>{setIsAnimating(true)},10);
            return () => clearTimeout(timer);
        } else  setIsAnimating(false);
    },[isOpen]);

    const handleClose = () => setIsClosing(true);

    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = event.target;
        if (name === 'shippingStreet' || name === 'shippingCity' || name === 'shippingPostalCode') {
            setOrderInfo(prevState => ({
                ...prevState,
                address: {
                    ...prevState.address,
                    [name]: value
                }
            }));
        } else {
            setOrderInfo(prevState => ({
                ...prevState,
                [name]: value
            }));
        }
    }

    const handlePostOrder = () => {
        api.post('/orders', orderInfo)
            .then((response) => {
                setTimeout(() => onOrderSuccess(), 1500);
                setError({postErr:''});
                setSuccessMsg('Заказ успешно оформлен. Подробности заказа ищите во вкладке "История заказов"');
            })
            .catch(error => {
                console.log(error);
                setError({postErr: error.message});
            });
    }

    return (
        <div className={`modal-backdrop${isClosing ? ' closing' : ''}`} onClick={handleClose}>
            <div className={`page-content ${isAnimating && !isClosing ? 'open' : ''} ${isClosing ? ' closing' : ''}`} onClick={e => e.stopPropagation()}>
                <button type='button' aria-label='Закрыть форму' className="onClose" onClick={handleClose}>✕</button>
                <h3>Оформление заказа</h3>
                <label htmlFor="street" className='street'> Адрес доставки</label>
                <input type="text" id="street" value={orderInfo.address.shippingStreet}
                    onChange={handleInputChange} name="shippingStreet" />
                <label htmlFor="city" className='city'>Город доставки</label>
                <input type="text" id='city' value={orderInfo.address.shippingCity}
                    onChange={handleInputChange} name="shippingCity" />
                <label htmlFor="postalCode" className='postalCode'>Почтовый индекс</label>
                <input type="text" id="postalCode" value={orderInfo.address.shippingPostalCode}
                    onChange={handleInputChange} name="shippingPostalCode" />
                <label htmlFor="comment" className='comment'>Комментарий к заказу</label>
                <input type="text" id='comment' value={orderInfo.comment}
                    onChange={handleInputChange} name="comment" />
                <button type='button' className='submit-btn' onClick={handlePostOrder}>Оформить заказ</button>
                {error.postErr && (<div className='error-msg'>{error.postErr}</div>)}
                {!error.postErr && (<p className='success-msg'>{successMsg}</p>)}
            </div>
        </div>
    )
}
export default OrderFormModal;