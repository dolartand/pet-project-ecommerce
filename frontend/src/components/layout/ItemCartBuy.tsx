import React, {useState} from "react";
import {Delete02Icon} from 'hugeicons-react';
import '../../styles/ShoppingBag.css'

interface Product {
    id: number;
    productName: string;
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
    isDeleting: boolean;
}
function ItemCartBuy({item, onSelectionChange, isSelected, onQuantityChange, onDeleteItem, isDeleting}: ItemCartBuyProps) {

    return (
        <div className={`item-cart ${isDeleting ? 'item-hiding' : ''}`}>
            <img src={item.imageUrl} alt="фото товара"/>
            <div className='main-content'>
                <div className='to-buy-content'>
                    <p>{(item.price * item.quantity).toFixed(2)} BYN</p>
                    <p className='product-name'>{item.productName}</p>
                    <input type="checkbox" id={`chooseItem-${item.id}`} onChange={() => onSelectionChange(item.id)} checked={isSelected}/>
                </div>
                <div className='bottom-content'>
                    <div className='item-amount'>
                        <button className='btn change-amount' onClick={() => onQuantityChange(item.id, item.quantity - 1)} disabled={item.quantity===1}>-</button>
                        <p>{item.quantity}</p>
                        <button className='btn change-amount' onClick={() => onQuantityChange(item.id, item.quantity + 1)}>+</button>
                    </div>
                    <button className='btn delete-btn' onClick={() =>onDeleteItem(item.id)}>
                        <Delete02Icon size={20}/>
                    </button>
                </div>
            </div>
        </div>
    )
}

export default React.memo(ItemCartBuy)