package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpClient {
    private static final int HTTP_PORT = 80;

    public static Builder newBuilder() {
        return new Builder();
    }

    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
        String host = request.uri().getHost();
        try (Socket socket = new Socket(host, HTTP_PORT);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            sendRequest(request, out);
            return new HttpResponse<>(in, responseBodyHandler);
        }
    }

    void sendRequest(HttpRequest request, OutputStream out) throws IOException {
        PrintWriter pw = new PrintWriter(out);
        printFirstLine(request, pw);

        HttpRequest.BodyPublisher bodyPublisher = request.bodyPublisher().get();
        if (bodyPublisher.isInputStream) {
            ByteArrayOutputStream baosBody = getBaosBody(bodyPublisher);
            request.headers().replace("Content-Length", String.valueOf(baosBody.size()));
            printHeaders(request.headers(), pw);
            baosBody.writeTo(out);
            return;
        }

        printHeaders(request.headers(), pw);
        String method = request.method();
        if ((method.equals("POST") || method.equals("PUT")) && bodyPublisher.body != null)
            printBody(bodyPublisher, pw);
    }

    void printFirstLine(HttpRequest request, PrintWriter pw) {
        URI uri = request.uri();
        String method = request.method();
        pw.print(method + " " + uri.getPath() + " HTTP/1.1\r\n");
    }

    void printHeaders(HttpHeaders headers, PrintWriter pw) {
        for (Map.Entry<String, List<String>> headerEntry : headers.headers.entrySet()) {
            String headerName = headerEntry.getKey();
            List<String> headerValuesList = headerEntry.getValue();
            String headerValue = buildHeaderValue(headerValuesList);
            pw.print(headerName + ": " + headerValue + "\r\n");
        }

        pw.print("\r\n");
        pw.flush();
    }

    String buildHeaderValue(List<String> headerValuesList) {
        String header = headerValuesList.get(0);
        for (int i = 1; i < headerValuesList.size(); i++) {
            header += ";" + headerValuesList.get(i);
        }
        return header;
    }

    void printBody(HttpRequest.BodyPublisher bodyPublisher, PrintWriter pw) throws IOException {
        try (var bodyInputStream = bodyPublisher.body;
             var bufferIn = new BufferedInputStream(bodyInputStream)) {
            byte[] buffer = new byte[1024];
            for (int read; (read = bufferIn.read(buffer, 0, buffer.length)) != -1;) {
                String s = new String(buffer, 0, read);
                pw.write(s);
                pw.flush();
            }
        }
    }

    ByteArrayOutputStream getBaosBody(HttpRequest.BodyPublisher bodyPublisher) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = (ByteArrayInputStream) bodyPublisher.body;

        byte[] data = new byte[1024];
        for (int nRead; (nRead = bais.read(data, 0, data.length)) != -1;) {
            baos.write(data, 0, nRead);
            baos.flush();
        }

        return baos;
    }

    public static class Builder {
        HttpClient httpClient;

        Builder() {
            httpClient = new HttpClient();
        }

        public HttpClient build() {
            return httpClient;
        }
    }
}
