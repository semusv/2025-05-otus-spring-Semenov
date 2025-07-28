package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.mappers.AuthorMapper;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    private final AuthorMapper authorMapper;

    @Override
    public List<AuthorDto> findAll() {
        return authorRepository.findAll().stream().map(authorMapper::toDto).toList();
    }

    @Override
    public Optional<AuthorDto> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return authorRepository.findById(id).map(authorMapper::toDto);
    }
}
