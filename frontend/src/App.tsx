import React, {useState,useEffect} from 'react';
import Header from "./components/layout/Header";
import AuthMode from "./components/layout/AuthMode";
import ShoppingBagPage from "./pages/ShoppingBagPage";
import MainPage from "./pages/MainPage";
import Sidebar from "./components/layout/Sidebar";
import FilterModal from "./components/layout/FilterModal";
import ProfileModal from "./components/layout/ProfileModal";
import {useAuth} from "./hooks/useAuth";
import styles from 'styles/global.module.css';
import ProductPage from "./pages/ProductPage";
import OrderPage from "./pages/OrdersPage";
import AdminPage from "./pages/AdminPage";

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
const INITIAL_FILTERS: Filters = {
    page: 0,
    size: 20,
    sortBy: "createdAt",
    sortOrder: "desc",
}
export {INITIAL_FILTERS};

function App() {
    const [authMode, setAuthMode] = useState<AuthModeType>(null);
    const {isLoggedIn, user, logOut} = useAuth();
    const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);
    const [isClosing, setIsClosing] = useState<boolean>(false);
    const [isCartOpen, setIsCartOpen] = useState<boolean>(false);
    const [isOrderHistoryOpen, setIsOrderHistoryOpen] = useState<boolean>(false);
    const [isFilterOpen, setIsFilterOpen] = useState<boolean>(false);
    const [isFilterClosing, setIsFilterClosing] = useState<boolean>(false);
    const [selectedProductId, setSelectedProductId] = useState<number | null>(null);

    const [filters, setFilters] = useState<Filters>(INITIAL_FILTERS);

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

    if (isLoggedIn && user?.role === 'ADMIN') {return <AdminPage handleClose={logOut}/>}

    const handleOpenLogInModal = () => {setAuthMode('login');}

    const handleOpenLoginOrProfileClick = () =>{
        if (isLoggedIn) setAuthMode('profile');
        else
            setAuthMode('login');
    }

    const handleSidebarClose = () => { setIsClosing(true); }
    const handleSidebarToggle = () => {
        if (isSidebarOpen)    setIsClosing(true);
        else {
            setIsSidebarOpen(true);
        }
    }

    const handleCategorySelect = (categoryId: number) => {
        setFilters(prev=>({...prev, categoryId: categoryId ?? undefined, page: 0}));
        handleSidebarClose();
    }

    const handleCartClick = () => {
        if (isLoggedIn) {
            setSelectedProductId(null);
            setIsCartOpen(true);
        }
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

    const handleOpenOrderHistory = () => {
        if(isLoggedIn) {
            setSelectedProductId(null);
            setIsCartOpen(false);
            setIsOrderHistoryOpen(true);
        } else  handleOpenLogInModal();
    }

    const handleSearchSubmit = (searchInput: string) => {
        setFilters(prev=>({...prev, search: searchInput ?? undefined, page: 0}));
    }

    return (
            <div className="App">
                <Header onCartClick={handleCartClick} onMenuClick={handleSidebarToggle} onFilterClick={handleFilterToggle}
                        isLoggedIn={isLoggedIn} onAuthClick={handleOpenLoginOrProfileClick}
                        onOrderHistoryClick={handleOpenOrderHistory} onSearchSubmit={handleSearchSubmit}/>
                <Sidebar isOpen={isSidebarOpen} onClose={handleSidebarClose}
                         isClosing={isClosing} handleCategorySelected={handleCategorySelect}/>
                <FilterModal isOpen={isFilterOpen} isClosing={isFilterClosing} onClose={handleFilterClose}
                             filters={filters} setFilters={setFilters}/>

                {authMode=== 'profile' && isLoggedIn && (
                            <ProfileModal handleClose={() => setAuthMode(null)} mode={authMode}/>
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
                    : isOrderHistoryOpen ? (<OrderPage/>)
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