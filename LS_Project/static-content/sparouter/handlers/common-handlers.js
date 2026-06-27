import { li, a, h1, h2, div, p, button, small, span, form, input, label } from '../dsl/html-dsl.js';
import { locationService } from '../services/location-service.js';
import { houseService } from '../services/house-service.js';
import { searchLocation } from '../utils/location-search-bar.js';

function toastBadge(type) {
    if (type === 'success') return 'OK';
    if (type === 'error') return 'ERR';
    if (type === 'warning') return 'WARN';
    return 'INFO';
}

function closeWithAnimation(node) {
    node.classList.add('removing');
    setTimeout(() => node.remove(), 250);
}

function getOrCreateToastContainer() {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = div({ id: 'toastContainer', className: 'toast-container' });
        document.body.appendChild(container);
    }

    return container;
}

function createToast(message, type) {
    const toast = div(
        { className: `toast-item toast-${type}` },
        div(
            { className: 'toast-content' },
            span({ className: 'toast-icon', 'aria-hidden': 'true' }, toastBadge(type)),
            span({ className: 'toast-message' }, message)
        ),
        button(
            {
                className: 'toast-close',
                type: 'button',
                onclick: () => closeWithAnimation(toast)
            },
            'x'
        )
    );

    return toast;
}

function renderAuthLinks(token) {
    if (token) {
        return [
            li({ className: 'nav-item' }, a({ href: '#bookings/me', className: 'nav-link' }, 'My Bookings')),
            li({ className: 'nav-item' }, a({ href: '#profile', className: 'nav-link' }, 'Profile')),
            li({ className: 'nav-item' }, a({ href: '#logout', className: 'nav-link' }, 'Logout'))
        ];
    }

    return [
        li({ className: 'nav-item' }, a({ href: '#login', className: 'nav-link' }, 'Sign In')),
        li({ className: 'nav-item' }, a({ href: '#register', className: 'nav-link' }, 'Create Account'))
    ];
}

function renderHomeHero(availabilityValue) {
    const title = div(
        { className: 'hero-title-block' },
        h1({ className: 'hero-title' }, 'The Best'),
        h2({ className: 'hero-project-title' }, 'LS Project')
    );

    return div(
        { className: 'hero-section' },
        div(
            { className: 'hero-copy' },
            title,
            div(
                { className: 'hero-actions' },
                button(
                    {
                        className: 'btn btn-primary btn-lg',
                        onclick: () => { window.location.hash = '#houses'; }
                    },
                    'Browse Houses'
                ),
                button(
                    {
                        className: 'btn btn-outline-success btn-lg',
                        onclick: () => { window.location.hash = '#bookings/me'; }
                    },
                    'My Bookings'
                )
            )
        ),
        div(
            { className: 'hero-panel', 'aria-hidden': 'true' },
            div(
                { className: 'hero-glass' },
                div({ className: 'hero-stat-label' }, 'Live Availability'),
                availabilityValue
            )
        )
    );
}

function createHomeSearchForm(locations) {
    const locationField = searchLocation({
        locations,
        inputId: 'homeSearchLocationInput',
        hiddenId: 'homeSearchLocation'
    });

    return form(
        {
            className: 'card p-4 mt-4',
            onsubmit: (event) => {
                event.preventDefault();

                const params = new URLSearchParams({ skip: '0', limit: '10' });
                const locationId = document.getElementById('homeSearchLocation').value;
                const minPrice = document.getElementById('homeSearchMinPrice').value;
                const maxPrice = document.getElementById('homeSearchMaxPrice').value;

                if (!locationId || (!minPrice && !maxPrice)) {
                    commonHandlers.showToast('Choose a location and at least one price value.', 'warning');
                    return;
                }

                params.set('locationId', locationId);
                if (minPrice) params.set('minPrice', minPrice);
                if (maxPrice) params.set('maxPrice', maxPrice);

                window.location.hash = `#houses?${params.toString()}`;
            }
        },
        h2({ className: 'h4 mb-3' }, 'Search houses'),
        div(
            { className: 'row g-3' },
            div({ className: 'col-md-4' }, label({ className: 'form-label', for: 'homeSearchLocationInput' }, 'Location'), locationField),
            div({ className: 'col-md-3' }, label({ className: 'form-label', for: 'homeSearchMinPrice' }, 'Min price'), input({ className: 'form-control', id: 'homeSearchMinPrice', type: 'number', min: '0' })),
            div({ className: 'col-md-3' }, label({ className: 'form-label', for: 'homeSearchMaxPrice' }, 'Max price'), input({ className: 'form-control', id: 'homeSearchMaxPrice', type: 'number', min: '0' })),
            div({ className: 'col-md-2 d-flex align-items-end' }, button({ className: 'btn btn-primary w-100', type: 'submit' }, 'Search'))
        )
    );
}

export const commonHandlers = {
    askConfirmation: ({ title, message, confirmLabel = 'Confirm', cancelLabel = 'Cancel' }) =>
        new Promise((resolve) => {
            const overlay = div({ className: 'confirm-overlay' });
            const modal = div(
                { className: 'confirm-modal card p-4' },
                h2({ className: 'h5 mb-2' }, title),
                p({ className: 'mb-3 text-muted' }, message),
                div(
                    { className: 'd-flex gap-2 justify-content-end flex-wrap' },
                    button(
                        {
                            className: 'btn btn-outline-secondary',
                            type: 'button',
                            onclick: () => {
                                overlay.remove();
                                resolve(false);
                            }
                        },
                        cancelLabel
                    ),
                    button(
                        {
                            className: 'btn btn-danger',
                            type: 'button',
                            onclick: () => {
                                overlay.remove();
                                resolve(true);
                            }
                        },
                        confirmLabel
                    )
                )
            );

            overlay.appendChild(modal);
            document.body.appendChild(overlay);
        }),

    showToast: (message, type = 'info') => {
        const container = getOrCreateToastContainer();
        const toast = createToast(message, type);

        container.appendChild(toast);
        requestAnimationFrame(() => toast.classList.add('show'));

        setTimeout(() => {
            if (toast.isConnected) closeWithAnimation(toast);
        }, 3500);
    },

    renderSectionSkeleton: (items = 3) =>
        div(
            { className: 'skeleton-grid' },
            Array.from({ length: items }, (_, idx) =>
                div(
                    { className: `skeleton-card skeleton-delay-${idx % 6}` },
                    div({ className: 'skeleton-line skeleton-line-lg' }),
                    div({ className: 'skeleton-line' }),
                    div({ className: 'skeleton-line skeleton-line-sm' })
                )
            )
        ),

    renderEmptyState: ({ title, message = '', actionLabel = null, onAction = null }) => {
        let actionButton = null;
        let messageElement = null;

        if (actionLabel) {
            if (typeof onAction === 'function') {
                actionButton = button(
                    { className: 'btn btn-primary', type: 'button', onclick: onAction },
                    actionLabel
                );
            }
        }

        if (message) {
            messageElement = p({ className: 'empty-state-message' }, message);
        }

        return div(
            { className: 'empty-state' },
            h2({ className: 'empty-state-title' }, title),
            messageElement,
            actionButton
        );
    },

    renderInlineError: ({ title = 'Something went wrong', message = 'Please try again.' }) =>
        div(
            { className: 'alert alert-danger' },
            h2({ className: 'h5 mb-2' }, title),
            small({}, message)
        ),

    updateAuthNav: () => {
        const authNav = document.getElementById('authNav');
        if (!authNav) return;

        authNav.replaceChildren(...renderAuthLinks(sessionStorage.getItem('token')));
    },

    getHome: async (mainContent) => {
        const availabilityValue = div({ className: 'hero-stat-value' }, 'Loading...');
        const home = div({}, renderHomeHero(availabilityValue));

        mainContent.replaceChildren(home);

        try {
            const locations = await locationService.getAllLocations();
            home.appendChild(createHomeSearchForm(locations));
        } catch (err) {
            console.warn('Could not load home search locations:', err);
        }

        try {
            const data = await houseService.countHouses();
            availabilityValue.textContent = String(data.count);
        } catch (err) {
            console.warn('Could not load house count:', err);
            availabilityValue.textContent = 'Unavailable';
        }
    }
};
