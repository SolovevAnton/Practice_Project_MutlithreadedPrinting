package com.solovev.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Document {
    private String name;
    private final DocumentType type;
}
