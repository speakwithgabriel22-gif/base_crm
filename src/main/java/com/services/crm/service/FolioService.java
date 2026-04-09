package com.services.crm.service;

import com.services.crm.entity.RegistroFolio;
import com.services.crm.repository.RegistroFolioRepository;
import com.services.crm.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolioService {

    private final RegistroFolioRepository repository;
    private final TenantRepository tenantRepository;

    /**
     * Genera el siguiente folio para un tenant y tipo dados.
     * Ejemplo de resultado: "COT-20250404-0001"
     *
     * Es thread-safe gracias al PESSIMISTIC_WRITE lock.
     * Debe ejecutarse dentro de una transacción activa.
     */
    @Transactional
    public String siguiente(UUID tenantId, String tipo) {
        LocalDate hoy = LocalDate.now();

        RegistroFolio registro = repository
                .findForUpdate(tenantId, tipo, hoy)
                .orElseGet(() -> {
                    RegistroFolio nuevo = new RegistroFolio();
                    nuevo.setTenant(tenantRepository.getReferenceById(tenantId));
                    nuevo.setTipo(tipo);
                    nuevo.setFecha(hoy);
                    nuevo.setUltimoFolio(0);
                    return nuevo;
                });

        registro.setUltimoFolio(registro.getUltimoFolio() + 1);
        repository.save(registro);

        // COT-20250404-0001
        return String.format("%s-%s-%04d",
                tipo,
                hoy.toString().replace("-", ""),
                registro.getUltimoFolio());
    }
}
