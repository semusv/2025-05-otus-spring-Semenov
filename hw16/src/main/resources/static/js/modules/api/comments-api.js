import { handleApiResponse } from "../utils.js";

export async function fetchCommentsByBookId(bookId) {
    if (!bookId) {
        return [];
    }

    const response = await fetch(`/api/books/${bookId}/comments`, {
        headers: {
            'Accept': 'application/json'
        }
    });
    return await handleApiResponse(response);
}
export async function addComment(commendData) {
    const response = await fetch(`/api/books/${commendData.bookId}/comments`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(commendData)
    });
    return await handleApiResponse(response);
}
export async function deleteComment(commentId, bookId) {
    const response = await fetch(`/api/books/${bookId}/comments/${commentId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
    });
    return await handleApiResponse(response);
}
