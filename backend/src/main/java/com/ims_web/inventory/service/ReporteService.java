package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.repository.MovimientoDetalleRepository;
import com.ims_web.inventory.repository.MovimientoRepository;
import jakarta.persistence.criteria.Predicate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteService {

    private final MovimientoRepository movimientoRepo;
    private final MovimientoDetalleRepository detalleRepo;

    public ReporteService(
            MovimientoRepository movimientoRepo,
            MovimientoDetalleRepository detalleRepo) {
        this.movimientoRepo = movimientoRepo;
        this.detalleRepo = detalleRepo;
    }

    public ByteArrayOutputStream generarExcel(
            String tipo, String estado, String usuario,
            String desde, String hasta) throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Sheet sheet = workbook.createSheet("Historial de Movimientos");

        String[] headers = {
                "ID", "Tipo", "Estado", "Descripción",
                "Fecha", "Usuario", "Unidades", "Monto Total"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Specification<Movimiento> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tipo != null && !tipo.isBlank())
                predicates.add(cb.equal(root.get("movimientoTipo"), tipo));
            if (estado != null && !estado.isBlank())
                predicates.add(cb.equal(root.get("movimientoEstado"), estado));
            if (usuario != null && !usuario.isBlank())
                predicates.add(cb.like(cb.lower(root.get("movimientoUsuarioCreacion")),
                        "%" + usuario.toLowerCase() + "%"));
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (desde != null && !desde.isBlank()) {
                LocalDateTime d = LocalDate.parse(desde, fmt).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("movimientoFechaCreacion"), d));
            }
            if (hasta != null && !hasta.isBlank()) {
                LocalDateTime h = LocalDate.parse(hasta, fmt).atTime(23, 59, 59);
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("movimientoFechaCreacion"), h));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Movimiento> movimientos = movimientoRepo.findAll(spec);

        int rowIdx = 1;
        for (Movimiento m : movimientos) {
            List<MovimientoDetalle> detalles = detalleRepo.findByMovimiento(m);
            int totalUnidades = detalles.stream()
                    .mapToInt(d -> d.getMovimientoDetalleCantidad() *
                            (d.getMovimientoDetalleUnidadesPorPaquete() != null
                                    ? d.getMovimientoDetalleUnidadesPorPaquete()
                                    : 1))
                    .sum();

            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(m.getMovimientoId());
            row.createCell(1).setCellValue(m.getMovimientoTipo());
            row.createCell(2).setCellValue(m.getMovimientoEstado());
            row.createCell(3).setCellValue(m.getMovimientoDescripcion() != null
                    ? m.getMovimientoDescripcion()
                    : "");
            row.createCell(4).setCellValue(m.getMovimientoFechaCreacion() != null
                    ? m.getMovimientoFechaCreacion().toString()
                    : "");
            row.createCell(5).setCellValue(m.getMovimientoUsuarioCreacion() != null
                    ? m.getMovimientoUsuarioCreacion()
                    : "");
            row.createCell(6).setCellValue(totalUnidades);
            row.createCell(7).setCellValue(m.getMovimientoPrecioTotal() != null
                    ? m.getMovimientoPrecioTotal().doubleValue()
                    : 0);
        }

        for (int i = 0; i < headers.length; i++)
            sheet.autoSizeColumn(i);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out;
    }
}