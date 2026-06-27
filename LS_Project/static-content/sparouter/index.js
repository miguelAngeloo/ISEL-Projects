import router from "./router.js"
import {commonHandlers} from "./handlers/common-handlers.js"
import {houseHandlers} from "./handlers/house-handlers.js"
import {authHandlers} from "./handlers/auth-handlers.js"
import {bookingHandlers} from "./handlers/booking-handlers.js"
import {userHandlers} from "./handlers/user-handlers.js"
import {locationHandlers} from "./handlers/location-handlers.js"

window.addEventListener('load', () => {
    router.addRouteHandler("home", commonHandlers.getHome);
    router.addRouteHandler("houses", houseHandlers.getHouses);
    router.addRouteHandler("houses/create", houseHandlers.getCreateHouse);
    router.addRouteHandler("login", authHandlers.getLogin);
    router.addRouteHandler("logout", authHandlers.logout);
    router.addRouteHandler("register", authHandlers.getRegister);
    router.addRouteHandler("bookings/me", bookingHandlers.getMyBookings);
    router.addRouteHandler("locations/create", locationHandlers.getCreateLocation);
    router.addRouteHandler("profile", userHandlers.getProfile);
    router.addRouteHandler("profile/edit", userHandlers.editProfile);
    router.addRouteHandler("bookings/:id/edit", bookingHandlers.editBooking);
    router.addRouteHandler("bookings/:id", bookingHandlers.getBookingDetails);
    router.addRouteHandler("houses/:id/available-days", houseHandlers.getAvailableDays);
    router.addRouteHandler("houses/:id/bookings/create", bookingHandlers.getCreateBooking);
    router.addRouteHandler("houses/:id/bookings", bookingHandlers.getHouseBookings);
    router.addRouteHandler("houses/:id", houseHandlers.getHouseDetail);
    router.addRouteHandler("locations/:id", locationHandlers.getLocationDetails);
    router.addRouteHandler("users/:id/bookings", bookingHandlers.getUserBookings);
    router.addRouteHandler("users/:id", userHandlers.getUserDetails);

    router.addDefaultNotFoundRouteHandler(() => window.location.hash = "home");

    commonHandlers.updateAuthNav();

    const handleHash = () => {
        const mainContent = document.getElementById("mainContent");
        if (!mainContent) {
            return;
        }

        const hash = window.location.hash.replace("#", "");
        let path = hash.split('?')[0];

        if (!path) {
            path = "home";
        }

        const handler = router.getRouteHandler(path);

        if (typeof handler === 'function') {
            handler(mainContent);
        } else {
            window.location.hash = "home";
        }
    };

    window.addEventListener('hashchange', handleHash);

    handleHash();
});
