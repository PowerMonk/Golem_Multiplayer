import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameMessage {
    private static final String SEPARATOR = "|";

    private final MessageType type;
    private final List<String> fields;

    private GameMessage(MessageType type, List<String> fields) {
        this.type = type;
        this.fields = fields;
    }

    public static GameMessage of(MessageType type, Object... values) {
        List<String> parts = new ArrayList<>();
        if (values != null) {
            for (Object value : values) {
                parts.add(value == null ? "" : value.toString());
            }
        }
        return new GameMessage(type, parts);
    }

    public static GameMessage parse(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String[] split = trimmed.split("\\\\|", -1);
        if (split.length == 0) {
            return null;
        }

        MessageType parsedType;
        try {
            parsedType = MessageType.valueOf(split[0].toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }

        List<String> parsedFields = new ArrayList<>();
        for (int i = 1; i < split.length; i++) {
            parsedFields.add(split[i]);
        }

        return new GameMessage(parsedType, parsedFields);
    }

    public MessageType getType() {
        return type;
    }

    public List<String> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public String getField(int index, String defaultValue) {
        if (index < 0 || index >= fields.size()) {
            return defaultValue;
        }
        return fields.get(index);
    }

    public int getInt(int index, int defaultValue) {
        String value = getField(index, null);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public long getLong(int index, long defaultValue) {
        String value = getField(index, null);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public double getDouble(int index, double defaultValue) {
        String value = getField(index, null);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public String encode() {
        StringBuilder sb = new StringBuilder(type.name());
        for (String field : fields) {
            sb.append(SEPARATOR).append(field == null ? "" : field);
        }
        return sb.toString();
    }
}
