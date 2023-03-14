package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.util.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private NotificationService notificationService;

    private void shouldSendToNotification(final Account fromAccount, final Account toAccount, final BigDecimal transferAmount) {
        verify(notificationService, Mockito.times(1)).notifyAboutTransfer((fromAccount), "The amount of " + transferAmount + " for transfer request account number: " + toAccount.getAccountId() + " is completed.");
        verify(notificationService, Mockito.times(1)).notifyAboutTransfer((toAccount), "The amount of " + transferAmount + " to account with the account with ID + " + fromAccount.getAccountId() + " is completed.");
    }

    @Test
    public void addAccount() throws Exception {
        Account account = new Account("uid-111");
        account.setBalance(new BigDecimal(1110));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("uid-111")).isEqualTo(account);
    }

    @Test
    public void addAccountFailsOnDuplicateIdAndThrowException() throws Exception {
        String uniqueId = "uid-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }

    @Test
    public void amountAccountTransferTransactionForMultipleAccount() {
        // Account 1 creted
        Account accountFrom = new Account("uid-251");
        accountFrom.setBalance(new BigDecimal(5000));
        this.accountsService.createAccount(accountFrom);

        // Account 2 creted
        Account accountTo = new Account("uid-252");
        accountTo.setBalance(new BigDecimal(5000));
        this.accountsService.createAccount(accountTo);

        //Basic Check
        this.accountsService.amountTransfer("uid-251", "uid-252", new BigDecimal(1111));

        assertThat(this.accountsService.getAccount("uid-251").getBalance()).isEqualTo(new BigDecimal(3889));
        assertThat(this.accountsService.getAccount("uid-252").getBalance()).isEqualTo(new BigDecimal(6111));
    }

    @Test // when balance is low
    public void accountAmountTransferTransactionOnFailure() {
        Account accountFrom = new Account("uid-390");
        accountFrom.setBalance(new BigDecimal(5000));
        this.accountsService.createAccount(accountFrom);

        Account accountTo = new Account("uid-389");
        accountTo.setBalance(new BigDecimal(5000));
        this.accountsService.createAccount(accountTo);

        this.accountsService.amountTransfer("uid-390", "uid-389", new BigDecimal(5000));

        try {
            this.accountsService.amountTransfer("uid-390", "uid-389", new BigDecimal(15000)); //insufficient - total 10k and trying 15 k
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo(Constants.INSUFFICIENT_BALANCE);
        }

        assertThat(this.accountsService.getAccount("uid-390").getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(this.accountsService.getAccount("uid-389").getBalance()).isEqualTo(new BigDecimal(10000));

    }


    @Test // Positive case
    public void accountAmountTransferFunds() {
        final BigDecimal transferAmount = new BigDecimal("200.99");

        final String accountFromId = UUID.randomUUID().toString();
        final String accountToId = UUID.randomUUID().toString();

        final Account accountFrom = new Account(accountFromId, new BigDecimal("880.55"));
        final Account accountTo = new Account(accountToId, new BigDecimal("256.88"));
        this.accountsService.createAccount(accountFrom);
        this.accountsService.createAccount(accountTo);


        this.accountsService.amountTransfer(accountFromId, accountToId, transferAmount);

        assertThat(this.accountsService.getAccount(accountFromId).getBalance()).isEqualTo(new BigDecimal("679.56"));
        assertThat(this.accountsService.getAccount(accountToId).getBalance()).isEqualTo(new BigDecimal("457.87"));

        shouldSendToNotification(accountFrom, accountTo, transferAmount);
    }


}
