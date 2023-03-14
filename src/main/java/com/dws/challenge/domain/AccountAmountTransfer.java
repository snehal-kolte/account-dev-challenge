package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AccountAmountTransfer {

	@NotNull
	private String accountFrom;
	
	@NotNull
	private String accountTo;
	
	@NotNull
	@Min(value = 0, message = "Please check you have initial balance must be positive.")
	private BigDecimal transferAmount;
	
	@JsonCreator
	public AccountAmountTransfer(@JsonProperty("accountFrom") String accountFrom,
								 @JsonProperty("accountTo") String accountTo,
								 @JsonProperty("transferAmount") BigDecimal transferAmount) {
		this.accountFrom = accountFrom;
		this.accountTo = accountTo;
	    this.transferAmount = transferAmount;
	}
}
