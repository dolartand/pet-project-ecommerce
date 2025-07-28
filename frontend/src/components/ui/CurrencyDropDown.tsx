import React, {useState, useRef, useEffect} from "react";
import styles from '../../styles/CurrencyDropDown.module.css'

interface Currency {
    currency: string;
    code: string;
}
const currencies: Currency[] = [
    {currency: 'Белорусский рубль', code: 'BYN'},
    {currency: 'Российский рубль', code: 'RUB'},
    {currency: 'Казахстанский тенге', code: 'KZT'},
    {currency: 'Армянский драм', code: 'AMD'},
];

const CurrencyDropDown: React.FC = () => {
    const [selected, setSelected] = useState<Currency>(currencies[0]);
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
                {currencies.map(currency => (
                    <li key={currency.code} className={styles.item}>
                        <button
                            className={styles.itemBtn}
                            onClick={() => {
                                setSelected(currency);
                                setOpen(false);
                            }}
                        >
                            {currency.currency}
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    )
}
export default CurrencyDropDown;