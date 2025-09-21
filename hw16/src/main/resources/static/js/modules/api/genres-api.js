import { handleApiResponse } from "../utils.js";

export async function fetchGenres() {
    const response = await fetch("/api/genres", {
        headers: {
            'Accept': 'application/json'
        }
    });
    return await handleApiResponse(response);
}