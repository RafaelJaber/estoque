package br.psi.giganet.stockapi.common.reports.services;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

@Service
public class CsvReportService {

    public File csvWriterAll(String[] headers, List<String[]> data, File file) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(file));
        writer.writeNext(headers);
        writer.writeAll(data);
        writer.close();
        return file;
    }

}
