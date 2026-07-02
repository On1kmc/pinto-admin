package com.ivanov.pinto_admin;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PushMessageDTO {

    private Integer id;
    private String category;
    private String stage;
    private String gender;
    private String style;
    private String text;
    private String media;
    private String buttonText;
    private String buttonPayload;
    private boolean enabled;
}
