import {
    a,
    button,
    div,
    form,
    h2,
    h5,
    input,
    label,
    li,
    nav,
    ol,
    p,
    small,
    span,
    textarea
} from '../dsl/html-dsl.js';
import {buildHashPagination} from '../handlers/handler-utils.js';
import { searchLocation } from '../utils/location-search-bar.js';

function formatPrice(value) {
    const amount = Number(value);
    if (Number.isNaN(amount)) return `${value} EUR`;
    return `${amount.toFixed(0)} EUR`;
}

function createHouseCard(house, skip, limit) {
    return div(
        {className: 'house-item house-item-professional'},
        div(
            {className: 'house-item-content'},
            div(
                {className: 'house-item-top'},
                h5({className: 'house-item-title'}, house.title),
                span({className: 'house-item-tag'}, 'Available')
            ),
            p({className: 'house-item-description'}, house.description || 'No description available.'),
            div(
                {className: 'house-item-footer'},
                div(
                    {},
                    small({className: 'house-price-label'}, 'From'),
                    span({className: 'house-item-price'}, `${formatPrice(house.pricePerNight)} / night`)
                ),
                a(
                    {
                        href: `#houses/${house.id}?skip=${skip}&limit=${limit}`,
                        className: 'btn btn-sm btn-primary'
                    },
                    'Details'
                )
            )
        )
    );
}

export function renderHousesLayout({
                                       filteredHouses,
                                       skip,
                                       limit,
                                       hasNextPage,
                                       locationId,
                                       minPrice,
                                       maxPrice,
                                       token
                                   }) {
    let queryParts = [];
    if (locationId) queryParts.push(`locationId=${encodeURIComponent(locationId)}`);
    if (minPrice) queryParts.push(`minPrice=${encodeURIComponent(minPrice)}`);
    if (maxPrice) queryParts.push(`maxPrice=${encodeURIComponent(maxPrice)}`);
    let extraQuery = '';

    if (queryParts.length > 0) {
        extraQuery = '&' + queryParts.join('&');
    }

    let createHouseLink = null;
    if (token) {
        createHouseLink = a({href: '#houses/create', className: 'btn btn-success'}, 'Create House');
    }

    return div(
        {className: 'houses-shell'},
        div(
            {className: 'houses-header d-flex justify-content-between align-items-center mb-2'},
            div(
                {},
                h2({className: 'mb-1'}, 'All Houses')
            ),
            div(
                {className: 'd-flex align-items-center gap-3'},
                span({className: 'houses-count-pill'}, `Showing ${skip + 1} to ${skip + filteredHouses.length}`),
                createHouseLink
            )
        ),
        div(
            {className: 'houses-grid'},
            filteredHouses.map((house) => createHouseCard(house, skip, limit))
        ),
        buildHashPagination({
            basePath: 'houses',
            skip,
            limit,
            itemCount: filteredHouses.length,
            ariaLabel: 'House pagination',
            hasNextPage,
            extraQuery
        })
    );
}

export function createPricePredictionPanel({onsubmit}) {
    const predictionMessage = div({id: 'predictionMessage'});
    const nightlyValue = span({className: 'prediction-value', id: 'predictionNightlyValue'}, 'Select dates');
    const totalValue = span({className: 'prediction-value', id: 'predictionTotalValue'}, 'Select dates');
    const submitBtn = button({
        className: 'btn btn-outline-primary w-100',
        type: 'submit',
        id: 'predictionSubmitBtn'
    }, 'Estimate stay');

    const predictionForm = form(
        {
            id: 'predictionForm',
            className: 'prediction-form',
            onsubmit
        },
        div(
            {className: 'prediction-grid'},
            div(
                {className: 'field-stack'},
                label({className: 'form-label', for: 'predictionStartDate'}, 'Check-in date'),
                input({
                    type: 'date',
                    className: 'form-control',
                    id: 'predictionStartDate',
                    required: true
                })
            ),
            div(
                {className: 'field-stack'},
                label({className: 'form-label', for: 'predictionEndDate'}, 'Check-out date'),
                input({
                    type: 'date',
                    className: 'form-control',
                    id: 'predictionEndDate',
                    required: true
                })
            )
        ),
        div(
            {className: 'prediction-results'},
            div(
                {className: 'prediction-result-block'},
                small({className: 'prediction-label'}, 'Estimated nightly rate'),
                nightlyValue
            ),
            div(
                {className: 'prediction-result-block'},
                small({className: 'prediction-label'}, 'Estimated total stay'),
                totalValue
            )
        ),
        submitBtn
    );

    return {predictionMessage, nightlyValue, totalValue, submitBtn, node: div({}, predictionMessage, predictionForm)};
}

export function renderHouseDetailLayout({house, locationName, ownerName, predictionContent, onBookingSearchSubmit}) {
    let locationContent = locationName;
    if (house.locationId) {
        locationContent = a({href: `#locations/${house.locationId}`}, locationName);
    }

    let areaContent = 'N/A';
    if (house.areaSqMt) {
        areaContent = `${house.areaSqMt} m²`;
    }

    let ownerContent = 'N/A';
    if (house.ownerId) {
        ownerContent = a({href: `#users/${house.ownerId}`}, ownerName || 'Owner');
    }

    return div(
        {className: 'house-detail-shell'},
        div(
            {className: 'house-detail-header p-4 mb-4 rounded house-detail-hero'},
            small({className: 'house-detail-kicker'}, 'Property'),
            h2({className: 'mb-2'}, house.title),
            nav(
                {'aria-label': 'breadcrumb'},
                ol(
                    {className: 'breadcrumb mb-0'},
                    li(
                        {className: 'breadcrumb-item'},
                        a({href: '#houses', className: 'house-breadcrumb-link'}, 'Houses')
                    ),
                    li(
                        {className: 'breadcrumb-item active house-breadcrumb-current', 'aria-current': 'page'},
                        'Details'
                    )
                )
            )
        ),
        div(
            {className: 'house-detail-grid'},
            div(
                {className: 'house-detail-main'},
                div(
                    {className: 'card p-4 h-100 house-overview-card'},
                    h5({className: 'border-bottom pb-2'}, 'Description'),
                    p({className: 'lead mb-4 house-description-text'}, house.description || 'No description available.'),
                    div(
                        {className: 'house-meta-grid'},
                        div(
                            {className: 'house-meta-block'},
                            small({className: 'house-meta-label'}, 'Nightly rate'),
                            span({className: 'house-meta-value'}, `${formatPrice(house.pricePerNight)} / night`)
                        ),
                        div(
                            {className: 'house-meta-block'},
                            small({className: 'house-meta-label'}, 'Reservation'),
                            span({className: 'house-meta-value'}, 'Instant booking')
                        ),
                        div(
                            {className: 'house-meta-block'},
                            small({className: 'house-meta-label'}, 'Location'),
                            span({className: 'house-meta-value'}, locationContent)
                        ),
                        div(
                            {className: 'house-meta-block'},
                            small({className: 'house-meta-label'}, 'Area'),
                            span({className: 'house-meta-value'}, areaContent)
                        ),
                        div(
                            {className: 'house-meta-block'},
                            small({className: 'house-meta-label'}, 'Owner'),
                            span({className: 'house-meta-value'}, ownerContent)
                        )
                    )
                )
            ),
            div(
                {className: 'house-detail-side'},
                div(
                    {className: 'card p-4 h-100 house-booking-card'},
                    small({className: 'text-muted text-uppercase'}, 'Current listed price'),
                    h2({className: 'price-highlight mb-3'}, `${formatPrice(house.pricePerNight)}`),
                    div(
                        {className: 'prediction-panel'},
                        h5({className: 'prediction-panel-title'}, 'Price prediction'),
                        predictionContent
                    ),
                    form(
                        {className: 'mt-4 border-top pt-3', onsubmit: onBookingSearchSubmit},
                        h5({className: 'mb-3'}, 'Search bookings'),
                        div(
                            {className: 'field-stack mb-2'},
                            label({className: 'form-label', for: 'bookingSearchStartDate'}, 'Start date'),
                            input({
                                className: 'form-control',
                                id: 'bookingSearchStartDate',
                                type: 'date',
                                required: true
                            })
                        ),
                        div(
                            {className: 'field-stack mb-3'},
                            label({className: 'form-label', for: 'bookingSearchEndDate'}, 'End date'),
                            input({className: 'form-control', id: 'bookingSearchEndDate', type: 'date', required: true})
                        ),
                        button({className: 'btn btn-outline-primary w-100', type: 'submit'}, 'Search House Bookings')
                    ),
                    div(
                        {className: 'd-flex gap-2 flex-wrap mt-3'},
                        a({
                            href: `#houses/${house.id}/bookings/create`,
                            className: 'btn btn-success'
                        }, 'Create Booking'),
                        a({
                            href: `#houses/${house.id}/bookings`,
                            className: 'btn btn-outline-primary'
                        }, 'House Bookings'),
                        a({
                            href: `#houses/${house.id}/available-days`,
                            className: 'btn btn-outline-success'
                        }, 'Available Days'),
                        a({href: '#houses', className: 'btn btn-link text-secondary px-0'}, 'Back to list')
                    )
                )
            )
        )
    );
}

export function createHouseFormLayout({onsubmit, locations = [], selectedLocationId = ''}) {
    const formMessage = div({id: 'formMessage'});
    const submitBtn = button({className: 'btn btn-primary', type: 'submit'}, 'Create House');

    const locationSelect = searchLocation({
        locations,
        inputId: 'locationIdInput',
        hiddenId: 'locationId',
        selectedId: selectedLocationId
    });

    const formNode = form(
        {className: 'card p-4', onsubmit},
        h2({className: 'h4 mb-4'}, 'Create New House'),
        formMessage,
        div(
            {className: 'mb-3'},
            label({className: 'form-label', for: 'title'}, 'Title'),
            input({type: 'text', className: 'form-control', id: 'title', required: true})
        ),
        div(
            {className: 'mb-3'},
            label({className: 'form-label', for: 'description'}, 'Description'),
            textarea({className: 'form-control', id: 'description', rows: '3', required: true})
        ),
        div(
            {className: 'mb-3'},
            div(
                {className: 'd-flex justify-content-between align-items-center gap-2 flex-wrap mb-2'},
                label({className: 'form-label mb-0', for: 'locationIdInput'}, 'Location'),
                a({
                    href: '#locations/create?returnTo=houses/create',
                    className: 'btn btn-sm btn-outline-primary'
                }, 'Create Location')
            ),
            locationSelect
        ),
        div(
            {className: 'row'},
            div(
                {className: 'col-md-6 mb-3'},
                label({className: 'form-label', for: 'areaSqMt'}, 'Area (sq meters)'),
                input({type: 'number', className: 'form-control', id: 'areaSqMt', required: true, min: '1'})
            ),
            div(
                {className: 'col-md-6 mb-3'},
                label({className: 'form-label', for: 'pricePerNight'}, 'Price Per Night (EUR)'),
                input({
                    type: 'number',
                    step: '0.01',
                    className: 'form-control',
                    id: 'pricePerNight',
                    required: true,
                    min: '1'
                })
            )
        ),
        div(
            {className: 'd-flex gap-2'},
            submitBtn,
            a({href: '#houses', className: 'btn btn-outline-secondary'}, 'Cancel')
        )
    );

    return {formMessage, submitBtn, node: div({className: 'create-house-shell mx-auto'}, formNode)};
}

export function renderAvailableDaysPage({house, days, year, month, onsubmit}) {
    const monthValue = `${year}-${String(month).padStart(2, '0')}`;
    let daysContent = p({className: 'text-muted mb-0'}, 'No available days in this month.');

    if (days.length > 0) {
        const dayBadges = days.map((day) => span({className: 'badge text-bg-success p-2'}, String(day).slice(8, 10)));
        daysContent = div({className: 'd-flex gap-2 flex-wrap'}, dayBadges);
    }

    return div(
        {className: 'houses-shell'},
        div(
            {className: 'card p-4'},
            small({className: 'text-uppercase text-muted'}, 'Availability'),
            h2({className: 'mb-2'}, house.title),
            p({className: 'text-muted mb-4'}, 'Available days for the selected month.'),
            form(
                {className: 'd-flex gap-2 flex-wrap align-items-end mb-4', onsubmit},
                div(
                    {},
                    label({className: 'form-label', for: 'availabilityMonth'}, 'Month'),
                    input({
                        className: 'form-control',
                        id: 'availabilityMonth',
                        type: 'month',
                        value: monthValue,
                        required: true
                    })
                ),
                button({className: 'btn btn-primary', type: 'submit'}, 'Show Days'),
                a({href: `#houses/${house.id}`, className: 'btn btn-outline-secondary'}, 'Back to House')
            ),
            daysContent
        )
    );
}
