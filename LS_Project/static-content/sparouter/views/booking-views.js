import { div, h2, p, button, form, input, label, small, span, a } from '../dsl/html-dsl.js';
import { buildHashPagination } from '../handlers/handler-utils.js';

const DATE_FORMATTER = new Intl.DateTimeFormat('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric'
});

function getDateMeta(booking) {
    const startDate = new Date(booking.startDate);
    const endDate = new Date(booking.endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const nights = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));
    const isPast = endDate < today;
    const isUpcoming = startDate > today;

    let statusClass = 'active';
    let statusLabel = 'Active stay';

    if (isPast) {
        statusClass = 'past';
        statusLabel = 'Completed';
    } else if (isUpcoming) {
        statusClass = 'upcoming';
        statusLabel = 'Upcoming';
    }

    return {
        startDate,
        endDate,
        nights,
        statusClass,
        statusLabel
    };
}

function createBookingCard(booking, { onDetails, onEdit, onDelete } = {}) {
    const meta = getDateMeta(booking);
    let detailsButton = null;
    let editButton = null;
    let deleteButton = null;

    if (onDetails) {
        detailsButton = a(
            {
                href: onDetails(booking.id),
                className: 'btn btn-sm btn-outline-primary'
            },
            'Details'
        );
    }

    if (onEdit) {
        editButton = button(
            {
                className: 'btn btn-sm btn-primary',
                onclick: onEdit
            },
            'Edit'
        );
    }

    if (onDelete) {
        deleteButton = button(
            {
                className: 'btn btn-sm btn-danger',
                onclick: onDelete
            },
            'Delete'
        );
    }

    return div(
        { className: 'booking-item booking-item-professional' },
        div(
            { className: 'booking-item-top' },
            div(
                { className: 'booking-item-title-wrap' },
                div({ className: 'booking-title-row' },
                    span({ className: 'booking-id-pill' }, 'Booking'),
                    span({ className: `booking-status ${meta.statusClass}` }, meta.statusLabel)
                )
            ),
            div(
                { className: 'booking-actions-group' },
                detailsButton,
                editButton,
                deleteButton
            )
        ),
        div(
            { className: 'booking-dates-grid' },
            div(
                { className: 'booking-date-block' },
                small({ className: 'booking-date-label' }, 'Check-in'),
                div({ className: 'booking-date-value' }, DATE_FORMATTER.format(meta.startDate))
            ),
            div(
                { className: 'booking-date-block' },
                small({ className: 'booking-date-label' }, 'Check-out'),
                div({ className: 'booking-date-value' }, DATE_FORMATTER.format(meta.endDate))
            ),
            div(
                { className: 'booking-date-block' },
                small({ className: 'booking-date-label' }, 'Duration'),
                div({ className: 'booking-date-value' }, `${meta.nights} nights`)
            )
        )
    );
}

export function renderBookingsLayout(bookings, skip, limit, callbacks = {}, options = {}) {
    const title = options.title || 'My Bookings';
    const subtitle = options.subtitle || `Showing ${skip + 1} to ${skip + bookings.length}`;
    const basePath = options.basePath || 'bookings/me';
    const extraQuery = options.extraQuery || '';

    return div(
        { className: 'bookings-shell' },
        div(
            { className: 'bookings-header' },
            h2({ className: 'mb-1' }, title),
            p({ className: 'bookings-header-subtitle mb-0' }, subtitle)
        ),
        div(
            { className: 'booking-list' },
            bookings.map((booking) => {
                let onEdit = null;
                let onDelete = null;

                if (callbacks.onEdit) {
                    onEdit = () => callbacks.onEdit(booking.id);
                }

                if (callbacks.onDelete) {
                    onDelete = () => callbacks.onDelete(booking.id);
                }

                return createBookingCard(booking, {
                    onDetails: callbacks.onDetails,
                    onEdit,
                    onDelete
                });
            })
        ),
        buildHashPagination({
            basePath,
            skip,
            limit,
            itemCount: bookings.length,
            ariaLabel: 'Bookings pagination',
            extraQuery
        })
    );
}

export function renderBookingDetails(booking) {
    const meta = getDateMeta(booking);

    return div(
        { className: 'bookings-shell' },
        div(
            { className: 'card p-4' },
            small({ className: 'text-uppercase text-muted' }, 'Booking'),
            h2({ className: 'mb-2' }, 'Booking Details'),
            p({ className: 'text-muted mb-4' }, meta.statusLabel),
            div(
                { className: 'booking-dates-grid mb-4' },
                div(
                    { className: 'booking-date-block' },
                    small({ className: 'booking-date-label' }, 'Check-in'),
                    div({ className: 'booking-date-value' }, DATE_FORMATTER.format(meta.startDate))
                ),
                div(
                    { className: 'booking-date-block' },
                    small({ className: 'booking-date-label' }, 'Check-out'),
                    div({ className: 'booking-date-value' }, DATE_FORMATTER.format(meta.endDate))
                ),
                div(
                    { className: 'booking-date-block' },
                    small({ className: 'booking-date-label' }, 'Duration'),
                    div({ className: 'booking-date-value' }, `${meta.nights} nights`)
                )
            ),
            div(
                { className: 'd-flex gap-2 flex-wrap' },
                a({ href: `#houses/${booking.houseId}`, className: 'btn btn-outline-primary' }, 'House Details'),
                a({ href: `#users/${booking.userId}`, className: 'btn btn-outline-primary' }, 'Booker Details'),
                a({ href: '#bookings/me', className: 'btn btn-outline-secondary' }, 'My Bookings')
            )
        )
    );
}

export function createEditBookingElements({ startDateStr, endDateStr, onsubmit }) {
    const messageContainer = div({ id: 'editBookingMessage' });
    const saveBtn = button({ type: 'submit', className: 'btn btn-success', id: 'saveBookingBtn' }, 'Save Changes');

    const endDateInput = input({
        type: 'date',
        className: 'form-control',
        id: 'endDate',
        value: endDateStr,
        required: true
    });

    const editForm = form(
        {
            id: 'editBookingForm',
            className: 'booking-edit-form',
            onsubmit
        },
        div(
            { className: 'booking-edit-shell' },
            div(
                { className: 'booking-edit-summary' },
                small({ className: 'text-uppercase text-muted' }, 'Booking'),
                h2({ className: 'mb-2' }, 'Reservation details')
            ),
            div(
                { className: 'booking-edit-fields' },
                messageContainer,
                div(
                    { className: 'mb-3 field-stack' },
                    label({ className: 'form-label', for: 'startDate' }, 'Check-in date'),
                    input({
                        type: 'date',
                        className: 'form-control',
                        id: 'startDate',
                        value: startDateStr,
                        required: true
                    })
                ),
                div(
                    { className: 'mb-3 field-stack' },
                    label({ className: 'form-label', for: 'endDate' }, 'Check-out date'),
                    endDateInput
                ),
                div(
                    { className: 'd-flex gap-2 flex-wrap' },
                    saveBtn,
                    a({ href: '#bookings/me', className: 'btn btn-outline-secondary' }, 'Cancel')
                )
            )
        )
    );

    return { messageContainer, saveBtn, editForm };
}

export function renderEditBookingPage(editForm) {
    return div(
        { className: 'bookings-shell' },
        h2({ className: 'mb-4' }, 'Edit Booking'),
        editForm
    );
}

export function createBookingPageElements({ house, onsubmit }) {
    const messageContainer = div({ id: 'createBookingMessage' });
    const submitBtn = button({ type: 'submit', className: 'btn btn-success' }, 'Create Booking');
    const bookingForm = form(
        { className: 'card p-4', onsubmit },
        h2({ className: 'h4 mb-2' }, 'Create Booking'),
        p({ className: 'text-muted mb-4' }, house.title),
        messageContainer,
        div(
            { className: 'mb-3' },
            label({ className: 'form-label', for: 'bookingCreateStartDate' }, 'Check-in date'),
            input({ className: 'form-control', id: 'bookingCreateStartDate', type: 'date', required: true })
        ),
        div(
            { className: 'mb-3' },
            label({ className: 'form-label', for: 'bookingCreateEndDate' }, 'Check-out date'),
            input({ className: 'form-control', id: 'bookingCreateEndDate', type: 'date', required: true })
        ),
        div(
            { className: 'd-flex gap-2 flex-wrap' },
            submitBtn,
            a({ href: `#houses/${house.id}`, className: 'btn btn-outline-secondary' }, 'Cancel')
        )
    );

    return {
        messageContainer,
        submitBtn,
        node: div({ className: 'bookings-shell' }, bookingForm)
    };
}
