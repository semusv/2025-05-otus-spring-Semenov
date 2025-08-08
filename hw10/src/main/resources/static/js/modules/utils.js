export class ApiError extends Error {
    constructor(message, type, status, errors = []) {
        super(message);
        this.name = 'ApiError';
        this.type = type;
        this.status = status;
        this.errors = errors;
    }
}

export async function handleApiResponse(response) {
    if (response.ok) {
        return await response.json();
    }


     const errorData = await response.json();


    // Валидационные ошибки
    if (errorData.errorType === 'VALIDATION_FAILED') {
        throw new ApiError(
            errorData.message || 'Validation failed',
            errorData.errorType,
            response.status,
            errorData.errors || []
        );
    }

    // Стандартные API ошибки
    throw new ApiError(
        errorData.message || errorData.errorText || `HTTP error ${response.status}`,
        errorData.errorType || 'API_ERROR',
        response.status,
        errorData.errors
    );
}

function showNotification(message, type = 'success') {
    const container = document.getElementById('notification-container');

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;

    notification.innerHTML = `
            ${message}
            <button class="close" onclick="this.parentElement.remove()">&times;</button>
        `;

    container.appendChild(notification);

    // Автоматическое удаление через 5 секунд
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}

// Примеры использования:
export function showSuccessMessage(message) {
    showNotification(message, 'success');
}

export function showErrorMessage(message) {
    showNotification(message, 'error');
}

export function showWarningMessage(message) {
    showNotification(message, 'warning');
}


export function getBookIdFromUrl() {
    const pathSegments = window.location.pathname.split('/');
    const bookId = !isNaN(Number(pathSegments[2])) ? Number(pathSegments[2]) : null;
    return bookId;
}

export function delayLoader(minTime, startTime) {
    const elapsedTime = Date.now() - startTime;
    const remainingTime = minTime - elapsedTime;

    if (remainingTime > 0) {
        return new Promise(resolve => setTimeout(resolve, remainingTime));
    }
    return Promise.resolve();
}