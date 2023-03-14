# account-dev-challenge


Implementation/Further improvements
--------------------
* All Junit test cases are covered
* uid consider as unique transaction id
* Amount consider as decimal point(ex 55.32)
* Currently concurrentHashmap used but in live production can be replaced by actual database

Sample Example request:

``` 
Request -  'http://localhost:8080/v1/accounts/transfer'

POST
"Content-Type:application/json" 
 
'{
  "accountFrom":"uid-1",
  "accountTo":"uid-2",
  "transferAmount": "5500.80"
}' 
```
