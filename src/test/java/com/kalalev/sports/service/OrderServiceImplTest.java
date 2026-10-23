package com.kalalev.sports.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kalalev.sports.dto.CartItem;
import com.kalalev.sports.exception.InvalidOrderException;
import com.kalalev.sports.model.Order;
import com.kalalev.sports.model.Product;
import com.kalalev.sports.model.User;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.repository.ProductRepository;
import com.kalalev.sports.service.impl.OrderServiceImpl;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2023-05-10T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    private OrderServiceImpl orderService;
    private final User user = new User();

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, productRepository, CLOCK);
    }

    private Product product(int id, String price) {
        Product product = new Product("Product " + id, new BigDecimal(price), "Description", null);
        product.setProductId(id);
        return product;
    }

    @Test
    void placeOrderPricesEachLineFromTheDatabase() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product(1, "19.99")));
        when(productRepository.findById(3)).thenReturn(Optional.of(product(3, "12.99")));
        when(orderRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Order> orders = orderService.placeOrder(user, List.of(new CartItem(1, 2), new CartItem(3, 1)));

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getTotalPrice()).isEqualByComparingTo("39.98");
        assertThat(orders.get(0).getProductQuantity()).isEqualTo(2);
        assertThat(orders.get(1).getTotalPrice()).isEqualByComparingTo("12.99");
        assertThat(orders).allSatisfy(order -> {
            assertThat(order.getUser()).isSameAs(user);
            assertThat(order.getOrderDate()).isEqualTo(LocalDateTime.of(2023, 5, 10, 12, 0));
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 100})
    void placeOrderRejectsQuantitiesOutsideTheAllowedRange(int quantity) {
        assertThatThrownBy(() -> orderService.placeOrder(user, List.of(new CartItem(1, quantity))))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("between 1 and 99");
        verify(orderRepository, never()).saveAll(any());
    }

    @Test
    void placeOrderSavesNothingWhenOneProductDoesNotExist() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product(1, "19.99")));
        when(productRepository.findById(42)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(user, List.of(new CartItem(1, 1), new CartItem(42, 1))))
                .isInstanceOf(InvalidOrderException.class);
        verify(orderRepository, never()).saveAll(any());
    }

    @Test
    void placeOrderRejectsAnEmptyCart() {
        assertThatThrownBy(() -> orderService.placeOrder(user, List.of()))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("Your cart is empty");
    }
}
