package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

class HttpResponseTest {
    HttpResponse<String> validResponse;

    @BeforeEach
    void setUp() throws IOException {
        String validResponseStr = """
                HTTP/1.1 200 OK
                Content-Type: text/html; charset=utf8
                Content-Length: 9
                
                some body
                """;
        InputStream in = new ByteArrayInputStream(validResponseStr.getBytes());
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
        validResponse = new HttpResponse<>(in, bodyHandler);
    }

    @Test
    void invalidHeaders() {
        String responseStr = """
                HTTP/1.1 200 OK
                Content-Type text/html; charset=utf8
                
                some body
                """;
        InputStream in = new ByteArrayInputStream(responseStr.getBytes());
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
        assertThrows(IOException.class, () -> new HttpResponse<>(in, bodyHandler), "Invalid headers");
    }

    @Test
    void invalidFirstLine() {
        String responseStr = """
                HTTP/1.1 200 OK adakjsd
                Content-Type: text/html; charset=utf8
                
                some body
                """;
        InputStream in = new ByteArrayInputStream(responseStr.getBytes());
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
        assertThrows(IOException.class, () -> new HttpResponse<>(in, bodyHandler), "Invalid first line");
    }

    @Test
    void invalidProtocol() {
        String responseStr = """
                HTTP 200OK
                Content-Type: text/html; charset=utf8
                
                some body
                """;
        InputStream in = new ByteArrayInputStream(responseStr.getBytes());
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
        assertThrows(IOException.class, () -> new HttpResponse<>(in, bodyHandler), "Invalid protocol");
    }

    @Test
    void invalidStatusCode() {
        String responseStr = """
                HTTP/1.1 200OK
                Content-Type: text/html; charset=utf8
                
                some body
                """;
        InputStream in = new ByteArrayInputStream(responseStr.getBytes());
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
        assertThrows(IOException.class, () -> new HttpResponse<>(in, bodyHandler), "Invalid status code");
    }

    @Test
    void testStatusCode() {
        assertEquals(200, validResponse.statusCode());
    }

    @Test
    void testVersion() {
        assertEquals("HTTP/1.1", validResponse.version());
    }

    @Test
    void testHeaders() {
        Map<String, String> expectedHeaders = Map.of("Content-Type", "text/html; charset=utf8", "Content-Length", "9");
        for (Map.Entry<String, String> expectedHeader : expectedHeaders.entrySet()) {
            List<String> headerValue = validResponse.headers().headers.get(expectedHeader.getKey());
            assertAll(
                    () -> assertNotNull(headerValue),
                    () -> assertEquals(1, headerValue.size()),
                    () -> assertEquals(headerValue.get(0), expectedHeader.getValue())
            );
        }
    }

    @Test
    void testStringBody() {
        assertEquals("some body", validResponse.body());
    }

    HttpResponse<Path> getValidFileResponse(Path filePath) throws IOException {
        String validResponseStr = """
                HTTP/1.1 200 OK
                Content-Type: text/html; charset=utf8
                Content-Length: 9
                
                some body
                """;
        InputStream in = new ByteArrayInputStream(validResponseStr.getBytes());
        HttpResponse.BodyHandler<Path> bodyHandler = HttpResponse.BodyHandlers.ofFile(filePath);
        return new HttpResponse<>(in, bodyHandler);
    }

    @Test
    void testPathBody() throws IOException {
        String filePathStr = "file.txt";
        Path filePath = Path.of(filePathStr);
        HttpResponse<Path> validFileResponse = getValidFileResponse(filePath);
        assertEquals(filePath, validFileResponse.body());
    }
}