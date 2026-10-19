package com.example.recomendation.seed;

import com.example.recomendation.model.entity.*;
import com.example.recomendation.model.enums.MovementType;
import com.example.recomendation.model.enums.RiskLevel;
import com.example.recomendation.model.enums.Role;
import com.example.recomendation.model.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;
    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final AnomalyRepository anomalyRepository;
    private final PurchaseRecommendationRepository recommendationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Ya existen datos de desarrollo, se omite el seed.");
            return;
        }

        // ---------- USUARIOS ----------
        User admin = userRepository.save(User.builder()
                .name("Administrador Demo")
                .email("admin@demo.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .active(true)
                .build());

        User employee = userRepository.save(User.builder()
                .name("Empleado Demo")
                .email("empleado@demo.com")
                .password(passwordEncoder.encode("empleado123"))
                .role(Role.EMPLOYEE)
                .active(true)
                .build());

        log.warn("Usuarios de DESARROLLO creados: admin@demo.com / empleado@demo.com (contraseñas de prueba, NO usar en producción)");

        // ---------- CATEGORÍAS ----------
        Category general = categoryRepository.save(Category.builder().name("General").description("Categoría por defecto").active(true).build());
        Category abarrotes = categoryRepository.save(Category.builder().name("Abarrotes").description("Productos de abarrotes").active(true).build());
        Category limpieza = categoryRepository.save(Category.builder().name("Limpieza").description("Productos de limpieza").active(true).build());

        // ---------- PRODUCTOS ----------
        Product arroz = productRepository.save(Product.builder()
                .name("Arroz 1kg").price(new BigDecimal("4500")).category(abarrotes)
                .minimumStock(20).currentStock(0).active(true).build());

        Product aceite = productRepository.save(Product.builder()
                .name("Aceite de cocina 1L").price(new BigDecimal("9800")).category(abarrotes)
                .minimumStock(15).currentStock(0).active(true).build());

        Product azucar = productRepository.save(Product.builder()
                .name("Azúcar 1kg").price(new BigDecimal("3200")).category(abarrotes)
                .minimumStock(25).currentStock(0).active(true).build());

        Product detergente = productRepository.save(Product.builder()
                .name("Detergente en polvo 500g").price(new BigDecimal("6700")).category(limpieza)
                .minimumStock(10).currentStock(0).active(true).build());

        Product jabon = productRepository.save(Product.builder()
                .name("Jabón de baño").price(new BigDecimal("2100")).category(limpieza)
                .minimumStock(30).currentStock(0).active(true).build());

        // ---------- MOVIMIENTOS DE INVENTARIO (entradas iniciales) ----------
        registerInitialStock(arroz, 100, admin);
        registerInitialStock(aceite, 60, admin);
        registerInitialStock(azucar, 80, admin);
        registerInitialStock(detergente, 8, admin);   // deliberadamente bajo, para ver alerta de stock bajo
        registerInitialStock(jabon, 150, admin);

        log.info("Productos y stock inicial creados");

        // ---------- VENTAS DE PRUEBA (últimos ~10 días, para que el método tradicional tenga historial) ----------
        crearVenta(employee, LocalDateTime.now().minusDays(9), arroz, 3, aceite, 1);
        crearVenta(employee, LocalDateTime.now().minusDays(7), arroz, 5);
        crearVenta(admin, LocalDateTime.now().minusDays(6), azucar, 4, jabon, 10);
        crearVenta(employee, LocalDateTime.now().minusDays(4), arroz, 2, detergente, 2);
        crearVenta(employee, LocalDateTime.now().minusDays(2), aceite, 3, azucar, 6);
        crearVenta(admin, LocalDateTime.now().minusDays(1), jabon, 20);

        log.info("Ventas de prueba registradas");

        // ---------- ANOMALÍA DE EJEMPLO ----------
        anomalyRepository.save(Anomaly.builder()
                .product(jabon)
                .date(LocalDateTime.now().minusDays(1))
                .expectedValue(new BigDecimal("5"))
                .observedValue(new BigDecimal("20"))
                .deviation(new BigDecimal("3.0"))
                .severity("HIGH")
                .description("Venta de jabón muy por encima del promedio habitual (posible compra al por mayor o promoción no registrada).")
                .build());

        log.info("Anomalía de prueba creada");

        // ---------- RECOMENDACIÓN DE EJEMPLO ----------
        recommendationRepository.save(PurchaseRecommendation.builder()
                .product(detergente)
                .recommendationDate(LocalDate.now())
                .predictedDemand(new BigDecimal("12"))
                .currentStock(detergente.getCurrentStock() - 2) // refleja la venta ya registrada arriba
                .rotation("HIGH")
                .stockoutRisk(RiskLevel.HIGH)
                .recommendedQuantity(20)
                .priority(RiskLevel.HIGH)
                .reason("El stock de detergente está por debajo del mínimo y la demanda reciente lo confirma.")
                .source("TRADITIONAL")
                .status("PENDING")
                .build());

        log.info("Recomendación de prueba creada");
        log.info("=== Seed de desarrollo completado ===");
    }

    private void registerInitialStock(Product product, int quantity, User user) {
        product.setCurrentStock(product.getCurrentStock() + quantity);
        productRepository.save(product);

        movementRepository.save(InventoryMovement.builder()
                .product(product)
                .type(MovementType.IN)
                .quantity(quantity)
                .date(LocalDateTime.now().minusDays(15))
                .user(user)
                .description("Carga inicial de inventario (dato de prueba)")
                .build());
    }

    /** Venta con un solo ítem */
    private void crearVenta(User user, LocalDateTime date, Product product, int quantity) {
        crearVentaInterna(user, date, new Object[]{product, quantity});
    }

    /** Venta con dos ítems */
    private void crearVenta(User user, LocalDateTime date, Product p1, int q1, Product p2, int q2) {
        crearVentaInterna(user, date, new Object[]{p1, q1}, new Object[]{p2, q2});
    }

    @SafeVarargs
    private void crearVentaInterna(User user, LocalDateTime date, Object[]... items) {
        Sale sale = Sale.builder().user(user).date(date).total(BigDecimal.ZERO).build();
        BigDecimal total = BigDecimal.ZERO;

        Sale savedSale = saleRepository.save(sale);

        for (Object[] item : items) {
            Product productRef = (Product) item[0];
            int quantity = (int) item[1];

            // Recarga el producto para obtener la versión actual y evitar
            // optimistic locking al reutilizar la misma instancia en varias ventas
            Product product = productRepository.findById(productRef.getId())
                    .orElseThrow(() -> new IllegalStateException("Producto no encontrado: " + productRef.getId()));

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            total = total.add(subtotal);

            SaleDetail detail = SaleDetail.builder()
                    .sale(savedSale)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            saleDetailRepository.save(detail);

            product.setCurrentStock(product.getCurrentStock() - quantity);
            productRepository.save(product);

            movementRepository.save(InventoryMovement.builder()
                    .product(product)
                    .type(MovementType.OUT)
                    .quantity(quantity)
                    .date(date)
                    .user(user)
                    .description("Salida por venta (dato de prueba)")
                    .build());
        }

        savedSale.setTotal(total);
        saleRepository.save(savedSale);
    }
}