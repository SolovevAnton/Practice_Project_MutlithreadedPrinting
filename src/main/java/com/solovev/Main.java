package com.solovev;

import com.solovev.model.Document;
import com.solovev.model.DocumentType;
import com.solovev.service.Dispatcher;

public class Main {
    /* todo
    *   1. правильный путь проверки окончания заданий
    *
     */
    public static void main(String[] args) {

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.add(new Document(DocumentType.PLAN));
        dispatcher.add(new Document(DocumentType.DOC));
        dispatcher.shutdownNow();
    }
}