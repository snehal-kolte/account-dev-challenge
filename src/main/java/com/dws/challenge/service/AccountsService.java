package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AmountTransactionException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.util.AccountTransferValidator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

import static com.dws.challenge.util.Constants.*;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AccountTransferValidator accountTransferValidator;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    // @Transactional - In live production environment can leverage Transaction
    public void amountTransfer(final String fromAccount, final String toAccount, final BigDecimal transferAmount)
            throws AmountTransactionException {

        accountTransferValidator.validate(getAccount(fromAccount), getAccount(toAccount), transferAmount);

        //Multithreading env
        final ReentrantLock lock = new ReentrantLock();
        lock.lock();

        try {
            this.requestToDebit(fromAccount, transferAmount);
            this.requestToCredit(toAccount, transferAmount);
        } finally {
            lock.unlock();
        }


        notificationService.notifyAboutTransfer(getAccount(fromAccount), "The amount of " + transferAmount + " for transfer request account number: " + toAccount + " is completed.");
        notificationService.notifyAboutTransfer(getAccount(toAccount), "The amount of " + transferAmount + " to account with the account with ID + " + fromAccount + " is completed.");
    }

    Account requestToDebit(String accountId, BigDecimal amount) throws AmountTransactionException {
        final Account account = getAccount(accountId);
        if (account == null) {
            throw new AmountTransactionException(ACCOUNT_NOT_EXISTS);
        }
        if (account.getBalance().compareTo(amount) < 0) { // -1 check
            throw new AmountTransactionException(INSUFFICIENT_BALANCE);
        }
        BigDecimal bal = account.getBalance().subtract(amount);
        account.setBalance(bal);
        return account;
    }

    Account requestToCredit(String accountId, BigDecimal amount) throws AmountTransactionException {

        final Account account = getAccount(accountId);
        if (account == null) {
            throw new AmountTransactionException(ACCOUNT_NOT_EXISTS);
        }
        BigDecimal bal = account.getBalance().add(amount);
        account.setBalance(bal);
        return account;
    }

}
