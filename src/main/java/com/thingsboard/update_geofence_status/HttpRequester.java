package com.thingsboard.update_geofence_status;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
public class HttpRequester {
    private final static String CONTENT_TYPE = "Content-Type";
    private final static String X_AUTHORIZATION = "X-Authorization";
    private final static String BEARER = "Bearer %s";
    private final static String JSON = "application/json";
    private final HttpClient HTTP = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public HttpResponse<String> sendGetRequest(String url, String jwtToken) {
        url = url.replace(" ", "%20");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(CONTENT_TYPE, JSON)
                    .header(X_AUTHORIZATION, String.format(BEARER, jwtToken))
                    .GET()
                    .build();

            return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.error("Failed to send GET request to {}", url, e);
            throw new RuntimeException("Failed to send request");
        }
    }

    public HttpResponse<String> sendPostRequest(String url, String jwtToken, String body) {
        url = url.replace(" ", "%20");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(CONTENT_TYPE, JSON)
                    .header(X_AUTHORIZATION, String.format(BEARER, jwtToken))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.error("Failed to send POST request to {}", url, e);
            throw new RuntimeException("Failed to send request");
        }
    }
    public HttpResponse<String> sendPostRequest(String url, String body) {
        url = url.replace(" ", "%20");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(CONTENT_TYPE, JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.error("Failed to send POST request to {}", url, e);
            throw new RuntimeException("Failed to send request");
        }
    }

    public HttpResponse<String> sendDeleteRequest(String url, String jwtToken) {
        url = url.replace(" ", "%20");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(CONTENT_TYPE, JSON)
                    .header(X_AUTHORIZATION, String.format(BEARER, jwtToken))
                    .DELETE()
                    .build();
            return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.error("Failed to send DELETE request {}", url, e);
            throw new RuntimeException("Failed to send request");
        }
    }
}
