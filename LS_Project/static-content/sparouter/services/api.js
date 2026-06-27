class APIError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = 'APIError';
        this.status = status;
        this.data = data;
    }
}

function getErrorMessage(errorData, status) {
    if (errorData.errorCause) {
        return errorData.errorCause;
    }

    if (errorData.error) {
        return errorData.error;
    }

    if (errorData.message) {
        return errorData.message;
    }

    if (errorData.description) {
        return errorData.description;
    }

    return `Erro ${status}`;
}

export async function request(endpoint, method = 'GET', body = null) {
    const token = sessionStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        method,
        headers,
    };

    if (body) {
        config.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(endpoint, config);

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            const errorMessage = getErrorMessage(errorData, response.status);
            throw new APIError(errorMessage, response.status, errorData);
        }

        if (response.status === 204) return null;

        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }

        return null;
    } catch (err) {
        if (err instanceof APIError) {
            console.error("API Error:", {
                message: err.message,
                status: err.status,
                data: err.data
            });
            throw err;
        }

        if (err instanceof TypeError && err.message.includes('fetch')) {
            const networkError = new Error('Erro de ligacao. Verifique a sua internet.');
            console.error("Network Error:", networkError);
            throw networkError;
        }

        console.error("Unexpected Error:", err);
        throw err;
    }
}

export function buildQuery(params) {
    const query = new URLSearchParams();

    Object.entries(params).forEach(([key, value]) => {
        if (value === null) {
            return;
        }

        if (value === undefined) {
            return;
        }

        if (value === '') {
            return;
        }

        query.set(key, String(value));
    });

    return query.toString();
}

export { APIError };

