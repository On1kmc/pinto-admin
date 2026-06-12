package com.ivanov.pinto_admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserDTO {

    @JsonProperty("userId")
    private long userId;
    @JsonProperty("userType")
    private UserType userType;
    @JsonProperty("count")
    private int count;
    @JsonProperty("bonuses")
    private int bonus_count;
    @JsonProperty("avatars")
    private int avatars;
    @JsonProperty("avatarsAvailable")
    private int avatarsAvailable;

    @JsonProperty("balanceChanges")
    private List<BalanceChangeDTO> balanceChanges;
    @JsonProperty("ref1Count")
    private int ref1Count;
    @JsonProperty("ref2Count")
    private int ref2Count;
    @JsonProperty("friendId")
    private long friendId;
    @JsonProperty("username")
    private String username;
    @JsonProperty("name")
    private String name;
    @JsonProperty("marketSource")
    private String marketSource;
    @JsonProperty("startDate")
    private LocalDate startDate;
    @JsonProperty("segment")
    private Segment segment;

    @JsonProperty("rights")
    private java.util.Set<String> rights;


    @Override
    public String toString() {
        return "UserDTO{" +
                "userId=" + userId +
                ", count=" + count +
                ", avatars=" + avatars +
                ", balanceChanges=" + balanceChanges +
                ", ref1Count=" + ref1Count +
                ", ref2Count=" + ref2Count +
                ", friendId=" + friendId +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", marketSource='" + marketSource + '\'' +
                ", startDate=" + startDate +
                '}';
    }
}
