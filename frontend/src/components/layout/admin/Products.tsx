import React, {useEffect, useState} from "react";
import api from "../../../api/axios";
import '../../../styles/admin/products.css';

interface Product {
    id?: number;
    name: string;
    description: string;
    price: number;
    imageUrl: string;
    categoryId: number;
    available: boolean;
    initialQuantity?: number;
}

function Products () {
    const [products, setProducts] = useState<Product[]>([]);
    const [product, setProduct] = useState<Product>({
        name: '',   description: '',    price: 0,
        imageUrl: '',   categoryId: 0,  available: false, initialQuantity: 0
    });
    const [editingProductId, setEditingProductId] = useState<number | undefined>(undefined);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<any>({});
    const [success, setSuccess] = useState<any>({});

    useEffect(() => {
        api.get(`/products`)
            .then(res => {
                setProducts(res.data.content);
                setError({getErr:''});
            })
            .catch(err => {
                console.log(err.message);
                setError({getErr:err.message});
            })
            .finally(() => setLoading(false));
    }, []);

    const handleClearFields = () => {
        setProduct({name: '',   description: '',    price: 0,
        imageUrl: '',   categoryId: 0,  available: false, initialQuantity: 0})
    }

    const handlePostProduct = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        api.post('/admin/products', product)
        .then(res => {
            setError('');
            setSuccess({postSuccess:'Товар добавлен успешно.'});
            setProducts(prevState => [...prevState, res.data.content]);
            handleClearFields();
        })
        .catch(err => {
            console.log(err.message);
            setError({postErr:err.message});
        })
        .finally(() => setLoading(false));
    }

    const handlePutProduct = (productId: number) => {
        const updatedProduct = products.find(product => product.id === productId);
        if (!updatedProduct) {
            setError({ putErr: 'Не удалось найти товар для обновления.' });
            return;
        }

        api.put(`/admin/products/${editingProductId}`, updatedProduct)
        .then(res => {
            setError({});
            setSuccess({putSuccess:'Товар успешно обновлен.'});
            setEditingProductId(undefined);
        }).catch(err => {
            console.log(err.message);
            setError({putErr:err.message});
        })
    }

    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value, type, checked} = event.target;
        const inputValue = type === 'checkbox' ? checked : value;
        setProduct(prevState => ({
            ...prevState,
            [name]: inputValue
        }));
    }

    const handleUpdateInputChange = (event: React.ChangeEvent<HTMLInputElement>, productId: number) => {
        const { name, value, type, checked } = event.target;
        const inputValue = type === 'checkbox' ? checked : value;

        setProducts(currentProducts =>
            currentProducts.map(product => {
                if (product.id === productId) {
                    return { ...product, [name]: inputValue };
                }
                return product;
            })
        );
    }

    const handleDeleteProduct = (productId: number) => {
        api.delete(`/admin/products/${productId}`)
        .then(res => {
            setError({deleteErr:''});
            setSuccess({deleteSuccess:'Товар успешно удален'});
            setProducts(current => current.filter(product => product.id !== productId));
        })
        .catch(err => {
            console.log(err.message);
            setError({deleteErr:err.message});
        })
    }

    if (loading)    return <div>Loading...</div>

    return (<div className='products-page'>
            <div className='products'>
                {products.map((product: Product) => (
                    <div className='item-container' key={product.id}>
                        {product.id === editingProductId ? (<>
                                <input type="text" value={product.imageUrl}
                                       onChange={(e) => handleUpdateInputChange(e, product.id!)} name='imageUrl'
                                    placeholder='Введите ссылку на фото'/>
                                <div className='item-content'>
                                    <label htmlFor="name">Название товара</label>
                                    <input type="text" id='name' value={product.name}
                                           onChange={(e) => handleUpdateInputChange(e, product.id!)} name='name'/>
                                    <label htmlFor="description">Описание</label>
                                    <input type="text" id='description' value={product.description}
                                           onChange={(e) => handleUpdateInputChange(e, product.id!)} name='description'/>
                                    <label htmlFor="price">Цена товара:</label>
                                    <input type="number" id='price' value={product.price}
                                           onChange={(e) => handleUpdateInputChange(e, product.id!)} name='price'/>
                                    <label htmlFor="available">Доступен сейчас</label>
                                    <input type="checkbox" className='available'
                                           onChange={(e) => handleUpdateInputChange(e, product.id!)} name="available"/>
                                </div>
                                <button className='update-btn' onClick={() => handlePutProduct(product.id!)}>Сохранить</button>
                                {success.putSuccess && <p className='success-msg'>{success.putSuccess}</p>}
                                {error.putErr && <p className='error-msg'>{error.putErr}</p>}
                                <button className='cancel-btn' onClick={()=> setEditingProductId(undefined)}>Отменить</button>
                            </>)
                        : (<>
                                <img src={product.imageUrl} alt="фото"/>
                                <div className='item-content'>
                                    <p className='item-price'>{product.price} BYN</p>
                                    <p><span>{product.name}</span><span className='item-description'>  / {product.description}</span></p>
                                </div>
                                <button className='update-btn' onClick={()=> setEditingProductId(product.id)}>Обновить товар</button>
                                <button className='dlete-btn' onClick={() => handleDeleteProduct(product.id!)}>Удалить товар</button>
                                {success.deleteSuccess && <p className='success-msg'>{success.deleteSuccess}</p>}
                                {error.deleteErr && <p className='error-msg'>{error.deleteErr}</p>}
                        </>)}
                    </div>
                ))}
                {error.getErr && (<p>{error.getErr}</p>)}
            </div>
            <div className='new-product'>
                <form onSubmit={handlePostProduct}>
                    <h3>Добавить новый товар</h3>
                    <label htmlFor="product-name">Название товара</label>
                    <input type="text" id='product-name'
                           value={product.name} onChange={handleInputChange} name="name"/>
                    <label htmlFor="product-description">Описание</label>
                    <input type="text" id='product-description'
                           value={product.description} onChange={handleInputChange} name="description"/>
                    <label htmlFor="product-price">Цена</label>
                    <input type="number" id='product-price' value={product.price}
                           onChange={handleInputChange} name="price"/>
                    <label htmlFor="product-categoryId">ID категории</label>
                    <input type="number" id='product-categoryId' value={product.categoryId}
                           onChange={handleInputChange} name="categoryId"/>
                    <label htmlFor="product-imageUrl">Ссылка на изображение</label>
                    <input type="text" id='product-imageUrl' value={product.imageUrl}
                           onChange={handleInputChange} name="imageUrl"/>
                    <label htmlFor="product-available">Доступен сейчас</label>
                    <input type="checkbox" className='product-available' onChange={handleInputChange} name="available" checked={product.available}/>
                    <label htmlFor="product-initial-quantity">Начальное количество товара</label>
                    <input type="number" id='product-initial-quantity' value={product.initialQuantity}
                           onChange={handleInputChange} name="initialQuantity"/>

                    <button type='submit' className='submit-btn'>Добавить товар</button>
                    {error.postErr && <p className='error-msg'>{error.postErr}</p>}
                    {success.postSuccess && <p className='success-msg'>{success.postSuccess}</p>}
                </form>
            </div>
        </div>

    )
}
export default Products;