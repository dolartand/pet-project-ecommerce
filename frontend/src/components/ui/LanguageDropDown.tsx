import React, {useState, useRef, useEffect} from "react";
import styles from '../../styles/LanguageDropDown.module.css'

interface Languages {
    language: string;
    code: string;
}
const languages: Languages[] = [
    {language: 'Русский', code: 'RU'},
    {language: 'English', code: 'ENG'},
    {language: 'Spanish', code: 'ESP'},
];

const LanguageDropDown: React.FC = () => {
    const [selected, setSelected] = useState<Languages>(languages[0]);
    const [open, setOpen] = useState<boolean>(false);
    const ref = useRef<HTMLDivElement>(null);

    useEffect(()=>{
        const handleClickOutsideComponent = (e:MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) {
            setOpen(false);
        }
        };
        document.addEventListener("mousedown", handleClickOutsideComponent);
        return ()=>{document.removeEventListener("mousedown", handleClickOutsideComponent);}
    },[])
    return (
        <div className={styles.dropdown} ref={ref} onMouseEnter={()=>setOpen(true)} onMouseLeave={()=>setOpen(false)}>
            <button className={styles.toggle} onClick={()=>setOpen(o=>!o)} aria-expanded={open}>
                <span>{selected.code}</span>
            </button>
            <ul className={`${styles.menu} ${open ? styles.menuOpen : ''}`}>
                {languages.map(language => (
                    <li key={language.code} className={styles.item}>
                        <button
                            className={styles.itemBtn}
                            onClick={() => {
                                setSelected(language);
                                setOpen(false);
                            }}
                        >
                            {language.language}
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    )
}
export default LanguageDropDown;