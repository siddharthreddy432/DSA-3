package engine.models;

/**
 * Immutable model representing an Indicator of Compromise (IoC).
 *
 * <p>Architectural constraint: Zero java.util collections or third-party libraries.
 */
public class IOC {

    private final String id;
    private final String type;
    private final String value;
    private final String description;
    private final String severity;

    /**
     * Constructs an IOC instance.
     *
     * @param id          unique identifier (e.g., "IOC-001")
     * @param type        indicator classification (e.g., "COMMAND", "TOOL", "PROTOCOL")
     * @param value       signature pattern to search for (e.g., "powershell -enc")
     * @param description contextual explanation of the threat indicator
     * @param severity    severity rating (e.g., "LOW", "MEDIUM", "HIGH", "CRITICAL")
     * @throws IllegalArgumentException if any mandatory field is null or blank
     */
    public IOC(String id, String type, String value, String description, String severity) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("IOC id must not be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("IOC type must not be null or empty");
        }
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("IOC value must not be null or empty");
        }
        if (description == null) {
            throw new IllegalArgumentException("IOC description must not be null");
        }
        if (severity == null || severity.trim().isEmpty()) {
            throw new IllegalArgumentException("IOC severity must not be null or empty");
        }

        this.id = id.trim();
        this.type = type.trim();
        this.value = value.trim();
        this.description = description.trim();
        this.severity = severity.trim();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IOC other = (IOC) obj;
        return id.equals(other.id) &&
               type.equals(other.type) &&
               value.equals(other.value) &&
               severity.equals(other.severity);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + severity.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "IOC{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", severity='" + severity + '\'' +
                '}';
    }
}
