package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AccountAmountTransfer;
import com.dws.challenge.exception.AmountTransactionException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PostMapping(
          path = {"/transfer"},
          consumes = {"application/json"}
  )
  public ResponseEntity<Object> amountToTransfer(@RequestBody @Valid AccountAmountTransfer accountAmountTransfer) {
    try {
      this.accountsService.amountTransfer(accountAmountTransfer.getAccountFrom(), accountAmountTransfer.getAccountTo(), accountAmountTransfer.getTransferAmount());
    } catch (AmountTransactionException amountTransactionException) {
      return new ResponseEntity(amountTransactionException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity("Amount Transfer Completed", HttpStatus.ACCEPTED);
  }

}
