package com.services.crm.service;

import com.services.crm.entity.*;
import com.services.crm.exception.BusinessException;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.request.SaleItemRequest;
import com.services.crm.models.request.SaleRequest;
import com.services.crm.models.response.SaleDTO;
import com.services.crm.models.response.SaleItemDTO;
import com.services.crm.repository.*;
import com.services.crm.utils.mapper.EntityMapper;
import com.services.crm.enums.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final StoreProductRepository storeProductRepository;
    private final CashSessionRepository cashSessionRepository;
    private final FolioService folioService;
    private final UpcCatalogRepository upcCatalogRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public SaleDTO createSale(User user, com.services.crm.entity.Tenant tenant, SaleRequest request) {
        // 1. Validar sesión de caja
        CashSession session = cashSessionRepository.findById(request.cashSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada"));

        if (session.getClosedAt() != null) {
            throw new BusinessException("SESSION_CLOSED", "La sesión de caja ya está cerrada", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // 2. Generar folio usando FolioService
        String folio = folioService.siguiente(tenant.getId(), "SAL");

        // 3. Crear cabecera de venta
        Sale sale = Sale.builder()
                .tenant(tenant)
                .user(user)
                .cashSession(session)
                .folio(folio)
                .paymentType(PaymentType.valueOf(request.paymentType()))
                .total(BigDecimal.ZERO)
                .build();

        sale = saleRepository.save(sale);

        BigDecimal total = BigDecimal.ZERO;
        List<SaleItem> items = new ArrayList<>();

        // Buscar cada producto por UPC dentro del tenant
        for (SaleItemRequest itemReq : request.items()) {
            StoreProduct product = storeProductRepository
                    .findByTenantIdAndUpcAndRegBorrado(
                            tenant.getId(), itemReq.upc(), 1)
                    .orElseGet(() -> {
                        // 1. Si no existe en la tienda, buscar o crearlo en el catálogo global
                        UpcCatalog catalog = upcCatalogRepository.findByUpc(itemReq.upc())
                                .orElseGet(() -> upcCatalogRepository.save(
                                        UpcCatalog.builder()
                                                .upc(itemReq.upc())
                                                .nombre("Art. " + itemReq.upc()) // Nombre por defecto
                                                .build()));

                        // 2. Crear StoreProduct localmente para permitir la venta
                        StoreProduct newProduct = new StoreProduct();
                        newProduct.setTenant(tenant);
                        newProduct.setUpc(catalog.getUpc());
                        newProduct.setName(catalog.getNombre());
                        if (catalog.getMeasurementUnit() != null) {
                            newProduct.setMeasurementUnit(catalog.getMeasurementUnit().name());
                        }
                        newProduct.setPrice(itemReq.unitPrice());
                        // Iniciamos en 0. Al vender, bajará a negativo.
                        newProduct.setStock(BigDecimal.ZERO);
                        newProduct.setRegBorrado(1);
                        newProduct.setIsActive(true);
                        return storeProductRepository.save(newProduct);
                    });

            BigDecimal quantity = itemReq.quantity();
            if (quantity.compareTo(BigDecimal.ZERO) <= 0 || itemReq.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("INVALID_ITEM", "La cantidad y precio unitario deben ser mayores a cero", org.springframework.http.HttpStatus.BAD_REQUEST);
            }
            
            // RELAJAR VALIDACIÓN DE STOCK: "lo importante es procesar la venta"
            // Dejamos que el stock quede en negativo en lugar de bloquear al comercio
            // if (product.getStock().compareTo(quantity) < 0) { ... }

            // Reducir stock
            product.setStock(product.getStock().subtract(quantity));
            storeProductRepository.save(product);

            // Obtener el catálogo original para el SaleItem
            UpcCatalog catalogItem = upcCatalogRepository.findByUpc(product.getUpc())
                    .orElseGet(() -> upcCatalogRepository.save(
                             UpcCatalog.builder()
                                    .upc(product.getUpc())
                                    .nombre(product.getName() != null ? product.getName() : "Art. " + product.getUpc())
                                    .build()));

            // Crear item de venta
            BigDecimal subtotal = itemReq.unitPrice().multiply(quantity);
            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .upcCatalog(catalogItem)
                    .productName(product.getName())
                    .measurementUnit(catalogItem.getMeasurementUnit())
                    .quantity(quantity)
                    .unitPrice(itemReq.unitPrice())
                    .subtotal(subtotal)
                    .build();

            items.add(item);
            total = total.add(subtotal);
        }

        saleItemRepository.saveAll(items);
        sale.setTotal(total);
        
        // 4. Actualizar total efectivo si aplica
        if (sale.getPaymentType() == PaymentType.CASH) {
            session.setCashTotal(session.getCashTotal().add(total));
        }

        // Siempre actualizar totalSales con la venta
        session.setTotalSales(session.getTotalSales().add(total));
        cashSessionRepository.save(session);

        return entityMapper.toDto(saleRepository.save(sale));
    }

    /**
     * Lista ventas del tenant, opcionalmente filtradas por sesión de caja.
     * Usa proyección JPQL directa a SaleDTO — no carga entidades Sale.
     * Items = null en listado (se obtienen con getItems por separado).
     *
     * @param tenantId UUID del tenant
     * @param cashSessionId filtro opcional por sesión de caja
     * @return lista de SaleDTO sin items (items = null)
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> findAll(UUID tenantId, UUID cashSessionId) {
        if (cashSessionId != null) {
            // Proyección directa — solo los 6 campos que el frontend necesita
            return saleRepository.findAllDtoByCashSessionId(cashSessionId);
        }
        return saleRepository.findAllDtoByTenantId(tenantId);
    }

    /**
     * Obtiene los items de una venta — proyección JPQL directa a SaleItemDTO.
     * No carga entidad SaleItem ni UpcCatalog completo.
     * Solo trae: id, upc, productName, measurementUnit, quantity, unitPrice, subtotal.
     *
     * @param saleId UUID de la venta
     * @return lista de SaleItemDTO
     */
    @Transactional(readOnly = true)
    public List<SaleItemDTO> getItems(UUID saleId) {
        return saleItemRepository.findAllDtoBySaleId(saleId);
    }
}
