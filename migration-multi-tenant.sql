-- Migración a Multi-Tenant (Múltiples sucursales por usuario)

-- 1. Crear tabla intermedia user_tenant
CREATE TABLE user_tenant (
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_user_tenant PRIMARY KEY (user_id, tenant_id),
    CONSTRAINT fk_user_tenant_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_tenant_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);

-- Índices para búsqueda rápida
CREATE INDEX idx_user_tenant_user ON user_tenant(user_id);
CREATE INDEX idx_user_tenant_tenant ON user_tenant(tenant_id);

-- 2. Migrar los datos existentes
-- Inserta la relación actual asegurando que todo usuario existente mantenga acceso a su tienda actual
INSERT INTO user_tenant (user_id, tenant_id, role, created_at)
SELECT id, tenant_id, role, CURRENT_TIMESTAMP
FROM users
WHERE tenant_id IS NOT NULL;

-- 3. Limpieza futura (OPCIONAL y MANUAL)
-- NO EJECUTAR hasta que el código Java esté desplegado, funcionando,
-- y se confirme que ya no se usan las columnas viejas.
/*
ALTER TABLE users DROP CONSTRAINT fk_users_tenant; -- (Reemplazar fk_users_tenant por el nombre real de tu constraint si existe)
ALTER TABLE users DROP COLUMN tenant_id;
ALTER TABLE users DROP COLUMN role;
*/
