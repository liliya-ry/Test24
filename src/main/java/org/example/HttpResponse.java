import java.io.*;
import java.nio.file.Path;

public class HttpResponse<T> {
    private String version;
    private int statusCode;
    private final HttpHeaders headers = new HttpHeaders();
    private final T body;

    HttpResponse(InputStream in, BodyHandler<T> bodyHandler) throws IOException {
        try (var isr = new InputStreamReader(in);
             BufferedReader reader = new BufferedReader(isr)) {
            readFirstLine(reader);
            readHeaders(reader);
            String contentLengthStr = headers.headers.get("Content-Length").get(0);
            long contentLength = Long.parseLong(contentLengthStr);
            this.body = bodyHandler.readBody(reader, contentLength);
        }
    }

    private void readFirstLine(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine();
        String[] firstLineParts = firstLine.split(" ");
        version = firstLineParts[0];
        statusCode = Integer.parseInt(firstLineParts[1]);
    }

    private void readHeaders(BufferedReader reader) throws IOException {
        for (String line = reader.readLine(); line != null && !line.isBlank(); line = reader.readLine()) {
            String[] parts = line.split(": ");
            if (parts.length != 2) {
                throw new IOException("invalid headers");
            }
            String headerName = parts[0];
            String headerValue = parts[1];
            headers.add(headerName, headerValue);
        }
    }

    public int statusCode() {
        return statusCode;
    }

    public String version() {
        return version;
    }

    public T body() {
        return body;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public static class BodyHandlers {
        public static HttpResponse.BodyHandler<String> ofString() {
            return new BodyHandlerOfString();
        }

        public static HttpResponse.BodyHandler<Path> ofFile(Path file) {
            return new BodyHandlerOfFile(file);
        }
    }

    public interface BodyHandler<T> {
        T readBody(BufferedReader br, long contentLength) throws IOException;
    }

    static class BodyHandlerOfString implements BodyHandler<String> {

        @Override
        public String readBody(BufferedReader br, long contentLength) throws IOException {
            StringBuilder sb = new StringBuilder();
            for (long redBytes = 0; br.ready() || redBytes < contentLength; redBytes++) {
                char ch = (char) br.read();
                sb.append(ch);
            }

            return sb.toString();
        }
    }

    static class BodyHandlerOfFile implements BodyHandler<Path> {
        Path path;

        BodyHandlerOfFile(Path path) {
            this.path = path;
        }

        @Override
        public Path readBody(BufferedReader br, long contentLength) throws IOException {
            try (var fileWriter = new FileWriter(path.toString());
                 var pw = new PrintWriter(fileWriter)) {
                for (long redBytes = 0; br.ready() || redBytes < contentLength; redBytes++) {
                    char ch = (char) br.read();
                    pw.print(ch);
                }
            }

            return path;
        }
    }
}
