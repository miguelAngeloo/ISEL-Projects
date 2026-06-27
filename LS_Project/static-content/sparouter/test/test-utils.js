export const testUtils = {
    setupEnvironment: () => {
        let mainContent = document.getElementById('main-content');
        if (!mainContent) {
            mainContent = document.createElement('div');
            mainContent.id = 'main-content';
            document.body.appendChild(mainContent);
        }

        sessionStorage.clear();
        window.location.hash = '';

        return mainContent;
    },

    teardownEnvironment: (container) => {
        if (container) {
            container.remove();
        }
    },

    sleep: (ms = 10) => new Promise(resolve => setTimeout(resolve, ms))
};
