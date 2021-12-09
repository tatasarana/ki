package com.docotel.ki.controller;

import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.model.wipo.ImageFile;
import com.docotel.ki.model.wipo.ImageFileList;
import com.docotel.ki.model.wipo.PDFFile;
import com.docotel.ki.model.wipo.XMLFile;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.custom.master.MDocTypeCustomRepository;
import com.docotel.ki.repository.custom.master.MWorkflowCustomRepository;
import com.docotel.ki.repository.custom.master.MWorkflowProcessCustomRepository;
import com.docotel.ki.repository.master.*;
import com.docotel.ki.repository.transaction.*;
import com.docotel.ki.service.BingTranslationService;
import com.docotel.ki.service.WipoService;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.ImageFileListService;
import com.docotel.ki.service.transaction.MadridService;
import com.docotel.ki.service.transaction.PermohonanOnlineService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jdom2.*;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.docotel.ki.repository.custom.master.MClassDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.MrepresentativeCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxMadridCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxReceptionCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmBrandCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmClassCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmOwnerCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmReprsCustomRepository;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MadridController extends BaseController {

    @Autowired
    MadridService madridService;
    @Autowired
    FileSeqService fileSeqService;
    @Autowired
    FileService fileService;
    @Autowired
    UserService userService;
    @Autowired
    BrandService brandService;
    @Autowired
    WipoService wipoService;
    @Autowired
    private LookupService lookupService;

    @Autowired
    private ImageFileListService imageFileListService;
    @Autowired
    private PermohonanOnlineService permohonanOnlineService;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private MCountryRepository mCountryRepository;
    @Autowired
    private MClassHeaderRepository mClassHeaderRepository;
    @Autowired
    private MrepresentativeCustomRepository mRepresentativeCustomRepository;
    @Autowired
    private MClassDetailCustomRepository mClassDetailCustomRepository;
    @Autowired
    private TxReceptionCustomRepository txReceptionCustomRepository;
    @Autowired
    private TxReceptionRepository txReceptionRepository;
    @Autowired
    private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
    private TxTmOwnerCustomRepository txTmOwnerCustomRepository;
    @Autowired
    private TxTmReprsCustomRepository txTmReprsCustomRepository;
    @Autowired
    private TxTmReprsRepository txTmReprsRepository;
    @Autowired
    private TxTmClassCustomRepository txTmClassCustomRepository;
    @Autowired
    private TxTmClassRepository txTmClassRepository;
    @Autowired
    private TxTmBrandCustomRepository txTmBrandCustomRepository;
    @Autowired
    private TxTmBrandRepository txTmBrandRepository;
    @Autowired
    private TxMadridRepository txMadridRepository;
    @Autowired
    private TxMadridCustomRepository txMadridCustomRepository;
    @Autowired
    private MWorkflowCustomRepository mWorkflowCustomRepository;
    @Autowired
    private MWorkflowRepository mWorkflowRepository;
    @Autowired
    private MLawRepository mLawRepository;
    @Autowired
    private MWorkflowProcessCustomRepository mWorkflowProcessCustomRepository;
    @Autowired
    private TxTmOwnerRepository txTmOwnerRepository;
    @Autowired
    private TxTmOwnerDetailRepository txTmOwnerDetailRepository;
    @Autowired
    private MRepresentativeRepository mRepresentativeRepository;
    @Autowired
    private TxTmPriorRepository txTmPriorRepository;
    @Autowired
    private TxRegistrationRepository txRegistrationRepository;
    @Autowired
    private TxRegistrationDetailRepository txRegistrationDetailRepository;
    @Autowired
    private ImageFileRepository imageFileRepository;
    @Autowired
    private MDocTypeCustomRepository mDocTypeCustomRepository;

    @Value("${wipo.madrid.extract.location.xml}")
    private String wipoMadridExtractLocationXml;
    @Value("${wipo.madrid.extract.location.pdf}")
    private String wipoMadridExtractLocationPdf;
    @Value("${upload.file.doc.application.path}")
    private String uploadFileDocApplicationPath;
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;


    private static final String DIRECTORY_PAGE = "madrid/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-madrid";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-madrid";

    private static final String PATH_LIST = "/list-madrid";
    private static final String PATH_EDIT = "/edit-madrid";
    private static final String PATH_TAMBAH = "/tambah-madrid";
    private static final String PATH_TAMBAH_KD = "/tambah-madrid";


    private static final String PATH_AJAX_LIST = "/cari-madrid";
    private static final String PATH_AJAX_UPLOAD = "/upload-list-madrid";
    private static final String PATH_PROSES_UPLOAD = "/proses-list-madrid";
    private static final String PATH_PROSES_COPY = "/copy-list-madrid";

    private static final String REQUEST_MAPPING_LIST = PATH_LIST + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_AJAX_UPLOAD = PATH_AJAX_UPLOAD + "*";
    private static final String REQUEST_MAPPING_PROSES_UPLOAD = PATH_PROSES_UPLOAD + "*";
    private static final String REQUEST_MAPPING_PROSES_COPY = PATH_PROSES_COPY + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

    public static final String REDIRECT_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;

    HashMap<TxReception, TxMadrid> hListProcess = null;
    HashMap<String, TxMadrid> hListMadrid = null;
    //HashMap<TxReception, TxMadrid> hListProcessDuplicate = null;
    HashMap<String, MCountry> hListCountry = null;
    HashMap<Integer, MClass> hListClass = null;
    HashMap<TxReception, HashMap<TxMadrid, MadridDetailInfo>> hListObject = null;
    HashMap<String, TxReception> hListReceptionMadrid = null;
    HashMap<String, TxTmGeneral> hListGeneralMadrid = null;
    HashMap<String, TxTmOwner> hListTxTmOwnerMadrid = null;
    HashMap<String, TxTmBrand> hListTxTmBrandMadrid = null;
    HashMap<String, MRepresentative> hListMReprsMadrid = null;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "Madrid");
        model.addAttribute("subMenu", "Madrid");

        if (request.getRequestURI().contains(PATH_TAMBAH_KD)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MClassDetail());
            }
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_LIST)
    public String doShowPage(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        List<MFileType> fileTypeList = fileService.findByMenu("MADRID_DCP");
        Collections.sort(fileTypeList, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        model.addAttribute("fileTypeList", fileTypeList);

        List<String> tranTypeList = txMadridRepository.findDistinctTrantype();
        model.addAttribute("tranTypeList", tranTypeList);

        List<String> noList = txMadridRepository.findDistinctNo();
        Collections.sort(noList);
        model.addAttribute("noList", noList);

        return PAGE_LIST;
    }

    @RequestMapping(path = REQUEST_MAPPING_TAMBAH)
    public String showPageList(Model model, @RequestParam(value = "no", required = false) String no) {
        MClass mClass = null;

        model.addAttribute("mClass", mClass);

        return PAGE_LIST;
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
                        if (searchBy.equalsIgnoreCase("uploadDate")) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        } else {
                            if (StringUtils.isNotBlank(value)) {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                        }
                    }
                }
            }

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("") || orderBy.equalsIgnoreCase("0")) {
                    orderBy = null;
                } else {

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
            GenericSearchWrapper<TxMadrid> searchResult = madridService.listMadrid(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxMadrid result : searchResult.getList()) {
                    Date date = new Date(result.getCreatedDate().getTime());
                    String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);

                    no++;
                    data.add(new String[]{
                            "" + no,
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getUuid()) + "\">" + result.getId() + "</a>",
                            result.getIntregn(),
                            result.getDocType(),
                            result.getNo(),
                            result.getTranTyp(),
                            pDate
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }


    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
        TxMadrid txMadrid = madridService.selectOne("uuid", no);
        if (txMadrid == null) {
            return REDIRECT_LIST + "?error=Nomor Dokumen " + no + " tidak ditemukan";
        }

        try {
            if (hListClass == null) {
                hListClass = new HashMap<Integer, MClass>();
                List<MClass> listClass = mClassHeaderRepository.findByStatusFlagTrue();
                for (MClass mClass : listClass) {
                    hListClass.put(mClass.getNo(), mClass);
                }
            }

            String application_no = "" ;
            TxTmGeneral txTmGeneral = txTmGeneralCustomRepository.selectOne("madrid_id.uuid",txMadrid.getUuid(),true);
            if (txTmGeneral != null) {
                application_no = txTmGeneral.getApplicationNo();
            }

            model.addAttribute("application_no",application_no);
            model.addAttribute("form", txMadrid);

            MadridDetailInfo mdi = new MadridDetailInfo();
            mdi = madridDetailXml(no, "", "Y");

            model.addAttribute("mdi", txMadrid != null ? mdi : new MadridDetailInfo());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Gagal Menampilkan Data Madrid");
        }
        return PAGE_EDIT;
    }


    @PostMapping(REQUEST_MAPPING_AJAX_UPLOAD)
    public void doAjaxUpload(final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            List<XMLFile> fileList = wipoService.selectNotProcessedXmlFile();
            MUser currentUser = userService.getUserByName("SYSTEM");
            MLaw mLaw = mLawRepository.findOne("7");
            MWorkflowProcess mWorkflowProcess = mWorkflowProcessCustomRepository.selectOne("status.id", StatusEnum.M_NEW_APPLICATION.value().getId(), true);
            MWorkflow wfMadrid = mWorkflowRepository.findOne("WF_UU_20");

            List<MCountry> listCountry = mCountryRepository.findAll();
            Map<String, MCountry> lookupCountry = new HashMap<>();
            for (MCountry mCountry : listCountry) {
                lookupCountry.put(mCountry.getId(), mCountry);
            }

            List<MFileSequence> listSequence = fileSeqService.findAll();
            Map<String, MFileSequence> lookupSequence = new HashMap<>();
            for (MFileSequence mFileSequence : listSequence) {
                lookupSequence.put(mFileSequence.getId(), mFileSequence);
            }

            List<MFileType> listFileType = fileService.findAll();
            Map<String, MFileType> lookupFileType = new HashMap<>();
            for (MFileType mFileType : listFileType) {
                lookupFileType.put(mFileType.getId(), mFileType);
            }

            MFileTypeDetail fileTypeDetailMD = fileService.getMFileTypeDetailById("MD");
            MFileTypeDetail fileTypeDetailMO = fileService.getMFileTypeDetailById("MO");

            MBrandType brandTypeSigvrbl = brandService.selectOne("xmlMadrid", "SIGVRBL");
            MBrandType brandTypeMarkve = brandService.selectOne("xmlMadrid", "MARKVE");
            MBrandType brandTypeSoumari = brandService.selectOne("xmlMadrid", "SOUMARI");
            MBrandType brandTypeThrdmar = brandService.selectOne("xmlMadrid", "THRDMAR");
            MBrandType brandDefault = brandService.selectOne("xmlMadrid", "MARCOLI");

            List<MClass> mClassList = mClassHeaderRepository.findAll();
            Map<Integer, MClass> lookupClassHeader = new HashMap<>();
            for (MClass mClass : mClassList) {
                lookupClassHeader.put(mClass.getNo(), mClass);
            }

            Map<String, Object> listLookup = new HashMap<>();
            listLookup.put("currentUser", currentUser);
            listLookup.put("mLaw", mLaw);
            listLookup.put("mWorkflowProcess", mWorkflowProcess);
            listLookup.put("wfMadrid", wfMadrid);
            listLookup.put("lookupCountry", lookupCountry);
            listLookup.put("lookupSequence", lookupSequence);
            listLookup.put("lookupFileType", lookupFileType);
            listLookup.put("fileTypeDetailMD", fileTypeDetailMD);
            listLookup.put("fileTypeDetailMO", fileTypeDetailMO);
            listLookup.put("brandTypeSigvrbl", brandTypeSigvrbl);
            listLookup.put("brandTypeMarkve", brandTypeMarkve);
            listLookup.put("brandTypeSoumari", brandTypeSoumari);
            listLookup.put("brandTypeThrdmar", brandTypeThrdmar);
            listLookup.put("brandDefault", brandDefault);
            listLookup.put("lookupClassHeader", lookupClassHeader);

            MDocType txTmDocMDocType = mDocTypeCustomRepository.selectOne("id", "391");
            listLookup.put("txTmDocMDocType", txTmDocMDocType);

            if (!fileList.isEmpty()) {
                for (XMLFile file : fileList) {
                    try {
                        String xmlFileId = file.getId();

                        String replaceFirst = file.getFileName().replaceAll("x", "N");
                        String replaceExtension = replaceFirst.replaceAll(".zip", ".xml");

                        File files = new File(wipoMadridExtractLocationXml + file.getModifiedYear() + File.separator + replaceExtension);
                        if (files.exists()) {
                            PDFFile pdfFile = wipoService.selectPdfByWeek(file.getWeek());

                            if (pdfFile == null) {
                                continue;
                            }

                            List<PdfFile> listPdf = madridService.listPdfFiles(pdfFile);
                            listLookup.put("listPdf", listPdf);

                            // Create XML Document
                            SAXBuilder saxBuilder = new SAXBuilder();
                            Document document = (Document) saxBuilder.build(files);

                            // Get and Separate XML Header
                            Element header = document.getRootElement();
                            Document documentHeader = new Document(header.clone().detach());
                            Element headerXML = documentHeader.getRootElement();
                            headerXML.removeContent();

                            Document docHeader = new Document(headerXML.clone());
                            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
                            String xmlHeader = xmlOutputter.outputString(docHeader);    //create xml with all tag enotif or header tag.

                            Element rootNode = document.getRootElement();
                            List<Element> nodes = rootNode.getChildren();
                            for (Element element : nodes) {
                                TxMadrid txMadrid = new TxMadrid();
                                txMadrid.setSkipLogAuditTrail(false);
                                txMadrid.setXmlFileId(xmlFileId);
                                txMadrid.setNo(headerXML.getAttribute("GAZNO").getValue());
                                txMadrid.setDocType(element.getName());
                                txMadrid.setCreatedBy(currentUser);
                                txMadrid.setDocXMLHeader(xmlHeader);
                                Document docDetail = new Document(element.clone().detach());
                                String xmlDetail = xmlOutputter.outputString(docDetail);
                                txMadrid.setDocXMLDetail(xmlDetail);

                                if (element.getAttribute("INTREGN") != null) {
                                    txMadrid.setIntregn(element.getAttributeValue("INTREGN"));
                                }

                                if (element.getAttribute("TRANTYP") != null) {
                                    txMadrid.setTranTyp(element.getAttributeValue("TRANTYP"));
                                }

                                if (element.getAttribute("DOCID") != null) {
                                    txMadrid.setId(element.getAttributeValue("DOCID"));
                                }

                                if (element.getName().equalsIgnoreCase("birth")) {
                                    madridService.insertBirth(txMadrid, element, listLookup);
                                }

                                if (element.getName().equalsIgnoreCase("death")) {
                                    madridService.insertDeath(txMadrid, element, listLookup);
                                }

                                if (element.getName().equalsIgnoreCase("newname")) {
                                    madridService.insertNewname(txMadrid, element, listLookup);
                                }

                                if (element.getName().equalsIgnoreCase("restrict")) {
                                    madridService.insertRestrict(txMadrid, element, listLookup);
                                }

                                if (element.getName().equalsIgnoreCase("newbase")) {
                                    madridService.insertNewbase(txMadrid, element, listLookup);
                                }

                                if (element.getName().equalsIgnoreCase("prolong")) {
                                    madridService.insertProlong(txMadrid, element, listLookup);
                                }

                                if (element.getName().equalsIgnoreCase("correction")) {
                                    Element correctElement = element.getChild("CORRECT").getChild("BIRTH");
                                    if (correctElement == null) {
                                        correctElement = element.getChild("CORRECT").getChild("NEWNAME");
                                        if (correctElement == null) {
                                            correctElement = element.getChild("CORRECT").getChild("RESTRICT");
                                        }
                                    }

                                    if (correctElement != null) {
                                        if (correctElement.getAttribute("INTREGN") != null) {
                                            txMadrid.setIntregn(correctElement.getAttributeValue("INTREGN"));
                                        }

                                        if (correctElement.getAttribute("DOCID") != null) {
                                            txMadrid.setId(correctElement.getAttributeValue("DOCID"));
                                        }

                                        txMadrid.setTranTyp("RIN");
                                        madridService.insertCorrection(txMadrid, correctElement, listLookup);
                                    }
                                }

                                if (element.getName().equalsIgnoreCase("created")) {
                                    madridService.insertCreated(txMadrid, element, listLookup);
                                }

                                if (element.getName().equalsIgnoreCase("processed")) {
                                    txMadrid.setIntregn(element.getChild("INTREGN").getValue());
                                    madridService.insertProcessed(txMadrid, element);
                                }

                                if (element.getName().equalsIgnoreCase("paid")) {
                                    // Not used
                                }

                                if (element.getName().equalsIgnoreCase("license-birth")) {
                                    // Not used
                                }

                                if (element.getName().equalsIgnoreCase("license-newname")) {
                                    // Not used
                                }
                            }

                            madridService.updateMadridXMLFile(file.getId());
                        }
                        result.put("success", true);
                    } catch (JDOMException | IOException e) {
                        e.printStackTrace();
                        result.put("message", "Gagal membaca file. Pastikan file dalam format yang disediakan.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.put("message", "Gagal membaca file. Pastikan file dalam format yang disediakan.");
                    }
                }
            } else {
                result.put("success", false);
                result.put("message", "Tidak ada file yang diproses");
            }

            writeJsonResponse(response, result);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_PROSES_UPLOAD)
    public void doProsesUpload(final HttpServletRequest request, final HttpServletResponse response) throws IOException, InvalidFormatException {
        if (isAjaxRequest(request)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            List<ImageFile> fileList = wipoService.selectNotProcessedImageFile();
            if (!fileList.isEmpty()) {
                for (ImageFile file : fileList) {
                    try {
                        // XML already run or not
                        XMLFile xmlFile = wipoService.selectXmlByWeek(file.getWeek());
                        if (xmlFile != null) {
                            List<ImageFileList> imageFileLists = file.getImageFileList();
                            madridService.prosesMadridImage(imageFileLists);
                            file.setProcessed(true);
                            imageFileRepository.save(file);
                        }
                    } catch (Exception e) {

                    }
                }
            } else {
                result.put("success", false);
                result.put("message", "Tidak ada file yang diproses");
            }

            result.put("success", true);
            writeJsonResponse(response, result);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    public void copy(String source, String target) throws IOException {
        Path pathTarget = Paths.get(target);
        if (!Files.exists(pathTarget.getParent())) {
            Files.createDirectories(pathTarget.getParent());
        }
        if (source.contains(".TIF")) {
            final BufferedImage tif = ImageIO.read(new File(source));
            ImageIO.write(tif, "jpg", new File(target));
        } else {
            Path pathSource = Paths.get(source);
            Files.copy(pathSource, pathTarget, StandardCopyOption.REPLACE_EXISTING);
        }
    }


    public String getTagElement(Document doc, String childElement, String ElementFilterTag) {
        String temp = "";
        Element e = null;
        ElementFilter ef = new ElementFilter();

        e = doc.getRootElement().getChild(childElement);
        if (e != null) {
            ef = new ElementFilter(ElementFilterTag);
            for (Element c : e.getDescendants(ef)) {
                temp += c.getTextNormalize() + " ";

                if (temp.equalsIgnoreCase(" ")) //tag CORRIND, only tag without value, but in UI must display as character 'Y'.
                    temp = "Y";
            }
        }
        return temp.trim();
    }


    public String getSingleElement(Element element) {
        String ret = "";
        if (element != null) {
            ret = element.getValue();
        }
        return ret;
    }

    public String findElementByAttribute(Document document, String child, String attributesName, String attributeValue, String patternTag) {
        String temp = "";
        String expression = "//" + child + "[@" + attributesName + "=\"" + attributeValue + "\"]" + patternTag;
        XPathFactory xpath = XPathFactory.instance();
        //XPathExpression<Element> expr = xpath.compile(expression, Filters.element());
        XPathExpression<Element> expr = xpath.compile(expression, Filters.element());
        List<Element> values = expr.evaluate(document);
        for (Element c : values) {
            temp += c.getTextNormalize() + " ";

            if (temp.equalsIgnoreCase(" ")) //tag CORRIND, only tag without value, but in UI must display as character 'Y'.
                temp = "Y";
        }

        return temp;
    }


    private void mappingTxReception(TxMadrid txMadrid, Element element) {
	    	  /*
	       	APPPLICATION_DATE	-> BASAPPD diganti NOTDATE
			APPLICATION_NO	-> M+00+REGEDAT(AMBIL TAHUN SAJA-2018)+INTREGN
			
			<BIRTH TRANTYP="ENN" INTREGN="1429066" OOCD="JP" ORIGLAN="1" EXPDATE="20280713" DOCID="1206948402" REGRDAT="20181212" NOTDATE="20181227" REGEDAT="20180713" OFFREF="2018-301555" INTREGD="20180713" DESUNDER="P">
			<NEWNAME TRANTYP="MAN" INTREGN="0994744" DOCID="1205478901" NOTDATE="20181227" REGEDAT="20181128">
			<RESTRICT TRANTYP="CBNP" ORIGLAN="1" DOCID="1188281301" INTREGN="1416724" NOTDATE="20181210" REGRDAT="20181207" REGEDAT="20181003">						
			
			<DEATH TRANTYP="RAN" DOCID="1205984801" REGRDAT="20181211" INTREGN="1434838" EXPDATE="20181203" />
			<PROLONG TRANTYP="REN" INTREGN="0702266" EXPDATE="20281028" DOCID="1194884801" REGRDAT="20181210" DESUNDER="P" RENDATE="20181028" />
	
			BANK_CODE	-> INTREGN
			CREATED_DATE ->	CURRENT_TIMESTAMP
			ONLINE_FLAG	-> 0
			PAYMENT_DATE ->	BASAPPD diganti NOTDATE
			TOTAL_CLASS	-> COUNT(NICCLAI)
			TOTAL_PAYMENT ->	0
			CREATED_BY	GET UUID USER/SYSTEM
			FILE_SEQ_ID	-> UUID FROM M_FILE_SEQ=00,  OFFICE OF ORIGIN  (TAG:OOCD)
			FILE_TYPE_ID -> GET UUID FROM M_FILE_TYPE=M
			FILE_TYPE_DETAIL_ID	-> GET UUID FROM M_FILE_TYPE_DETAIL
									OOCD="ID" -> file type detail code : M2
										OOCD!="ID" -> file type detail code : M1
	        */
        try {
            TxReception txReception = new TxReception();
            if (element.getAttribute("OOCD") != null) {
                txReception.setmFileSequence(fileSeqService.findOne(element.getAttribute("OOCD").getValue()));
                txReception.setmFileType(fileService.getFileTypeById(element.getName()));
                if (!"ID".equalsIgnoreCase(element.getAttribute("OOCD").getValue())) {
                    txReception.setmFileTypeDetail(fileService.getMFileTypeDetailById("MD"));
                } else {
                    txReception.setmFileTypeDetail(fileService.getMFileTypeDetailById("MO"));
                }
            } else {
                txReception.setmFileSequence(fileSeqService.findOne("WO"));
                txReception.setmFileType(fileService.getFileTypeById(element.getName()));
                txReception.setmFileTypeDetail(fileService.getMFileTypeDetailById("MD"));
            }

            if (element.getAttribute("INTREGN") != null) {
                txMadrid.setIntregn(element.getAttribute("INTREGN").getValue());
                txReception.setBankCode(element.getAttribute("INTREGN").getValue());
                txReception.setApplicationNo("M00".concat(String.valueOf(DateUtil.currentYear())).concat(element.getAttribute("INTREGN").getValue()));
            }

            if (element.getAttribute("NOTDATE") != null) {
                txReception.setApplicationDate(DateUtil.toDate("yyyymmdd", element.getAttribute("NOTDATE").getValue()));
                txReception.setPaymentDate(DateUtil.toDate("yyyymmdd", element.getAttribute("NOTDATE").getValue()));
            } else if (element.getAttribute("REGRDATE") != null) {
                txReception.setApplicationDate(DateUtil.toDate("yyyymmdd", element.getAttribute("REGRDATE").getValue()));
                txReception.setPaymentDate(DateUtil.toDate("yyyymmdd", element.getAttribute("REGRDATE").getValue()));
            }

            Element e = null;
            ElementFilter ef = new ElementFilter();
            int totalClass = 0;
            e = element.getChild("BASICGS");
            if (e != null) {
                ef = new ElementFilter("GSGR");
                for (Element c : e.getDescendants(ef)) {
                    if (!c.getAttributeValue("NICCLAI").isEmpty()) {
                        totalClass++;
                    }
                }
            }

            MUser mUser = new MUser();
            mUser.setId((userService.getUserByName("SYSTEM")).getId());

            txReception.setTotalClass(totalClass);
            txReception.setTotalPayment(new BigDecimal(0));
            txReception.setOnlineFlag(false);
            txReception.setCreatedBy(mUser);
            txReception.setXmlFileId(txMadrid.getXmlFileId());
            //madridService.mappingMadrid(txReception, txMadrid);

            if (hListProcess == null) {
                hListProcess = new HashMap<>();
            }

            hListProcess.put(txReception, txMadrid); 
            
            /*if(hListProcessDuplicate==null) {
            	hListProcessDuplicate = new HashMap<>();
            }            
            if("D".equalsIgnoreCase(flag)) {
            	hListProcessDuplicate.put(txReception, txMadrid); 
            } else if("".equalsIgnoreCase(flag)) {
            	hListProcess.put(txReception, txMadrid); 
            }*/


        } catch (ParseException e) {
            logger.error(e);
        }

    }

    public MadridDetailInfo madridDetail(String xmlString, String flagUI) {
        MadridDetailInfo mdi = new MadridDetailInfo();
        SAXBuilder saxBuilder = new SAXBuilder();
        StringReader stringReader = new StringReader(xmlString);
        try {
            Document jdomDocument = saxBuilder.build(stringReader);
            Element root = jdomDocument.getRootElement();
            if (root.getAttributeValue("ORIGLAN") != null) {
                mdi.setOriginalLanguage(root.getAttributeValue("ORIGLAN"));
            }
            if (root.getAttributeValue("DOCID") != null) {
                mdi.setDocumentId(root.getAttributeValue("DOCID"));
            }
            if (root.getAttributeValue("REGRDAT") != null) {
                mdi.setDateRecord(root.getAttributeValue("REGRDAT"));
            }
            if (root.getAttributeValue("NOTDATE") != null) {
                mdi.setNotificationDate(root.getAttributeValue("NOTDATE"));
            }
            if (root.getAttributeValue("REGEDAT") != null) {
                mdi.setEffectiveDate(root.getAttributeValue("REGEDAT"));
            }
            if (root.getAttributeValue("OOCD") != null) {
                mdi.setOfficeOrigin(root.getAttributeValue("OOCD"));
            }
            if (root.getAttributeValue("EXPDATE") != null) {
                mdi.setExpiryDate(root.getAttributeValue("EXPDATE"));
            }

            mdi.setInternationalRegistrationNumber("INTREGN");
            if (root.getAttributeValue("INTREGD") != null) {
                mdi.setInternationalRegistrationDate(root.getAttributeValue("INTREGD"));
            }
            if (root.getAttributeValue("DESUNDER") != null) {
                mdi.setInstrumentContractingPartyDesign(root.getAttributeValue("DESUNDER"));
            }

            //holder harus dibuat sebagai table, karena ada tenuan tag HOLGR lebih dari 1.
            List<Element> eList = jdomDocument.getRootElement().getChildren("HOLGR");
            ArrayList<MadridHolding> mhList = new ArrayList<>();
            if (eList != null) {
                for (Element el : eList) {
                    MadridHolding mh = new MadridHolding();
                    mh.setHoldCLID(el.getAttributeValue("CLID"));
                    mh.setHoldName(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_NAME));
                    mh.setHoldAddress(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_ADDRESS));
                    mh.setHoldCountry(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_COUNTRY));
                    mh.setHoldEntitlement(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_ENTITLEMENT));
                    mh.setHoldLegalNature(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_LEGAL_NATURE));
                    mh.setHoldPlaceIncorporated(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_PLACE_INCORPORATED));
                    mh.setHoldCorrsAddress(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_CORRESPONDENCE_ADDRESS));
                    mhList.add(mh);
                }
            }
            mdi.setHolderList(mhList);
            mdi.setPrevHolderDetail(getTagElement(jdomDocument, "PHOLGR", "NAMEL").concat(getTagElement(jdomDocument, "PHOLGR", "ADDRL")).concat(
                    getTagElement(jdomDocument, "PHOLGR", "COUNTRY")));

            mdi.setCorrespondenceDetail(getTagElement(jdomDocument, "CORRGR", "NAMEL").concat(getTagElement(jdomDocument, "CORRGR", "ADDRL")).concat(getTagElement(jdomDocument, "CORRGR", "COUNTRY")));
            //mdi.setReprsDetail(getTagElement(jdomDocument, "REPGR", "NAMEL").concat(getTagElement(jdomDocument, "REPGR", "ADDRL")).concat(getTagElement(jdomDocument, "REPGR", "COUNTRY")) );

            eList = jdomDocument.getRootElement().getChildren("REPGR");
            ArrayList<MadridReprs> repList = new ArrayList<>();
            if (eList != null) {
                for (Element el : eList) {
                    MadridReprs mr = new MadridReprs();
                    mr.setRepCLID(el.getAttributeValue("CLID"));
                    mr.setRepName(findElementByAttribute(jdomDocument, el.getName(), new MadridReprs().ATTR_CLID, mr.getRepCLID(), new MadridReprs().TAG_NAME));
                    mr.setRepAddress(findElementByAttribute(jdomDocument, el.getName(), new MadridReprs().ATTR_CLID, mr.getRepCLID(), new MadridReprs().TAG_ADDRESS));
                    mr.setRepCountry(findElementByAttribute(jdomDocument, el.getName(), new MadridReprs().ATTR_CLID, mr.getRepCLID(), new MadridReprs().TAG_COUNTRY));

                    MRepresentative mRepresentative = mRepresentativeCustomRepository.selectOne("name", mr.getRepName().toUpperCase().trim(), false);
                    mr.setRepExist(mRepresentative != null);
                    mr.setRepId(mRepresentative == null ? (new MRepresentative()).getId() : mRepresentative.getId());

                    repList.add(mr);
                }
            }
            mdi.setReprsList(repList);


            Element viennaList = root.getChild("VIENNAGR");
            ElementFilter filterVienna = new ElementFilter("VIECLAI");
            String temp = "";
            if (viennaList != null) {
                for (Element c : viennaList.getDescendants(filterVienna)) {
                    temp += c.getTextNormalize().substring(0, 2) + "." + c.getTextNormalize().substring(2, c.getTextNormalize().length()) + ",";
                }
                //remove last character ','
                if (temp.endsWith(",")) {
                    temp = temp.substring(0, temp.length() - 1);
                }
            }
            mdi.setVienna(temp);

            temp = "";
            if (viennaList != null) {
                ElementFilter filterViennaCLA = new ElementFilter("VIECLA3");
                for (Element c : viennaList.getDescendants(filterViennaCLA)) {
                    temp += c.getTextNormalize() + ", ";
                }
            }
            if (temp.endsWith(",")) {
                temp = temp.substring(0, temp.length() - 1);
            }

            mdi.setVIECLA3(temp);

            eList = jdomDocument.getRootElement().getChildren("VRBLNOT");
            temp = "";
            if (eList != null) {
                for (Element el : eList) {
                    String[] parts = el.getValue().toString().trim().split("\n", 500);
                    for (String s : parts) {
                        temp += s + ",";
                    }
                }
            }
            mdi.setVRBLNOT(temp == "" ? "VRBLNOT" : temp);

            //brand, default is SIGVRBL
            MBrandType mBrandType = null;
            if ("SIGVRBL".contains(xmlString)) {
                mBrandType = brandService.selectOne("xmlMadrid", "SIGVRBL");
            } else if ("MARKVE".contains(xmlString)) {
                mBrandType = brandService.selectOne("xmlMadrid", "MARKVE");
            } else if ("SOUMARI".contains(xmlString)) {
                mBrandType = brandService.selectOne("xmlMadrid", "SOUMARI");
            } else if ("THRDMAR".contains(xmlString)) {
                mBrandType = brandService.selectOne("xmlMadrid", "THRDMAR");
            }

            if (mBrandType != null) {
                mdi.setBrandType(mBrandType);
                mdi.setBrandTypeName(mBrandType.getName()); //display in user interface
            }

            //<IMAGE NAME="1430298" TEXT="Youm Balance" COLOUR="N" TYPE="JPG" />
            String filename = "";
            String brandName = "";
            Element eImg = jdomDocument.getRootElement().getChild("IMAGE");
            if (eImg != null) {
                //filename = eImg.getAttributeValue("NAME").concat(".").concat(eImg.getAttributeValue("TYPE"));
                filename = Integer.parseInt(eImg.getAttributeValue("NAME")) + "." + eImg.getAttributeValue("TYPE");
                brandName = eImg.getAttributeValue("TEXT");
            }
            mdi.setFileName(filename);
            mdi.setBrandName(brandName);
            mdi.setMarkDescription(getTagElement(jdomDocument, "MARDESGR", "MARDESEN"));
            mdi.setColorsClaimed(getTagElement(jdomDocument, "COLCLAGR", "COLCLAEN"));
            mdi.setDisclaimer(getTagElement(jdomDocument, "DISCLAIMGR", "DISCLAIMEREN"));
            mdi.setTranslation(getTagElement(jdomDocument, "MARTRGR", "MARTREN"));
            mdi.setMarkDescriptionChar(getSingleElement(jdomDocument.getRootElement().getChild("MARTRAN")));
            mdi.setMarkinColorEn(getTagElement(jdomDocument, "COLCLAGR", "COLPAREN"));
            mdi.setMarkVoluntary(getTagElement(jdomDocument, "VOLDESGR", "VOLDESEN"));

            String tempClass = "";
            Element e = null;
            ElementFilter ef = new ElementFilter();

            e = jdomDocument.getRootElement().getChild("BASICGS");
            Map<String, String> hClass = new HashMap<String, String>();
            Map<String, String> hClassUI = new HashMap<String, String>();

            if (e != null) {
                ef = new ElementFilter("GSGR");
                for (Element c : e.getDescendants(ef)) {
                    tempClass = c.getAttributeValue("NICCLAI");
                    if (!tempClass.isEmpty()) {
                        ElementFilter efDetail = new ElementFilter("GSTERMEN");
                        for (Element elDetail : c.getDescendants(efDetail)) {
                            hClassUI.put(tempClass, elDetail.getTextNormalize());

                            String classHeader = hListClass.get(Integer.valueOf(tempClass)).getId();
                            String[] arrDesc = (elDetail.getTextNormalize()).split("\\;");

                            for (int i = 0; i < arrDesc.length; i++) {
                                String descEn = arrDesc[i].trim();

                                hClass.put(tempClass, classHeader + ";" + "null" + ";" + descEn + ";" + "null");
                            }
                        }
                    }
                }
            }
            TreeMap<String, String> sortedMap = new TreeMap<>();
            if ("Y".equalsIgnoreCase(flagUI)) {
                sortedMap.putAll(hClassUI);
            } else if ("N".equalsIgnoreCase(flagUI)) {
                sortedMap.putAll(hClass);
            }

            mdi.setBasicList(sortedMap);

            eList = jdomDocument.getRootElement().getChildren("BASGR");
            Map<String, String> hApp = new HashMap<String, String>();
            if (eList != null) {
                for (Element el : eList) {
                    String[] parts = el.getValue().toString().trim().split("\n", 2);
                    Date d = new SimpleDateFormat("yyyyMMdd").parse(parts[0].trim());
                    String appDate = new SimpleDateFormat("dd/MM/yyyy").format(d);
                    hApp.put(parts[1].trim(), appDate);
                }

            }

            mdi.setBasicDetail(hApp);

            eList = jdomDocument.getRootElement().getChildren("PRIGR");
            ArrayList<MadridPrior> mpList = new ArrayList<>();
            if (eList != null) {
                for (Element el : eList) {
                    String[] parts = el.getValue().toString().trim().split("\n", 5);
                    MadridPrior mp = new MadridPrior();
                    mp.setPriorCountry(parts[0].trim());
                    mp.setPriorDate(new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyyMMdd").parse(parts[1].trim())));
                    mp.setPriorNo(parts[2].trim());
                    String clai = el.getChild("PRIGS") != null ? el.getChild("PRIGS").getAttributeValue("NICCLAI") : "";
                    mp.setClassNo(clai);
                    mpList.add(mp);
                }
            }
            mdi.setPriorityDetail(mpList);

            eList = jdomDocument.getRootElement().getChildren("DESPG");
            temp = "";
            if (eList != null) {
                for (Element el : eList) {
                    String[] parts = el.getValue().toString().trim().split("\n", 500);
                    for (String s : parts) {
                        temp += s + ",";
                    }
                }
            }

            if (temp.endsWith(",")) {
                temp = temp.substring(0, temp.length() - 1);
            }

            mdi.setProtocolDesignation(temp);
            mdi.setLimitation(getTagElement(jdomDocument, "LIMGR", "GSHEADEN"));


            e = jdomDocument.getRootElement();
            Map<String, String> hLim = new HashMap<String, String>();
            if (e != null) {
                ef = new ElementFilter("LIMTO");
                for (Element c : e.getDescendants(ef)) {
                    tempClass = c.getAttributeValue("NICCLAI");
                    if (!tempClass.isEmpty()) {
                        ElementFilter efDetail = new ElementFilter("GSTERMEN");
                        for (Element elDetail : c.getDescendants(efDetail)) {
                            hLim.put(tempClass, elDetail.getTextNormalize());
                        }
                    }
                }
            }
            sortedMap = new TreeMap<>();
            sortedMap.putAll(hLim);
            mdi.setLimtoList(sortedMap);


            eList = jdomDocument.getRootElement().getChildren("DESPG2");
            temp = "";
            if (eList != null) {
                for (Element el : eList) {
                    String[] parts = el.getValue().toString().trim().split("\n", 500);
                    for (String s : parts) {
                        temp += s + ",";
                    }
                }
            }
            mdi.setDESPG2(temp);
        } catch (JDOMException | ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mdi;
    }

    public MadridDetailInfo madridDetailXml(String no, String xmlDetail, String flagUI) {
        TxMadrid txMadrid = null;
        if (!no.isEmpty()) {
            txMadrid = madridService.selectOne("uuid", no);
        }
        return txMadrid != null ? madridDetail(txMadrid.getDocXMLDetail(), flagUI) : madridDetail(xmlDetail, flagUI);
    }


    //mapping dari kolom table xml_detail ke pojo MadridDetailInfo
    /*public MadridDetailInfo madridDetailXml (String no, String xmlDetail) {    	
    	TxMadrid txMadrid = madridService.selectOne("id", no);    	
        MadridDetailInfo mdi = new MadridDetailInfo();
    	if (txMadrid != null) {
        	
        	String xmlString = txMadrid.getDocXMLDetail();
        	SAXBuilder saxBuilder=new SAXBuilder();        	 
        	StringReader stringReader=new StringReader(xmlString);
        	try {  
				Document jdomDocument=saxBuilder.build(stringReader);
				Element root =  jdomDocument.getRootElement();	
				if (root.getAttributeValue("ORIGLAN")!=null ) {
					mdi.setOriginalLanguage(root.getAttributeValue("ORIGLAN"));
				}
				if(root.getAttributeValue("DOCID")!=null) {
					mdi.setDocumentId(root.getAttributeValue("DOCID"));
				}
				if(root.getAttributeValue("REGRDAT")!=null) {
					mdi.setDateRecord(root.getAttributeValue("REGRDAT")); 
				}
				if(root.getAttributeValue("NOTDATE")!=null) {
					mdi.setNotificationDate(root.getAttributeValue("NOTDATE"));
				}
				if(root.getAttributeValue("REGEDAT")!=null) {
					mdi.setEffectiveDate(root.getAttributeValue("REGEDAT"));
				}
				if(root.getAttributeValue("OOCD")!=null) {
					mdi.setOfficeOrigin(root.getAttributeValue("OOCD"));
				}
				if(root.getAttributeValue("EXPDATE")!=null) {
					mdi.setExpiryDate(root.getAttributeValue("EXPDATE"));
				}

				mdi.setInternationalRegistrationNumber("INTREGN");
				if(root.getAttributeValue("INTREGD")!=null) {
					mdi.setInternationalRegistrationDate(root.getAttributeValue("INTREGD"));
				}
				if(root.getAttributeValue("DESUNDER")!=null) {
					mdi.setInstrumentContractingPartyDesign(root.getAttributeValue("DESUNDER"));
				}				
				
				//holder harus dibuat sebagai table, karena ada tenuan tag HOLGR lebih dari 1.
				List<Element> eList = jdomDocument.getRootElement().getChildren("HOLGR");		
				ArrayList<MadridHolding> mhList = new ArrayList<>();				
				if(eList !=null) {
					for(Element el : eList) {																																				
						MadridHolding mh = new MadridHolding();						
						mh.setHoldCLID(el.getAttributeValue("CLID"));										
						mh.setHoldName(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_NAME ));
						mh.setHoldAddress(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_ADDRESS ));						
						mh.setHoldCountry(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_COUNTRY ));														
						mh.setHoldEntitlement(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_ENTITLEMENT));						
						mh.setHoldLegalNature(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_LEGAL_NATURE));
						mh.setHoldPlaceIncorporated(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_PLACE_INCORPORATED));
						mh.setHoldCorrsAddress(findElementByAttribute(jdomDocument, el.getName(), new MadridHolding().ATTR_CLID, mh.getHoldCLID(), new MadridHolding().TAG_CORRESPONDENCE_ADDRESS));	 
						mhList.add(mh);	 											
					}					
				}  
				mdi.setHolderList(mhList);				
				mdi.setPrevHolderDetail(getTagElement(jdomDocument, "PHOLGR", "NAMEL").concat(getTagElement(jdomDocument, "PHOLGR", "ADDRL")).concat(
						getTagElement(jdomDocument, "PHOLGR", "COUNTRY") ) );								
				
				mdi.setCorrespondenceDetail(getTagElement(jdomDocument, "CORRGR", "NAMEL").concat(getTagElement(jdomDocument, "CORRGR", "ADDRL")).concat(getTagElement(jdomDocument, "CORRGR", "COUNTRY")) );								
				//mdi.setReprsDetail(getTagElement(jdomDocument, "REPGR", "NAMEL").concat(getTagElement(jdomDocument, "REPGR", "ADDRL")).concat(getTagElement(jdomDocument, "REPGR", "COUNTRY")) );				
			
				eList = jdomDocument.getRootElement().getChildren("REPGR");		
				ArrayList<MadridReprs> repList = new ArrayList<>();				
				if(eList !=null) {
					for(Element el : eList) {																																				
						MadridReprs mr = new MadridReprs();						
						mr.setRepCLID(el.getAttributeValue("CLID"));										
						mr.setRepName(findElementByAttribute(jdomDocument, el.getName(), new MadridReprs().ATTR_CLID, mr.getRepCLID(), new MadridReprs().TAG_NAME ));
						mr.setRepAddress(findElementByAttribute(jdomDocument, el.getName(), new MadridReprs().ATTR_CLID, mr.getRepCLID(), new MadridReprs().TAG_ADDRESS ));						
						mr.setRepCountry(findElementByAttribute(jdomDocument, el.getName(), new MadridReprs().ATTR_CLID, mr.getRepCLID(), new MadridReprs().TAG_COUNTRY ));																					 
						repList.add(mr);												
					}					
				} 
				mdi.setReprsList(repList);		
				
				
				Element viennaList = root.getChild("VIENNAGR");
				ElementFilter filterVienna = new ElementFilter("VIECLAI");				
				String temp = "";
				if ( viennaList!=null) {
					for(Element c : viennaList.getDescendants(filterVienna)) {					
						temp += c.getTextNormalize().substring(0, 2) + "." + c.getTextNormalize().substring(2, c.getTextNormalize().length()) + ",";
					}
					//remove last character ','
					if(temp.endsWith(",")) {
						temp = temp.substring(0, temp.length()-1);				
					}					
				}
				mdi.setVienna(temp);				
											
				temp = "";
				if ( viennaList!=null) {
					ElementFilter filterViennaCLA = new ElementFilter("VIECLA3");							 
					for(Element c : viennaList.getDescendants(filterViennaCLA)) {					
						temp +=  c.getTextNormalize() +", " ;
					}
				}
				if(temp.endsWith(",")) {
					temp = temp.substring(0, temp.length()-1);				
				}
				
				mdi.setVIECLA3(temp);
				
				 eList = jdomDocument.getRootElement().getChildren("VRBLNOT");
				temp = "";
				if(eList !=null) { 
					for(Element el : eList) {		
						String[] parts = el.getValue().toString().trim().split("\n", 500);
						for (String s : parts) {
							temp+= s +",";	
						}
					}						
				}				
				mdi.setVRBLNOT(temp=="" ? "VRBLNOT" : temp );
				
				//brand, default is MARCOLI	
				MBrandType mBrandType =null;
				if("MARCOLI".contains(xmlString)) {
					mBrandType = brandService.selectOne("xmlMadrid", "MARCOLI");					
				} else if("MARKVE".contains(xmlString)) {
					mBrandType = brandService.selectOne("xmlMadrid", "MARKVE");		
				}else if("SOUMARI".contains(xmlString)) {
					mBrandType = brandService.selectOne("xmlMadrid", "SOUMARI");			
				}else if("THRDMAR".contains(xmlString)) {
					mBrandType = brandService.selectOne("xmlMadrid", "THRDMAR");			
				}else {
					mBrandType = brandService.selectOne("xmlMadrid", "MARCOLI");	
				}
				
				mdi.setBrandType(mBrandType);
				mdi.setBrandTypeName(mBrandType.getName()); //display in user interface
				
				//<IMAGE NAME="1430298" TEXT="Youm Balance" COLOUR="N" TYPE="JPG" />
				String filename="";
				String brandName="";
				Element eImg = jdomDocument.getRootElement().getChild("IMAGE");
				if(eImg!=null) {
					filename = eImg.getAttributeValue("NAME").concat(".").concat(eImg.getAttributeValue("TYPE"));
					brandName = eImg.getAttributeValue("TEXT");
				}
				mdi.setFileName(filename);
				mdi.setBrandName(brandName);
				mdi.setMarkDescription(getTagElement(jdomDocument, "MARDESGR", "MARDESEN"));				
				mdi.setColorsClaimed(getTagElement(jdomDocument, "COLCLAGR", "COLCLAEN"));
				mdi.setDisclaimer(getTagElement(jdomDocument, "DISCLAIMGR", "DISCLAIMEREN"));
				mdi.setTranslation(getTagElement(jdomDocument, "MARTRGR", "MARTREN"));
				mdi.setMarkDescriptionChar(getSingleElement(jdomDocument.getRootElement().getChild("MARTRAN")));
				mdi.setMarkinColorEn(getTagElement(jdomDocument, "COLCLAGR", "COLPAREN"));
				mdi.setMarkVoluntary(getTagElement(jdomDocument, "VOLDESGR", "VOLDESEN"));				
				
				String tempClass= "";				
		    	Element e = null;
		    	ElementFilter ef = new ElementFilter();
		    	
		    	e = jdomDocument.getRootElement().getChild("BASICGS");	
		    	Map<String, String> hClass= new HashMap<String, String> ();
				if(e !=null) {
					ef = new ElementFilter("GSGR");					 
					for(Element c : e.getDescendants(ef)) {
						tempClass = c.getAttributeValue("NICCLAI");						 
						if(!tempClass.isEmpty()) {
							ElementFilter efDetail = new ElementFilter("GSTERMEN");
							for(Element elDetail : c.getDescendants(efDetail)) {
								hClass.put(tempClass, elDetail.getTextNormalize()) ;									 
							}
						}
					}
				}    				
				TreeMap<String, String> sortedMap = new TreeMap<>(); 		  
		        sortedMap.putAll(hClass);								
				mdi.setBasicList(sortedMap);								
				 
				eList = jdomDocument.getRootElement().getChildren("BASGR");	
				Map<String, String> hApp = new HashMap<String, String> ();
				if(eList !=null) {
					for(Element el : eList) {											
						String[] parts = el.getValue().toString().trim().split("\n", 2);								
						Date d  = new SimpleDateFormat("yyyyMMdd").parse(parts[0].trim());
						String appDate = new SimpleDateFormat("dd/MM/yyyy").format(d);						
						hApp.put( parts[1].trim(), appDate);						
					}
					
				}  
			 
				mdi.setBasicDetail(hApp); 
				
				eList = jdomDocument.getRootElement().getChildren("PRIGR");		
				ArrayList<MadridPrior> mpList = new ArrayList<>(); 
				if(eList !=null) {
					for(Element el : eList) {											
						String[] parts = el.getValue().toString().trim().split("\n", 5);							
						MadridPrior mp = new MadridPrior();
						mp.setPriorCountry(parts[0].trim());
						mp.setPriorDate(new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyyMMdd").parse(parts[1].trim())) );
						mp.setPriorNo(parts[2].trim());						
						String clai = el.getChild("PRIGS")!=null ? el.getChild("PRIGS").getAttributeValue("NICCLAI") : "";
						mp.setClassNo(clai);
						mpList.add(mp);												
					}					
				}   				
				mdi.setPriorityDetail(mpList);
				
				eList = jdomDocument.getRootElement().getChildren("DESPG");
				temp = "";
				if(eList !=null) {
					for(Element el : eList) {		
						String[] parts = el.getValue().toString().trim().split("\n", 500);
						for (String s : parts) {
							temp+= s +",";
						}
					}						
				}
				
				if(temp.endsWith(",")) {
					temp = temp.substring(0, temp.length()-1);				
				}
				
				mdi.setProtocolDesignation(temp);
				mdi.setLimitation(getTagElement(jdomDocument, "LIMGR", "GSHEADEN"));
				
				
				e = jdomDocument.getRootElement();	
		    	Map<String, String> hLim= new HashMap<String, String> ();
				if(e !=null) {
					ef = new ElementFilter("LIMTO");					 
					for(Element c : e.getDescendants(ef)) {
						tempClass = c.getAttributeValue("NICCLAI");						 
						if(!tempClass.isEmpty()) {
							ElementFilter efDetail = new ElementFilter("GSTERMEN");
							for(Element elDetail : c.getDescendants(efDetail)) {
								hLim.put(tempClass, elDetail.getTextNormalize()) ;									 
							}
						}
					}
				}    				
				sortedMap = new TreeMap<>(); 		  
		        sortedMap.putAll(hLim);								
				mdi.setLimtoList(sortedMap);
				
				
				eList = jdomDocument.getRootElement().getChildren("DESPG2");
				temp = "";
				if(eList !=null) {
					for(Element el : eList) {		
						String[] parts = el.getValue().toString().trim().split("\n", 500);
						for (String s : parts) {
							temp+= s +",";
						}
					}						
				}				
				mdi.setDESPG2(temp );
        	} catch (JDOMException | ParseException e) {			
				e.printStackTrace();				
			} catch (IOException e) {
				e.printStackTrace();				
			}
    	
    	} else {
    		
    	}
		return mdi;
    }*/
}
