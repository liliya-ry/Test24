package org.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class HttpClientTest {
    URI uri;
    String bodyStr;
    HttpRequest.Builder requestBuilder;
    HttpClient client;

    @BeforeEach
    void setUp() throws URISyntaxException {
        client = HttpClient.newBuilder().build();
        bodyStr = "some body";
        uri = new URI("https://postman-echo.com/post");
        requestBuilder = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(bodyStr));
    }

    @Test
    void send() throws Exception {
        HttpRequest request = requestBuilder.build();
        String host = request.host();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String validResponseStr = """
                HTTP/1.1 200 OK
                Content-Type: text/html; charset=utf8
                Content-Length: 9
                
                some body
                """;
        InputStream in = new ByteArrayInputStream(validResponseStr.getBytes());

        Socket mockedSocket = mock(Socket.class);
        when(mockedSocket.getInputStream()).thenReturn(in);
        when(mockedSocket.getOutputStream()).thenReturn(out);

        HttpClient mockedClient = spy(HttpClient.class);
        when(mockedClient.getSocket(host)).thenReturn(mockedSocket);

        assertNotNull(mockedClient.send(request, HttpResponse.BodyHandlers.ofString()));
    }

    String getHeadersStr() {
        StringWriter out = new StringWriter();
        PrintWriter printWriter = new PrintWriter(out);
        HttpRequest validRequest = requestBuilder.build();
        client.printHeaders(validRequest.headers(), printWriter);
        return out.toString();
    }

    String[][] getHeaderPairs() {
        String headersStr = getHeadersStr();
        String[] headerLines = headersStr.split("\r\n");
        String[][] headerPairs = new String[headerLines.length][];

        for (int i = 0; i < headerLines.length; i++) {
            headerPairs[i] = headerLines[i].split(": ");
        }

        return headerPairs;
    }

    @Test
    void printValidHeaders() {
        String[][] headerPairs = getHeaderPairs();
        for (String[] headerPair : headerPairs) {
            assertEquals(2, headerPair.length);
        }
    }

    @Test
    void testDefaultHeaders() {
        Map<String, String> expectedHeaders = generateExpectedHeaders();
        String headersStr = getHeadersStr();
        String[] headerLines = headersStr.split("\r\n");
        for (String headerLine : headerLines) {
            String[] headerPair = headerLine.split(": ");
            String expectedHeaderValue = expectedHeaders.get(headerPair[0]);
            assertAll(
                    () -> assertEquals(2, headerPair.length),
                    () -> assertNotNull(expectedHeaderValue),
                    () -> assertEquals(expectedHeaderValue, headerPair[1])
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
        expectedHeaders.put("Content-Length", "9");
        return expectedHeaders;
    }

    @Test
    void changeDefaultHeader() {
        requestBuilder.setHeader("Connection", "keep-alive");
        String[][] headerPairs = getHeaderPairs();
        Map<String, String> actualHeaders = Arrays.stream(headerPairs).collect(Collectors.toMap(m -> m[0], m -> m[1]));
        String connectionHeaderValue = actualHeaders.get("Connection");
        assertEquals("keep-alive", connectionHeaderValue);
    }

    @Test
    void printStatus() {
        StringWriter out = new StringWriter();
        PrintWriter printWriter = new PrintWriter(out);
        HttpRequest validRequest = requestBuilder.build();

        client.printFirstLine(validRequest, printWriter);
        String firstLine = out.toString();
        String[] firstLineParts = firstLine.split(" ");

        assertEquals(validRequest.method(), firstLineParts[0]);
        String path = validRequest.uri().getPath();
        assertEquals(path, firstLineParts[1]);
        assertEquals("HTTP/1.1\r\n", firstLineParts[2]);
    }

    @Test
    void buildHeaderValue() {
        List<String> headerValues = List.of("value1", "value2", "value3");
        String headerValue = client.buildHeaderValue(headerValues);
        assertEquals("value1;value2;value3", headerValue);
    }

    @Test
    void printBodyContentLength() throws IOException {
        StringWriter out = new StringWriter();
        PrintWriter printWriter = new PrintWriter(out);
        HttpRequest validRequest = requestBuilder.build();

        HttpRequest.BodyPublisher bodyPublisher = validRequest.bodyPublisher().get();
        client.printBody(bodyPublisher, printWriter);

        String body = out.toString();
        String expectedContentLength = validRequest.headers().headers.get("Content-Length").get(0);
        String actualContentLength = String.valueOf(body.length());
        assertEquals(expectedContentLength, actualContentLength);
    }

    @Test
    void printBody() throws IOException {
        StringWriter out = new StringWriter();
        PrintWriter printWriter = new PrintWriter(out);
        HttpRequest validRequest = requestBuilder.build();
        HttpRequest.BodyPublisher bodyPublisher = validRequest.bodyPublisher().get();
        client.printBody(bodyPublisher, printWriter);

        byte[] expectedBody = bodyStr.getBytes();
        byte[] actualBody = out.toString().getBytes();
        assertArrayEquals(expectedBody, actualBody);
    }

    @Test
    void getBaosBody() throws IOException {
        byte[] body = bodyStr.getBytes();
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(body));
        ByteArrayOutputStream baos = client.getBaosBody(bodyPublisher);
        assertArrayEquals(baos.toByteArray(), body);
    }

    @Test
    void inputStreamOutput() throws IOException {
        byte[] body = bodyStr.getBytes();
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(body));
        HttpRequest validRequest = HttpRequest.newBuilder(uri).POST(bodyPublisher).build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.sendRequest(validRequest, out);

        String contentLengthHeader = validRequest.headers().headers.get("Content-Length").get(0);
        String actualContentLength = String.valueOf(body.length);
        assertEquals(contentLengthHeader, actualContentLength);
    }

    @Test
    void stringOutput() {
        HttpRequest request = requestBuilder.build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> client.sendRequest(request, out));
    }
}