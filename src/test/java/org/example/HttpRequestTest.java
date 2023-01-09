package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.util.*;

class HttpRequestTest {
    URI uri;

    HttpRequest.Builder requestBuilder;

    @BeforeEach
    void setUp() throws URISyntaxException {
        uri = new URI("https://postman-echo.com/post");
        requestBuilder = HttpRequest.newBuilder(uri);
    }

    @Test
    @DisplayName("Tests NoBody content length")
    void bodyPublisherNoBody() {
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        String contentLengthStr = request.headers().headers.get("Content-Length").get(0);
        assertEquals("0", contentLengthStr);
    }

    @Test
    @DisplayName("Tests String content length")
    void bodyPublisherOfString() {
        String body = "some string";
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        String contentLengthStr = request.headers().headers.get("Content-Length").get(0);
        assertEquals(String.valueOf(body.length()), contentLengthStr);
    }

    @Test
    @DisplayName("Tests ByteArray content length")
    void bodyPublisherOfByteArray() {
        byte[] body = "some string".getBytes();
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        String contentLengthStr = request.headers().headers.get("Content-Length").get(0);
        assertEquals(String.valueOf(body.length), contentLengthStr);
    }

    @Test
    @DisplayName("Tests ByteArray content length with indexes")
    void bodyPublisherOfByteArray2() {
        byte[] body = "some string".getBytes();
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofByteArray(body, 1, 4))
                .build();
        String contentLengthStr = request.headers().headers.get("Content-Length").get(0);
        assertEquals("4", contentLengthStr);
    }

    @Test
    @DisplayName("Tests if bodyPublisherOfByteArray throw IndexOutOfBounds Exception")
    void bodyPublisherOfByteArray3() {
        byte[] body = "some string".getBytes();
        assertThrows(IndexOutOfBoundsException.class, () -> HttpRequest.BodyPublishers.ofByteArray(body, -1, 4));
    }

    @Test
    @DisplayName("Test ofInputStream content length")
    void bodyPublisherOfInputStream() throws IOException {
        byte[] body = "some string".getBytes();
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(body)))
                .build();
        String contentLengthStr = request.headers().headers.get("Content-Length").get(0);
        assertEquals(String.valueOf(body.length), contentLengthStr);
    }

    @Test
    void testDefaultHeaders() {
        Map<String, String> expectedHeaders = generateExpectedHeaders();
        HttpRequest httpRequest = requestBuilder.build();
        for (Map.Entry<String, String> expectedHeader : expectedHeaders.entrySet()) {
            List<String> headerValue = httpRequest.headers().headers.get(expectedHeader.getKey());
            assertAll(
                    () -> assertNotNull(headerValue),
                    () -> assertEquals(1, headerValue.size()),
                    () -> assertEquals(headerValue.get(0), expectedHeader.getValue())
            );
        }
    }

    Map<String, String> generateExpectedHeaders() {
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        expectedHeaders.put("Accept-Language", "en-US,en;q=0.5");
        expectedHeaders.put("Accept-Encoding", "gzip, deflate, br");
        expectedHeaders.put("Connection", "close");
        expectedHeaders.put("Upgrade-Insecure-Requests", "1");
        expectedHeaders.put("User-Agent", "Java-http-client/11.0.6");
        expectedHeaders.put("Host", "postman-echo.com");
        return expectedHeaders;
    }

    @Test
    void changeDefaultHeader() {
        HttpRequest request = requestBuilder.setHeader("Connection", "keep-alive").build();
        String connectionHeader = request.headers().headers.get("Connection").get(0);
        assertEquals("keep-alive", connectionHeader);
    }

    @Test
    void testMethods() {
        HttpRequest request = requestBuilder.GET().build();
        assertEquals("GET", request.method());

        request = requestBuilder.POST(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals("POST", request.method());

        request = requestBuilder.PUT(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals("PUT", request.method());

        request = requestBuilder.DELETE().build();
        assertEquals("DELETE", request.method());
    }

    @Test
    void testDefaultMethod() {
       HttpRequest request = requestBuilder.build();
       assertEquals("GET", request.method());
    }

    @Test
    void testUri() {
        HttpRequest request = requestBuilder.GET().build();
        assertEquals(uri, request.uri());
    }

    @Test
    void testAddingHost() {
        HttpRequest request = requestBuilder.GET().build();
        assertEquals(uri.getHost(), request.host());
    }

    @Test
    void testInvalidUri1() throws URISyntaxException {
        uri = new URI("abc");
        assertThrows(IllegalStateException.class, () -> HttpRequest.newBuilder(uri));
    }

    @Test
    void testInvalidUri2() throws URISyntaxException {
        uri = new URI("abc");
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        assertThrows(IllegalStateException.class, () -> builder.uri(uri));
    }

    @Test
    void addContentLengthHeader() {
        assertThrows(IllegalStateException.class, () -> requestBuilder.header("Content-Length", "3"));
        assertThrows(IllegalStateException.class, () -> requestBuilder.setHeader("Content-Length", "3"));
    }

    @Test
    @DisplayName("Odd number of values")
    void testHeaders() {
        assertThrows(IllegalStateException.class, () -> requestBuilder.headers("header1", "value1", "header2"));
    }

    @Test
    void noUriBuild() {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void bodyPubisher() {
        HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpRequest.BodyPublisher bodyPublisher = request.bodyPublisher().get();
        assertNull(bodyPublisher.body);
    }
}