package com.services.crm.utils.mapper;

import com.services.crm.entity.CashSession;
import com.services.crm.entity.Expense;
import com.services.crm.entity.Sale;
import com.services.crm.entity.SaleItem;
import com.services.crm.entity.StoreProduct;
import com.services.crm.entity.Supplier;
import com.services.crm.entity.Tenant;
import com.services.crm.entity.User;
import com.services.crm.entity.UserTenant;
import com.services.crm.models.response.CashSessionDTO;
import com.services.crm.models.response.ExpenseDTO;
import com.services.crm.models.response.ProductDTO;
import com.services.crm.models.response.SaleDTO;
import com.services.crm.models.response.SaleItemDTO;
import com.services.crm.models.response.SupplierDTO;
import com.services.crm.models.response.TenantDTO;
import com.services.crm.models.response.UserDTO;
import com.services.crm.models.response.UserTenantDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mapper MapStruct para convertir entidades JPA a DTOs.
 * - Nunca expone entidades directamente (regla AGENTS.md)
 * - Convierte UUID a String automáticamente via mapUuid()
 * - Enums a String automáticamente vía mapEnum()
 */
@Mapper(componentModel = "spring")
public interface EntityMapper {

    /** Instancia singleton para uso en servicios que no tienen inyección */
    EntityMapper INSTANCE = Mappers.getMapper(EntityMapper.class);

    // ─── Conversiones automáticas ──────────────────────────────────────

    /** UUID → String. MapStruct lo detecta automáticamente. */
    default String mapUuid(UUID value) {
        return value != null ? value.toString() : null;
    }

    /** Enum → String. MapStruct lo usa para plan, subscriptionStatus, etc. */
    default String mapEnum(Enum<?> value) {
        return value != null ? value.name() : null;
    }

    // ─── User ───────────────────────────────────────────────────────────
    UserDTO toDto(User user);

    // ─── Tenant ─────────────────────────────────────────────────────────
    /**
     * Convierte Tenant → TenantDTO.
     * plan (TenantPlan enum) y subscriptionStatus (SubscriptionStatus enum)
     * se convierten a String automáticamente via mapEnum().
     */
    TenantDTO toDto(Tenant tenant);

    // ─── CashSession ────────────────────────────────────────────────────
    CashSessionDTO toDto(CashSession session);

    // ─── Product ────────────────────────────────────────────────────────
    /**
     * Convierte StoreProduct → ProductDTO.
     * name y upc vienen de upcCatalog. category y description se ignoran.
     * status se calcula desde stock y minStock via calculateStockStatus().
     */
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "status", source = ".", qualifiedByName = "calculateStockStatus")
    ProductDTO toDto(StoreProduct product);

    /**
     * Calcula el status de stock según reglas de negocio:
     * - CRITICO → stock ≤ minStock
     * - BAJO → minStock < stock ≤ minStock*2
     * - OK → stock > minStock*2
     */
    @Named("calculateStockStatus")
    default String calculateStockStatus(StoreProduct product) {
        if (product.getStock() == null || product.getMinStock() == null) {
            return "OK";
        }
        BigDecimal stock = product.getStock();
        BigDecimal min = product.getMinStock();
        if (stock.compareTo(min) <= 0) return "CRITICO";
        if (stock.compareTo(min.multiply(BigDecimal.valueOf(2))) <= 0) return "BAJO";
        return "OK";
    }

    // ─── Supplier ───────────────────────────────────────────────────────
    SupplierDTO toDto(Supplier supplier);

    // ─── Expense ────────────────────────────────────────────────────────
    @Mapping(target = "cashSessionId", source = "cashSession.id")
    ExpenseDTO toDto(Expense expense);

    // ─── Sale ───────────────────────────────────────────────────────────
    @Mapping(target = "cashSessionId", source = "cashSession.id")
    SaleDTO toDto(Sale sale);

    // ─── Caja (Cash Session) ───────────────────────────────────────────────────────
    @Mapping(target = "upc", source = "upcCatalog.upc")
    SaleItemDTO toDto(SaleItem saleItem);

    // ─── UserTenant ──────────────────────────────────────────────────────────────
    @Mapping(source = "tenant", target = "tenant")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "createdAt", target = "joinedAt")
    UserTenantDTO toDto(UserTenant userTenant);
}
