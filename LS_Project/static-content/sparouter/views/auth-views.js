import { div, form, input, label, h1, p, button, a } from '../dsl/html-dsl.js';

function brandIcon() {
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('width', '36');
    svg.setAttribute('height', '36');
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('fill', 'none');
    svg.setAttribute('class', 'brand-svg-anim');

    const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute('d', 'M12 3L2 12H5V21H19V12H22L12 3ZM10 19V14H14V19H10Z');
    path.setAttribute('stroke', 'var(--primary-color)');
    path.setAttribute('stroke-width', '1.5');
    path.setAttribute('stroke-linejoin', 'round');
    path.setAttribute('stroke-linecap', 'round');
    path.setAttribute('class', 'brand-path-anim');

    svg.appendChild(path);
    return svg;
}

export function makeField({ id, type, labelText, placeholder = '', minlength = null }) {
    const inputAttributes = {
        type,
        id,
        placeholder,
        required: true,
        className: 'form-control auth-field-input'
    };

    if (minlength) {
        inputAttributes.minlength = minlength;
    }

    return div(
        { className: 'mb-4' },
        label(
            {
                className: 'form-label auth-field-label',
                for: id
            },
            labelText
        ),
        input(inputAttributes)
    );
}

export function renderCenteredLayout(title, subtitle, formElement) {
    let subtitleElement = null;

    if (subtitle) {
        subtitleElement = p({ className: 'text-center mb-4 auth-subtitle' }, subtitle);
    }

    return div(
        {
            className: 'd-flex align-items-center justify-content-center auth-page'
        },
        div(
            {
                className: 'card border-0 auth-card'
            },
            div({ className: 'd-flex justify-content-center mb-4' }, brandIcon()),
            h1(
                {
                    className: 'text-center mb-2 auth-title'
                },
                title
            ),
            subtitleElement,
            formElement
        )
    );
}

export function createAuthForm({
    fields,
    messageContainer,
    submitButton,
    onsubmit,
    secondaryText,
    secondaryLinkText,
    secondaryLinkHref
}) {
    let secondaryContent = secondaryLinkText;

    if (secondaryLinkHref) {
        secondaryContent = a(
            {
                href: secondaryLinkHref,
                className: 'text-decoration-none auth-secondary-link'
            },
            secondaryLinkText
        );
    }

    return form(
        { onsubmit },
        messageContainer,
        ...fields,
        submitButton,
        p(
            { className: 'mt-4 text-center auth-secondary-text' },
            secondaryText,
            secondaryContent
        )
    );
}

export function createAuthMessageContainer(id) {
    return div({ id });
}

export function createAuthSubmitButton(label) {
    return button(
        {
            className: 'btn btn-primary w-100 mt-2 auth-submit-btn'
        },
        label
    );
}

export function createAuthError(message) {
    return div({ className: 'alert alert-danger' }, message);
}

export function createAuthSuccess(message) {
    return div({ className: 'alert alert-success' }, message);
}
