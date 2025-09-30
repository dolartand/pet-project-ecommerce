import React, {useEffect, useState} from "react";
import ItemCartMain from "../components/layout/ItemCartMain";
import api from "../api/axios";
import {Filters} from "../App";
import "../styles/ItemCartMain.css";

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
type MainPageProps = {
    filters: Filters;
    handleOpenLoginModal: () => void;
    onProductSelect: (product: number) => void;
}

function cleanParams(filters: Filters) {
    const params: any = {};
    if (filters.search) params.search = filters.search;
    if (filters.categoryId != null && filters.categoryId !== undefined) params.categoryId = filters.categoryId;
    if (filters.minPrice != null) params.minPrice = filters.minPrice;
    if (filters.maxPrice != null) params.maxPrice = filters.maxPrice;
    if (filters.available != null) params.available = filters.available;
    if (filters.page != null) params.page = filters.page;
    if (filters.size != null) params.size = filters.size;
    if (filters.sortBy) params.sortBy = filters.sortBy;
    if (filters.sortOrder) params.sortOrder = filters.sortOrder;
    return params;
}

function MainPage ({handleOpenLoginModal, filters, onProductSelect}: MainPageProps) {
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [products, setProducts] = useState<Product[]>([]);
    useEffect(() => {
        let isMounted = true;
        setLoading(true);
        let url = '/products';
        const params: any = cleanParams(filters);
        api.get(url, {params})
            .then(res => {
                if (isMounted) {
                    const productsData = res.data.content;
                    if (Array.isArray(productsData)) {
                        setProducts(productsData);
                    } else {
                        console.warn("Получена неожиданная структура данных от API:", res.data);
                        setProducts([]);
                    }
                    setError(null);
                }
            })
            .catch(err => {
                if(isMounted){
                    console.log('Ошибка получения товаров ', err);
                    setError('Не удалось загрузить товары. Попробуйте позже.')
                    setProducts([]);
                }
            })
            .finally(() => {
                if(isMounted)   setLoading(false);
            });
        return () => {isMounted = false;};
    },[filters])

    if (loading)    return <div>Loading...</div>
    if (error)    return <div className='error-msg'>{error}</div>;
    if (products.length === 0) return <div>Товаров нет</div>;

    return (
        <div className='main-page'>
            {products.map((product: Product) => (
                <ItemCartMain item={product} key={product.id}
                              onLogInRequired={handleOpenLoginModal} onProductClick={()=> onProductSelect(product.id)}/>
            ))}
        </div>
    );
}
export default MainPage;