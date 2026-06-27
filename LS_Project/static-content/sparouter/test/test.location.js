import { testUtils } from './test-utils.js';
import { locationHandlers } from '../handlers/location-handlers.js';
import { locationService } from '../services/location-service.js';

const { expect } = window.chai;

describe('Testes de Localizacoes', () => {
    let mainContent;
    let originalGetLocationDetails;
    let originalGetChildrenLocations;
    let originalGetAllLocations;
    let originalCreateLocation;

    beforeEach(() => {
        mainContent = testUtils.setupEnvironment();

        originalGetLocationDetails = locationService.getLocationDetails;
        originalGetChildrenLocations = locationService.getChildrenLocations;
        originalGetAllLocations = locationService.getAllLocations;
        originalCreateLocation = locationService.createLocation;
    });

    afterEach(() => {
        testUtils.teardownEnvironment(mainContent);
        sessionStorage.clear();

        locationService.getLocationDetails = originalGetLocationDetails;
        locationService.getChildrenLocations = originalGetChildrenLocations;
        locationService.getAllLocations = originalGetAllLocations;
        locationService.createLocation = originalCreateLocation;
    });

    it('deve mostrar os detalhes de uma localizacao', async () => {
        window.location.hash = '#locations/2';
        locationService.getLocationDetails = async () => ({ lid: 2, name: 'Lisboa', type: 'DISTRICT' });
        locationService.getChildrenLocations = async () => ({
            locations: [{ lid: 3, name: 'Oeiras', type: 'MUNICIPALITY' }]
        });

        await locationHandlers.getLocationDetails(mainContent);

        expect(mainContent.textContent).to.include('Lisboa');
        expect(mainContent.textContent).to.include('Oeiras');
    });

    it('deve mostrar erro se falhar ao carregar a localizacao', async () => {
        window.location.hash = '#locations/2';
        locationService.getLocationDetails = async () => {
            throw new Error('Falha na rede');
        };

        await locationHandlers.getLocationDetails(mainContent);

        const alertBox = mainContent.querySelector('.alert-danger');
        expect(alertBox).to.exist;
        expect(alertBox.textContent).to.include('Falha na rede');
    });

    it('deve criar uma localizacao e voltar ao formulario da casa', async () => {
        sessionStorage.setItem('token', 'token-valido');
        window.location.hash = '#locations/create?returnTo=houses/create';
        locationService.getAllLocations = async () => [{ lid: 1, name: 'Portugal', type: 'COUNTRY' }];
        locationService.createLocation = async (location) => {
            expect(location.name).to.equal('Lisboa');
            expect(location.type).to.equal('DISTRICT');
            expect(location.parentId).to.equal(1);
            return { lid: 2 };
        };

        await locationHandlers.getCreateLocation(mainContent);

        mainContent.querySelector('#locationName').value = 'Lisboa';
        mainContent.querySelector('#locationType').value = 'DISTRICT';
        mainContent.querySelector('#locationParentId').value = '1';
        mainContent.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));
        await testUtils.sleep();

        expect(window.location.hash).to.equal('#houses/create?locationId=2');
    });
});
