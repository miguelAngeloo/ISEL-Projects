import { bookingService } from '../services/booking-service.js';
import { houseService } from '../services/house-service.js';
import { commonHandlers } from './common-handlers.js';
import { readPaginationFromHash, readHashQuery, getHashPathSegment, isDateRangeValid, toDateInputValue } from './handler-utils.js';
import {
    renderBookingsLayout,
    createEditBookingElements,
    renderEditBookingPage,
    renderBookingDetails,
    createBookingPageElements
} from '../views/booking-views.js';

function renderLoginRequired(mainContent) {
    mainContent.replaceChildren(
        commonHandlers.renderEmptyState({
            title: 'Session required',
            actionLabel: 'Go to login',
            onAction: () => { window.location.hash = '#login'; }
        })
    );
}

function renderNoBookings(mainContent) {
    mainContent.replaceChildren(
        commonHandlers.renderEmptyState({
            title: 'No bookings yet',
            actionLabel: 'Browse houses',
            onAction: () => { window.location.hash = '#houses'; }
        })
    );
}

function readEditBookingDates() {
    return {
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value
    };
}

function readCreateBookingDates() {
    return {
        startDate: document.getElementById('bookingCreateStartDate').value,
        endDate: document.getElementById('bookingCreateEndDate').value
    };
}

function renderBookingDateError(messageContainer) {
    messageContainer.replaceChildren(commonHandlers.renderInlineError({
        title: 'Invalid date range',
        message: 'Check-out date must be after check-in date.'
    }));
}

function createEditBookingSubmitHandler({ bookingId, booking, messageContainer, saveBtn }) {
    return async (event) => {
        event.preventDefault();
        messageContainer.replaceChildren();

        const { startDate, endDate } = readEditBookingDates();

        if (!isDateRangeValid(startDate, endDate)) {
            renderBookingDateError(messageContainer);
            return;
        }

        saveBtn.disabled = true;
        saveBtn.textContent = 'Updating...';

        try {
            await bookingService.updateBooking(bookingId, booking.houseId, startDate, endDate);
            commonHandlers.showToast('Booking updated successfully.', 'success');
            window.location.hash = '#bookings/me';
        } catch (err) {
            messageContainer.replaceChildren(commonHandlers.renderInlineError({
                title: 'Booking update failed',
                message: err.message
            }));
        } finally {
            saveBtn.disabled = false;
            saveBtn.textContent = 'Save Changes';
        }
    };
}

function createBookingSubmitHandler(house, pageElements) {
    return async (event) => {
        event.preventDefault();
        pageElements.messageContainer.replaceChildren();

        const { startDate, endDate } = readCreateBookingDates();

        if (!isDateRangeValid(startDate, endDate)) {
            renderBookingDateError(pageElements.messageContainer);
            return;
        }

        pageElements.submitBtn.disabled = true;
        pageElements.submitBtn.textContent = 'Creating...';

        try {
            await bookingService.createBooking({
                hid: Number(house.id),
                startDate,
                endDate
            });
            commonHandlers.showToast('Booking created successfully.', 'success');
            window.location.hash = `#houses/${house.id}/bookings`;
        } catch (err) {
            pageElements.messageContainer.replaceChildren(commonHandlers.renderInlineError({
                title: 'Could not create booking',
                message: err.message
            }));
        } finally {
            pageElements.submitBtn.disabled = false;
            pageElements.submitBtn.textContent = 'Create Booking';
        }
    };
}

function refreshCurrentRoute() {
    window.dispatchEvent(new HashChangeEvent('hashchange'));
}

export const bookingHandlers = {
    getMyBookings: async (mainContent) => {
        const { skip, limit } = readPaginationFromHash(6);

        if (!sessionStorage.getItem('token')) {
            renderLoginRequired(mainContent);
            return;
        }

        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(3));

        try {
            const data = await bookingService.getMyBookings(skip, limit);
            const bookings = data.bookings || [];

            if (bookings.length === 0) {
                renderNoBookings(mainContent);
                return;
            }

            mainContent.replaceChildren(
                renderBookingsLayout(bookings, skip, limit, {
                    onDetails: (id) => `#bookings/${id}`,
                    onEdit: (id) => { window.location.hash = `#bookings/${id}/edit`; },
                    onDelete: (id) => bookingHandlers.deleteBookingConfirm(id)
                })
            );
        } catch (err) {
            mainContent.replaceChildren(
                commonHandlers.renderInlineError({
                    title: 'Unable to load bookings',
                    message: err.message
                })
            );
        }
    },

    deleteBookingConfirm: async (bookingId) => {
        const accepted = await commonHandlers.askConfirmation({
            title: 'Delete this booking?',
            message: 'This action cannot be undone.',
            confirmLabel: 'Delete booking',
            cancelLabel: 'Keep booking'
        });

        if (!accepted) return;
        await bookingHandlers.deleteBooking(bookingId);
    },

    deleteBooking: async (bookingId) => {
        try {
            await bookingService.deleteBooking(bookingId);
            commonHandlers.showToast('Booking deleted.', 'success');
            refreshCurrentRoute();
        } catch (err) {
            commonHandlers.showToast(err.message, 'error');
        }
    },

    getHouseBookings: async (mainContent) => {
        const houseId = getHashPathSegment(1);
        const { skip, limit } = readPaginationFromHash(6);
        const params = readHashQuery();
        const startDate = params.get('startDate');
        const endDate = params.get('endDate');
        let hasDateInterval = false;
        let extraQuery = '';

        if (startDate) {
            if (endDate) {
                hasDateInterval = true;
            }
        }

        if (hasDateInterval) {
            extraQuery = `&startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`;
        }

        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(3));

        try {
            const data = await bookingService.getBookings(houseId, skip, limit, startDate, endDate);
            const bookings = data.bookings || [];

            if (bookings.length === 0) {
                mainContent.replaceChildren(
                    commonHandlers.renderEmptyState({
                        title: 'No bookings for this house',
                        actionLabel: 'Back to house',
                        onAction: () => { window.location.hash = `#houses/${houseId}`; }
                    })
                );
                return;
            }

            let subtitle = `Showing ${skip + 1} to ${skip + bookings.length} for this house`;

            if (hasDateInterval) {
                subtitle = `Showing ${skip + 1} to ${skip + bookings.length} between ${startDate} and ${endDate}`;
            }

            mainContent.replaceChildren(
                renderBookingsLayout(
                    bookings,
                    skip,
                    limit,
                    {
                        onDetails: (id) => `#bookings/${id}`
                    },
                    {
                        title: 'House Bookings',
                        subtitle,
                        basePath: `houses/${houseId}/bookings`,
                        extraQuery
                    }
                )
            );
        } catch (err) {
            mainContent.replaceChildren(
                commonHandlers.renderInlineError({
                    title: 'Unable to load house bookings',
                    message: err.message
                })
            );
        }
    },

    getUserBookings: async (mainContent) => {
        const userId = getHashPathSegment(1);
        const { skip, limit } = readPaginationFromHash(6);

        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(3));

        try {
            const data = await bookingService.getUserBookings(userId, skip, limit);
            const bookings = data.bookings || [];

            if (bookings.length === 0) {
                mainContent.replaceChildren(
                    commonHandlers.renderEmptyState({
                        title: 'No bookings for this user',
                        actionLabel: 'Back to user',
                        onAction: () => { window.location.hash = `#users/${userId}`; }
                    })
                );
                return;
            }

            mainContent.replaceChildren(
                renderBookingsLayout(
                    bookings,
                    skip,
                    limit,
                    {
                        onDetails: (id) => `#bookings/${id}`
                    },
                    {
                        title: 'User Bookings',
                        subtitle: `Showing ${skip + 1} to ${skip + bookings.length}`,
                        basePath: `users/${userId}/bookings`
                    }
                )
            );
        } catch (err) {
            mainContent.replaceChildren(
                commonHandlers.renderInlineError({
                    title: 'Unable to load user bookings',
                    message: err.message
                })
            );
        }
    },

    getBookingDetails: async (mainContent) => {
        const bookingId = getHashPathSegment(1);
        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(1));

        try {
            const booking = await bookingService.getBookingDetails(bookingId);
            mainContent.replaceChildren(renderBookingDetails(booking));
        } catch (err) {
            mainContent.replaceChildren(
                commonHandlers.renderInlineError({
                    title: 'Unable to load booking',
                    message: err.message
                })
            );
        }
    },

    getCreateBooking: async (mainContent) => {
        if (!sessionStorage.getItem('token')) {
            renderLoginRequired(mainContent);
            return;
        }

        const houseId = getHashPathSegment(1);
        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(1));

        try {
            const house = await houseService.getHouseDetails(houseId);
            let pageElements;
            pageElements = createBookingPageElements({
                house,
                onsubmit: (event) => createBookingSubmitHandler(house, pageElements)(event)
            });

            mainContent.replaceChildren(pageElements.node);
        } catch (err) {
            mainContent.replaceChildren(commonHandlers.renderInlineError({
                title: 'Unable to load booking form',
                message: err.message
            }));
        }
    },

    editBooking: async (mainContent) => {
        if (!sessionStorage.getItem('token')) {
            renderLoginRequired(mainContent);
            return;
        }

        const bookingId = getHashPathSegment(1);
        mainContent.replaceChildren(commonHandlers.renderSectionSkeleton(1));

        try {
            const booking = await bookingService.getBookingDetails(bookingId);
            const startDateStr = toDateInputValue(booking.startDate);
            const endDateStr = toDateInputValue(booking.endDate);

            let editElements;
            editElements = createEditBookingElements({
                startDateStr,
                endDateStr,
                onsubmit: (event) => createEditBookingSubmitHandler({
                    bookingId,
                    booking,
                    messageContainer: editElements.messageContainer,
                    saveBtn: editElements.saveBtn
                })(event)
            });

            mainContent.replaceChildren(renderEditBookingPage(editElements.editForm));
        } catch (err) {
            mainContent.replaceChildren(
                commonHandlers.renderInlineError({
                    title: 'Unable to load booking',
                    message: err.message
                })
            );
        }
    }
};
