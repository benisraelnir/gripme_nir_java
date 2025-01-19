package com.grip.reader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Reads Readme text from standard input.
 */
public class StdinReader extends TextReader {
    /**
     * Creates a new StdinReader.
     *
     * @param displayFilename Optional display filename
     */
    public StdinReader(String displayFilename) {
        super(null, displayFilename);
    }

    @Override
    public Object read(String subpath) throws IOException {
        if (getText() == null && subpath == null) {
            setText(readStdin());
        }
        return super.read(subpath);
    }

    @Override
    public String normalizeSubpath(String subpath) {
        return super.normalizeSubpath(subpath);
    }

    @Override
    public String mimetypeFor() {
        return mimetypeFor(null);
    }

    @Override
    public boolean isBinary() {
        return isBinary(null);
    }

    @Override
    public Long lastUpdated() {
        return lastUpdated(null);
    }

    /**
     * Reads STDIN until the end of input and returns a string.
     */
    private String readStdin() throws IOException {
        Charset charset;
        try {
            charset = Charset.forName(System.getProperty("file.encoding", "UTF-8"));
        } catch (IllegalArgumentException | SecurityException e) {
            charset = StandardCharsets.UTF_8;
        }
        
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = System.in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(charset.name());
    }
}