import { fetchBooks } from "../modules/api/books-api.js";
import { showErrorMessage } from "../modules/utils.js";


document.addEventListener('DOMContentLoaded', init);
async function init() {
    try {
        const books = await fetchBooks();
        renderBooksTable(books);
    } catch (error) {
        console.error('Failed get books:', error);
        showErrorMessage(error.message);
    }
}

function renderBooksTable(books) {
    const tableBody = document.getElementById('books-table-body');
    tableBody.innerHTML = '';



    books.forEach(book => {
        const row = document.createElement('tr');

        // Ячейка с ID
        const idCell = document.createElement('td');
        idCell.className = 'clickable-cell';
        idCell.tabIndex = 0;
        idCell.addEventListener('click', () => {
            window.location.href = `/books/${book.id}`;
        });
        idCell.addEventListener('keydown', (event) => {
            if (event.key === 'Enter') {
                window.location.href = `/books/${book.id}`;
            }
        });
        const idCellSpan = document.createElement('span');
        idCellSpan.textContent = book.id;
        idCell.appendChild(idCellSpan);

        // Ячейка с названием
        const titleCell = document.createElement('td');
        const titleCellSpan = document.createElement('span');
        titleCellSpan.textContent = book.title;
        titleCell.appendChild(titleCellSpan);

        // Ячейка с автором
        const authorCell = document.createElement('td');
        const authorCellSpan = document.createElement('span');
        authorCellSpan.textContent = book.author?.fullName || '—'
        authorCell.appendChild(authorCellSpan);

        // Ячейка с жанрами
        const genresCell = document.createElement('td');
        book.genres.forEach(genre => {
            const genreSpan = document.createElement('span');
            genreSpan.className = 'genre-tag';
            genreSpan.textContent = genre.name;
            genresCell.appendChild(genreSpan);
        });

        // Добавляем ячейки в строку
        row.appendChild(idCell);
        row.appendChild(titleCell);
        row.appendChild(authorCell);
        row.appendChild(genresCell);

        // Добавляем строку в таблицу
        tableBody.appendChild(row);
    });

}
