package com.godson.discoin4j;

import com.godson.discoin4j.exceptions.*;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Discoin4J {
    private String token;
    private OkHttpClient client = new OkHttpClient();
    private final String url = "https://discoin.sidetrip.xyz/";
    private Headers headers;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final Gson gson = new Gson();
    private Type pendTransType = new TypeToken<List<PendingTransaction>>(){}.getType();

    /**
     * The main class used to interact with the Discoin API.
     *
     * @param token The token given from the Discoin developers to communicate with the API.
     *              If you don't have a token, you won't be able to use Discoin.
     */
    public Discoin4J(String token) {
        this.token = token;
        headers = new Headers.Builder().add("Authorization", token).build();
    }

    /**
     * Makes a transaction to the Discoin API.
     *
     * @param userID The ID of the user making the transaction.
     * @param amount The amount of the "from" currency that's being converted.
     * @param to The currency code belonging to the bot the currency is being converted to.
     * @return The {@link Confirmation confirmation object} that confirms the transaction.
     * @throws IOException If for some reason OkHttp throws an error.
     * @throws DiscoinErrorException If return code is 400.
     * @throws UnauthorizedException If return code is 401.
     * @throws RejectedException If return code is 403.
     * @throws UnknownErrorException If return code does not match any of the codes this wrapper handles.
     */
    public Confirmation makeTransaction(String userID, int amount, String to) throws IOException, DiscoinErrorException, UnauthorizedException, RejectedException, UnknownErrorException {
        RequestBody body = RequestBody.create(JSON, new Transaction(userID, amount, to).toString());
        Request request = new Request.Builder().url(url + "transaction").headers(headers).post(body).build();
        Response response = client.newCall(request).execute();
        switch (response.code()) {
            case 200: return gson.fromJson(response.body().string(), Confirmation.class);
            case 400: throw new DiscoinErrorException(gson.fromJson(response.body().string(), Status.class));
            case 401: throw new UnauthorizedException();
            case 403: throw new RejectedException(gson.fromJson(response.body().string(), Status.class));
            default: throw new UnknownErrorException();
        }
    }

    /**
     * Reverses a transaction in the Discoin API.
     *
     * @param receipt The receipt code given from a transaction.
     * @return The {@link Refund confirmation of the reversal}.
     * @throws IOException If for some reason OkHttp throws an error.
     * @throws DiscoinErrorException If return code is 400.
     * @throws UnauthorizedException If return code is 401.
     * @throws RejectedException If return code is 403.
     * @throws TransactionNotFoundException If return code is 404.
     * @throws UnknownErrorException If return code does not match any of the codes this wrapper handles.
     */
    public String reverseTransaction(String receipt) throws IOException, DiscoinErrorException, UnauthorizedException, RejectedException, TransactionNotFoundException, UnknownErrorException {
        RequestBody body = RequestBody.create(JSON, "{\"receipt\":\"" + receipt + "\"}");
        Request request = new Request.Builder().url(url + "transaction/reverse").headers(headers).post(body).build();
        Response response = client.newCall(request).execute();
        switch (response.code()) {
            case 200: return response.body().string();
            case 400: throw new DiscoinErrorException(gson.fromJson(response.body().string(), Status.class));
            case 401: throw new UnauthorizedException();
            case 403: throw new RejectedException(gson.fromJson(response.body().string(), Status.class));
            case 404: throw new TransactionNotFoundException();
            default: throw new UnknownErrorException();
        }
    }

    /**
     * Loads a receipt's information based on its code given to the user after the transaction.
     *
     * @param receipt The receipt code.
     * @return The completed {@link Receipt receipt} object.
     * @throws IOException If for some reason OkHttp throws an error.
     * @throws DiscoinErrorException If return code is 400.
     * @throws UnauthorizedException If return code is 401.
     * @throws RejectedException If return code is 403.
     * @throws TransactionNotFoundException If return code is 404.
     * @throws UnknownErrorException If return code does not match any of the codes this wrapper handles.
     */
    public Receipt loadReceipt(String receipt) throws IOException, DiscoinErrorException, UnauthorizedException, RejectedException, TransactionNotFoundException, UnknownErrorException {
        Request request = new Request.Builder().url(url + "transaction/" + receipt).headers(headers).get().build();
        Response response = client.newCall(request).execute();
        switch (response.code()) {
            case 200: return gson.fromJson(response.body().string(), Receipt.class);
            case 400: throw new DiscoinErrorException(gson.fromJson(response.body().string(), Status.class));
            case 401: throw new UnauthorizedException();
            case 403: throw new RejectedException(gson.fromJson(response.body().string(), Status.class));
            case 404: throw new TransactionNotFoundException();
            default: throw new UnknownErrorException();
        }
    }

    /**
     * Loads a list of pending transactions for the user to interact with.
     *
     * @return The {@link List list of} {@link PendingTransaction pending transactions}.
     * @throws IOException If for some reason OkHttp throws an error.
     * @throws DiscoinErrorException If return code is 400.
     * @throws UnauthorizedException If return code is 401.
     * @throws RejectedException If return code is 403.
     * @throws UnknownErrorException If return code does not match any of the codes this wrapper handles.
     */
    public List<PendingTransaction> getPendingTransactions() throws IOException, DiscoinErrorException, UnauthorizedException, RejectedException, UnknownErrorException {
        Request request = new Request.Builder().url(url + "transactions").headers(headers).get().build();
        Response response = client.newCall(request).execute();
        switch (response.code()) {
            case 200: return gson.fromJson(response.body().string(), pendTransType);
            case 400: throw new DiscoinErrorException(gson.fromJson(response.body().string(), Status.class));
            case 401: throw new UnauthorizedException();
            case 403: throw new RejectedException(gson.fromJson(response.body().string(), Status.class));
            default: throw new UnknownErrorException();
        }
    }

    /**
     * Represents a transaction.
     */
    private class Transaction {
        @SerializedName("user")
        private String id;
        private int amount;
        @SerializedName("exchangeTo")
        private String to;

        /**
         * Creates an object representing a transaction, which will be sent to the Discoin API.
         *
         * @param id The ID of the user making the transaction.
         * @param amount The amount of the "from" currency that's being converted.
         * @param to The currency code belonging to the bot the currency is being converted to.
         */
        public Transaction(String id, int amount, String to) {
            this.id = id;
            this.amount = amount;
            this.to = to;
        }

        /**
         * Converts the transaction to a json string.
         *
         * @return The converted json string.
         */
        @Override
        public String toString() {
            return gson.toJson(this);
        }
    }

    /**
     * Represents a successful refund.
     * Cannot be created, can only be returned from the wrapper.
     * The main purpose of this is to allow the user to make use of whatever data is outputted, for whatever reason they may have.
     */
    public class Refund {
        private String status;
        private int refundAmount;

        private Refund() {}

        public String getStatus() {
            return status;
        }

        public int getRefundAmount() {
            return refundAmount;
        }
    }

    /**
     * Represents a successful transaction.
     * Cannot be created, can only be returned from the wrapper.
     * The main purpose of this is to allow the user to make use of whatever data is outputted, for whatever reason they may have.
     */
    public class Confirmation {
        private String status;
        private String receipt;
        private int limitNow;
        private int resultAmount;

        private Confirmation() {}

        public String getStatus() {
            return status;
        }

        public String getReceiptCode() {
            return receipt;
        }

        public int getLimitNow() {
            return limitNow;
        }

        public int getResultAmount() {
            return resultAmount;
        }
    }

    /**
     * Represents a single pending transaction.
     * Cannot be created, can only be returned from the wrapper.
     * Separating a pending transaction down to a single object allows the user to handle each pending transaction individually.
     * However, the user will only ever interact with this object when it's returned in a {@link List List object}.
     * The main purpose of this is to allow the user to make use of whatever data is outputted, for whatever reason they may have.
     */
    public class PendingTransaction {
        @SerializedName("user")
        private String userID;
        private long timestamp;
        private String source;
        private double amount;
        private String receipt;
        private String type;

        private PendingTransaction() {}

        public String getUserID() {
            return userID;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getSource() {
            return source;
        }

        public double getAmount() {
            return amount;
        }

        public String getReceipt() {
            return receipt;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * Represents a receipt object.
     * Cannot be created, can only be returned from the wrapper.
     * The main purpose of this is to allow the user to make use of whatever data is outputted, for whatever reason they may have.
     */
    public class Receipt {
        @SerializedName("user")
        private String id;
        private long timestamp;
        private String source;
        private String target;
        private String receipt;
        private int amountSource;
        private int amountDiscoin;
        private int amountTarget;
        private boolean processed;
        private long processTime;
        private boolean reversed;
        private String type;

        private Receipt() {}

        public String getId() {
            return id;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public String getReceiptCode() {
            return receipt;
        }

        public int getAmountSource() {
            return amountSource;
        }

        public int getAmountDiscoin() {
            return amountDiscoin;
        }

        public int getAmountTarget() {
            return amountTarget;
        }

        public boolean isProcessed() {
            return processed;
        }

        public long getProcessTime() {
            return processTime;
        }

        public boolean isReversed() {
            return reversed;
        }

        public String getType() {
            return type;
        }
    }

    public class Status {
        private String status;
        private String reason;
        private String currency;
        private double limit;

        private Status() {}

        public String getStatus() {
            return status;
        }

        public String getReason() {
            return reason;
        }

        public String getCurrency() {
            return currency;
        }

        public double getLimit() {
            return limit;
        }
    }
}
