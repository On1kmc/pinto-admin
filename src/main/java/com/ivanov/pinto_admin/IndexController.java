package com.ivanov.pinto_admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/")
public class IndexController {

    private final String apiEndpoint;
    private final String API_TOKEN;

    public IndexController(@Value("${api.endpoint}") String apiEndpoint, @Value("${api.token}") String apiToken) {
        this.apiEndpoint = apiEndpoint;
        API_TOKEN = apiToken;
    }

    @SneakyThrows
    @GetMapping
    public String index(Model model, Authentication authentication,
                        @RequestParam(value = "page", required = false) Integer page) throws IOException {
        boolean isMain = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MAIN"));
        model.addAttribute("isMainUser", isMain);
        if (page == null) {
            page = 1;
        }
        HttpGet httpGet = new HttpGet(apiEndpoint + "users/all?page=" + page);
        httpGet.setHeader("Authorization", API_TOKEN);
        SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
                SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                NoopHostnameVerifier.INSTANCE);
        try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(scsf).build()) {
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity responseEntity = response.getEntity();
                try (InputStream ins = responseEntity.getContent()) {
                    byte[] bytes1 = ins.readAllBytes();
                    String str = new String(bytes1, StandardCharsets.UTF_8);
                    JsonMapper mapper1 = JsonMapper.builder()
                            .addModule(new JavaTimeModule())
                            .build();
                    List<UserDTO> list = new ArrayList<>();
                    JsonNode jsonNode = mapper1.readTree(str);
                    int size = jsonNode.get("size").intValue();
                    int payedUsers = jsonNode.get("payed").intValue();
                    int allUsersBalance = jsonNode.get("balance").intValue();
                    int active = jsonNode.get("active").intValue();
                    int notActive = jsonNode.get("inactive").intValue();
                    int avatars = jsonNode.get("avatars").intValue();

                    for (JsonNode node : jsonNode.get("users")) {
                        list.add(mapper1.readValue(node.toString(), UserDTO.class));
                    }


                    model.addAttribute("payed", payedUsers);
                    model.addAttribute("sum", allUsersBalance);
                    model.addAttribute("active", active);
                    model.addAttribute("inactive", notActive);
                    model.addAttribute("usersSize", size);
                    model.addAttribute("page", page);
                    model.addAttribute("users", list);
                    model.addAttribute("avatars", avatars);
                }
            }
        }

        return "index";
    }


    @SneakyThrows
    @GetMapping("/admins")
    public String admins(Model model, Authentication authentication) throws IOException {
        boolean isMain = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MAIN"));
        model.addAttribute("isMainUser", isMain);
        HttpGet httpGet = new HttpGet(apiEndpoint + "users/alladmins");

//        HttpGet httpGet = new HttpGet(apiEndpoint + "users/admins");
        httpGet.setHeader("Authorization", API_TOKEN);
        SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
                SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                NoopHostnameVerifier.INSTANCE);
        try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(scsf).build()) {
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity responseEntity = response.getEntity();
                try (InputStream ins = responseEntity.getContent()) {
                    byte[] bytes1 = ins.readAllBytes();
                    String str = new String(bytes1, StandardCharsets.UTF_8);
                    JsonMapper mapper1 = JsonMapper.builder()
                            .addModule(new JavaTimeModule())
                            .build();
                    List<UserDTO> list = new ArrayList<>();
                    JsonNode jsonNode = mapper1.readTree(str);

                    for (JsonNode node : jsonNode.get("users")) {
                        list.add(mapper1.readValue(node.toString(), UserDTO.class));
                    }
                    model.addAttribute("users", list);
                }
            }
        }

        return "admins";
    }

    private List<UserDTO> getPaginationList(List<UserDTO> list, Integer page) {

        int lastIndex = page * 100 - 1;
        int startIndex = lastIndex - 99;
        if (lastIndex >= list.size()) {
            lastIndex = list.size() - 1;
        }
        List<UserDTO> result = new ArrayList<>();
        for (int i = startIndex; i <= lastIndex; i++) {
            result.add(list.get(i));
        }
        return result;
    }
}
