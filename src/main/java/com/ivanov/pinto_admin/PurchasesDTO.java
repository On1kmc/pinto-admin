package com.ivanov.pinto_admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PurchasesDTO {

    @JsonProperty("payItemType")
    private String payItemType;
    @JsonProperty("amount")
    private int amount;
    @JsonProperty("purchaseDate")
    private LocalDateTime purchaseDate;


    @Override
    public String toString() {
        return "PurchasesDTO{" +
                "payItemType='" + payItemType + '\'' +
                ", amount=" + amount +
                ", purchaseDate=" + purchaseDate +
                '}';
    }
}
