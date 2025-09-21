import { fetchGenres } from "../modules/api/genres-api.js";
import { showErrorMessage } from "../modules/utils.js";

document.addEventListener('DOMContentLoaded', init);

async function init() {
    try {
        const genres = await fetchGenres();
        renderGenresTable(genres);
    } catch (error) {
        console.error('Failed get genres:', error);
        showErrorMessage(error.message);
    }
}

function renderGenresTable(genres) {
    genres.forEach(genre => {

        const tableBody = document.getElementById('genres-table-body');
        tableBody.innerHTML = '';
        genres.forEach(genre => {
            const row = document.createElement('tr');

            // Ячейка с ID
            const idCell = document.createElement('td');
            const idCellSpan = document.createElement('span');
            idCellSpan.textContent = genre.id;
            idCell.appendChild(idCellSpan);

            // Ячейка с названием
            const nameCell = document.createElement('td');
            const nameSpan = document.createElement('span');
            nameSpan.textContent = genre.name;
            nameCell.appendChild(nameSpan);

            // Добавляем ячейки в строку
            row.appendChild(idCell);
            row.appendChild(nameCell);

            // Добавляем строку в таблицу
            tableBody.appendChild(row);
        });
    });
}

