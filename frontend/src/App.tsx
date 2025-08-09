import React, {useState,useEffect} from 'react';
import Header from "./components/layout/Header";
import AuthMode from "./components/layout/AuthMode";
import ShoppingBagPage from "./pages/ShoppingBagPage";
import MainPage from "./pages/MainPage";
import Sidebar from "./components/layout/Sidebar";
import styles from 'styles/global.module.css';

type AuthMode = 'login' | 'signup' | 'reset' | null;

function App() {
    const [authMode, setAuthMode] = useState<AuthMode>(null);
    const [isCartOpen, setIsCartOpen] = useState<boolean>(false);
    const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);

    useEffect(() => {
        if (authMode === 'login' || authMode === 'signup')  document.body.classList.add('noScroll');
        else    document.body.classList.remove('noScroll');
        return () => {
            document.body.classList.remove('noScroll');
        };
    }, [authMode]);

    const handleSidebarClose = () => { setIsSidebarOpen(false); }
    const handleSidebarToggle = () => { setIsSidebarOpen(!isSidebarOpen); }

    return (
        <div className="App">
            <Header onLoginClick={()=>setAuthMode('login')} onCartClick={()=>setIsCartOpen(true)}
            onMenuClick={handleSidebarToggle} />
            <Sidebar isOpen={isSidebarOpen} isClose={handleSidebarClose} />
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
                {isCartOpen ? <ShoppingBagPage /> : <MainPage />}
            </main>
        </div>
  );
}
export default App;
