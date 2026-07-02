package com.ivanov.pinto_admin;

import lombok.Getter;

@Getter
public enum PushStyle {

    FAME("Слава"),
    MONEY("Деньги"),
    SOUL("Душа"),
    PRO("Профи"),
    BLOGGER("Блогер");

    private final String text;

    PushStyle(String text) {
        this.text = text;
    }
}
