package com.tus.coupon.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tus.coupon.model.Coupon;
import com.tus.coupon.repo.CouponRepo;
import com.tus.coupon.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Coupon REST Controller Integration Tests")
class CouponRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepo couponRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        couponRepo.deleteAll();
    }

    @Test
    @DisplayName("Should create and retrieve coupon through full stack")
    void shouldCreateAndRetrieveCouponThroughFullStack() throws Exception {
        // Given
        Coupon inputCoupon = TestDataBuilder.aCoupon()
                .withCode("INTEGRATION10")
                .withDiscount("15.00")
                .withExpDate("2024-12-31")
                .build();

        // When - Create coupon
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCoupon)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("INTEGRATION10")))
                .andExpect(jsonPath("$.discount", is(15.00)))
                .andExpect(jsonPath("$.expDate", is("2024-12-31")))
                .andExpect(jsonPath("$.id", notNullValue()));

        // Then - Verify coupon was saved in database
        Coupon savedCoupon = couponRepo.findByCode("INTEGRATION10");
        assertThat(savedCoupon).isNotNull();
        assertThat(savedCoupon.getCode()).isEqualTo("INTEGRATION10");
        assertThat(savedCoupon.getDiscount()).isEqualTo(new BigDecimal("15.00"));

        // And - Retrieve coupon by code
        mockMvc.perform(get("/couponapi/coupons/{code}", "INTEGRATION10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("INTEGRATION10")))
                .andExpect(jsonPath("$.discount", is(15.00)))
                .andExpect(jsonPath("$.expDate", is("2024-12-31")));
    }

    @Test
    @DisplayName("Should handle multiple coupons creation and retrieval")
    void shouldHandleMultipleCouponsCreationAndRetrieval() throws Exception {
        // Given
        Coupon coupon1 = TestDataBuilder.aCoupon()
                .withCode("MULTI1")
                .withDiscount("10.00")
                .build();
        Coupon coupon2 = TestDataBuilder.aCoupon()
                .withCode("MULTI2")
                .withDiscount("20.00")
                .build();

        // When - Create multiple coupons
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coupon1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coupon2)))
                .andExpect(status().isOk());

        // Then - Retrieve all coupons
        mockMvc.perform(get("/couponapi/coupons"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("MULTI1", "MULTI2")))
                .andExpect(jsonPath("$[*].discount", containsInAnyOrder(10.00, 20.00)));

        // And - Verify database state
        assertThat(couponRepo.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return null for non-existent coupon code")
    void shouldReturnNullForNonExistentCouponCode() throws Exception {
        // When & Then
        mockMvc.perform(get("/couponapi/coupons/{code}", "NONEXISTENT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Should handle database persistence correctly")
    void shouldHandleDatabasePersistenceCorrectly() throws Exception {
        // Given
        Coupon inputCoupon = TestDataBuilder.aCoupon()
                .withCode("PERSIST20")
                .withDiscount("25.50")
                .withExpDate("2025-01-15")
                .build();

        // When - Create coupon
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCoupon)))
                .andExpect(status().isOk());

        // Then - Verify direct database access
        Coupon persistedCoupon = couponRepo.findByCode("PERSIST20");
        assertThat(persistedCoupon).isNotNull();
        assertThat(persistedCoupon.getId()).isNotNull();
        assertThat(persistedCoupon.getCode()).isEqualTo("PERSIST20");
        assertThat(persistedCoupon.getDiscount()).isEqualTo(new BigDecimal("25.50"));
        assertThat(persistedCoupon.getExpDate()).isEqualTo("2025-01-15");
    }

    @Test
    @DisplayName("Should handle special characters in coupon codes")
    void shouldHandleSpecialCharactersInCouponCodes() throws Exception {
        // Given
        Coupon inputCoupon = TestDataBuilder.aCoupon()
                .withCode("SPECIAL-CODE_2024!")
                .withDiscount("30.00")
                .build();

        // When - Create coupon with special characters
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCoupon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("SPECIAL-CODE_2024!")));

        // Then - Retrieve coupon with special characters
        mockMvc.perform(get("/couponapi/coupons/{code}", "SPECIAL-CODE_2024!"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("SPECIAL-CODE_2024!")))
                .andExpect(jsonPath("$.discount", is(30.00)));
    }

    @Test
    @DisplayName("Should handle large discount values")
    void shouldHandleLargeDiscountValues() throws Exception {
        // Given
        Coupon inputCoupon = TestDataBuilder.aCoupon()
                .withCode("LARGE999")
                .withDiscount("999.99")
                .build();

        // When - Create coupon with large discount
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCoupon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount", is(999.99)));

        // Then - Verify persistence of large values
        Coupon savedCoupon = couponRepo.findByCode("LARGE999");
        assertThat(savedCoupon.getDiscount()).isEqualTo(new BigDecimal("999.99"));
    }

    @Test
    @DisplayName("Should handle decimal precision correctly")
    void shouldHandleDecimalPrecisionCorrectly() throws Exception {
        // Given
        Coupon inputCoupon = TestDataBuilder.aCoupon()
                .withCode("PRECISION")
                .withDiscount("12.345")
                .build();

        // When - Create coupon with precise decimal
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCoupon)))
                .andExpect(status().isOk());

        // Then - Verify precision is maintained
        Coupon savedCoupon = couponRepo.findByCode("PRECISION");
        assertThat(savedCoupon.getDiscount()).isEqualTo(new BigDecimal("12.345"));
    }

    @Test
    @DisplayName("Should return empty list when no coupons exist")
    void shouldReturnEmptyListWhenNoCouponsExist() throws Exception {
        // Given - Empty database (cleared in setUp)

        // When & Then
        mockMvc.perform(get("/couponapi/coupons"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        assertThat(couponRepo.count()).isEqualTo(0);
    }
}
