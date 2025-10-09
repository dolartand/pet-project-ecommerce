import React, {useState, useEffect, useCallback} from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import ItemCartBuy from "../components/layout/ItemCartBuy";
import '../styles/ShoppingBag.css';
import api from "../api/axios";

interface Product {
    id: number;
    productName: string;
    description: string;
    price: number;
    imageUrl: string;
    categoryId: number;
    available: boolean;
    quantity: number;
}

function ShoppingBagPage() {
    const [goods, setGoods] = useState<Product[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<any>({});
    const [totalSum, setTotalSum] = useState<number>(0);
    const [selectedItems, setSelectedItems] = useState<number[]>([]); // по умолчанию весь массив выбран
    const [deletingItemId, setDeletingItemId] = useState<number>();
    const [isCartClearing, setIsCartClearing] = useState<boolean>(false);
    const {isLoggedIn} = useAuth();

    useEffect(() => {
        setLoading(true);
        api.get('/cart')
            .then(res =>{
                console.log(res.data.items);
                setGoods(res.data.items);
                const allItems = res.data.items.map((item: Product) => item.id);
                setSelectedItems(allItems);
                setError({});
            })
            .catch(err => {
                console.log('Ошибка получения корзины ', err);
                setError({pageErr:'Не удалось загрузить корзину. Попробуйте позже.'});
            })
            .finally(() => setLoading(false));
    }, []);

    useEffect(() => { // чтобы при изменении корзины, все выделялось по дефолту
        const allItemsId = goods.map((item) => item.id);
        setSelectedItems(allItemsId);
    },[goods]);

    useEffect(() => {
        const newTotalSum = goods
            .filter(item => selectedItems.includes(item.id))
            .reduce((sum, currentItem) => {
                const itemPrice = currentItem.price || 0;
                const itemQuantity = currentItem.quantity || 1;
                return sum + (itemPrice*itemQuantity);
            },0);
        setTotalSum(newTotalSum);
    }, [goods, selectedItems]);

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

    const handleQuantityChange = useCallback((itemId:number, newQuantity:number) => {
        if (newQuantity < 1) {
            return;
        }
        const previousGoods = [...goods];
        setGoods(current => current.map(item =>
            item.id === itemId ? { ...item, quantity: newQuantity } : item
        ));

        api.put(`/cart/items/${itemId}`, {quantity: newQuantity})
            .catch(err => {
                console.log(err.message);
                setError({newQuantityErr: err.message});
                setGoods(previousGoods);
            });
    },[goods]);

    const handleDeleteItem = (itemId: number) => {
        setDeletingItemId(itemId);
        setTimeout(()=> {
            const previousGoods = [...goods];
            setGoods(current => current.filter(item => item.id !== itemId));
            api.delete(`/cart/items/${itemId}`)
                .catch(err => {
                    console.log(err.message);
                    setError({deleteItemErr: err.message});
                    setGoods(previousGoods);
                })
        },300);
    }

    const handleClearAll = () => {
        setIsCartClearing(true);
        setTimeout(()=> {
            api.delete('/cart')
                .then(res =>{
                    setGoods([]);
                })
                .catch(err =>{
                    console.log(err.message);
                    setError({clearCart:err.message});
                })
        },300);
    }

    if (!isLoggedIn)    return <Navigate to="/" replace />
    if (loading)    return <div>Loading...</div>
    if (error.pageErr)    return <div className='error-msg'>{error.pageErr}</div>;
    if (goods.length === 0) return <div>Ваша корзина пуста</div>;

    return (
     <>
         <div className='shopping-top'>
             <div className='input-container'>
                 <label htmlFor="chooseAll">Выбрать все</label>
                 <input type="checkbox" id='chooseAll' name='chooseAll' checked={areAllSelected}
                            onChange={handleSelectedAll}/>
             </div>
         </div>
         <div className='shopping-container'>
             <div className={`shopping-main ${isCartClearing ? 'cart-clearing' : ''}`}>
                 {goods.map((item) => (
                     <ItemCartBuy key={item.id} item={item} onSelectionChange={handleItemSelection}
                     isSelected={selectedItems.includes(item.id)} onQuantityChange={handleQuantityChange}
                     onDeleteItem={handleDeleteItem} isDeleting={deletingItemId === item.id}/>
                 ))}
                 {error.deleteItemErr && <p>{error.deleteItemErr}</p>}
             </div>
             <div className='shopping-payments'>
                 <p>Всего товаров выбрано: {selectedItems.length}</p>
                 <button className='btn clear-cart' type='button' onClick={handleClearAll}>Очистить корзину</button>
                 {error.clearCart && <p>{error.clearCart}</p>}
                 <h3>К оплате: <strong>{totalSum.toFixed(2)}</strong> BYN</h3>

             </div>
         </div>
     </>
    )
}
export default ShoppingBagPage;