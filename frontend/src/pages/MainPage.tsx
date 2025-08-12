import React, {useEffect, useState} from "react";
import ItemCartMain from "../components/layout/ItemCartMain";
import axios from "axios";
import "../styles/ItemCartMain.css";

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
type MainPageProps = {
    categoryId: number | null;
    handleOpenLoginModal: () => void;
}

function MainPage ({categoryId, handleOpenLoginModal}: MainPageProps) {
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [products, setProducts] = useState<Product[]>([]);
    useEffect(() => {
        setLoading(true);
        let url = "http://localhost:8080/api/products";
        if (categoryId) url += `?categoryId=${categoryId}`;
        axios.get(url)
            .then(res =>{
                if (res.data && Array.isArray(res.data.content)) {
                    setProducts(res.data.content);
                }
                setError(null);
            })
            .catch(err => {
                console.log('Ошибка получения товаров ', err);
                setError('Не удалось загрузить товары. Попробуйте позже.')
            })
            .finally(() => setLoading(false));
    },[categoryId])

    if (loading)    return <div>Загрузка...</div>
    if (error)    return <div className='error-msg'>{error}</div>;
    if (products.length === 0) return <div>Товаров нет</div>;

    return (
        <div className='main-page'>
            {products.filter(product => product.available)
                .map((product: Product) => (
                <ItemCartMain item={product} key={product.productId} onLogInRequired={handleOpenLoginModal} />
            ))}
        </div>
    )
}
export default MainPage;