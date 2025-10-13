import React, {useEffect, useState} from "react";
import '../../styles/Header.css'
import {Book02Icon, Search01Icon} from 'hugeicons-react';
import LanguageDropDown from "../ui/LanguageDropDown";

type HeaderProps = {
    isLoggedIn: boolean;
    onAuthClick: () => void;
    onCartClick: () => void;
    onMenuClick: () => void;
    onFilterClick: () => void;
    onOrderHistoryClick: () => void;
    onSearchSubmit: (input:string) => void;
};

function Header({onCartClick, onMenuClick, onFilterClick,isLoggedIn, onAuthClick, onOrderHistoryClick, onSearchSubmit}: HeaderProps) {
    const [localSearchInput, setLocalSearchInput] = useState('');

    const handleSearch = (event: React.FormEvent) => {
        event.preventDefault();
        onSearchSubmit(localSearchInput);
    };

    return (
        <header className="navigation-block">
            <div className='navigation-container'>
                <div className='navigation-top hide-mobile hide-laptop'>
                    <ul className='simple-menu'>
                        <li className='simple-menu-item money'>
                            <LanguageDropDown />
                        </li>
                    </ul>
                </div>
                <div className='navigation-bottom'>
                    <div className='header_nav-element hide-mobile'>
                        <a className="header-logo" href="/">
                            <img src="/assets/logo.png" alt="Online Shop Logo" loading="lazy" />
                        </a>
                        <button className='burger-btn' type='button' aria-label='Навигация' onClick={onMenuClick}>☰</button>
                    </div>
                    <div className='search-block'>
                        <form onSubmit={handleSearch}>
                            <input type="text" placeholder="Найти на сайте" value={localSearchInput}
                                   onChange={e => setLocalSearchInput(e.target.value)}/>
                            <button className='submit-input' type='submit'><Search01Icon color='#000000' size={36}/></button>
                        </form>
                        <button id='filterBtn' type='button' onClick={onFilterClick}>
                            <img src="/assets/filter.png" alt="Фильтр" loading="lazy"/>
                        </button>
                    </div>
                    <div className='header-navbar hide-mobile'>
                        <ul>
                            <li className='simple-menu' onClick={onAuthClick}>
                                <img src="https://img.icons8.com/ios-glyphs/40/FFFFFF/user-male-circle.png" loading="lazy"/>
                                <p>{isLoggedIn ? "Профиль" : "Войти"}</p></li>
                            <li className='simple-menu' onClick={onCartClick}>
                                <img src="https://img.icons8.com/?size=40&id=9671&format=png&color=FFFFFF" loading="lazy"/>
                                <p>Корзина</p></li>
                            <li className='simple-menu' onClick={onOrderHistoryClick}>
                                <Book02Icon color='#ffffff' size={42}></Book02Icon>
                                <p>История заказов</p>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </header>
    )
}
export default Header;