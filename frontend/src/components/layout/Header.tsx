import React from "react";
import '../../styles/Header.css'
import LanguageDropDown from "../ui/LanguageDropDown";

type HeaderProps = {
    isLoggedIn: boolean;
    onAuthClick: () => void;
    onCartClick: () => void;
    onMenuClick: () => void;
    onFilterClick: () => void;
};

function Header({onCartClick, onMenuClick, onFilterClick,isLoggedIn, onAuthClick}: HeaderProps) {

    return (
        <header className="navigation-block">
            <div className='navigation-container'>
                <div className='navigation-top hide-mobile hide-laptop'>
                    <ul className='simple-menu'>
                        <li className='simple-menu-item money'>
                            <LanguageDropDown />
                        </li>
                        <li className='simple-menu-item collection-point'>Пункты выдачи</li>
                        <li className='simple-menu-item help'>Помощь</li>
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
                        <input type="text" placeholder="Найти на сайте"/>
                        <button className='submit-input'><img src='https://img.icons8.com/?size=100&id=132&format=png&color=000000' alt="Поиск" loading="lazy"/></button>
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
                        </ul>
                    </div>
                </div>
            </div>
        </header>
    )
}
export default Header;