package com.services.crm.service;

import com.services.crm.entity.CashSession;
import com.services.crm.entity.Expense;
import com.services.crm.entity.ReceptionItem;
import com.services.crm.entity.RegistroFolio;
import com.services.crm.entity.Sale;
import com.services.crm.entity.SaleItem;
import com.services.crm.entity.StoreProduct;
import com.services.crm.entity.Supplier;
import com.services.crm.entity.SupplierTransaction;
import com.services.crm.entity.Tenant;
import com.services.crm.entity.UpcCatalog;
import com.services.crm.entity.User;
import com.services.crm.enums.MeasurementUnit;
import com.services.crm.enums.PaymentType;
import com.services.crm.enums.TransactionType;
import com.services.crm.repository.CashSessionRepository;
import com.services.crm.repository.ExpenseRepository;
import com.services.crm.repository.ReceptionItemRepository;
import com.services.crm.repository.RegistroFolioRepository;
import com.services.crm.repository.SaleItemRepository;
import com.services.crm.repository.SaleRepository;
import com.services.crm.repository.StoreProductRepository;
import com.services.crm.repository.SupplierRepository;
import com.services.crm.repository.SupplierTransactionRepository;
import com.services.crm.repository.TenantRepository;
import com.services.crm.repository.UpcCatalogRepository;
import com.services.crm.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final SupplierRepository supplierRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final CashSessionRepository cashSessionRepository;
    private final ExpenseRepository expenseRepository;
    private final SupplierTransactionRepository supplierTransactionRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final UpcCatalogRepository upcCatalogRepository;
    private final StoreProductRepository storeProductRepository;
    private final RegistroFolioRepository registroFolioRepository;
    private final ReceptionItemRepository receptionItemRepository;

    // ==========================================
    // SUPPLIERS
    // ==========================================

    @Transactional
    public int processSuppliers(String tenantIdStr, List<Map<String, Object>> records) {
        UUID tenantId = toUUID(tenantIdStr);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado: " + tenantIdStr));

        int successCount = 0;

        for (Map<String, Object> record : records) {
            try {
                processSingleSupplier(tenant, record);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error procesando supplier {}: {}", record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Suppliers procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleSupplier(Tenant tenant, Map<String, Object> record) {
        UUID id = toUUID(record.get("id"));
        Supplier supplier = supplierRepository.findById(id)
                .orElseGet(() -> Supplier.builder()
                        .id(id)
                        .tenant(tenant)
                        .createdAt(OffsetDateTime.now())
                        .build());

        supplier.setName(getString(record, "name"));
        supplier.setContactName(getString(record, "contact_name"));
        supplier.setPhone(getString(record, "phone"));
        supplier.setCategory(getString(record, "category"));
        supplier.setVisitDay(getString(record, "visit_day"));
        supplier.setDeliveryDay(getString(record, "delivery_day"));
        supplier.setFrequency(getString(record, "frequency"));

        Object totalDebt = record.get("total_debt");
        if (totalDebt != null) {
            supplier.setTotalDebt(new BigDecimal(totalDebt.toString()));
        }

        Object isActive = record.get("is_active");
        supplier.setIsActive(isActive != null && (int) isActive == 1);

        Object regBorrado = record.get("reg_borrado");
        supplier.setRegBorrado(regBorrado != null ? (int) regBorrado : 1);

        supplier.setUpdatedAt(OffsetDateTime.now());

        supplierRepository.save(supplier);
        log.debug("   ✅ Supplier guardado: {}", supplier.getName());
    }

    // ==========================================
    // CASH SESSIONS (UPSERT)
    // ==========================================

    @Transactional
    public int processCashSessions(String tenantIdStr, List<Map<String, Object>> records) {
        UUID tenantId = toUUID(tenantIdStr);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado"));

        int successCount = 0;

        for (Map<String, Object> record : records) {
            try {
                processSingleCashSession(tenant, record);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error procesando cash_session {}: {}", record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Cash sessions procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleCashSession(Tenant tenant, Map<String, Object> record) {
        UUID id = toUUID(record.get("id"));

        // 🆕 1. Buscar si YA existe una sesión ABIERTA para este tenant
        Optional<CashSession> existingOpen = cashSessionRepository
                .findByTenantIdAndClosedAtIsNull(tenant.getId());

        CashSession session;
        if (existingOpen.isPresent()) {
            session = existingOpen.get();
            log.debug("   🔄 Actualizando sesión existente: {}", session.getId());
        } else {
            session = cashSessionRepository.findById(id)
                    .orElseGet(() -> CashSession.builder()
                            .id(id)
                            .tenant(tenant)
                            .openedAt(OffsetDateTime.now())
                            .build());
        }

        // 2. Actualizar campos (usar valores más altos)
        updateCashSessionFields(session, record);

        cashSessionRepository.save(session);
        log.debug("   ✅ CashSession guardada: {} (abierta: {})",
                session.getId(), session.getClosedAt() == null);
    }

    private void updateCashSessionFields(CashSession session, Map<String, Object> record) {
        // Initial amount (mantener original si ya tiene)
        if (session.getInitialAmount() == null ||
                session.getInitialAmount().compareTo(BigDecimal.ZERO) == 0) {
            session.setInitialAmount(new BigDecimal(record.get("initial_amount").toString()));
        }

        // Totales (usar el más alto)
        BigDecimal newTotalSales = new BigDecimal(record.get("total_sales").toString());
        if (newTotalSales.compareTo(session.getTotalSales()) > 0) {
            session.setTotalSales(newTotalSales);
        }

        BigDecimal newCashTotal = new BigDecimal(record.get("cash_total").toString());
        if (newCashTotal.compareTo(session.getCashTotal()) > 0) {
            session.setCashTotal(newCashTotal);
        }

        BigDecimal newCardTotal = new BigDecimal(record.get("card_total").toString());
        if (newCardTotal.compareTo(session.getCardTotal()) > 0) {
            session.setCardTotal(newCardTotal);
        }

        BigDecimal newTransferTotal = new BigDecimal(record.get("transfer_total").toString());
        if (newTransferTotal.compareTo(session.getTransferTotal()) > 0) {
            session.setTransferTotal(newTransferTotal);
        }

        // Fechas
        if (session.getOpenedAt() == null && record.get("opened_at") != null) {
            session.setOpenedAt(OffsetDateTime.parse((String) record.get("opened_at")));
        }

        if (record.get("closed_at") != null) {
            session.setClosedAt(OffsetDateTime.parse((String) record.get("closed_at")));
        }

        // Usuario
        if (session.getUser() == null) {
            UUID userId = toUUID(record.get("user_id"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User no encontrado"));
            session.setUser(user);
        }
    }

    // ==========================================
    // SALES + SALE_ITEMS
    // ==========================================

    @Transactional
    public int processSales(String tenantIdStr, List<Map<String, Object>> records) {
        UUID tenantId = toUUID(tenantIdStr);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado: " + tenantIdStr));

        int successCount = 0;

        for (Map<String, Object> record : records) {
            try {
                processSingleSale(tenant, record);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error procesando sale {}: {}", record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Sales procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleSale(Tenant tenant, Map<String, Object> record) {
        UUID saleId = toUUID(record.get("id"));

        // 1. Cabecera de venta
        Sale sale = saleRepository.findById(saleId)
                .orElseGet(() -> Sale.builder()
                        .id(saleId)
                        .tenant(tenant)
                        .createdAt(OffsetDateTime.now())
                        .build());

        // Usuario (cajero)
        UUID userId = toUUID(record.get("user_id"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User no encontrado: " + userId));
        sale.setUser(user);

        // Sesión de caja
        UUID cashSessionId = toUUID(record.get("cash_session_id"));
        CashSession cashSession = cashSessionRepository
                .findById(cashSessionId)
                .orElseThrow(() -> new RuntimeException("CashSession no encontrada: " + cashSessionId));
        sale.setCashSession(cashSession);

        // Datos de venta
        sale.setFolio((String) record.get("folio"));
        sale.setTotal(new BigDecimal(record.get("total").toString()));
        sale.setPaymentType(PaymentType.valueOf((String) record.get("payment_type")));

        Object regBorrado = record.get("reg_borrado");
        sale.setRegBorrado(regBorrado != null ? (Integer) regBorrado : 1);

        if (record.get("created_at") != null) {
            sale.setCreatedAt(OffsetDateTime.parse((String) record.get("created_at")));
        }
        sale.setUpdatedAt(OffsetDateTime.now());

        sale = saleRepository.save(sale);
        log.debug("   ✅ Sale guardada: {} - ${}", sale.getFolio(), sale.getTotal());

        // 2. Procesar items de venta
        List<Map<String, Object>> items = (List<Map<String, Object>>) record.get("items");

        if (items != null) {
            for (Map<String, Object> itemRecord : items) {
                log.info("🔍 Recibido SaleItem - UPC: '{}', product_name: '{}'", itemRecord.get("upc"),
                        itemRecord.get("product_name"));
                processSingleSaleItem(sale, itemRecord);
            }
        }

        // 3. Actualizar totales de la sesión de caja
        updateCashSessionTotals(cashSession, sale);
    }

    private void processSingleSaleItem(Sale sale, Map<String, Object> record) {
        UUID itemId = toUUID(record.get("id"));
        String upc = (String) record.get("upc");
        String productName = (String) record.get("product_name");
        String measurementUnitStr = (String) record.get("measurement_unit");

        // Validar nombre
        if (productName == null || productName.trim().isEmpty()) {
            productName = "Producto " + upc;
        }

        // 1. Obtener o crear el registro de inventario (StoreProduct)
        // Este método ahora se encarga de crear el StoreProduct y vincularlo al
        // UpcCatalog si aplica
        StoreProduct storeProduct = getOrCreateStoreProductInternal(sale.getTenant(), upc, productName,
                measurementUnitStr);

        // 2. Crear o actualizar SaleItem vinculado al registro de inventario de la
        // tienda
        SaleItem item = saleItemRepository.findById(itemId)
                .orElseGet(() -> SaleItem.builder()
                        .id(itemId)
                        .sale(sale)
                        .build());

        item.setStoreProduct(storeProduct);
        item.setProductName(productName);
        item.setMeasurementUnit(parseMeasurementUnit(measurementUnitStr));
        item.setQuantity(new BigDecimal(record.get("quantity").toString()));
        item.setUnitPrice(new BigDecimal(record.get("unit_price").toString()));
        item.setSubtotal(new BigDecimal(record.get("subtotal").toString()));

        if (record.get("iva_amount") != null) {
            item.setIvaAmount(new BigDecimal(record.get("iva_amount").toString()));
        }
        if (record.get("ieps_amount") != null) {
            item.setIepsAmount(new BigDecimal(record.get("ieps_amount").toString()));
        }

        saleItemRepository.save(item);
        log.debug("   ✅ SaleItem guardado: {} - {}", upc, item.getQuantity());

        // 3. Descontar stock
        storeProduct.setStock(storeProduct.getStock().subtract(item.getQuantity()));
        storeProductRepository.save(storeProduct);
    }

    /**
     * Busca un StoreProduct por tenant y UPC. Si no existe, lo crea asegurando
     * también la existencia en UpcCatalog si es un UPC válido.
     */
    private StoreProduct getOrCreateStoreProductInternal(Tenant tenant, String upc, String name,
            String measurementUnit) {
        // A. Asegurar UpcCatalog si es válido
        final UpcCatalog finalCatalog;
        if (isValidProductUpc(upc)) {
            finalCatalog = upcCatalogRepository.findById(upc)
                    .orElseGet(() -> {
                        UpcCatalog newCatalog = UpcCatalog.builder()
                                .upc(upc)
                                .nombre(name != null ? name : "Producto " + upc)
                                .measurementUnit(parseMeasurementUnit(measurementUnit))
                                .build();
                        return upcCatalogRepository.save(newCatalog);
                    });
        } else {
            finalCatalog = null;
        }

        // B. Buscar/Crear StoreProduct
        return storeProductRepository
                .findByTenantIdAndUpcCatalogUpcAndRegBorrado(tenant.getId(), upc, 1)
                .orElseGet(() -> {
                    StoreProduct newProduct = StoreProduct.builder()
                            .id(UUID.randomUUID())
                            .tenant(tenant)
                            .upcCatalog(finalCatalog)
                            .name(name != null ? name : "Producto " + upc)
                            .measurementUnit(measurementUnit != null ? measurementUnit : "PZA")
                            .price(BigDecimal.ZERO)
                            .stock(BigDecimal.ZERO)
                            .minStock(BigDecimal.valueOf(3))
                            .productType(isValidProductUpc(upc) ? "STANDARD" : "INTERNAL")
                            .isActive(true)
                            .regBorrado(1)
                            .createdAt(OffsetDateTime.now())
                            .build();

                    if (finalCatalog == null) {
                        UpcCatalog internalCatalog = UpcCatalog.builder()
                                .upc(upc)
                                .nombre(name)
                                .measurementUnit(parseMeasurementUnit(measurementUnit))
                                .build();
                        newProduct.setUpcCatalog(upcCatalogRepository.save(internalCatalog));
                    }

                    return storeProductRepository.save(newProduct);
                });
    }

    // 🆕 Método para validar si un UPC es un código de barras estándar
    private boolean isValidProductUpc(String upc) {
        if (upc == null || upc.trim().isEmpty())
            return false;

        // UPCs internos/genéricos (los que NO deben ir a upc_catalog)
        if (upc.startsWith("INT-") || upc.startsWith("PRP-") || upc.equals("N/A")) {
            return false;
        }

        // UPC estándar: solo dígitos, 12-13 caracteres
        return upc.matches("^\\d{12,13}$");
    }

    // 🆕 Método auxiliar para parsear MeasurementUnit
    private MeasurementUnit parseMeasurementUnit(String unitStr) {
        if (unitStr == null || unitStr.trim().isEmpty()) {
            return MeasurementUnit.PZA;
        }
        try {
            return MeasurementUnit.valueOf(unitStr);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ MeasurementUnit inválido: '{}', usando PZA", unitStr);
            return MeasurementUnit.PZA;
        }
    }

    private void updateCashSessionTotals(CashSession session, Sale sale) {
        session.setTotalSales(session.getTotalSales().add(sale.getTotal()));

        switch (sale.getPaymentType()) {
            case CASH:
                session.setCashTotal(session.getCashTotal().add(sale.getTotal()));
                break;
            case CARD:
                session.setCardTotal(session.getCardTotal().add(sale.getTotal()));
                break;
            case TRANSFER:
                session.setTransferTotal(session.getTransferTotal().add(sale.getTotal()));
                break;
        }

        cashSessionRepository.save(session);
    }

    // ==========================================
    // STORE PRODUCTS
    // ==========================================

    // updateStoreProductStock removido y reemplazado por lógica en
    // getOrCreateStoreProductInternal y subtractStock

    @Transactional
    public int processStoreProducts(String tenantIdStr, List<Map<String, Object>> records) {
        UUID tenantId = toUUID(tenantIdStr);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado"));

        int successCount = 0;

        for (Map<String, Object> record : records) {
            try {
                processSingleStoreProduct(tenant, record);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error procesando store_product {}: {}", record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Store products procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleStoreProduct(Tenant tenant, Map<String, Object> record) {
        log.info("🔍 Recibido StoreProduct - UPC: '{}', name: '{}'", record.get("upc"),
                record.get("name"));

        UUID id = toUUID(record.get("id"));
        String upc = (String) record.get("upc");

        StoreProduct product = storeProductRepository
                .findByTenantIdAndUpcCatalogUpcAndRegBorrado(tenant.getId(), upc, 1)
                .orElseGet(() -> StoreProduct.builder()
                        .id(id)
                        .tenant(tenant)
                        .createdAt(OffsetDateTime.now())
                        .build());

        // Asegurar UPC en catálogo
        UpcCatalog catalog = upcCatalogRepository.findById(upc)
                .orElseGet(() -> {
                    UpcCatalog newCatalog = UpcCatalog.builder()
                            .upc(upc)
                            .nombre((String) record.get("name"))
                            .measurementUnit(parseMeasurementUnit((String) record.get("measurement_unit")))
                            .build();
                    return upcCatalogRepository.save(newCatalog);
                });

        product.setUpcCatalog(catalog);
        product.setName((String) record.get("name"));
        product.setMeasurementUnit((String) record.get("measurement_unit"));
        product.setPrice(new BigDecimal(record.get("price").toString()));
        product.setStock(new BigDecimal(record.get("stock").toString()));
        product.setMinStock(new BigDecimal(record.get("min_stock").toString()));

        if (record.get("cost_price") != null) {
            product.setCostPrice(new BigDecimal(record.get("cost_price").toString()));
        }
        if (record.get("supplier_id") != null) {
            Supplier supplier = supplierRepository.findById(toUUID(record.get("supplier_id")))
                    .orElse(null);
            product.setSupplier(supplier);
        }
        if (record.get("product_type") != null) {
            product.setProductType((String) record.get("product_type"));
        }

        Object isActive = record.get("is_active");
        product.setIsActive(
                isActive != null && (isActive instanceof Boolean ? (Boolean) isActive : (int) isActive == 1));

        Object regBorrado = record.get("reg_borrado");
        product.setRegBorrado(regBorrado != null ? (Integer) regBorrado : 1);
        product.setUpdatedAt(OffsetDateTime.now());

        storeProductRepository.save(product);
        log.debug("   ✅ StoreProduct guardado: {} - ${}", product.getName(), product.getPrice());
    }

    // ==========================================
    // REGISTRO FOLIOS
    // ==========================================

    @Transactional
    public int processRegistroFolios(String tenantIdStr, List<Map<String, Object>> records) {
        UUID tenantId = toUUID(tenantIdStr);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado: " + tenantIdStr));

        int successCount = 0;

        for (Map<String, Object> record : records) {
            try {
                processSingleRegistroFolio(tenant, record);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error procesando registro_folio {}: {}", record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Registro folios procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleRegistroFolio(Tenant tenant, Map<String, Object> record) {
        String tipo = (String) record.get("tipo");
        String fechaStr = (String) record.get("fecha");
        LocalDate fecha = LocalDate.parse(fechaStr);
        Integer ultimoFolio = (Integer) record.get("ultimo_folio");

        Optional<RegistroFolio> existing = registroFolioRepository
                .findForUpdate(tenant.getId(), tipo, fecha);

        RegistroFolio folio;
        if (existing.isPresent()) {
            folio = existing.get();
            if (ultimoFolio > folio.getUltimoFolio()) {
                folio.setUltimoFolio(ultimoFolio);
            }
        } else {
            folio = RegistroFolio.builder()
                    .id(UUID.randomUUID())
                    .tenant(tenant)
                    .tipo(tipo)
                    .fecha(fecha)
                    .ultimoFolio(ultimoFolio)
                    .build();
        }

        registroFolioRepository.save(folio);
    }

    // ==========================================
    // EXPENSES
    // ==========================================

    @Transactional
    public int processExpenses(String tenantIdStr, List<Map<String, Object>> records) {
        UUID tenantId = toUUID(tenantIdStr);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado: " + tenantIdStr));

        int successCount = 0;

        for (Map<String, Object> record : records) {
            try {
                processSingleExpense(tenant, record);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error procesando expense {}: {}", record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Expenses procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleExpense(Tenant tenant, Map<String, Object> record) {
        UUID id = toUUID(record.get("id"));

        Expense expense = expenseRepository.findById(id)
                .orElseGet(() -> Expense.builder()
                        .id(id)
                        .tenant(tenant)
                        .build());

        UUID cashSessionId = toUUID(record.get("cash_session_id"));
        if (cashSessionId != null) {
            CashSession cashSession = cashSessionRepository
                    .findById(cashSessionId)
                    .orElse(null);
            expense.setCashSession(cashSession);
        }

        expense.setCategory((String) record.get("category"));
        expense.setDescription((String) record.get("description"));
        expense.setNote((String) record.get("note"));
        expense.setAmount(new BigDecimal(record.get("amount").toString()));

        String paymentMethod = (String) record.get("payment_method");
        expense.setPaymentMethod(paymentMethod != null ? PaymentType.valueOf(paymentMethod) : PaymentType.CASH);

        Object regBorrado = record.get("reg_borrado");
        expense.setRegBorrado(regBorrado != null ? (Integer) regBorrado : 1);

        if (record.get("created_at") != null) {
            expense.setCreatedAt(OffsetDateTime.parse((String) record.get("created_at")));
        }
        expense.setUpdatedAt(OffsetDateTime.now());

        expenseRepository.save(expense);
        log.debug("   ✅ Expense guardado: {} - ${}", expense.getDescription(), expense.getAmount());
    }

    // ==========================================
    // SUPPLIER TRANSACTIONS
    // ==========================================

    @Transactional
    public int processSupplierTransactions(String tenantIdStr, List<Map<String, Object>> records) {
        UUID tenantId = toUUID(tenantIdStr);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado: " + tenantIdStr));

        int successCount = 0;

        for (Map<String, Object> record : records) {
            try {
                processSingleSupplierTransaction(tenant, record);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error procesando supplier_transaction {}: {}", record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Supplier transactions procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleSupplierTransaction(Tenant tenant, Map<String, Object> record) {
        UUID id = toUUID(record.get("id"));

        SupplierTransaction transaction = supplierTransactionRepository
                .findById(id)
                .orElseGet(() -> SupplierTransaction.builder()
                        .id(id)
                        .tenant(tenant)
                        .createdAt(OffsetDateTime.now())
                        .build());

        UUID supplierId = toUUID(record.get("supplier_id"));
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier no encontrado: " + supplierId));
        transaction.setSupplier(supplier);

        UUID cashSessionId = toUUID(record.get("cash_session_id"));
        if (cashSessionId != null) {
            CashSession cashSession = cashSessionRepository
                    .findById(cashSessionId)
                    .orElse(null);
            transaction.setCashSession(cashSession);
        }

        String type = (String) record.get("type");
        transaction.setType(TransactionType.valueOf(type));

        transaction.setAmount(new BigDecimal(record.get("amount").toString()));
        transaction.setNote((String) record.get("note"));

        Object regBorrado = record.get("reg_borrado");
        transaction.setRegBorrado(regBorrado != null ? (Integer) regBorrado : 1);

        if (record.get("created_at") != null) {
            transaction.setCreatedAt(OffsetDateTime.parse((String) record.get("created_at")));
        }
        transaction.setUpdatedAt(OffsetDateTime.now());

        supplierTransactionRepository.save(transaction);
        log.debug("   ✅ Supplier transaction guardado: {} - {} - ${}",
                transaction.getType(), transaction.getSupplier().getName(), transaction.getAmount());
    }

    // ==========================================
    // RECEPTION ITEMS
    // ==========================================

    @Transactional
    public int processReceptionItems(String tenantIdStr, List<Map<String, Object>> records) {
        UUID tenantId = toUUID(tenantIdStr);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado: " + tenantIdStr));

        int successCount = 0;

        for (Map<String, Object> record : records) {
            try {
                processSingleReceptionItem(tenant, record);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error procesando reception_item {}: {}", record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Reception items procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleReceptionItem(Tenant tenant, Map<String, Object> record) {
        UUID id = toUUID(record.get("id"));

        ReceptionItem item = receptionItemRepository.findById(id)
                .orElseGet(() -> ReceptionItem.builder()
                        .id(id)
                        .build());

        UUID transactionId = toUUID(record.get("transaction_id"));
        SupplierTransaction transaction = supplierTransactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new RuntimeException("SupplierTransaction no encontrada: " + transactionId));
        item.setTransaction(transaction);

        String upc = (String) record.get("upc");
        String productName = (String) record.get("product_name");

        UpcCatalog upcCatalog = upcCatalogRepository.findById(upc)
                .orElseGet(() -> {
                    UpcCatalog newCatalog = UpcCatalog.builder()
                            .upc(upc)
                            .nombre(productName)
                            .measurementUnit(MeasurementUnit.PZA)
                            .build();
                    return upcCatalogRepository.save(newCatalog);
                });
        item.setUpcCatalog(upcCatalog);

        item.setQuantity(Integer.parseInt(record.get("quantity").toString()));
        item.setCostPrice(new BigDecimal(record.get("cost_price").toString()));
        item.setSubtotal(new BigDecimal(record.get("subtotal").toString()));

        receptionItemRepository.save(item);
        log.debug("   ✅ ReceptionItem guardado: {} - cantidad: {}", upc, item.getQuantity());
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    private UUID toUUID(Object value) {
        if (value == null)
            return null;
        String str = value.toString().trim();
        if (str.isEmpty())
            return null;
        return UUID.fromString(str);
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}