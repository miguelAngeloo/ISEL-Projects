import { request, buildQuery } from './api.js';

export const bookingService = {
    createBooking: (bookingData) =>
        request('/bookings', 'POST', bookingData),

    getBookingDetails: (id) =>
        request(`/bookings/${id}`, 'GET'),

    getBookings: (hid, skip = 0, limit = 10, startDate = null, endDate = null) =>
        request(`/bookings?${buildQuery({ skip, limit, hid, startDate, endDate })}`, 'GET'),

    getMyBookings: (skip = 0, limit = 10) =>
        request(`/bookings/me?${buildQuery({ skip, limit })}`, 'GET'),

    getUserBookings: (userId, skip = 0, limit = 10) =>
        request(`/users/${userId}/bookings?${buildQuery({ skip, limit })}`, 'GET'),

    deleteBooking: (id) =>
        request(`/bookings/${id}`, 'DELETE'),

    updateBooking: (id, houseId, startDate, endDate) => {
        const bookingData = {
            hid: Number(houseId),
            startDate,
            endDate,
        };
        return request(`/bookings/${id}`, 'PUT', bookingData);
    },
};



