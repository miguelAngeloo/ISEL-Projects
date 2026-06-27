import { testUtils } from './test-utils.js';
import { authHandlers } from '../handlers/auth-handlers.js';
import { authService } from '../services/auth-service.js';
import { commonHandlers } from '../handlers/common-handlers.js';

const { expect } = window.chai;

describe('Testes de Autenticação', () => {
    let mainContent;
    let originalLogin, originalRegister, originalLogout, originalUpdateAuthNav;

    beforeEach(() => {
        mainContent = testUtils.setupEnvironment();

        originalLogin = authService.login;
        originalRegister = authService.register;
        originalLogout = authService.logout;
        originalUpdateAuthNav = commonHandlers.updateAuthNav;

        commonHandlers.updateAuthNav = () => {};
    });

    afterEach(() => {
        testUtils.teardownEnvironment(mainContent);

        authService.login = originalLogin;
        authService.register = originalRegister;
        authService.logout = originalLogout;
        commonHandlers.updateAuthNav = originalUpdateAuthNav;
    });

    it('deve renderizar o formulário de Login corretamente', () => {
        authHandlers.getLogin(mainContent);

        expect(mainContent.querySelector('#loginEmail')).to.exist;
        expect(mainContent.querySelector('#loginPassword')).to.exist;

        const btn = mainContent.querySelector('.auth-submit-btn');
        const title = mainContent.querySelector('h1');

        expect(btn).to.exist;
        expect(btn.textContent).to.equal('Sign In');
        expect(title.textContent).to.equal('Sign In');
    });

    it('deve renderizar o formulário de Registo corretamente', () => {
        authHandlers.getRegister(mainContent);

        expect(mainContent.querySelector('#regName')).to.exist;
        expect(mainContent.querySelector('#regEmail')).to.exist;
        expect(mainContent.querySelector('#regPassword')).to.exist;

        const btn = mainContent.querySelector('.auth-submit-btn');

        expect(btn).to.exist;
        expect(btn.textContent).to.equal('Create Account');
    });

    it('deve fazer Login com sucesso, guardar o token e mostrar mensagem de sucesso', async () => {
        let emailEnviado = '';
        authService.login = async (email, password) => {
            emailEnviado = email;
            return { token: 'token-falso-123', userId: 'user-789' };
        };

        authHandlers.getLogin(mainContent);

        mainContent.querySelector('#loginEmail').value = 'teste@casa.com';
        mainContent.querySelector('#loginPassword').value = '123456';

        const form = mainContent.querySelector('form');
        form.dispatchEvent(new Event('submit', { cancelable: true }));

        await testUtils.sleep();

        expect(emailEnviado).to.equal('teste@casa.com');
        expect(sessionStorage.getItem('token')).to.equal('token-falso-123');
        expect(sessionStorage.getItem('userId')).to.equal('user-789');

        const alertBox = mainContent.querySelector('.alert-success');
        expect(alertBox).to.exist;
        expect(alertBox.textContent).to.include('Success');
    });

    it('deve mostrar mensagem de erro se o Login falhar', async () => {
        authService.login = async () => {
            throw new Error('Credenciais inválidas');
        };

        authHandlers.getLogin(mainContent);

        mainContent.querySelector('#loginEmail').value = 'errado@casa.com';
        mainContent.querySelector('#loginPassword').value = 'senha-errada';

        mainContent.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }));

        await testUtils.sleep();

        expect(sessionStorage.getItem('token')).to.be.null;

        const alertBox = mainContent.querySelector('.alert-danger');
        expect(alertBox).to.exist;
        expect(alertBox.textContent).to.equal('Credenciais inválidas');
    });

    it('deve fazer Logout e limpar os dados do sessionStorage', async () => {
        sessionStorage.setItem('token', 'token-antigo');
        sessionStorage.setItem('userId', 'user-antigo');

        authService.logout = async () => { return true; };

        await authHandlers.logout();

        expect(sessionStorage.getItem('token')).to.be.null;
        expect(sessionStorage.getItem('userId')).to.be.null;
        expect(window.location.hash).to.equal('#home');
    });
});
