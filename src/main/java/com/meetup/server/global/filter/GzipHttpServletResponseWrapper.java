package com.meetup.server.global.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

public class GzipHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private GzipServletOutputStream gzipOutputStream;

    public GzipHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (gzipOutputStream == null) {
            gzipOutputStream = new GzipServletOutputStream(getResponse().getOutputStream());
        }
        return gzipOutputStream;
    }

    public void finish() throws IOException {
        if (gzipOutputStream != null) {
            gzipOutputStream.flush();
            gzipOutputStream.close();
        }
    }
}
