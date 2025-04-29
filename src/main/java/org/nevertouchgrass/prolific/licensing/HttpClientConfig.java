package org.nevertouchgrass.prolific.licensing;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class HttpClientConfig {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ResponseEntity<String> postJson(String url, Map<String, String> payload, String token) throws Exception {
        String requestBody = mapper.writeValueAsString(payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public static boolean isBackendAvailable(String serviceUrl) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(serviceUrl + "/ping", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Backend not reachable: " + e.getMessage());
            return false;
        }
    }

}
