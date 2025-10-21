import React, {useState} from "react";
import {UserMultipleIcon, FilterIcon, ProductLoadingIcon, Book02Icon, Logout02Icon} from "hugeicons-react";
import Users from "../components/layout/admin/Users";
import Categories from "../components/layout/admin/Categories";
import Products from "../components/layout/admin/Products";
import '../styles/admin/common.css';
import Orders from "../components/layout/admin/Orders";

interface AdminPageProps {
    handleClose: () => void;
}

function AdminPage ({handleClose}: AdminPageProps) {
    const [isUserOpen, setIsUserOpen] = useState<boolean>(false);
    const [isCategoriesOpen, setIsCategoriesOpen] = useState<boolean>(false);
    const [isGoodsOpen, setIsGoodsOpen] = useState<boolean>(false);
    const [isOrdersOpen, setIsOrdersOpen] = useState<boolean>(false);

    const onUsersClick = () => {
        setIsUserOpen(true);
        setIsCategoriesOpen(false);
        setIsOrdersOpen(false);
        setIsGoodsOpen(false);
    };

    const onCategoriesClick = () => {
        setIsCategoriesOpen(true);
        setIsUserOpen(false);
        setIsOrdersOpen(false);
        setIsGoodsOpen(false);
    }

    const onGoodsClick = () => {
        setIsGoodsOpen(true);
        setIsUserOpen(false);
        setIsCategoriesOpen(false);
        setIsOrdersOpen(false);
    }

    const onOrdersClick = () => {
        setIsOrdersOpen(true);
        setIsCategoriesOpen(false);
        setIsUserOpen(false);
        setIsGoodsOpen(false);
    }

    return (<>
        <div className='header hide-mobile'>
            <ul>
                <li className='simple-menu' onClick={onUsersClick}>
                    <UserMultipleIcon color='#ffffff' size={42}/>
                    <p>Пользователи</p></li>
                <li className='simple-menu' onClick={onCategoriesClick}>
                    <FilterIcon color='#ffffff' size={42}/>
                    <p>Категории</p></li>
                <li className='simple-menu' onClick={onGoodsClick}>
                    <ProductLoadingIcon color='#ffffff' size={42}/>
                    <p>Товары</p>
                </li>
                <li className='simple-menu' onClick={onOrdersClick}>
                    <Book02Icon color='#ffffff' size={42}/>
                    <p>Заказы</p>
                </li>
                <li className='simple-menu' onClick={handleClose}>
                    <Logout02Icon color='#ffffff' size={42}/>
                    <p>Выйти из аккаунта</p>
                </li>
            </ul>
        </div>
        <main>
            {isUserOpen ? (<Users/>)
            : isCategoriesOpen ? (<Categories/>)
            : isGoodsOpen ? (<Products/>)
            : isOrdersOpen ? (<Orders/>)
            : <Products/>}
        </main>
    </>)
}

export default AdminPage;