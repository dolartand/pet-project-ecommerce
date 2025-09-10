import React, {useState, useEffect} from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import ItemCartBuy from "../components/layout/ItemCartBuy";
import '../styles/ShoppingBag.css';
import api from "../api/axios";

interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    imageUrl: string;
    categoryId: number;
    available: boolean;
}

function ShoppingBagPage() {
    const [goods, setGoods] = useState<Product[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [selectedItems, setSelectedItems] = useState<number[]>([]); // по умолчанию весь массив выбран
    const {isLoggedIn} = useAuth();

    useEffect(() => { // чтобы при изменении корзины, все выделялось по дефолту
        const allItemsId = goods.map((item) => item.id);
        setSelectedItems(allItemsId);
    },[goods]);
    useEffect(() => {
        setLoading(true);
        api.get('http://localhost:8080/api/cart')
            .then(res =>{
                setGoods(res.data);
                const allItems = res.data.map((item: Product) => item.id);
                setSelectedItems(allItems);
                setError(null)
            })
            .catch(err => {
                console.log('Ошибка получения корзины ', err);
                setError('Не удалось загрузить корзину. Попробуйте позже.')
            })
            .finally(() => setLoading(false));
        }, []);

    const handleItemSelection = (itemId: number) => {
        setSelectedItems(prevSelected => {
            if (prevSelected.includes(itemId))  return prevSelected.filter(id => id !== itemId);
            else return [...prevSelected, itemId];
        });
    }
    const handleSelectedAll = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (event.target.checked) {
            const allItemsIds = goods.map((item) => item.id);
            setSelectedItems(allItemsIds);
        }
        else setSelectedItems([]);
    }
    const areAllSelected = goods.length > 0 && selectedItems.length === goods.length;

    if (!isLoggedIn)    return <Navigate to="/" replace />
    if (loading)    return <div>Загрузка...</div>
    if (error)    return <div className='error-msg'>{error}</div>;
    if (goods.length === 0) return <div>Ваша корзина пуста</div>;

    return (
     <>
         <div className='shopping-top'>
             <div className='input-container'>
                 <label htmlFor="chooseAll">Выбрать все</label>
                 <div className='round'>
                     <input type="checkbox" id='chooseAll' name='chooseAll' checked={areAllSelected}
                            onChange={handleSelectedAll}/>
                     <label htmlFor="chooseAll"></label>
                 </div>
             </div>
             <button className='action-with-goods'>Что делать с товаром?</button>
         </div>
         <div className='shopping-container'>
             <div className='shopping-main'>
                 {goods.map((item) => (
                     <ItemCartBuy key={item.id} item={item} onSelectionChange={handleItemSelection}
                     isSelected={selectedItems.includes(item.id)} />
                 ))}
             </div>
             <div className='shopping-payments'>
                 <p>Всего товаров выбрано {selectedItems.length}</p>
                 <p>ИТОГО</p>

             </div>
         </div>
     </>
    )
}
export default ShoppingBagPage;