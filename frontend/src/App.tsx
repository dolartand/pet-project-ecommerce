import React, {useState,useEffect} from 'react';
import {Route, Routes, useNavigate} from "react-router-dom";
import Header from "./components/layout/Header";
import AuthMode from "./components/layout/AuthMode";
import ShoppingBagPage from "./pages/ShoppingBagPage";
import MainPage from "./pages/MainPage";
import Sidebar from "./components/layout/Sidebar";
import FilterPage from "./components/layout/FilterPage";
import ProfileModal from "./components/layout/ProfileModal";
import {useAuth} from "./hooks/useAuth";
import styles from 'styles/global.module.css';
import ProductPage from "./pages/ProductPage";

type AuthModeType = 'login' | 'signup' | 'reset' | 'profile' | null;
export interface Filters {
    search?: string;
    categoryId?: number;
    page?: number;
    size?: number;
    minPrice?: number;
    maxPrice?: number;
    available?: boolean;
    sortBy?: string;
    sortOrder?: string;
}
export const INITIAL_FILTERS: Filters = {
    page: 0,
    size: 20,
    sortBy: "createdAt",
    sortOrder: "desc",
}

function App() {
    const [authMode, setAuthMode] = useState<AuthModeType>(null);
    const {isLoggedIn} = useAuth();
    const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);
    const [isClosing, setIsClosing] = useState<boolean>(false);
    const [isCartOpen, setIsCartOpen] = useState<boolean>(false);
    const [isFilterOpen, setIsFilterOpen] = useState<boolean>(false);
    const [isFilterClosing, setIsFilterClosing] = useState<boolean>(false);
    const [selectedProductId, setSelectedProductId] = useState<number | null>(null);
    const navigate = useNavigate();

    const [filters, setFilters] = useState<Filters>(INITIAL_FILTERS);

    const handleOpenLogInModal = () => {
        setAuthMode('login');
    }

    const handleOpenLoginOrProfileClick = () =>{
        if (isLoggedIn) setAuthMode('profile');
        else
            setAuthMode('login');
    }

    useEffect(() => {
        if (authMode === 'login' || authMode === 'signup')  document.body.classList.add('noScroll');
        else    document.body.classList.remove('noScroll');
        return () => {
            document.body.classList.remove('noScroll');
        };
    }, [authMode]);
    useEffect(()=>{
        if (!isClosing) return;
        const timer = setTimeout(()=>{
            setIsClosing(false);
            setIsSidebarOpen(false);
        },300);
        return () => {clearTimeout(timer);};
    },[isClosing]);
    useEffect(()=>{
        if (!isFilterClosing) return;
        const timer = setTimeout(()=>{
            setIsFilterClosing(false);
            setIsFilterOpen(false);
        },300);
        return () => {clearTimeout(timer);};
    },[isFilterClosing]);

    const handleSidebarClose = () => { setIsClosing(true); }
    const handleSidebarToggle = () => {
        if (isSidebarOpen)    setIsClosing(true);
        else {
            setIsSidebarOpen(true);
            setIsClosing(false);
        }
    }

    const handleCategorySelect = (categoryId: number) => {
        setFilters(prev=>({...prev, categoryId: categoryId ?? undefined, page: 0}));
        handleSidebarClose();
    }

    const handleCartClick = () => {
        if (isLoggedIn)
            // setTimeout(()=> navigate('/cart'), 0);
            setIsCartOpen(true);
        else handleOpenLogInModal();
    }

    const handleFilterToggle = () => {
        if (isFilterOpen)    setIsFilterClosing(true);
        else {
            setIsFilterOpen(true);
            setIsFilterClosing(false);
        }
    }
    const handleFilterClose = () => {setIsFilterClosing(true);}

    const handleProductSelect = (productId: number) => {setSelectedProductId(productId);}
    const handleProductClose = () => {setSelectedProductId(null);}

    return (
            <div className="App">
                <Header onCartClick={handleCartClick} onMenuClick={handleSidebarToggle} onFilterClick={handleFilterToggle}
                        isLoggedIn={isLoggedIn} onAuthClick={handleOpenLoginOrProfileClick}/>
                <Sidebar isOpen={isSidebarOpen} onClose={handleSidebarClose}
                         isClosing={isClosing} handleCategorySelected={handleCategorySelect} />
                <FilterPage isOpen={isFilterOpen} isClosing={isFilterClosing} onClose={handleFilterClose}
                        filters={filters} setFilters={setFilters}/>

                {authMode=== 'profile' && isLoggedIn && (
                    <div className="modal-backdrop" onClick={() => setAuthMode(null)}>
                        <div className="page-content" onClick={e => e.stopPropagation()}>
                            <ProfileModal handleClose={() => setAuthMode(null)}/>
                        </div>
                    </div>
                )}
                {(authMode === 'login' || authMode === 'signup' || authMode === 'reset') && !isLoggedIn && (
                    <AuthMode mode={authMode}
                              onClose={() => setAuthMode(null)}
                              onSwitchMode={()=>{
                                  setAuthMode(prevMode => (prevMode === 'login' ? 'signup' : 'login'));
                              }}
                              onResetMode={()=> setAuthMode('reset')}
                              onBackToLogIn={()=>setAuthMode('login')}/>
                )}
                <main>
                    {selectedProductId !== null ? (
                        <ProductPage productId={selectedProductId} onClose={handleProductClose} />)
                    : isCartOpen ? (<ShoppingBagPage/>)
                        : (
                        <MainPage
                            filters={filters}
                            handleOpenLoginModal={handleOpenLogInModal}
                            onProductSelect={handleProductSelect}
                        />
                    )}
                </main>
            </div>
    );
}
export default App;
