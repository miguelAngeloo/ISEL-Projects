const routes = [];

let notFoundRouteHandler = () => {
    console.error("Route handler for unknown routes not defined");
};

function addRouteHandler(pathTemplate, handler) {
    routes.push({ pathTemplate, handler });
}

function addDefaultNotFoundRouteHandler(notFoundRH) {
    notFoundRouteHandler = notFoundRH;
}

function matchesPathTemplate(pathTemplate, path) {
    if (pathTemplate === path) {
        return true;
    }

    const templateParts = pathTemplate.split('/');
    const pathParts = path.split('/');

    if (templateParts.length !== pathParts.length) {
        return false;
    }

    for (let index = 0; index < templateParts.length; index++) {
        const templatePart = templateParts[index];
        const pathPart = pathParts[index];

        if (templatePart.startsWith(':')) {
            if (pathPart.length === 0) {
                return false;
            }
        } else if (templatePart !== pathPart) {
            return false;
        }
    }

    return true;
}

function getRouteHandler(path) {
    const route = routes.find(r => matchesPathTemplate(r.pathTemplate, path));

    if (route) {
        return route.handler;
    }

    return notFoundRouteHandler;
}

export default {
    addRouteHandler,
    getRouteHandler,
    addDefaultNotFoundRouteHandler
};
