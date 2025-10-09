import React, {ChangeEvent, useEffect, useState} from "react";
import api from "../api/axios";
import {useAuth} from "../hooks/useAuth";
import '../styles/ProductPage.css';

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
    const [postReview, setPostReview] = useState({
        comment: '',
        rating: 0
    });
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

    const handlePostReviews = (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();
        if(isLoggedIn){
            if(!postReview.comment.trim() || !postReview.rating){
                alert('Пожалуйста, заполните отзыв и выставьте рейтинг.');
                return;
            }
            if(postReview.rating < 0 || postReview.rating > 5){
                alert('Пожалуйста, выставьте корректный рейтинг.');
                return;
            }
            api.post(`/products/${productId}/reviews`, {rating:postReview.rating, comment: postReview.comment})
                .then(res => {
                    console.log('Ответ сервера:', res.data);
                    const newReview = res.data;
                    setGetReviews(prevReviews => [newReview, ...prevReviews]);
                    setPostReview({comment: '', rating: 0});
                })
                .catch(err => {
                    console.log(err);
                    setError({postReviewErr:err.message});
                });
        } else  alert('Необходима регистрация');
    }

    const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        if (name === 'comment') {
            setPostReview(prevState => ({...prevState, [name]: value}));
        }
        if (name === 'rating') {
            if (value === '') {
                setPostReview(prevState => ({...prevState, rating: 0}));
                return;
            }
            const numValue = parseInt(value, 10);
            if (!isNaN(numValue) && numValue >= 0 && numValue <= 5) {
                setPostReview(prevState => ({ ...prevState, rating: numValue }));
            }
        }
    };

    const handleAddToCart = (event: React.MouseEvent<HTMLButtonElement>) => {
        event.stopPropagation();
        if (isLoggedIn) {
            api.post('/cart/items', {productId: product?.id,quantity: 1})
                .then(res => {
                    alert(`Товар "${product?.name}" добавлен в корзину.`);
                })
                .catch(err => {
                    setError(err.response?.data?.message || err.response?.data?.errors );
                })
        } else alert('Необходима регистрация');
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
                    <input type="text" placeholder='Оставить отзыв'
                           name="comment" value={postReview.comment} onChange={handleInputChange}/>
                    <input type='number' placeholder='рейтинг'
                           name="rating" value={postReview.rating} onChange={handleInputChange}/>
                    <button type='button' className='leave-review' onClick={handlePostReviews}>Отправить</button>
                    {error.postReviewErr && (<div className='error-msg'>{error.postReviewErr}</div>)}
                </div>
                <div className='price-info'>
                    <p className='product-price'>{product?.price} BYN</p>
                    <button className='add-to-cart' onClick={handleAddToCart}>Добавить в корзину</button>
                    {!product?.available && (<p>Товар пока отсутствует</p>)}
                </div>
            </div>
            <div className='reviews'>
                {getReviews.map((review: ProductReview) => (
                    <div className='review' key={review.id}>
                        <div className='rating'>
                            <p>⭐{review.rating}</p>
                            <p>{review.userName}</p>
                        </div>
                        <p>{review.comment}</p>
                    </div>
                ))}
            </div>
        </div>
    )
}

export default ProductPage;