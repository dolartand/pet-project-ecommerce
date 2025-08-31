import React, {useState} from "react";
import "../../styles/ItemCartMain.css";
import {useAuth} from "../../context/AuthContext";
import api from "../../api/axios";

interface Product {
    productId: number;
    name: string;
    description: string;
    price: number;
    imageUrl: string;
    categoryId: number;
    rating: number;
    reviewCount: number;
    available: boolean;
}
type ItemCartMainProps = {
    item: Product;
    onLogInRequired: () => void;
}

function ItemCartMain({item, onLogInRequired}: ItemCartMainProps) {
    const {isLoggedIn} = useAuth();
    const [error, setError] = useState<string>('');

    const handleAddToCart = () => {
        if (isLoggedIn) {
            api.post('/cart/items', {productId: item.productId,quantity: 1})
            .then(res => {
                alert(`Товар "${item.name}" добавлен в корзину.`);
            })
            .catch(err => {
                setError(err.response?.data?.message || err.response?.data?.errors );
            })
        } else{
            onLogInRequired();
        }
    }

    return (
        <div className='item-container'>
            <img src={item.imageUrl} alt="фото"/>
            <div className='item-content'>
                <p className='item-price'>{item.price}</p>
                <p><span>{item.name}</span><span className='item-description'>  / {item.description}</span></p>
                <div className='item-review'>
                    <p className='rating'>⭐{item.rating}</p>
                    <p className='reviewCount'>{item.reviewCount} оценок</p>
                </div>
                {error && (<div className='error-msg'>{error}</div>)}
            </div>
            <button className='to-cart' onClick={handleAddToCart}>В корзину</button>
        </div>
    )
}
export default ItemCartMain;