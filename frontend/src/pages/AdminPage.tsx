import React, {useState} from "react";
import {UserMultipleIcon, FilterIcon, ProductLoadingIcon, Book02Icon} from "hugeicons-react";
import Users from "../components/layout/admin/Users";
import Categories from "../components/layout/admin/Categories";


function AdminPage () {
    const [isUserOpen, setIsUserOpen] = useState<boolean>(false);
    const [isCategoriesOpen, setIsCategoriesOpen] = useState<boolean>(false);
    const [isGoodsOpen, setIsGoodsOpen] = useState<boolean>(false);
    const [isOrdersOpen, setIsOrdersOpen] = useState<boolean>(false);

    const onUsersClick = () => {
        setIsUserOpen(true);
        setIsCategoriesOpen(false);};

    const onCategoriesClick = () => {
        setIsCategoriesOpen(true);
        setIsUserOpen(false);
    }

    const onGoodsClick = () => {}

    const onOrdersClick = () => {

    }

    return (<>
        <div className='header-navbar hide-mobile'>
            <ul>
                <li className='simple-menu' onClick={onUsersClick}>
                    <UserMultipleIcon color='#ffffff' size={42}/>
                    <p>Пользователи</p></li>
                <li className='simple-menu' onClick={onCategoriesClick}>
                    <FilterIcon color='#000000' size={42}/>
                    <p>Категории</p></li>
                <li className='simple-menu' onClick={onGoodsClick}>
                    <ProductLoadingIcon color='#ffffff' size={42}/>
                    <p>Товары</p>
                </li>
                <li className='simple-menu' onClick={onOrdersClick}>
                    <Book02Icon color='#ffffff' size={42}/>
                    <p>Заказы</p>
                </li>
            </ul>
        </div>
        <main>
            {isUserOpen ? (<Users/>)
            : isCategoriesOpen ? (<Categories/>)
            : null}
        </main>
    </>)
}

export default AdminPage;