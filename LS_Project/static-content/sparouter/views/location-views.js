import { a, div, h2, p, small, span, h5, h, form, input, label, button } from '../dsl/html-dsl.js';

function normalizeType(type) {
    if (!type) return 'Unknown';
    return String(type)
        .toLowerCase()
        .replace(/_/g, ' ')
        .replace(/\b\w/g, (char) => char.toUpperCase());
}

function createLocationCard(location) {
    const title = location.name || 'Location';

    return div(
        { className: 'house-item house-item-professional' },
        div(
            { className: 'house-item-content' },
            div(
                { className: 'house-item-top' },
                h5({ className: 'house-item-title' }, title),
                span({ className: 'house-item-tag' }, normalizeType(location.type))
            ),
            div(
                { className: 'mt-3' },
                a({ href: `#locations/${location.lid}`, className: 'btn btn-sm btn-primary' }, 'Details')
            )
        )
    );
}

export function renderLocationDetails(location, children = []) {
    let parentLink = null;
    if (location.parentId) {
        parentLink = a({ href: `#locations/${location.parentId}`, className: 'btn btn-outline-primary' }, 'Parent Location');
    }

    let childLocationsSection = null;
    if (children.length > 0) {
        childLocationsSection = div(
            { className: 'border-top pt-4' },
            h5({ className: 'mb-3' }, 'Child locations'),
            div(
                { className: 'houses-grid' },
                children.map((child) => createLocationCard(child))
            )
        );
    }

    return div(
        { className: 'houses-shell' },
        div(
            { className: 'card p-4' },
            small({ className: 'text-uppercase text-muted' }, 'Location'),
            h2({ className: 'mb-2' }, location.name || 'Location'),
            p({ className: 'text-muted mb-4' }, `Type: ${normalizeType(location.type)}`),
            div(
                { className: 'd-flex gap-2 flex-wrap mb-4' },
                parentLink
            ),
            childLocationsSection
        )
    );
}

export function createLocationOption(value, text, isSelected) {
    const attrs = { value };
    if (isSelected) {
        attrs.selected = true;
    }

    return h('option', attrs, text);
}

export function createLocationFormLayout({ locations = [], onsubmit, cancelHref = '#home' }) {
    const formMessage = div({ id: 'locationFormMessage' });
    const typeSelect = h(
        'select',
        { className: 'form-select', id: 'locationType', required: true },
        h('option', { value: '' }, 'Choose type'),
        ['COUNTRY', 'REGION', 'DISTRICT', 'MUNICIPALITY', 'LOCALITY'].map((type) =>
            h('option', { value: type }, normalizeType(type))
        )
    );
    const parentSelect = h(
        'select',
        { className: 'form-select', id: 'locationParentId' },
        h('option', { value: '' }, 'No parent location'),
        locations.map((location) =>
            h('option', { value: location.lid }, `${location.name} (${normalizeType(location.type)})`)
        )
    );

    return div(
        { className: 'container mt-5' },
        form(
            { className: 'card p-4 mx-auto', style: { maxWidth: '640px' }, onsubmit },
            h2({ className: 'h4 mb-3' }, 'Create Location'),
            formMessage,
            div(
                { className: 'mb-3' },
                label({ className: 'form-label', for: 'locationName' }, 'Name'),
                input({ className: 'form-control', id: 'locationName', type: 'text', required: true })
            ),
            div(
                { className: 'mb-3' },
                label({ className: 'form-label', for: 'locationType' }, 'Type'),
                typeSelect
            ),
            div(
                { className: 'mb-3' },
                label({ className: 'form-label', for: 'locationParentId' }, 'Parent'),
                parentSelect
            ),
            div(
                { className: 'd-flex gap-2 flex-wrap' },
                button({ className: 'btn btn-primary', type: 'submit' }, 'Create Location'),
                a({ href: cancelHref, className: 'btn btn-outline-secondary' }, 'Cancel')
            )
        )
    );
}
