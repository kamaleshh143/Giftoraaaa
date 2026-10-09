package com.giftora.service;

import com.giftora.dao.ProductDao;
import com.giftora.exception.AuthorizationException;
import com.giftora.exception.NotFoundException;
import com.giftora.exception.ValidationException;
import com.giftora.model.Product;
import com.giftora.model.Role;
import com.giftora.model.User;
import com.giftora.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductService {

    public static final int PAGE_SIZE = 12;

    private final ProductDao productDao;

    public ProductService() {
        this(new ProductDao());
    }

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<Product> browse(String query, String category, String sort, int page) throws SQLException {
        int offset = Math.max(page - 1, 0) * PAGE_SIZE;
        return productDao.search(query, category, sort, false, offset, PAGE_SIZE);
    }

    public List<Product> browseAll(String query, String category, String sort) throws SQLException {
        return productDao.search(query, category, sort, false, 0, 1000);
    }

    public int count(String query, String category) throws SQLException {
        return productDao.count(query, category, false);
    }

    public List<String> categories() throws SQLException {
        return productDao.listCategories();
    }

    public Product getById(long id) throws SQLException, NotFoundException {
        return productDao.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new NotFoundException("Product not found."));
    }

    public Product getAnyById(long id) throws SQLException, NotFoundException {
        return productDao.findById(id).orElseThrow(() -> new NotFoundException("Product not found."));
    }

    public List<Product> findBySeller(long sellerId) throws SQLException {
        return productDao.findBySeller(sellerId);
    }

    public Product create(User actor, String name, String description, String priceValue,
                          String category, String imageUrl, String stockValue)
            throws ValidationException, AuthorizationException, SQLException {
        requireSellerOrAdmin(actor);
        Product product = buildProduct(null, name, description, priceValue, category, imageUrl, stockValue);
        product.setActive(true);
        if (actor.getRole() == Role.SELLER) {
            product.setSellerId(actor.getId());
        }
        productDao.insert(product);
        return product;
    }

    public Product update(User actor, long productId, String name, String description, String priceValue,
                          String category, String imageUrl, String stockValue, boolean active)
            throws ValidationException, AuthorizationException, NotFoundException, SQLException {
        requireSellerOrAdmin(actor);
        Product existing = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found."));
        if (actor.getRole() == Role.SELLER
                && (existing.getSellerId() == null || existing.getSellerId() != actor.getId())) {
            throw new AuthorizationException("You can only edit your own products.");
        }
        Product updated = buildProduct(existing, name, description, priceValue, category, imageUrl, stockValue);
        updated.setActive(active);
        productDao.update(updated);
        return updated;
    }

    public void delete(User actor, long productId)
            throws AuthorizationException, NotFoundException, SQLException {
        requireSellerOrAdmin(actor);
        Product existing = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found."));
        if (actor.getRole() == Role.SELLER
                && (existing.getSellerId() == null || existing.getSellerId() != actor.getId())) {
            throw new AuthorizationException("You can only delete your own products.");
        }
        productDao.delete(productId);
    }

    private Product buildProduct(Product existing, String name, String description, String priceValue,
                                 String category, String imageUrl, String stockValue)
            throws ValidationException {
        if (ValidationUtil.isBlank(name) || name.trim().length() < 3) {
            throw new ValidationException("Product name must be at least 3 characters.");
        }
        if (ValidationUtil.isBlank(description)) {
            throw new ValidationException("Product description is required.");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(priceValue.trim());
        } catch (Exception ex) {
            throw new ValidationException("Please enter a valid price.");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero.");
        }
        if (ValidationUtil.isBlank(category)) {
            throw new ValidationException("Please select a category.");
        }
        if (!ValidationUtil.isValidImageUrl(imageUrl)) {
            throw new ValidationException("Image URL must start with http(s):// or be a relative path.");
        }
        int stock;
        try {
            stock = Integer.parseInt(stockValue.trim());
        } catch (Exception ex) {
            throw new ValidationException("Please enter a valid stock quantity.");
        }
        if (stock < 0) {
            throw new ValidationException("Stock cannot be negative.");
        }
        Product product = existing != null ? existing : new Product();
        product.setName(name.trim());
        product.setDescription(description.trim());
        product.setPrice(price);
        product.setCategory(category.trim());
        product.setImageUrl(ValidationUtil.trimToNull(imageUrl));
        product.setStock(stock);
        return product;
    }

    private void requireSellerOrAdmin(User actor) throws AuthorizationException {
        if (actor == null || (actor.getRole() != Role.SELLER && actor.getRole() != Role.ADMIN)) {
            throw new AuthorizationException("Only sellers and administrators can manage products.");
        }
    }
}
