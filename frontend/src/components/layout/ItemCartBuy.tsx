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
    quantity: number;
}
type ItemCartBuyProps = {
    item: Product;
    isSelected: boolean;
    onSelectionChange: (itemId: number) => void;
    onQuantityChange: (itemId: number, newQuantity: number) => void;
    onDeleteItem: (itemId: number) => void;
}
function ItemCartBuy({item, onSelectionChange, isSelected, onQuantityChange, onDeleteItem}: ItemCartBuyProps) {

    return (
        <div className='item-cart'>
            <img src={item.imageUrl} alt="фото товара"/>
            <div className='main-content'>
                <div className='to-buy-content'>
                    <p>{(item.price * item.quantity).toFixed(2)} BYN</p>
                    <p className='product-name'>{item.name}</p>
                    <div className='round'>
                        <input type="checkbox" id={`chooseItem-${item.id}`} onChange={() => onSelectionChange(item.id)} checked={isSelected}/>
                        {/*<label htmlFor={`chooseItem-${item.id}`}></label>*/}
                    </div>

                </div>
                <p>{item.name}</p>
                <div className='bottom-content'>
                    <div className='item-amount'>
                        <button className='change-amount' onClick={() => onQuantityChange(item.id, item.quantity - 1)} disabled={item.quantity===1}>-</button>
                        <p>{item.quantity}</p>
                        <button className='change-amount' onClick={() => onQuantityChange(item.id, item.quantity + 1)}>+</button>
                    </div>
                    <button className='delete' onClick={() =>onDeleteItem(item.id)}>
                        <Delete02Icon size={20}/>
                    </button>
                </div>
            </div>
        </div>
    )
}

export default React.memo(ItemCartBuy)