package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import ru.otus.hw.config.AclConfig;
import ru.otus.hw.config.CacheConfig;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.api.BookFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mappers.AuthorMapperImpl;
import ru.otus.hw.mappers.BookMapperImpl;
import ru.otus.hw.mappers.CommentMapperImpl;
import ru.otus.hw.mappers.GenreMapperImpl;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Сервис для работы с книгами")
@DataJpaTest()
@Import({
        BookServiceImpl.class,
        BookMapperImpl.class,
        AuthorMapperImpl.class,
        GenreMapperImpl.class,
        CommentMapperImpl.class,
        AclServiceWrapperServiceImpl.class,
        AclConfig.class,
        CacheConfig.class
})
@EnableMethodSecurity
class BookServiceImplSecurityTest {

    @Autowired
    private BookService bookService;


    @Test
    @DisplayName("должен позволять создателю книги обновлять ее")
    @WithMockUser(username = "user")
    void shouldAllowOwnerToUpdateBook() {
        // Создаем книгу от имени user
        BookFormDto bookCreateDto = BookFormDto.builder()
                .title("Test Book")
                .authorId(1L)
                .genreIds(Set.of(1L, 2L))
                .build();

        BookDto createdBook = bookService.insert(bookCreateDto);

        // Пытаемся обновить книгу от того же пользователя
        BookFormDto updateDto = BookFormDto.builder()
                .title("Updated Title")
                .authorId(1L)
                .genreIds(Set.of(1L, 2L))
                .build();

        BookDto updatedBook = bookService.update(createdBook.id(), updateDto);

        assertThat(updatedBook.title()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("должен запрещать другому пользователю обновлять книгу")
    @WithMockUser(username = "user999")
    void shouldDenyOtherUserToUpdateBook() {
        long id = 1L;
        BookFormDto updateDto = BookFormDto.builder()
                .title("Hacked Title")
                .authorId(1L)
                .genreIds(Set.of(1L, 2L))
                .build();

        assertThatThrownBy(() -> bookService.update(id, updateDto))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("должен позволять ADMIN удалять любую книгу")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAllowAdminToDeleteAnyBook() {
        long bookId = 1L;

        // ADMIN должен иметь возможность удалить книгу без исключений
        bookService.deleteById(bookId);

        // Проверяем, что книга действительно удалена
        assertThatThrownBy(() -> bookService.findById(bookId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("должен запрещать обычному пользователю удалять книгу")
    @WithMockUser(username = "user9999")
    void shouldDenyRegularUserToDeleteBook() {
        long bookId = 1L;

        assertThatThrownBy(() -> bookService.deleteById(bookId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("должен позволять создателю удалять свою книгу")
    @WithMockUser(username = "user")
    void shouldAllowOwnerToDeleteOwnBook() {
        // Создаем книгу от имени owner
        BookFormDto bookCreateDto = BookFormDto.builder()
                .title("Owned Book")
                .authorId(1L)
                .genreIds(Set.of(1L, 2L))
                .build();

        BookDto createdBook = bookService.insert(bookCreateDto);

        bookService.deleteById(createdBook.id());

        assertThatThrownBy(() -> bookService.findById(createdBook.id()))
                .isInstanceOf(EntityNotFoundException.class);
    }

}