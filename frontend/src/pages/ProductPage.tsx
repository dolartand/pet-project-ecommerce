import React, {useEffect, useState} from "react";
import api from "../api/axios";
import {useAuth} from "../hooks/useAuth";

type ProductPageProps = {
    productId:number;
    onClose: () => void;
}

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
interface ProductReview {
    id: number;
    productId: number;
    userName: string;
    comment: string;
    rating: number;
}

function ProductPage({productId, onClose}: ProductPageProps) {
    const {isLoggedIn} = useAuth();
    const [product, setProduct] = useState<Product>();
    const [getReviews, setGetReviews] = useState<ProductReview[]>([]);
    const [postReview, setPostReviews] = useState<ProductReview | null>(null);
    const [review, setReview] = useState<string>('');
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<any>({});

    useEffect(()=>{
        api.get(`/products/${productId}`)
            .then(res => setProduct(res.data))
            .catch(err => {
                console.log(err);
                setError({getProductErr:'Не удалось загрузить страницу товара. Попробуйте позже.'})
                setProduct(undefined);
            })
            .finally(() => {
                setLoading(false);
            });
    },[productId]);

    useEffect(() => {
        api.get(`/products/${productId}/reviews`)
            .then(res => {
                setGetReviews(res.data.content);
                setReview('');
            })
            .catch(err => {
                console.log(err);
                setError({getReviewErr: 'Не удалось загрузить отзывы товара. Попробуйте позже.'})
                setProduct(undefined);}
            )
            .finally(() => setLoading(false));
    }, [productId]);

    const handlePostReviews = () => {
        if(isLoggedIn){
            if(postReview){
                api.post('/products/{productId}/reviews', {rating:postReview.rating, comment: postReview.comment})
                    .catch(err => {
                        console.log(err);
                        setError({postReviewErr:err.message});
                    });
            }
        } else  alert('Необходима регистрация');
    }

    if (loading)    return <div>Loading...</div>
    return (
        <div className='product-container'>
            <button className='onClose' onClick={onClose}>⭠</button>
            <div className='product-info'>
                <img src={product?.imageUrl} alt="фото товара"/>
                <div className='product-mainInfo'>
                    <h3>{product?.name}</h3>
                    <p>{product?.description}</p>
                    <input type="text" placeholder='Оставить отзыв'/>
                    <input type='number' placeholder='рейтинг'/>
                    <button className='leave-review' onClick={handlePostReviews}>Отправить</button>
                    {error.postReviewErr && (<div className='error-msg'>{error.postReviewErr}</div>)}
                </div>
                <div className='price-info'>
                    <p className='product-price'>{product?.price} BYN</p>
                    <button className='add-to-cart'>Добавить в корзину</button>
                    {!product?.available && (<p>Товар пока отсутствует</p>)}
                </div>
                <div className='reviews'>
                    {getReviews.map((review: ProductReview) => (
                        <div className='review' key={review.id}>
                            <div className='rating'>
                                <p>{review.rating}</p>
                                <p>{review.userName}</p>
                            </div>
                            <p>{review.comment}</p>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}

export default ProductPage;