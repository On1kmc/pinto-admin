package com.ivanov.pinto_admin;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints used by frontend AJAX. Mirrors the form-based MVC actions
 * but returns JSON so the page doesn't need a full reload.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final String apiEndpoint;
    private final String API_TOKEN;
    private final CloseableHttpClient httpClient;
    private final JsonMapper mapper;

    @SneakyThrows
    public ApiController(
            @Value("${api.endpoint}") String apiEndpoint,
            @Value("${api.token}") String apiToken) {
        this.apiEndpoint = apiEndpoint;
        this.API_TOKEN = apiToken;
        SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
                SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                NoopHostnameVerifier.INSTANCE);
        this.httpClient = HttpClients.custom().setSSLSocketFactory(scsf).build();
        this.mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    }

    /** Returns user data as JSON — used by frontend to refresh stats after edits. */
    @SneakyThrows
    @GetMapping("/users")
    public ResponseEntity<UserDTO> getUser(@RequestParam String id) {
        HttpGet get = new HttpGet(apiEndpoint + "users/get?id=" + id);
        get.setHeader("Authorization", API_TOKEN);
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            if (response.getStatusLine().getStatusCode() == 404) {
                return ResponseEntity.notFound().build();
            }
            String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(mapper.readValue(body, UserDTO.class));
        }
    }

    /** Grant bonuses / photos / avatar slot. */
    @SneakyThrows
    @PostMapping("/users/edit")
    public Map<String, Object> editUser(@RequestParam Map<String, String> params) {
        HttpPost post = new HttpPost(apiEndpoint + "users/edit");
        post.setHeader("Authorization", API_TOKEN);
        post.setEntity(new UrlEncodedFormEntity(List.of(
                new BasicNameValuePair("avatars", params.get("avatars")),
                new BasicNameValuePair("count",   params.get("count")),
                new BasicNameValuePair("id",      params.get("id")),
                new BasicNameValuePair("bonus",   params.get("bonus"))
        ), StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            EntityUtils.consume(response.getEntity());
            return Map.of("ok", true);
        }
    }

    /** Toggle admin / user role. */
    @SneakyThrows
    @PostMapping("/users/changestatus")
    public Map<String, Object> changeStatus(@RequestParam Long id) {
        HttpPost post = new HttpPost(apiEndpoint + "users/changestatus");
        post.setHeader("Authorization", API_TOKEN);
        post.setEntity(new UrlEncodedFormEntity(
                List.of(new BasicNameValuePair("id", id.toString())),
                StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            EntityUtils.consume(response.getEntity());
            return Map.of("ok", true);
        }
    }

    /** Delete user. */
    @SneakyThrows
    @PostMapping("/users/remove")
    public Map<String, Object> removeUser(@RequestParam String id) {
        HttpPost post = new HttpPost(apiEndpoint + "users/remove");
        post.setHeader("Authorization", API_TOKEN);
        post.setEntity(new UrlEncodedFormEntity(
                List.of(new BasicNameValuePair("id", id)),
                StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            EntityUtils.consume(response.getEntity());
            return Map.of("ok", true);
        }
    }

    /** Returns all available bot rights: { "RIGHT_KEY": "Описание", ... } */
    @SneakyThrows
    @GetMapping("/rightslist")
    public ResponseEntity<String> getRightsList() {
        HttpGet get = new HttpGet(apiEndpoint + "users/rightslist");
        get.setHeader("Authorization", API_TOKEN);
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
        }
    }

    /**
     * Returns a page (50 items) of a user's balance-change history.
     * Proxies the backend, which responds with { size, page, totalPages, changes: [...] }.
     * Used by frontend "load more" pagination (page 1 is already embedded in users/get).
     */
    @SneakyThrows
    @GetMapping("/balance-changes")
    public ResponseEntity<String> getBalanceChanges(@RequestParam String id,
                                                    @RequestParam(defaultValue = "1") int page) {
        HttpGet get = new HttpGet(apiEndpoint + "/users/balance-changes?id=" + id + "&page=" + page);
        get.setHeader("Authorization", API_TOKEN);
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
        }
    }

    /**
     * Sets bot rights for a user (full replacement).
     * Body: { "user_id": 123, "rights": ["RIGHT_A", "RIGHT_B"] }
     */
    @SneakyThrows
    @PostMapping("/setrights")
    public Map<String, Object> setRights(@RequestBody String body) {
        HttpPost post = new HttpPost(apiEndpoint + "users/setrights");
        post.setHeader("Authorization", API_TOKEN);
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            EntityUtils.consume(response.getEntity());
            return Map.of("ok", true);
        }
    }
}
