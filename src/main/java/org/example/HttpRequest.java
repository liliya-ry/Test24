package org.example;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.Optional;
import java.util.function.Supplier;

public class HttpRequest {
    private String method;
    private URI uri;
    private BodyPublisher bodyPublisher = BodyPublishers.noBody();
    private final HttpHeaders headers = new HttpHeaders();

    protected HttpRequest() {}

    public String method() {
        return method;
    }

    public URI uri() {
        return uri;
    }

    public Optional<BodyPublisher> bodyPublisher() {
        return Optional.of(bodyPublisher);
    }

    public HttpHeaders headers() {
        return headers;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(URI uri) {
        return new Builder(uri);
    }

    public static class Builder {
        HttpRequest httpRequest;

        Builder() {
            httpRequest = new HttpRequest();
        }

        Builder(URI uri) {
            httpRequest = new HttpRequest();
            httpRequest.uri = uri;
            httpRequest.headers.add("Host", uri.getHost());
            addDefaultHeaders();
        }

        public HttpRequest build() {
            if (httpRequest.uri == null) {
                throw new IllegalStateException("URI has not been set");
            }
            return httpRequest;
        }

        private void addDefaultHeaders() {
            httpRequest.headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            httpRequest.headers.add("Accept-Language", "en-US,en;q=0.5");
            httpRequest.headers.add("Accept-Encoding", "gzip, deflate, br");
            httpRequest.headers.add("Connection", "close");
            httpRequest.headers.add("Upgrade-Insecure-Requests", "1");
        }

        public Builder uri(URI uri) {
            httpRequest.uri = uri;
            httpRequest.headers.add("Host", uri.getHost());
            addDefaultHeaders();
            return this;
        }

        public Builder GET() {
            httpRequest.method = "GET";
            return this;
        }

        public Builder DELETE() {
            httpRequest.method = "DELETE";
            return this;
        }

        public Builder POST(BodyPublisher bodyPublisher) {
            return bodyQueryBuilder(bodyPublisher, "POST");
        }

        public Builder PUT(BodyPublisher bodyPublisher) {
            return bodyQueryBuilder(bodyPublisher, "PUT");
        }

        private Builder bodyQueryBuilder(BodyPublisher bodyPublisher, String method) {
            httpRequest.method = method;
            httpRequest.bodyPublisher = bodyPublisher;
            httpRequest.headers.add("Content-Length", String.valueOf(bodyPublisher.contentLength));
            return this;
        }

        public Builder setHeader(String name, String value) {
            httpRequest.headers.replace(name, value);
            return this;
        }

        public Builder header(String name, String value) {
            httpRequest.headers.add(name, value);
            return this;
        }

        public Builder headers(String... headers) {
            if (headers.length % 2 != 0)
                throw new IllegalStateException("Headers length is odd.");

            for (int i = 0; i < headers.length; i += 2)
                httpRequest.headers.add(headers[i], headers[i + 1]);

            return this;
        }
    }

    public static class BodyPublisher {
        InputStream body;
        long contentLength;

        BodyPublisher(InputStream body, long contentLength) {
            this.body = body;
            this.contentLength = contentLength;
        }
    }

    public static class BodyPublishers {
        public static BodyPublisher noBody() {
            return new BodyPublisher(null, 0);
        }

        public static BodyPublisher ofString(String s) {
            var bais = new ByteArrayInputStream(s.getBytes());
            return new BodyPublisher(bais, s.length());
        }

        public static BodyPublisher fromFile(Path path) throws IOException {
            File f = new File(path.toString());
            var fis = new FileInputStream(f);
            long contentLength = f.length();
            return new BodyPublisher(fis, contentLength);
        }

        public static BodyPublisher ofInputStream(Supplier<? extends InputStream> streamSupplier) throws IOException {
            InputStream in = streamSupplier.get();
            return new BodyPublisher(in, in.available());
        }

        public static BodyPublisher ofByteArray(byte[] buf) {
            var bais = new ByteArrayInputStream(buf);
            return new BodyPublisher(bais, buf.length);
        }

        public static BodyPublisher ofByteArray(byte[] buf, int offset, int length) {
            var bais = new ByteArrayInputStream(buf, offset, length);
            return new BodyPublisher(bais, length);
        }
    }
}
