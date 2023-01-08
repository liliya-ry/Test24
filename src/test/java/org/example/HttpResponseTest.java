package org.example;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpResponseTest {
    @Mock
    HttpHeaders httpHeaders;
    @Mock
    InputStream in;
    @Mock
    BufferedReader reader;

    HttpResponse.BodyHandler<String> bodyHandler;
    HttpResponse httpResponse;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        bodyHandler = HttpResponse.BodyHandlers.ofString();
        httpResponse = new HttpResponse<>(in, bodyHandler);
    }

    @Test
    @DisplayName("Check if readHeaders throw IOException for invalid headers")
    void testInvalidHeaders() throws IOException {
        in = new ByteArrayInputStream("cookie 1".getBytes());
        when(reader.readLine()).thenReturn("cookie 1");
        assertThrows(IOException.class, () -> httpResponse.readHeaders(reader));
    }

    @Test
    @DisplayName("Check if readFirstLine throw IOException for invalid line")
    void testInvalidFirstLine() throws IOException {
        when(reader.readLine()).thenReturn("HttpOK");
        assertThrows(IOException.class, () -> httpResponse.readFirstLine(reader));
    }

    @Test
    void testStatusCode() throws IOException {
        when(reader.readLine()).thenReturn("HTTP/1.1 200");
        httpResponse.readFirstLine(reader);
        assertEquals(httpResponse.statusCode(), 200);
    }

    @Test
    void testVersion() throws IOException {
        when(reader.readLine()).thenReturn("HTTP/1.1 200");
        httpResponse.readFirstLine(reader);
        assertEquals(httpResponse.version(), "HTTP/1.1");
    }

    @Test
    void testNegativeContentLength() {
        assertThrows(IndexOutOfBoundsException.class, () -> bodyHandler.readBody(reader, -1));
    }
}