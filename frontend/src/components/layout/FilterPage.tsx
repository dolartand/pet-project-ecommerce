import React, {useEffect, useState} from 'react';
import {Filters, INITIAL_FILTERS} from '../../App';
import '../../styles/FormsAndModals.css';

type FilterPageProps = {
    isOpen: boolean;
    isClosing: boolean;
    onClose: () => void;
    filters: Filters;
    setFilters: (f: Filters) => void;
}

function FilterPage({isOpen, onClose, isClosing, filters, setFilters}: FilterPageProps) {
    const [local, setLocal] = useState<Filters>(filters);
    const [err, setErr] = useState<string|null>(null);

    useEffect(()=>{
        setLocal(filters);
    }, [filters]);

    useEffect(()=>{
        const onKey = (e:KeyboardEvent)=>{
            if (e.key === 'Escape') onClose();
        };
        window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    },[onClose]);

    if(!isOpen && !isClosing) return null;

    const change = (key: keyof Filters, value: any) => {
        setLocal(prev => ({ ...prev, [key]: value }));
    };

    const apply = () => {
        if (local.minPrice != null && local.maxPrice != null && Number(local.minPrice) > Number(local.maxPrice)) {
            setErr('Минимальная цена не может быть больше максимальной');
            return;
        }
        setErr(null);
        setFilters({...local, page:0});
        onClose();
    };

    const reset = () => {
        const newFilters = {...INITIAL_FILTERS};
        if (filters.categoryId != null) newFilters.categoryId = filters.categoryId;
        setFilters(newFilters);
        setLocal(newFilters);
        setErr(null);
    }

    return (
        <div className={`filter-content${isClosing ? ' closing' : ''}`} onClick={onClose}>
            <nav className={`filter-container ${isOpen && !isClosing ? 'open' : ''} ${isClosing ? 'closing' : ''}`}
                 onClick={e => e.stopPropagation()}>
                <header className="filter-header">
                    <button className='onClose' aria-label="Закрыть фильтры" onClick={onClose}>✕</button>
                    <h3>Фильтрация поиска</h3>
                </header>
                <div className='filter-body'>
                    <input
                        className='filter-section'
                        id="f-search"
                        type="text"
                        value={local.search ?? ''}
                        onChange={e => change('search', e.target.value)}
                        placeholder="Название или описание"
                    />
                    <section className='filter-section'>
                        <label>Цена</label>
                        <div className='price'>
                            <input type="number"
                            placeholder='от'
                            value={local.minPrice}
                            onChange={e => change('minPrice', e.target.value === '' ? undefined : Number(e.target.value))}
                            min={0}/>
                            <input type="number"
                                   placeholder='до'
                                   value={local.maxPrice}
                                   onChange={e => change('maxPrice', e.target.value === '' ? undefined : Number(e.target.value))}
                                   min={100}/>
                        </div>
                        {err && <div className='error-msg'>{err}</div>}
                    </section>
                    <section className='filter-section'>
                        <label className='checkbox-label'>
                            <input type="checkbox" checked={!!local.available}
                            onChange={e => change('available', e.target.checked)}/>
                            Только в наличии
                        </label>
                    </section>
                    <section className='filter-section'>
                        <label htmlFor="sort">Сортировать</label>
                        <div className='sorting'>
                            <select name="sort" id="sort" value={local.sortBy ?? 'createdAt'}
                            onChange={e=>change('sortBy', e.target.value)}>
                                <option value="createdAt">По дате</option>
                                <option value="price">По цене</option>
                                <option value="rating">По рейтингу</option>
                                <option value="name">По названию</option>
                            </select>
                            <select value={local.sortOrder ?? 'desc'} onChange={e=>change('sortOrder', e.target.value)}>
                                <option value="asc">По возрастанию</option>
                                <option value="desc">По убыванию</option>
                            </select>
                        </div>
                        <footer className='filter-actions'>
                            <button type='button' className='submit-btn' onClick={reset}>Сбросить</button>
                            <button type='button' className='submit-btn' onClick={apply}>Применить</button>
                        </footer>
                    </section>
                </div>
            </nav>
        </div>
    )
}
export default FilterPage;