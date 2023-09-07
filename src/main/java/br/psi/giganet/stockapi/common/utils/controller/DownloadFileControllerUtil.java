package br.psi.giganet.stockapi.common.utils.controller;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DownloadFileControllerUtil {

    public static void appendFile(File file, HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                String.join("; ", "attachment", "filename=" + file.getName()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        response.setContentType(Files.probeContentType(file.toPath()));

        Files.copy(file.toPath(), response.getOutputStream());
    }

}
