package com.ivanov.pinto_admin;

import lombok.Getter;

@Getter
public enum PushStage {

    MIN5("+5 минут"),
    MIN20("+20 минут"),
    EVENING("Вечер того же дня"),
    DAY2_MORNING("День 2, утро"),
    DAY2_EVENING("День 2, вечер"),
    DAY3("День 3"),
    DAY4("День 4"),
    DAY5("День 5"),
    DAY10("День 10"),
    DAY15("День 15"),
    DAY20("День 20"),
    DAY30("День 30"),
    MONTHLY("Раз в месяц (после дня 60)");

    private final String text;

    PushStage(String text) {
        this.text = text;
    }
}
