package com.godson.discoin4j;

import com.godson.discoin4j.exceptions.*;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class Discoin4J {
    private String token;
    private OkHttpClient client = new OkHttpClient();
    private final String url = "https://discoin.zws.im/";
    private Headers headers;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final Gson gson = new Gson();
    private Type pendTransType = new TypeToken<List<Transaction>>(){}.getType();

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
     * @return The {@link Transaction confirmation object} that confirms the transaction.
     * @throws IOException If for some reason OkHttp throws an error.
     * @throws UnauthorizedException If return code is 401.
     * @throws GenericErrorException If return code does not match any of the codes this wrapper handles.
     */
    public Transaction makeTransaction(String userID, int amount, String to) throws IOException, UnauthorizedException, GenericErrorException {
        RequestBody body = RequestBody.create(JSON, new TransactionRequest(userID, amount, to).toString());
        Request request = new Request.Builder().url(url + "transactions").headers(headers).post(body).build();
        Response response = client.newCall(request).execute();
        switch (response.code()) {
            case 200: return gson.fromJson(response.body().string(), Transaction.class);
            case 401: throw new UnauthorizedException();
            default: throw new GenericErrorException();
        }
    }

    /**
     * Loads a transaction's information based on its code given to the user after it was completed.
     *
     * @param id The id code.
     * @return The completed {@link Transaction id} object.
     * @throws IOException If for some reason OkHttp throws an error.
     * @throws UnauthorizedException If return code is 401.
     * @throws TransactionNotFoundException If return code is 404.
     * @throws GenericErrorException If return code does not match any of the codes this wrapper handles.
     */
    public Transaction getTransaction(String id) throws IOException, UnauthorizedException, TransactionNotFoundException, GenericErrorException {
        Request request = new Request.Builder().url(url + "transaction/" + id).headers(headers).get().build();
        Response response = client.newCall(request).execute();
        switch (response.code()) {
            case 200: return gson.fromJson(response.body().string(), Transaction.class);
            case 401: throw new UnauthorizedException();
            case 404: throw new TransactionNotFoundException();
            default: throw new GenericErrorException();
        }
    }

    /**
     * Loads a list of pending transactions for the user to interact with.
     *
     * @return The {@link List list of} {@link Transaction pending transactions}.
     * @throws IOException If for some reason OkHttp throws an error.
     * @throws UnauthorizedException If return code is 401.
     * @throws GenericErrorException If return code does not match any of the codes this wrapper handles.
     */
    public List<Transaction> getPendingTransactions(String currency) throws IOException, UnauthorizedException, GenericErrorException {
        Request request = new Request.Builder().url(url + "transactions?filter=to.id||eq||" + currency + "&filter=handled||eq||true").headers(headers).get().build();
        Response response = client.newCall(request).execute();
        switch (response.code()) {
            case 200: return gson.fromJson(response.body().string(), pendTransType);
            case 401: throw new UnauthorizedException();
            default: throw new GenericErrorException();
        }
    }

    /**
     * Represents a transaction to be requested.
     */
    private class TransactionRequest {
        @SerializedName("user")
        private String id;
        private int amount;
        @SerializedName("toId")
        private String to;

        /**
         * Creates an object representing a requested transaction, which will be sent to the Discoin API.
         *
         * @param id The ID of the user making the transaction.
         * @param amount The amount of the "from" currency that's being converted.
         * @param to The currency code belonging to the bot the currency is being converted to.
         */
        public TransactionRequest(String id, int amount, String to) {
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
     * Represents a transaction retrieved from the Discoin API.
     */
    public class Transaction {
        private String id;
        private double amount;
        private String user;
        private boolean handled;
        private String timestamp;
        private double payout;
        private Currency from;
        private Currency to;

        private class Currency {
            String id;
            String name;
            double amount;
            double reserve;
        }

        public String getId() {
            return id;
        }

        public double getAmount() {
            return amount;
        }

        public String getUser() {
            return user;
        }

        public boolean isHandled() {
            return handled;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public double getPayout() {
            return payout;
        }

        public Currency getFrom() {
            return from;
        }

        public Currency getTo() {
            return to;
        }

        public void setHandled() throws GenericErrorException, IOException, UnauthorizedException {
            //If somehow we're trying to set an already handled transaction as handled, do nothing.
            if (handled) return;
            handled = true;
            patch();
        }

        private void patch() throws UnauthorizedException, GenericErrorException, IOException {
            RequestBody body = RequestBody.create(JSON, this.toString());
            Request request = new Request.Builder().url(url + "transactions/" + id).headers(headers).patch(body).build();
            Response response = client.newCall(request).execute();
            switch (response.code()) {
                case 200: return; //Assume all went well. We won't need the data being given to us.
                case 401: throw new UnauthorizedException();
                default: throw new GenericErrorException();
            }
        }

        @Override
        public String toString() {
            return gson.toJson(this);
        }
    }
}
