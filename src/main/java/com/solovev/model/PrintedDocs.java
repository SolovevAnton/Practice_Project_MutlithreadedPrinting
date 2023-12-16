package com.solovev.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PrintedDocs {
    private final List<Document> printedDocs = new ArrayList<>();
    public boolean add(Document document){
        return printedDocs.add(document);
    }
}
