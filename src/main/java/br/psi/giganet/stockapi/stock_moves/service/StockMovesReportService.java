package br.psi.giganet.stockapi.stock_moves.service;

import br.psi.giganet.stockapi.common.reports.model.ReportFormat;
import br.psi.giganet.stockapi.common.reports.services.CsvReportService;
import br.psi.giganet.stockapi.common.reports.services.PdfReportService;
import br.psi.giganet.stockapi.stock_moves.controller.response.AdvancedStockMoveProjection;
import br.psi.giganet.stockapi.stock_moves.dto.ColumnDTO;
import br.psi.giganet.stockapi.stock_moves.dto.StockMoveReportDTO;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockMovesReportService {

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private CsvReportService csvReportService;

    public File createStockMovesSimpleReport(List<ColumnDTO> columns, List<StockMoveReportDTO> data, ReportFormat format) throws Exception {
        String fileName = "movimentacoes_" +
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
        fileName = StringUtils.stripAccents(fileName);

        return format == ReportFormat.CSV ?
                getStockMovesSimpleReportAsCSV(columns, data, fileName) :
                getStockMovesSimpleReportAsPDF(columns, data, fileName);
    }

    private File getStockMovesSimpleReportAsCSV(List<ColumnDTO> columns, List<StockMoveReportDTO> data, String fileName) throws Exception {
        File file = File.createTempFile(fileName, ".csv");

        return csvReportService.csvWriterAll(
                columns.stream().map(ColumnDTO::getHeader).toArray(String[]::new),
                data.stream()
                        .map(item -> {
                            BeanWrapper propertyAccessOrigin = PropertyAccessorFactory.forBeanPropertyAccess(item);

                            List<String> values = columns.stream().map(column -> {
                                Object value = propertyAccessOrigin.getPropertyValue(column.getName());

                                return value == null ? StringUtils.EMPTY : value.toString();
                            }).collect(Collectors.toList());

                            return values.toArray(String[]::new);
                        }).collect(Collectors.toList()),
                file
        );
    }

    private File getStockMovesSimpleReportAsPDF(List<ColumnDTO> columns, List<StockMoveReportDTO> data, String fileName) throws SQLException, IOException, JRException {
        return null;
//        Map<String, Object> params = new HashMap<>();
//        params.put("stock", stock.getId());
//        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
//        params.put("header", "templates/reports/stock/header_portrait.jasper");
//        params.put("items_list", "templates/reports/stock/stock_situation_items_with_blank_spaces.jasper");
//
//        return pdfReportService.exportToPdf(
//                "stock/",
//                fileName,
//                "stock_situation",
//                params);
    }

    public File createStockMovesAdvancedReport(List<ColumnDTO> columns, List<AdvancedStockMoveProjection> data, ReportFormat format) throws Exception {
        String fileName = "movimentacoes_de_estoque_" +
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
        fileName = StringUtils.stripAccents(fileName);

        return format == ReportFormat.CSV ?
                getStockMovesAdvancedReportAsCSV(columns, data, fileName) :
                getStockMovesAdvancedReportAsPDF(columns, data, fileName);
    }

    private File getStockMovesAdvancedReportAsCSV(List<ColumnDTO> columns, List<AdvancedStockMoveProjection> data, String fileName) throws Exception {
        File file = File.createTempFile(fileName, ".csv");

        return csvReportService.csvWriterAll(
                columns.stream().map(ColumnDTO::getHeader).toArray(String[]::new),
                data.stream()
                        .map(item -> new String[]{
                                item.getProduct().getCode(),
                                ZonedDateTime.parse(item.getDate())
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à's' HH:mm:ss")),
                                MoveStatus.getLabel(item.getStatus()),
                                item.getQuantity().toString(),
                                item.getProduct().getName(),
                                item.getFrom(),
                                item.getTo()
                        })
                        .collect(Collectors.toList()),
                file
        );
    }

    private File getStockMovesAdvancedReportAsPDF(List<ColumnDTO> columns, List<AdvancedStockMoveProjection> data, String fileName) throws SQLException, IOException, JRException {
        return null;
    }
}
