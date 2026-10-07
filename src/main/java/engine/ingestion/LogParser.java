package engine.ingestion;

import engine.datastructures.DynamicArray;
import engine.models.LogEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Ingestion parser for structured security logs.
 *
 * <p>Expected format per record (pipe-delimited):
 * <pre>
 * timestamp|sourceIp|targetHost|eventType|payload
 * </pre>
 *
 * <p>Architectural constraints:
 * <ul>
 *   <li>Zero java.util collections or third-party libraries.</li>
 *   <li>Zero String.indexOf(), String.contains(), String.split(), or regex.</li>
 *   <li>Malformed records are rejected or skipped safely without crashing batch ingestion.</li>
 * </ul>
 */
public class LogParser {

    private static final int EXPECTED_FIELD_COUNT = 5;

    private LogParser() {
        // Prevent instantiation of static utility class
    }

    /**
     * Parses a single pipe-delimited line into a {@link LogEntry}.
     *
     * @param line the raw log line to parse
     * @return a validated {@link LogEntry} instance
     * @throws IllegalArgumentException if the record is null, blank, or does not contain exactly 5 fields
     */
    public static LogEntry parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Log record must not be null or empty");
        }

        DynamicArray<String> fields = splitByPipe(line);
        if (fields.size() != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Malformed log record: expected " + EXPECTED_FIELD_COUNT +
                    " pipe-delimited fields, found " + fields.size() + " in line: " + line
            );
        }

        String timestamp = fields.get(0);
        String sourceIp = fields.get(1);
        String targetHost = fields.get(2);
        String eventType = fields.get(3);
        String payload = fields.get(4);

        return new LogEntry(timestamp, sourceIp, targetHost, eventType, payload);
    }

    /**
     * Parses a multi-line string containing multiple log entries.
     * Skips empty lines, comment lines (starting with '#'), and logs skipped malformed records.
     *
     * @param content multi-line log text
     * @return a DynamicArray of successfully parsed {@link LogEntry} records
     */
    public static DynamicArray<LogEntry> parseContent(String content) {
        DynamicArray<LogEntry> entries = new DynamicArray<>();
        if (content == null || content.isEmpty()) {
            return entries;
        }

        DynamicArray<String> lines = splitLines(content);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }
            try {
                LogEntry entry = parseLine(line);
                entries.add(entry);
            } catch (IllegalArgumentException e) {
                System.err.println("[LogParser WARNING] Skipping malformed record at line " + (i + 1) + ": " + e.getMessage());
            }
        }

        return entries;
    }

    /**
     * Reads a log file from disk and parses each line into {@link LogEntry} records.
     * Malformed lines are skipped with a warning message.
     *
     * @param filePath absolute or relative path to the log file
     * @return a DynamicArray of successfully parsed {@link LogEntry} records
     * @throws IOException if a file reading error occurs
     */
    public static DynamicArray<LogEntry> parseFile(String filePath) throws IOException {
        DynamicArray<LogEntry> entries = new DynamicArray<>();
        if (filePath == null) {
            return entries;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.charAt(0) == '#') {
                    continue;
                }
                try {
                    LogEntry entry = parseLine(trimmed);
                    entries.add(entry);
                } catch (IllegalArgumentException e) {
                    System.err.println("[LogParser WARNING] Skipping malformed line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        return entries;
    }

    /**
     * Splits a string by pipe ('|') character using first principles without regex or indexOf.
     */
    public static DynamicArray<String> splitByPipe(String line) {
        DynamicArray<String> fields = new DynamicArray<>();
        if (line == null) {
            return fields;
        }

        int len = line.length();
        int start = 0;
        for (int i = 0; i < len; i++) {
            if (line.charAt(i) == '|') {
                fields.add(line.substring(start, i));
                start = i + 1;
            }
        }
        fields.add(line.substring(start, len));
        return fields;
    }

    /**
     * Splits text into individual lines without regex or collections.
     */
    public static DynamicArray<String> splitLines(String text) {
        DynamicArray<String> lines = new DynamicArray<>();
        if (text == null) {
            return lines;
        }

        int len = text.length();
        int start = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                int end = (i > start && text.charAt(i - 1) == '\r') ? i - 1 : i;
                lines.add(text.substring(start, end));
                start = i + 1;
            }
        }
        if (start <= len) {
            lines.add(text.substring(start, len));
        }
        return lines;
    }
}
