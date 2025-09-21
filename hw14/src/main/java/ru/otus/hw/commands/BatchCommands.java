package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@ShellComponent
public class BatchCommands {


    private static final String MIGRATION_JOB = "migrationJob";

    private final JobLauncher jobLauncher;

    private final Job migrationJob;

    private final JobExplorer jobExplorer;

    @ShellMethod(value = "Start migration H2 -> MongoDb", key = "m-start")
    public String migrate() throws JobExecutionException {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addLong("startAt", System.currentTimeMillis());
        JobParameters parameters = jobParametersBuilder
                .toJobParameters();

        JobExecution execution = jobLauncher.run(migrationJob, parameters);
        System.out.println(execution);
        return "Миграция запущена. ID: " + execution.getId() +
               ", Status: " + execution.getStatus();

    }

    @ShellMethod(value = "Показать статус миграций", key = "m-status")
    public String status() {
        List<JobInstance> instances = jobExplorer.getJobInstances(MIGRATION_JOB, 0, 10);

        if (instances.isEmpty()) {
            return "Миграции не выполнялись";
        }

        StringBuilder result = new StringBuilder("История миграций:\n");
        for (JobInstance instance : instances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
            for (JobExecution execution : executions) {
                result.append(String.format("ID: %d, Статус: %s, Время: %s%n",
                        execution.getId(),
                        execution.getStatus(),
                        execution.getCreateTime()));
            }
        }

        return result.toString();
    }

    @ShellMethod(value = "Перезапуск последней неудачной миграции", key = "m-restart")
    public String restart() throws JobExecutionException {

        List<JobInstance> lastInstances = jobExplorer.getJobInstances(MIGRATION_JOB, 0, 1);

        if (lastInstances.isEmpty()) {
            return "Задания не создавались " + MIGRATION_JOB;
        }

        JobInstance lastInstance = lastInstances.get(0);
        List<JobExecution> executions = jobExplorer.getJobExecutions(lastInstance);
        if (executions.isEmpty()) {
            return "Нет заданий, к-е ранее выполнялись для " + MIGRATION_JOB;
        }

        JobExecution lastExecution = executions.get(0);
        if (lastExecution.getStatus() != BatchStatus.FAILED) {
            return "Последнее задание не завершилось ошибкой. Статус: " + lastExecution.getStatus();
        }

        JobParameters parameters = lastExecution.getJobParameters();
        JobExecution restartedExecution = jobLauncher.run(migrationJob, parameters);

        return "Перезапуск выполнен. Новый ID: " + restartedExecution.getId();
    }
}
