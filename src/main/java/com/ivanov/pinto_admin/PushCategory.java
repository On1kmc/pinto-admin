package com.ivanov.pinto_admin;

import lombok.Getter;

@Getter
public enum PushCategory {

    CAT1_START("Старт (зашёл в бот)"),
    CAT2_PAY("Не оплатил"),
    CAT3_TRYFREE("Попробовал бесплатно"),
    CAT4_PHOTO("Загрузил фото"),
    CAT5_GENDER("Выбрал пол"),
    CAT6_STYLE("Выбрал стиль"),
    CAT7_ANSWER("Ответил на вопрос");

    private final String text;

    PushCategory(String text) {
        this.text = text;
    }
}
