package com.giftora.service;

import com.giftora.dao.ProductDao;
import com.giftora.exception.AuthorizationException;
import com.giftora.exception.NotFoundException;
import com.giftora.exception.ValidationException;
import com.giftora.model.Product;
import com.giftora.model.Role;
import com.giftora.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private ProductDao productDao;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productDao = mock(ProductDao.class);
        productService = new ProductService(productDao);
    }

    private User user(Role role, long id) {
        User u = new User("User " + id, "u" + id + "@x.com", "hash", role);
        u.setId(id);
        return u;
    }

    @Test
    void buyerCannotCreateProducts() throws Exception {
        assertThrows(AuthorizationException.class, () ->
                productService.create(user(Role.BUYER, 5), "A lovely gift", "Nice", "100", "Home", null, "5"));
        verify(productDao, never()).insert(any());
    }

    @Test
    void sellerCreateBuildsActiveProductWithSellerId() throws Exception {
        productService.create(user(Role.SELLER, 7), "A lovely gift", "Nice description", "199.99", "Home", "/img/x.png", "5");

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productDao).insert(captor.capture());
        Product saved = captor.getValue();
        assertTrue(saved.isActive());
        assertEquals(7L, saved.getSellerId());
        assertEquals(new BigDecimal("199.99"), saved.getPrice());
        assertEquals(5, saved.getStock());
    }

    @Test
    void createRejectsInvalidPrice() {
        assertThrows(ValidationException.class, () ->
                productService.create(user(Role.SELLER, 7), "A lovely gift", "Nice", "free", "Home", null, "5"));
    }

    @Test
    void createRejectsNegativeStock() {
        assertThrows(ValidationException.class, () ->
                productService.create(user(Role.SELLER, 7), "A lovely gift", "Nice", "10", "Home", null, "-1"));
    }

    @Test
    void createRejectsUnsafeImageUrl() {
        assertThrows(ValidationException.class, () ->
                productService.create(user(Role.SELLER, 7), "A lovely gift", "Nice", "10", "Home", "javascript:alert(1)", "1"));
    }

    @Test
    void createRejectsShortName() {
        assertThrows(ValidationException.class, () ->
                productService.create(user(Role.SELLER, 7), "Hi", "Nice", "10", "Home", null, "1"));
    }

    @Test
    void sellerCannotEditAnotherSellersProduct() throws Exception {
        Product existing = new Product();
        existing.setId(1);
        existing.setSellerId(99L);
        when(productDao.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(AuthorizationException.class, () ->
                productService.update(user(Role.SELLER, 7), 1, "Renamed gift", "Desc", "10", "Home", null, "1", true));
    }

    @Test
    void adminCanEditAnyProduct() throws Exception {
        Product existing = new Product();
        existing.setId(1);
        existing.setSellerId(99L);
        when(productDao.findById(1L)).thenReturn(Optional.of(existing));

        Product result = productService.update(user(Role.ADMIN, 2), 1, "Renamed gift", "Desc", "10", "Home", null, "3", true);
        assertEquals("Renamed gift", result.getName());
        verify(productDao).update(any(Product.class));
    }

    @Test
    void updateMissingProductThrowsNotFound() throws Exception {
        when(productDao.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
                productService.update(user(Role.ADMIN, 2), 123, "Renamed gift", "Desc", "10", "Home", null, "3", true));
    }
}
