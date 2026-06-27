import { a, nav, ul, li } from '../dsl/html-dsl.js';

export function readHashQuery() {
    const hashParts = window.location.hash.split('?');
    const queryString = hashParts[1] || '';
    return new URLSearchParams(queryString);
}

export function readPaginationFromHash(defaultLimit = 10) {
    const params = readHashQuery();
    let skip = parseInt(params.get('skip'), 10);
    let limit = parseInt(params.get('limit'), 10);

    if (Number.isNaN(skip)) {
        skip = 0;
    }

    if (Number.isNaN(limit)) {
        limit = defaultLimit;
    }

    return { skip, limit };
}

export function buildHashPagination({
    basePath,
    skip,
    limit,
    itemCount,
    ariaLabel,
    extraQuery = '',
    hasNextPage
}) {
    let nextIsDisabled;
    if (typeof hasNextPage === 'boolean') {
        nextIsDisabled = !hasNextPage;
    } else {
        nextIsDisabled = itemCount < limit;
    }

    let previousClass = 'page-item';
    if (skip === 0) {
        previousClass += ' disabled';
    }

    let nextClass = 'page-item';
    if (nextIsDisabled) {
        nextClass += ' disabled';
    }

    return nav(
        { className: 'mt-4', 'aria-label': ariaLabel },
        ul(
            { className: 'pagination justify-content-center' },
            li(
                { className: previousClass },
                a(
                    {
                        className: 'page-link',
                        href: `#${basePath}?skip=${Math.max(0, skip - limit)}&limit=${limit}${extraQuery}`
                    },
                    'Previous'
                )
            ),
            li(
                { className: nextClass },
                a(
                    {
                        className: 'page-link',
                        href: `#${basePath}?skip=${skip + limit}&limit=${limit}${extraQuery}`
                    },
                    'Next'
                )
            )
        )
    );
}

export function getHashPathSegment(index = 1) {
    const pathParts = window.location.hash.split('/');
    const segment = pathParts[index];

    if (!segment) {
        return undefined;
    }

    return segment.split('?')[0];
}

export function isDateRangeValid(startDate, endDate) {
    if (!startDate) return false;
    if (!endDate) return false;

    return new Date(startDate) < new Date(endDate);
}

export function toDateInputValue(value) {
    return new Date(value).toISOString().split('T')[0];
}
