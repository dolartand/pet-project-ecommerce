import React, {useState,useEffect} from 'react';
import {Route, Routes, useNavigate} from "react-router-dom";
import Header from "./components/layout/Header";
import AuthMode from "./components/layout/AuthMode";
import ShoppingBagPage from "./pages/ShoppingBagPage";
import MainPage from "./pages/MainPage";
import Sidebar from "./components/layout/Sidebar";
import {useAuth} from "./hooks/useAuth";
import styles from 'styles/global.module.css';

type AuthModeType = 'login' | 'signup' | 'reset' | null;

function App() {
    const [authMode, setAuthMode] = useState<AuthModeType>(null);
    const {isLoggedIn} = useAuth();
    const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);
    const [isClosing, setIsClosing] = useState<boolean>(false);
    const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
    const navigate = useNavigate();

    const handleOpenLogInModal = () => {
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

    const handleSidebarClose = () => { setIsClosing(true); }
    const handleSidebarToggle = () => {
        if (isSidebarOpen)    setIsClosing(true);
        else {
            setIsSidebarOpen(true);
            setIsClosing(false);
        }
    }

    const handleCategorySelect = (categoryId: number) => {
        setSelectedCategory(categoryId);
    }

    const handleCartClick = () => {
        debugger;
        if (isLoggedIn)
            setTimeout(()=> navigate('/cart'), 0);
        else handleOpenLogInModal();
    }

    return (
            <div className="App">
                <Header onLoginClick={()=>setAuthMode('login')} onCartClick={handleCartClick}
                        onMenuClick={handleSidebarToggle} />
                <Sidebar isOpen={isSidebarOpen} onClose={handleSidebarClose}
                         isClosing={isClosing} handleCategorySelected={handleCategorySelect} />
                {authMode && (
                    <AuthMode mode={authMode}
                              onClose={() => setAuthMode(null)}
                              onSwitchMode={()=>{
                                  setAuthMode(prevMode => (prevMode === 'login' ? 'signup' : 'login'));
                              }}
                              onResetMode={()=> setAuthMode('reset')}
                              onBackToLogIn={()=>setAuthMode('login')}/>
                )}
                <main>
                    <Routes>
                        <Route path="/" element={<MainPage categoryId={selectedCategory} handleOpenLoginModal={handleOpenLogInModal} />} />
                        <Route path="/cart" element={<ShoppingBagPage />} />
                    </Routes>
                </main>
            </div>
    );
}
export default App;
