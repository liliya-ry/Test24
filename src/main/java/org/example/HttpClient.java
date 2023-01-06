import java.io.*;
import java.net.*;
import java.util.*;

public class HttpClient {
    private static final int HTTP_PORT = 80;
    private String followRedirects;

    public static Builder newBuilder() {
        return new Builder();
    }

    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
        String host = request.uri().getHost();
        try (Socket socket = new Socket(host, HTTP_PORT);
             PrintWriter pw = new PrintWriter(socket.getOutputStream());
             InputStream in = socket.getInputStream()) {

            sendRequest(request, pw);
            return new HttpResponse<>(in, responseBodyHandler);
        }
    }

    private void sendRequest(HttpRequest request, PrintWriter pw) throws IOException {
        URI uri = request.uri();
        String method = request.method();
        pw.print(method + " " + uri.getPath() + " HTTP/1.1\r\n");

        HttpHeaders headers = request.headers();
        printHeaders(headers, pw);

        HttpRequest.BodyPublisher bodyPublisher = request.bodyPublisher().get();
        if ((method.equals("POST") || method.equals("PUT")) && bodyPublisher.body != null)
            printBody(bodyPublisher, pw);
    }

    private void printHeaders(HttpHeaders headers, PrintWriter pw) {
        for (Map.Entry<String, List<String>> headerEntry : headers.headers.entrySet()) {
            String headerName = headerEntry.getKey();
            List<String> headerValuesList = headerEntry.getValue();
            String headerValue = buildHeaderValue(headerValuesList);
            pw.print(headerName + ": " + headerValue + "\r\n");
        }

        pw.print("\r\n");
        pw.flush();
    }

    private String buildHeaderValue(List<String> headerValuesList) {
        String header = headerValuesList.get(0);
        for (int i = 1; i < headerValuesList.size(); i++) {
            header += ";" + headerValuesList.get(i);
        }
        return header;
    }

    private void printBody(HttpRequest.BodyPublisher bodyPublisher, PrintWriter pw) throws IOException {
        try (var bodyInputStream = bodyPublisher.body;
             var bufferIn = new BufferedInputStream(bodyInputStream)) {
            byte[] buffer = new byte[4 * 1024];
            for (int read; (read = bufferIn.read(buffer, 0, buffer.length)) != -1;) {
                String s = new String(buffer, 0, read);
                pw.write(s);
                pw.flush();
            }
        }
        pw.print("\r\n\r\n");
        pw.flush();
    }

    public static class Builder {
        HttpClient httpClient;

        Builder() {
            httpClient = new HttpClient();
        }

        public HttpClient build() {
            return httpClient;
        }

        public HttpClient.Builder followRedirects(HttpClient.Redirect policy) {
            httpClient.followRedirects = String.valueOf(policy);
            return this;
        }
    }

    public enum Redirect {ALWAYS, NEVER, NORMAL}
}
