package com.solovev.service;

import com.solovev.model.Document;
import com.solovev.model.DocumentType;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Getter
public class Dispatcher implements Runnable{
    private final List<Document> printedDocs = new CopyOnWriteArrayList<>();
    private final BlockingQueue<Document> queue = new LinkedBlockingDeque<>();
    private boolean isStopped;

    @Override
    @SneakyThrows
    public void run() {
        System.out.println("Printer started");
        while(!isStopped){
            Optional<Document> takenDocIfAny =  Optional.ofNullable(queue.poll(1, TimeUnit.SECONDS));
            takenDocIfAny.ifPresent(this::printDoc);
        }
        System.out.println("Printer shut down");
    }

    @SneakyThrows
    private void printDoc(Document doc){
        System.out.printf("Doc named: %s type:%s start printing\n",doc.getName(),doc.getType().getTypeName());
        waitForDocumentToBePrinted(doc);
        printedDocs.add(doc);
        System.out.printf("Doc named: %s type:%s end printing\n",doc.getName(),doc.getType().getTypeName());
    }

    /**
     * Shuts down operation
     * @return list of unprinted documents
     */
    public List<Document> shutDown(){
        System.out.println("Printer is shutting down. Will finish printing current document, if present");
        isStopped = true;
        return new ArrayList<>(queue);
    }

    /**
     * Will shut down as soon as the printing queue is empty.
     * additional elements CAN be added to queue is this state,they WILL be printed
     */
    public void softShutDown(){
        System.out.println("Printer is shutting down. Will finish printing all documents in queue, if present");
        while (!queue.isEmpty()){
            Optional<Document> nextDocIfAny = Optional.ofNullable(queue.peek());
            nextDocIfAny.ifPresent(this::waitForDocumentToBePrinted);
        }
        isStopped = true;
    }

    @SneakyThrows
    private void waitForDocumentToBePrinted(Document doc){
        Thread.sleep(doc.getType().getPrintingTimeSeconds() * 1000L);
    }

    public boolean add(Document doc){
        return queue.add(doc);
    }

    public boolean cancelPrintDoc(Document doc) {
        return queue.remove(doc);
    }

    public List<Document> getSortedByOrderPrintedDocs(){
        return new ArrayList<>(printedDocs);
    }

    public List<Document> getSortedByTypePrintedDocs(){
        return getSortedByComparatorPrintedDocs(Comparator.comparing(Document::getType));
    }
    public List<Document> getSortedByPrintingTimePrintedDocs(){
        return getSortedByComparatorPrintedDocs(Comparator.comparing(d -> d.getType().getPrintingTimeSeconds()));
    }
    public List<Document> getSortedByPaperSizePrintedDocs(){
        return getSortedByComparatorPrintedDocs(Comparator.comparing(d -> d.getType().getPaper()));
    }

    private List<Document> getSortedByComparatorPrintedDocs(Comparator<Document> documentComparator){
        List<Document> documents =  new ArrayList<>(printedDocs);
        documents.sort(documentComparator);
        return documents;
    }
    public double getAvgPrintingTime(){
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
