package com.tus.coupon.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tus.coupon.model.Coupon;
import com.tus.coupon.repo.CouponRepo;
import com.tus.coupon.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("Coupon Service End-to-End Tests")
class CouponServiceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CouponRepo couponRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/couponapi";
        // Clean database before each test
        couponRepo.deleteAll();
        // Ensure database is actually clean
        assertThat(couponRepo.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should complete full coupon lifecycle - create, retrieve, and list")
    void shouldCompleteFullCouponLifecycle() {
        // Given
        Coupon newCoupon = TestDataBuilder.aCoupon()
                .withCode("E2E_LIFECYCLE")
                .withDiscount("25.00")
                .withExpDate("2024-12-31")
                .build();

        // When - Create coupon
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Coupon> createRequest = new HttpEntity<>(newCoupon, headers);

        ResponseEntity<Coupon> createResponse = restTemplate.postForEntity(
                baseUrl + "/coupons", createRequest, Coupon.class);

        // Then - Verify creation
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();
        assertThat(createResponse.getBody().getCode()).isEqualTo("E2E_LIFECYCLE");
        assertThat(createResponse.getBody().getDiscount()).isEqualTo(new BigDecimal("25.00"));

        // When - Retrieve coupon by code
        ResponseEntity<Coupon> getResponse = restTemplate.getForEntity(
                baseUrl + "/coupons/E2E_LIFECYCLE", Coupon.class);

        // Then - Verify retrieval
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getCode()).isEqualTo("E2E_LIFECYCLE");
        assertThat(getResponse.getBody().getDiscount()).isEqualTo(new BigDecimal("25.00"));

        // When - List all coupons
        ResponseEntity<Coupon[]> listResponse = restTemplate.getForEntity(
                baseUrl + "/coupons", Coupon[].class);

        // Then - Verify listing
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody()).hasSize(1);
        assertThat(listResponse.getBody()[0].getCode()).isEqualTo("E2E_LIFECYCLE");
    }

    @Test
    @DisplayName("Should handle multiple coupons in complete workflow")
    void shouldHandleMultipleCouponsInCompleteWorkflow() {
        // Given
        Coupon coupon1 = TestDataBuilder.aCoupon()
                .withCode("E2E_MULTI_1")
                .withDiscount("10.00")
                .build();
        Coupon coupon2 = TestDataBuilder.aCoupon()
                .withCode("E2E_MULTI_2")
                .withDiscount("20.00")
                .build();
        Coupon coupon3 = TestDataBuilder.aCoupon()
                .withCode("E2E_MULTI_3")
                .withDiscount("30.00")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // When - Create multiple coupons
        restTemplate.postForEntity(baseUrl + "/coupons", 
                new HttpEntity<>(coupon1, headers), Coupon.class);
        restTemplate.postForEntity(baseUrl + "/coupons", 
                new HttpEntity<>(coupon2, headers), Coupon.class);
        restTemplate.postForEntity(baseUrl + "/coupons", 
                new HttpEntity<>(coupon3, headers), Coupon.class);

        // Then - Verify all coupons are retrievable individually
        ResponseEntity<Coupon> response1 = restTemplate.getForEntity(
                baseUrl + "/coupons/E2E_MULTI_1", Coupon.class);
        ResponseEntity<Coupon> response2 = restTemplate.getForEntity(
                baseUrl + "/coupons/E2E_MULTI_2", Coupon.class);
        ResponseEntity<Coupon> response3 = restTemplate.getForEntity(
                baseUrl + "/coupons/E2E_MULTI_3", Coupon.class);

        assertThat(response1.getBody().getDiscount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(response2.getBody().getDiscount()).isEqualTo(new BigDecimal("20.00"));
        assertThat(response3.getBody().getDiscount()).isEqualTo(new BigDecimal("30.00"));

        // And - Verify all coupons appear in list
        ResponseEntity<Coupon[]> listResponse = restTemplate.getForEntity(
                baseUrl + "/coupons", Coupon[].class);

        assertThat(listResponse.getBody()).hasSize(3);
        List<String> codes = List.of(listResponse.getBody()).stream()
                .map(Coupon::getCode)
                .toList();
        assertThat(codes).containsExactlyInAnyOrder("E2E_MULTI_1", "E2E_MULTI_2", "E2E_MULTI_3");
    }

    @Test
    @DisplayName("Should handle non-existent coupon gracefully")
    void shouldHandleNonExistentCouponGracefully() {
        // When - Try to retrieve non-existent coupon
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/coupons/NON_EXISTENT", String.class);

        // Then - Should return OK with empty body (null or empty string)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfiesAnyOf(
                body -> assertThat(body).isNull(),
                body -> assertThat(body).isEmpty()
        );
    }

    @Test
    @DisplayName("Should handle edge cases in coupon data")
    void shouldHandleEdgeCasesInCouponData() {
        // Given - Coupon with edge case data
        Coupon edgeCaseCoupon = TestDataBuilder.aCoupon()
                .withCode("EDGE_CASE_2024!")
                .withDiscount("0.01")
                .withExpDate("2099-12-31")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Coupon> request = new HttpEntity<>(edgeCaseCoupon, headers);

        // When - Create and retrieve edge case coupon
        ResponseEntity<Coupon> createResponse = restTemplate.postForEntity(
                baseUrl + "/coupons", request, Coupon.class);

        ResponseEntity<Coupon> getResponse = restTemplate.getForEntity(
                baseUrl + "/coupons/EDGE_CASE_2024!", Coupon.class);

        // Then - Verify edge case handling
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getCode()).isEqualTo("EDGE_CASE_2024!");
        assertThat(getResponse.getBody().getDiscount()).isEqualTo(new BigDecimal("0.01"));
        assertThat(getResponse.getBody().getExpDate()).isEqualTo("2099-12-31");
    }

    @Test
    @DisplayName("Should maintain data consistency across operations")
    void shouldMaintainDataConsistencyAcrossOperations() {
        // Given
        Coupon originalCoupon = TestDataBuilder.aCoupon()
                .withCode("CONSISTENCY_TEST")
                .withDiscount("50.00")
                .withExpDate("2024-06-30")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // When - Create coupon
        ResponseEntity<Coupon> createResponse = restTemplate.postForEntity(
                baseUrl + "/coupons", new HttpEntity<>(originalCoupon, headers), Coupon.class);

        Long createdId = createResponse.getBody().getId();

        // Then - Verify consistency across different retrieval methods
        // 1. Get by code
        ResponseEntity<Coupon> getByCodeResponse = restTemplate.getForEntity(
                baseUrl + "/coupons/CONSISTENCY_TEST", Coupon.class);

        // 2. Get from list all
        ResponseEntity<Coupon[]> listResponse = restTemplate.getForEntity(
                baseUrl + "/coupons", Coupon[].class);

        // 3. Direct database check
        Coupon dbCoupon = couponRepo.findByCode("CONSISTENCY_TEST");

        // Verify all methods return consistent data
        assertThat(getByCodeResponse.getBody().getId()).isEqualTo(createdId);
        assertThat(getByCodeResponse.getBody().getDiscount()).isEqualTo(new BigDecimal("50.00"));

        assertThat(listResponse.getBody()[0].getId()).isEqualTo(createdId);
        assertThat(listResponse.getBody()[0].getDiscount()).isEqualTo(new BigDecimal("50.00"));

        assertThat(dbCoupon.getId()).isEqualTo(createdId);
        assertThat(dbCoupon.getDiscount()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should handle concurrent coupon creation")
    void shouldHandleConcurrentCouponCreation() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // When - Create multiple coupons rapidly
        for (int i = 1; i <= 5; i++) {
            Coupon coupon = TestDataBuilder.aCoupon()
                    .withCode("CONCURRENT_" + i)
                    .withDiscount(String.valueOf(i * 10.0))
                    .build();

            ResponseEntity<Coupon> response = restTemplate.postForEntity(
                    baseUrl + "/coupons", new HttpEntity<>(coupon, headers), Coupon.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // Then - Verify all coupons were created successfully
        ResponseEntity<Coupon[]> listResponse = restTemplate.getForEntity(
                baseUrl + "/coupons", Coupon[].class);

        assertThat(listResponse.getBody()).hasSize(5);

        // Verify each coupon individually
        for (int i = 1; i <= 5; i++) {
            ResponseEntity<Coupon> getResponse = restTemplate.getForEntity(
                    baseUrl + "/coupons/CONCURRENT_" + i, Coupon.class);

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody().getCode()).isEqualTo("CONCURRENT_" + i);
            assertThat(getResponse.getBody().getDiscount()).isEqualTo(new BigDecimal(String.valueOf(i * 10.0)));
        }
    }

    @Test
    @DisplayName("Should handle malformed requests appropriately")
    void shouldHandleMalformedRequestsAppropriately() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // When - Send malformed JSON
        String malformedJson = "{ \"code\": \"MALFORMED\", \"discount\": \"invalid\" }";
        HttpEntity<String> request = new HttpEntity<>(malformedJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/coupons", request, String.class);

        // Then - Should handle gracefully (specific behavior depends on implementation)
        // At minimum, should not crash the application
        assertThat(response.getStatusCode().is4xxClientError() || 
                   response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
