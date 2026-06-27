import {houseService} from '../services/house-service.js';
import {locationService} from '../services/location-service.js';
import {predictionService} from '../services/prediction-service.js';
import {userService} from '../services/user-service.js';
import {commonHandlers} from './common-handlers.js';
import {readPaginationFromHash, readHashQuery, isDateRangeValid, getHashPathSegment} from './handler-utils.js';
import {
    renderHousesLayout,
    createPricePredictionPanel,
    renderHouseDetailLayout,
    createHouseFormLayout,
    renderAvailableDaysPage
} from '../views/house-views.js';

function readHouseFilters() {
    const {skip, limit} = readPaginationFromHash(10);
    const params = readHashQuery();

    return {
        skip,
        limit,
        locationId: params.get('locationId'),
        minPrice: params.get('minPrice'),
        maxPrice: params.get('maxPrice'),
    };
}

function hasPriceFilter(filters) {
    if (filters.minPrice) return true;
    return !!filters.maxPrice;


}

function hasHouseSearch(filters) {
    if (!filters.locationId) return false;
    return hasPriceFilter(filters);
}

async function loadHousesForCurrentFilters(filters) {
    if (hasHouseSearch(filters)) {
        const data = await houseService.searchHouses({
            locationId: filters.locationId,
            minPrice: filters.minPrice,
            maxPrice: filters.maxPrice,
            skip: filters.skip,
            limit: filters.limit
        });
        const houses = data.houses || [];
        return {
            houses,
            hasNextPage: houses.length >= filters.limit
        };
    }

    const data = await houseService.getAllHouses(filters.skip, filters.limit, filters.locationId);
    const houses = data.houses || [];
    return {
        houses,
        hasNextPage: houses.length >= filters.limit
    };
}

async function loadLocations() {
    return locationService.getAllLocations();
}

async function loadLocationsOrEmpty() {
    try {
        return await loadLocations();
    } catch (err) {
        console.warn('Could not load locations for filters:', err);
        return [];
    }
}

async function loadLocationsForHouseForm(selectedLocationId) {
    const locations = await loadLocationsOrEmpty();
    const selectedIsVisible = locations.some((location) => String(location.lid) === String(selectedLocationId));

    if (!selectedLocationId) {
        return locations;
    }

    if (selectedIsVisible) {
        return locations;
    }

    try {
        const selectedLocation = await locationService.getLocationDetails(selectedLocationId);
        return [...locations, selectedLocation];
    } catch (err) {
        console.warn(`Could not load selected location ${selectedLocationId}:`, err);
        return locations;
    }
}

async function resolveHouseLocationName(locationId) {
    const fallback = `Location #${locationId}`;

    try {
        const fullPath = await locationService.getFullPath(locationId);
        let path = [];

        if (fullPath && fullPath.path) {
            path = fullPath.path;
        }

        if (path.length > 0) {
            const names = [];

            path.forEach((location) => {
                if (location && location.name) {
                    names.push(location.name);
                }
            });

            return names.join(' > ');
        }

        const location = await locationService.getLocationDetails(locationId);
        if (location && location.name) {
            return location.name;
        }

        return fallback;
    } catch (err) {
        console.warn(`Could not resolve location ${locationId}:`, err);
        return fallback;
    }
}

async function resolveHouseOwnerName(ownerId) {
    if (!ownerId) return 'Owner';

    try {
        const owner = await userService.getUserDetails(ownerId);
        if (owner && owner.name) {
            return owner.name;
        }

        if (owner && owner.email) {
            return owner.email;
        }

        return 'Owner';
    } catch (err) {
        console.warn(`Could not resolve owner ${ownerId}:`, err);
        return 'Owner';
    }
}

function readDateInputs(startInputId, endInputId) {
    return {
        startDate: document.getElementById(startInputId).value,
        endDate: document.getElementById(endInputId).value
    };
}

function renderDateError(container, title, message) {
    container.replaceChildren(commonHandlers.renderInlineError({title, message}));
}

function validateDateInputs({startDate, endDate}, messageContainer) {
    if (!startDate) {
        renderDateError(messageContainer, 'Invalid dates', 'Both start and end dates are required.');
        return false;
    }

    if (!endDate) {
        renderDateError(messageContainer, 'Invalid dates', 'Both start and end dates are required.');
        return false;
    }

    if (!isDateRangeValid(startDate, endDate)) {
        renderDateError(messageContainer, 'Date range not valid', 'End date must be after start date.');
        return false;
    }

    return true;
}

function calculateNights(startDate, endDate) {
    return Math.ceil((new Date(endDate) - new Date(startDate)) / (1000 * 60 * 60 * 24));
}

function createPredictionSubmitHandler(house, predictionPanel) {
    return async (event) => {
        event.preventDefault();
        predictionPanel.predictionMessage.replaceChildren();

        const dates = readDateInputs('predictionStartDate', 'predictionEndDate');
        if (!validateDateInputs(dates, predictionPanel.predictionMessage)) return;

        if (!house.areaSqMt) {
            predictionPanel.predictionMessage.replaceChildren(commonHandlers.renderInlineError({
                title: 'Prediction unavailable',
                message: 'This house is missing the data required for a price estimate.'
            }));
            return;
        }

        if (!house.locationId) {
            predictionPanel.predictionMessage.replaceChildren(commonHandlers.renderInlineError({
                title: 'Prediction unavailable',
                message: 'This house is missing the data required for a price estimate.'
            }));
            return;
        }

        predictionPanel.submitBtn.disabled = true;
        predictionPanel.submitBtn.textContent = 'Estimating...';

        try {
            const nights = calculateNights(dates.startDate, dates.endDate);
            const prediction = await predictionService.getPricePrediction(
                house.areaSqMt,
                house.locationId,
                nights
            );

            predictionPanel.nightlyValue.textContent = `${Number(prediction.predictedPricePerNight).toFixed(2)} EUR`;
            predictionPanel.totalValue.textContent = `${Number(prediction.predictedTotalPrice).toFixed(2)} EUR`;
        } catch (err) {
            predictionPanel.predictionMessage.replaceChildren(commonHandlers.renderInlineError({
                title: 'Could not estimate price',
                message: err.message
            }));
        } finally {
            predictionPanel.submitBtn.disabled = false;
            predictionPanel.submitBtn.textContent = 'Estimate stay';
        }
    };
}

function createBookingSearchSubmitHandler(house) {
    return (event) => {
        event.preventDefault();

        const startDate = document.getElementById('bookingSearchStartDate').value;
        const endDate = document.getElementById('bookingSearchEndDate').value;

        if (!isDateRangeValid(startDate, endDate)) {
            commonHandlers.showToast('End date must be after start date.', 'error');
            return;
        }

        window.location.hash = `#houses/${house.id}/bookings?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`;
    };
}

function readCreateHouseForm() {
    return {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        location: Number(document.getElementById('locationId').value),
        areaSqMt: Number(document.getElementById('areaSqMt').value),
        pricePerNight: Number(document.getElementById('pricePerNight').value)
    };
}

function createHouseSubmitHandler(createForm) {
    return async (event) => {
        event.preventDefault();
        createForm.formMessage.replaceChildren();
        createForm.submitBtn.disabled = true;
        createForm.submitBtn.textContent = 'Creating...';

        try {
            const data = await houseService.createHouse(readCreateHouseForm());
            commonHandlers.showToast('House created successfully!', 'success');
            let newHouseId = data;

            if (data && data.id) {
                newHouseId = data.id;
            }

            window.location.hash = `#houses/${newHouseId}`;
        } catch (err) {
            createForm.formMessage.replaceChildren(commonHandlers.renderInlineError({
                title: 'Creation Failed',
                message: err.message
            }));
            createForm.submitBtn.disabled = false;
            createForm.submitBtn.textContent = 'Create House';
        }
    };
}

export const houseHandlers = {
    getHouses: async (mainContent) => {
        const filters = readHouseFilters();
        const token = sessionStorage.getItem('token');

        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(4));

        try {
            const {houses, hasNextPage} = await loadHousesForCurrentFilters(filters);

            if (houses.length === 0) {
                mainContent.replaceChildren(
                    commonHandlers.renderEmptyState({
                        title: 'No houses available',
                        actionLabel: 'Back to home',
                        onAction: () => {
                            window.location.hash = '#home';
                        }
                    })
                );
                return;
            }

            mainContent.replaceChildren(renderHousesLayout({
                filteredHouses: houses,
                skip: filters.skip,
                limit: filters.limit,
                hasNextPage,
                locationId: filters.locationId,
                minPrice: filters.minPrice,
                maxPrice: filters.maxPrice,
                token
            }));
        } catch (err) {
            mainContent.replaceChildren(
                commonHandlers.renderInlineError({
                    title: 'Unable to load houses',
                    message: err.message
                })
            );
        }
    },

    getHouseDetail: async (mainContent) => {
        const id = getHashPathSegment(1);
        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(1));

        try {
            const house = await houseService.getHouseDetails(id);
            const locationName = await resolveHouseLocationName(house.locationId);
            const ownerName = await resolveHouseOwnerName(house.ownerId);

            let predictionPanel;
            predictionPanel = createPricePredictionPanel({
                onsubmit: (event) => createPredictionSubmitHandler(house, predictionPanel)(event)
            });

            mainContent.replaceChildren(renderHouseDetailLayout({
                house,
                locationName,
                ownerName,
                predictionContent: predictionPanel.node,
                onBookingSearchSubmit: createBookingSearchSubmitHandler(house)
            }));
        } catch (err) {
            mainContent.replaceChildren(
                commonHandlers.renderInlineError({
                    title: 'Unable to load house details',
                    message: err.message
                })
            );
        }
    },

    getCreateHouse: async (mainContent) => {
        if (!sessionStorage.getItem('token')) {
            window.location.hash = '#login';
            return;
        }

        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(2));

        const params = readHashQuery();
        const selectedLocationId = params.get('locationId') || '';
        let createForm;
        createForm = createHouseFormLayout({
            locations: await loadLocationsForHouseForm(selectedLocationId),
            selectedLocationId,
            onsubmit: (event) => createHouseSubmitHandler(createForm)(event)
        });

        mainContent.replaceChildren(createForm.node);
    },

    getAvailableDays: async (mainContent) => {
        const id = getHashPathSegment(1);
        const params = readHashQuery();
        const today = new Date();
        const year = Number(params.get('year') || today.getFullYear());
        const month = Number(params.get('month') || today.getMonth() + 1);

        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(1));

        try {
            const [house, data] = await Promise.all([
                houseService.getHouseDetails(id),
                houseService.getAvailableDays(id, year, month)
            ]);

            mainContent.replaceChildren(renderAvailableDaysPage({
                house,
                days: data.days || [],
                year,
                month,
                onsubmit: (event) => {
                    event.preventDefault();
                    const value = document.getElementById('availabilityMonth').value;
                    const [selectedYear, selectedMonth] = value.split('-');
                    window.location.hash = `#houses/${id}/available-days?year=${selectedYear}&month=${selectedMonth}`;
                }
            }));
        } catch (err) {
            mainContent.replaceChildren(commonHandlers.renderInlineError({
                title: 'Unable to load available days',
                message: err.message
            }));
        }
    }
};
