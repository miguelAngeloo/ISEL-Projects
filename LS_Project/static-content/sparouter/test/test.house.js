import { testUtils } from './test-utils.js';
import { houseHandlers } from '../handlers/house-handlers.js';
import { houseService } from '../services/house-service.js';
import { predictionService } from '../services/prediction-service.js';
import { commonHandlers } from '../handlers/common-handlers.js';

const { expect } = window.chai;

describe('Testes de Casas', () => {
    let mainContent;
    let originalGetAllHouses, originalGetHouseDetails, originalGetPricePrediction, originalShowToast;

    beforeEach(() => {
        mainContent = testUtils.setupEnvironment();

        originalGetAllHouses = houseService.getAllHouses;
        originalGetHouseDetails = houseService.getHouseDetails;
        originalGetPricePrediction = predictionService.getPricePrediction;
        originalShowToast = commonHandlers.showToast;

        commonHandlers.showToast = () => {};
    });

    afterEach(() => {
        testUtils.teardownEnvironment(mainContent);

        houseService.getAllHouses = originalGetAllHouses;
        houseService.getHouseDetails = originalGetHouseDetails;
        predictionService.getPricePrediction = originalGetPricePrediction;
        commonHandlers.showToast = originalShowToast;
    });

    it('deve mostrar um Empty State se não houver casas disponíveis', async () => {
        houseService.getAllHouses = async () => ({ houses: [], total: 0 });

        await houseHandlers.getHouses(mainContent);

        const title = mainContent.querySelector('h2');
        expect(title.textContent).to.equal('No houses available');
    });

    it('deve desenhar a lista de casas se a API devolver dados', async () => {
        houseService.getAllHouses = async () => ({
            houses: [{ id: 1, title: 'Casa Praia', description: 'Vista mar', pricePerNight: 100 }],
            total: 1
        });

        await houseHandlers.getHouses(mainContent);

        const title = mainContent.querySelector('h2');
        expect(title.textContent).to.equal('All Houses');

        const houseTitle = mainContent.querySelector('.house-item-title');
        expect(houseTitle.textContent).to.equal('Casa Praia');
    });

    it('deve mostrar erro inline se a API de listar casas falhar', async () => {
        houseService.getAllHouses = async () => {
            throw new Error('Erro de servidor');
        };

        await houseHandlers.getHouses(mainContent);

        const alertBox = mainContent.querySelector('.alert-danger');
        expect(alertBox).to.exist;
        expect(alertBox.textContent).to.include('Erro de servidor');
    });

    it('deve mostrar link para criar reserva nos detalhes da casa', async () => {
        sessionStorage.removeItem('token');
        window.location.hash = '#houses/1';

        houseService.getHouseDetails = async () => ({
            id: 1, title: 'Casa Montanha', pricePerNight: 50, areaSqMt: 80, locationId: 2
        });

        await houseHandlers.getHouseDetail(mainContent);

        const createBookingLink = mainContent.querySelector('a[href="#houses/1/bookings/create"]');
        expect(createBookingLink).to.exist;
        expect(createBookingLink.textContent).to.equal('Create Booking');
        expect(mainContent.querySelector('#predictionForm')).to.exist;
    });

    it('deve mostrar os detalhes da casa se tiver token', async () => {
        sessionStorage.setItem('token', 'token-valido');
        window.location.hash = '#houses/1';

        houseService.getHouseDetails = async () => ({
            id: 1, title: 'Casa Montanha', pricePerNight: 50, areaSqMt: 80, locationId: 2
        });

        await houseHandlers.getHouseDetail(mainContent);

        const createBookingLink = mainContent.querySelector('a[href="#houses/1/bookings/create"]');
        expect(createBookingLink).to.exist;

        const price = mainContent.querySelector('.price-highlight');
        expect(price.textContent).to.include('50 EUR');
    });

    it('deve calcular a previsao de preco no detalhe da casa', async () => {
        sessionStorage.removeItem('token');
        window.location.hash = '#houses/1';

        houseService.getHouseDetails = async () => ({ id: 1, title: 'Casa', pricePerNight: 50, areaSqMt: 80, locationId: 2 });
        predictionService.getPricePrediction = async (areaSqMt, lid, nights) => {
            expect(areaSqMt).to.equal(80);
            expect(lid).to.equal(2);
            expect(nights).to.equal(4);
            return { predictedPricePerNight: 92.5, predictedTotalPrice: 370 };
        };

        await houseHandlers.getHouseDetail(mainContent);

        mainContent.querySelector('#predictionStartDate').value = '2026-06-01';
        mainContent.querySelector('#predictionEndDate').value = '2026-06-05';
        mainContent.querySelector('#predictionForm').dispatchEvent(new Event('submit', { cancelable: true }));
        await testUtils.sleep();

        expect(mainContent.querySelector('#predictionNightlyValue').textContent).to.equal('92.50 EUR');
        expect(mainContent.querySelector('#predictionTotalValue').textContent).to.equal('370.00 EUR');
    });
});
