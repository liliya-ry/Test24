package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpHeadersTest {
    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
    }

    @Test
    void add() {
        String cookieStr1 = "cookie1=value1";
        String cookieStr2 = "cookie2=value2";

        List<String> result = new ArrayList<>();
        result.add(cookieStr1);
        result.add(cookieStr2);

        httpHeaders.add("Cookie", cookieStr1);
        httpHeaders.add("Cookie", cookieStr2);
        assertEquals(result, httpHeaders.headers.get("Cookie"));
    }

    @Test
    void replace() {
        String cookieStr1 = "cookie1=value1";
        String cookieStr2 = "cookie2=value2";

        List<String> result = new ArrayList<>();
        result.add(cookieStr2);

        httpHeaders.replace("Cookie", cookieStr1);
        httpHeaders.replace("Cookie", cookieStr2);
        assertEquals(result, httpHeaders.headers.get("Cookie"));
    }
}