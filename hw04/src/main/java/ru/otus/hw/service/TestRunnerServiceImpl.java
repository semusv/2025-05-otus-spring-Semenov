package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestRunnerServiceImpl implements TestRunnerService {

    private final TestService testService;

    private final StudentService studentService;

    private final ResultService resultService;

    @Override
    public void run() {
        var currentStudent = studentService.getCurrentStudent();
        var testResult = testService.executeTestFor(currentStudent);
        resultService.showResult(testResult);
    }
}
