package com.dws.challenge;


import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
        accountsService.getAccountsRepository().clearAccounts();
    }

    private void verifyAccountBalance(final String accountId, final BigDecimal balance) throws Exception {
        this.mockMvc.perform(get("/v1/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(
                        content().string("{\"accountId\":\"" + accountId + "\",\"balance\":" + balance + "}"));
    }

    @Test
    public void mockCreateAccountShouldReturnAccountId() throws Exception {

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"uid-567\",\"balance\":5000}")).andExpect(status().isCreated());

        Account account = accountsService.getAccount("uid-567");

        assertThat(account.getAccountId()).isEqualTo("uid-567");
        assertThat(account.getBalance()).isEqualByComparingTo("5000");
    }

    @Test
    public void verifyAccountDetailsAfterCreation() throws Exception {
        String uniqueAccountId = "uid-" + System.currentTimeMillis();
        Account account = new Account(uniqueAccountId, new BigDecimal("685.88"));
        this.accountsService.createAccount(account);
        this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
                .andExpect(status().isOk())
                .andExpect(
                        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":685.88}"));
    }

    @Test
    public void verifyToThrowExceptionOnDuplicateAccount() throws Exception {
        //#1 account
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"uid-567\",\"balance\":5000}")).andExpect(status().isCreated());

        //#2 same account
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"uid-567\",\"balance\":5000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void verifyToThrowExeptionIfReqAccountIdAndBlanceNotInReq() throws Exception {

        //Body empty
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        //AccountId Empty 
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"\",\"balance\":5000}")).andExpect(status().isBadRequest());

        //AccountIdNotSet
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"balance\":5000}")).andExpect(status().isBadRequest());

        //No Balance
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"uid-567\"}")).andExpect(status().isBadRequest());

    }

    @Test
    public void verifyOnNegativeBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"uid-567\",\"balance\":-10}")).andExpect(status().isBadRequest());
    }

    @Test
    public void verifyAccountToAccountTransfer() throws Exception {

        String accountIdFrom = "uid-785";
        Account accountFrom = new Account(accountIdFrom, new BigDecimal("685.88"));
        this.accountsService.createAccount(accountFrom);

        String accountIdTo = "uid-763";
        Account accountTo = new Account(accountIdTo, new BigDecimal("685.88"));
        this.accountsService.createAccount(accountTo);

        this.mockMvc.perform(post("/v1/accounts/transfer/").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFrom\":\"uid-785\",\"accountTo\":\"uid-763\",\"transferAmount\":100}")).andExpect(status().isAccepted());
    }

    @Test
    public void verifyAccountToAccountTransferOnZeroBalance() throws Exception {
        String accountIdFrom = "uid-785";
        Account accountFrom = new Account(accountIdFrom, new BigDecimal("100.00"));
        this.accountsService.createAccount(accountFrom);
        String accountIdTo = "uid-763";
        Account accountTo = new Account(accountIdTo, new BigDecimal("0"));
        this.accountsService.createAccount(accountTo);

        this.mockMvc.perform(post("/v1/accounts/transfer/").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFrom\":\"uid-785\",\"accountTo\":\"uid-763\",\"transferAmount\":100.00}")).andExpect(status().isAccepted());

        verifyAccountBalance("uid-785", new BigDecimal("0.00"));
        verifyAccountBalance("uid-763", new BigDecimal("100.00"));
    }

    @Test
    public void verifyAccountToAccountTransferOnNegativeAmount() throws Exception {

        String accountIdFrom = "uid-785";
        Account accountFrom = new Account(accountIdFrom, new BigDecimal("685.88"));
        this.accountsService.createAccount(accountFrom);

        String accountIdTo = "uid-763";
        Account accountTo = new Account(accountIdTo, new BigDecimal("685.88"));
        this.accountsService.createAccount(accountTo);

        this.mockMvc.perform(post("/v1/accounts/transfer/").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFrom\":\"uid-785\",\"accountTo\":\"uid-763\",\"transferAmount\":-1.00}")).andExpect(status().isBadRequest());
    }

    @Test
    public void validationCheckForMissingInputRequest() throws Exception {
        this.mockMvc.perform(post("/v1/accounts/transfer/").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isBadRequest());
    }


}
