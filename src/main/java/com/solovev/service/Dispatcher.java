package com.solovev.service;

import com.solovev.model.Document;
import com.solovev.model.DocumentType;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.*;

public class Dispatcher {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<Document> printedDocs = new CopyOnWriteArrayList<>();
    @Getter
    private final BlockingQueue<Document> queue = new LinkedBlockingDeque<>();
    @Getter
    private boolean isShutdown;


    public boolean add(Document doc) {
        boolean addingResult = queue.add(doc);
        executorService.execute(this::takeFromQueueAndPrintDoc);
        return addingResult;
    }

    private void takeFromQueueAndPrintDoc() {
        if (!isShutdown) {
            Optional<Document> documentFromQueueIfAny = Optional.ofNullable(queue.poll());
            documentFromQueueIfAny.ifPresent(this::printDoc);
        }
    }

    private void printDoc(Document doc) {
        System.out.printf("Doc named: %s type:%s start printing\n", doc.getName(), doc.getType().getTypeName());
        try {
            waitForDocumentToBePrinted(doc);
            printedDocs.add(doc);
            System.out.printf("Doc named: %s type:%s end printing\n", doc.getName(), doc.getType().getTypeName());
        } catch (InterruptedException e) {
            System.out.printf("Document %s printing is cancelled\n",doc.getName());;
        }
    }


    private void waitForDocumentToBePrinted(Document doc) throws InterruptedException {
            Thread.sleep(doc.getType().getPrintingTimeSeconds() * 1000L);

    }

    /**
     * Shuts down operations, will print of the current document
     *
     * @return list of unprinted documents
     */
    public List<Document> shutdownNow() {
        System.out.println("Printer is shutting down. Will finish printing current document, if present");
        isShutdown = true;
        executorService.shutdown();
        return new ArrayList<>(queue);
    }

    /**
     * Will shut down as soon as the printing queue is empty.
     */
    public void awaitTermination() throws InterruptedException {
        System.out.println("Printer is shutting down. Will finish printing all documents in queue, if present");
        executorService.shutdown();

        while (!executorService.isTerminated()) {
            Thread.sleep(1000L);
        }
    }

    public boolean cancelPrintDoc(Document doc) {
        return queue.remove(doc);
    }
    public void cancelPrintNow(){
        executorService.shutdownNow();
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this::takeFromQueueAndPrintDoc);
    }

    public List<Document> getSortedByOrderPrintedDocs() {
        return new ArrayList<>(printedDocs);
    }

    public List<Document> getSortedByTypePrintedDocs() {
        return getSortedByComparatorPrintedDocs(Comparator.comparing(Document::getType));
    }

    public List<Document> getSortedByPrintingTimePrintedDocs() {
        return getSortedByComparatorPrintedDocs(Comparator.comparing(d -> d.getType().getPrintingTimeSeconds()));
    }

    public List<Document> getSortedByPaperSizePrintedDocs() {
        return getSortedByComparatorPrintedDocs(Comparator.comparing(d -> d.getType().getPaper()));
    }

    private List<Document> getSortedByComparatorPrintedDocs(Comparator<Document> documentComparator) {
        List<Document> documents = new ArrayList<>(printedDocs);
        documents.sort(documentComparator);
        return documents;
    }

    public double getAvgPrintingTime() {
        return printedDocs
                .stream()
                .map(Document::getType)
                .mapToInt(DocumentType::getPrintingTimeSeconds)
                .average()
                .orElse(0);
    }

    public List<Document> getPrintedDocs() {
        return Collections.unmodifiableList(printedDocs);
    }
}
