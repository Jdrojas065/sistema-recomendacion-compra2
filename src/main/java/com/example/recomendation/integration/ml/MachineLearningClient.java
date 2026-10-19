package com.example.recomendation.integration.ml;

import com.example.recomendation.integration.ml.dto.MLPredictionRequest;
import com.example.recomendation.integration.ml.dto.MLPredictionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente HTTP hacia el servicio de Machine Learning (Python/FastAPI).
 *
 * IMPORTANTE: si el servicio Python falla por cualquier motivo (timeout,
 * connection refused, HTTP 500, respuesta inválida), este cliente NUNCA
 * propaga la excepción hacia arriba. Registra el error y devuelve null;
 * quien llama (PredictionServiceImpl) decide usar el método tradicional
 * como fallback.
 */
@Component
@Slf4j
public class MachineLearningClient {

    private final RestClient restClient;

    public MachineLearningClient(@Value("${ml.service.url}") String mlServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(clientRequestFactory())
                .build();
    }

    private org.springframework.http.client.ClientHttpRequestFactory clientRequestFactory() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 3 segundos
        factory.setReadTimeout(5000);    // 5 segundos
        return factory;
    }

    public MLPredictionResponse predict(MLPredictionRequest request) {
        try {
            return restClient.post()
                    .uri("/predict")
                    .body(request)
                    .retrieve()
                    .body(MLPredictionResponse.class);
        } catch (RestClientException ex) {
            log.warn("Servicio ML no disponible ({}), se usará el método tradicional como fallback.",
                    ex.getMessage());
            return null;
        } catch (Exception ex) {
            log.warn("Error inesperado consultando el servicio ML, se usará el método tradicional: {}",
                    ex.getMessage());
            return null;
        }
    }
}