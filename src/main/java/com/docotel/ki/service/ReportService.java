package com.docotel.ki.service;

import com.docotel.ki.model.master.MDocument;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.repository.master.MDocumentRepository;
import com.docotel.ki.util.DateUtil;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    MDocumentRepository documentRepository ;

    @Value("${download.output.letters.file.path}")
    private String downloadFileLetterMonitorOutput;

    public void generateReport(OutputStream outputStream) {
        Resource resource = resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + "report/ReportMandarin.jasper");
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("nominal", 25000000);
            params.put("text_pinyin","这是拼音字母的样本 (This is sample of pinyin text)") ;
            params.put("text_latin", "This is sample of latin text");
            params.put("text_arabic","هذه عينة من الحروف العربية");

            JREmptyDataSource ds = new JREmptyDataSource();
            JasperPrint jasperPrint = JasperFillManager.fillReport(resource.getInputStream(), params, ds);

            JRPdfExporter exporter = this.createPdfExporter(jasperPrint, outputStream);
            exporter.exportReport();
        } catch (IOException e) {
            logger.error("Failed loading file", e);
        } catch (JRException e) {
            logger.error("Failed loading report", e);
        }
    }

    private JRPdfExporter createPdfExporter(JasperPrint jp, OutputStream os) {
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jp));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(os));
        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setMetadataAuthor("PT DOCOTEL INDONESIA");
        exporter.setConfiguration(configuration);
        return exporter;
    }

    public String  getFolderDownloadMonitor(TxMonitor result, boolean isSign){
        String btn_download = "";

        try {
            if (result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download")){
                String pathFolder = DateUtil.formatDate(result.getCreatedDate(), "yyyy/MM/dd/");
                String docid =result.getmWorkflowProcessActions().getAction().getDocument().getId() ;

                if (docid != null){
                    MDocument document = documentRepository.findOne(docid);
                    String fileTemplate = FilenameUtils.getName(document.getFilePath());
                    fileTemplate = fileTemplate.substring(0, fileTemplate.length() - 5);
                    TxTmGeneral txTmGeneral = result.getTxTmGeneral();
                    String outputFilePdf = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + "_noSIGN.pdf";
                    String outputFilePdfSign = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".pdf";

                    if (isSign == true)
                        return "<a class=\"btn btn-xs btn-success\" href=\""+outputFilePdfSign+"\" >Download</a>" ;
                    else
                        return  "<a class=\"btn btn-xs btn-success\" href=\""+outputFilePdf+"\" >Download</a>" ;
                }

            }
            else
                return "" ;

        }
        catch (Exception e){}
        return "" ;
    }


}
