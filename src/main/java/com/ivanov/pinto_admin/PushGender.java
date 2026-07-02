package com.ivanov.pinto_admin;

import lombok.Getter;

@Getter
public enum PushGender {

    MAN("Мужчина"),
    WOMAN("Женщина"),
    WW("Женщина + женщина"),
    WM("Женщина + мужчина"),
    MM("Мужчина + мужчина");

    private final String text;

    PushGender(String text) {
        this.text = text;
    }
}
