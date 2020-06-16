package fi.helsinki.cs.tmc.langs.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class ValueObject {

    private static final Logger log = LoggerFactory.getLogger(ValueObject.class);

    private Object value;

    public ValueObject(Object object) {
        this.value = object;
    }

    public Object get() {
        return this.value;
    }

    /**
     * Returns the value of this object as a String.
     *
     * @return Value as a String. Null if value isn't a String.
     */
    public String asString() {
        if (!(value instanceof String)) {
            log.error("Couldn't convert configuration {} to String.", value.toString());
            return null;
        }
        return (String) this.value;
    }

    /**
     * Returns the value of this object as a Boolean.
     *
     * @return Value as a Boolean. Null if value isn't a Boolean.
     */
    public Boolean asBoolean() {
        if (!(value instanceof Boolean)) {
            log.error("Couldn't convert configuration {} to Boolean.", value.toString());
            return null;
        }
        return (Boolean) this.value;
    }

    public List<String> asList() {
        if (!(value instanceof List)) {
            log.error("Couldn't convert configuration {} to List.", value.toString());
            return null;
        }
        return (List<String>) this.value;
    }

    public Integer asInteger() {
        if (!(value instanceof Integer)) {
            log.error("Couldn't convert configuration {} to Long.", value.toString());
            return null;
        }
        return (Integer) this.value;
    }

    @Override
    public String toString() {
        return "<ValueObject: value=" + value + ">";
    }
}
