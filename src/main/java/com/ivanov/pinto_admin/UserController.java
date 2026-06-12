package com.ivanov.pinto_admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserController {

    private final String API_TOKEN;
    private final String apiEndpoint;

    public UserController(@Value("${api.token}") String apiToken, @Value("${api.endpoint}") String apiEndpoint) {
        API_TOKEN = apiToken;
        this.apiEndpoint = apiEndpoint;
    }

    @SneakyThrows
    @GetMapping
    public String getUser(Model model, Authentication authentication,
                          @RequestParam(value = "id", required = false) String id,
                          @RequestParam(value = "username", required = false) String username) throws IOException {
        boolean isMain = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MAIN"));
        model.addAttribute("isMainUser", isMain);
        HttpGet httpGet;
        if (id != null && !id.isBlank()) {
            httpGet = new HttpGet(apiEndpoint + "users/get?id=" + id);
        } else {
            if (username.startsWith("@")) {
                username = username.substring(1);
            }
            httpGet = new HttpGet(apiEndpoint + "users/get?username=" + username);
        }

        httpGet.setHeader("Authorization", API_TOKEN);
        SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
                SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                NoopHostnameVerifier.INSTANCE);
        try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(scsf).build()) {
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 404) {
                    return "redirect:/";
                }
                HttpEntity responseEntity = response.getEntity();
                try (InputStream ins = responseEntity.getContent()) {
                    byte[] bytes1 = ins.readAllBytes();
                    String str = new String(bytes1, StandardCharsets.UTF_8);
                    JsonMapper mapper1 = JsonMapper.builder()
                            .addModule(new JavaTimeModule())
                            .build();
                    JsonNode jsonNode = mapper1.readTree(str);
                    UserDTO userDTO = mapper1.readValue(jsonNode.toString(), UserDTO.class);
                    if (userDTO.getBalanceChanges() != null) {
                        userDTO.getBalanceChanges().sort(Comparator.comparing(
                                BalanceChangeDTO::getDateTime,
                                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                    }
                    model.addAttribute("user", userDTO);
                    model.addAttribute("isAdmin", userDTO.getUserType() == UserType.ADMIN);
                }
            }
        }
        return "show-user";
    }
    @SneakyThrows
    @PostMapping("/edit")
    public String editUser(@RequestParam Map<String, String> params) throws IOException {
        HttpPost httpPost = new HttpPost(apiEndpoint + "users/edit");
        httpPost.setHeader("Authorization", API_TOKEN);
        List<BasicNameValuePair> param = new ArrayList<>();
        param.add(new BasicNameValuePair("avatars", params.get("avatars")));
        param.add(new BasicNameValuePair("count", params.get("count")));
        param.add(new BasicNameValuePair("id", params.get("id")));
        param.add(new BasicNameValuePair("bonus", params.get("bonus")));
        httpPost.setEntity(new UrlEncodedFormEntity(param, StandardCharsets.UTF_8));

        SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
                SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                NoopHostnameVerifier.INSTANCE);
        try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(scsf).build()) {
            httpclient.execute(httpPost);
        }
        return "redirect:/users?id=" + params.get("id");
    }


    @SneakyThrows
    @PostMapping("/changestatus")
    public String editUser(@RequestParam(value = "id") Long userId) throws IOException {
        HttpPost httpPost = new HttpPost(apiEndpoint + "users/changestatus");
        httpPost.setHeader("Authorization", API_TOKEN);
        List<BasicNameValuePair> param = new ArrayList<>();
        param.add(new BasicNameValuePair("id", userId.toString()));
        httpPost.setEntity(new UrlEncodedFormEntity(param, StandardCharsets.UTF_8));

        SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
                SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                NoopHostnameVerifier.INSTANCE);
        try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(scsf).build()) {
            httpclient.execute(httpPost);
        }
        return "redirect:/users?id=" + userId;
    }

    @SneakyThrows
    @PostMapping("/remove")
    public String removeUser(@RequestParam("id") String id) throws IOException {
        HttpPost httpPost = new HttpPost(apiEndpoint + "users/remove");
        httpPost.setHeader("Authorization", API_TOKEN);
        List<BasicNameValuePair> param = new ArrayList<>();
        param.add(new BasicNameValuePair("id", id));
        httpPost.setEntity(new UrlEncodedFormEntity(param, StandardCharsets.UTF_8));

        SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
                SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                NoopHostnameVerifier.INSTANCE);
        try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(scsf).build()) {
            httpclient.execute(httpPost);
        }
        return "redirect:/";
    }

}
