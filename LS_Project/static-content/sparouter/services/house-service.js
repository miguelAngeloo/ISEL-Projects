import {request, buildQuery} from './api.js';

export const houseService = {
    getAllHouses: (skip = 0, limit = 10, locationId = null) => {
        const query = buildQuery({skip, limit, locationId});
        return request(`/houses?${query}`);
    },

    countHouses: () =>
        request('/houses/count'),

    getHouseDetails: (id) =>
        request(`/houses/${id}`),

    createHouse: (houseData) =>
        request('/houses', 'POST', houseData),
    searchHouses: (filters) =>
        request(`/houses/search?${buildQuery(filters)}`),

    getAvailableDays: (houseId, year, month) =>
        request(`/houses/${houseId}/available-days?${buildQuery({year, month})}`)
};
