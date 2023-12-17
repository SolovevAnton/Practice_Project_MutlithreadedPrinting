package com.solovev.service;

import com.solovev.model.Document;
import com.solovev.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Comparator;
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

    @Test
    public void addingNotBlockingPrinting() throws InterruptedException {
        Thread disp = new Thread(dispatcher);
        disp.start();
        Document firstAdded = documents.get(0);

        documents.forEach(dispatcher::add);
        Thread.sleep(10);
        assertFalse(dispatcher.getQueue().contains(firstAdded));
        documents.forEach(dispatcher::add);
        dispatcher.shutDown();
        disp.join();
        assertTrue(dispatcher.getPrintedDocs().contains(firstAdded));
    }
    @Test
    public void docRemovedFromPrintingTest() throws InterruptedException {
        Thread disp = new Thread(dispatcher);
        disp.start();
        Document firstAdded = documents.get(0);
        Document docToCancel = documents.get(1);
        Document notPresentedInQueue = documents.get(2);

        dispatcher.add(firstAdded);
        dispatcher.add(docToCancel);

        assertTrue(dispatcher.cancelPrintDoc(docToCancel));
        assertFalse(dispatcher.cancelPrintDoc(notPresentedInQueue));
        Thread.sleep(firstAdded.getType().getPrintingTimeSeconds() * 1000L); //wait for first to be printed

        assertFalse(dispatcher.shutDown().contains(docToCancel));
        disp.join();
        assertFalse(dispatcher.getPrintedDocs().contains(docToCancel));
    }
    @Test
    public void getListSortedByPrintOrder() throws InterruptedException {
        documents.forEach(dispatcher::add);
        Thread disp = new Thread(dispatcher);
        disp.start();
        dispatcher.softShutDown();
        disp.join();

        assertEquals(documents,dispatcher.getSortedByOrderPrintedDocs());
    }

    @Test
    public void getListSortedByType() throws InterruptedException {
        documents.forEach(dispatcher::add);
        Thread disp = new Thread(dispatcher);
        disp.start();
        dispatcher.softShutDown();
        disp.join();

        List<Document> expectedDocuments = documents.stream().sorted(Comparator.comparing(Document::getType)).toList();

        assertEquals(expectedDocuments,dispatcher.getSortedByTypePrintedDocs());
    }
    @Test
    public void getListSortedByTime() throws InterruptedException {
        documents.forEach(dispatcher::add);
        Thread disp = new Thread(dispatcher);
        disp.start();
        dispatcher.softShutDown();
        disp.join();

        List<Document> expectedDocuments = documents.stream().sorted(Comparator.comparing(d -> d.getType().getPrintingTimeSeconds())).toList();

        assertEquals(expectedDocuments,dispatcher.getSortedByPrintingTimePrintedDocs());
    }

    @Test
    public void getListSortedByPaperSize() throws InterruptedException {
        documents.forEach(dispatcher::add);
        Thread disp = new Thread(dispatcher);
        disp.start();
        dispatcher.softShutDown();
        disp.join();

        List<Document> expectedDocuments = documents.stream().sorted(Comparator.comparing(d -> d.getType().getPaper())).toList();

        assertEquals(expectedDocuments,dispatcher.getSortedByPaperSizePrintedDocs());
    }
    @Test
    public void getAVGPrintingTimeAddAll() throws InterruptedException {
        documents.forEach(dispatcher::add);
        Thread disp = new Thread(dispatcher);
        disp.start();
        double expectedTime = documents.stream().mapToInt(d -> d.getType().getPrintingTimeSeconds()).average().orElse(0);
        dispatcher.softShutDown();
        disp.join();

        assertEquals(expectedTime,dispatcher.getAvgPrintingTime());
    }

    @Test
    public void getAVGPrintingTimeAddOneByOne() throws InterruptedException {
        Thread disp = new Thread(dispatcher);
        disp.start();
        double expectedTime =0;

        dispatcher.add(documents.get(0));
        expectedTime += documents.get(0).getType().getPrintingTimeSeconds();
        Thread.sleep((long)expectedTime * 1010L); //wait for first to be printed
        assertEquals(expectedTime,dispatcher.getAvgPrintingTime());

        dispatcher.add(documents.get(1));
        expectedTime = (expectedTime + documents.get(1).getType().getPrintingTimeSeconds())/2.0;
        Thread.sleep(documents.get(1).getType().getPrintingTimeSeconds() * 1010L); //wait for second to be printed
        assertEquals(expectedTime,dispatcher.getAvgPrintingTime());

        dispatcher.add(documents.get(2));
        expectedTime = (documents.get(0).getType().getPrintingTimeSeconds() + documents.get(1).getType().getPrintingTimeSeconds() + documents.get(2).getType().getPrintingTimeSeconds())/3.0;
        dispatcher.softShutDown();
        disp.join();
        assertEquals(expectedTime,dispatcher.getAvgPrintingTime());
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