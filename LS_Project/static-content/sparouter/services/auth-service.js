import { request } from './api.js';

export const authService = {
    login: (email, password) =>
        request('/auth/login', 'POST', { email, password }),

    register: (name, email, password) =>
        request('/auth/register', 'POST', { name, email, password }),

    logout: () =>
        request('/auth/logout', 'POST')
};