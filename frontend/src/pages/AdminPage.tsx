import React from "react";
import {UserMultipleIcon, FilterIcon, ProductLoadingIcon, Book02Icon} from "hugeicons-react";


function AdminPage () {

    const onUsersClick = () => {}

    const onCategoriesClick = () => {}

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
    </>)
}

export default AdminPage;