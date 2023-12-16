package com.solovev.service;

import com.solovev.model.Document;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
        Thread.sleep(doc.getType().getPrintingTimeSeconds() * 1000L);
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

    public boolean add(Document doc){
        return queue.add(doc);
    }

}
