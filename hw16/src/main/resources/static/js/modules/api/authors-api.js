import { handleApiResponse } from "../utils.js";

export async function fetchAuthors() {
    const response = await fetch("/api/authors", {
        headers: {
            'Accept': 'application/json'
        }
    });
    return await handleApiResponse(response);
}