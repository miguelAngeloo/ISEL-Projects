import { authService } from '../services/auth-service.js';
import { commonHandlers } from './common-handlers.js';
import {
    makeField,
    renderCenteredLayout,
    createAuthForm,
    createAuthMessageContainer,
    createAuthSubmitButton,
    createAuthError,
    createAuthSuccess
} from '../views/auth-views.js';

function createAuthSubmitHandler({
                                     messageContainer,
                                     submitButton,
                                     submitLabelLoading,
                                     submitLabelDefault,
                                     requestFn
                                 }) {
    return async (event) => {
        event.preventDefault();
        messageContainer.replaceChildren();
        submitButton.disabled = true;
        submitButton.textContent = submitLabelLoading;

        try {
            const data = await requestFn();
            if (data.token) {
                sessionStorage.setItem('token', data.token);
            }
            if (data.userId) {
                sessionStorage.setItem('userId', data.userId);
            }
            messageContainer.replaceChildren(createAuthSuccess('Success. Redirecting...'));

            setTimeout(() => {
                commonHandlers.updateAuthNav();
                window.location.hash = '#home';
            }, 800);
        } catch (err) {
            messageContainer.replaceChildren(createAuthError(err.message));
            submitButton.disabled = false;
            submitButton.textContent = submitLabelDefault;
        }
    };
}

function createAuthFormElement({
                                   fields,
                                   messageContainerId,
                                   submitLabelDefault,
                                   submitLabelLoading,
                                   submitRequest,
                                   secondaryText,
                                   secondaryLinkText,
                                   secondaryLinkHref
                               }) {
    const messageContainer = createAuthMessageContainer(messageContainerId);
    const submitButton = createAuthSubmitButton(submitLabelDefault);

    const onsubmit = createAuthSubmitHandler({
        messageContainer,
        submitButton,
        submitLabelLoading,
        submitLabelDefault,
        requestFn: submitRequest
    });

    return createAuthForm({
        fields,
        messageContainer,
        submitButton,
        onsubmit,
        secondaryText,
        secondaryLinkText,
        secondaryLinkHref
    });
}

export const authHandlers = {
    getRegister: (mainContent) => {
        const registerForm = createAuthFormElement({
            fields: [
                makeField({ id: 'regName', type: 'text', labelText: 'Full Name', minlength: 2 }),
                makeField({ id: 'regEmail', type: 'email', labelText: 'Email Address' }),
                makeField({ id: 'regPassword', type: 'password', labelText: 'Password', minlength: 6 })
            ],
            messageContainerId: 'regMessage',
            submitLabelDefault: 'Create Account',
            submitLabelLoading: 'Processing...',
            submitRequest: () =>
                authService.register(
                    document.getElementById('regName').value,
                    document.getElementById('regEmail').value,
                    document.getElementById('regPassword').value
                ),
            secondaryText: '',
            secondaryLinkText: 'Sign In',
            secondaryLinkHref: '#login'
        });

        mainContent.replaceChildren(
            renderCenteredLayout(
                'Create Account',
                '',
                registerForm
            )
        );
    },

    getLogin: (mainContent) => {
        const loginForm = createAuthFormElement({
            fields: [
                makeField({ id: 'loginEmail', type: 'email', labelText: 'Email Address' }),
                makeField({ id: 'loginPassword', type: 'password', labelText: 'Password' })
            ],
            messageContainerId: 'loginMessage',
            submitLabelDefault: 'Sign In',
            submitLabelLoading: 'Verifying...',
            submitRequest: () =>
                authService.login(
                    document.getElementById('loginEmail').value,
                    document.getElementById('loginPassword').value
                ),
            secondaryText: '',
            secondaryLinkText: 'Create Account',
            secondaryLinkHref: '#register'
        });

        mainContent.replaceChildren(
            renderCenteredLayout(
                'Sign In',
                '',
                loginForm
            )
        );
    },

    logout: async () => {
        try {
            await authService.logout();
        } catch (err) {
            console.error('Logout error:', err);
        } finally {
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('userId');
            commonHandlers.updateAuthNav();
            window.location.hash = '#home';
        }
    }
};