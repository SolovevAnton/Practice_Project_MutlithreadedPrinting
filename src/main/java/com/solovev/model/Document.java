package com.solovev.model;

import lombok.Data;

@Data
public class Document {
    private String name;
    private final DocumentType type;
}
