package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://postman-echo.com/post"))
                .headers("Content-Type", "image/x-png")
                .POST(HttpRequest.BodyPublishers.fromFile(
                        Paths.get("src/Slide15.png")))
                .build();

        HttpResponse<Path> response = HttpClient.newBuilder().build()
                .send(request, HttpResponse.BodyHandlers.ofFile(Paths.get("src/result.png")));
                       // .send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(request.headers());
        System.out.println(response.headers());
        System.out.println(response.body());
        System.out.println(response.statusCode());
    }
}
