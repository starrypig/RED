package org.robotframework.ide.core.testData.parser.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.robotframework.ide.core.testData.parser.IParsePositionMarkable;


/**
 * Gives possibility of {@link BufferedReader} and {@link PushbackReader} with
 * respect to underlining {@link ByteBuffer} position. If you use this class you
 * can simple use {@link ByteBuffer} as common view for many readers.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class ByteBufferReader extends Reader implements IParsePositionMarkable {

    private final Charset charset;
    private final ByteBuffer buffer;
    private final ByteBufferInputStream inStream;


    /**
     * @param inStream
     *            input stream with byte buffer inside
     * @param charsetName
     *            encoding {@link Charset}
     */
    public ByteBufferReader(final ByteBufferInputStream inStream,
            String charsetName) {
        this.inStream = inStream;
        this.buffer = inStream.getByteBuffer();
        this.charset = Charset.forName(charsetName);
    }


    /**
     * 
     * @param b
     *            buffer with data
     * @param charsetName
     *            encoding {@link Charset}
     */
    public ByteBufferReader(final ByteBuffer b, String charsetName) {
        this.buffer = b;
        this.inStream = new ByteBufferInputStream(buffer);
        this.charset = Charset.forName(charsetName);
    }


    /**
     * @return single read line or {@code null}
     * @throws IOException
     */
    public String readLine() throws IOException {
        String line = null;

        boolean wasSomeData = inStream.available() > 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while(inStream.available() > 0) {
            int b = inStream.read();
            if (b == '\r') {
                if (inStream.currentByteInBuffer() == '\n') {
                    inStream.read();
                }
                break;
            } else if (b == '\n') {
                break;
            } else {
                baos.write(b);
            }
        }

        if (wasSomeData) {
            line = baos.toString(charset.name());
        }

        return line;
    }


    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        byte[] bytes = new byte[cbuf.length];
        int readBytes = inStream.read(bytes, off, len);
        if (readBytes > 0) {
            char[] got = charset.decode(ByteBuffer.wrap(bytes, off, readBytes))
                    .array();
            copyArrayToArray(got, cbuf, off, len);
        }

        return readBytes;
    }


    private void copyArrayToArray(char[] src, char[] dst, int off, int len) {
        for (int i = 0; i < len; i++) {
            dst[off + i] = src[i];
        }
    }


    @Override
    public void close() throws IOException {
        inStream.close();
    }


    @Override
    public void mark() {
        inStream.mark();
    }


    @Override
    public void mark(int readAheadBytes) {
        inStream.mark(readAheadBytes);
    }


    @Override
    public void reset() {
        inStream.reset();
    }
}
