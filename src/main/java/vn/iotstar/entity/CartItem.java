package vn.iotstar.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long productId;
    private String name;
    private Double price;
    private Integer quantity;
    private String imageUrl;

    public Double getTotalPrice() {
        return price * quantity;
    }
}