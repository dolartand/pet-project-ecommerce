import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ProfileModal from '../components/layout/ProfileModal';
import api from '../api/axios';
import { useAuth } from '../hooks/useAuth';

jest.mock('../api/axios');
const mockedApi = api as jest.Mocked<typeof api>;

jest.mock('../hooks/useAuth');
const mockedUseAuth = useAuth as jest.Mock;

describe('ProfileModal', () => {
    beforeEach(() => {
        jest.clearAllMocks();

        mockedUseAuth.mockReturnValue({
            logout: jest.fn().mockResolvedValue(undefined),
            isLoggedIn: true,
        });
    });

    test('should render readOnly field upon initial boot', async() => {
        const handleClose = jest.fn();

        const mockProfileData = {
            firstName: 'Dasha',
            lastName: 'Doe',
        };
        mockedApi.get.mockResolvedValue({data: mockProfileData});

        render(<ProfileModal handleClose={handleClose} mode='profile' />);

        const firstNameInput = await screen.findByDisplayValue('Dasha');
        const lastNameInput = await screen.findByDisplayValue('Doe');

        expect(firstNameInput).toHaveAttribute('readonly');
        expect(lastNameInput).toHaveAttribute('readonly');

        expect(mockedApi.get).toHaveBeenCalledWith('/users/profile');
    });

    test('should allow to edit after clicking on edit profile', async() => {
        const handleClose = jest.fn();

        const mockProfileData = {
            firstName: 'Dasha',
            lastName: 'Doe',
        };
        mockedApi.get.mockResolvedValue({data: mockProfileData});

        render(<ProfileModal handleClose={handleClose} mode='profile' />);

        const editBtn = await screen.findByRole('button', { name: /редактировать/i });
        fireEvent.click(editBtn);

        const firstNameInput = await screen.findByDisplayValue('Dasha');
        expect(firstNameInput).not.toHaveAttribute('readonly');
        expect(editBtn).toHaveTextContent('Сохранить');
    });

    test('should save changes if clicked save button ', async() => {
        mockedApi.get.mockResolvedValue({ data: { firstName: 'Dasha', lastName: 'Doe' } });
        mockedApi.put.mockResolvedValue({});

        render(<ProfileModal handleClose={() => {}} mode="profile" />);

        const editBtn = await screen.findByRole('button', { name: /редактировать/i });
        fireEvent.click(editBtn);

        const firstNameInput = await screen.findByDisplayValue('Dasha');
        fireEvent.change(firstNameInput, { target: { value: 'Jane' } });

        const saveBtn = await screen.findByRole('button', {name: /сохранить/i});
        fireEvent.click(saveBtn);

        await waitFor(() => {
            expect(mockedApi.put).toHaveBeenCalledWith('/users/profile', {
                firstName: 'Jane', lastName: 'Doe' });
        });
        //проблема была, что в компоненте сначала дожидаемся ответа, только потом
        // меняются состояния кнопки и полей
        await screen.findByRole('button', { name: /редактировать/i });

        expect(firstNameInput).toHaveAttribute('readonly');
    });
})