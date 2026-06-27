import { testUtils } from './test-utils.js';
import { bookingHandlers } from '../handlers/booking-handlers.js';
import { bookingService } from '../services/booking-service.js';
import { commonHandlers } from '../handlers/common-handlers.js';

const { expect } = window.chai;

describe('Testes de Reservas', () => {
    let mainContent;
    let originalGetMyBookings, originalGetBookingDetails, originalUpdateBooking, originalDeleteBooking;
    let originalAskConfirmation, originalShowToast;

    beforeEach(() => {
        mainContent = testUtils.setupEnvironment();

        originalGetMyBookings = bookingService.getMyBookings;
        originalGetBookingDetails = bookingService.getBookingDetails;
        originalUpdateBooking = bookingService.updateBooking;
        originalDeleteBooking = bookingService.deleteBooking;

        originalAskConfirmation = commonHandlers.askConfirmation;
        originalShowToast = commonHandlers.showToast;

        commonHandlers.showToast = () => {};
    });

    afterEach(() => {
        testUtils.teardownEnvironment(mainContent);

        bookingService.getMyBookings = originalGetMyBookings;
        bookingService.getBookingDetails = originalGetBookingDetails;
        bookingService.updateBooking = originalUpdateBooking;
        bookingService.deleteBooking = originalDeleteBooking;

        commonHandlers.askConfirmation = originalAskConfirmation;
        commonHandlers.showToast = originalShowToast;
    });

    it('deve pedir para fazer Login se não houver token no sessionStorage', async () => {
        sessionStorage.removeItem('token');

        await bookingHandlers.getMyBookings(mainContent);

        const title = mainContent.querySelector('h2');
        expect(title.textContent).to.equal('Session required');

        const btn = mainContent.querySelector('button');
        expect(btn.textContent).to.equal('Go to login');
    });

    it('deve mostrar um Empty State se a API devolver uma lista vazia', async () => {
        sessionStorage.setItem('token', 'token-valido');

        bookingService.getMyBookings = async () => ({ bookings: [], total: 0 });

        await bookingHandlers.getMyBookings(mainContent);

        const title = mainContent.querySelector('h2');
        expect(title.textContent).to.equal('No bookings yet');
    });

    it('deve desenhar a lista de reservas se a API devolver dados', async () => {
        sessionStorage.setItem('token', 'token-valido');

        bookingService.getMyBookings = async () => ({
            bookings: [{
                id: 'res-123',
                houseId: 'house-1',
                startDate: '2026-05-01T10:00:00.000Z',
                endDate: '2026-05-05T10:00:00.000Z'
            }],
            total: 1
        });

        await bookingHandlers.getMyBookings(mainContent);

        const title = mainContent.querySelector('h2');
        expect(title.textContent).to.equal('My Bookings');

        const editBtn = mainContent.querySelector('button.btn-primary');
        const deleteBtn = mainContent.querySelector('button.btn-danger');

        expect(editBtn.textContent).to.equal('Edit');
        expect(deleteBtn.textContent).to.equal('Delete');
    });

    it('deve validar datas erradas e NÃO submeter o formulário de edição', async () => {
        sessionStorage.setItem('token', 'token-valido');
        window.location.hash = '#bookings/res-123/edit';

        bookingService.getBookingDetails = async () => ({
            id: 'res-123',
            houseId: 'house-1',
            startDate: '2026-05-01',
            endDate: '2026-05-05'
        });

        await bookingHandlers.editBooking(mainContent);

        mainContent.querySelector('#startDate').value = '2026-05-10';
        mainContent.querySelector('#endDate').value = '2026-05-02';

        const form = mainContent.querySelector('form');
        form.dispatchEvent(new Event('submit', { cancelable: true }));

        await testUtils.sleep();

        const alertBox = mainContent.querySelector('.alert-danger');
        expect(alertBox).to.exist;
        expect(alertBox.textContent).to.include('Invalid date range');
    });

    it('deve submeter a edição com sucesso se as datas forem válidas', async () => {
        sessionStorage.setItem('token', 'token-valido');
        window.location.hash = '#bookings/res-123/edit';

        bookingService.getBookingDetails = async () => ({
            id: 'res-123',
            houseId: 'house-1',
            startDate: '2026-05-01',
            endDate: '2026-05-05'
        });

        let dadosEnviadosParaApi = {};

        bookingService.updateBooking = async (bId, hId, start, end) => {
            dadosEnviadosParaApi = { bId, hId, start, end };
            return true;
        };

        await bookingHandlers.editBooking(mainContent);

        mainContent.querySelector('#startDate').value = '2026-06-01';
        mainContent.querySelector('#endDate').value = '2026-06-10';

        mainContent.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));

        await testUtils.sleep();

        expect(dadosEnviadosParaApi.start).to.equal('2026-06-01');
        expect(dadosEnviadosParaApi.end).to.equal('2026-06-10');
    });

    it('deve eliminar a reserva se o utilizador confirmar no Modal', async () => {
        commonHandlers.askConfirmation = async () => true;

        let idApagado = null;
        bookingService.deleteBooking = async (id) => {
            idApagado = id;
            return true;
        };

        await bookingHandlers.deleteBookingConfirm('res-999');

        expect(idApagado).to.equal('res-999');
    });

    it('NÃO deve eliminar a reserva se o utilizador cancelar no Modal', async () => {
        commonHandlers.askConfirmation = async () => false;

        let apiFoiChamada = false;
        bookingService.deleteBooking = async () => {
            apiFoiChamada = true;
        };

        await bookingHandlers.deleteBookingConfirm('res-999');

        expect(apiFoiChamada).to.be.false;
    });
});
