package com.tus.coupon.util;

import com.tus.coupon.model.Coupon;
import java.math.BigDecimal;

/**
 * Test data builder utility class for creating test fixtures
 */
public class TestDataBuilder {

    public static class CouponBuilder {
        private Long id;
        private String code = "TEST10";
        private BigDecimal discount = new BigDecimal("10.00");
        private String expDate = "2024-12-31";

        public CouponBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public CouponBuilder withCode(String code) {
            this.code = code;
            return this;
        }

        public CouponBuilder withDiscount(BigDecimal discount) {
            this.discount = discount;
            return this;
        }

        public CouponBuilder withDiscount(String discount) {
            this.discount = new BigDecimal(discount);
            return this;
        }

        public CouponBuilder withExpDate(String expDate) {
            this.expDate = expDate;
            return this;
        }

        public Coupon build() {
            Coupon coupon = new Coupon();
            coupon.setId(id);
            coupon.setCode(code);
            coupon.setDiscount(discount);
            coupon.setExpDate(expDate);
            return coupon;
        }
    }

    public static CouponBuilder aCoupon() {
        return new CouponBuilder();
    }

    public static Coupon validCoupon() {
        return aCoupon().build();
    }

    public static Coupon expiredCoupon() {
        return aCoupon()
                .withCode("EXPIRED10")
                .withExpDate("2020-01-01")
                .build();
    }

    public static Coupon highDiscountCoupon() {
        return aCoupon()
                .withCode("MEGA50")
                .withDiscount("50.00")
                .build();
    }

    public static Coupon couponWithCode(String code) {
        return aCoupon()
                .withCode(code)
                .build();
    }
}
