package com.solovev.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaperType {
    A2(420,594),
    A3(297,420),
    A4(210,297),
    A5(148,210);

    private final int widthMM;
    private final int lengthMM;
}
