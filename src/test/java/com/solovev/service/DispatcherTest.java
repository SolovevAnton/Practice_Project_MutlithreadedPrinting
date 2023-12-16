package com.solovev.service;

import com.solovev.model.Document;
import com.solovev.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {
    @Test
    public void simpleAddTest(){
        documents.forEach(dispatcher::add);

        assertTrue(dispatcher.getPrintedDocs().isEmpty());
        assertTrue(dispatcher.getQueue().containsAll(documents));
    }

    @Test
    public void simplePrintTest() throws InterruptedException {
        dispatcher.add(documents.get(4));
        dispatcher.add(documents.get(4));

        Thread disp = new Thread(dispatcher);
        disp.start();
        Thread.sleep(2000 + DocumentType.REPORT.getPrintingTimeSeconds() * 2L * 1000L); // wait for all to be processed
        assertTrue(dispatcher.shutDown().isEmpty());
        assertEquals(List.of(documents.get(4),documents.get(4)),dispatcher.getPrintedDocs());
    }

    @Test
    @Timeout(3)
    public void shutDownEmpty() throws InterruptedException {
        Thread disp = new Thread(dispatcher);
        disp.start();
        Thread.sleep(1);
        dispatcher.shutDown();
        assertAll(disp::join);
    }

    @BeforeEach
    private void setUp(){
        dispatcher = new Dispatcher();
    }

    private Dispatcher dispatcher;
    private  List<Document> documents = List.of(
            new Document("First", DocumentType.DOC),
            new Document("Second", DocumentType.PLAN),
            new Document("Third", DocumentType.TABLE),
            new Document("Fourth", DocumentType.DOC),
            new Document("Fifth", DocumentType.REPORT)
            );
}