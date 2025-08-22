package com.tus.coupon.repo;

import com.tus.coupon.model.Coupon;
import com.tus.coupon.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Coupon Repository Tests")
class CouponRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CouponRepo couponRepo;

    @Test
    @DisplayName("Should save and retrieve coupon")
    void shouldSaveAndRetrieveCoupon() {
        // Given
        Coupon coupon = TestDataBuilder.validCoupon();

        // When
        Coupon savedCoupon = couponRepo.save(coupon);

        // Then
        assertThat(savedCoupon.getId()).isNotNull();
        assertThat(savedCoupon.getCode()).isEqualTo("TEST10");
        assertThat(savedCoupon.getDiscount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(savedCoupon.getExpDate()).isEqualTo("2024-12-31");
    }

    @Test
    @DisplayName("Should find coupon by code")
    void shouldFindCouponByCode() {
        // Given
        Coupon coupon = TestDataBuilder.aCoupon()
                .withCode("FINDME20")
                .withDiscount("20.00")
                .build();
        entityManager.persistAndFlush(coupon);

        // When
        Coupon foundCoupon = couponRepo.findByCode("FINDME20");

        // Then
        assertThat(foundCoupon).isNotNull();
        assertThat(foundCoupon.getCode()).isEqualTo("FINDME20");
        assertThat(foundCoupon.getDiscount()).isEqualTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("Should return null when coupon code not found")
    void shouldReturnNullWhenCouponCodeNotFound() {
        // When
        Coupon foundCoupon = couponRepo.findByCode("NONEXISTENT");

        // Then
        assertThat(foundCoupon).isNull();
    }

    @Test
    @DisplayName("Should find all coupons")
    void shouldFindAllCoupons() {
        // Given
        Coupon coupon1 = TestDataBuilder.aCoupon()
                .withCode("COUPON1")
                .withDiscount("10.00")
                .build();
        Coupon coupon2 = TestDataBuilder.aCoupon()
                .withCode("COUPON2")
                .withDiscount("20.00")
                .build();
        
        entityManager.persistAndFlush(coupon1);
        entityManager.persistAndFlush(coupon2);

        // When
        List<Coupon> allCoupons = couponRepo.findAll();

        // Then
        assertThat(allCoupons).hasSize(2);
        assertThat(allCoupons).extracting(Coupon::getCode)
                .containsExactlyInAnyOrder("COUPON1", "COUPON2");
    }

    @Test
    @DisplayName("Should find coupon by ID")
    void shouldFindCouponById() {
        // Given
        Coupon coupon = TestDataBuilder.validCoupon();
        Coupon savedCoupon = entityManager.persistAndFlush(coupon);

        // When
        Optional<Coupon> foundCoupon = couponRepo.findById(savedCoupon.getId());

        // Then
        assertThat(foundCoupon).isPresent();
        assertThat(foundCoupon.get().getCode()).isEqualTo("TEST10");
    }

    @Test
    @DisplayName("Should return empty optional when ID not found")
    void shouldReturnEmptyOptionalWhenIdNotFound() {
        // When
        Optional<Coupon> foundCoupon = couponRepo.findById(999L);

        // Then
        assertThat(foundCoupon).isEmpty();
    }

    @Test
    @DisplayName("Should delete coupon")
    void shouldDeleteCoupon() {
        // Given
        Coupon coupon = TestDataBuilder.validCoupon();
        Coupon savedCoupon = entityManager.persistAndFlush(coupon);
        Long couponId = savedCoupon.getId();

        // When
        couponRepo.deleteById(couponId);
        entityManager.flush();

        // Then
        Optional<Coupon> deletedCoupon = couponRepo.findById(couponId);
        assertThat(deletedCoupon).isEmpty();
    }

    @Test
    @DisplayName("Should update coupon")
    void shouldUpdateCoupon() {
        // Given
        Coupon coupon = TestDataBuilder.validCoupon();
        Coupon savedCoupon = entityManager.persistAndFlush(coupon);

        // When
        savedCoupon.setDiscount(new BigDecimal("25.00"));
        savedCoupon.setExpDate("2025-12-31");
        Coupon updatedCoupon = couponRepo.save(savedCoupon);

        // Then
        assertThat(updatedCoupon.getDiscount()).isEqualTo(new BigDecimal("25.00"));
        assertThat(updatedCoupon.getExpDate()).isEqualTo("2025-12-31");
        assertThat(updatedCoupon.getId()).isEqualTo(savedCoupon.getId());
    }

    @Test
    @DisplayName("Should handle case-sensitive code search")
    void shouldHandleCaseSensitiveCodeSearch() {
        // Given
        Coupon coupon = TestDataBuilder.aCoupon()
                .withCode("CaseSensitive")
                .build();
        entityManager.persistAndFlush(coupon);

        // When
        Coupon foundUpperCase = couponRepo.findByCode("CASESENSITIVE");
        Coupon foundLowerCase = couponRepo.findByCode("casesensitive");
        Coupon foundCorrectCase = couponRepo.findByCode("CaseSensitive");

        // Then
        assertThat(foundUpperCase).isNull();
        assertThat(foundLowerCase).isNull();
        assertThat(foundCorrectCase).isNotNull();
        assertThat(foundCorrectCase.getCode()).isEqualTo("CaseSensitive");
    }

    @Test
    @DisplayName("Should count total coupons")
    void shouldCountTotalCoupons() {
        // Given
        entityManager.persistAndFlush(TestDataBuilder.aCoupon().withCode("COUNT1").build());
        entityManager.persistAndFlush(TestDataBuilder.aCoupon().withCode("COUNT2").build());
        entityManager.persistAndFlush(TestDataBuilder.aCoupon().withCode("COUNT3").build());

        // When
        long count = couponRepo.count();

        // Then
        assertThat(count).isEqualTo(3);
    }
}
