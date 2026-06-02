package app.photogear.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Instancia compartida de Gson con adaptadores para java.time.
 * Usar GsonConfig.get() en lugar de new Gson() en toda la aplicación.
 */
public class GsonConfig {

    private static final Gson INSTANCE = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,     new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .create();

    private GsonConfig() {}

    public static Gson get() { return INSTANCE; }

    // ── Adaptadores ──────────────────────────────────────────

    private static class LocalDateAdapter
            implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate src, Type type, JsonSerializationContext ctx) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) {
            if (json == null || json.isJsonNull()) return null;
            String s = json.getAsString();
            return s.isBlank() ? null : LocalDate.parse(s);
        }
    }

    private static class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime src, Type type, JsonSerializationContext ctx) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) {
            if (json == null || json.isJsonNull()) return null;
            String s = json.getAsString();
            return s.isBlank() ? null : LocalDateTime.parse(s);
        }
    }
}
