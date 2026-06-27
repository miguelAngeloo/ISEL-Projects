import { div, input, ul, li } from '../dsl/html-dsl.js';

export function searchLocation({ locations, inputId, hiddenId, selectedId }) {
    const hidden = input({ type: 'hidden', id: hiddenId });
    const list = ul({ className: 'autocomplete-list', hidden: true });
    const text = input({ type: 'text', id: inputId, className: 'form-control', autocomplete: 'off' });

    text.addEventListener('input', () => {
        const term = text.value.trim().toLowerCase();
        hidden.value = '';
        list.hidden = term.length < 3;
        if (list.hidden) return;

        list.replaceChildren(...locations
            .filter((loc) => loc.name.toLowerCase().includes(term))
            .map((loc) => {
                const item = li({ className: 'autocomplete-item' }, loc.name);
                item.onclick = () => { text.value = loc.name; hidden.value = loc.lid; list.hidden = true; };
                return item;
            }));
    });

    if (selectedId) {
        const sel = locations.find((loc) => String(loc.lid) === String(selectedId));
        if (sel) { text.value = sel.name; hidden.value = sel.lid; }
    }

    return div({ className: 'autocomplete' }, text, hidden, list);
}