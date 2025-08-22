package com.tus.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tus.coupon.model.Coupon;
import com.tus.coupon.repo.CouponRepo;
import com.tus.coupon.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponRestController.class)
@DisplayName("Coupon REST Controller Tests")
class CouponRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponRepo couponRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create coupon successfully")
    void shouldCreateCouponSuccessfully() throws Exception {
        // Given
        Coupon inputCoupon = TestDataBuilder.validCoupon();
        Coupon savedCoupon = TestDataBuilder.aCoupon()
                .withId(1L)
                .withCode("TEST10")
                .withDiscount("10.00")
                .withExpDate("2024-12-31")
                .build();

        when(couponRepo.save(any(Coupon.class))).thenReturn(savedCoupon);

        // When & Then
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCoupon)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("TEST10")))
                .andExpect(jsonPath("$.discount", is(10.00)))
                .andExpect(jsonPath("$.expDate", is("2024-12-31")));

        verify(couponRepo, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("Should get coupon by code successfully")
    void shouldGetCouponByCodeSuccessfully() throws Exception {
        // Given
        String couponCode = "GETME20";
        Coupon coupon = TestDataBuilder.aCoupon()
                .withId(1L)
                .withCode(couponCode)
                .withDiscount("20.00")
                .withExpDate("2024-12-31")
                .build();

        when(couponRepo.findByCode(couponCode)).thenReturn(coupon);

        // When & Then
        mockMvc.perform(get("/couponapi/coupons/{code}", couponCode))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is(couponCode)))
                .andExpect(jsonPath("$.discount", is(20.00)))
                .andExpect(jsonPath("$.expDate", is("2024-12-31")));

        verify(couponRepo, times(1)).findByCode(couponCode);
    }

    @Test
    @DisplayName("Should return null when coupon code not found")
    void shouldReturnNullWhenCouponCodeNotFound() throws Exception {
        // Given
        String nonExistentCode = "NOTFOUND";
        when(couponRepo.findByCode(nonExistentCode)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/couponapi/coupons/{code}", nonExistentCode))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(couponRepo, times(1)).findByCode(nonExistentCode);
    }

    @Test
    @DisplayName("Should get all coupons successfully")
    void shouldGetAllCouponsSuccessfully() throws Exception {
        // Given
        List<Coupon> coupons = Arrays.asList(
                TestDataBuilder.aCoupon()
                        .withId(1L)
                        .withCode("COUPON1")
                        .withDiscount("10.00")
                        .build(),
                TestDataBuilder.aCoupon()
                        .withId(2L)
                        .withCode("COUPON2")
                        .withDiscount("20.00")
                        .build()
        );

        when(couponRepo.findAll()).thenReturn(coupons);

        // When & Then
        mockMvc.perform(get("/couponapi/coupons"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].code", is("COUPON1")))
                .andExpect(jsonPath("$[0].discount", is(10.00)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].code", is("COUPON2")))
                .andExpect(jsonPath("$[1].discount", is(20.00)));

        verify(couponRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no coupons exist")
    void shouldReturnEmptyListWhenNoCouponsExist() throws Exception {
        // Given
        when(couponRepo.findAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/couponapi/coupons"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(couponRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle invalid JSON in create coupon request")
    void shouldHandleInvalidJsonInCreateCouponRequest() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(couponRepo, never()).save(any(Coupon.class));
    }

    @Test
    @DisplayName("Should create coupon with null ID")
    void shouldCreateCouponWithNullId() throws Exception {
        // Given
        Coupon inputCoupon = TestDataBuilder.aCoupon()
                .withCode("NULLID")
                .withDiscount("15.00")
                .build();
        
        Coupon savedCoupon = TestDataBuilder.aCoupon()
                .withId(5L)
                .withCode("NULLID")
                .withDiscount("15.00")
                .build();

        when(couponRepo.save(any(Coupon.class))).thenReturn(savedCoupon);

        // When & Then
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCoupon)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.code", is("NULLID")));

        verify(couponRepo, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("Should handle special characters in coupon code")
    void shouldHandleSpecialCharactersInCouponCode() throws Exception {
        // Given
        String specialCode = "SPECIAL-CODE_2024!";
        Coupon coupon = TestDataBuilder.aCoupon()
                .withCode(specialCode)
                .build();

        when(couponRepo.findByCode(specialCode)).thenReturn(coupon);

        // When & Then
        mockMvc.perform(get("/couponapi/coupons/{code}", specialCode))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(specialCode)));

        verify(couponRepo, times(1)).findByCode(specialCode);
    }

    @Test
    @DisplayName("Should handle large discount values")
    void shouldHandleLargeDiscountValues() throws Exception {
        // Given
        Coupon inputCoupon = TestDataBuilder.aCoupon()
                .withDiscount("999.99")
                .build();
        
        Coupon savedCoupon = TestDataBuilder.aCoupon()
                .withId(1L)
                .withDiscount("999.99")
                .build();

        when(couponRepo.save(any(Coupon.class))).thenReturn(savedCoupon);

        // When & Then
        mockMvc.perform(post("/couponapi/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCoupon)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount", is(999.99)));

        verify(couponRepo, times(1)).save(any(Coupon.class));
    }
}
