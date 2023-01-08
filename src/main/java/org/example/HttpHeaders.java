package org.example;

import java.util.*;

class HttpHeaders {
    Map<String, List<String>> headers = new HashMap<>();

    public Map<String, List<String>> map() {
        return Collections.unmodifiableMap(headers);
    }

    public String toString() {
        return headers.toString();
    }

    void add(String key, String value) {
        if (headers.containsKey(key)) {
            headers.get(key).add(value);
            return;
        }

        replace(key, value);
    }

    void replace(String key, String value) {
        List<String> list = new ArrayList<>();
        list.add(value);
        headers.put(key, list);
    }
}
