package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.response.AnalyticsPointDTO;
import com.proyectS1.warehouse_management.dtos.response.AnalyticsSeriesDTO;
import com.proyectS1.warehouse_management.dtos.response.MovementResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.MovementAnalyticsResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.ProductResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.ReportColumnDTO;
import com.proyectS1.warehouse_management.dtos.response.ReportPreviewResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.ReportSummaryItemDTO;
import com.proyectS1.warehouse_management.dtos.response.WarehouseResponseDTO;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.enums.MovementType;
import com.proyectS1.warehouse_management.model.enums.ReportFormat;
import com.proyectS1.warehouse_management.model.enums.ReportType;
import com.proyectS1.warehouse_management.reports.model.MovementAnalyticsQuery;
import com.proyectS1.warehouse_management.reports.model.ReportColumn;
import com.proyectS1.warehouse_management.reports.model.ReportDataset;
import com.proyectS1.warehouse_management.reports.model.ReportFilePayload;
import com.proyectS1.warehouse_management.reports.model.ReportQuery;
import com.proyectS1.warehouse_management.reports.model.ReportSummaryItem;
import com.proyectS1.warehouse_management.reports.renderers.ReportRenderer;
import com.proyectS1.warehouse_management.services.MovementService;
import com.proyectS1.warehouse_management.services.ProductService;
import com.proyectS1.warehouse_management.services.ReportService;
import com.proyectS1.warehouse_management.services.WarehouseService;
import com.proyectS1.warehouse_management.services.support.WarehouseAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final MovementService movementService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final WarehouseAccessService warehouseAccessService;
    private final List<ReportRenderer> renderers;

    @Override
    public ReportPreviewResponseDTO generatePreview(ReportQuery query) {
        ReportDataset dataset = buildDataset(query);
        return new ReportPreviewResponseDTO(
            dataset.reportType(),
            dataset.title(),
            dataset.subtitle(),
            dataset.description(),
            dataset.generatedAt(),
            dataset.generatedBy(),
            dataset.filters(),
            dataset.summary().stream()
                .map(item -> new ReportSummaryItemDTO(item.key(), item.label(), item.value()))
                .toList(),
            dataset.columns().stream()
                .map(column -> new ReportColumnDTO(column.key(), column.label()))
                .toList(),
            dataset.rows()
        );
    }

    @Override
    public ReportFilePayload generateDownload(ReportQuery query) {
        if (query.format() == null || query.format() == ReportFormat.JSON) {
            throw new ResponseStatusException(BAD_REQUEST, "A downloadable format is required");
        }

        ReportDataset dataset = buildDataset(query);
        ReportRenderer renderer = renderers.stream()
            .filter(item -> item.getFormat() == query.format())
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Renderer not available for format " + query.format()));
        ReportFilePayload payload = renderer.render(dataset);
        String filename = buildDownloadFilename(dataset, payload.filename());
        return new ReportFilePayload(payload.content(), payload.mediaType(), filename);
    }

    @Override
    public MovementAnalyticsResponseDTO generateMovementAnalytics(MovementAnalyticsQuery query) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        if (currentUser.getRole().name().equals("EMPLOYEE")) {
            throw new ResponseStatusException(FORBIDDEN, "Employees cannot access movement analytics");
        }

        String window = normalizeWindow(query.window());
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        List<MovementResponseDTO> movements = movementService.findAll().stream()
            .filter(item -> item.createdAt() != null)
            .filter(item -> !item.createdAt().toLocalDate().isBefore(startDate) && !item.createdAt().toLocalDate().isAfter(endDate))
            .filter(item -> query.productId() == null || query.productId().equals(item.productId()))
            .filter(item -> query.warehouseId() == null
                || query.warehouseId().equals(item.originWarehouseId())
                || query.warehouseId().equals(item.destinationWarehouseId()))
            .filter(item -> query.movementType() == null || query.movementType() == item.movementType())
            .toList();

        Map<LocalDate, Integer> totalPerDay = new LinkedHashMap<>();
        Map<MovementType, Map<LocalDate, Integer>> typeSeries = new EnumMap<>(MovementType.class);
        for (MovementType type : MovementType.values()) {
            typeSeries.put(type, new LinkedHashMap<>());
        }

        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            totalPerDay.put(cursor, 0);
            for (MovementType type : MovementType.values()) {
                typeSeries.get(type).put(cursor, 0);
            }
            cursor = cursor.plusDays(1);
        }

        for (MovementResponseDTO movement : movements) {
            LocalDate date = movement.createdAt().toLocalDate();
            totalPerDay.computeIfPresent(date, (key, value) -> value + 1);
            typeSeries.get(movement.movementType()).computeIfPresent(date, (key, value) -> value + 1);
        }

        List<AnalyticsPointDTO> totalPoints = toPointList(totalPerDay);
        List<AnalyticsSeriesDTO> series = new ArrayList<>();
        series.add(new AnalyticsSeriesDTO("total", "Total diario", "#22c55e", totalPoints));
        series.add(new AnalyticsSeriesDTO("entry", "Entradas", "#38bdf8", toPointList(typeSeries.get(MovementType.ENTRY))));
        series.add(new AnalyticsSeriesDTO("exit", "Salidas", "#f97316", toPointList(typeSeries.get(MovementType.EXIT))));
        series.add(new AnalyticsSeriesDTO("transfer", "Transferencias", "#facc15", toPointList(typeSeries.get(MovementType.TRANSFER))));

        long activeDays = totalPoints.stream().filter(point -> point.value() > 0).count();
        AnalyticsPointDTO peakPoint = totalPoints.stream()
            .max(java.util.Comparator.comparingInt(AnalyticsPointDTO::value))
            .orElse(new AnalyticsPointDTO(startDate.format(DATE_ONLY_FORMATTER), 0));

        Map<String, String> filters = orderedFilters(
            filterEntry("Ventana", "Ultimos 30 dias"),
            filterEntry("Producto", query.productId() == null ? null : String.valueOf(query.productId())),
            filterEntry("Bodega", query.warehouseId() == null ? null : String.valueOf(query.warehouseId())),
            filterEntry("Tipo", query.movementType() == null ? null : query.movementType().name())
        );

        List<ReportSummaryItemDTO> summary = List.of(
            new ReportSummaryItemDTO("totalEvents", "Movimientos totales", String.valueOf(movements.size())),
            new ReportSummaryItemDTO("activeDays", "Dias con movimiento", String.valueOf(activeDays)),
            new ReportSummaryItemDTO("peakDay", "Pico diario", peakPoint.time() + " · " + peakPoint.value())
        );

        return new MovementAnalyticsResponseDTO(
            "Analisis de movimientos",
            "Tendencia diaria de los ultimos 30 dias",
            window,
            startDate.format(DateTimeFormatter.ofPattern("dd MMM")) + " - " + endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            LocalDateTime.now(),
            filters,
            summary,
            series,
            totalPoints
        );
    }

    private ReportDataset buildDataset(ReportQuery query) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        if (currentUser.getRole().name().equals("EMPLOYEE")) {
            throw new ResponseStatusException(FORBIDDEN, "Employees cannot access reports");
        }

        return switch (query.type()) {
            case MOVEMENTS -> buildMovementsReport(query, currentUser);
            case PRODUCTS -> buildProductsReport(query, currentUser);
            case WAREHOUSES -> buildWarehousesReport(query, currentUser);
        };
    }

    private ReportDataset buildMovementsReport(ReportQuery query, AppUser currentUser) {
        List<MovementResponseDTO> movements = movementService.findAll().stream()
            .filter(item -> query.productId() == null || query.productId().equals(item.productId()))
            .filter(item -> query.warehouseId() == null
                || query.warehouseId().equals(item.originWarehouseId())
                || query.warehouseId().equals(item.destinationWarehouseId()))
            .filter(item -> query.movementType() == null || query.movementType() == item.movementType())
            .toList();

        List<ReportColumn> columns = List.of(
            new ReportColumn("movementType", "Tipo"),
            new ReportColumn("productName", "Producto"),
            new ReportColumn("performedByEmployeeName", "Empleado"),
            new ReportColumn("originWarehouseName", "Bodega origen"),
            new ReportColumn("destinationWarehouseName", "Bodega destino"),
            new ReportColumn("createdAt", "Fecha")
        );

        List<Map<String, String>> rows = movements.stream()
            .map(item -> orderedRow(
                Map.entry("movementType", safe(item.movementType())),
                Map.entry("productName", safe(item.productName())),
                Map.entry("performedByEmployeeName", safe(item.performedByEmployeeName())),
                Map.entry("originWarehouseName", safe(item.originWarehouseName())),
                Map.entry("destinationWarehouseName", safe(item.destinationWarehouseName())),
                Map.entry("createdAt", formatDate(item.createdAt()))
            ))
            .toList();

        Map<String, String> filters = orderedFilters(
            filterEntry("Producto", query.productId() == null ? null : String.valueOf(query.productId())),
            filterEntry("Bodega", query.warehouseId() == null ? null : String.valueOf(query.warehouseId())),
            filterEntry("Tipo", query.movementType() == null ? null : query.movementType().name())
        );

        Map<String, Long> perType = movements.stream()
            .collect(Collectors.groupingBy(item -> item.movementType().name(), LinkedHashMap::new, Collectors.counting()));

        List<ReportSummaryItem> summary = new ArrayList<>();
        summary.add(new ReportSummaryItem("totalRecords", "Total de registros", String.valueOf(movements.size())));
        perType.forEach((key, value) -> summary.add(new ReportSummaryItem("count" + key, "Movimientos " + key, String.valueOf(value))));

        return new ReportDataset(
            ReportType.MOVEMENTS,
            "Reporte de movimientos",
            "Trazabilidad operativa de entradas, salidas y transferencias",
            "Este documento resume los movimientos visibles para el usuario autenticado e incluye el contexto operativo principal, filtros aplicados y una tabla organizada para revision o auditoria.",
            LocalDateTime.now(),
            currentUser.getFirstName() + " " + currentUser.getLastName(),
            filters,
            summary,
            columns,
            rows
        );
    }

    private ReportDataset buildProductsReport(ReportQuery query, AppUser currentUser) {
        List<ProductResponseDTO> products = productService.findAll().stream()
            .filter(item -> query.warehouseId() == null || query.warehouseId().equals(item.warehouseId()))
            .filter(item -> query.category() == null || query.category().isBlank() || query.category().equalsIgnoreCase(item.category()))
            .toList();

        List<ReportColumn> columns = List.of(
            new ReportColumn("name", "Producto"),
            new ReportColumn("category", "Categoria"),
            new ReportColumn("price", "Precio"),
            new ReportColumn("warehouseName", "Bodega"),
            new ReportColumn("createdAt", "Creado"),
            new ReportColumn("updatedAt", "Actualizado")
        );

        List<Map<String, String>> rows = products.stream()
            .map(item -> orderedRow(
                Map.entry("name", safe(item.name())),
                Map.entry("category", safe(item.category())),
                Map.entry("price", formatMoney(item.price())),
                Map.entry("warehouseName", safe(item.warehouseName())),
                Map.entry("createdAt", formatDate(item.createdAt())),
                Map.entry("updatedAt", formatDate(item.updatedAt()))
            ))
            .toList();

        Map<String, String> filters = orderedFilters(
            filterEntry("Bodega", query.warehouseId() == null ? null : String.valueOf(query.warehouseId())),
            filterEntry("Categoria", blankToNull(query.category()))
        );

        BigDecimal totalPrice = products.stream()
            .map(ProductResponseDTO::price)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> perCategory = products.stream()
            .collect(Collectors.groupingBy(item -> safe(item.category()), LinkedHashMap::new, Collectors.counting()));

        List<ReportSummaryItem> summary = new ArrayList<>();
        summary.add(new ReportSummaryItem("totalRecords", "Total de productos", String.valueOf(products.size())));
        summary.add(new ReportSummaryItem("totalPrice", "Suma total de precios", formatMoney(totalPrice)));
        perCategory.forEach((key, value) -> summary.add(new ReportSummaryItem("category" + key, "Categoria " + key, String.valueOf(value))));

        return new ReportDataset(
            ReportType.PRODUCTS,
            "Reporte de productos",
            "Catalogo consolidado por categoria, precio y bodega",
            "Este documento presenta el catalogo de productos dentro del alcance del usuario, junto con un resumen ejecutivo y una tabla clara para consultas administrativas.",
            LocalDateTime.now(),
            currentUser.getFirstName() + " " + currentUser.getLastName(),
            filters,
            summary,
            columns,
            rows
        );
    }

    private ReportDataset buildWarehousesReport(ReportQuery query, AppUser currentUser) {
        List<WarehouseResponseDTO> warehouses = warehouseService.findAll().stream()
            .filter(item -> query.managerUserId() == null || query.managerUserId().equals(item.managerUserId()))
            .toList();

        List<ReportColumn> columns = List.of(
            new ReportColumn("name", "Bodega"),
            new ReportColumn("ubication", "Ubicacion"),
            new ReportColumn("capacity", "Capacidad"),
            new ReportColumn("managerName", "Manager"),
            new ReportColumn("managerUserId", "Id manager")
        );

        List<Map<String, String>> rows = warehouses.stream()
            .map(item -> orderedRow(
                Map.entry("name", safe(item.name())),
                Map.entry("ubication", safe(item.ubication())),
                Map.entry("capacity", formatNumber(item.capacity())),
                Map.entry("managerName", safe(item.managerName())),
                Map.entry("managerUserId", item.managerUserId() == null ? "-" : String.valueOf(item.managerUserId()))
            ))
            .toList();

        Map<String, String> filters = orderedFilters(
            filterEntry("Manager", query.managerUserId() == null ? null : String.valueOf(query.managerUserId()))
        );

        BigDecimal totalCapacity = warehouses.stream()
            .map(WarehouseResponseDTO::capacity)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long withManager = warehouses.stream().filter(item -> item.managerUserId() != null).count();
        long withoutManager = warehouses.size() - withManager;

        List<ReportSummaryItem> summary = List.of(
            new ReportSummaryItem("totalRecords", "Total de bodegas", String.valueOf(warehouses.size())),
            new ReportSummaryItem("totalCapacity", "Capacidad total", formatNumber(totalCapacity)),
            new ReportSummaryItem("withManager", "Con manager", String.valueOf(withManager)),
            new ReportSummaryItem("withoutManager", "Sin manager", String.valueOf(withoutManager))
        );

        return new ReportDataset(
            ReportType.WAREHOUSES,
            "Reporte de bodegas",
            "Vista de capacidad, ubicacion y responsables operativos",
            "Este documento concentra la informacion estructural de las bodegas disponibles para el usuario, destacando capacidad instalada, managers asignados y detalle tabular.",
            LocalDateTime.now(),
            currentUser.getFirstName() + " " + currentUser.getLastName(),
            filters,
            summary,
            columns,
            rows
        );
    }

    @SafeVarargs
    private final Map<String, String> orderedRow(Map.Entry<String, String>... entries) {
        return orderedMap(entries, Map.Entry::getKey, Map.Entry::getValue);
    }

    @SafeVarargs
    private final Map<String, String> orderedFilters(Map.Entry<String, String>... entries) {
        return orderedMap(entries, Map.Entry::getKey, Map.Entry::getValue);
    }

    private Map.Entry<String, String> filterEntry(String key, String value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private <T> Map<String, String> orderedMap(T[] entries, Function<T, String> keyMapper, Function<T, String> valueMapper) {
        Map<String, String> values = new LinkedHashMap<>();
        for (T entry : entries) {
            String value = valueMapper.apply(entry);
            if (value == null || value.isBlank()) {
                continue;
            }
            values.put(keyMapper.apply(entry), value);
        }
        return values;
    }

    private String safe(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "-" : DATE_FORMATTER.format(value);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "$0";
        }
        return "$" + value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatNumber(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private String buildDownloadFilename(ReportDataset dataset, String extension) {
        String timestamp = dataset.generatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        List<String> parts = new ArrayList<>();
        parts.add(slugify(dataset.title()));

        if (!dataset.filters().isEmpty()) {
            dataset.filters().entrySet().stream()
                .limit(2)
                .map(entry -> slugify(entry.getKey() + "-" + entry.getValue()))
                .filter(value -> !value.isBlank())
                .forEach(parts::add);
        }

        parts.add(timestamp);
        return String.join("_", parts) + "." + extension;
    }

    private String slugify(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return normalized
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
    }

    private String normalizeWindow(String window) {
        if (window == null || window.isBlank() || "30d".equalsIgnoreCase(window)) {
            return "30d";
        }
        throw new ResponseStatusException(BAD_REQUEST, "Unsupported analytics window: " + window);
    }

    private List<AnalyticsPointDTO> toPointList(Map<LocalDate, Integer> values) {
        return values.entrySet().stream()
            .map(entry -> new AnalyticsPointDTO(entry.getKey().format(DATE_ONLY_FORMATTER), entry.getValue()))
            .toList();
    }
}
