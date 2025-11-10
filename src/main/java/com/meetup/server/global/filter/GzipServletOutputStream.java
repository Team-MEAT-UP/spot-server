package com.meetup.server.global.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipServletOutputStream extends ServletOutputStream {

    private final GZIPOutputStream gzipOutputStream;

    public GzipServletOutputStream(OutputStream outputStream) throws IOException {
        this.gzipOutputStream = new GZIPOutputStream(outputStream);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }

    @Override
    public void write(int b) throws IOException {
        gzipOutputStream.write(b);
    }

    @Override
    public void flush() throws IOException {
        gzipOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        gzipOutputStream.close();
    }
}
