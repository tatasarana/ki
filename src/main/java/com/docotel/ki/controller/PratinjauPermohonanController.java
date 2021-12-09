package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.PriorStatusEnum;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.MAction;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.custom.master.MCountryCostumRepository;
import com.docotel.ki.repository.transaction.TxTmClassLimitationRepository;
import com.docotel.ki.repository.transaction.TxTmReferenceRepository;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.*;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BadPdfFormatException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PratinjauPermohonanController extends BaseController {
    private static final String DIRECTORY_PAGE = "permohonan";
    private static final String PAGE_PRATINJAU_PERMOHONAN = DIRECTORY_PAGE + "/pratinjau-permohonan";
    private static final String PATH_UBAH_PERMOHONAN_LICENSE = "/form-ubah-lisensi";
    private static final String PATH_AJAX_SEARCH_LIST_REGISTRATION = "/cari-data-registration";
    private static final String PATH_AJAX_SEARCH_LIST_LICENSE = "/cari-data-license";
    private static final String PATH_EDIT_PERMOHONAN = "/edit-permohonan";
    private static final String PATH_DWN_FILE = "/dwn/{isIPAS}/{file}";
    private static final String PATH_PRATINJAU_PERMOHONAN = "/pratinjau-permohonan";
    private static final String PATH_CETAK_MEREK_PRATINJAU = "/cetak-merek-pratinjau";
    private static final String PATH_CETAK_MADRID_PRATINJAU = "/cetak-madrid-pratinjau";
    private static final String PATH_CETAK_MEREK_PRATINJAU2 = "/cetak-merek-pratinjau-debug";
    private static final String PATH_CETAK_SURAT_PERNYATAAN_PRATINJAU = "/cetak-surat-pernyataan-pratinjau";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_REGISTRATION = PATH_AJAX_SEARCH_LIST_REGISTRATION + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_LICENSE = PATH_AJAX_SEARCH_LIST_LICENSE + "*";
    private static final String REQUEST_MAPPING_EDIT_PERMOHONAN = PATH_AFTER_LOGIN + PATH_EDIT_PERMOHONAN;
    private static final String REQUEST_MAPPING_PRATINJAU_PERMOHONAN = PATH_PRATINJAU_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_CETAK_MEREK_PRATINJAU = PATH_CETAK_MEREK_PRATINJAU + "*";
    private static final String REQUEST_MAPPING_CETAK_MADRID_PRATINJAU = PATH_CETAK_MADRID_PRATINJAU + "*";
    private static final String REQUEST_MAPPING_CETAK_MEREK_PRATINJAU2 = PATH_CETAK_MEREK_PRATINJAU2 + "*";
    private static final String REQUEST_MAPPING_CETAK_SURAT_PERNYATAAN_PRATINJAU = PATH_CETAK_SURAT_PERNYATAAN_PRATINJAU + "*";
    @Autowired
    RegistrationService registrationService;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ClassService classService;
    @Autowired
    private PermohonanService permohonanService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    @Autowired
    private ReprsService reprsService;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private CityService cityService;
    @Autowired
    private ProvinceService provinceService;
    @Autowired
    private LookupService lookupService;
    @Autowired
    private TxTmClassLimitationRepository txTmClassLimitationRepository;
    @Autowired
    private TxTmReferenceRepository txTmReferenceRepository;
    @Autowired
    private MadridOoService madridOoService;
    @Autowired
    ResourceLoader resourceLoader;
    @Autowired
    MCountryCostumRepository mCountryCostumRepository;


    @Autowired
    private IPASService ipasService ;

    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;
    @Value("${upload.file.doc.application.path:}")
    private String uploadFileDocApplicationPath;
    private FileInputStream fileInputStreamReader;
    @Value("${upload.file.image.tandatangan.path}")
    private String uploadFileImageTandaTangan;
    @Value("${upload.ipas.file.path}")
    private String uploadIPASDoc;
    @Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;
    @Value("${upload.file.path.signature:}")
    private String uploadFilePathSignature;
    @Value("${upload.file.branddetail.path:}")
    private String uploadFileBrandDetailPath;
    @Value("${download.output.permohonan.cetakmerek.file.path:}")
    private String downloadFileDocPermohonanCetakMerekPath;
    @Value("${upload.file.web.image:}")
    private String pathImage;
    @Value("${download.output.madridOO.cetakmerek.file.path}")
    private String downloadFileDocMadridOOCetakPath;
    private Object SQLException;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "permohonanMerek");
        model.addAttribute("subMenu", "permohonan");
    }

    @RequestMapping(path = PATH_DWN_FILE, method = {RequestMethod.GET})
    @ResponseBody
    public void doDownloadFile(@PathVariable("file") String fileInput, @PathVariable("isIPAS") Boolean isIPAS, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String pathIPAS = "";
        if(isIPAS) {
            pathIPAS = this.uploadIPASDoc + "/";
        }
        String fullPath = pathIPAS + new String(Base64.getDecoder().decode(fileInput));
        File file = new File(fullPath);
        String fileName = file.getName();

        OutputStream stream = response.getOutputStream();

//        HttpHeaders respHeaders = new HttpHeaders();
//        respHeaders.setContentType(file.get);
//        response.setContentLength(file.length());
//        respHeaders.setContentDispositionFormData("attachment", fileName);
//        response.setContentDispositionFormData("inline", fileName);

//        InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
        String ext = fileName.substring(fileName.length()-3);
        String contentType = ext.equalsIgnoreCase("pdf")
            ? "application/pdf" : "image/" + ext;
//        response.setContentType(Files.probeContentType(file.toPath()));
        response.setContentType(contentType);
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "inline; filename=" + fullPath + ";");
        stream.write(Files.readAllBytes(file.toPath()));
        stream.flush();
        stream.close();
//        return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
    }

    @RequestMapping(path = REQUEST_MAPPING_PRATINJAU_PERMOHONAN, method = RequestMethod.GET)
    public String showPratinjau(Model model, @RequestParam(value = "no", required = false) String no, final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        //General & Pemohon
        try {
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
            //If condition when MAction value = null
            if (txTmGeneral.getmAction() == null) {
                MAction mAction = new MAction();
                mAction.setName("-");
                txTmGeneral.setmAction(mAction);
            }

            TxReception txReception = txTmGeneral.getTxReception();
            /*TxTmOwner txTmOwner = permohonanService.selectOneOwnerByApplicationNo(txTmGeneral.getId());*/


            //KUASA
            List<TxTmRepresentative> txTmRepresentative = reprsService.selectAllReprsByIdGeneral(txTmGeneral.getId());

            if (txTmRepresentative == null) {
                txTmRepresentative = new List<TxTmRepresentative>() {

                    @Override
                    public <T> T[] toArray(T[] a) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Object[] toArray() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public List<TxTmRepresentative> subList(int fromIndex, int toIndex) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public int size() {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public TxTmRepresentative set(int index, TxTmRepresentative element) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public TxTmRepresentative remove(int index) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean remove(Object o) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public ListIterator<TxTmRepresentative> listIterator(int index) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public ListIterator<TxTmRepresentative> listIterator() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public int lastIndexOf(Object o) {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public Iterator<TxTmRepresentative> iterator() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean isEmpty() {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public int indexOf(Object o) {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public TxTmRepresentative get(int index) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean containsAll(Collection<?> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean contains(Object o) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public void clear() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public boolean addAll(int index, Collection<? extends TxTmRepresentative> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends TxTmRepresentative> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public void add(int index, TxTmRepresentative element) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public boolean add(TxTmRepresentative e) {
                        // TODO Auto-generated method stub
                        return false;
                    }
                };
            }
            
            /*if (txTmRepresentative == null){
                txTmRepresentative = new TxTmRepresentative();
                txTmRepresentative.setmRepresentative(new MRepresentative());
            }*/

            //PEMOHON
            List<TxTmOwner> txTmOwnerList = permohonanService.selectAllOwnerByIdGeneral(txTmGeneral.getId());

            TxTmOwner txTmOwner = permohonanService.selectOneOwnerByApplicationNo(txTmGeneral.getId());
            
            List<TxTmOwnerDetail> txTmOwnerDetailList = null;
            List<DataOwner> dataOwnerList = new ArrayList<DataOwner>();
            if(txTmOwner!=null) {
            	txTmOwnerDetailList = permohonanService.selectAllOwnerByOwnerId(txTmOwner.getId());

                String detail_name = "";
                String country = "";
                
                for (TxTmOwner owners : txTmOwnerList) {
                    //System.out.println("dump data " + owners.getId());
                    detail_name = "-";
                    DataOwner dOwner = new DataOwner();
                    dOwner.setId(owners.getId());
                    dOwner.setOwnerName(owners.getName());
                    for (TxTmOwnerDetail txTmOwnerDetail : owners.getTxTmOwnerDetails()) {
                        if (detail_name.equalsIgnoreCase("-")) {
                            detail_name = txTmOwnerDetail.getName();
                        } else {
                            detail_name = detail_name + "; " + txTmOwnerDetail.getName();
                        }
                    }

                    /**
                     * handle jika tm_owner_country = "-"
                     */
                    String temp_country = "";
                    try{
                        temp_country = owners.getmCountry().getName();
                    }catch (Exception e){
                        MCountry mCountry = mCountryCostumRepository.selectOne("id", "99");
                        owners.setmCountry(mCountry);
                        temp_country = owners.getmCountry().getName();
                    }

                    dOwner.setOwnerDetailName(detail_name);

                    dOwner.setOwnerAddress(owners.getAddress());
                    dOwner.setOwnerEmail(owners.getEmail());
                    dOwner.setOwnerCountry(temp_country);
                    dOwner.setOwnerPhone(owners.getPhone());
                    dOwner.setOwnerStatus(owners.isStatus());
                    dOwner.setOwnerUpdatedBy(owners.getUpdatedBy().getUsername());
                    dataOwnerList.add(dOwner);
                }
            }
            

            if (txTmOwnerList == null) {
                txTmOwnerList = new List<TxTmOwner>() {
                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public Iterator<TxTmOwner> iterator() {
                        return null;
                    }

                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @Override
                    public <T> T[] toArray(T[] a) {
                        return null;
                    }

                    @Override
                    public boolean add(TxTmOwner txTmOwner) {
                        return false;
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends TxTmOwner> c) {
                        return false;
                    }

                    @Override
                    public boolean addAll(int index, Collection<? extends TxTmOwner> c) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }

                    @Override
                    public TxTmOwner get(int index) {
                        return null;
                    }

                    @Override
                    public TxTmOwner set(int index, TxTmOwner element) {
                        return null;
                    }

                    @Override
                    public void add(int index, TxTmOwner element) {

                    }

                    @Override
                    public TxTmOwner remove(int index) {
                        return null;
                    }

                    @Override
                    public int indexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public int lastIndexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public ListIterator<TxTmOwner> listIterator() {
                        return null;
                    }

                    @Override
                    public ListIterator<TxTmOwner> listIterator(int index) {
                        return null;
                    }

                    @Override
                    public List<TxTmOwner> subList(int fromIndex, int toIndex) {
                        return null;
                    }
                };
            }

            //PRIORITAS
            List<TxTmPrior> txTmPriorList = permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId());
            List<TxTmPrior> tempTxTmPriorList = new ArrayList<>();
            if (txTmPriorList != null) {
                for (TxTmPrior temp : txTmPriorList){
                    TxTmPrior addingPrior = temp;
                    addingPrior.setStatus(PriorStatusEnum.valueOf(temp.getStatus().trim().toUpperCase()).getLabel());
                    tempTxTmPriorList.add(addingPrior);
//                    logger.info("STATUS txtmprior "+addingPrior.getStatus());

                }
            }

            //MEREK
            TxTmBrand txTmBrand = brandService.selectOneByAppId(txTmGeneral.getId());
            if(txTmBrand!=null) {
            	try {
                    String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
                    File file = new File(uploadFileBrandPath + pathFolder + txTmBrand.getId() + ".jpg");
                    if (file.exists() && !file.isDirectory()) {
                        fileInputStreamReader = new FileInputStream(file);
                        byte[] bytes = new byte[(int) file.length()];
                        fileInputStreamReader.read(bytes);
                        model.addAttribute("imgMerek", "data:image/jpg;base64," + Base64.getEncoder().encodeToString(bytes));
                    } else {
                        model.addAttribute("imgMerek", "");
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
            	txTmBrand = new TxTmBrand();
            }
            

            //KELAS ACCEPT
            List<TxTmClass> txTmClassListAccept = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
            Map<Integer, String[]> listTxTmClassAccept = new TreeMap<>();
            int keyA;
            int ica = 0;
            String descA = "";
            String descEnA = "";
            for (TxTmClass result : txTmClassListAccept) {
                keyA = result.getmClass().getNo();
                descA = result.getmClassDetail().getDesc();
                descEnA = result.getmClassDetail().getDescEn() != null ? result.getmClassDetail().getDescEn() : descEnA;
                if (listTxTmClassAccept.containsKey(keyA)) {
                    descA = listTxTmClassAccept.get(keyA)[0] + "; " + descA;
                    descEnA = result.getmClassDetail().getDescEn() != null
                            ? listTxTmClassAccept.get(keyA)[1] + "; " + descEnA
                                : txTmClassListAccept.size() - 1 == ica && !(descEnA.equalsIgnoreCase("") || descEnA.equalsIgnoreCase("-"))
                            ? listTxTmClassAccept.get(keyA)[1].substring(2)
                                : descEnA;
                }
                listTxTmClassAccept.put(keyA, new String[]{descA, descEnA});
                ica++;
            }

            //KELAS REJECT
            List<TxTmClass> txTmClassListReject = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.REJECT.name());
            Map<Integer, String[]> listTxTmClassReject = new TreeMap<>();
            int keyR;
            String descR = "";
            String descEnR = "";
            for (TxTmClass result : txTmClassListReject) {
            	keyR = result.getmClass().getNo();
                descR = result.getmClassDetail().getDesc();
                descEnR = result.getmClassDetail().getDescEn();

                if (listTxTmClassReject.containsKey(keyR)) {
                    descR = listTxTmClassReject.get(keyR)[0] + "; " + descR;
                    descEnR = listTxTmClassReject.get(keyR)[1] + "; " + descEnR;
                }
                listTxTmClassReject.put(keyR, new String[]{descR, descEnR});
            }

            String is_migrated = "" ;

            //Dokumen Lampiran
            List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral == null ? "" : txTmGeneral.getId());
            String image = "";
            /*List<TxTmDoc> txtmDocList = doclampiranService.selectAllDocByGeneralId(txTmGeneral.getId());*/
            
            //Referensi List
            List<TxTmReference> txTmReferenceList = txTmReferenceRepository.getByTxTmGeneralId(txTmGeneral.getId());

            List<TxTmReference> searchRefIds = txTmReferenceRepository.searchRefId(txTmGeneral.getId());
            String searchRefIdStr = "-";
            for( TxTmReference srID : searchRefIds) {
                if(searchRefIdStr == "-"){
                    searchRefIdStr = srID.getTxTmGeneral().getApplicationNo();
                } else {
                    searchRefIdStr += "," + srID.getTxTmGeneral().getApplicationNo();
                }
            }
            model.addAttribute("searchRefId", searchRefIdStr);

            String[] urlpath = null ;
            int jz = 0 ;

            for (TxTmDoc result : txtmDocList) {
                File file = null;
                String pathFolder = DateUtil.formatDate(result.getCreatedDate(), "yyyy/MM/dd/");

                if(result.getFileNameTemp().toUpperCase().endsWith(".JPG") || result.getFileNameTemp().toUpperCase().endsWith(".JPEG")) {
                    String fullPathExist = uploadFileImageTandaTangan + pathFolder + result.getFileNameTemp();
                    file = new File(fullPathExist);
                	if (file.exists() && !file.isDirectory()) {
//                        fileInputStreamReader = new FileInputStream(file);
//                        byte[] bytes = new byte[(int) file.length()];
//                        fileInputStreamReader.read(bytes);
//                        image = "data:image/jpeg;base64" + "," + Base64.getEncoder().encodeToString(bytes);
//                        result.setFileName(image);
                        String dwnUrl = "/layanan/dwn/0/"  + Base64.getEncoder().encodeToString(fullPathExist.getBytes());
                        result.setFileName(dwnUrl);
                        result.setXmlFileId("MEREK");

                	}
                	else{
                	    String applicationNo = result.getTxTmGeneral().getId();
                	    String pathquery = ipasService.findDocIPASS(applicationNo, null);
                	    String fullPath = this.uploadIPASDoc + "/" + pathquery;
                        //System.out.println("++++++++++++++PATH nya: "+  pathquery );

                        file = new File (fullPath);
                        if (file.exists() && !file.isDirectory()) {
                            String dwnUrl = "/layanan/dwn/1/"  + Base64.getEncoder().encodeToString(pathquery.getBytes());
                            result.setFileName(dwnUrl);
                            result.setXmlFileId("IPAS");

                        }
                        else{
                            //System.out.println("<<<<<<<  Maaf File untuk "+result.getFileNameTemp()+ " Tidak Ditemukan di "+ pathquery );
                        }

                        is_migrated = "Note: Dokumen Aplikasi ini ada yang Hilang atau Belum Di-migrasi dari IPASS" ;
                    }
                } else {

                    String fullPathExist = uploadFileDocApplicationPath + pathFolder + result.getFileNameTemp();
                	file = new File(fullPathExist);
//                    System.out.println(">>>>>>> File yg ingin dicari: "+ fullPathExist);

                    if (file.exists() && !file.isDirectory()) {
                        String dwnUrl = "/layanan/dwn/0/"  + Base64.getEncoder().encodeToString(fullPathExist.getBytes());
                        result.setFileName(dwnUrl);
                        result.setXmlFileId("MEREK");
                    }
                    else{
                        String applicationNo = result.getTxTmGeneral().getId();
                        String pathquery = ipasService.findDocIPASS(applicationNo, null);
                        String fullPath = this.uploadIPASDoc + "/" + pathquery;
                        //System.out.println("++++++++++++++PATH nya: "+  pathquery );
//                        String thefile = uploadIPASDoc  + pathquery  ;
                          file = new File ( fullPath);
                        if (file.exists() && !file.isDirectory()) {
                            String dwnUrl = "/layanan/dwn/1/"  + Base64.getEncoder().encodeToString(pathquery.getBytes());
                            result.setFileName(dwnUrl);
                            result.setXmlFileId("IPAS");

                            //System.out.println("================  OK, FIle IPASS nya ada untuk " + result.getFileNameTemp());
                        }
                        else{
                            //System.out.println("<<<<<<<  Maaf File untuk "+result.getFileNameTemp()+ " Tidak Ditemukan di "+ pathquery );
                        }

                        is_migrated = "Note: Dokumen Aplikasi ini ada yang Hilang atau Belum Di-migrasi dari IPASS" ;
                    }

                }
                jz++ ;
            }

            if (txtmDocList == null) {
                txtmDocList = new List<TxTmDoc>() {
                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public Iterator<TxTmDoc> iterator() {
                        return null;
                    }

                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @Override
                    public <T> T[] toArray(T[] a) {
                        return null;
                    }

                    @Override
                    public boolean add(TxTmDoc listDocType) {
                        return false;
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends TxTmDoc> c) {
                        return false;
                    }

                    @Override
                    public boolean addAll(int index, Collection<? extends TxTmDoc> c) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }

                    @Override
                    public TxTmDoc get(int index) {
                        return null;
                    }

                    @Override
                    public TxTmDoc set(int index, TxTmDoc element) {
                        return null;
                    }

                    @Override
                    public void add(int index, TxTmDoc element) {

                    }

                    @Override
                    public TxTmDoc remove(int index) {
                        return null;
                    }

                    @Override
                    public int indexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public int lastIndexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public ListIterator<TxTmDoc> listIterator() {
                        return null;
                    }

                    @Override
                    public ListIterator<TxTmDoc> listIterator(int index) {
                        return null;
                    }

                    @Override
                    public List<TxTmDoc> subList(int fromIndex, int toIndex) {
                        return null;
                    }
                };
            }

            /*Publikasi*/
            TxPubsJournal txPubsJournal = trademarkService.selectOnePubJournalByAppId(txTmGeneral.getId());
            if (txPubsJournal == null) {
                txPubsJournal = new TxPubsJournal();
            }

            TxRegistration txRegistration = registrationService.selectOne(txTmGeneral.getId());
            if (txRegistration == null) {
                txRegistration = new TxRegistration();
            }
            //List<TxRegistration> txRegistrationList = registrationService.selectAll(txTmGeneral.getId());

            //LISENSI
            List<TxLicense> txLicenseList = licenseService.selectAllLicenseByIdGeneral(txTmGeneral.getId());

            if (txLicenseList == null) {
                txLicenseList = new List<TxLicense>() {

                    @Override
                    public <T> T[] toArray(T[] a) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Object[] toArray() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public List<TxLicense> subList(int fromIndex, int toIndex) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public int size() {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public TxLicense set(int index, TxLicense element) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public TxLicense remove(int index) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean remove(Object o) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public ListIterator<TxLicense> listIterator(int index) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public ListIterator<TxLicense> listIterator() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public int lastIndexOf(Object o) {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public Iterator<TxLicense> iterator() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean isEmpty() {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public int indexOf(Object o) {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public TxLicense get(int index) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean containsAll(Collection<?> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean contains(Object o) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public void clear() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public boolean addAll(int index, Collection<? extends TxLicense> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends TxLicense> c) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public void add(int index, TxLicense element) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public boolean add(TxLicense e) {
                        // TODO Auto-generated method stub
                        return false;
                    }
                };
            }

            model.addAttribute("appNo", no);

            String madrid_id = "";
            try{
                TxMadrid txMadrid = txTmGeneral.getMadrid_id();
                madrid_id = txMadrid.getUuid();
            }catch (Exception e){}


            model.addAttribute("no_madrid", madrid_id);
            model.addAttribute("dataGeneral", txTmGeneral);


            TxTmMadridFee txTmMadridFee = txTmGeneral.getTxTmMadridFee();
            if (txTmMadridFee == null) {
                txTmMadridFee = new TxTmMadridFee();
            } else {
                txTmMadridFee.setTmpLanguage2(txTmGeneral.getLanguage2());
            }


            List<TxTmMadridFeeDetail> txTmMadridFeeDetails = txTmMadridFee.getTxTmMadridFeeDetails();



            if (txTmMadridFeeDetails == null) {
                txTmMadridFeeDetails = new ArrayList<>();
            }

            model.addAttribute("txTmMadridFee", txTmMadridFee);
            model.addAttribute("txTmMadridFeeDetails", txTmMadridFeeDetails);


            model.addAttribute("noGeneral", txTmGeneral.getApplicationNo());
            model.addAttribute("dataLoketPenerimaan", txReception);
            //model.addAttribute("pemohon", txTmOwnerList);
            model.addAttribute("pemohon", dataOwnerList);
            model.addAttribute("listPemohonChild", txTmOwnerDetailList==null ? new TxTmOwnerDetail() : txTmOwnerDetailList);
            model.addAttribute("txTmReprs", txTmRepresentative==null ? new TxTmRepresentative() : txTmRepresentative);
            model.addAttribute("txTmBrand", txTmBrand);
            model.addAttribute("txTmPrior", tempTxTmPriorList==null ? new TxTmPrior() : tempTxTmPriorList );
            //model.addAttribute("txTmClassList",txTmClassList);
            model.addAttribute("listTxTmClassAccept", listTxTmClassAccept==null ? new TxTmClass() : listTxTmClassAccept );
            model.addAttribute("listTxTmClassReject", listTxTmClassReject==null? new TxTmClass() : listTxTmClassReject);
            model.addAttribute("listDocType", txtmDocList==null ? new TxTmDoc() : txtmDocList);
            model.addAttribute("listReference", txTmReferenceList==null ? new TxTmReference() : txTmReferenceList);
            model.addAttribute("is_migrated",is_migrated);
            model.addAttribute("image", image);
            model.addAttribute("txPubsJournal", txPubsJournal);
            model.addAttribute("txRegistration", txRegistration);
            model.addAttribute("notFirstRegistration", registrationService.countDetail("txRegistration.id", txRegistration.getId()) > 1);
            //model.addAttribute("txRegistrationList",txRegistrationList);
            model.addAttribute("txLicenseList", txLicenseList==null ? new TxLicense() : txLicenseList);
            return PAGE_PRATINJAU_PERMOHONAN;
        } catch (NullPointerException e) {
        	return PAGE_PRATINJAU_PERMOHONAN;
            //return "redirect:" + REQUEST_MAPPING_EDIT_PERMOHONAN + "?no=" + no;
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_AJAX_SEARCH_LIST_REGISTRATION, method = {RequestMethod.POST})
    public void doSearchDataTablesListRegistration(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            searchCriteria = new ArrayList<>();
            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    }
                }
            }

            String orderBy = request.getParameter("orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                }
            }

            String orderType = request.getParameter("orderType");
            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("DESC")) {
                    orderType = "ASC";
                }
            }

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxRegistrationDetail> searchResult = registrationService.selectAll(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxRegistrationDetail result : searchResult.getList()) {
                    no++;

                    data.add(new String[]{
                            "" + no,
                            result.getTxRegistration().getNo(),
                            result.getStartDateTemp(),
                            result.getEndDateTemp(),
                            result.isStatus() ? "Aktif" : "Tidak Aktif",
                            result.getCreatedBy().getUsername()
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @RequestMapping(path = REQUEST_MAPPING_AJAX_SEARCH_LIST_LICENSE, method = {RequestMethod.POST})
    public void doSearchDataTablesListLicense(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            searchCriteria = new ArrayList<>();
            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    }
                }
            }

            String orderBy = request.getParameter("orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                }
            }

            String orderType = request.getParameter("orderType");
            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("DESC")) {
                    orderType = "ASC";
                }
            }

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxLicense> searchResult = licenseService.selectAll(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxLicense result : searchResult.getList()) {
                    no++;

                    data.add(new String[]{
                            "" + no,
                            "<a href="+  getPathURLAfterLogin(PATH_UBAH_PERMOHONAN_LICENSE + "?id=" + result.getId())  +"&no=" + result.getTxTmGeneral().getApplicationNo() + ">" + result.getName() + "</a>",
                            result.getTxLicenseParent() == null ? "Utama" : result.getTxLicenseParent().getName(),
                            result.getAddress(),
                            result.getPhone(),
                            result.getmCountry().getName(),
                            result.getStartDateTemp(),
                            result.getEndDateTemp(),
                            result.isStatus() ? "Aktif" : "Tidak Aktif",
                            result.getCreatedBy().getUsername(),
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @RequestMapping(value = REQUEST_MAPPING_CETAK_MEREK_PRATINJAU2, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMerekPratinjau2(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

    	//CETAK MEREK AND SURAT PERNYATAAN SAVE ON PATH
        try {
            List<CetakMerek> dataListCetakMerek = new ArrayList<CetakMerek>();
            CetakMerek dataCetakMerek = null;
            
            String applicationNo = request.getParameter("no");
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);
            TxTmBrand txTmBrand  =  trademarkService.selectOneTxtmBrandBy("txTmGeneral.id",txTmGeneral.getId());

            dataCetakMerek = new CetakMerek(1, "DEBUG STRING Data Permohonan", "Application");
            dataCetakMerek.setApplicationNo("getApplicationNo()");
            dataCetakMerek.seteFilingNo("geteFilingNo()");
            dataCetakMerek.setFileSequence("mFileSequence().getDesc()");
            dataCetakMerek.setFileType("mFileType().getDesc()");
            dataCetakMerek.setFileTypeDetail("mFileTypeDetail().getDesc()");
//            dataCetakMerek.setApplicationDate(txTmGeneral.getTxReception().getApplicationDate());
            dataCetakMerek.setApplicationDate(new Timestamp(System.currentTimeMillis()));
//            dataCetakMerek.setFillingDate(txTmGeneral.getFilingDate());
            dataCetakMerek.setFillingDate(new Timestamp(System.currentTimeMillis()));
            dataListCetakMerek.add(dataCetakMerek);

            dataCetakMerek = new CetakMerek(2, "DEBUG STRING Data Merek", "Description of Mark");
            if (txTmGeneral.getTxTmBrand() != null) {
            	dataCetakMerek.setBrandName("txTmGeneral.getTxTmBrand().getName()");
            	dataCetakMerek.setBrandType("txTmGeneral.getTxTmBrand().getmBrandType().getName()");
            	dataCetakMerek.setBrandColor("txTmGeneral.getTxTmBrand().getColor()");
            	dataCetakMerek.setBrandDescription("txTmGeneral.getTxTmBrand().getDescription()");
            	dataCetakMerek.setBrandDescChar("txTmGeneral.getTxTmBrand().getDescChar()");
            	dataCetakMerek.setBrandTranslation("txTmGeneral.getTxTmBrand().getTranslation()");
            	dataCetakMerek.setBrandDisclaimer("txTmGeneral.getTxTmBrand().getDisclaimer()");
            	dataCetakMerek.setBrandFilename("txTmGeneral.getTxTmBrand().getId() + \".jpg\"");
            	
            }
            dataListCetakMerek.add(dataCetakMerek);

            List<TxTmOwner> txTmOwnerList = permohonanService.selectAllOwnerByIdGenerals(txTmGeneral.getId(), true);
            if (txTmOwnerList.size() == 0) {
            	dataCetakMerek = new CetakMerek(3, "DEBUG STRING Data Pemohon", "Applicant");
            	dataListCetakMerek.add(dataCetakMerek);
            } else {
            	for (TxTmOwner owners : txTmOwnerList) {
            		dataCetakMerek = new CetakMerek(3, "DEBUG STRING Data Pemohon", "Applicant");
            		dataCetakMerek.setOwnerName("owners.getName()");
            		dataCetakMerek.setOwnerType("owners.getOwnerType()");
            		dataCetakMerek.setOwnerNationality("owners.getNationality().getName()");
            		dataCetakMerek.setOwnerAddress("owners.getAddress()");
//                    dataCetakMerek.setOwnerCity(owners.getmCity() == null ? null : owners.getmCity().getName());
                    dataCetakMerek.setOwnerCity("owners.getmCity()");
//                    dataCetakMerek.setOwnerProvince(owners.getmProvince() == null ? null : owners.getmProvince().getName());
                    dataCetakMerek.setOwnerProvince("owners.getmProvince()");
//                    dataCetakMerek.setOwnerCountry(owners.getmCountry() == null ? null : owners.getmCountry().getName());
                    dataCetakMerek.setOwnerCountry("owners.getmCountry()");
            		dataCetakMerek.setOwnerZipCode("owners.getZipCode()");
            		dataCetakMerek.setOwnerPhone("owners.getPhone()");
            		dataCetakMerek.setOwnerEmail("owners.getEmail()");
            		dataListCetakMerek.add(dataCetakMerek);
            	}
            }
            
            if (txTmOwnerList.size() == 0) {
            	dataCetakMerek = new CetakMerek(4, "DEBUG STRING Alamat Surat Menyurat", "Mailing Address");
            	dataListCetakMerek.add(dataCetakMerek);
            }
            else {
            	for (TxTmOwner owners : txTmOwnerList) {
            		dataCetakMerek = new CetakMerek(4, "DEBUG STRING Alamat Surat Menyurat", "Mailing Address");
            		dataCetakMerek.setOwnerPostAddress("owners.getPostAddress()");
//                    dataCetakMerek.setOwnerPostCity(owners.getPostCity() == null ? null : owners.getPostCity().getName());
                    dataCetakMerek.setOwnerPostCity("owners.getPostCity()");
//                    dataCetakMerek.setOwnerPostProvince(owners.getPostProvince() == null ? null : owners.getPostProvince().getName());
                    dataCetakMerek.setOwnerPostProvince("owners.getPostProvince()");
//                    dataCetakMerek.setOwnerPostCountry(owners.getPostCountry() == null ? null : owners.getPostCountry().getName());
                    dataCetakMerek.setOwnerPostCountry("owners.getPostCountry()");
            		dataCetakMerek.setOwnerPostZipCode("owners.getPostZipCode()");
            		dataCetakMerek.setOwnerPostPhone("owners.getPostPhone()");
            		dataCetakMerek.setOwnerPostEmail("owners.getPostEmail()");
            		dataListCetakMerek.add(dataCetakMerek);
            	}
            }

            List<TxTmRepresentative> txTmReprsList = permohonanService.selectTxTmReprsByGeneralId(txTmGeneral.getId(), true);
            if (txTmReprsList.size() == 0) {
            	dataCetakMerek = new CetakMerek(5, "DEBUG STRING Data Kuasa", "Representative/IP Consultant");
            	dataListCetakMerek.add(dataCetakMerek);
            } else {
            	for(TxTmRepresentative txTmReprs : txTmReprsList) {
            		dataCetakMerek = new CetakMerek(5, "DEBUG STRING Data Kuasa", "Representative/IP Consultant");
            		dataCetakMerek.setReprsNo("txTmReprs.getmRepresentative().getNo()");
            		dataCetakMerek.setReprsName("txTmReprs.getmRepresentative().getName()");
            		dataCetakMerek.setReprsName("txTmReprs.getmRepresentative().getName()");
            		dataCetakMerek.setReprsOffice("txTmReprs.getmRepresentative().getOffice()");

//            		dataCetakMerek.setReprsAddress(txTmReprs.getmRepresentative().getAddress());
//            		dataCetakMerek.setReprsPhone(txTmReprs.getmRepresentative().getPhone());
//                    dataCetakMerek.setReprsEmail(txTmReprs.getmRepresentative().getEmail());
                    /**---GET FIELD ADDRESS, PHONE, EMAIL FROM TX_TM_REPRS **/
                    dataCetakMerek.setReprsAddress("txTmReprs.getAddress()");
                    dataCetakMerek.setReprsPhone("txTmReprs.getPhone()");
                    dataCetakMerek.setReprsEmail("txTmReprs.getEmail()");
                    dataListCetakMerek.add(dataCetakMerek);
            	}
            }

            List<TxTmPrior> txTmPriorList = permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId());
            if (txTmPriorList.size() == 0) {
            	dataCetakMerek = new CetakMerek(6, "DEBUG STRING Data Prioritas", "Priority Data");
            	dataListCetakMerek.add(dataCetakMerek);
            } else {
                int count = 1;
                for (TxTmPrior txTmPrior : txTmPriorList) {
                	dataCetakMerek = new CetakMerek(6, "DEBUG STRING Data Prioritas", "Priority Data");
                    dataCetakMerek.setPriorOrder("" + count++);
                    dataCetakMerek.setPriorNo("txTmPrior.getNo()");
                    dataCetakMerek.setPriorCountry("txTmPrior.getmCountry().getName()");
//                    dataCetakMerek.setPriorDate(txTmPrior.getPriorDate());
                    dataCetakMerek.setPriorDate(new Timestamp(System.currentTimeMillis()));
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            if (txTmGeneral.getTxTmClassList().size() == 0) {
            	dataCetakMerek = new CetakMerek(7, "DEBUG STRING Data Kelas", "Class");
            	dataListCetakMerek.add(dataCetakMerek);
            } else {
                Map<Integer, String[]> mapClass = new HashMap<>();
                List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
                for (TxTmClass result : txTmClassList) {
                    int key = result.getmClass().getNo();
                    String desc = "result.getmClassDetail().getDesc()";
                    String descEn = "result.getmClassDetail().getDescEn()";

                    if (mapClass.containsKey(key)) {
                        desc = mapClass.get(key)[0] + "; " + desc;
                        descEn = mapClass.get(key)[1] + "; " + descEn;
                    }

                    mapClass.put(key, new String[]{desc, descEn});
                }
                for (Map.Entry<Integer, String[]> entry : mapClass.entrySet()) {
                    String desc = entry.getValue()[0].replaceAll(" ", "").replaceAll(";", "");
                    String descEn = entry.getValue()[1].replaceAll(" ", "").replaceAll(";", "");

                    dataCetakMerek = new CetakMerek(7, "DEBUG STRING Data Kelas", "Class");
                    dataCetakMerek.setClassNo("\"\" + entry.getKey()");
//                    dataCetakMerek.setClassDesc(desc.length() == 0 ? " - " : entry.getValue()[0]);
                    dataCetakMerek.setClassDesc("desc.length()");
//                    dataCetakMerek.setClassDescEn(descEn.length() == 0 ? " - " : entry.getValue()[1]);
                    dataCetakMerek.setClassDescEn("descEn.length()");
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }
        	
            String tandaTanganDigital = "";
        	String pathFolderTandaTanganDigital = "";
        	
        	String docIdTTDPTemp = "";
        	String docIdTTDKTemp = "";
        	String txTmDocDateTTDK = "";
        	String txTmDocDateTTDP = "";
            List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
            if (txtmDocList.size() == 0) {
            	dataCetakMerek = new CetakMerek(8, "DEBUG STRING Dokumen Lampiran", "Attachment");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                Collections.sort(txtmDocList, (o1, o2) -> o1.getmDocType().getName().compareTo(o2.getmDocType().getName()));
                for (TxTmDoc txTmDoc : txtmDocList) {
                	if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK")) {
                		docIdTTDKTemp = "txTmDoc.getFileNameTemp()";
//                        txTmDocDateTTDK = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                        txTmDocDateTTDK = "txTmDoc.getCreatedDate()";
                	} else if (txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP")) {
                		docIdTTDPTemp = "txTmDoc.getFileNameTemp()";
//                        txTmDocDateTTDP = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                        txTmDocDateTTDP = "txTmDoc.getCreatedDate()";
                	}
            		
                	dataCetakMerek = new CetakMerek(8, "DEBUG STRING Dokumen Lampiran", "Attachment");
                	dataCetakMerek.setDocName("txTmDoc.getmDocType().getName()");
                	dataListCetakMerek.add(dataCetakMerek);
                }
            }
            if(txTmReprsList.size() > 0 && !docIdTTDKTemp.isEmpty()) {
            	tandaTanganDigital = docIdTTDKTemp; 
            	pathFolderTandaTanganDigital = txTmDocDateTTDK;
            } else {
            	tandaTanganDigital = docIdTTDPTemp; 
            	pathFolderTandaTanganDigital = txTmDocDateTTDP;
            }
            
            // Data Pemohon Tambahan
            List<TxTmOwnerDetail> txOwnerDetail = null;
            for (TxTmOwner owners : txTmOwnerList) {
            	txOwnerDetail = permohonanService.selectTxTmOwnerDetailByTxTmOwner(owners.getId(), true);
            	if (txOwnerDetail.size() == 0) {
            		dataCetakMerek = new CetakMerek(9, "DEBUG STRING Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                	try {
                		dataCetakMerek.setNo(null);
                	} catch (NumberFormatException e) {
                	}
                	dataCetakMerek.setOwnerDetailName("");
                	dataListCetakMerek.add(dataCetakMerek);
                } else {
                	int no = 0;
                	for (TxTmOwnerDetail ownerDetail : txOwnerDetail) {
                		no++;
                		dataCetakMerek = new CetakMerek(9, "DEBUG STRING Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                		dataCetakMerek.setNo(no);
                		dataCetakMerek.setOwnerDetailName("ownerDetail.getName()");
                		dataListCetakMerek.add(dataCetakMerek);
                	}
                }
            }
            
            // Data Gambar Logo Merek Tambahan
            List<TxTmBrandDetail> txTmBrandDetail = brandService.selectTxTmBrandDetailByTxTmBrand(txTmBrand.getId());
            if (txTmBrandDetail.size() == 0) {
            	dataCetakMerek = new CetakMerek(10, "DEBUG STRING Gambar Merek Tambahan", "Additional Mark");
            	try {
            		dataCetakMerek.setNoBrand(null);
            	} catch (NumberFormatException e) {
            	}
            	dataCetakMerek.setBrandLogo("");
            	dataListCetakMerek.add(dataCetakMerek);
            } else {
            	int no = 0;
            	for(TxTmBrandDetail brandDetail : txTmBrandDetail) {
            		no++;
                	String brandLogo = "brandDetail.getId() + \".jpg\"";
//                    String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                    String pathFolder = "txTmGeneral.getTxTmBrand().getCreatedDate()";
//                    String pathBrandLogo = brandLogo == null ? "" : uploadFileBrandDetailPath + pathFolder + brandLogo;
                    String pathBrandLogo = "uploadFileBrandDetailPath + pathFolder + brandLogo";
                	
                	dataCetakMerek = new CetakMerek(10, "DEBUG STRING Gambar Merek Tambahan", "Additional Mark");
                	dataCetakMerek.setNoBrand(no);
//                    dataCetakMerek.setBrandLogo(brandDetail.getId() == null ? "" : pathBrandLogo);
                    dataCetakMerek.setBrandLogo("brandDetail.getId()");
                	dataListCetakMerek.add(dataCetakMerek);
                }
            }

//            String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
            String pathFolder = "txTmGeneral.getTxTmBrand().getCreatedDate()";

            ClassLoader classLoader = getClass().getClassLoader();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("pUploadFilePath", uploadFilePathSignature);
            params.put("pUploadFileBrandPath", uploadFileBrandPath + pathFolder);
            params.put("pSignImage", "signimage.jpg");
            
            params.put("pUploadFileTandaTanganDigital", uploadFileImageTandaTangan + pathFolderTandaTanganDigital);
            params.put("pTtdImage", "tandaTanganDigital");
            
            List<Object> addListOwnerDetail = new ArrayList<>();
            for (CetakMerek datas : dataListCetakMerek) {
            	if(datas.getOwnerDetailName() != null) {
            		addListOwnerDetail.add(datas);
            		params.put("daftarPemohonTambahan", new JRBeanCollectionDataSource(addListOwnerDetail));
            	}
            }
            
            List<Object> addListBrandDetail = new ArrayList<>();
            for (CetakMerek datas : dataListCetakMerek) {
            	if(datas.getBrandLogo() != null) {
            		addListBrandDetail.add(datas);
            		params.put("daftarMerekTambahan", new JRBeanCollectionDataSource(addListBrandDetail));
            	}
            }
            if(txTmReprsList.size() > 0) {
            	for(TxTmRepresentative txTmReprs : txTmReprsList) {
                	params.put("pSignFullName", txTmReprs == null ? "" : "txTmReprs.getmRepresentative().getName()");
                }
            } else {
            	for(TxTmOwner txTmOwner : txTmOwnerList) {
            		params.put("pSignFullName", txTmOwner == null ? "" : "txTmOwner.getName()");
            	}
            }
            
            params.put("pSignPlaceDate", "Jakarta, " + DateUtil.formatDate(txTmGeneral.getTxReception().getApplicationDate(), "dd-MM-yyyy"));

            File file = new File(classLoader.getResource("report/CetakMerek.jasper").getFile());
            InputStream jasperStream1 = new FileInputStream(file);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(dataListCetakMerek));

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.exportReport();

            byte[] result;
            
            try (ByteArrayOutputStream msOut = new ByteArrayOutputStream()) {
                Document document = new Document();
                PdfCopy copy = new PdfCopy(document, msOut);
                document.open();

                ByteArrayInputStream bs = new ByteArrayInputStream(output.toByteArray());
                PdfReader jasperReader = new PdfReader(bs); //Reader for Jasper Report
                for (int page = 0; page < jasperReader.getNumberOfPages(); ) {
                    copy.addPage(copy.getImportedPage(jasperReader, ++page));
                }
                jasperReader.close();
                
                for (TxTmDoc txTmDoc : txtmDocList) {
                    if (txTmDoc.getFileName() != null) {
                    	if(txTmDoc.getFileName().toUpperCase().endsWith(".JPG") || txTmDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
                    		continue;
                    	}
                        String pathFolderDoc = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                        PdfReader nonJasperReader = new PdfReader(uploadFileDocApplicationPath + pathFolderDoc + txTmDoc.getFileNameTemp()); // Reader for the Pdf to be attached (not .jasper file)
                        for (int page = 0; page < nonJasperReader.getNumberOfPages(); ) {
                            copy.addPage(copy.getImportedPage(nonJasperReader, ++page));
                        }
                        nonJasperReader.close();
                    }
                }
                document.close();

                result = msOut.toByteArray();
            }
            
            String folderDoc = downloadFileDocPermohonanCetakMerekPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
            String filenameDoc = "CetakMerek-" + txTmGeneral.getApplicationNo() + ".pdf";
            
	        Path pathDir = Paths.get(folderDoc);
	        Path pathFile = Paths.get(folderDoc + filenameDoc);
	        if (!Files.exists(pathDir)) {
	            Files.createDirectories(pathDir);
	        }
	        if (Files.exists(pathFile)) {
	            Files.delete(pathFile);
	        }
	        if (!Files.exists(pathFile)) {
	            Files.createFile(pathFile);
	        }
	        OutputStream outputStream = new FileOutputStream(pathFile.toFile());
	        
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "attachment; filename=" + "CetakMerek-" + applicationNo + ".pdf");
            
            InputStream is = new ByteArrayInputStream(result);
            this.signPdf(is, outputStream);
            outputStream.close();
            
            try (OutputStream outputs = response.getOutputStream()) {
            	Path path = Paths.get(folderDoc + filenameDoc);
            	Files.copy(path, outputs);
            	outputs.flush();
            } catch(IOException e) {
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return "";
    }

    @RequestMapping(value = REQUEST_MAPPING_CETAK_MEREK_PRATINJAU, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMerekPratinjau(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        //CETAK MEREK AND SURAT PERNYATAAN SAVE ON PATH
        try {
            List<CetakMerek> dataListCetakMerek = new ArrayList<CetakMerek>();
            CetakMerek dataCetakMerek = null;

            String applicationNo = request.getParameter("no");
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);
            TxTmBrand txTmBrand  =  trademarkService.selectOneTxtmBrandBy("txTmGeneral.id",txTmGeneral.getId());

            dataCetakMerek = new CetakMerek(1, "Data Permohonan", "Application");
            dataCetakMerek.setApplicationNo(txTmGeneral.getApplicationNo());
            dataCetakMerek.seteFilingNo(txTmGeneral.getTxReception().geteFilingNo());
            dataCetakMerek.setFileSequence(txTmGeneral.getTxReception().getmFileSequence().getDesc());
            dataCetakMerek.setFileType(txTmGeneral.getTxReception().getmFileType().getDesc());
            dataCetakMerek.setFileTypeDetail(txTmGeneral.getTxReception().getmFileTypeDetail().getDesc());
            dataCetakMerek.setApplicationDate(txTmGeneral.getTxReception().getApplicationDate());
            dataCetakMerek.setFillingDate(txTmGeneral.getFilingDate());
            dataListCetakMerek.add(dataCetakMerek);

            dataCetakMerek = new CetakMerek(2, "Data Merek", "Description of Mark");
            if (txTmGeneral.getTxTmBrand() != null) {
                dataCetakMerek.setBrandName(txTmGeneral.getTxTmBrand().getName());
                dataCetakMerek.setBrandType(txTmGeneral.getTxTmBrand().getmBrandType().getName());
                dataCetakMerek.setBrandColor(txTmGeneral.getTxTmBrand().getColor());
                dataCetakMerek.setBrandDescription(txTmGeneral.getTxTmBrand().getDescription());
                dataCetakMerek.setBrandDescChar(txTmGeneral.getTxTmBrand().getDescChar());
                dataCetakMerek.setBrandTranslation(txTmGeneral.getTxTmBrand().getTranslation());
                dataCetakMerek.setBrandDisclaimer(txTmGeneral.getTxTmBrand().getDisclaimer());
                dataCetakMerek.setBrandFilename(txTmGeneral.getTxTmBrand().getId() + ".jpg");

            }
            dataListCetakMerek.add(dataCetakMerek);

            List<TxTmOwner> txTmOwnerList = permohonanService.selectAllOwnerByIdGenerals(txTmGeneral.getId(), true);
            if (txTmOwnerList.size() == 0) {
                dataCetakMerek = new CetakMerek(3, "Data Pemohon", "Applicant");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                for (TxTmOwner owners : txTmOwnerList) {
                    dataCetakMerek = new CetakMerek(3, "Data Pemohon", "Applicant");
                    dataCetakMerek.setOwnerName(owners.getName());
                    dataCetakMerek.setOwnerType(owners.getOwnerType());
                    dataCetakMerek.setOwnerNationality(owners.getNationality().getName());
                    dataCetakMerek.setOwnerAddress(owners.getAddress());
                    dataCetakMerek.setOwnerCity(owners.getmCity() == null ? null : owners.getmCity().getName());
                    dataCetakMerek.setOwnerProvince(owners.getmProvince() == null ? null : owners.getmProvince().getName());
                    dataCetakMerek.setOwnerCountry(owners.getmCountry() == null ? null : owners.getmCountry().getName());
                    dataCetakMerek.setOwnerZipCode(owners.getZipCode());
                    dataCetakMerek.setOwnerPhone(owners.getPhone());
                    dataCetakMerek.setOwnerEmail(owners.getEmail());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            if (txTmOwnerList.size() == 0) {
                dataCetakMerek = new CetakMerek(4, "Alamat Surat Menyurat", "Mailing Address");
                dataListCetakMerek.add(dataCetakMerek);
            }
            else {
                for (TxTmOwner owners : txTmOwnerList) {
                    dataCetakMerek = new CetakMerek(4, "Alamat Surat Menyurat", "Mailing Address");
                    dataCetakMerek.setOwnerPostAddress(owners.getPostAddress());
                    dataCetakMerek.setOwnerPostCity(owners.getPostCity() == null ? null : owners.getPostCity().getName());
                    dataCetakMerek.setOwnerPostProvince(owners.getPostProvince() == null ? null : owners.getPostProvince().getName());
                    dataCetakMerek.setOwnerPostCountry(owners.getPostCountry() == null ? null : owners.getPostCountry().getName());
                    dataCetakMerek.setOwnerPostZipCode(owners.getPostZipCode());
                    dataCetakMerek.setOwnerPostPhone(owners.getPostPhone());
                    dataCetakMerek.setOwnerPostEmail(owners.getPostEmail());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            List<TxTmRepresentative> txTmReprsList = permohonanService.selectTxTmReprsByGeneralId(txTmGeneral.getId(), true);
            if (txTmReprsList.size() == 0) {
                dataCetakMerek = new CetakMerek(5, "Data Kuasa", "Representative/IP Consultant");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                for(TxTmRepresentative txTmReprs : txTmReprsList) {
                    dataCetakMerek = new CetakMerek(5, "Data Kuasa", "Representative/IP Consultant");
                    dataCetakMerek.setReprsNo(txTmReprs.getmRepresentative().getNo());
                    dataCetakMerek.setReprsName(txTmReprs.getmRepresentative().getName());
                    dataCetakMerek.setReprsName(txTmReprs.getmRepresentative().getName());
                    dataCetakMerek.setReprsOffice(txTmReprs.getmRepresentative().getOffice());

//            		dataCetakMerek.setReprsAddress(txTmReprs.getmRepresentative().getAddress());
//            		dataCetakMerek.setReprsPhone(txTmReprs.getmRepresentative().getPhone());
//                    dataCetakMerek.setReprsEmail(txTmReprs.getmRepresentative().getEmail());
                    /**---GET FIELD ADDRESS, PHONE, EMAIL FROM TX_TM_REPRS **/
                    dataCetakMerek.setReprsAddress( txTmReprs.getAddress() );
                    dataCetakMerek.setReprsPhone( txTmReprs.getPhone() );
                    dataCetakMerek.setReprsEmail( txTmReprs.getEmail() );
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            List<TxTmPrior> txTmPriorList = permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId());
            if (txTmPriorList.size() == 0) {
                dataCetakMerek = new CetakMerek(6, "Data Prioritas", "Priority Data");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                int count = 1;
                for (TxTmPrior txTmPrior : txTmPriorList) {
                    dataCetakMerek = new CetakMerek(6, "Data Prioritas", "Priority Data");
                    dataCetakMerek.setPriorOrder("" + count++);
                    dataCetakMerek.setPriorNo(txTmPrior.getNo());
                    dataCetakMerek.setPriorCountry(txTmPrior.getmCountry().getName());
                    dataCetakMerek.setPriorDate(txTmPrior.getPriorDate());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            if (txTmGeneral.getTxTmClassList().size() == 0) {
                dataCetakMerek = new CetakMerek(7, "Data Kelas", "Class");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                Map<Integer, String[]> mapClass = new HashMap<>();
                List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
                for (TxTmClass result : txTmClassList) {
                    int key = result.getmClass().getNo();
                    String desc = result.getmClassDetail().getDesc();
                    String descEn = result.getmClassDetail().getDescEn();

                    if (mapClass.containsKey(key)) {
                        desc = mapClass.get(key)[0] + "; " + desc;
                        descEn = mapClass.get(key)[1] + "; " + descEn;
                    }

                    mapClass.put(key, new String[]{desc, descEn});
                }
                for (Map.Entry<Integer, String[]> entry : mapClass.entrySet()) {
                    String desc = entry.getValue()[0].replaceAll(" ", "").replaceAll(";", "");
                    String descEn = entry.getValue()[1].replaceAll(" ", "").replaceAll(";", "");

                    dataCetakMerek = new CetakMerek(7, "Data Kelas", "Class");
                    dataCetakMerek.setClassNo("" + entry.getKey());
                    dataCetakMerek.setClassDesc(desc.length() == 0 ? " - " : entry.getValue()[0]);
                    dataCetakMerek.setClassDescEn(descEn.length() == 0 ? " - " : entry.getValue()[1]);
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            String tandaTanganDigital = "";
            String pathFolderTandaTanganDigital = "";

            String docIdTTDPTemp = "";
            String docIdTTDKTemp = "";
            String txTmDocDateTTDK = "";
            String txTmDocDateTTDP = "";
            List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
            if (txtmDocList.size() == 0) {
                dataCetakMerek = new CetakMerek(8, "Dokumen Lampiran", "Attachment");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                Collections.sort(txtmDocList, (o1, o2) -> o1.getmDocType().getName().compareTo(o2.getmDocType().getName()));
                for (TxTmDoc txTmDoc : txtmDocList) {
                    if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK")) {
                        docIdTTDKTemp = txTmDoc.getFileNameTemp();
                        txTmDocDateTTDK = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                    } else if (txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP")) {
                        docIdTTDPTemp = txTmDoc.getFileNameTemp();
                        txTmDocDateTTDP = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                    }

                    dataCetakMerek = new CetakMerek(8, "Dokumen Lampiran", "Attachment");
                    dataCetakMerek.setDocName(txTmDoc.getmDocType().getName());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }
            if(txTmReprsList.size() > 0 && !docIdTTDKTemp.isEmpty()) {
                tandaTanganDigital = docIdTTDKTemp;
                pathFolderTandaTanganDigital = txTmDocDateTTDK;
            } else {
                tandaTanganDigital = docIdTTDPTemp;
                pathFolderTandaTanganDigital = txTmDocDateTTDP;
            }

            // Data Pemohon Tambahan
            List<TxTmOwnerDetail> txOwnerDetail = null;
            for (TxTmOwner owners : txTmOwnerList) {
                txOwnerDetail = permohonanService.selectTxTmOwnerDetailByTxTmOwner(owners.getId(), true);
                if (txOwnerDetail.size() == 0) {
                    dataCetakMerek = new CetakMerek(9, "Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                    try {
                        dataCetakMerek.setNo(null);
                    } catch (NumberFormatException e) {
                    }
                    dataCetakMerek.setOwnerDetailName("");
                    dataListCetakMerek.add(dataCetakMerek);
                } else {
                    int no = 0;
                    for (TxTmOwnerDetail ownerDetail : txOwnerDetail) {
                        no++;
                        dataCetakMerek = new CetakMerek(9, "Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                        dataCetakMerek.setNo(no);
                        dataCetakMerek.setOwnerDetailName(ownerDetail.getName());
                        dataListCetakMerek.add(dataCetakMerek);
                    }
                }
            }

            // Data Gambar Logo Merek Tambahan
            List<TxTmBrandDetail> txTmBrandDetail = brandService.selectTxTmBrandDetailByTxTmBrand(txTmBrand.getId());
            if (txTmBrandDetail.size() == 0) {
                dataCetakMerek = new CetakMerek(10, "Gambar Merek Tambahan", "Additional Mark");
                try {
                    dataCetakMerek.setNoBrand(null);
                } catch (NumberFormatException e) {
                }
                dataCetakMerek.setBrandLogo("");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                int no = 0;
                for(TxTmBrandDetail brandDetail : txTmBrandDetail) {
                    no++;
                    String brandLogo = brandDetail.getId() + ".jpg";
                    String pathFolder = DateUtil.formatDate(brandDetail.getUploadDate(), "yyyy/MM/dd/");
                    String pathBrandLogo = brandLogo == null ? "" : uploadFileBrandDetailPath + pathFolder + brandLogo;
                    dataCetakMerek = new CetakMerek(10, "Gambar Merek Tambahan", "Additional Mark");
                    dataCetakMerek.setNoBrand(no);
                    dataCetakMerek.setBrandLogo(brandDetail.getId() == null ? "" : pathBrandLogo);
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("pUploadFilePath", uploadFilePathSignature);
            params.put("pUploadFileBrandPath", uploadFileBrandPath + pathFolder);
            params.put("pSignImage", "signimage.jpg");

            params.put("pUploadFileTandaTanganDigital", uploadFileImageTandaTangan + pathFolderTandaTanganDigital);
            params.put("pTtdImage", tandaTanganDigital);

            List<Object> addListOwnerDetail = new ArrayList<>();
            for (CetakMerek datas : dataListCetakMerek) {
                if(datas.getOwnerDetailName() != null) {
                    addListOwnerDetail.add(datas);
                    params.put("daftarPemohonTambahan", new JRBeanCollectionDataSource(addListOwnerDetail));
                }
            }

            List<Object> addListBrandDetail = new ArrayList<>();
            for (CetakMerek datas : dataListCetakMerek) {
                if(datas.getBrandLogo() != null) {
                    addListBrandDetail.add(datas);
                    params.put("daftarMerekTambahan", new JRBeanCollectionDataSource(addListBrandDetail));
                }
            }
            if(txTmReprsList.size() > 0) {
                for(TxTmRepresentative txTmReprs : txTmReprsList) {
                    params.put("pSignFullName", txTmReprs == null ? "" : txTmReprs.getmRepresentative().getName());
                }
            } else {
                for(TxTmOwner txTmOwner : txTmOwnerList) {
                    params.put("pSignFullName", txTmOwner == null ? "" : txTmOwner.getName());
                }
            }

            params.put("pSignPlaceDate", "Jakarta, " + DateUtil.formatDate(txTmGeneral.getTxReception().getApplicationDate(), "dd-MM-yyyy"));

            /*Resource resource = resourceLoader.getResource("classpath:report/CetakMerek.jasper");
            File file = resource.getFile();
            InputStream jasperStream1 = new FileInputStream(file);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(dataListCetakMerek));*/

            final InputStream reportInputStream = getClass().getResourceAsStream("/report/CetakMerek.jrxml");
            final JasperDesign jasperDesign = JRXmlLoader.load(reportInputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(dataListCetakMerek));

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.exportReport();

            byte[] result;

            try (ByteArrayOutputStream msOut = new ByteArrayOutputStream()) {
                Document document = new Document();
                PdfCopy copy = new PdfCopy(document, msOut);
                document.open();

                ByteArrayInputStream bs = new ByteArrayInputStream(output.toByteArray());
                PdfReader jasperReader = new PdfReader(bs); //Reader for Jasper Report
                for (int page = 0; page < jasperReader.getNumberOfPages(); ) {
                    copy.addPage(copy.getImportedPage(jasperReader, ++page));
                }
                jasperReader.close();

                for (TxTmDoc txTmDoc : txtmDocList) {
                    if (txTmDoc.getFileName() != null) {
                        if(txTmDoc.getFileName().toUpperCase().endsWith(".JPG") || txTmDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
                            continue;
                        }
                        String pathFolderDoc = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                        PdfReader nonJasperReader = new PdfReader(uploadFileDocApplicationPath + pathFolderDoc + txTmDoc.getFileNameTemp()); // Reader for the Pdf to be attached (not .jasper file)
                        for (int page = 0; page < nonJasperReader.getNumberOfPages(); ) {
                            copy.addPage(copy.getImportedPage(nonJasperReader, ++page));
                        }
                        nonJasperReader.close();
                    }
                }
                document.close();

                result = msOut.toByteArray();
            }

            String folderDoc = downloadFileDocPermohonanCetakMerekPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
            String filenameDoc = "CetakMerek-" + txTmGeneral.getApplicationNo() + ".pdf";

            Path pathDir = Paths.get(folderDoc);
            Path pathFile = Paths.get(folderDoc + filenameDoc);
            if (!Files.exists(pathDir)) {
                Files.createDirectories(pathDir);
            }
            if (Files.exists(pathFile)) {
                Files.delete(pathFile);
            }
            if (!Files.exists(pathFile)) {
                Files.createFile(pathFile);
            }
            OutputStream outputStream = new FileOutputStream(pathFile.toFile());

            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "attachment; filename=" + "CetakMerek-" + applicationNo + ".pdf");

            InputStream is = new ByteArrayInputStream(result);
            this.signPdf(is, outputStream);
            outputStream.close();

            try (OutputStream outputs = response.getOutputStream()) {
                Path path = Paths.get(folderDoc + filenameDoc);
                Files.copy(path, outputs);
                outputs.flush();
            } catch(IOException e) {
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return "";
    }

    @RequestMapping(value = REQUEST_MAPPING_CETAK_MADRID_PRATINJAU, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMadrid(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String applicationNo = request.getParameter("no");
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);

        List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
        int f26 = 0;
        String f27 = "";
        for(TxTmDoc doc: txtmDocList){
            if(doc.getmDocType().getId().equalsIgnoreCase("F26")){
                f26 = f26+1;
            }
            if(doc.getmDocType().getId().equalsIgnoreCase("F27")){
                f27 = "F27";
            }
        }

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("SUBREPORT_DIR", "report/madrid/");
            parameters.put("applicationId", txTmGeneral.getId());
            parameters.put("approve", txTmGeneral.getmStatus().getId().equals(StatusEnum.IPT_PENGAJUAN_MADRID.toString())?"1":"0");

            String brandLogo = txTmGeneral.getTxTmBrand() !=null ?  txTmGeneral.getTxTmBrand().getId()+ ".jpg":null;
            String pathFolderLogo = txTmGeneral.getTxTmBrand() !=null ? DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/"):null;
            String pathBrandLogo = brandLogo == null ? "" : uploadFileBrandPath + pathFolderLogo + brandLogo;

            if (!pathBrandLogo.equals("")) {
            	if(Files.exists(Paths.get(pathBrandLogo)))  {
                    parameters.put("brandLogo", pathBrandLogo);
                }
            }

            parameters.put("draftLogo", pathImage+"nodraft.png");
            parameters.put("lookup", lookupService.selectAllbyGroup("FormMadridMM2").get(0).getName());
            parameters.put("mm17", String.valueOf(f26));
            parameters.put("mm18", f27);

            parameters.put("priorList", txTmGeneral.getTxTmPriorList().size());

            List<TxTmClassLimitation> limitations = txTmClassLimitationRepository.findByTxTmGeneral(txTmGeneral,new Sort(Sort.Direction.ASC, "id"));
            int listContract = 0;
            int contractContinues = 0;
            long descLim = 0;
            for(int i=1;i<=limitations.size();i++){
                TxTmClassLimitation tmClassLimitation = limitations.get(i-1);
                if(tmClassLimitation.getmClassDetail().getDescEn()!=null){
                    descLim = descLim + tmClassLimitation.getmClassDetail().getDescEn().length();
                }else{
                    descLim = descLim + tmClassLimitation.getmClassDetail().getDesc().length();
                }
                if(descLim>100){
                    listContract = i;
                    contractContinues = 1;
                    break;
                }
            }
            parameters.put("contractContinues", contractContinues);
            parameters.put("listContract", listContract);

            List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
            Map<Integer, String> listTxTmClass = new HashMap<>();
            if (txTmClassList != null) {
                for (TxTmClass result : txTmClassList) {
                    int key = result.getmClass().getNo();
                    String descEn = result.getmClassDetail().getDescEn();

                    if (listTxTmClass.containsKey(key)) {
                        descEn = listTxTmClass.get(key) + "; " + descEn;
                    }

                    listTxTmClass.put(key, descEn);
                }
            }

            int listClass = 0;
            int classContinues = 0;
            long desc = 0;
            for(Map.Entry<Integer, String> entry : listTxTmClass.entrySet()){
                desc = desc+entry.getValue().length();
                if(desc>800 || listClass>2){ // only show 3 class preview, others at continuation
                    classContinues = 1;
                    break;
                }else{
                    listClass = listClass+1;
                }
            }
            parameters.put("classContinues", classContinues);
            parameters.put("listClass", listClass);

            int regCount =  0;
            List<TxTmReference> txTmReferenceList = txTmReferenceRepository.getByTxTmGeneralId(txTmGeneral.getId());
            regCount = txTmReferenceList.size();
            parameters.put("regCount", regCount);

            // owner detail list count
            List<TxTmOwner> txTmOwnerList = permohonanService.selectAllOwnerByIdGenerals(txTmGeneral.getId(), true);
            for(TxTmOwner owner : txTmOwnerList) {
                List<TxTmOwnerDetail> txTmOwnerDetailList = owner.getTxTmOwnerDetails();
                if(txTmOwnerDetailList.size() > 0 ) {
                    parameters.put("ownerCount", txTmOwnerDetailList.size());
                    break;
                }
            }

            int madridCountrys = countryService.findByMadrid(Boolean.TRUE).size();
            //BigDecimal listMadridOneReport = BigDecimal.valueOf(madridCountrys).divide(BigDecimal.valueOf(4), BigDecimal.ROUND_UP);
            //if(listMadridOneReport.intValue()>4){
                //give static value for custom column
                int total = madridCountrys;
                int curr = -1;
                int[] customColumn = {28,25,25}; //cuma define 3 kolum pertama, sisanya ke kolom 4
                for(int a=0; a<3; a++){
                    parameters.put("start"+(a+1),curr+=1);
                    parameters.put("end"+(a+1),(curr+=customColumn[a]));
                }
                parameters.put("start4",curr+=1);
                parameters.put("end4",(curr+=(total-curr)));

                /*for(int a=0; a<4; a++){
                    parameters.put("start"+(a+1),(a*listMadridOneReport.intValue())+1);
                    parameters.put("end"+(a+1),(a+1)*listMadridOneReport.intValue());
                }*/
            /*}else{
                parameters.put("start1",1);
                parameters.put("start2",2);
                parameters.put("start3",3);
                parameters.put("start4",4);
                parameters.put("end1",1);
                parameters.put("end2",2);
                parameters.put("end3",3);
                parameters.put("end4",4);
            }
            /*parameters.put("start1",1);
            parameters.put("start2",28+1);
            parameters.put("start3",28+1+25+1);
            parameters.put("start4",28+1+25+1+24+1);
            parameters.put("end1",28);
            parameters.put("end2",28+1+25);
            parameters.put("end3",28+1+25+1+24);
            parameters.put("end4",28+1+25+1+24+1+30);*/

            for(TxTmDoc txTmDoc : txtmDocList){
                if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTD")){
                    String pathTtdp = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                    String fileTtdp = txTmDoc.getFileNameTemp();
                    parameters.put("ttdp",uploadFileImageTandaTangan+pathTtdp+fileTtdp);
                    break;
                }
            }

            if(txTmGeneral.getApprovedBy()!=null){
                MUser mUser = txTmGeneral.getApprovedBy();
                parameters.put("ttEmployee",uploadFilePathSignature+mUser.getId()+"."+mUser.getUsername()+".jpg");
                parameters.put("logoKI", uploadFilePathSignature+"signimage.jpg");
            }

            if(txTmGeneral.getTxTmMadridFee() !=null && txTmGeneral.getTxTmMadridFee().getTxTmMadridFeeDetails()!=null){
                List<TxTmMadridFeeDetail> feeDetails = txTmGeneral.getTxTmMadridFee().getTxTmMadridFeeDetails();
                if(feeDetails.size()>20){
                    parameters.put("feeContinues", 1);
                    parameters.put("startFees", 1);
                    parameters.put("endFees", 10);
                    parameters.put("startFees2", 11);
                    parameters.put("endFees2", 20);
                }else{
                    parameters.put("feeContinues", 0);
                    BigDecimal feeOneReport = BigDecimal.valueOf(feeDetails.size()).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_UP);
                    parameters.put("startFees", 1);
                    parameters.put("endFees", feeOneReport.intValue());
                    parameters.put("startFees2", feeOneReport.intValue()+1);
                    parameters.put("endFees2", feeDetails.size());
                }
            }else{
                parameters.put("feeContinues", 0);
                parameters.put("startFees", 1);
                parameters.put("endFees", 1);
                parameters.put("startFees2", 1);
                parameters.put("endFees2", 1);
            }

            final InputStream reportInputStream = getClass().getResourceAsStream("/report/madrid/CetakMadridOO.jrxml");
            final JasperDesign jasperDesign = JRXmlLoader.load(reportInputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, madridOoService.getConnection());

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.exportReport();

            byte[] result = new byte[0];

            try (ByteArrayOutputStream msOut = new ByteArrayOutputStream()) {
                Document document = new Document();
                PdfCopy copy = new PdfCopy(document, msOut);
                document.open();

                ByteArrayInputStream bs = new ByteArrayInputStream(output.toByteArray());
                PdfReader jasperReader = new PdfReader(bs); //Reader for Jasper Report
                for (int page = 0; page < jasperReader.getNumberOfPages(); ) {
                    copy.addPage(copy.getImportedPage(jasperReader, ++page));
                }
                jasperReader.close();

                /*for (TxTmDoc txTmDoc : txtmDocList) {
                    if (txTmDoc.getFileName() != null) {
                        if(txTmDoc.getFileName().toUpperCase().endsWith(".JPG") || txTmDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
                            continue;
                        }
                        String pathFolderDoc = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                        PdfReader nonJasperReader = new PdfReader(uploadFileDocApplicationPath + pathFolderDoc + txTmDoc.getFileNameTemp()); // Reader for the Pdf to be attached (not .jasper file)
                        for (int page = 0; page < nonJasperReader.getNumberOfPages(); ) {
                            copy.addPage(copy.getImportedPage(nonJasperReader, ++page));
                        }
                        nonJasperReader.close();
                    }
                }*/
                document.close();

                result = msOut.toByteArray();
            } catch (BadPdfFormatException e) {
                System.err.println("ERROR!!!!!!!!!!!!!!!!!!"+ e.toString());
            } catch (DocumentException e) {
                System.err.println("ERROR!!!!!!!!!!!!!!!!!!"+ e.toString());
            }
            String folderDoc = downloadFileDocMadridOOCetakPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
            String filenameDoc = "CetakMadrid-" + txTmGeneral.getId() + ".pdf";

            Path pathDir = Paths.get(folderDoc);
            Path pathFile = Paths.get(folderDoc + filenameDoc);
            if (!Files.exists(pathDir)) {
                Files.createDirectories(pathDir);
            }
            if (!Files.exists(pathFile)) {
                Files.createFile(pathFile);
            }
            OutputStream outputStream = new FileOutputStream(pathFile.toFile());

            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "attachment; filename=CetakMadrid.pdf");
            InputStream is = new ByteArrayInputStream(result);
            this.signPdf(is, outputStream);
            outputStream.close();

            try (OutputStream outputF = response.getOutputStream()) {
                Path path = pathFile;
                Files.copy(path, outputF);
                outputF.flush();
            } catch(IOException e) {
                System.err.println("ERROR!!!!!!!!!!!!!!!!!!"+ e.toString());
            }

        }catch (JRException e) {
            System.err.println("ERROR!!!!!!!!!!!!!!!!!!"+ e.toString());
        }
        return "";
    }

    @RequestMapping(value = REQUEST_MAPPING_CETAK_SURAT_PERNYATAAN_PRATINJAU, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakSuratPernyataanP(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    	try {
            List<CetakSuratPernyataan> listData = new ArrayList<CetakSuratPernyataan>();
            CetakSuratPernyataan data = new CetakSuratPernyataan();
            Timestamp createdDate = new Timestamp(System.currentTimeMillis());
            SimpleDateFormat fmtDate= new SimpleDateFormat("dd MMMM yyyy", new Locale("ID"));
            
            String applicationNo = request.getParameter("no");
            TxTmGeneral txTmGeneral = trademarkService.selectOneBy("applicationNo", applicationNo);
            List<TxTmOwner> txTmOwnerList = permohonanService.selectAllOwnerByIdGenerals(txTmGeneral.getId(), true);
            
            String brandLogo = "";
            String pathFolder = "";
            String pathBrandLogo = "";
    	 	try {
	            brandLogo = txTmGeneral.getTxTmBrand().getId() + ".jpg";
	        	pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
	        	pathBrandLogo = brandLogo == null ? "" : uploadFileBrandPath + pathFolder + brandLogo;
	        	
	        	data.setBrandName(txTmGeneral.getTxTmBrand() == null ? "" : txTmGeneral.getTxTmBrand().getName());
	         	data.setBrandLogo(txTmGeneral.getTxTmBrand().getId() == null ? "" : pathBrandLogo);
    	 	}catch (NullPointerException e) {
            	
            }
    	 	data.setCreatedDate(fmtDate.format(createdDate).toString());
        	
        	for (TxTmOwner owners : txTmOwnerList) {
        		data.setOwnerName(owners.getName());
        		data.setOwnerAddress(owners.getAddress());
        		data.setOwnerOrReprsName(owners.getName());
        	}
        	
        	String pathFolderTandaTanganDigital = "";
        	String tandaTanganDigitalSuratPernyataan = "";
        	
            List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
            if (txtmDocList.size() > 0) {
                for (TxTmDoc txTmDoc : txtmDocList) {
                	if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP")) {
                		pathFolderTandaTanganDigital = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                		tandaTanganDigitalSuratPernyataan = txTmDoc.getFileNameTemp();
                	}
                }
            }
	        listData.add(data);

            Map<String, Object> paramsSrtPrnyataan = new HashMap<String, Object>();
            paramsSrtPrnyataan.put("pUploadFileTandaTanganDigital", uploadFileImageTandaTangan + pathFolderTandaTanganDigital);
            paramsSrtPrnyataan.put("pTtdImage", tandaTanganDigitalSuratPernyataan);

            Resource resource = resourceLoader.getResource("classpath:report/CetakSuratPernyataan.jasper");
            File file = resource.getFile();
            InputStream jasperStream = new FileInputStream(file);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, paramsSrtPrnyataan, new JRBeanCollectionDataSource(listData));
            
            String folderDocSuratPernyataan = downloadFileDocPermohonanCetakMerekPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
            String filenameDocSuratPernyataan = "CetakSuratPernyataan-" + txTmGeneral.getApplicationNo() + ".pdf";
            
            Path pathDir = Paths.get(folderDocSuratPernyataan);
	        Path pathFile = Paths.get(folderDocSuratPernyataan + filenameDocSuratPernyataan);
	        if (!Files.exists(pathDir)) {
	            Files.createDirectories(pathDir);
	        }
	        if (Files.exists(pathFile)) {
	            Files.delete(pathFile);
	        }
	        if (!Files.exists(pathFile)) {
	            Files.createFile(pathFile);
	        }
	        
	        OutputStream outputStreamSuratPernyataan = new FileOutputStream(pathFile.toFile());
	        response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "attachment; filename=" + "CetakSuratPernyataan-" + applicationNo + ".pdf");
	        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStreamSuratPernyataan);
	        outputStreamSuratPernyataan.close();

            
	        try (OutputStream output = response.getOutputStream()) {
	        	Path path = Paths.get(folderDocSuratPernyataan + filenameDocSuratPernyataan);
	        	Files.copy(path, output);
	        	output.flush();
	        } catch(IOException e) {
	        }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return "";
    }
    
    private void signPdf(InputStream input, OutputStream output) {
        String key = CERTIFICATE_FILE + "eFiling.p12";
        //System.out.println("PATH : " + key);
        try {
            PDFSignatureFacade facade = new PDFSignatureFacade();
            facade.sign(key, "JakartaPP123!@#", input, output, true, new java.awt.Rectangle(250, 0, 400, 50));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    } 
        
}
