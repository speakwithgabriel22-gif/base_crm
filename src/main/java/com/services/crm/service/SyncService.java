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
                log.error("❌ Error procesando supplier {}: {}",
                        record.get("id"), e.getMessage());
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

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

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
                log.error("❌ Error procesando sale {}: {}",
                        record.get("id"), e.getMessage());
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
                processSingleSaleItem(sale, itemRecord);
            }
        }

        // 3. Actualizar totales de la sesión de caja
        updateCashSessionTotals(cashSession, sale);
    }

    private void processSingleSaleItem(Sale sale, Map<String, Object> record) {
        UUID itemId = toUUID(record.get("id"));
        String upc = (String) record.get("upc");

        // 🆕 Asegurar que UpcCatalog existe
        UpcCatalog upcCatalog = upcCatalogRepository.findById(upc)
                .orElseGet(() -> {
                    log.info("🆕 Creando UpcCatalog para UPC: {}", upc);
                    UpcCatalog newCatalog = UpcCatalog.builder()
                            .upc(upc)
                            .nombre((String) record.get("product_name"))
                            .measurementUnit(MeasurementUnit.valueOf((String) record.get("measurement_unit")))
                            .build();
                    return upcCatalogRepository.save(newCatalog);
                });

        SaleItem item = saleItemRepository.findById(itemId)
                .orElseGet(() -> SaleItem.builder()
                        .id(itemId)
                        .sale(sale)
                        .build());

        item.setUpcCatalog(upcCatalog); // ✅ Asignar el catálogo
        item.setProductName((String) record.get("product_name"));
        item.setMeasurementUnit(MeasurementUnit.valueOf((String) record.get("measurement_unit")));
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

        // Actualizar stock
        Tenant tenant = sale.getTenant();
        updateStoreProductStock(tenant, upc, item.getQuantity().negate());
    }

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
                log.error("❌ Error procesando registro_folio {}: {}",
                        record.get("id"), e.getMessage());
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

        // 🆕 Buscar por tenant, tipo y fecha (NO por ID)
        Optional<RegistroFolio> existing = registroFolioRepository
                .findByTenantIdAndTipoAndFecha(tenant.getId(), tipo, fecha);

        RegistroFolio folio;
        if (existing.isPresent()) {
            folio = existing.get();
            // Solo actualizar si el folio enviado es mayor
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

    private void updateStoreProductStock(Tenant tenant, String upc, BigDecimal delta) {
        StoreProduct product = storeProductRepository
                .findByTenantIdAndUpcAndRegBorrado(tenant.getId(), upc, 1) // ✅ Usar upc directo
                .orElseGet(() -> {
                    // Si no existe, crearlo
                    return StoreProduct.builder()
                            .tenant(tenant)
                            .upc(upc)
                            .name("Producto " + upc) // Nombre temporal
                            .measurementUnit("PZA") // Unidad por defecto
                            .price(BigDecimal.ZERO)
                            .stock(BigDecimal.ZERO)
                            .minStock(BigDecimal.valueOf(3))
                            .productType("STANDARD")
                            .isActive(true)
                            .regBorrado(1)
                            .createdAt(OffsetDateTime.now())
                            .build();
                });

        product.setStock(product.getStock().add(delta));
        product.setUpdatedAt(OffsetDateTime.now());
        storeProductRepository.save(product);
    }

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
                log.error("❌ Error procesando store_product {}: {}",
                        record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Store products procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleStoreProduct(Tenant tenant, Map<String, Object> record) {
        UUID id = toUUID(record.get("id"));

        StoreProduct product = storeProductRepository
                .findByTenantIdAndUpcAndRegBorrado(tenant.getId(), (String) record.get("upc"), 1)
                .orElseGet(() -> StoreProduct.builder()
                        .id(id)
                        .tenant(tenant)
                        .createdAt(OffsetDateTime.now())
                        .build());

        // Asegurar UPC en catálogo
        String upc = (String) record.get("upc");
        upcCatalogRepository.findById(upc)
                .orElseGet(() -> {
                    UpcCatalog newCatalog = UpcCatalog.builder()
                            .upc(upc)
                            .nombre((String) record.get("name"))
                            .measurementUnit(MeasurementUnit.valueOf((String) record.get("measurement_unit")))
                            .build();
                    return upcCatalogRepository.save(newCatalog);
                });

        product.setUpc(upc);
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

        product.setIsActive((Boolean) record.get("is_active"));
        product.setRegBorrado((Integer) record.get("reg_borrado"));
        product.setUpdatedAt(OffsetDateTime.now());

        storeProductRepository.save(product);
        log.debug("   ✅ StoreProduct guardado: {} - ${}", product.getName(), product.getPrice());
    }

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
                log.error("❌ Error procesando supplier_transaction {}: {}",
                        record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Supplier transactions procesados: {}/{}", successCount, records.size());
        return successCount;
    }

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
                log.error("❌ Error procesando reception_item {}: {}",
                        record.get("id"), e.getMessage());
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

        // Transacción padre (debe existir)
        UUID transactionId = toUUID(record.get("transaction_id"));
        SupplierTransaction transaction = supplierTransactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new RuntimeException("SupplierTransaction no encontrada: " + transactionId));
        item.setTransaction(transaction);

        // UPC Catalog
        String upc = (String) record.get("upc");
        UpcCatalog upcCatalog = upcCatalogRepository.findById(upc)
                .orElseGet(() -> {
                    UpcCatalog newCatalog = UpcCatalog.builder()
                            .upc(upc)
                            .nombre((String) record.get("product_name"))
                            .measurementUnit(MeasurementUnit.PZA)
                            .build();
                    return upcCatalogRepository.save(newCatalog);
                });
        item.setUpcCatalog(upcCatalog);

        // Datos del item
        item.setQuantity(Integer.parseInt(record.get("quantity").toString()));
        item.setCostPrice(new BigDecimal(record.get("cost_price").toString()));
        item.setSubtotal(new BigDecimal(record.get("subtotal").toString()));

        receptionItemRepository.save(item);
        log.debug("   ✅ ReceptionItem guardado: {} - cantidad: {}", upc, item.getQuantity());
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

        // Proveedor (obligatorio)
        UUID supplierId = toUUID(record.get("supplier_id"));
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier no encontrado: " + supplierId));
        transaction.setSupplier(supplier);

        // Sesión de caja (opcional, solo para PAYMENT)
        UUID cashSessionId = toUUID(record.get("cash_session_id"));
        if (cashSessionId != null) {
            CashSession cashSession = cashSessionRepository
                    .findById(cashSessionId)
                    .orElse(null);
            transaction.setCashSession(cashSession);
        }

        // Tipo de transacción: PAYMENT o RECEPTION
        String type = (String) record.get("type");
        transaction.setType(TransactionType.valueOf(type));

        // Monto
        transaction.setAmount(new BigDecimal(record.get("amount").toString()));

        // Nota opcional
        transaction.setNote((String) record.get("note"));

        // Soft delete
        Object regBorrado = record.get("reg_borrado");
        transaction.setRegBorrado(regBorrado != null ? (Integer) regBorrado : 1);

        // Fechas
        if (record.get("created_at") != null) {
            transaction.setCreatedAt(OffsetDateTime.parse((String) record.get("created_at")));
        }
        transaction.setUpdatedAt(OffsetDateTime.now());

        supplierTransactionRepository.save(transaction);
        log.debug("   ✅ Supplier transaction guardado: {} - {} - ${}",
                transaction.getType(), transaction.getSupplier().getName(), transaction.getAmount());
    }

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
                log.error("❌ Error procesando expense {}: {}",
                        record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Expenses procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleExpense(Tenant tenant, Map<String, Object> record) {
        UUID id = toUUID(record.get("id"));

        // Buscar o crear el gasto
        Expense expense = expenseRepository.findById(id)
                .orElseGet(() -> Expense.builder()
                        .id(id)
                        .tenant(tenant)
                        .build());

        // Asignar sesión de caja (puede ser null)
        UUID cashSessionId = toUUID(record.get("cash_session_id"));
        if (cashSessionId != null) {
            CashSession cashSession = cashSessionRepository
                    .findById(cashSessionId)
                    .orElse(null);
            expense.setCashSession(cashSession);
        }

        // Mapear campos
        expense.setCategory((String) record.get("category"));
        expense.setDescription((String) record.get("description"));
        expense.setNote((String) record.get("note"));
        expense.setAmount(new BigDecimal(record.get("amount").toString()));

        // Método de pago
        String paymentMethod = (String) record.get("payment_method");
        expense.setPaymentMethod(paymentMethod != null ? PaymentType.valueOf(paymentMethod) : PaymentType.CASH);

        // Soft delete
        Object regBorrado = record.get("reg_borrado");
        expense.setRegBorrado(regBorrado != null ? (Integer) regBorrado : 1);

        // Fechas
        if (record.get("created_at") != null) {
            expense.setCreatedAt(OffsetDateTime.parse((String) record.get("created_at")));
        }
        expense.setUpdatedAt(OffsetDateTime.now());

        expenseRepository.save(expense);
        log.debug("   ✅ Expense guardado: {} - ${}",
                expense.getDescription(), expense.getAmount());
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

    private UUID toUUID(Object value) {
        if (value == null)
            return null;
        String str = value.toString().trim();
        if (str.isEmpty())
            return null;
        return UUID.fromString(str);
    }

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
                log.error("❌ Error procesando cash_session {}: {}",
                        record.get("id"), e.getMessage());
            }
        }

        log.info("✅ Cash sessions procesados: {}/{}", successCount, records.size());
        return successCount;
    }

    private void processSingleCashSession(Tenant tenant, Map<String, Object> record) {
        UUID id = toUUID(record.get("id"));
        CashSession session = cashSessionRepository.findById(id)
                .orElseGet(() -> CashSession.builder()
                        .id(id)
                        .tenant(tenant)
                        .build());

        session.setInitialAmount(new BigDecimal(record.get("initial_amount").toString()));
        session.setTotalSales(new BigDecimal(record.get("total_sales").toString()));
        session.setCashTotal(new BigDecimal(record.get("cash_total").toString()));
        session.setCardTotal(new BigDecimal(record.get("card_total").toString()));
        session.setTransferTotal(new BigDecimal(record.get("transfer_total").toString()));
        session.setOpenedAt(OffsetDateTime.parse((String) record.get("opened_at")));

        if (record.get("closed_at") != null) {
            session.setClosedAt(OffsetDateTime.parse((String) record.get("closed_at")));
        } else {
            // 🆕 Validar regla de negocio: "Solo una caja abierta por tenant"
            // Si la sesión que llega está abierta (closed_at == null),
            // verificamos que no exista otra abierta con ID diferente.
            Optional<CashSession> activeSession = cashSessionRepository
                    .findByTenantIdAndClosedAtIsNull(tenant.getId());

            if (activeSession.isPresent() && !activeSession.get().getId().equals(session.getId())) {
                log.warn("⚠️ Intento de sincronizar segunda sesión abierta para tenant {}. Saltando registro.",
                        tenant.getId());
                throw new RuntimeException("Ya existe una sesión abierta para este tenant.");
            }
            session.setClosedAt(null);
        }

        UUID userId = toUUID(record.get("user_id"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User no encontrado"));
        session.setUser(user);

        cashSessionRepository.save(session);
        log.debug("   ✅ Cash session guardada: {}", session.getId());
    }

}
