import React from "react";
import "../../styles/ItemCartMain.css";

interface Product {
    id: number;
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
}

function ItemCartMain({item}: ItemCartMainProps) {
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
            </div>
            <button className='to-cart'>В корзину</button>
        </div>
    )
}
export default ItemCartMain;