package fi.helsinki.cs.tmc.langs.make;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * A pair (course name, exercise name).
 */
public final class ExerciseKey {
    public final String courseName;
    public final String exerciseName;

    public ExerciseKey(String courseName, String exerciseName) {
        this.courseName = courseName;
        this.exerciseName = exerciseName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExerciseKey that = (ExerciseKey) o;

        if (courseName != null ? !courseName.equals(that.courseName) : that.courseName != null) return false;
        return !(exerciseName != null ? !exerciseName.equals(that.exerciseName) : that.exerciseName != null);

    }

    @Override
    public int hashCode() {
        int result = courseName != null ? courseName.hashCode() : 0;
        result = 31 * result + (exerciseName != null ? exerciseName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return courseName + "/" + exerciseName;
    }


    public static class GsonAdapter implements JsonSerializer<ExerciseKey>, JsonDeserializer<ExerciseKey> {
        @Override
        public JsonElement serialize(ExerciseKey key, Type type, JsonSerializationContext jsc) {
            return new JsonPrimitive(key.toString());
        }

        @Override
        public ExerciseKey deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            String[] parts = je.getAsString().split("/", 2);
            if (parts.length != 2) {
                throw new JsonParseException("Invalid ExerciseKey representation: \"" + je.getAsString() + "\"");
            }
            return new ExerciseKey(parts[0], parts[1]);
        }
    }
}
