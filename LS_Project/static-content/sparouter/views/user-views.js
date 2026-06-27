import { a, div, h1, p, button, input, label, form } from '../dsl/html-dsl.js';

export function renderProfileLoading() {
    return div({ className: 'text-center mt-5' }, p({}, 'Loading profile...'));
}

export function renderProfileError(message, onBackHome) {
    return div({ className: 'alert alert-danger m-5' },
        p({}, `Error loading profile: ${message}`),
        button({
            className: 'btn btn-outline-primary',
            onclick: onBackHome
        }, 'Back to Home')
    );
}

export function renderProfileDetails(user, { onEdit, onDelete }) {
    return div({ className: 'container mt-5' },
        div({ className: 'card shadow-sm mx-auto', style: { maxWidth: '600px' } },
            div({ className: 'card-body p-4' },
                div({ className: 'd-flex align-items-center mb-4' },
                    div({
                        className: 'bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-3',
                        style: { width: '64px', height: '64px', fontSize: '24px' }
                    }, user.name.charAt(0).toUpperCase()),
                    div(
                        {},
                        h1({ className: 'h3 mb-0' }, user.name),
                        p({ className: 'text-muted mb-0' }, user.email)
                    )
                ),
                div({ className: 'border-top pt-4' },
                    div({ className: 'd-flex gap-2' },
                        button({ className: 'btn btn-primary', onclick: onEdit }, 'Edit Name'),
                        button({ className: 'btn btn-outline-danger', onclick: onDelete }, 'Delete Account')
                    )
                )
            )
        )
    );
}

export function renderUserDetails(user) {
    let userBookingsLink = null;

    if (user.userId) {
        userBookingsLink = a(
            { href: `#users/${user.userId}/bookings`, className: 'btn btn-outline-primary' },
            'User Bookings'
        );
    }

    return div(
        { className: 'container mt-5' },
        div(
            { className: 'card shadow-sm mx-auto', style: { maxWidth: '600px' } },
            div(
                { className: 'card-body p-4' },
                h1({ className: 'h3 mb-3' }, user.name),
                p({ className: 'text-muted mb-4' }, user.email),
                div(
                    { className: 'border-top pt-4 d-flex gap-2 flex-wrap' },
                    userBookingsLink,
                    a({ href: '#houses', className: 'btn btn-outline-secondary' }, 'Back to Houses')
                )
            )
        )
    );
}

export function createEditProfileForm(user, { onSubmit, onCancel }) {
    const messageContainer = div({ id: 'editProfileMessage' });
    const nameInput = input({
        type: 'text',
        id: 'editName',
        className: 'form-control',
        value: user.name,
        required: true,
        minlength: 3,
        maxlength: 20
    });

    const editForm = form({ onsubmit: (e) => onSubmit(e, nameInput, messageContainer) },
        messageContainer,
        div({ className: 'mb-3' },
            label({ for: 'editName', className: 'form-label' }, 'New Name'),
            nameInput,
            p({ className: 'form-text' }, 'Name must be between 3 and 20 characters.')
        ),
        div({ className: 'd-flex gap-2' },
            button({ type: 'submit', className: 'btn btn-primary' }, 'Save Changes'),
            button({ type: 'button', className: 'btn btn-outline-secondary', onclick: onCancel }, 'Cancel')
        )
    );

    return div({ className: 'container mt-5' },
        div({ className: 'card shadow-sm mx-auto', style: { maxWidth: '500px' } },
            div({ className: 'card-body p-4' },
                h1({ className: 'h4 mb-4' }, 'Edit Profile'),
                editForm
            )
        )
    );
}

export function renderEditProfileError(message) {
    return div({ className: 'alert alert-danger' }, message);
}
