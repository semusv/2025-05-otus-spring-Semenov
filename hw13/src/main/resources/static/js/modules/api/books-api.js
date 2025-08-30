import { handleApiResponse } from "../utils.js";

export async function fetchBooks() {
    const response = await fetch("/api/books", {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}
export async function fetchBook(bookId) {
    if (bookId == null) {
        return null;
    }

    const response = await fetch(`/api/books/${bookId}`, {
        headers: {
            'Accept': 'application/json'
        }
    });

    return await handleApiResponse(response);
}
export async function addBook(bookCreateDto) {
    if (bookCreateDto == null) {
        return null;
    }

    const response = await fetch(`/api/books`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(bookCreateDto)
    });

    return await handleApiResponse(response);
}
export async function saveBook(bookUpdateDto) {
    if (bookUpdateDto == null) {
        return null;
    }

    const response = await fetch(`/api/books/${bookUpdateDto.id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(bookUpdateDto)
    });

    return await handleApiResponse(response);
}
export async function deleteBook(bookId) {
    if (bookId == null) {
        return null;
    }

    const response = await fetch(`/api/books/${bookId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        },
    });

    return await handleApiResponse(response);
}
