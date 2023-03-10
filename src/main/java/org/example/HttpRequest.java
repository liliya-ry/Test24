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

    public String host() {
        return headers.headers.get("Host").get(0);
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
            checkUri(uri);
            httpRequest.uri = uri;
            httpRequest.headers.add("Host", uri.getHost());
            addDefaultHeaders();
        }

        public HttpRequest build() {
            if (httpRequest.uri == null) {
                throw new IllegalStateException("URI has not been set");
            }

            if (httpRequest.method == null) {
                httpRequest.method = "GET";
            }

            return httpRequest;
        }

        private void checkUri(URI uri) {
            String uriStr = uri.toString();
            if (!uriStr.startsWith("http://") && !uriStr.startsWith("https://")) {
                int lastIndex = uriStr.indexOf("://");
                String scheme = lastIndex != -1 ? uriStr.substring(0, lastIndex) : "";
                throw new IllegalStateException("Invalid URI scheme " + scheme);
            }
        }

        private void addDefaultHeaders() {
            httpRequest.headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            httpRequest.headers.add("Accept-Language", "en-US,en;q=0.5");
            httpRequest.headers.add("Accept-Encoding", "gzip, deflate, br");
            httpRequest.headers.add("Connection", "close");
            httpRequest.headers.add("Upgrade-Insecure-Requests", "1");
            httpRequest.headers.add("User-Agent", "Java-http-client/11.0.6");
        }

        public Builder uri(URI uri) {
            checkUri(uri);
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
            if (name.equals("Content-Length")) {
                throw new IllegalStateException("Content-Length header is restricted");
            }

            httpRequest.headers.replace(name, value);
            return this;
        }

        public Builder header(String name, String value) {
            if (name.equals("Content-Length")) {
                throw new IllegalStateException("Content-Length header is restricted");
            }

            httpRequest.headers.add(name, value);
            return this;
        }

        public Builder headers(String... headers) {
            if (headers.length % 2 != 0)
                throw new IllegalStateException("Headers length is odd.");

            for (int i = 0; i < headers.length; i += 2)
                header(headers[i], headers[i + 1]);

            return this;
        }
    }

    public static class BodyPublisher {
        boolean isInputStream;
        InputStream body;
        long contentLength;

        BodyPublisher(InputStream body, long contentLength, boolean isInputStream) {
            this.body = body;
            this.contentLength = contentLength;
            this.isInputStream = isInputStream;
        }

        BodyPublisher(InputStream body, long contentLength) {
            this(body, contentLength, false);
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
            ByteArrayInputStream bais = new ByteArrayInputStream(in.readAllBytes());
            return new BodyPublisher(bais, 0, true);
        }

        public static BodyPublisher ofByteArray(byte[] buf) {
            var bais = new ByteArrayInputStream(buf);
            return new BodyPublisher(bais, buf.length);
        }

        public static BodyPublisher ofByteArray(byte[] buf, int offset, int length) {
            if (offset < 0 || length - offset > buf.length) {
                throw new IndexOutOfBoundsException("invalid indexes");
            }
            var bais = new ByteArrayInputStream(buf, offset, length);
            return new BodyPublisher(bais, length);
        }
    }
}
