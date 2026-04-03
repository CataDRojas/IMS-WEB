package com.ims_web.inventory.util;

import java.time.LocalDateTime;

public interface Auditable {
    // Creation
    void setUsuarioCreacion(String usuario);
    void setFechaCreacion(LocalDateTime fecha);

    // Modification
    void setUsuarioModif(String usuario);
    void setFechaModif(LocalDateTime fecha);
}