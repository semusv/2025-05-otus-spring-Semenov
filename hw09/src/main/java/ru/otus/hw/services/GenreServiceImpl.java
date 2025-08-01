package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.mappers.GenreMapper;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    private final GenreMapper genreDtoConverter;

    @Override
    @Transactional(readOnly = true)
    public List<GenreDto> findAll() {
        return genreRepository.findAll().stream().map(genreDtoConverter::toGenreDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenreDto> findAllByIds(Set<Long> ids) {
        return genreRepository.findAllById(ids).stream().map(genreDtoConverter::toGenreDto).toList();
    }
}
