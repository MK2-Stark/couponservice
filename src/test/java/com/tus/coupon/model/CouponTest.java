package com.tus.coupon.model;

import com.tus.coupon.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Coupon Model Tests")
class CouponTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid coupon with all required fields")
    void shouldCreateValidCoupon() {
        // Given
        Coupon coupon = TestDataBuilder.validCoupon();

        // When & Then
        assertThat(coupon.getCode()).isEqualTo("TEST10");
        assertThat(coupon.getDiscount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(coupon.getExpDate()).isEqualTo("2024-12-31");
        assertThat(coupon.getId()).isNull(); // ID should be null before persistence
    }

    @Test
    @DisplayName("Should set and get ID correctly")
    void shouldSetAndGetId() {
        // Given
        Coupon coupon = TestDataBuilder.validCoupon();
        Long expectedId = 1L;

        // When
        coupon.setId(expectedId);

        // Then
        assertThat(coupon.getId()).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("Should set and get code correctly")
    void shouldSetAndGetCode() {
        // Given
        Coupon coupon = new Coupon();
        String expectedCode = "NEWCODE20";

        // When
        coupon.setCode(expectedCode);

        // Then
        assertThat(coupon.getCode()).isEqualTo(expectedCode);
    }

    @Test
    @DisplayName("Should set and get discount correctly")
    void shouldSetAndGetDiscount() {
        // Given
        Coupon coupon = new Coupon();
        BigDecimal expectedDiscount = new BigDecimal("25.50");

        // When
        coupon.setDiscount(expectedDiscount);

        // Then
        assertThat(coupon.getDiscount()).isEqualTo(expectedDiscount);
    }

    @Test
    @DisplayName("Should set and get expiration date correctly")
    void shouldSetAndGetExpDate() {
        // Given
        Coupon coupon = new Coupon();
        String expectedExpDate = "2025-06-15";

        // When
        coupon.setExpDate(expectedExpDate);

        // Then
        assertThat(coupon.getExpDate()).isEqualTo(expectedExpDate);
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValues() {
        // Given
        Coupon coupon = new Coupon();

        // When & Then
        assertThat(coupon.getId()).isNull();
        assertThat(coupon.getCode()).isNull();
        assertThat(coupon.getDiscount()).isNull();
        assertThat(coupon.getExpDate()).isNull();
    }

    @Test
    @DisplayName("Should create coupon with builder pattern")
    void shouldCreateCouponWithBuilder() {
        // Given & When
        Coupon coupon = TestDataBuilder.aCoupon()
                .withCode("BUILDER20")
                .withDiscount("20.00")
                .withExpDate("2025-01-01")
                .build();

        // Then
        assertThat(coupon.getCode()).isEqualTo("BUILDER20");
        assertThat(coupon.getDiscount()).isEqualTo(new BigDecimal("20.00"));
        assertThat(coupon.getExpDate()).isEqualTo("2025-01-01");
    }

    @Test
    @DisplayName("Should create high discount coupon")
    void shouldCreateHighDiscountCoupon() {
        // Given & When
        Coupon coupon = TestDataBuilder.highDiscountCoupon();

        // Then
        assertThat(coupon.getCode()).isEqualTo("MEGA50");
        assertThat(coupon.getDiscount()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should create expired coupon")
    void shouldCreateExpiredCoupon() {
        // Given & When
        Coupon coupon = TestDataBuilder.expiredCoupon();

        // Then
        assertThat(coupon.getCode()).isEqualTo("EXPIRED10");
        assertThat(coupon.getExpDate()).isEqualTo("2020-01-01");
    }

    @Test
    @DisplayName("Should create coupon with specific code")
    void shouldCreateCouponWithSpecificCode() {
        // Given
        String specificCode = "SPECIFIC15";

        // When
        Coupon coupon = TestDataBuilder.couponWithCode(specificCode);

        // Then
        assertThat(coupon.getCode()).isEqualTo(specificCode);
    }
}
