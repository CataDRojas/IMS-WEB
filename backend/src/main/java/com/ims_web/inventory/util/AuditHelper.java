package com.ims_web.inventory.util;

import java.time.LocalDateTime;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditHelper {

    private static String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "SYSTEM";
    }

    public static <T extends Auditable> void setCreationAudit(T entity, String currentUser) {
        LocalDateTime now = LocalDateTime.now();

        String userToUse = (currentUser != null) ? currentUser : getCurrentUser();

        entity.setUsuarioCreacion(userToUse);
        entity.setFechaCreacion(now);
    }

    public static <T extends Auditable> void setModificationAudit(T entity, String currentUser) {
        LocalDateTime now = LocalDateTime.now();

        String userToUse = (currentUser != null) ? currentUser : getCurrentUser();

        entity.setUsuarioModif(userToUse);
        entity.setFechaModif(now);
    }
}