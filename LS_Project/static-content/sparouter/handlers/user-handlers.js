import { userService } from '../services/user-service.js';
import { authHandlers } from './auth-handlers.js';
import { commonHandlers } from './common-handlers.js';
import { getHashPathSegment } from './handler-utils.js';
import {
    renderProfileLoading,
    renderProfileError,
    renderProfileDetails,
    renderUserDetails,
    createEditProfileForm,
    renderEditProfileError
} from '../views/user-views.js';

function getCurrentUserId() {
    return sessionStorage.getItem('userId');
}

function redirectToLoginIfNeeded() {
    if (getCurrentUserId()) {
        return false;
    }

    window.location.hash = '#login';
    return true;
}

async function loadCurrentUser() {
    return userService.getUserDetails(getCurrentUserId());
}

async function submitProfileUpdate(event, nameInput, messageContainer) {
    event.preventDefault();

    try {
        const updatedUser = await userService.updateUser(nameInput.value);
        if (updatedUser && updatedUser.userId) {
            sessionStorage.setItem('userId', updatedUser.userId);
        }
        window.location.hash = '#profile';
    } catch (err) {
        messageContainer.replaceChildren(renderEditProfileError(err.message));
    }
}

async function deleteAccount() {
    const accepted = await commonHandlers.askConfirmation({
        title: 'Delete account?',
        message: 'This action cannot be undone.',
        confirmLabel: 'Delete account',
        cancelLabel: 'Keep account'
    });
    if (!accepted) {
        return;
    }

    try {
        await userService.deleteUser();
        commonHandlers.showToast('Account deleted successfully.', 'success');
        await authHandlers.logout();
    } catch (err) {
        commonHandlers.showToast(err.message, 'error');
    }
}

export const userHandlers = {
    getProfile: async (mainContent) => {
        if (redirectToLoginIfNeeded()) {
            return;
        }

        mainContent.replaceChildren(renderProfileLoading());

        try {
            const user = await loadCurrentUser();
            mainContent.replaceChildren(
                renderProfileDetails(user, {
                    onEdit: () => { window.location.hash = '#profile/edit'; },
                    onDelete: deleteAccount
                })
            );
        } catch (err) {
            mainContent.replaceChildren(renderProfileError(err.message, () => { window.location.hash = '#home'; }));
        }
    },

    editProfile: async (mainContent) => {
        if (redirectToLoginIfNeeded()) {
            return;
        }

        try {
            const user = await loadCurrentUser();
            mainContent.replaceChildren(
                createEditProfileForm(user, {
                    onSubmit: submitProfileUpdate,
                    onCancel: () => { window.location.hash = '#profile'; }
                })
            );
        } catch (err) {
            mainContent.replaceChildren(renderProfileError(err.message, () => { window.location.hash = '#profile'; }));
        }
    },

    getUserDetails: async (mainContent) => {
        const userId = getHashPathSegment(1);
        mainContent.replaceChildren(renderProfileLoading());

        try {
            const user = await userService.getUserDetails(userId);

            mainContent.replaceChildren(renderUserDetails(user));
        } catch (err) {
            mainContent.replaceChildren(renderProfileError(err.message, () => { window.location.hash = '#houses'; }));
        }
    }
};
