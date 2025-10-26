import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Categories from '../components/layout/admin/Categories';
import api from '../api/axios';

jest.mock('../api/axios');
const mockApi = api as jest.Mocked<typeof api>;

describe('Categories', () => {
    beforeEach(() => {jest.clearAllMocks();})

    test('should render correctly all categories', async () => {
        const mockCategoriesData = [
                {name: 'Telephone', description: 'Smartphone', id: 1},
                {name: 'Book', description: 'book', id: 2}
        ];
        mockApi.get.mockResolvedValue({data: mockCategoriesData});

        render(<Categories />);

        const categoryElement1 = await screen.findByText(/1\. Telephone \(Smartphone\)/i);
        const categoryElement2 = await screen.findByText(/2\. Book \(book\)/i);

        expect(categoryElement1).toBeInTheDocument();
        expect(categoryElement2).toBeInTheDocument();

        expect(mockApi.get).toHaveBeenCalledWith('/categories');
    });

    test('should correctly create new category', async () => {
        mockApi.post.mockResolvedValue({data: {description: 'Категория успешно создана'} });
        mockApi.get.mockResolvedValue({data:[]});

        render(<Categories />);

        //используем ассинхронные методы
        const nameInput = await screen.findByLabelText(/Название категории/i);
        const descriptionInput = await screen.findByLabelText(/Описание/i);
        const parentIdInput = await screen.findByLabelText(/Родительский ID/i);

        fireEvent.change(nameInput, {target: {value:'Книги'}});
        fireEvent.change(descriptionInput, {target: {value:'Детективные'}});
        fireEvent.change(parentIdInput, {target: {value:'2'}});

        const saveBtn = screen.getByRole('button', { name: /создать/i });
        fireEvent.click(saveBtn);

        const msg = await screen.findByText(/Категория успешно создана/i);
        expect(msg).toBeInTheDocument();

        expect(mockApi.post).toHaveBeenCalledWith('/admin/categories', {
            categoryPost: {
                name: 'Книги', description: 'Детективные', parentId: '2'
            }
        });

        expect(mockApi.get).toHaveBeenCalledTimes(1);
    });
})
