package com.ivanov.pinto_admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/push-messages")
public class PushMessageController {

    private final String API_TOKEN;
    private final String apiEndpoint;
    private final JsonMapper mapper;

    public PushMessageController(@Value("${api.token}") String apiToken, @Value("${api.endpoint}") String apiEndpoint) {
        API_TOKEN = apiToken;
        this.apiEndpoint = apiEndpoint;
        this.mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    }

    @SneakyThrows
    @GetMapping
    public String list(Model model, @RequestParam(value = "category", required = false) String category) throws IOException {
        String url = apiEndpoint + "push-messages";
        if (category != null && !category.isBlank()) {
            url += "?category=" + category;
        }
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", API_TOKEN);
        ApiResponse response = execute(httpGet);

        List<PushMessageDTO> messages = new ArrayList<>();
        JsonNode root = mapper.readTree(response.body());
        for (JsonNode node : root) {
            messages.add(mapper.readValue(node.toString(), PushMessageDTO.class));
        }
        messages.sort(Comparator
                .comparingInt((PushMessageDTO m) -> PushStage.valueOf(m.getStage()).ordinal())
                .thenComparingInt(m -> m.getGender() == null ? -1 : PushGender.valueOf(m.getGender()).ordinal())
                .thenComparingInt(m -> m.getStyle() == null ? -1 : PushStyle.valueOf(m.getStyle()).ordinal()));

        model.addAttribute("messages", messages);
        model.addAttribute("categories", PushCategory.values());
        model.addAttribute("stages", PushStage.values());
        model.addAttribute("genders", PushGender.values());
        model.addAttribute("styles", PushStyle.values());
        model.addAttribute("selectedCategory", category);
        return "push-messages";
    }

    @SneakyThrows
    @PostMapping("/create")
    public String create(@RequestParam Map<String, String> params) throws IOException {
        PushMessageDTO dto = buildDto(params);
        HttpPost httpPost = new HttpPost(apiEndpoint + "push-messages");
        httpPost.setHeader("Authorization", API_TOKEN);
        httpPost.setEntity(new StringEntity(mapper.writeValueAsString(dto), ContentType.APPLICATION_JSON));
        ApiResponse response = execute(httpPost);
        return redirectTo(params.get("redirectCategory"), !isSuccess(response.status()));
    }

    @SneakyThrows
    @PostMapping("/{id}/update")
    public String update(@PathVariable Integer id, @RequestParam Map<String, String> params) throws IOException {
        PushMessageDTO dto = buildDto(params);
        HttpPut httpPut = new HttpPut(apiEndpoint + "push-messages/" + id);
        httpPut.setHeader("Authorization", API_TOKEN);
        httpPut.setEntity(new StringEntity(mapper.writeValueAsString(dto), ContentType.APPLICATION_JSON));
        ApiResponse response = execute(httpPut);
        return redirectTo(params.get("redirectCategory"), !isSuccess(response.status()));
    }

    @SneakyThrows
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, @RequestParam(value = "redirectCategory", required = false) String redirectCategory) throws IOException {
        HttpDelete httpDelete = new HttpDelete(apiEndpoint + "push-messages/" + id);
        httpDelete.setHeader("Authorization", API_TOKEN);
        execute(httpDelete);
        return redirectTo(redirectCategory, false);
    }

    private PushMessageDTO buildDto(Map<String, String> params) {
        PushMessageDTO dto = new PushMessageDTO();
        dto.setCategory(params.get("category"));
        dto.setStage(params.get("stage"));
        dto.setGender(emptyToNull(params.get("gender")));
        dto.setStyle(emptyToNull(params.get("style")));
        dto.setText(params.get("text"));
        dto.setMedia(emptyToNull(params.get("media")));
        dto.setButtonText(emptyToNull(params.get("buttonText")));
        dto.setButtonPayload(emptyToNull(params.get("buttonPayload")));
        dto.setEnabled("on".equals(params.get("enabled")));
        return dto;
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private boolean isSuccess(int status) {
        return status >= 200 && status < 300;
    }

    private String redirectTo(String redirectCategory, boolean error) {
        List<String> queryParams = new ArrayList<>();
        if (redirectCategory != null && !redirectCategory.isBlank()) {
            queryParams.add("category=" + redirectCategory);
        }
        if (error) {
            queryParams.add("pmError=1");
        }
        if (queryParams.isEmpty()) {
            return "redirect:/push-messages";
        }
        return "redirect:/push-messages?" + String.join("&", queryParams);
    }

    private record ApiResponse(int status, String body) {
    }

    private ApiResponse execute(HttpUriRequest request) throws IOException {
        try {
            SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(
                    SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                    NoopHostnameVerifier.INSTANCE);
            try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(scsf).build()) {
                try (CloseableHttpResponse response = httpclient.execute(request)) {
                    int status = response.getStatusLine().getStatusCode();
                    String body = "";
                    if (response.getEntity() != null) {
                        body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    }
                    return new ApiResponse(status, body);
                }
            }
        } catch (java.security.GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
