import { test, expect } from '@playwright/test';

test.describe('Authentication Test', () => {

    test('should be able to log in and view their profile', async({page}) => {
        await page.goto('http://localhost:3000');
        await page.getByText('Войти').click();

        await expect(page.getByRole('heading', {name: 'Вход в профиль'})).toBeVisible();

        await page.getByLabel('Почта').fill('artem@mail.ru');
        await page.getByLabel('Пароль').fill('Artem357');

        await page.getByRole('button', {name: 'Войти'}).click();

        await expect(page.getByText('Профиль')).toBeVisible();

        await page.getByText('Профиль').click();

        await expect(page.getByRole('heading', {name: 'Профиль'})).toBeVisible();
        await expect(page.getByLabel('Имя')).toHaveValue('Артем');
        await expect(page.getByLabel('Фамилия')).toHaveValue('Пимошенко');
    })
})