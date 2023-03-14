package com.dws.challenge.util;


import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.AmountTransactionException;
import com.dws.challenge.exception.NotSufficientBalanceException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.dws.challenge.util.Constants.*;

@Component
public class AccountTransferValidator {
    public void validate(final Account accountFrom, final Account accountTo, final BigDecimal amount)
            throws AccountNotFoundException, NotSufficientBalanceException {

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AmountTransactionException("Account transfer amount is not valid. Please try with valid amount(Positive)");
        }

        if (accountFrom == null || accountTo == null) {
            throw new AccountNotFoundException(ACCOUNT_NOT_EXISTS);
        }


        if (!isBalanceSufficient(accountFrom, amount)) {
            throw new NotSufficientBalanceException(INSUFFICIENT_BALANCE);
        }
    }

    private boolean isBalanceSufficient(final Account account, final BigDecimal amount) {
        return account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
    }

}
