package com.ivanov.pinto_admin;

import lombok.Getter;

@Getter
public enum Segment {

    OFTEN("Часто"),
    MEDIUM("Средне"),
    SOMETIMES("Иногда"),
    LESS("Редко"),
    NEVER("Не генерят");

    private final String text;

    Segment(String text) {
        this.text = text;
    }


}
