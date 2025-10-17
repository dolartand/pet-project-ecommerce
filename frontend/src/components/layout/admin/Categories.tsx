import React, {useEffect, useState} from "react";
import api from "axios";

interface Category {
    id: number;
    name: string;
    description:string;
}
interface CategoryPost {
    name: string;
    description: string;
    parentId: number;
}

function Categories () {
    const [categories, setCategories] = useState<Category[]>([]);
    const [categoryPost, setCategoryPost] = useState<CategoryPost>({
        name: '', description: '', parentId: 0,
    });
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = React.useState<string | null>(null);
    const [successmsg, setSuccessmsg] = useState<string | null>(null);

    useEffect(() => {
        api.get('/categories')
            .then(res => {
                setCategories(res.data);
                setError(null);
            })
            .catch(err => {
                console.log(err);
                setError('Не удалось загрузить категории.');
            })
            .finally(() => setLoading(false));
    }, []);

    const handleCreateCategory = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        api.post('/admin/categories', {categoryPost})
            .then(res => {
                setSuccessmsg(res.data.description);
            })
    }

    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = event.target;
        setCategoryPost(prevState => ({
            ...prevState,
            [name]: value
        }));
    }

    if (loading)    return <div>Loading...</div>

    return (<div className='categories-page'>
            <div className='categories'>
                <h3>Категории товаров:</h3>
                <ul className='categories-list'>
                    {categories.map(category => (
                        <li key={category.id}>{category.id}. {category.name} ({category.description})</li>
                    ))}
                </ul>
            </div>
            <div className='new-category'>
                <form onSubmit={handleCreateCategory}>
                    <h3>Создать новую категорию</h3>
                    <label htmlFor="category-name">Название категории</label>
                    <input type="text" id='category-name' placeholder='Смартфоны'
                            value={categoryPost.name} onChange={handleInputChange} name="name"/>
                    <label htmlFor="category-description">Описание</label>
                    <input type="text" id='category-description' placeholder='Мобильные телефоны различных брендов'
                            value={categoryPost.description} onChange={handleInputChange} name="description"/>
                    <label htmlFor="category-parentId">Родительский ID</label>
                    <input type="number" id='category-parentId' value={categoryPost.parentId}
                           onChange={handleInputChange} name="parentId"/>
                    <button type='submit'>Создать</button>
                    {error && <p className='error-msg'>{error}</p>}
                    {successmsg && <p className='success-msg'>{successmsg}</p>}
                </form>
            </div>
        </div>
    )
}
export default Categories;