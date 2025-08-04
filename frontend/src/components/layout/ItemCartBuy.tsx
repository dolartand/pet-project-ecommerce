import React, {useState} from "react";
import {Delete02Icon} from 'hugeicons-react';
import '../../styles/ShoppingBag.css'

interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    imageUrl: string;
    categoryId: number;
}
type ItemCartBuyProps = {
    item: Product;
    isSelected: boolean;
    onSelectionChange: (itemId: number) => void;
}
function ItemCartBuy({item, onSelectionChange, isSelected}: ItemCartBuyProps) {

    const [amount, setAmount] = useState<number>(1);

    const incrementAmount = () => {
        setAmount((prev) => prev + 1);
    }
    const decrementAmount = () => {
        if (amount > 1) {
            setAmount((prev) => prev - 1);
        }
    }
    return (
        <div className='item-cart'>
            <img src={item.imageUrl} alt="фото товара"/>
            <div className='main-content'>
                <div className='to-buy-content'>
                    <p>{item.price * amount}</p>
                    <div className='round'>
                        <input type="checkbox" id={`chooseItem-${item.id}`} onChange={() => onSelectionChange(item.id)} checked={isSelected}/>
                        <label htmlFor={`chooseItem-${item.id}`}></label>
                    </div>
                    <button className='buy-btn'>Купить</button>
                </div>
                <p>{item.name}</p>
                <div className='bottom-content'>
                    <div className='item-amount'>
                        <button className='change-amount' onClick={decrementAmount} disabled={amount===1}>-</button>
                        <p>{amount}</p>
                        <button className='change-amount' onClick={incrementAmount}>+</button>
                    </div>
                    <button className='delete'>
                        <Delete02Icon size={20}/>
                    </button>
                </div>
            </div>
        </div>
    )
}

export default React.memo(ItemCartBuy)