package fi.helsinki.cs.tmc.langs.io.zip;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class ZipProcessor {

    private byte[] buffer = new byte[1024];

    protected void copyBytes(InputStream in, OutputStream out) throws IOException {
        int available = in.read(buffer);
        while (available >= 0) {
            out.write(buffer, 0, available);
            available = in.read(buffer);
        }
    }
}
