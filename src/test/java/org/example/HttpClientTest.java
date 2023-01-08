package org.example;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpClientTest {
    HttpRequest.Builder requestBuilder;
    HttpClient client;

    @BeforeEach
    void setUp() throws URISyntaxException {
       client = HttpClient.newBuilder().build();
       requestBuilder = HttpRequest.newBuilder().uri(new URI("https://postman-echo.com/post"));
    }

    @Test
    void testBuildHeaderValue() {
        List<String> headerValuesList = List.of("image/png", "text/html", "image/jpeg");
        String resultHeaderValue = client.buildHeaderValue(headerValuesList);
        assertEquals("image/png;text/html;image/jpeg", resultHeaderValue);
    }
}