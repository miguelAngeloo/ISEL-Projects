import { locationService } from '../services/location-service.js';
import { commonHandlers } from './common-handlers.js';
import { getHashPathSegment, readHashQuery } from './handler-utils.js';
import { renderLocationDetails, createLocationFormLayout } from '../views/location-views.js';

function readLocationForm() {
    const parentValue = document.getElementById('locationParentId').value;
    let parentId = null;

    if (parentValue) {
        parentId = Number(parentValue);
    }

    return {
        name: document.getElementById('locationName').value,
        type: document.getElementById('locationType').value,
        parentId
    };
}

function readReturnToHouseForm() {
    const params = readHashQuery();
    const returnTo = params.get('returnTo');

    return returnTo === 'houses/create';
}

function getLocationFormCancelHref(shouldReturnToHouseForm) {
    if (shouldReturnToHouseForm) {
        return '#houses/create';
    }

    return '#home';
}

function getCreatedLocationId(result) {
    if (result && result.lid) {
        return result.lid;
    }

    return result;
}

function getLocationRedirect(shouldReturnToHouseForm, locationId) {
    if (shouldReturnToHouseForm) {
        return `#houses/create?locationId=${locationId}`;
    }

    return `#locations/${locationId}`;
}

export const locationHandlers = {
    getLocationDetails: async (mainContent) => {
        const locationId = getHashPathSegment(1);

        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(1));

        try {
            const location = await locationService.getLocationDetails(locationId);
            const childrenData = await locationService.getChildrenLocations(locationId);
            const children = childrenData.locations || [];

            mainContent.replaceChildren(renderLocationDetails(location, children));
        } catch (err) {
            mainContent.replaceChildren(
                commonHandlers.renderInlineError({
                    title: 'Unable to load location',
                    message: err.message
                })
            );
        }
    },

    getCreateLocation: async (mainContent) => {
        if (!sessionStorage.getItem('token')) {
            window.location.hash = '#login';
            return;
        }

        const shouldReturnToHouseForm = readReturnToHouseForm();

        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(1));

        try {
            const locations = await locationService.getAllLocations();

            mainContent.replaceChildren(createLocationFormLayout({
                locations,
                cancelHref: getLocationFormCancelHref(shouldReturnToHouseForm),
                onsubmit: async (event) => {
                    event.preventDefault();
                    const messageContainer = document.getElementById('locationFormMessage');
                    messageContainer.replaceChildren();

                    try {
                        const result = await locationService.createLocation(readLocationForm());
                        const locationId = getCreatedLocationId(result);
                        commonHandlers.showToast('Location created successfully.', 'success');
                        window.location.hash = getLocationRedirect(shouldReturnToHouseForm, locationId);
                    } catch (err) {
                        messageContainer.replaceChildren(commonHandlers.renderInlineError({
                            title: 'Could not create location',
                            message: err.message
                        }));
                    }
                }
            }));
        } catch (err) {
            mainContent.replaceChildren(commonHandlers.renderInlineError({
                title: 'Unable to load location form',
                message: err.message
            }));
        }
    }
};
