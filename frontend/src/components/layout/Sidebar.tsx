import React, {useEffect, useState} from "react";
import '../../styles/Sidebar.css';
import axios from "axios";

interface Category {
    id: number;
    name: string;
    description:string;
}
type SidebarProps = {
    isOpen: boolean;
    onClose: () => void;
    isClosing: boolean;
    handleCategorySelected: (categoryId: number) => void;
}

function Sidebar({ isOpen, onClose, isClosing, handleCategorySelected }: SidebarProps) {
    const [categories, setCategories] = React.useState<Category[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = React.useState<string | null>(null);

    useEffect(() => {
        setLoading(true);
        axios.get('http://localhost:8080/api/categories')
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

    if (!isOpen && !isClosing)    return null;

    return (
        <div className={`sidebar-content ${isClosing ? 'closing' : ''}`} onClick={onClose}>
            <nav className={`sidebar-container ${isOpen && !isClosing ? 'open' : ''} ${isClosing ? 'closing' : ''}`} onClick={e => e.stopPropagation()}>
                <h1>Категории товаров</h1>
                {loading && (<div>Загрузка...</div>)}
                {error && <div className='error-msg'>{error}</div>}
                <ul className='categories-list'>
                    {categories.map(category => (
                        <li key={category.id} onClick={()=> handleCategorySelected(category.id)}>{category.name}</li>
                    ))}
                </ul>
            </nav>
        </div>
    )
}
export default Sidebar;