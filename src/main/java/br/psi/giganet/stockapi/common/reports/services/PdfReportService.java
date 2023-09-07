package br.psi.giganet.stockapi.common.reports.services;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Service
public class PdfReportService {

    @Autowired
    private DataSource dataSource;

    public File exportToPdf(String path, String fileName, String template, Map<String, Object> params) throws IOException, JRException, SQLException {
        File temp = File.createTempFile(fileName, ".pdf");
        Connection connection = dataSource.getConnection();
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                loadTemplate(path, template),
                params,
                connection);

        // print report to file
        JRPdfExporter exporter = new JRPdfExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
        reportConfig.setSizePageToContent(true);
        reportConfig.setForceLineBreakPolicy(false);
        exporter.setConfiguration(reportConfig);

        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(temp));
        SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
        exportConfig.setMetadataAuthor("Giganet");
        exporter.setConfiguration(exportConfig);

        exporter.exportReport();

        connection.close();

        return temp;
    }

    private InputStream loadTemplate(String path, String templateName) throws IOException {
        return new ClassPathResource("templates/reports/" + path + templateName + ".jasper")
                .getInputStream();
    }
}
