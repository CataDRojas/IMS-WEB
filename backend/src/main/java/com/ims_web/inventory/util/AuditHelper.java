package com.ims_web.inventory.util;

import java.time.LocalDateTime;

public class AuditHelper {

    public static <T extends Auditable> void setCreationAudit(T entity, String currentUser) {
        LocalDateTime now = LocalDateTime.now();
        entity.setUsuarioCreacion(currentUser);
        entity.setFechaCreacion(now);
    }

    public static <T extends Auditable> void setModificationAudit(T entity, String currentUser) {
        LocalDateTime now = LocalDateTime.now();
        entity.setUsuarioModif(currentUser);
        entity.setFechaModif(now);
    }
}