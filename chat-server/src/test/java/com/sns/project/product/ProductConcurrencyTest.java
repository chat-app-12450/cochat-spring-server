package com.sns.project.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sns.project.core.domain.product.Product;
import com.sns.project.core.domain.product.ProductStatus;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.exception.conflict.ProductStateConflictException;
import com.sns.project.core.repository.product.ProductRepository;
import com.sns.project.core.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ProductConcurrencyTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentPurchase_onlyOneBuyerSucceeds() throws Exception {
        ProductFixture fixture = transactionTemplate.execute(status -> {
            User seller = userRepository.save(newUser("seller@test.com", "seller", "seller01"));
            User buyer1 = userRepository.save(newUser("buyer1@test.com", "buyer1", "buyer01"));
            User buyer2 = userRepository.save(newUser("buyer2@test.com", "buyer2", "buyer02"));

            Product product = Product.builder()
                .title("맥북 프로")
                .description("동시 구매 테스트 상품")
                .price(1000000L)
                .status(ProductStatus.ON_SALE)
                .build();
            seller.addProduct(product);
            Product savedProduct = productRepository.save(product);
            return new ProductFixture(savedProduct.getId(), buyer1.getId(), buyer2.getId());
        });

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> first = executor.submit(() -> tryPurchase(fixture.productId(), fixture.firstBuyerId(), ready, start));
            Future<Boolean> second = executor.submit(() -> tryPurchase(fixture.productId(), fixture.secondBuyerId(), ready, start));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<Boolean> results = List.of(
                first.get(5, TimeUnit.SECONDS),
                second.get(5, TimeUnit.SECONDS)
            );

            assertThat(results).containsExactlyInAnyOrder(true, false);

            Product product = transactionTemplate.execute(status ->
                productRepository.findDetailById(fixture.productId()).orElseThrow());

            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD);
            assertThat(product.getSoldBuyer()).isNotNull();
            assertThat(List.of(fixture.firstBuyerId(), fixture.secondBuyerId()))
                .contains(product.getSoldBuyer().getId());
            assertThat(product.getVersion()).isNotNull();
            assertThat(product.getVersion()).isGreaterThanOrEqualTo(1L);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void reservedProduct_canOnlyBePurchasedByReservedBuyer() {
        User seller = userRepository.save(newUser("seller-reserved@test.com", "seller-reserved", "seller02"));
        User reservedBuyer = userRepository.save(newUser("reserved@test.com", "reserved", "buyer03"));
        User anotherBuyer = userRepository.save(newUser("another@test.com", "another", "buyer04"));

        Product product = Product.builder()
            .title("아이폰")
            .description("예약자만 구매 가능")
            .price(500000L)
            .status(ProductStatus.ON_SALE)
            .build();
        seller.addProduct(product);
        productRepository.save(product);

        product.reserve(reservedBuyer);

        assertThatThrownBy(() -> product.purchase(anotherBuyer))
            .isInstanceOf(ProductStateConflictException.class)
            .hasMessage("다른 사용자가 예약한 상품입니다.");
    }

    private boolean tryPurchase(Long productId, Long buyerId, CountDownLatch ready, CountDownLatch start) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();

            Product product = entityManager.createQuery(
                    "select p from Product p join fetch p.seller where p.id = :productId", Product.class)
                .setParameter("productId", productId)
                .getSingleResult();
            User buyer = entityManager.find(User.class, buyerId);

            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("동시성 테스트 시작 신호를 받지 못했습니다.");
            }

            product.purchase(buyer);
            entityManager.flush();
            entityManager.getTransaction().commit();
            return true;
        } catch (OptimisticLockException | RollbackException ex) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return false;
        } finally {
            entityManager.close();
        }
    }

    private User newUser(String email, String name, String userId) {
        return User.builder()
            .email(email)
            .password("encoded-password")
            .name(name)
            .userId(userId)
            .build();
    }

    private record ProductFixture(Long productId, Long firstBuyerId, Long secondBuyerId) {
    }
}
