package com.services.crm.repository;

import com.services.crm.entity.UpcCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio del catálogo global de productos (UPC).
 * Recurso compartido entre todos los tenants.
 *
 * Estrategia:
 * - Lectura (lookup) → query ligera, solo trae upc y nombre
 * - Escritura (crear producto) → entidad completa
 */
@Repository
public interface UpcCatalogRepository extends JpaRepository<UpcCatalog, String> {

    /**
     * Busca producto por UPC — ENTIDAD completa.
     * Necesario para operaciones de escritura (crear StoreProduct, SaleItem).
     */
    Optional<UpcCatalog> findByUpc(String upc);

    /**
     * Verifica si un UPC existe — query booleana, no carga nada.
     */
    boolean existsByUpc(String upc);

    /**
     * Busca solo upc y nombre — para lookup donde no necesitamos la entidad
     * completa.
     * Retorna Object[] con [0]=upc, [1]=nombre para evitar cargar
     * marca, measurementUnit, isActive, regBorrado, timestamps.
     */
    @Query("SELECT u.upc, u.nombre FROM UpcCatalog u WHERE u.upc = :upc")
    Optional<Object[]> findUpcAndNombreByUpc(@Param("upc") String upc);

    /**
     * Búsqueda en tiempo real por UPC o Nombre en el catálogo global.
     */
    @Query("SELECT u FROM UpcCatalog u WHERE u.upc LIKE CONCAT('%', :term, '%') OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :term, '%'))")
    java.util.List<UpcCatalog> searchGlobal(@Param("term") String term,
            org.springframework.data.domain.Pageable pageable);
}
