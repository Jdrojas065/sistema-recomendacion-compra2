package com.example.recomendation.controller;

import com.example.recomendation.exception.BusinessException;
import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.entity.Sale;
import com.example.recomendation.model.form.SaleCartFormDto;
import com.example.recomendation.model.repository.ProductRepository;
import com.example.recomendation.model.service.interfaces.SaleService;
import com.example.recomendation.web.SaleCartSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final ProductRepository productRepository;
    private final SaleCartSession cartSession;

    @GetMapping("/new")
    public String newSale(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("cartItems", cartSession.getItems());
        model.addAttribute("cartTotal", cartSession.getTotal());
        return "sales/registrar";
    }

    // Página con la cámara para escanear código de barras desde el celular.
    @GetMapping("/scan")
    public String scanPage() {
        return "sales/escanear";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                             @RequestParam Integer quantity,
                             RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        if (quantity == null || quantity <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "La cantidad debe ser mayor que 0");
            return "redirect:/sales/new";
        }
        if (product.getCurrentStock() < quantity) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Stock insuficiente para '" + product.getName() + "' (disponible: " + product.getCurrentStock() + ")");
            return "redirect:/sales/new";
        }

        cartSession.addItem(buildCartItem(product, quantity));
        return "redirect:/sales/new";
    }

    // Versión JSON del mismo "agregar por código de barras", pensada para
    // ser llamada desde la página de escaneo (fetch), sin recargar la página.
    @PostMapping("/api/cart/add-by-barcode")
    @ResponseBody
    public ResponseEntity<ScanResponse> addToCartByBarcodeApi(@RequestBody ScanRequest request) {
        Product product = productRepository.findByBarcodeAndActiveTrue(request.barcode()).orElse(null);

        if (product == null) {
            return ResponseEntity.ok(new ScanResponse(false, "No se encontró ningún producto con ese código", null, cartSummary()));
        }
        int quantity = (request.quantity() == null || request.quantity() <= 0) ? 1 : request.quantity();

        if (product.getCurrentStock() < quantity) {
            return ResponseEntity.ok(new ScanResponse(false,
                    "Stock insuficiente para '" + product.getName() + "' (disponible: " + product.getCurrentStock() + ")",
                    product.getName(), cartSummary()));
        }

        cartSession.addItem(buildCartItem(product, quantity));
        return ResponseEntity.ok(new ScanResponse(true, "Agregado: " + product.getName(), product.getName(), cartSummary()));
    }

    @PostMapping("/cart/remove/{index}")
    public String removeFromCart(@PathVariable int index) {
        cartSession.removeItem(index);
        return "redirect:/sales/new";
    }

    @PostMapping
    public String confirmSale(Authentication authentication, RedirectAttributes redirectAttributes) {
        Sale sale = saleService.confirmSale(cartSession.getItems(), authentication.getName());
        cartSession.clear();
        redirectAttributes.addFlashAttribute("successMessage", "Venta registrada correctamente");
        return "redirect:/sales/" + sale.getId();
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) Long productId,
                        @RequestParam(required = false)
                        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                        LocalDate startDate,
                        @RequestParam(required = false)
                        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                        LocalDate endDate,
                        Model model) {
        model.addAttribute("salePage", saleService.findAll(PageRequest.of(page, size), productId, startDate, endDate));
        return "sales/listar";
    }

    @GetMapping("/my-sales")
    public String mySales(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           Authentication authentication,
                           Model model) {
        model.addAttribute("salePage", saleService.findByUser(authentication.getName(), PageRequest.of(page, size)));
        return "sales/my-sales";
    }
    @GetMapping("/reports")
    public String reports(Model model) {
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        int currentYear = java.time.LocalDate.now().getYear();

        model.addAttribute("monthlySales", saleService.getMonthlySales(currentMonth));
        model.addAttribute("annualSales", saleService.getAnnualSales(currentYear));
        model.addAttribute("currentMonthLabel", currentMonth.getMonthValue() + "/" + currentMonth.getYear());
        model.addAttribute("currentYear", currentYear);
        return "sales/reportes";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication authentication, Model model) {
        Sale sale = saleService.findById(id);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!saleService.canView(sale, authentication.getName(), isAdmin)) {
            throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para ver esta venta");
        }
        model.addAttribute("sale", sale);
        return "sales/ver";
    }

    private SaleCartFormDto buildCartItem(Product product, int quantity) {
        SaleCartFormDto item = new SaleCartFormDto();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private CartSummary cartSummary() {
        List<SaleCartFormDto> items = cartSession.getItems();
        return new CartSummary(items.size(), cartSession.getTotal());
    }

    // --- DTOs internos solo para este endpoint JSON ---
    public record ScanRequest(String barcode, Integer quantity) {}
    public record CartSummary(int itemCount, BigDecimal total) {}
    public record ScanResponse(boolean success, String message, String productName, CartSummary cart) {}
}