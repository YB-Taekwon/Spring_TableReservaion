package com.ian.tablereservation.store.application;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoGeocodingApiService {
    private final RestTemplate restTemplate;


    @Value("${kakao.api.key}")
    private String apiKey;

    @Value("${kakao.api.url}")
    private String apiUrl;


    public LatLng getCoordinates(String address) {
        log.info("apiKey={}, apiUrl={}", apiKey, apiUrl);

        URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("query", address)
                .build()
                .encode()
                .toUri();

        log.info(String.valueOf(uri));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<KakaoResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                KakaoResponse.class
        );

        log.info(response.getBody().toString());

        List<KakaoDocument> documents = response.getBody().getDocuments();
        if (documents.isEmpty()) {
            throw new RuntimeException("주소로 좌표를 찾을 수 없습니다.");
        }

        KakaoDocument doc = documents.get(0);

        // 가장 신뢰도 높은 좌표 추출: 우선순위 - 도로명 > 지번 > 루트
        String x = firstNonNull(doc.getRoad_address() != null ? doc.getRoad_address().getX() : null,
                doc.getAddress() != null ? doc.getAddress().getX() : null,
                doc.getX());

        String y = firstNonNull(doc.getRoad_address() != null ? doc.getRoad_address().getY() : null,
                doc.getAddress() != null ? doc.getAddress().getY() : null,
                doc.getY());

        log.info("최종 좌표: x={}, y={}", x, y);

        if (x == null || y == null) {
            throw new RuntimeException("좌표 정보가 없습니다.");
        }

        return new LatLng(Double.parseDouble(y), Double.parseDouble(x));
    }

    private String firstNonNull(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }


    @Data
    public static class LatLng {
        private final Double lat;
        private final Double lng;
    }

    @Data
    public static class KakaoResponse {
        private List<KakaoDocument> documents;
    }

    @Data
    public static class KakaoDocument {
        private String address_name;
        private String x;
        private String y;
        private Address address;
        private RoadAddress road_address;

        @Data
        public static class Address {
            private String address_name;
            private String x;
            private String y;
        }

        @Data
        public static class RoadAddress {
            private String address_name;
            private String x;
            private String y;
        }
    }
}
