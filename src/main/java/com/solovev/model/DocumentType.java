package com.solovev.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.solovev.model.PaperType.*;

@RequiredArgsConstructor
@Getter
public enum DocumentType {
    DOC("doc",A4,2),
    REPORT("reports",A5,1),
    TABLE("table",A3, 3),
    PLAN("construction Plan",A2,5);

    private final String typeName;
    private final PaperType paper;
    private final int printingTimeSeconds;
}
