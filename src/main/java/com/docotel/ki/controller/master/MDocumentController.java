package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.master.MDocumentParamRepository;
import com.docotel.ki.repository.master.MDocumentRepository;
import com.docotel.ki.repository.master.MLookupRepository;
import com.docotel.ki.repository.transaction.*;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.master.MDocumentService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.transaction.ParamSubtitudeService;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.ImageResizerUtil;
import com.docotel.ki.util.WordReplacerUtil;
import com.docotel.ki.util.ZxingUtil;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.docotel.ki.repository.transaction.*;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.repository.custom.master.MDocTypeDetailCustomRepository;
import com.docotel.ki.repository.custom.master.MDocumentCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmClassCustomRepository;
import com.docotel.ki.repository.master.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.poi.poifs.filesystem.FileMagic;


@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MDocumentController extends BaseController {

    private static final String DIRECTORY_PAGE = "master/dokumen/";
    private static final String DIRECTORY_PAGE2 = "monitor/";
    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-dokumen";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-dokumen";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-dokumen";
    private static final String PAGE_EDIT_PARAM = DIRECTORY_PAGE + "edit-dokumen-param";
    private static final String PAGE_EDIT_DOCUMENT_PARAM = DIRECTORY_PAGE2 + "edit-dokumen-param-substitute";
    private static final String PATH_CREATE_DOCUMENT = "/create-dokumen";
    private static final String PATH_DOWNLOAD_DOCUMENT = "/download-dokumen";
    private static final String PATH_LIST_DOCUMENT = "/list-dokumen";
    private static final String PATH_EDIT_DOCUMENT = "/edit-dokumen";
    private static final String PATH_DELETE_DOCUMENT = "/delete-dokumen";
    private static final String PATH_TAMBAH_DOCUMENT = "/tambah-dokumen";
    private static final String PATH_EDIT_DOCUMENT_PARAM = "/edit-dokumen-param";
    private static final String PATH_UPDATE_DOCUMENT_PARAM = "/update-dokumen-param";
    private static final String PATH_AJAX_LIST = "/cari-dokumen";
    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String PATH_AJAX_PARAMS = "/document-params";
    private static final String PATH_AJAX_DOCTYPE_NAME = "/doctype_name";
    private static final String PATH_AJAX_CLASS_DESC = "/jenis_barang_jasa";
    private static final String PATH_AJAX_CLASS_DESC_ACCEPT = "/class_desc_accept";
    private static final String PATH_AJAX_CLASS_DESC_REJECT = "/class_desc_reject";
    private static final String PATH_AJAX_CLASS_DESC_CORRECTION = "/class_desc_correction";
    private static final String PATH_AJAX_CLASS_NO = "/class_no";
    private static final String PATH_AJAX_LOOKUP = "/lookup";
    private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST_DOCUMENT;
    private static final String REDIRECT_TO_EDIT_DOCUMENT = "redirect:" + PATH_AFTER_LOGIN + PATH_EDIT_DOCUMENT;
    @Autowired
    MDocumentRepository documentRepository;
    @Autowired
    TxTmBrandRepository brandRepository;
    @Autowired
    private StatusService statusService;
    @Value("${logo.qr.pengayoman}")
    private String logoQRPengayoman;
    @Autowired
    private FileService fileService;
    @Autowired
    private MDocumentParamRepository mDocumentParamRepository;
    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
    private TxMonitorRepository txMonitorRepository;
    @Autowired
    private TxReceptionRepository txReceptionRepository;
    @Autowired
    private MDocTypeDetailCustomRepository mDocTypeDetailCustomRepository;
    @Autowired
    private TxTmClassCustomRepository txTmClassCustomRepository;
    @Autowired
    private ParamSubtitudeService paramSubtitudeService;
    @Autowired
    private MLookupRepository mLookupRepository;
    @Autowired
    private MDocumentService mDocumentService;
    @Autowired
    private ClassService classService;
    @Autowired
    private MDocumentCustomRepository mDocumentCustomRepository;
    @Autowired
    private TxDocumentParamRepository txDocumentParamRepository;
    @Value("${download.output.letters.file.path}")
    private String downloadFileLetterMonitorOutput;

    @Value("${upload.file.letters.masterdoc.path}")
    private String uploadFileLettersMasterDocPath;

    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;

    @Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;
    
    @Value(("${upload.file.path.signature}"))
    private String uploadFileSignaturePath;
    
    @Value("${upload.file.letters.masterdoc.path:}")
    private String pathDoc;

    private static String readFile(InputStream is) throws Exception {
        String text = "";
        try {
            XWPFDocument doc = new XWPFDocument(is);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            text = extractor.getText();
            extractor.close();
        } catch (OfficeXmlFileException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return text;
    }

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "config");
        model.addAttribute("subMenu", "listDoc");

        List<MFileSequence> fileSequenceList = fileService.getAllFileSequences();
        model.addAttribute("fileSequenceList", fileSequenceList);


        if (request.getRequestURI().contains(PATH_TAMBAH_DOCUMENT)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                MDocument mDocument = new MDocument();
                mDocument.setStatusFlag(true);
                model.addAttribute("form", mDocument);
            }
        }
    }

    @GetMapping(value = PATH_DOWNLOAD_DOCUMENT)
    public String download(final Model model, @RequestParam(value = "docid", required = true) String docid, @RequestParam(value = "no", required = true) String no, @RequestParam(value = "monitorId", required = true) String monitorId) {

        TxTmGeneral txTmGeneral = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        MDocument document = documentRepository.findOne(docid);
        TxMonitor monitor = txMonitorRepository.findOne(monitorId);
        List<TxDocumentParams> savedParams = txDocumentParamRepository.findTxDocumentParamsByApplicationNoAndDocumentId(no, docid+"-"+monitorId);

        if (savedParams.size() <= 0) {
            List<MDocumentParam> params = mDocumentParamRepository.findMDocumentParamByMDocumentId(docid);
            savedParams = paramSubtitudeService.subtitude(params, txTmGeneral, monitor); // <<<<add params
            txDocumentParamRepository.save(savedParams);
        }
        
        ArrayList<String> newList = new ArrayList<String>();
        List<TxTmClass> txTmClasses = classService.findTxTmClassByGeneralId(txTmGeneral);
        if(txTmClasses.size()>0){
            for(TxTmClass txTmClass: txTmClasses){
                MClass mClass = txTmClass.getmClass();
                if(!newList.contains(mClass.getNo().toString())) { //supaya gak duplikat no class
                	newList.add(mClass.getNo().toString());
                }
            }
        }
        
        model.addAttribute("classNo", newList);
        model.addAttribute("txTmGeneral", txTmGeneral);
        model.addAttribute("document", document);
        model.addAttribute("monitor", monitor);

        return PAGE_EDIT_DOCUMENT_PARAM;
    }

    @GetMapping(value = PATH_CREATE_DOCUMENT)
    public String createSurat(HttpServletResponse response, @RequestParam(value = "docid", required = true) String docid, @RequestParam(value = "no", required = true) String no,
                              @RequestParam(value = "monitorId", required = true) String monitorId) {
        try {
            TxTmGeneral txTmGeneral = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
            MDocument document = documentRepository.findOne(docid);
            TxMonitor txMonitor = txMonitorRepository.findOne(monitorId);

            /*
             * ini kenapa harus didelete yaaa ????? kan sudah ada di DB kenapa musti didelete
            TxMonitor monitor = txMonitorRepository.findOne(monitorId);
            List<TxDocumentParams> savedParams = txDocumentParamRepository.findTxDocumentParamsByApplicationNoAndDocumentId(no, docid);
            if (savedParams.size() > 0) {
                txDocumentParamRepository.delete(savedParams);
            }
            List<MDocumentParam> params = mDocumentParamRepository.findMDocumentParamByMDocumentId(docid);
            savedParams = paramSubtitudeService.subtitude(params, txTmGeneral, monitor);
            txDocumentParamRepository.save(savedParams);
			*/
            //proses download
            List<TxDocumentParams> savedParams = txDocumentParamRepository.findTxDocumentParamsByApplicationNoAndDocumentId(no, docid);


            FileInputStream fis = new FileInputStream(new File(document.getFilePath()));
            String fileTemplate = FilenameUtils.getName(document.getFilePath());
            fileTemplate = fileTemplate.substring(0, fileTemplate.length() - 5);
            XWPFDocument docx = new XWPFDocument(fis);

            WordReplacerUtil wordReplacer = new WordReplacerUtil(docx, brandRepository, uploadFileBrandPath);
            for (TxDocumentParams param : savedParams) {
//            	System.out.println(param.getParamName());
                if (param.getParamValue() != null) {
                    if (param.getParamName().equalsIgnoreCase("<<QR_Code>>")) {
                        //File file = QRCode.from(param.getParamValue()).file();
                        String qrText = param.getParamValue();

                        byte[] qrData = ZxingUtil.textToQrCode(qrText, new File(logoQRPengayoman), 125, 125, 30, 30);
                        Path path = Files.createTempFile(txTmGeneral.getApplicationNo(), ".png");
                        FileUtils.writeByteArrayToFile(path.toFile(), qrData);

                        wordReplacer.replaceImageInTable(param.getParamName(), path.toFile());
                    } else if (param.getParamName().equalsIgnoreCase("<<label_merek>>")) {
                        String labelMerek = param.getParamValue();

                        /*Format Image untuk brandlogo/ yg menggunakan "uploadFileBrandPath" adalah uuid.jpg ex. 8ad78671-009d-47b6-bb62-29f82fc97242.jpg*/
                        String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                        File fileInput = new File(uploadFileBrandPath + pathFolder + labelMerek + ".jpg");

                        BufferedImage image = ImageIO.read(fileInput);
						Path pathBrand = Files.createTempFile(labelMerek, ".jpg");
						ImageIO.write(resize(image, 189), "jpg", pathBrand.toFile());
						//ImageIO.write(image, formatName, pathBrand.toFile());

						wordReplacer.replaceImageInTable(param.getParamName(), pathBrand.toFile());
                    } else {
                        wordReplacer.replaceWordsInText(param.getParamName(), param.getParamValue());
                        wordReplacer.replaceWordsInTables(param.getParamName(), param.getParamValue());
                        wordReplacer.replaceTextInFooter(param.getParamName(), param.getParamValue());
                    }
                } else if (
            			param.getParamName().equalsIgnoreCase("<<application_no_digit_f>>") ||
            			param.getParamName().equalsIgnoreCase("<<application_no_year_f>>") ||
            			param.getParamName().equalsIgnoreCase("<<download_date_f>>") ||
            			param.getParamName().equalsIgnoreCase("<<created_by_f>>")
            		
            		  ) {
                	String replaceText = paramSubtitudeService.findTerdefinisiValue(txTmGeneral, txMonitor, param.getParamName());
	                wordReplacer.replaceTextInFooter(docx, param.getParamName(), replaceText);
	            } else {
	                    wordReplacer.replaceWordsInText(param.getParamName(), "");
	                    wordReplacer.replaceWordsInTables(param.getParamName(), "");
	                    wordReplacer.replaceTextInFooter(param.getParamName(),"");
	                }
	            }
            wordReplacer.replaceNewlineTabInTable();
            String pathFolder = DateUtil.formatDate(txMonitor.getCreatedDate(), "yyyy/MM/dd/");
            if (Files.notExists(Paths.get(downloadFileLetterMonitorOutput + pathFolder))) {
                Files.createDirectories(Paths.get(downloadFileLetterMonitorOutput + pathFolder));
            }
            //***comment by fitria
//            String outputFile = downloadFileLetterMonitorOutput + pathFolder + System.currentTimeMillis() + "_output.docx";
//            String outputFilePdf = downloadFileLetterMonitorOutput + pathFolder + System.currentTimeMillis() + "_output_.pdf";
//            String outputFilePdfSign = downloadFileLetterMonitorOutput + pathFolder + System.currentTimeMillis() + "_output_sign.pdf"; ***
            String outputFile = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".docx";
            String outputFilePdf = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + "_noSIGN.pdf";
            String outputFilePdfSign = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".pdf";
            String outputFilePdfSignWithwatermark = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".pdf";

            wordReplacer.saveAndGetModdedFile(outputFile);

            this.convertToPDF(outputFile, outputFilePdf);
            //this.cloudConvertToPdf(outputFile, outputFilePdf);
            //sign
            this.signPdf(outputFilePdf, outputFilePdfSign);
            this.downloadPdf(response, outputFilePdfSign); //*fitria added* (tanpa sign dulu karena bermasalah
//            this.downloadPdf(response, outputFilePdf); //*fitria added*
           // *comment by: fitria, watermark hanya utk surat pasca, harus dikasih kondisi lagi* this.addWatermarkPdf(outputFilePdfSign, outputFilePdfSignWithwatermark);
            // *comment by: fitria, watermark hanya utk surat pasca* this.downloadPdf(response, outputFilePdfSignWithwatermark);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null; 
    }

    private void signPdf(String input, String output) {
        String key = CERTIFICATE_FILE + "eAdministrasi.p12";
        //System.out.println("PATH : " + key);
        try {
            PDFSignatureFacade facade = new PDFSignatureFacade();
            facade.sign(key, "JakartaPP123!@#", input, output, true, new java.awt.Rectangle(250, -50, 400, 78));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public BufferedImage dropAlphaChannel(BufferedImage src) {
        BufferedImage convertedImg = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        convertedImg.getGraphics().drawImage(src, 0, 0, null);

        return convertedImg;
   }
    
    @GetMapping(value = PATH_DOWNLOAD_DOCUMENT + "/{docid}/{no}/{monitorId}")
    public String downloadDocument(HttpServletResponse response, @PathVariable("docid") String docid, @PathVariable("no") String no, @PathVariable("monitorId") String monitorId) throws Exception {
        TxTmGeneral txTmGeneral = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        MDocument document = documentRepository.findOne(docid);
        //document.setFilePath(document.getFilePath().replaceAll("/data1","D:/KI"));
        TxMonitor txMonitor = txMonitorRepository.findOne(monitorId);
        List<TxDocumentParams> savedParams = txDocumentParamRepository.findTxDocumentParamsByApplicationNoAndDocumentId(no, docid+"-"+monitorId);

        FileInputStream fis = new FileInputStream(new File(document.getFilePath()));
        String fileTemplate = FilenameUtils.getName(document.getFilePath());
        fileTemplate = fileTemplate.substring(0, fileTemplate.length() - 5);
        XWPFDocument docx = new XWPFDocument(fis);

        WordReplacerUtil wordReplacer = new WordReplacerUtil(docx, brandRepository, uploadFileBrandPath);
        for (TxDocumentParams param : savedParams) {
            if (param.getParamValue() != null) {
                if (param.getParamName().equalsIgnoreCase("<<QR_Code>>")) {
                    //File file = QRCode.from(param.getParamValue()).file();

                    String qrText = param.getParamValue();

                    byte[] qrData = ZxingUtil.textToQrCode(qrText, new File(logoQRPengayoman), 125, 125, 30, 30, true);
                    Path path = Files.createTempFile(txTmGeneral.getApplicationNo(), ".png");
                    FileUtils.writeByteArrayToFile(path.toFile(), qrData);

                    wordReplacer.replaceImageInTable(param.getParamName(), path.toFile());
                } else if (param.getParamName().equalsIgnoreCase("<<label_merek>>")) {
                    String labelMerek = param.getParamValue();

                    /*Format Image untuk brandlogo/ yg menggunakan "uploadFileBrandPath" adalah uuid.jpg ex. 8ad78671-009d-47b6-bb62-29f82fc97242.jpg*/
                    String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                    File fileInput = new File(uploadFileBrandPath + pathFolder + labelMerek + ".jpg");

                    BufferedImage image = ImageIO.read(fileInput);
					Path pathBrand = Files.createTempFile(labelMerek, ".jpg");
					ImageIO.write(resize(image, 189), "jpg", pathBrand.toFile());
					//ImageIO.write(image, formatName, pathBrand.toFile());

					wordReplacer.replaceImageInTable(param.getParamName(), pathBrand.toFile());
					
                } else if (param.getParamName().equalsIgnoreCase("<<created_by_sign>>")) {
                	String createdBySign = param.getParamValue();
                	File fileInput = new File(uploadFileSignaturePath + createdBySign + ".jpg");
                	
                	wordReplacer.replaceImageInTable(param.getParamName(), fileInput, 65);
//                } else if (param.getParamName().equalsIgnoreCase("<<jenis_barang_jasa>>")) {
//                    String jenisBarangJasa = param.getParamValue();
//
//
                } else {
	                    wordReplacer.replaceWordsInText(param.getParamName(), param.getParamValue());
	                    wordReplacer.replaceWordsInTables(param.getParamName(), param.getParamValue());
	                    wordReplacer.replaceTextInFooter(param.getParamName(), param.getParamValue());
                }
	
	            } else if (
            			param.getParamName().equalsIgnoreCase("<<application_no_digit_f>>") ||
            			param.getParamName().equalsIgnoreCase("<<application_no_year_f>>") ||
            			param.getParamName().equalsIgnoreCase("<<download_date_f>>") ||
            			param.getParamName().equalsIgnoreCase("<<created_by_f>>") ||
            			param.getParamName().equalsIgnoreCase("<<created_by>>")
            		  ) {

            	String replaceText = paramSubtitudeService.findTerdefinisiValue(txTmGeneral, txMonitor, param.getParamName());
                wordReplacer.replaceTextInFooter(docx, param.getParamName(), replaceText);
	            } else {
	                wordReplacer.replaceWordsInText(param.getParamName(), "");
	                wordReplacer.replaceWordsInTables(param.getParamName(), "");
	                wordReplacer.replaceTextInFooter(param.getParamName(), "");
	            }
        }
        wordReplacer.replaceNewlineTabInTable();
        String pathFolder = DateUtil.formatDate(txMonitor.getCreatedDate(), "yyyy/MM/dd/");
        if (Files.notExists(Paths.get(downloadFileLetterMonitorOutput + pathFolder))) {
            Files.createDirectories(Paths.get(downloadFileLetterMonitorOutput + pathFolder));
        }
//          *** comment by fitria
//        String outputFile = downloadFileLetterMonitorOutput + pathFolder + System.currentTimeMillis() + "_output.docx";
//        String outputFilePdf = downloadFileLetterMonitorOutput + pathFolder + System.currentTimeMillis() + "_output_.pdf";
//        String outputFilePdfSign = downloadFileLetterMonitorOutput + pathFolder + System.currentTimeMillis() + "_output_sign.pdf"; ****

//        String outputFile = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".docx";
//        String outputFilePdf = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + "_noSIGN.pdf";
//        String outputFilePdfSign = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".pdf";
//        String outputFilePdfSignWithwatermark = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".pdf";

        // Dibikin Simple aja file penyimpanannya
        String outputFile = downloadFileLetterMonitorOutput + pathFolder + "CetakMonitor-" + monitorId + ".docx";
        String outputFilePdf = downloadFileLetterMonitorOutput + pathFolder + "CetakMonitor-" + monitorId + "_noSIGN.pdf";
        String outputFilePdfSign = downloadFileLetterMonitorOutput + pathFolder + "CetakMonitor-" + monitorId + ".pdf";
        String outputFilePdfSignWithwatermark = downloadFileLetterMonitorOutput + pathFolder + "CetakMonitor-" + monitorId + ".pdf";


        wordReplacer.saveAndGetModdedFile(outputFile);
        //sign
        this.convertToPDF(outputFile, outputFilePdf);
        //this.cloudConvertToPdf(outputFile, outputFilePdf);
        this.signPdf(outputFilePdf, outputFilePdfSign); // masih bermasalah
        this.downloadPdf(response, outputFilePdfSign); // masih bermasalah
        //*comment by fitria (bg hanya utk surat pasca)* this.addWatermarkPdf(outputFilePdfSign, outputFilePdfSignWithwatermark);
        //*comment by fitria (bg hanya utk surat pasca)* this.downloadPdf(response, outputFilePdfSignWithwatermark);
        return null;
    }

    /*
    private void cloudConvertToPdf(String inputFile, String outputFile) throws Exception {
        CloudConvertService service = new CloudConvertService("gQ5RmyD9yO1HVjgUixJJHFqQiGBmkd5ZlGSWt2SX5Fpakspw3QYjYOb0Qy2cIflN");
        ConvertProcess process = service.startProcess("docx", "pdf");
        process.startConversion(new File(inputFile));
        // Wait for result
        ProcessStatus status;
        waitLoop:
        while (true) {
            status = process.getStatus();
            switch (status.step) {
                case FINISHED:
                    break waitLoop;
                case ERROR:
                    throw new RuntimeException(status.message);
            }
            Thread.sleep(200);
        }
        service.download(status.output.url, new File(outputFile));
        process.delete();
    }
    */

    private void convertToPDF(String docPath, String pdfPath) {
        try {
            InputStream doc = new FileInputStream(new File(docPath));
            XWPFDocument document = new XWPFDocument(doc);
            
            //Remark by Rudy K at 13/03/2019
//            //re-create margins 
//            CTSectPr getSectPr = document.getDocument().getBody().getSectPr();
//            getSectPr.unsetPgMar();  
//            CTPageMar addNewPgMar = getSectPr.addNewPgMar();
//            addNewPgMar.setLeft(BigInteger.valueOf(0L));
//            addNewPgMar.setTop(BigInteger.valueOf(0L));
//            addNewPgMar.setRight(BigInteger.valueOf(0L));
//            addNewPgMar.setBottom(BigInteger.valueOf(0L));
//            // Also good to handle footer and header for more expectable result
//            addNewPgMar.setFooter(BigInteger.valueOf(0L));
//            addNewPgMar.setHeader(BigInteger.valueOf(565L));
//            //end            
            
            PdfOptions options = PdfOptions.create();
            OutputStream out = new FileOutputStream(new File(pdfPath));
            PdfConverter.getInstance().convert(document, out, options);
        } catch (IOException ex) {
            //System.out.println(ex.getMessage());
        }
    }
    
    private void addWatermarkPdf(String input, String output) throws Exception {

    	PdfReader reader = new PdfReader(input);
        int n = reader.getNumberOfPages();
        PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(output));
	    // image watermark
	    Image img = Image.getInstance(pathDoc + "background-surat.jpeg");
	    img.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
	    float w = img.getScaledWidth();
	    float h = img.getScaledHeight();
	    // transparency
	    PdfGState gs1 = new PdfGState();
	    gs1.setFillOpacity(0.5f);
	    // properties
	    PdfContentByte under;
	    Rectangle pagesize;
	    float x, y;
	    // loop over every page
	    for (int i = 1; i <= n; i++) {
	        pagesize = reader.getPageSizeWithRotation(i);
	        x = (pagesize.getLeft() + pagesize.getRight()) / 2;
	        y = (pagesize.getTop() + pagesize.getBottom()) / 2;
	        under  = stamp.getUnderContent(i);
	        under.saveState();
	        under.setGState(gs1);
	        under.addImage(img, w, 0, 0, h, x - (w / 2), y - (h / 2));
	        under.restoreState();
	    }
	    stamp.close();
	    reader.close();
    }

    private void downloadPdf(HttpServletResponse response, String file) throws Exception {

    	File downloadFile = new File(file);
        FileInputStream inStream = new FileInputStream(downloadFile);
        response.setContentLength((int) downloadFile.length());
        response.setContentType("application/pdf");
        // forces download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("inline; filename=\"%s\"", downloadFile.getName());
        response.setHeader(headerKey, headerValue);
        OutputStream outStream = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inStream.close();
        outStream.close();
    }

    @GetMapping(PATH_AJAX_PARAMS + "/{docid}/{no}/{monitorId}")
    @ResponseBody
    public List<TxDocumentParams> getParams(@PathVariable("docid") String docid, @PathVariable("no") String no, @PathVariable("monitorId") String monitorId) {
        return txDocumentParamRepository.findTxDocumentParamsByApplicationNoAndDocumentId(no, docid+"-"+monitorId);
    }

    @GetMapping(PATH_AJAX_DOCTYPE_NAME + "/{no}")
    @ResponseBody
    public List<MDocType> getDocType(@PathVariable("no") String no) {
        TxReception txReception = txReceptionRepository.findTxReceptionByApplicationNo(no);
        MFileType mFileType = txReception.getmFileType();
        List<MDocTypeDetail> mDocTypeDetails = mDocTypeDetailCustomRepository.selectAll("LEFT JOIN FETCH c.mDocType",
                "mFileType", mFileType, true, null, null);
        List<MDocType> mDocTypes = new ArrayList<>();
        for (MDocTypeDetail mDocTypeDetail : mDocTypeDetails) {
            MDocType mDocType = mDocTypeDetail.getmDocType();
            mDocType.setCreatedBy(null);
            //System.out.println("DOC TYPE NAME: " + mDocType.getName());
            mDocTypes.add(mDocType);
        }
        return mDocTypes;
    }
    
    @GetMapping(PATH_AJAX_CLASS_NO + "/{no}")
    @ResponseBody
    public List<MClass> getClassNo(@PathVariable("no") String no) {
        TxTmGeneral txTmGeneral = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        List<TxTmClass> txTmClasses = txTmClassCustomRepository.selectAll("LEFT JOIN FETCH c.mClass",
                "txTmGeneral", txTmGeneral, true, "mClass.no", "ASC", null,null);
        List<MClass> mClasses = new ArrayList<>();
        for (TxTmClass txTmClass : txTmClasses) {
        	MClass mClass = txTmClass.getmClass();
        	mClass.setCreatedBy(null);
        	mClass.setmClassDetailList(null);
        	mClasses.add(mClass);
        }
        return mClasses;
    }

    @GetMapping(PATH_AJAX_CLASS_DESC + "/{no}")
    @ResponseBody
    public List<MClassDetail> getClassDesc(@PathVariable("no") String no) {
        TxTmGeneral txTmGeneral = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        List<TxTmClass> txTmClasses = txTmClassCustomRepository.selectAll("LEFT JOIN FETCH c.mClassDetail mcd LEFT JOIN FETCH mcd.parentClass",
                "txTmGeneral",txTmGeneral,true, "mClassDetail.parentClass.no", "ASC", null,null);
        List<MClassDetail> mClassDetails = new ArrayList<>();
        for (TxTmClass txTmClass : txTmClasses) {
            MClassDetail mClassDetail = txTmClass.getmClassDetail();
            mClassDetail.setCreatedBy(null);
            mClassDetail.getParentClass().setmClassDetailList(null);
            mClassDetail.getParentClass().setCreatedBy(null);
            mClassDetails.add(mClassDetail);
        }
        return mClassDetails;
    }
    
    @GetMapping(PATH_AJAX_CLASS_DESC_ACCEPT + "/{no}")
    @ResponseBody
    public List<MClassDetail> getClassDescAccept(@PathVariable("no") String no) {
        TxTmGeneral txTmGeneral = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        List<TxTmClass> txTmClasses = txTmClassCustomRepository.selectAllClassByTxGeneral(txTmGeneral, ClassStatusEnum.ACCEPT.name(), false);
        List<MClassDetail> mClassDetails = new ArrayList<>();
        for (TxTmClass txTmClass : txTmClasses) {
            MClassDetail mClassDetail = txTmClass.getmClassDetail();
            mClassDetail.setCreatedBy(null);
            mClassDetail.getParentClass().setmClassDetailList(null);
            mClassDetail.getParentClass().setCreatedBy(null);
            mClassDetails.add(mClassDetail);
        }
        return mClassDetails;
    }
    
    @GetMapping(PATH_AJAX_CLASS_DESC_REJECT + "/{no}")
    @ResponseBody
    public List<MClassDetail> getClassDescReject(@PathVariable("no") String no) {
        TxTmGeneral txTmGeneral = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        List<TxTmClass> txTmClasses = txTmClassCustomRepository.selectAllClassByTxGeneral(txTmGeneral, ClassStatusEnum.REJECT.name());
        List<MClassDetail> mClassDetails = new ArrayList<>();
        for (TxTmClass txTmClass : txTmClasses) {
            MClassDetail mClassDetail = txTmClass.getmClassDetail();
            mClassDetail.setCreatedBy(null);
            mClassDetail.getParentClass().setmClassDetailList(null);
            mClassDetail.getParentClass().setCreatedBy(null);
            mClassDetails.add(mClassDetail);
        }
        return mClassDetails;
    }
    
    @GetMapping(PATH_AJAX_CLASS_DESC_CORRECTION + "/{no}")
    @ResponseBody
    public List<MClassDetail> getClassDescCorrection(@PathVariable("no") String no) {
        TxTmGeneral txTmGeneral = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        List<TxTmClass> txTmClasses = txTmClassCustomRepository.selectAllClassByTxGeneral(txTmGeneral, ClassStatusEnum.ACCEPT.name(), true);
        List<MClassDetail> mClassDetails = new ArrayList<>();
        for (TxTmClass txTmClass : txTmClasses) {
            MClassDetail mClassDetail = txTmClass.getmClassDetail();
            mClassDetail.setCreatedBy(null);
            mClassDetail.getParentClass().setmClassDetailList(null);
            mClassDetail.getParentClass().setCreatedBy(null);
            mClassDetails.add(mClassDetail);
        }
        return mClassDetails;
    }

    @GetMapping(PATH_AJAX_LOOKUP + "/{group}")
    @ResponseBody
    public List<MLookup> getMLookup(@PathVariable("group") String group) {
        List<MLookup> mLookups = mLookupRepository.findMLookupByGroups(group);
        return mLookups;
    }

    @GetMapping(path = PATH_LIST_DOCUMENT)
    public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
        List<MDocument> list = documentRepository.findAll();
        model.addAttribute("list", list);
        return PAGE_LIST;
    }

    @GetMapping(path = PATH_TAMBAH_DOCUMENT)
    public String showPageAdd(Model model) {
        return PAGE_TAMBAH;
    }

    @GetMapping(path = PATH_EDIT_DOCUMENT + "/{id}")
    public String showPageEdit(@PathVariable("id") String id, Model model) {
        MDocument document = documentRepository.findOne(id);
        model.addAttribute("form", document);
        model.addAttribute("params", mDocumentParamRepository.findMDocumentParamByMDocumentId(document.getId()));
        return PAGE_EDIT;
    }
    
    @PostMapping(path = PATH_EDIT_DOCUMENT)
    //@Transactional
    public String saveEditDokumen(@RequestParam("file") MultipartFile file, @ModelAttribute("form") MDocument data, final Model model, final HttpServletRequest request, final HttpServletResponse response,
                                    final BindingResult errors) throws Exception {
        try {
        	
            if (!file.isEmpty()) {
                // Get the file and save it
                String filePath = uploadFileLettersMasterDocPath + file.getOriginalFilename();
                byte[] bytes = file.getBytes();
                Path path = Paths.get(filePath);
                Files.write(path, bytes);

                data.setFilePath(filePath);
                data.setStatusFlag(true);
                data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                MDocument tmp = mDocumentService.saveMDoc(data);

                List<MDocumentParam> temps = this.findParam(filePath, "(\\<<)(.*?)(\\>>)");
                List<MDocumentParam> params = new ArrayList<MDocumentParam>();
                for (MDocumentParam param : temps) {
                    if (!isContain(params, param.getName())) {
                        param.setmDocument(tmp);
                        param.setValue("");
                        param.setDescription("Keterangan Parameter");
                        param.setTypes(paramSubtitudeService.setParamType(param.getName()));
                        params.add(param);
                    }
                }
                mDocumentService.deleteMDocumentParamByMDocumentId(tmp.getId());
                mDocumentService.saveMDocParam(params);
            } else {
                data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                mDocumentService.saveMDoc(data);
            }

            model.asMap().clear();

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("errorMessage", "Gagal menambahkan Dokumen");
            writeJsonResponse(response, 500);
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("errorMessage", "Gagal menambahkan Dokumen");
            writeJsonResponse(response, 500);
        }
        return REDIRECT_TO_LIST;
    }

    @GetMapping(path = PATH_DELETE_DOCUMENT + "/{id}")
    //@Transactional
    public String deleteDokumen(@PathVariable("id") String id, Model model) {
    	mDocumentService.deleteMDocumentParamByMDocumentId(id);
    	mDocumentService.delete(id);
        return REDIRECT_TO_LIST;
    }

    @GetMapping(path = PATH_EDIT_DOCUMENT_PARAM + "/{id}")
    public String showPageEditParam(@PathVariable("id") String id, Model model) {
        MDocumentParam param = mDocumentParamRepository.findOne(id);
        model.addAttribute("form", param);
        return PAGE_EDIT_PARAM;
    }

    @PostMapping(path = PATH_EDIT_DOCUMENT_PARAM)
    public String saveEditParam(@ModelAttribute("form") MDocumentParam data, Model model) {
        mDocumentParamRepository.save(data);
        data.setmDocument(documentRepository.findOne(data.getmDocument().getId()));
        return REDIRECT_TO_EDIT_DOCUMENT + "/" + data.getmDocument().getId();
    }

    @PostMapping(path = PATH_UPDATE_DOCUMENT_PARAM)
    public void updateParam(final HttpServletRequest request, final HttpServletResponse response, Model model) {
        try {
            String paramId = request.getParameter("paramId");
            String paramValue = request.getParameter("paramValue");

            TxDocumentParams txDocumentParams = paramSubtitudeService.selectOneBy("id", paramId);
            txDocumentParams.setParamValue(paramValue);

            paramSubtitudeService.doSaveOrUpdate(txDocumentParams);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping(path = PATH_TAMBAH_DOCUMENT)
    //@Transactional
    public String processAddDokumen(@RequestParam("file") MultipartFile file, @ModelAttribute("form") MDocument data, final Model model, final HttpServletRequest request, final HttpServletResponse response,
                                    final BindingResult errors) throws Exception {
        try {
        	if (!errors.hasFieldErrors("name")) {
        		List<MDocument> mDocument = documentRepository.findByName(data.getName().trim());
            	if (!mDocument.isEmpty()) {
            		model.addAttribute("data", data);
	            	model.addAttribute("errorMessage", "Gagal menambah Data Dokumen " + data.getName() + " sudah ada");
	        		return PAGE_TAMBAH;
            	}            		
            }
        	
        	
            if (!file.isEmpty()) {
                // Get the file and save it
                String filePath = uploadFileLettersMasterDocPath + file.getOriginalFilename();
                byte[] bytes = file.getBytes();
                Path path = Paths.get(filePath);
                Files.write(path, bytes);

                data.setFilePath(filePath);
                data.setStatusFlag(true);
                data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                MDocument tmp = mDocumentService.saveMDoc(data);

                List<MDocumentParam> temps = this.findParam(filePath, "(\\<<)(.*?)(\\>>)");
                List<MDocumentParam> params = new ArrayList<MDocumentParam>();
                for (MDocumentParam param : temps) {
                    if (!isContain(params, param.getName())) {
                        param.setmDocument(tmp);
                        param.setValue("");
                        param.setDescription("Keterangan Parameter");
                        param.setTypes(paramSubtitudeService.setParamType(param.getName()));
                        params.add(param);
                    }
                }
                mDocumentService.deleteMDocumentParamByMDocumentId(tmp.getId());
                mDocumentService.saveMDocParam(params);
            } else {
                data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                mDocumentService.saveMDoc(data);
            }

            model.asMap().clear();

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("errorMessage", "Gagal menambahkan Dokumen");
            writeJsonResponse(response, 500);
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("errorMessage", "Gagal menambahkan Dokumen");
            writeJsonResponse(response, 500);
        }
        return REDIRECT_TO_LIST;
    }

    private boolean isContain(List<MDocumentParam> params, String name) {
        boolean isContain = false;
        for (MDocumentParam param : params) {
            if (param.getName().equalsIgnoreCase(name)) {
                isContain = true;
                break;
            }
        }
        return isContain;
    }

    private List<MDocumentParam> findParam(String filePath, String pattern) throws Exception {
        List<MDocumentParam> result = new ArrayList<MDocumentParam>();
        FileInputStream fis = new FileInputStream(new File(filePath));
        String data = readFile(fis);
        Pattern ptrn = Pattern.compile("(\\<<)(.*?)(\\>>)");
        Matcher matcher = ptrn.matcher(data);
        while (matcher.find()) {
            MDocumentParam param = new MDocumentParam();
            param.setName(matcher.group());
            result.add(param);
        }
        return result;
    }

    @PostMapping(value = REQUEST_MAPPING_AJAX_LIST)
    public void doGetListDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 50;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = null;

            if (searchByArr != null) {
                searchCriteria = new ArrayList<>();
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, false));
                        }
                    }
                }
            }

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "1":
                            orderBy = "name";
                            break;
                        case "2":
                            orderBy = "statusFlag";
                            break;
                        case "3":
                            orderBy = "createdBy.username";
                            break;
                        case "4":
                            orderBy = "createdDate";
                            break;
                        default:
                            orderBy = null;
                            break;
                    }
                }
            }

            String orderType = request.getParameter("order[0][dir]");
            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("DESC")) {
                    orderType = "ASC";
                }
            }

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<MDocument> searchResult = statusService.searchGeneralDocument(searchCriteria, orderBy, orderType, offset, limit);
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MDocument result : searchResult.getList()) {
                	
                	// For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UDK")) {
                    	button = "<div class=\"btn-actions\">"
                                 + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_DOCUMENT + "/" + result.getId()) + "\">Ubah</a> "
                                 + "</div>";
                    }
                	
                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getName(),
//                            result.isStatusFlag()==true?"Ya": "Tidak",
                            result.isStatusFlag() == true ? "Aktif" : "Tidak Aktif",
                            result.getCreatedBy().getUsername(),
                            fmt.format(new Date(result.getCreatedDate().getTime())),
                            button
                            /*"<div class=\"btn-actions\">" +
                                    "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_DOCUMENT + "/" + result.getId()) + "\">Ubah</a> "*/
//                                            + "<br/>" +
//                                            "<a class=\"btn btn-danger btn-xs\" href=\"" + getPathURLAfterLogin(PATH_DELETE_DOCUMENT + "/" + result.getId()) + "\">Hapus</a>" +
                                   /* + "</div>"*/

//                            "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_DOCUMENT + "/" + result.getId()) + "\">Ubah</a>&nbsp&nbsp"+
//                            "<a class=\"btn btn-danger btn-xs\" href=\"" + getPathURLAfterLogin(PATH_DELETE_DOCUMENT + "/" + result.getId()) + "\">Hapus</a>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    public static BufferedImage resize(BufferedImage inputImage, double limit) throws IOException {
        double imgWidth = inputImage.getWidth();
        double imgHeight = inputImage.getHeight();
        double percent = 1;

        if (imgWidth > limit || imgHeight > limit) {
            if (imgWidth > imgHeight) {
                percent = (double) (100 / (imgWidth / limit)) / 100;
            } else {
                percent = (double) (100 / (imgHeight / limit)) / 100;
            }
        }

        return ImageResizerUtil.resize(inputImage, percent);
    }

}
