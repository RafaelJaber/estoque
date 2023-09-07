package br.psi.giganet.stockapi.stock.service;

import br.psi.giganet.stockapi.common.reports.model.ReportFormat;
import br.psi.giganet.stockapi.common.reports.services.CsvReportService;
import br.psi.giganet.stockapi.common.reports.services.PdfReportService;
import br.psi.giganet.stockapi.stock.model.Stock;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockReportService {

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private CsvReportService csvReportService;

    public File getStockSituationReport(Stock stock, ReportFormat format) throws Exception {
        String fileName = "estoque_" +
                stock.getName()
                        .replaceAll(" - ", "_")
                        .replaceAll("[\\s+]", "_") + "_" +
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
        fileName = StringUtils.stripAccents(fileName);

        return format == ReportFormat.CSV ?
                getStockSituationAsCSV(stock, fileName) :
                getStockSituationAsPDF(stock, fileName);
    }

    private File getStockSituationAsCSV(Stock stock, String fileName) throws Exception {
        File file = File.createTempFile(fileName, ".csv");
        return csvReportService.csvWriterAll(
                new String[]{"Código", "Produto", "Unidade", "Quantidade Atual"},
                stock.getItems().stream()
                        .sorted(Comparator.comparing(i -> i.getProduct().getName()))
                        .map(item -> new String[]{
                                item.getProduct().getCode(),
                                item.getProduct().getName(),
                                item.getProduct().getUnit().getAbbreviation(),
                                item.getQuantity().toString()
                        })
                        .collect(Collectors.toList()),
                file
        );
    }

    private File getStockSituationAsPDF(Stock stock, String fileName) throws SQLException, IOException, JRException {
        Map<String, Object> params = new HashMap<>();
        params.put("stock", stock.getId());
        params.put("image_logo", "templates/reports/commons/giganetlogo.png");
        params.put("header", "templates/reports/stock/header_portrait.jasper");
        params.put("items_list", "templates/reports/stock/stock_situation_items_with_blank_spaces.jasper");

        return pdfReportService.exportToPdf(
                "stock/",
                fileName,
                "stock_situation",
                params);
    }


}
