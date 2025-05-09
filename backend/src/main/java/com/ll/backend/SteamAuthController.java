package com.ll.backend;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SteamAuthController {
    private final RestTemplate restTemplate;

    private static final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/login";
    private static final String RETURN_URL = "http://localhost:8080/api/auth/steam/callback";

    @GetMapping("/steam")
    public ResponseEntity<Map<String, String>> redirectToSteam() {
        String authUrl = STEAM_OPENID_URL + "?openid.ns=http://specs.openid.net/auth/2.0"
                + "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select"
                + "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select"
                + "&openid.return_to=" + RETURN_URL
                + "&openid.realm=http://localhost:8080"
                + "&openid.mode=checkid_setup";

        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", authUrl);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/steam/callback")
    public ResponseEntity<String> handleSteamCallback(@RequestParam Map<String, String> params) {
        if (!params.containsKey("openid.mode") || !params.get("openid.mode").equals("id_res")) {
            return ResponseEntity.badRequest().body("Invalid OpenID response");
        }

        String validationUrl = "https://steamcommunity.com/openid/login";
        String requestBody = buildValidationRequest(params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(validationUrl, HttpMethod.POST, request, String.class);

        if (response.getBody() != null && response.getBody().contains("is_valid:true")) {
            String steamId = extractSteamId(params.get("openid.claimed_id"));
            System.out.println("[Steam OpenID] User ID: " + steamId); // 스팀 아이디 출력

            return ResponseEntity.ok("Steam OpenID authentication successful!");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Steam OpenID authentication failed.");
        }
    }

    private String buildValidationRequest(Map<String, String> params) {
        params.put("openid.mode", "check_authentication");

        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private String extractSteamId(String claimedId) {
        if (claimedId != null && claimedId.matches(".*/id/\\d+$")) {
            return claimedId.substring(claimedId.lastIndexOf("/") + 1);
        }
        return "Unknown";
    }

}
