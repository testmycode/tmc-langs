package fi.helsinki.cs.tmc.langs.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueObject {

    private Logger log = LoggerFactory.getLogger(ValueObject.class);

    private Object value;

    public ValueObject(Object object) {
        this.value = object;
    }

    public Object get() {
        return this.value;
    }

    public String asString() {
        if (!(value instanceof String)) {
            log.error("Couldn't convert configuration {} to String.", value.toString());
            return null;
        }
        return (String) this.value;
    }

    public Boolean asBoolean() {
        if (!(value instanceof Boolean)) {
            log.error("Couldn't convert configuration {} to Boolean.", value.toString());
            return null;
        }
        return (Boolean) this.value;
    }
}
