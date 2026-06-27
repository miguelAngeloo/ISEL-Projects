function addChild(element, child) {
    if (child === null || child === undefined || child === false) {
        return;
    }

    if (Array.isArray(child)) {
        child.forEach((item) => addChild(element, item));
        return;
    }

    if (child instanceof Node) {
        element.appendChild(child);
        return;
    }

    element.appendChild(document.createTextNode(String(child)));
}

function setAttribute(element, name, value) {
    if (value === null || value === undefined || value === false) {
        return;
    }

    if (name === 'className') {
        element.className = value;
        return;
    }

    if (name === 'style') {
        Object.assign(element.style, value);
        return;
    }

    if (name.startsWith('on') && typeof value === 'function') {
        const eventName = name.slice(2).toLowerCase();
        element.addEventListener(eventName, value);
        return;
    }

    if (value === true) {
        element.setAttribute(name, '');
        return;
    }

    element.setAttribute(name, value);
}

export function h(tag, attributes = {}, ...children) {
    const element = document.createElement(tag);

    Object.entries(attributes).forEach(([name, value]) => {
        setAttribute(element, name, value);
    });

    children.forEach((child) => addChild(element, child));

    return element;
}

export function div(attributes = {}, ...children) {
    return h('div', attributes, ...children);
}

export function h1(attributes = {}, ...children) {
    return h('h1', attributes, ...children);
}

export function h2(attributes = {}, ...children) {
    return h('h2', attributes, ...children);
}

export function h5(attributes = {}, ...children) {
    return h('h5', attributes, ...children);
}

export function p(attributes = {}, ...children) {
    return h('p', attributes, ...children);
}

export function span(attributes = {}, ...children) {
    return h('span', attributes, ...children);
}

export function small(attributes = {}, ...children) {
    return h('small', attributes, ...children);
}

export function a(attributes = {}, ...children) {
    return h('a', attributes, ...children);
}

export function button(attributes = {}, ...children) {
    return h('button', attributes, ...children);
}

export function form(attributes = {}, ...children) {
    return h('form', attributes, ...children);
}

export function input(attributes = {}, ...children) {
    return h('input', attributes, ...children);
}

export function label(attributes = {}, ...children) {
    return h('label', attributes, ...children);
}

export function textarea(attributes = {}, ...children) {
    return h('textarea', attributes, ...children);
}

export function nav(attributes = {}, ...children) {
    return h('nav', attributes, ...children);
}

export function ul(attributes = {}, ...children) {
    return h('ul', attributes, ...children);
}

export function ol(attributes = {}, ...children) {
    return h('ol', attributes, ...children);
}

export function li(attributes = {}, ...children) {
    return h('li', attributes, ...children);
}
