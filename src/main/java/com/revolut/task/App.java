/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.revolut.task;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.task.bean.request.TransferRequest;
import com.revolut.task.bean.response.StandardResponse;
import com.revolut.task.bean.response.StatusResponse;
import com.revolut.task.exception.AccountNotFoundException;
import com.revolut.task.exception.BalanceNegativeException;
import com.revolut.task.exception.NegativeTransferAmountException;
import com.revolut.task.model.Account;


public class App {
    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new AccountServiceModule());
        AccountService accountService = injector.getInstance(AccountService.class);
       get("/account/:id", (req, res) -> accountService.getAccount(req.params("id")), new Gson()::toJson);
       post("/account", (req, res) -> accountService.createAccount(), new Gson()::toJson);
       delete("/account/:id", (req, res) -> accountService.deleteAccount(req.params("id")), new Gson()::toJson);
       put("/account/:id", (req, res) -> {
           Account acc = new Gson().fromJson(req.body(), Account.class);

           //TODO: handle empty and wrong body
           return accountService.updateAccount(req.params("id"), acc.getBalance());
       }, new Gson()::toJson);
       post("/transfer", (req, res) -> {

           //TODO: handle empty and wrong body
           TransferRequest payload = new Gson().fromJson(req.body(), TransferRequest.class);
           accountService.transferMoney(payload.getAccountFromId(), payload.getAccountToId(), payload.getAmount());
           return new StandardResponse(StatusResponse.SUCCESS, payload.getAmount().toString() + " has been transferred");
       }, new Gson()::toJson);

       exception(AccountNotFoundException.class, (exception, request, response) -> {
            response.type("application/json");
            response.body("{\"message\":\"Account not found\"}");
            response.status(404);
        });
        exception(BalanceNegativeException.class, (exception, request, response) -> {
            response.type("application/json");
            response.body("{\"message\":\"You have insufficient funds\"}");
            response.status(500);
        });
        exception(NegativeTransferAmountException.class, (exception, request, response) -> {
            response.type("application/json");
            response.body("{\"message\":\"You cannot transfer negative amount\"}");
            response.status(400);
        });
    }
}
