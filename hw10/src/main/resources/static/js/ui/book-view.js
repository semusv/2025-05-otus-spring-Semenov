document.addEventListener('DOMContentLoaded', initPage);


import { fetchBook } from "../modules/api/books-api.js";
import { fetchCommentsByBookId, addComment } from "../modules/api/comments-api.js";
import { showErrorMessage, getBookIdFromUrl, showSuccessMessage, delayLoader } from "../modules/utils.js";

async function initPage() {
    try {
        const loaderMinTime = 1000;
        const startTime = Date.now();

        updateProgress(30);

        const bookId = getBookIdFromUrl();
        const [book, comments] = await Promise.all([
            fetchBook(bookId),
            fetchCommentsByBookId(bookId),
            delayLoader(loaderMinTime, startTime)
        ]);
        updateProgress(60);

        renderBook(book);
        updateProgress(70);

        renderComments(comments);
        updateProgress(80);

        await setupEventListeners(bookId);
        updateProgress(100);
    } catch (error) {
        console.error('Failed init page while getting data:', error);
        showErrorMessage(error.message);
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
    document.getElementById('page-title').textContent = `${book.title} | ${document.getElementById('page-title').textContent}`;
    document.getElementById('meta-description').content += ` ${book.title}`;
    document.getElementById('breadcrumb-current').textContent = book.title;

    document.getElementById('book-id').value = book.id;
    document.getElementById('book-title').textContent = book.title;
    document.getElementById('book-author').textContent = book.author.fullName;
    document.getElementById('edit-button').href = `/books/${book.id}/edit`;

    const genresList = document.getElementById('genres-list');
    genresList.innerHTML = book.genres.map(genre =>
        `<span class="genre-badge">${genre.name}</span>`
    ).join('');
}

function renderComments(comments) {
    const commentsList = document.getElementById('comments-list');
    commentsList.querySelectorAll('.comment-item').forEach(el => el.remove());

    renderEmptyCommentsElement(comments);
    comments.forEach(comment => renderNextCommentElement(comment));
}

function renderEmptyCommentsElement(comments) {
    const emptyComments = document.getElementById('comments-empty');
    emptyComments.style.display = comments.length ? 'none' : 'block';
}

function renderNextCommentElement(comment) {
    const commentsList = document.getElementById('comments-list');

    const commentItem = document.createElement('div');
    commentItem.className = 'comment-item';

    //Заголовок комментария
    const commentHeaderItem = document.createElement('div');
    commentHeaderItem.className = 'comment-header';
    const commentHeaderText = document.createElement('span');
    commentHeaderText.textContent = `Комментарий #${comment.id}`;
    commentHeaderText.className = `comment-id`;
    commentHeaderItem.appendChild(commentHeaderText);
    //Текст комментария
    const commentText = document.createElement('div');
    commentText.className = 'comment-text';
    commentText.textContent = comment.text;

    //
    commentItem.appendChild(commentHeaderItem);
    commentItem.appendChild(commentText);
    //
    commentsList.appendChild(commentItem);
}
async function setupEventListeners(bookId) {
    const commentForm = document.getElementById('comment-form');
    commentForm.addEventListener('submit', handleAddComment);
}

async function handleAddComment(event) {
    const commentForm = document.getElementById('comment-form');

    if (!commentForm.checkValidity()) {
        commentForm.reportValidity();
        return;
    }

    event.preventDefault();
    const formData = new FormData(commentForm);
    const text = formData.get('text');
    const bookId = document.getElementById('book-id').value;

    const commentDto = {
        bookId: Number(bookId),
        text: text
    };

    try {
        const result = await addComment(commentDto);

        showSuccessMessage(result.message);

        const newComment = result.data;
        renderNextCommentElement(newComment);
        commentForm.reset();

    } catch (error) {
        console.error('Failed add comment:', error);

        if (error.type === 'VALIDATION_FAILED') {
            error.errors.forEach(error => {
                showErrorMessage(error.message);
            });
        } else {
            showErrorMessage(error.message);
        }
    }
}


