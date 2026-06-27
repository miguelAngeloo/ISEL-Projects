import { request } from './api.js';

export const userService = {
    getUserDetails: (uid) =>
        request(`/users/${uid}`, 'GET'),

    updateUser: (name) =>
        request('/users', 'PUT', { name }),

    deleteUser: () =>
        request('/users', 'DELETE')
};
