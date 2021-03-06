/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.revolut.task;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.task.bean.request.TransferRequest;
import com.revolut.task.exception.AccountNotFoundException;
import com.revolut.task.exception.BalanceNegativeException;
import com.revolut.task.exception.InvalidAmountException;
import com.revolut.task.exception.SelfTransferException;
import com.revolut.task.model.Account;

import static spark.Spark.*;


public class App {
    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new AccountServiceModule());
        AccountService accountService = injector.getInstance(AccountService.class);

        before((request, response) -> response.type("application/json"));

        get("/account/:id", (req, res) -> accountService.getAccount(req.params("id")), new Gson()::toJson);
        post("/account", (req, res) -> accountService.createAccount(), new Gson()::toJson);
        delete("/account/:id", (req, res) -> accountService.deleteAccount(req.params("id")), new Gson()::toJson);
        put("/account/:id", (req, res) -> {
            Account acc = new Gson().fromJson(req.body(), Account.class);
            if (acc == null) {
                halt(400);
            }
            return accountService.updateAccount(req.params("id"), acc.getBalance());
        }, new Gson()::toJson);
        post("/transfer", (req, res) -> {
            TransferRequest payload = new Gson().fromJson(req.body(), TransferRequest.class);
            if (payload == null) {
                halt(400);
            }
            accountService.transferMoney(payload.getAccountFromId(), payload.getAccountToId(), payload.getAmount());
            return "{\"message\":\"Money has been transferred\"}";
        }, new Gson()::toJson);

        exception(AccountNotFoundException.class, (exception, request, response) -> {
            response.type("application/json");
            response.body("{\"message\":\"Account not found\"}");
            response.status(404);
        });
        exception(SelfTransferException.class, (exception, request, response) -> {
            response.type("application/json");
            response.body("{\"message\":\"Cannot transfer to the same account\"}");
            response.status(400);
        });
        exception(BalanceNegativeException.class, (exception, request, response) -> {
            response.type("application/json");
            response.body("{\"message\":\"Insufficient funds\"}");
            response.status(400);
        });
        exception(InvalidAmountException.class, (exception, request, response) -> {
            response.type("application/json");
            response.body("{\"message\":\"Please input a positive amount\"}");
            response.status(400);
        });
        notFound((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"No such endpoint\"}";
        });
    }
}
