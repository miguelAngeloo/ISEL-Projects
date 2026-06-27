import { request, buildQuery } from './api.js';

function normalizeLocations(data) {
    if (!data) {
        return [];
    }

    if (data.locations) {
        return data.locations;
    }

    return data;
}

export const locationService = {
    getLocations: (skip = 0, limit = 10) =>
        request(`/locations?${buildQuery({ skip, limit })}`, 'GET'),

    getAllLocations: async () =>
        normalizeLocations(await locationService.getLocations(0, 100)),

    getLocationDetails: (id) =>
        request(`/locations/${id}`, 'GET'),

    getChildrenLocations: (id) =>
        request(`/locations/${id}/children`, 'GET'),

    getFullPath: (id) =>
        request(`/locations/${id}/fullpath`, 'GET'),

    createLocation: (locationData) =>
        request('/locations', 'POST', locationData)
};
