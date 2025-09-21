
import { fetchAuthors } from "../modules/api/authors-api.js";
import { showErrorMessage } from "../modules/utils.js";

document.addEventListener('DOMContentLoaded', init);
async function init() {
    try {
        const authors = await fetchAuthors();
        renderAuthorsTable(authors);
    } catch (error) {
        console.error('Failed get authors:', error);
        showErrorMessage(error.message);
    }

}

function renderAuthorsTable(authors) {
    authors.forEach(author => {
        const tableBody = document.getElementById('authors-table-body');
        tableBody.innerHTML = '';
        authors.forEach(author => {
            const row = document.createElement('tr');

            // Ячейка с ID
            const idCell = document.createElement('td');
            const idCellSpan = document.createElement('span');
            idCellSpan.textContent = author.id;
            idCell.appendChild(idCellSpan);

            // Ячейка с названием
            const fullNameCell = document.createElement('td');
            const fullNameSpan = document.createElement('span');
            fullNameSpan.textContent = author.fullName;
            fullNameCell.appendChild(fullNameSpan);

            // Добавляем ячейки в строку
            row.appendChild(idCell);
            row.appendChild(fullNameCell);

            // Добавляем строку в таблицу
            tableBody.appendChild(row);
        });
    });
}