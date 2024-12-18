package org.example.product.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.product.model.Product;
import org.example.product.model.Store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet("/product")
public class ProductController extends HttpServlet {

    private static final Gson gson = new Gson();
    private final Store store = Store.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        if (name == null || name.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.println(gson.toJson(store.getProducts()));
            return;
        }

        Optional<Product> product = store.searchToProduct(name);

        if (product.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println(gson.toJson("Product with this name doesn't exist in the store."));
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            out.println(gson.toJson(product.get()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        if (name == null || name.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println(gson.toJson("Missing 'name' parameter."));
            return;
        }

        boolean isDeleted = store.deleteProduct(name);
        if (!isDeleted) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println(gson.toJson("Product with this name doesn't exist."));
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            out.println(gson.toJson("Product has been deleted successfully."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (BufferedReader reader = request.getReader()) {
            Product product = gson.fromJson(reader, Product.class);

            if (product.getName() == null || product.getName().isEmpty() || product.getPrice() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println(gson.toJson("Invalid product data. Name must not be empty and price must be greater than zero."));
                return;
            }

            boolean isAdded = store.addProduct(product);
            if (!isAdded) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println(gson.toJson("Failed to add product. It may already exist."));
            } else {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.println(gson.toJson("The product '" + product.getName() + "' has been added successfully to the store."));
            }
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println(gson.toJson("Invalid JSON format."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (BufferedReader reader = request.getReader()) {
            Product product = gson.fromJson(reader, Product.class);

            if (product.getName() == null || product.getName().isEmpty() || product.getPrice() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println(gson.toJson("Invalid product data. Name must not be empty and price must be greater than zero."));
                return;
            }

            boolean isUpdated = store.updateProduct(product);
            if (!isUpdated) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println(gson.toJson("Failed to update product. It may not exist."));
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                out.println(gson.toJson("The product '" + product.getName() + "' has been updated successfully."));
            }
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println(gson.toJson("Invalid JSON format."));
        }
    }
}