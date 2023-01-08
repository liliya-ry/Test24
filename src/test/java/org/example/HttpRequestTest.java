package org.example;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import java.net.*;


class HttpRequestTest {
    private HttpHeaders httpHeaders;

    private HttpRequest.Builder requestBuilder;

    @BeforeEach
    void setUp() throws URISyntaxException {
        requestBuilder = HttpRequest.newBuilder(new URI("https://postman-echo.com/post"));
    }

    @Test
    @DisplayName("Tests bodyPublishersNoBody content length")
    void bodyPublisherNoBody() {
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        String contentLengthStr = request.headers().headers.get("Content-Length").get(0);
        assertEquals("0", contentLengthStr);
    }

    @Test
    @DisplayName("Tests bodyPublishersOfString content length")
    void bodyPublisherOfString() {
        String s = "some string";
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(s))
                .build();
        String contentLengthStr = request.headers().headers.get("Content-Length").get(0);
        assertEquals(String.valueOf(s.length()), contentLengthStr);
    }

    @Test
    void testDefaultHeaders() {
        HttpHeaders defaultHeaders = new HttpHeaders();
        defaultHeaders.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        defaultHeaders.add("Accept-Language", "en-US,en;q=0.5");
        defaultHeaders.add("Accept-Encoding", "gzip, deflate, br");
        defaultHeaders.add("Connection", "close");
        defaultHeaders.add("Upgrade-Insecure-Requests", "1");
        defaultHeaders.add("Host", "postman-echo.com");

        HttpRequest httpRequest = requestBuilder.build();
        assertEquals(defaultHeaders.toString(), httpRequest.headers().toString());
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
}