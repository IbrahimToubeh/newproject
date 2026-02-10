package com.example.auth.client;

import com.example.auth.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ApiClient {

    private final RestTemplate restTemplate;

    public ApiClient() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    public <T> T get(String url, Class<T> responseType) {
        return execute(url, HttpMethod.GET, null, responseType);
    }
    
    public <T> T get(String url, ParameterizedTypeReference<T> responseType) {
        return execute(url, HttpMethod.GET, null, responseType);
    }

    public <T, R> T post(String url, R body, Class<T> responseType) {
        return execute(url, HttpMethod.POST, body, responseType);
    }

    public <T, R> T put(String url, R body, Class<T> responseType) {
        return execute(url, HttpMethod.PUT, body, responseType);
    }

    public <T> T put(String url, Object body, ParameterizedTypeReference<T> responseType) {
        return execute(url, HttpMethod.PUT, body, responseType);
    }

    public <T, R> T patch(String url, R body, Class<T> responseType) {
        return execute(url, HttpMethod.PATCH, body, responseType);
    }

    public void delete(String url) {
        execute(url, HttpMethod.DELETE, null, Void.class);
    }

    private <T> T execute(String url, HttpMethod method, Object body, Class<T> responseType) {
        try {
            HttpEntity<Object> entity = new HttpEntity<>(body, createHeaders());
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("External service error: {} {} - {}", method, url, e.getResponseBodyAsString());
            throw new ExternalServiceException(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Unexpected error calling external service: {} {} - {}", method, url, e.getMessage());
            throw new RuntimeException("External service call failed", e);
        }
    }
    
    private <T> T execute(String url, HttpMethod method, Object body, ParameterizedTypeReference<T> responseType) {
        try {
            HttpEntity<Object> entity = new HttpEntity<>(body, createHeaders());
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("External service error: {} {} - {}", method, url, e.getResponseBodyAsString());
            throw new ExternalServiceException(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Unexpected error calling external service: {} {} - {}", method, url, e.getMessage());
            throw new RuntimeException("External service call failed", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
