package com.example.library.studentlibrary.services;

import com.example.library.studentlibrary.models.*;
import com.example.library.studentlibrary.repositories.BookRepository;
import com.example.library.studentlibrary.repositories.CardRepository;
import com.example.library.studentlibrary.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    @Autowired
    BookRepository bookRepository5;

    @Autowired
    CardRepository cardRepository5;

    @Autowired
    TransactionRepository transactionRepository5;

    @Value("${books.max_allowed}")
    int max_allowed_books;

    @Value("${books.max_allowed_days}")
    int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    int fine_per_day;

    public String issueBook(int cardId, int bookId) throws Exception {
        //check whether bookId and cardId already exist
        Transaction t = null;
        if(bookRepository5.existsById(bookId) && cardRepository5.existsById(cardId)){
            if(!bookRepository5.findById(bookId).get().isAvailable())
                throw new Exception("Book is either unavailable or not present");
            if(!(cardRepository5.getOne(cardId).getCardStatus()==CardStatus.ACTIVATED))
                throw new Exception("Card is invalid");
            if(!(cardRepository5.getOne(cardId).getBooks().size()<max_allowed_books))
                throw new Exception("Book limit has reached for this card");
           t = Transaction.builder().build();
            transactionRepository5.save(t);
        }
        //conditions required for successful transaction of issue book:
        //1. book is present and available
        // If it fails: throw new Exception("Book is either unavailable or not present");
        //2. card is present and activated
        // If it fails: throw new Exception("Card is invalid");
        //3. number of books issued against the card is strictly less than max_allowed_books
        // If it fails: throw new Exception("Book limit has reached for this card");
        //If the transaction is successful, save the transaction to the list of transactions and return the id

        //Note that the error message should match exactly in all cases

       return t.getTransactionId() ; //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId,TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);

        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well
        int fine = 0;
//        if(getMax_allowed_days<java.time.LocalDate.now().compareTo(transaction.getTransactionDate()))
//            ;
        Period diff
                = Period
                .between(transaction.getTransactionDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
        java.time.LocalDate.now());
        if(diff.getDays()>getMax_allowed_days)
            fine = (diff.getDays()-getMax_allowed_days)*fine_per_day;

        bookRepository5.getOne(bookId).setAvailable(true);

        Transaction returnBookTransaction  = Transaction.builder().build();
        returnBookTransaction.setFineAmount(fine);
        return returnBookTransaction;
        //return the transaction after updating all details
    }
}