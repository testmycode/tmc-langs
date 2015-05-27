package fi.helsinki.cs.tmc.langs.testrunner;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class StackTraceSerializer implements InstanceCreator<StackTraceElement>, JsonSerializer<StackTraceElement>, JsonDeserializer<StackTraceElement> {

    @Override
    public StackTraceElement createInstance(Type type) {
        return new StackTraceElement("", "", "", 0);
    }

    @Override
    public JsonElement serialize(StackTraceElement ste, Type type, JsonSerializationContext jsc) {
        JsonObject obj = new JsonObject();
        obj.addProperty("declaringClass", ste.getClassName());
        obj.addProperty("methodName", ste.getMethodName());
        obj.addProperty("fileName", ste.getFileName());
        obj.addProperty("lineNumber", ste.getLineNumber());
        return obj;
    }

    @Override
    public StackTraceElement deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonObject obj = je.getAsJsonObject();
        String declaringClass = getStringOrNull(obj, "declaringClass");
        String methodName = getStringOrNull(obj, "methodName");
        String fileName = getStringOrNull(obj, "fileName");
        int lineNumber = obj.get("lineNumber").getAsInt();
        return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }

    private String getStringOrNull(JsonObject obj, String property) {
        JsonElement element = obj.get(property);
        if (element != null && !element.isJsonNull()) {
            return element.getAsString();
        } else {
            return null;
        }
    }
}
