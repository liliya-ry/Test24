package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpHeadersTest {
    private HttpHeaders httpHeaders;
    private  List<String> expectedHeaderValues;
    private String cookieStr1;
    private String cookieStr2;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        cookieStr1 = "cookie1=value1";
        cookieStr2 = "cookie2=value2";
        expectedHeaderValues = new ArrayList<>();
    }

    @Test
    void add() {
        expectedHeaderValues.add(cookieStr1);
        expectedHeaderValues.add(cookieStr2);

        httpHeaders.add("Cookie", cookieStr1);
        httpHeaders.add("Cookie", cookieStr2);
        assertEquals(expectedHeaderValues, httpHeaders.headers.get("Cookie"));
    }

    @Test
    void replace() {
        expectedHeaderValues.add(cookieStr2);

        httpHeaders.replace("Cookie", cookieStr1);
        httpHeaders.replace("Cookie", cookieStr2);
        assertEquals(expectedHeaderValues, httpHeaders.headers.get("Cookie"));
    }
}