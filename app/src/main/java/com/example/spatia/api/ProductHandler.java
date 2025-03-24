package com.example.spatia.api;

import com.example.spatia.db.Database;
import com.example.spatia.model.Product;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ProductHandler implements HttpHandler {
    private Database db = new Database();
    private Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        List<Product> products = db.getAllProducts();
        String response = gson.toJson(products);
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}