package br.psi.giganet.stockapi.nfe.util;

import br.psi.giganet.stockapi.nfe.dto.NFeFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class NFeReader {

    public static NFeReaderResult readFile(InputStream file) throws JAXBException, IOException {
        String xmlContent = transformFileToString(file);
        NFeFile nFeFile = (NFeFile) JAXBContext.newInstance(NFeFile.class)
                .createUnmarshaller()
                .unmarshal(new StreamSource(
                        new StringReader(xmlContent)));
        return new NFeReaderResult(xmlContent, nFeFile);
    }

    public static String transformFileToString(InputStream is) throws IOException {
        return transformFileToString(is, true);
    }

    public static String transformFileToString(InputStream is, boolean ignoreNamespaces) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            if (ignoreNamespaces) {
                line = line.replaceAll("xmlns=\"http://www.portalfiscal.inf.br/nfe\"", "");
            }
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

}
