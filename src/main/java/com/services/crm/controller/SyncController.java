package com.services.crm.controller;

import org.springframework.web.bind.annotation.*;
import com.services.crm.models.request.SyncRequest;
import com.services.crm.service.SyncService;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {

        private final SyncService syncService;
        private final ObjectMapper objectMapper;

        // Endpoint de prueba - Verificar que todo funciona
        @GetMapping("/ping")
        public ResponseEntity<?> ping() {
                return ResponseEntity.ok(Map.of(
                                "ok", true,
                                "message", "Sync endpoint funcionando",
                                "timestamp", OffsetDateTime.now().toString()));
        }

        @PostMapping("/suppliers")
        public ResponseEntity<?> pushSuppliers(@RequestBody SyncRequest request) {
                log.info("📥 Recibiendo PUSH de suppliers: {} registros",
                                request.getRecords().size());

                // Escribir el payload íntegro en un archivo de texto .log
                logSyncPayload(request);

                int processed = syncService.processSuppliers(
                                request.getTenantId(),
                                request.getRecords());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "processed", processed,
                                "total", request.getRecords().size()));
        }

        @PostMapping("/cash-sessions")
        public ResponseEntity<?> pushCashSessions(@RequestBody SyncRequest request) {
                log.info("📥 Recibiendo PUSH de cash_sessions: {} registros",
                                request.getRecords().size());

                logSyncPayload(request);

                int processed = syncService.processCashSessions(
                                request.getTenantId(),
                                request.getRecords());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "processed", processed,
                                "total", request.getRecords().size()));
        }

        @PostMapping("/expenses")
        public ResponseEntity<?> pushExpenses(@RequestBody SyncRequest request) {
                log.info("📥 Recibiendo PUSH de expenses: {} registros",
                                request.getRecords().size());

                logSyncPayload(request);

                int processed = syncService.processExpenses(
                                request.getTenantId(),
                                request.getRecords());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "processed", processed,
                                "total", request.getRecords().size()));
        }

        @PostMapping("/supplier-transactions")
        public ResponseEntity<?> pushSupplierTransactions(@RequestBody SyncRequest request) {
                log.info("📥 Recibiendo PUSH de supplier_transactions: {} registros",
                                request.getRecords().size());

                logSyncPayload(request);

                int processed = syncService.processSupplierTransactions(
                                request.getTenantId(),
                                request.getRecords());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "processed", processed,
                                "total", request.getRecords().size()));
        }

        @PostMapping("/sales")
        public ResponseEntity<?> pushSales(@RequestBody SyncRequest request) {
                log.info("📥 Recibiendo PUSH de sales: {} registros",
                                request.getRecords().size());

                logSyncPayload(request);

                int processed = syncService.processSales(
                                request.getTenantId(),
                                request.getRecords());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "processed", processed,
                                "total", request.getRecords().size()));
        }

        @PostMapping("/registro-folios")
        public ResponseEntity<?> pushRegistroFolios(@RequestBody SyncRequest request) {
                log.info("📥 Recibiendo PUSH de registro_folios: {} registros",
                                request.getRecords().size());

                logSyncPayload(request);

                int processed = syncService.processRegistroFolios(
                                request.getTenantId(),
                                request.getRecords());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "processed", processed,
                                "total", request.getRecords().size()));
        }

        @PostMapping("/store-products")
        public ResponseEntity<?> pushStoreProducts(@RequestBody SyncRequest request) {
                log.info("📥 Recibiendo PUSH de store_products: {} registros",
                                request.getRecords().size());

                logSyncPayload(request);

                int processed = syncService.processStoreProducts(
                                request.getTenantId(),
                                request.getRecords());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "processed", processed,
                                "total", request.getRecords().size()));
        }

        @PostMapping("/reception-items")
        public ResponseEntity<?> pushReceptionItems(@RequestBody SyncRequest request) {
                log.info("📥 Recibiendo PUSH de reception_items: {} registros",
                                request.getRecords().size());

                logSyncPayload(request);

                int processed = syncService.processReceptionItems(
                                request.getTenantId(),
                                request.getRecords());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "processed", processed,
                                "total", request.getRecords().size()));
        }

        /**
         * Guarda el payload recibido en el archivo sync_payload.log para revisarlo
         * fácilmente
         */
        private void logSyncPayload(SyncRequest request) {
                try {
                        // Convertir la petición en un formato JSON legible y formateado
                        String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);

                        // Construir el bloque que escribiremos en el TXT/.log
                        String logEntry = "=========== [ " + OffsetDateTime.now() + " ] ===========\n"
                                        + "TENANT_ID: " + request.getTenantId() + "\n"
                                        + "RECORDS COUNT: "
                                        + (request.getRecords() != null ? request.getRecords().size() : 0) + "\n"
                                        + "PAYLOAD: \n" + jsonPayload + "\n\n";

                        // El archivo se creará en la raíz del proyecto (donde ejecutas el mvn)
                        Path logFilePath = Paths.get("sync_requests.log");

                        // Añadir al archivo o crearlo si no existe
                        Files.writeString(logFilePath, logEntry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                        log.info("✅ Payload guardado exitosamente en {}", logFilePath.toAbsolutePath());
                } catch (Exception e) {
                        log.error("❌ Ocurrió un error guardando el archivo .log de sincronización", e);
                }
        }
}