This is testing task for one of the employees.
The task is mostly focused on multithreading and usage of the blocking ques.

## Task description

1. The program must be written using Java 8 or higher.
2. The print dispatcher can work with several types of documents (3-5 types).
3. Each type of document must have unique details: print duration, name of the document type, paper size.
4. The dispatcher places an unlimited number of documents in the print queue. In this case, each document can be processed only if no other document is being processed at the same time, the processing time of each document is equal to the print duration of this document.
5. The dispatcher must have the following methods:
* Stop the dispatcher. Printing of documents in the queue is canceled. The output must be a list of unprinted documents.
* Accept a document for printing. The method must not block program execution.
* Cancel printing of the accepted document if it has not yet been printed.
* Get a sorted list of printed documents. The list can be sorted by choice: by print order, by document type, by print duration, by paper size.
* Calculate the average print duration of printed documents

All implemented and tested using native java
