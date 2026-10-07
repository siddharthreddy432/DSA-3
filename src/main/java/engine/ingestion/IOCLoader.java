package engine.ingestion;

import engine.datastructures.DynamicArray;
import engine.models.IOC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Ingestion loader for security Indicators of Compromise (IoCs).
 *
 * <p>Expected format per record (pipe-delimited):
 * <pre>
 * id|type|value|description|severity
 * </pre>
 *
 * <p>Architectural constraints:
 * <ul>
 *   <li>Zero java.util collections or third-party libraries.</li>
 *   <li>Zero String.indexOf(), String.contains(), String.split(), or regex.</li>
 *   <li>Malformed records are rejected or skipped safely without crashing batch loading.</li>
 * </ul>
 */
public class IOCLoader {

    private static final int EXPECTED_FIELD_COUNT = 5;

    private IOCLoader() {
        // Prevent instantiation of static utility class
    }

    /**
     * Parses a single pipe-delimited line into an {@link IOC} object.
     *
     * @param line the raw line to parse
     * @return a validated {@link IOC} instance
     * @throws IllegalArgumentException if the record is null, blank, or malformed
     */
    public static IOC parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("IOC record must not be null or empty");
        }

        DynamicArray<String> fields = splitByPipe(line);
        if (fields.size() != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Malformed IOC record: expected " + EXPECTED_FIELD_COUNT +
                    " pipe-delimited fields, found " + fields.size() + " in line: " + line
            );
        }

        String id = fields.get(0);
        String type = fields.get(1);
        String value = fields.get(2);
        String description = fields.get(3);
        String severity = fields.get(4);

        return new IOC(id, type, value, description, severity);
    }

    /**
     * Parses a multi-line string into a DynamicArray of {@link IOC} objects.
     * Skips empty lines, comments ('#'), and logs skipped malformed records.
     *
     * @param content multi-line string containing IOC definitions
     * @return a DynamicArray of valid {@link IOC} instances
     */
    public static DynamicArray<IOC> loadContent(String content) {
        DynamicArray<IOC> iocs = new DynamicArray<>();
        if (content == null || content.isEmpty()) {
            return iocs;
        }

        DynamicArray<String> lines = splitLines(content);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }
            try {
                IOC ioc = parseLine(line);
                iocs.add(ioc);
            } catch (IllegalArgumentException e) {
                System.err.println("[IOCLoader WARNING] Skipping malformed record at line " + (i + 1) + ": " + e.getMessage());
            }
        }

        return iocs;
    }

    /**
     * Loads IOC definitions from a file on disk.
     *
     * @param filePath absolute or relative path to the IOC file
     * @return a DynamicArray of valid {@link IOC} instances
     * @throws IOException if a file reading error occurs
     */
    public static DynamicArray<IOC> loadFile(String filePath) throws IOException {
        DynamicArray<IOC> iocs = new DynamicArray<>();
        if (filePath == null) {
            return iocs;
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
                    IOC ioc = parseLine(trimmed);
                    iocs.add(ioc);
                } catch (IllegalArgumentException e) {
                    System.err.println("[IOCLoader WARNING] Skipping malformed line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        return iocs;
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
