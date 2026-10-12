package com.ecommerce.order.shop;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
@Component
public class ShopSettings {
 private final JdbcTemplate db;
 public ShopSettings(JdbcTemplate db){this.db=db;}
 public BigDecimal shippingFee(){return new BigDecimal(db.queryForObject("SELECT setting_value FROM shop_settings WHERE setting_key='shipping_fee'",String.class));}
}
