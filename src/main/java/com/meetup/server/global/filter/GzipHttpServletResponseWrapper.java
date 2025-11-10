package com.meetup.server.global.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

public class GzipHttpServletResponseWrapper extends HttpServletResponseWrapper {

    public static final String GZIP = "gzip";

    private GzipServletOutputStream gzipServletOutputStream;

    public GzipHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        response.addHeader(HttpHeaders.CONTENT_ENCODING, GZIP);
        response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (gzipServletOutputStream == null) {
            gzipServletOutputStream = new GzipServletOutputStream(getResponse().getOutputStream());
        }
        return gzipServletOutputStream;
    }

    public void finish() throws IOException {
        if (gzipServletOutputStream != null) {
            gzipServletOutputStream.flush();
            gzipServletOutputStream.close();
        }
    }
}
