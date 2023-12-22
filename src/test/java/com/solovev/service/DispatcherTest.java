package com.solovev.service;

import com.solovev.model.Document;
import com.solovev.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.print.Doc;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {
    @Test
    public void emptyDispatcherTerminates() {
        assertAll(() -> dispatcher.toString());
    }

    @Test
    public void simpleAddTest() {
        documents.forEach(dispatcher::add);

        assertTrue(dispatcher.getPrintedDocs().isEmpty());
        assertTrue(dispatcher.getQueue().contains(documents.get(documents.size() - 1)));
    }

    @Test
    public void simplePrintTest() throws InterruptedException {
        dispatcher.add(documents.get(4));
        dispatcher.add(documents.get(4));

        Thread.sleep(2000 + DocumentType.REPORT.getPrintingTimeSeconds() * 2L * 1000L); // wait for all to be processed
        assertTrue(dispatcher.shutdownNow().isEmpty());
        assertEquals(List.of(documents.get(4), documents.get(4)), dispatcher.getPrintedDocs());
    }

    @Nested
    public class AwaitTerminationTests {
        @Test
        public void awaitToPrintAllTest() throws InterruptedException {
            documents.forEach(dispatcher::add);
            dispatcher.awaitTermination();
            assertEquals(documents, dispatcher.getPrintedDocs());
        }

        @Test
        public void awaitToPrintOneDocTest() throws InterruptedException {
            Document added = new Document("Added", DocumentType.PLAN);
            dispatcher.add(added);
            Thread.sleep(10); //to empty the queue
            dispatcher.awaitTermination();
            assertTrue(dispatcher.getPrintedDocs().contains(added));
        }

        @Test
        @Timeout(1)
        public void awaitTerminationWhenEmpty() {
            assertAll(() -> dispatcher.awaitTermination());
        }
    }

    @Nested
    public class ShutdownTests {
        @Test
        @Timeout(1)
        public void shutDownEmpty() {
            assertTrue(dispatcher.shutdownNow().isEmpty());
        }

        @Test
        public void shutNotWaitingToPrintLastDocument() throws InterruptedException {
            Document added = new Document("Added", DocumentType.PLAN);
            dispatcher.add(added);
            waitForDocumentToFinish(added);

            dispatcher.shutdownNow();
            assertFalse(dispatcher.getQueue().contains(added));
            assertTrue(dispatcher.getPrintedDocs().contains(added));
        }
        @Test
        public void shutDownNowShouldReturnNotPrintedDocuments() throws InterruptedException {
            Document expecetdOnlyPrintedDocument = documents.get(0);
            documents.forEach(dispatcher::add);
            Thread.sleep(10); //wait for first to start
            List<Document> notPrintedDocs = dispatcher.shutdownNow();
            waitForDocumentToFinish(expecetdOnlyPrintedDocument);

            List<Document> expectedNotPrintedDocuments = documents.stream().filter(d -> !d.equals(expecetdOnlyPrintedDocument)).toList();
            List<Document> expectedPrintedDocuments = List.of(expecetdOnlyPrintedDocument);
            assertEquals(expectedNotPrintedDocuments,notPrintedDocs);
            assertEquals(expectedPrintedDocuments,dispatcher.getPrintedDocs());
        }
    }

    @Test
    public void addingNotBlockingPrinting() throws InterruptedException {
        Document firstAdded = documents.get(0);

        documents.forEach(dispatcher::add);
        Thread.sleep(10);
        assertFalse(dispatcher.getQueue().contains(firstAdded));
        documents.forEach(dispatcher::add);
        dispatcher.shutdownNow();
        Thread.sleep(firstAdded.getType().getPrintingTimeSeconds() * 1000L); //wait for first to be printed
        assertTrue(dispatcher.getPrintedDocs().contains(firstAdded));
    }

    @Test
    public void docRemovedFromPrintingTest() throws InterruptedException {
        Document firstAdded = documents.get(0);
        Document docToCancel = documents.get(1);
        Document notPresentedInQueue = documents.get(2);

        dispatcher.add(firstAdded);
        dispatcher.add(docToCancel);

        assertTrue(dispatcher.cancelPrintDoc(docToCancel));
        assertFalse(dispatcher.cancelPrintDoc(notPresentedInQueue));
        Thread.sleep(firstAdded.getType().getPrintingTimeSeconds() * 1000L); //wait for first to be printed

        assertFalse(dispatcher.shutdownNow().contains(docToCancel));
        assertFalse(dispatcher.getPrintedDocs().contains(docToCancel));
    }

    @Nested
    public class DifferentSortTests {
        @Test
        public void getListSortedByPrintOrder() throws InterruptedException {
            documents.forEach(dispatcher::add);
            dispatcher.awaitTermination();

            assertEquals(documents, dispatcher.getSortedByOrderPrintedDocs());
        }

        @Test
        public void getListSortedByType() throws InterruptedException {
            documents.forEach(dispatcher::add);
            dispatcher.awaitTermination();

            List<Document> expectedDocuments = documents.stream().sorted(Comparator.comparing(Document::getType)).toList();

            assertEquals(expectedDocuments, dispatcher.getSortedByTypePrintedDocs());
        }

        @Test
        public void getListSortedByTime() throws InterruptedException {
            documents.forEach(dispatcher::add);
            dispatcher.awaitTermination();

            List<Document> expectedDocuments = documents.stream().sorted(Comparator.comparing(d -> d.getType().getPrintingTimeSeconds())).toList();

            assertEquals(expectedDocuments, dispatcher.getSortedByPrintingTimePrintedDocs());
        }

        @Test
        public void getListSortedByPaperSize() throws InterruptedException {
            documents.forEach(dispatcher::add);
            dispatcher.awaitTermination();

            List<Document> expectedDocuments = documents.stream().sorted(Comparator.comparing(d -> d.getType().getPaper())).toList();

            assertEquals(expectedDocuments, dispatcher.getSortedByPaperSizePrintedDocs());
        }
    }

    @Nested
    public class AVGTimeTests {
        @Test
        public void getAVGPrintingTimeAddAll() throws InterruptedException {
            documents.forEach(dispatcher::add);
            double expectedTime = documents.stream().mapToInt(d -> d.getType().getPrintingTimeSeconds()).average().orElse(0);
            dispatcher.awaitTermination();

            assertEquals(expectedTime, dispatcher.getAvgPrintingTime());
        }

        @Test
        public void getAVGPrintingTimeAddOneByOne() throws InterruptedException {
            double expectedTime = 0;

            dispatcher.add(documents.get(0));
            expectedTime += documents.get(0).getType().getPrintingTimeSeconds();
            Thread.sleep((long) expectedTime * 1010L); //wait for first to be printed
            assertEquals(expectedTime, dispatcher.getAvgPrintingTime());

            dispatcher.add(documents.get(1));
            expectedTime = (expectedTime + documents.get(1).getType().getPrintingTimeSeconds()) / 2.0;
            Thread.sleep(documents.get(1).getType().getPrintingTimeSeconds() * 1010L); //wait for second to be printed
            assertEquals(expectedTime, dispatcher.getAvgPrintingTime());

            dispatcher.add(documents.get(2));
            expectedTime = (documents.get(0).getType().getPrintingTimeSeconds() + documents.get(1).getType().getPrintingTimeSeconds() + documents.get(2).getType().getPrintingTimeSeconds()) / 3.0;
            dispatcher.awaitTermination();
            assertEquals(expectedTime, dispatcher.getAvgPrintingTime());
        }
    }

    @BeforeEach
    private void setUp() {
        dispatcher = new Dispatcher();
    }

    private Dispatcher dispatcher;
    private List<Document> documents = List.of(
            new Document("First", DocumentType.DOC),
            new Document("Second", DocumentType.PLAN),
            new Document("Third", DocumentType.TABLE),
            new Document("Fourth", DocumentType.DOC),
            new Document("Fifth", DocumentType.REPORT)
    );
    private void waitForDocumentToFinish(Document doc) throws InterruptedException {
        Thread.sleep(doc.getType().getPrintingTimeSeconds() * 1010L);
    }
}