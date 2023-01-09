package org.example;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class HttpClientTest {
    HttpRequest.Builder requestBuilder;
    HttpClient client;

//    @Mock
//    Socket socket;

    @BeforeEach
    void setUp() throws URISyntaxException {
        //socket = mock(Socket.class);
        client = HttpClient.newBuilder().build();
        String body = "some body";
        URI uri = new URI("https://postman-echo.com/post");
        requestBuilder = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(body));
    }

//    @Test
//    void send() throws Exception {
//        whenNew(Socket.class).withArguments(Mockito.anyString(), Mockito.anyInt()).thenReturn(socket);
//        when(socket.getOutputStream()).thenReturn(System.out);
//        client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
//    }

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
}