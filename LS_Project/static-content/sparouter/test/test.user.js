import { testUtils } from './test-utils.js';
import { userHandlers } from '../handlers/user-handlers.js';
import { userService } from '../services/user-service.js';
import { authHandlers } from '../handlers/auth-handlers.js';
import { commonHandlers } from '../handlers/common-handlers.js';

const { expect } = window.chai;

describe('Testes de Perfil de Utilizador', () => {
    let mainContent;
    let originalGetUserDetails, originalUpdateUser, originalDeleteUser, originalLogout;
    let originalAskConfirmation, originalShowToast;

    beforeEach(() => {
        mainContent = testUtils.setupEnvironment();

        originalGetUserDetails = userService.getUserDetails;
        originalUpdateUser = userService.updateUser;
        originalDeleteUser = userService.deleteUser;
        originalLogout = authHandlers.logout;
        originalAskConfirmation = commonHandlers.askConfirmation;
        originalShowToast = commonHandlers.showToast;

        authHandlers.logout = async () => {};
        commonHandlers.showToast = () => {};
    });

    afterEach(() => {
        testUtils.teardownEnvironment(mainContent);

        userService.getUserDetails = originalGetUserDetails;
        userService.updateUser = originalUpdateUser;
        userService.deleteUser = originalDeleteUser;
        authHandlers.logout = originalLogout;
        commonHandlers.askConfirmation = originalAskConfirmation;
        commonHandlers.showToast = originalShowToast;
    });

    it('deve redirecionar para login se não houver userId ao tentar ver perfil', async () => {
        sessionStorage.removeItem('userId');
        await userHandlers.getProfile(mainContent);
        expect(window.location.hash).to.equal('#login');
    });

    it('deve mostrar erro se o fetch do perfil falhar', async () => {
        sessionStorage.setItem('userId', 'user-123');
        userService.getUserDetails = async () => {
            throw new Error('Erro de API');
        };

        await userHandlers.getProfile(mainContent);

        const alertBox = mainContent.querySelector('.alert-danger');
        expect(alertBox).to.exist;
        expect(alertBox.textContent).to.include('Erro de API');
    });

    it('deve renderizar os detalhes do perfil com sucesso', async () => {
        sessionStorage.setItem('userId', 'user-123');
        userService.getUserDetails = async () => ({
            name: 'Maria Silva',
            email: 'maria@exemplo.com'
        });

        await userHandlers.getProfile(mainContent);

        const title = mainContent.querySelector('h1');
        const emailP = mainContent.querySelector('.text-muted');

        expect(title.textContent).to.equal('Maria Silva');
        expect(emailP.textContent).to.equal('maria@exemplo.com');
    });

    it('deve renderizar o formulário de edição corretamente', async () => {
        sessionStorage.setItem('userId', 'user-123');
        userService.getUserDetails = async () => ({
            name: 'Maria Silva',
            email: 'maria@exemplo.com'
        });

        await userHandlers.editProfile(mainContent);

        const input = mainContent.querySelector('#editName');
        expect(input).to.exist;
        expect(input.value).to.equal('Maria Silva');
    });

    it('deve submeter o formulário de edição e redirecionar', async () => {
        sessionStorage.setItem('userId', 'user-123');
        userService.getUserDetails = async () => ({
            name: 'Maria Silva',
            email: 'maria@exemplo.com'
        });

        let nomeEnviado = '';
        userService.updateUser = async (name) => {
            nomeEnviado = name;
            return { userId: 'user-123', name };
        };

        await userHandlers.editProfile(mainContent);

        mainContent.querySelector('#editName').value = 'Maria Atualizada';
        mainContent.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));

        await testUtils.sleep();

        expect(nomeEnviado).to.equal('Maria Atualizada');
        expect(window.location.hash).to.equal('#profile');
    });

    it('deve apagar a conta e fazer logout quando o utilizador confirmar', async () => {
        sessionStorage.setItem('userId', 'user-123');
        userService.getUserDetails = async () => ({
            name: 'Maria Silva',
            email: 'maria@exemplo.com'
        });

        await userHandlers.getProfile(mainContent);

        commonHandlers.askConfirmation = async () => true;

        let deleteFoiChamado = false;
        userService.deleteUser = async () => {
            deleteFoiChamado = true;
        };

        let logoutFoiChamado = false;
        authHandlers.logout = async () => {
            logoutFoiChamado = true;
        };

        const deleteBtn = mainContent.querySelector('.btn-outline-danger');
        deleteBtn.click();

        await testUtils.sleep();

        expect(deleteFoiChamado).to.be.true;
        expect(logoutFoiChamado).to.be.true;
    });

    it('NÃO deve apagar a conta quando o utilizador cancelar', async () => {
        sessionStorage.setItem('userId', 'user-123');
        userService.getUserDetails = async () => ({
            name: 'Maria Silva',
            email: 'maria@exemplo.com'
        });

        await userHandlers.getProfile(mainContent);

        commonHandlers.askConfirmation = async () => false;

        let deleteFoiChamado = false;
        userService.deleteUser = async () => {
            deleteFoiChamado = true;
        };

        const deleteBtn = mainContent.querySelector('.btn-outline-danger');
        deleteBtn.click();

        await testUtils.sleep();

        expect(deleteFoiChamado).to.be.false;
    });
});
