document.addEventListener('DOMContentLoaded', initPage);

import { fetchAuthors } from "../modules/api/authors-api.js";
import { fetchGenres } from "../modules/api/genres-api.js";
import { fetchBook, addBook, saveBook, deleteBook } from "../modules/api/books-api.js";
import { deleteComment, fetchCommentsByBookId } from "../modules/api/comments-api.js";
import { showErrorMessage, getBookIdFromUrl, showSuccessMessage, delayLoader } from "../modules/utils.js";


async function initPage() {
    const loaderMinTime = 1000;
    const startTime = Date.now();

    updateProgress(30);


    try {
        const bookId = getBookIdFromUrl();
        const [book, comments, authors, genres] = await Promise.all([
            fetchBook(bookId),
            fetchCommentsByBookId(bookId),
            fetchAuthors(),
            fetchGenres(),
            delayLoader(loaderMinTime, startTime)
        ]);
        updateProgress(60);
        renderAuthors(authors);

        updateProgress(70);
        renderGenres(genres);

        updateProgress(80);
        renderBook(book);

        updateProgress(90);
        renderComments(comments);

        await setupEventListeners(bookId);

        updateProgress(100);
    } catch (error) {
        console.error('Error init page while getting data:', error);
        const errorMsg = document.getElementById('i18n-messages').dataset.loadData;
        showErrorMessage(errorMsg);
    } finally {
        hideLoader();
    }
}

function updateProgress(percent) {
    document.getElementById('progress').style.width = percent + '%';
}

function hideLoader() {
    const loader = document.getElementById('loader');
    loader.style.opacity = '0';
    setTimeout(() => {
        loader.style.display = 'none';
        document.getElementById('content').style.display = 'block';
    }, 300);
}

function renderBook(book) {
    document.getElementById('page-title').textContent =
        book == null
            ? document.getElementById('i18n-messages').dataset.pageTitleNewBook
            : book.title + ' | ' + document.getElementById('i18n-messages').dataset.pageTitleEdit;
    document.getElementById('breadcrumb-current').textContent =
        book == null
            ? document.getElementById('i18n-messages').dataset.breadcrumbNew
            : document.getElementById('i18n-messages').dataset.breadcrumbEdit;

    document.getElementById('book-header-title').textContent =
        book == null
            ? document.getElementById('book-header-title').textContent
            : book.title;

    const saveButton = document.getElementById('save-button');
    if (book != null) {
        document.getElementById('view-button').href = `/books/${book.id}`;
        document.getElementById('book-id').value = book.id;
        document.getElementById('book-title').value = book.title;
        setSelectedAuthor(book.author)
        setSelectedGenres(book.genres)
        document.getElementById('cancel-button').style.display = 'none';
        saveButton.textContent = document.getElementById('i18n-messages').dataset.buttonSave;
    } else {
        document.getElementById('book-navigation-buttons').style.display = 'none';
        saveButton.textContent = document.getElementById('i18n-messages').dataset.buttonAdd;
    }
}

function renderAuthors(authors) {
    const authorList = document.getElementById('authors-list');
    authorList.innerHTML = '';

    const placeholderOption = document.createElement('option');
    placeholderOption.value = "";
    placeholderOption.textContent = "Выберите автора";
    placeholderOption.disabled = true;
    placeholderOption.selected = true;
    authorList.appendChild(placeholderOption);

    authors.forEach(author => {
        const option = document.createElement('option');
        option.value = author.id;
        option.textContent = author.fullName;
        authorList.appendChild(option);
    });
}

function renderGenres(genres) {
    const genreList = document.getElementById('genres-list');
    genreList.innerHTML = '';

    genres.forEach(genre => {
        const option = document.createElement('option');
        option.value = genre.id;
        option.textContent = genre.name;
        genreList.appendChild(option);
    });
}

function renderComments(comments) {
    const commentsSection = document.getElementById('comments-section');
    if (comments.length == 0) {
        commentsSection.style.display = 'none';
        return;
    }

    const commentsList = document.getElementById('comments-list');
    commentsList.querySelectorAll('.comment-item').forEach(el => el.remove());
    comments.forEach(comment => renderNextCommentElement(comment));
}

function renderNextCommentElement(comment) {
    const commentsList = document.getElementById('comments-list');

    // Создаем основной контейнер комментария
    const commentItem = document.createElement('div');
    commentItem.className = 'comment-item';
    commentItem.dataset.commentId = comment.id;

    // Создаем заголовок комментария
    const commentHeader = document.createElement('div');
    commentHeader.className = 'comment-header';

    // Создаем элемент с ID комментария
    const commentIdSpan = document.createElement('span');
    commentIdSpan.className = 'comment-id';
    commentIdSpan.textContent = `Комментарий #${comment.id}`;

    // Создаем кнопку удаления
    const deleteButton = document.createElement('button');
    deleteButton.type = 'button';
    deleteButton.className = 'btn-icon btn-icon-delete';
    deleteButton.textContent = '✖';
    deleteButton.addEventListener('click', handleDeleteComment);

    // Создаем блок с текстом комментария
    const commentText = document.createElement('div');
    commentText.className = 'comment-text';
    commentText.textContent = comment.text;

    // Собираем структуру
    commentHeader.appendChild(commentIdSpan);
    commentHeader.appendChild(deleteButton);

    commentItem.appendChild(commentHeader);
    commentItem.appendChild(commentText);

    // Добавляем в список комментариев
    commentsList.appendChild(commentItem);
}

function setSelectedAuthor(author) {
    const authorSelect = document.getElementById('authors-list');
    authorSelect.value = author.id;
}

function setSelectedGenres(genres) {
    const genreSelect = document.getElementById('genres-list');
    Array.from(genreSelect.options).forEach(option => {
        option.selected = false;
    });

    genres.forEach(genre => {
        const option = genreSelect.querySelector(`option[value="${genre.id}"]`);
        if (option) {
            option.selected = true;
        }
    });
}

async function setupEventListeners(bookId) {
    if (bookId == null) {
        document.getElementById('save-button').addEventListener('click', handleAddBook);

    } else {
        document.getElementById('delete-button').addEventListener('click', handleDeleteBook);
        document.getElementById('save-button').addEventListener('click', handleSaveBook);
    }
}

async function handleAddBook(event) {
    const bookForm = document.getElementById("book-form");
    event.preventDefault();

    if (!bookForm.checkValidity()) {
        bookForm.reportValidity();
        return;
    }

    const formData = new FormData(bookForm);
    const bookId = null;
    const title = formData.get('title');
    const authorId = formData.get('authorId');
    const genreIdList = formData.getAll('genresIds');

    const bookDto = {
        id: bookId,
        title: title,
        authorId: authorId,
        genreIds: genreIdList
    };

    try {
        const result = await addBook(bookDto);
        showSuccessMessage(result.message);
        window.location.href = '/books/' + result.data.id;
    }
    catch (error) {
        console.error('Failed insert book:', error);
        if (error.type === 'VALIDATION_FAILED') {
            error.errors.forEach(error => {
                showErrorMessage(error.message);
            });
        } else {
            showErrorMessage(error.message);
        }
    }
}

async function handleSaveBook(event) {
    const bookForm = document.getElementById("book-form");
    event.preventDefault();

    if (!bookForm.checkValidity()) {
        bookForm.reportValidity();
        return;
    }

    const formData = new FormData(bookForm);
    const bookId = formData.get('id');
    const title = formData.get('title');
    const authorId = formData.get('authorId');
    const genreIdList = formData.getAll('genresIds');

    const bookUpdateDto = {
        id: bookId,
        title: title,
        authorId: authorId,
        genreIds: genreIdList
    };

    try {
        const result = await saveBook(bookUpdateDto);
        showSuccessMessage(result.message);
    } catch (error) {
        console.error('Failed save book:', error);

        if (error.type === 'VALIDATION_FAILED') {
            error.errors.forEach(error => {
                showErrorMessage(error.message);
            });
        } else {
            showErrorMessage(error.message);
        }
    }
}

async function handleDeleteBook(event) {
    event.preventDefault();

    const bookId = document.getElementById('book-id').value;

    const confirmMsg = document.getElementById('i18n-messages').dataset.confirmDelete;
    if (!confirm(confirmMsg)) {
        return;
    }

    try {
        const result = await deleteBook(bookId);
        showSuccessMessage(result.message);

        window.location.href = '/';
    }
    catch (error) {
        console.error('Failed delete book:', error);
        if (error.type === 'VALIDATION_FAILED') {
            error.errors.forEach(error => {
                showErrorMessage(error.message);
            });
        } else {
            showErrorMessage(error.message);
        }
    }
}
async function handleDeleteComment(event) {
    const confirmMsg = document.getElementById('i18n-messages').dataset.confirmDeleteComment;

    const button = event.target;
    const commentItem = button.closest('.comment-item');
    const commentId = commentItem.dataset.commentId;

    if (!confirm(confirmMsg)) {
        return;
    }
    try {
        const result = await deleteComment(commentId);
        commentItem.remove();
        showSuccessMessage(result.message);
    } catch (error) {
        console.error('Failed delete comment:', error);
        if (error.type === 'VALIDATION_FAILED') {
            error.errors.forEach(error => {
                showErrorMessage(error.message);
            });
        } else {
            showErrorMessage(error.message);
        }
    }
}
