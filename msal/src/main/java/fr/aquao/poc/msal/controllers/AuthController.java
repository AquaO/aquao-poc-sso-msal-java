package fr.aquao.poc.msal.controllers;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import fr.aquao.poc.msal.models.ApiResponse;
import fr.aquao.poc.msal.models.Personne;

@Controller
public class AuthController {

    @Value("${APP_CLIENT_ID}")
    private String clientId;

    @Value("${APP_TENANT_ID}")
    private String tenantId;

    @Value("${AQUAO_ENDPOINT_BASE}")
    private String endpointBase;

    private String sessionId;
    private String msalReq;
    private String accessToken;
    private String userName;
    private String userFirstName;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("clientId", clientId);
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("endpointBase", endpointBase);

        if (sessionId != null) {
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("msalReq", msalReq);
            model.addAttribute("accessToken", accessToken);

            if (userName != null && userFirstName != null) {
                model.addAttribute("userName", userName);
                model.addAttribute("userFirstName", userFirstName);
                userName = null;
                userFirstName = null;
            }
        } else {
            model.addAttribute("isAuthenticated", false);
        }

        return "index";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<String> login(@RequestBody String requestBody) {
        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("redirect", "follow");
        headers.set("referrerPolicy", "no-referrer"); 
        headers.set("cache", "no-cache");
        headers.set("cors", "no-cors");
        headers.set("credentials", "include");

        HttpEntity<String> entity = new HttpEntity<String>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpointBase + "/api/security/authentication-support/msal/logas", entity, String.class);
        
        String setCookieHeader = response.getHeaders().getFirst("Set-Cookie");
        if (setCookieHeader != null) {
            Pattern pattern = Pattern.compile("SESSION=(\\w+)");
            Matcher matcher = pattern.matcher(setCookieHeader);

            if (matcher.find()) {
                System.out.println("!! New session ID found " + matcher.group(1));
                sessionId = matcher.group(1);
                msalReq = requestBody;
                int tokenIndex = msalReq.indexOf("token");
                if (tokenIndex != -1) {
                    accessToken = msalReq.substring(tokenIndex + 8, msalReq.indexOf("\"", tokenIndex + 8));
                }
                System.out.println("!! Access token found " + accessToken);
            }
        }

        return response;
    }

    @PostMapping("/logout")
    public String logout() {
        if (sessionId != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Cookie", "SESSION=" + sessionId);
    
            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(endpointBase + "/api/logout", HttpMethod.GET, entity, String.class);
            sessionId = null;
        }
        
        return "redirect:/";
    }

    @GetMapping("/whoami")
    public String whoAmI() {
        if (sessionId != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Cookie", "SESSION=" + sessionId);
    
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(endpointBase + "/api/whoami", HttpMethod.GET, entity, String.class);
    
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ApiResponse apiResponse = objectMapper.readValue(response.getBody(), ApiResponse.class);

                Personne personne = apiResponse.getPerson();
                
                if (personne != null) {
                    userName = personne.getNom();
                    userFirstName = personne.getPrenom();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/";
    }
}