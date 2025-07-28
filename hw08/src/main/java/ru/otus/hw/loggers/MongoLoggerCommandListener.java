package ru.otus.hw.loggers;


import org.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class MongoLoggerCommandListener implements CommandListener {

    private static final String INDENT = "  "; // Отступ для вложенных элементов

    private static final String FILTER = "Filter:\n";

    private static final String CURSOR = "cursor";

    private static final String COLLECTION = "Collection: ";

    private final Logger log = LoggerFactory.getLogger("MONGO.QUERY");

    @Override
    public void commandStarted(CommandStartedEvent event) {
        String commandName = event.getCommandName();
        BsonDocument command = event.getCommand();

        StringBuilder sb = new StringBuilder();
        sb.append("👉 ").append(commandName).append(" command\n");
        appendCommandDetails(sb, commandName, command);

        if (log.isInfoEnabled()) {
            log.info(sb.toString());
        }
    }

    private void appendCommandDetails(StringBuilder sb, String commandName, BsonDocument command) {
        switch (commandName.toLowerCase()) {
            case "find":
                appendFindCommand(sb, command);
                break;
            case "insert":
                appendInsertCommand(sb, command);
                break;
            case "update":
                appendUpdateCommand(sb, command);
                break;
            case "delete":
                appendDeleteCommand(sb, command);
                break;
            default:
                sb.append(INDENT).append("Command: ").append(command.toJson());
        }
    }

    private void appendFindCommand(StringBuilder sb, BsonDocument command) {
        sb.append(INDENT).append(COLLECTION).append(command.getString("find").getValue()).append("\n");

        if (command.containsKey("filter")) {
            sb.append(INDENT).append(FILTER);
            sb.append(formatBson((BsonDocument) command.get("filter")));
        }

        if (command.containsKey("projection")) {
            sb.append(INDENT).append("Projection:\n");
            sb.append(formatBson((BsonDocument) command.get("projection")));
        }

        if (command.containsKey("sort")) {
            sb.append(INDENT).append("Sort:\n");
            sb.append(formatBson((BsonDocument) command.get("sort")));
        }

        if (command.containsKey("limit")) {
            sb.append(INDENT).append("Limit: ").append(command.get("limit")).append("\n");
        }
    }

    private void appendInsertCommand(StringBuilder sb, BsonDocument command) {
        sb.append(INDENT).append(COLLECTION).append(command.getString("insert").getValue()).append("\n");
        sb.append(INDENT).append("Documents: ").append(command.getArray("documents").size()).append("\n");
    }

    private void appendUpdateCommand(StringBuilder sb, BsonDocument command) {
        sb.append(INDENT).append(COLLECTION).append(command.getString("update").getValue()).append("\n");

        if (command.containsKey("updates")) {
            BsonDocument updates = command.getArray("updates").get(0).asDocument();
            sb.append(INDENT).append(FILTER);
            sb.append(formatBson((BsonDocument) updates.get("q")));
            sb.append(INDENT).append("Update:\n");
            sb.append(formatBson((BsonDocument) updates.get("u")));
        }
    }

    private void appendDeleteCommand(StringBuilder sb, BsonDocument command) {
        sb.append(INDENT).append(COLLECTION).append(command.getString("delete").getValue()).append("\n");

        if (command.containsKey("deletes")) {
            BsonDocument deletes = command.getArray("deletes").get(0).asDocument();
            sb.append(INDENT).append(FILTER);
            sb.append(formatBson((BsonDocument) deletes.get("q")));
        }
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ ").append(event.getCommandName()).append(" succeeded\n");
        sb.append(INDENT).append("Time: ").append(event.getElapsedTime(TimeUnit.MILLISECONDS)).append(" ms\n");

        BsonDocument reply = event.getResponse();
        if (reply.containsKey(CURSOR) && reply.get(CURSOR).isDocument()) {
            var firstBatch = reply.get(CURSOR).asDocument().getArray("firstBatch");
            sb.append(INDENT).append("Documents: ").append(firstBatch.size()).append("\n");
        } else if (reply.containsKey("n")) {
            sb.append(INDENT).append("Modified: ").append(reply.get("n")).append("\n");
        }
        if (log.isInfoEnabled()) {
            log.info(sb.toString());
        }
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("❌ ").append(event.getCommandName()).append(" failed\n");
        sb.append(INDENT).append("Time: ").append(event.getElapsedTime(TimeUnit.MILLISECONDS)).append(" ms\n");
        sb.append(INDENT).append("Error: ").append(event.getThrowable().getMessage());
        if (log.isErrorEnabled()) {
            log.error(sb.toString());
        }
    }

    private String formatBson(BsonDocument bson) {
        String json = bson.toJson();
        return Arrays.stream(json.split("\n"))
                       .map(line -> INDENT + INDENT + line)
                       .collect(Collectors.joining("\n")) + "\n";
    }
}