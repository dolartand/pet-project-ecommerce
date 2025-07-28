import React, {useState,useEffect} from 'react';
import Header from "./components/layout/Header";
import AuthMode from "./components/layout/AuthMode";
import styles from 'styles/global.module.css';

type AuthMode = 'login' | 'signup' | null;

function App() {
    const [authMode, setAuthMode] = useState<AuthMode>(null);
    useEffect(() => {
        if (authMode === 'login' || authMode === 'signup')  document.body.classList.add('noScroll');
        else    document.body.classList.remove('noScroll');
        return () => {
            document.body.classList.remove('noScroll');
        };
    }, [authMode]);
    return (
        <div className="App">
            <Header onLoginClick={()=>setAuthMode('login')} />
            {authMode && (
                <AuthMode mode={authMode}
                onClose={() => setAuthMode(null)}
                onSwitchMode={()=>{
                    setAuthMode(prevMode => (prevMode === 'login' ? 'signup' : 'login'));
                }}/>
            )}
        </div>
  );
}
export default App;
