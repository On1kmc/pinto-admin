package com.ivanov.pinto_admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BalanceChangeDTO {

    @JsonProperty("type")
    private String type;
    @JsonProperty("credits")
    private int credits;
    @JsonProperty("requestType")
    private String requestType;
    @JsonProperty("creditType")
    private String creditType;
    @JsonProperty("dateTime")
    private LocalDateTime dateTime;


    @Override
    public String toString() {
        return "BalanceChangeDTO{" +
                "type='" + type + '\'' +
                ", credits=" + credits +
                ", requestType='" + requestType + '\'' +
                ", creditType='" + creditType + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
