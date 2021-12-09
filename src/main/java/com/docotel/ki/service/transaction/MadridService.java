package com.docotel.ki.service.transaction;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.sql.DataSource;

import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.model.wipo.ImageFileList;
import com.docotel.ki.model.wipo.PDFFile;
import com.docotel.ki.model.wipo.XMLFile;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.custom.master.*;
import com.docotel.ki.repository.custom.transaction.*;
import com.docotel.ki.repository.master.*;
import com.docotel.ki.repository.transaction.*;
import com.docotel.ki.service.WipoService;
import com.docotel.ki.service.master.BrandService;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.FileSeqService;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.util.DateUtil;

import com.docotel.ki.util.NumberUtil;
import org.jdom2.*;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.enumeration.LanguageTranslatorEnum;
import com.docotel.ki.enumeration.StatusEnum;
//import MadridListObject;

import org.springframework.web.util.UriUtils;

@Service
public class MadridService {
    @Autowired
    private TxMadridCustomRepository txMadridCustomRepository;
    @Autowired
    private TxMadridRepository txMadridRepository;
    @Autowired
    private TxReceptionRepository txReceptionRepository;
    @Autowired
    private TxReceptionCustomRepository txReceptionCustomRepository;
    @Autowired
    private MWorkflowProcessCustomRepository mWorkflowProcessCustomRepository;
    @Autowired
    private TxMonitorRepository txMonitorRepository;
    @Autowired
    private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
    private MLawRepository mLawRepository;
    @Autowired
    private MWorkflowCustomRepository mWorkflowCustomRepository;
    @Autowired
    private MWorkflowRepository mWorkflowRepository;
    @Autowired
    private TxTmOwnerCustomRepository txTmOwnerCustomRepository;
    @Autowired
    private TxTmOwnerRepository txTmOwnerRepository;
    @Autowired
    private TxTmOwnerDetailRepository txTmOwnerDetailRepository;
    @Autowired
    private MCountryCostumRepository mCountryCostumRepository;
    @Autowired
    private TxTmOwnerDetailCustomRepository txTmOwnerDetailCustomRepository;
    @Autowired
    private TxTmReprsCustomRepository txTmReprsCustomRepository;
    @Autowired
    private MrepresentativeCustomRepository mrepresentativeCustomRepository;
    @Autowired
    private MRepresentativeRepository mRepresentativeRepository;
    @Autowired
    private MBrandTypeCustomRepository mBrandTypeCustomRepository;
    @Autowired
    MClassHeaderRepository mClassHeaderRepository;
    @Autowired
    private MClassDetailCustomRepository mClassDetailCustomRepository;
    @Autowired
    private MClassDetailRepository mClassDetailRepository;
    @Autowired
    private TxTmBrandCustomRepository txTmBrandCustomRepository;
    @Autowired
    private TxTmBrandRepository txTmBrandRepository;
    @Autowired
    private TxTmPriorCustomRepository txTmPriorCustomRepository;
    @Autowired
    private TxTmClassCustomRepository txTmClassCustomRepository;
    @Autowired
    private TxTmClassRepository txTmClassRepository;
    @Autowired
    private TxTmDocCustomRepository txTmDocCustomRepository;
    @Autowired
    private MUserCustomRepository mUserCustomRepository;
    @Autowired
    private MDocTypeCustomRepository mDocTypeCustomRepository;
    @Autowired
    private PermohonanOnlineService permohonanOnlineService;
    @Autowired
    private ImageFileListService imageFileListService;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private WipoService wipoService;
    @Autowired
    private MCountryRepository mCountryRepository;
    @Autowired
    private MrepresentativeCustomRepository mRepresentativeCustomRepository;
    @Autowired
    private TxRegistrationRepository txRegistrationRepository;
    @Autowired
    private TxRegistrationDetailRepository txRegistrationDetailRepository;
    @Autowired
    private TxTmReferenceCustomRepository txTmReferenceCustomRepository;
    @Autowired
    BrandService brandService;
    @Autowired
    UserService userService;
    @Autowired
    FileSeqService fileSeqService;
    @Autowired
    FileService fileService;
    @Autowired
    private ClassService classService;

    @Value("${spring.datasource.driverClassName:}")
    private String driverClassName;
    @Value("${wipo.madrid.extract.location.xml}")
    private String wipoMadridExtractLocationXml;
    @Value("${wipo.madrid.extract.location.pdf}")
    private String wipoMadridExtractLocationPdf;
    @Value("${upload.file.doc.application.path}")
    private String uploadFileDocApplicationPath;
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;

    HashMap<String, TxMadrid> hListMadrid = null;
    HashMap<TxReception, TxMadrid> hListProcess = null;
    HashMap<String, MCountry> hListCountry = null;
    HashMap<Integer, MClass> hListClass = null;
    HashMap<String, TxReception> hListReceptionMadrid = null;
    HashMap<String, TxTmGeneral> hListGeneralMadrid = null;
    HashMap<String, TxTmClass> hListTxClassMadrid = null;
    HashMap<String, TxTmBrand> hListTxTmBrandMadrid = null;
    HashMap<String, TxTmOwner> hListTxTmOwnerMadrid = null;


    HashMap<String, MRepresentative> hListMReprsMadrid = null;
    HashMap<String, TxTmRepresentative> hListTxTmReprsMadrid = null;
    HashMap<TxReception, HashMap<TxMadrid, MadridDetailInfo>> hListObject = null;

    private DataSource dataSource;

    /***************************** - AUTO INJECT SECTION - ****************************/


    public GenericSearchWrapper<TxMadrid> listMadrid(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxMadrid> searchResult = new GenericSearchWrapper<>();
        searchResult.setCount(txMadridCustomRepository.countMadrid(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txMadridCustomRepository.selectAllMadrid(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    @Transactional
    public void insertOrModify(TxMadrid txMadrid) {
        txMadridCustomRepository.saveOrUpdate(txMadrid);
    }

    public TxMadrid selectOne(String by, String value) {
        return txMadridCustomRepository.selectOne("", by, value, true);
    }

    @Transactional
    public void insertTxTmDocWhenCopy(File data, TxTmDoc txTmDoc) {
	 	/*
		insert ke table tx_tm_doc || txtmdoc
		x*TM_DOC_ID = UUID ||  generate uuid
		x*CREATED_DATE = LAST_MODIFIED  ||sdf.format(data.lastModified());
		x*TM_DOC_DESC = NULL
		x*TM_DOC_FILENAME = 435999-EXN-1189871101.PDF ||data.getName()
		x*TM_DOC_FILENAME_TEMP = 435999-EXN-1189871101.PDF ||data.getName()
		x*TM_DOC_FILE_SIZE = FILE_SIZE || data. size file
		x*TM_DOC_STATUS = 1
		x*TM_DOC_UPLOAD_DATE = LAST_MODIFIED ||sdf.format(data.lastModified());
		x*CREATED_BY ='SYSTEM'
		x*DOC_TYPE_ID = 391 || referens dari M_doc_type
		x*APPLICATION_ID = 435999 (IRN) DARI APPLICATION_NO.TX_TM_GENERAL--> AMBIL APPLICATION_ID NYA

		*XML_FILE_ID = FILE_ID.TX_MADRID_PDF ||pdf list > pdf file .id

		 */

        Timestamp txTmDocCreatedDate = new Timestamp(data.lastModified());
        long timestamp = txTmDocCreatedDate.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String year = cal.get(Calendar.YEAR) + "";
        TxTmGeneral txTmGeneral = null;
        String[] parts = data.getName().split("-");
        if (parts.length == 3) {
            txTmGeneral = txTmGeneralCustomRepository.selectOneLike("applicationNo", "M00" + year + parts[0]);
        }
        if (parts.length == 2) {
            String[] subParts = parts[1].split("\\.");
            TxMadrid txMadrid = txMadridCustomRepository.selectOne("id", subParts[0]);

            if (txMadrid != null) {
                txTmGeneral = txTmGeneralCustomRepository.selectOneLike("applicationNo", "M00" + year + txMadrid.getIntregn());
//				txTmGeneral = txTmGeneralCustomRepository.selectOneByTxMadridDocId ("id", parts[1]);
            }
        }

        if (txTmGeneral != null) {
            MUser txTmDocCreatedBy = mUserCustomRepository.selectOne("username", "system");
            MDocType txTmDocMDocType = mDocTypeCustomRepository.selectOne("id", "391");

            TxTmDoc txTmDocHolder = new TxTmDoc(); //uuid generated
            //Not Null Value
            txTmDocHolder.setCreatedDate(txTmDocCreatedDate);
            txTmDocHolder.setStatus(true);
            txTmDocHolder.setCreatedBy(txTmDocCreatedBy);
            txTmDocHolder.setmDocType(txTmDocMDocType);
            txTmDocHolder.setTxTmGeneral(txTmGeneral);

            //Other Value
            double sizeInKb = (double) data.length() / 1024;
            long sizeInByte = data.length();
            String sizeInKbString = sizeInByte + "";

            txTmDocHolder.setDescription(null);
            txTmDocHolder.setFileName(data.getName());
            txTmDocHolder.setFileNameTemp(data.getName());
            txTmDocHolder.setFileSize(sizeInKbString);
//			txTmDocHolder.setUploadDate(txTmDocCreatedDate);
            txTmDocHolder.setXmlFileId(txTmGeneral.getXmlFileId());

            txTmDocCustomRepository.saveOrUpdate(txTmDocHolder);
        }


    }

    //@Transactional( propagation = Propagation.SUPPORTS,readOnly = true ) -> error  No EntityManager with actual transaction available for current thread - cannot reliably process 'merge' call; nested exception is javax.persistence.TransactionRequiredException: No EntityManager with actual transaction available for current thread - cannot reliably process 'merge' call
    // (propagation = Propagation.REQUIRES_NEW) contoh dari audit trail
    //// (propagation = Propagation.REQUIRED)
    @Transactional
    public void mappingMadrid(TxReception txReception, TxMadrid txMadrid) {
        txMadridCustomRepository.saveOrUpdate(txMadrid);

        MadridDetailInfo madridDetailInfo = new MadridDetailInfo();
        madridDetailInfo = madridDetail(txMadrid.getDocXMLDetail());

        txReceptionCustomRepository.saveOrUpdate(txReception);

        // insert to general
        TxTmGeneral txTmGeneral = new TxTmGeneral();
        txTmGeneral.setTxReception(txReception);
        txTmGeneral.setApplicationNo(txReception.getApplicationNo());
        txTmGeneral.setFilingDate(txReception.getCreatedDate());

        txTmGeneral.setmStatus(StatusEnum.M_NEW_APPLICATION.value());
        txTmGeneral.setCreatedBy(txReception.getCreatedBy());
        txTmGeneral.setCreatedDate(txReception.getCreatedDate());
        List<MLaw> mLawList = mLawRepository.findByStatusFlagTrue();
        for (MLaw ml : mLawList) {
            txTmGeneral.setmLaw(ml);
        }
        txTmGeneral.setXmlFileId(txReception.getXmlFileId());
        txTmGeneral.setIrn(txReception.getBankCode());
        txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);

        //mapping txtmOwner diambil dari xml tag HOLGR, tag ini bisa lebih dari 1 atau banyak owner.
        //jika TAG HOLGR lebih dari 1, maka setelah tag pertama / tag kedua dst dimasukkan ke tx_tm_owner_detail
        TxTmOwner ttoFirst = new TxTmOwner();
        for (MadridHolding mh : madridDetailInfo.getHolderList()) {
            if (madridDetailInfo.getHolderList().indexOf(mh) == 0) {
                TxTmOwner txTmOwner = new TxTmOwner();
                txTmOwner.setName(mh.getHoldName());
                txTmOwner.setAddress(mh.getHoldAddress());
                MCountry mco = mCountryCostumRepository.selectOne("id", mh.getHoldCountry().trim(), false);
                if (mco != null) {
                    txTmOwner.setNationality(mco);
                    txTmOwner.setmCountry(mco);
                }
                txTmOwner.setTxTmGeneral(txTmGeneral);
                txTmOwner.setCreatedBy(txReception.getCreatedBy());
                txTmOwner.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txTmOwner.setXmlFileId(txReception.getXmlFileId());
                //txTmOwnerCustomRepository.saveOrUpdate(txTmOwner);
                txTmOwnerRepository.save(txTmOwner);
                ttoFirst = txTmOwner;
            } else {
                TxTmOwnerDetail txTmOwnerDetail = new TxTmOwnerDetail();
                txTmOwnerDetail.setName(mh.getHoldName());
                txTmOwnerDetail.setStatus(true);
                txTmOwnerDetail.setTxTmGeneral(txTmGeneral);
                txTmOwnerDetail.setTxTmOwner(ttoFirst);
                txTmOwnerDetail.setXmlFileId(txReception.getXmlFileId());
                txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
            }
        }

        //mapping  MRepresentative
        for (MadridReprs mr : madridDetailInfo.getReprsList()) {
            MRepresentative mRepresentative = mrepresentativeCustomRepository.selectOne("name", mr.getRepName().toUpperCase().trim(), false); //reprsService.findOneByName(mr.getRepName());
            if (mRepresentative == null) {
                mRepresentative = new MRepresentative();
                mRepresentative.setNo(mr.getRepCLID());
                mRepresentative.setName(mr.getRepName());
                mRepresentative.setAddress(mr.getRepAddress());
                MCountry mco = mCountryCostumRepository.selectOne("id", mr.getRepCountry().trim(), false);
                if (mco != null) {
                    mRepresentative.setmCountry(mco);
                }
                mRepresentative.setStatusFlag(false);
                mRepresentative.setCreatedBy(txReception.getCreatedBy());
                mRepresentative.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                mrepresentativeCustomRepository.saveOrUpdate(mRepresentative);

                TxTmRepresentative txTmRepresentative = new TxTmRepresentative();
                txTmRepresentative.setTxTmGeneral(txTmGeneral);
                txTmRepresentative.setmRepresentative(mRepresentative);
                txTmRepresentative.setName(mRepresentative.getName());
                txTmRepresentative.setAddress(mRepresentative.getAddress());
                txTmRepresentative.setmCountry(mRepresentative.getmCountry());
                txTmRepresentative.setCreatedBy(txReception.getCreatedBy());
                txTmRepresentative.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txTmRepresentative.setXmlFileId(txReception.getXmlFileId());
                txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
            }
        }

        //mapping TxTmBrand
        TxTmBrand txTmBrand = new TxTmBrand();
        txTmBrand.setTxTmGeneral(txTmGeneral);
        txTmBrand.setmBrandType(madridDetailInfo.getBrandType());
        txTmBrand.setName(madridDetailInfo.getBrandName());
        txTmBrand.setFileName(madridDetailInfo.getFileName());
        txTmBrand.setDescription(madridDetailInfo.getMarkDescription());
        txTmBrand.setColor(madridDetailInfo.getColorsClaimed());
        txTmBrand.setDisclaimer(madridDetailInfo.getDisclaimer());
        txTmBrand.setTranslation(madridDetailInfo.getTranslation());
        txTmBrand.setDescChar(madridDetailInfo.getMarkDescriptionChar());
        txTmBrand.setCreatedBy(txReception.getCreatedBy());
        txTmBrand.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        txTmBrand.setXmlFileId(txReception.getXmlFileId());
        txTmBrandCustomRepository.saveOrUpdate(txTmBrand);
        //brandService.saveOrUpdate(txTmBrand);

        //mapping TxTmPrior
        for (MadridPrior mp : madridDetailInfo.getPriorityDetail()) {
            TxTmPrior txTmPrior = new TxTmPrior();
            txTmPrior.setTxTmGeneral(txTmGeneral);
            txTmPrior.setNo(mp.getPriorNo());
            try {
                txTmPrior.setPriorDate(DateUtil.toDate("dd/MM/yyyy", mp.getPriorDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            MCountry mCountry = mCountryCostumRepository.selectOne("id", mp.getPriorCountry().trim(), false);
            if (mCountry != null) {
                txTmPrior.setmCountry(mCountry);
            }
            txTmPrior.setStatus("PENDING");
            txTmPrior.setCreatedBy(txReception.getCreatedBy());
            txTmPrior.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            txTmPrior.setXmlFileId(txReception.getXmlFileId());
            txTmPriorCustomRepository.saveOrUpdate(txTmPrior);
        }

        //mapping class
        //-> mapping ke m_class_detail
        //penanda ; adalah new record
        //kolom notes diisi "Diterjemahkan oleh Microsoft Bing"
        LanguageTranslatorEnum languageTranslatorEnum = LanguageTranslatorEnum.BING;
        Iterator<Map.Entry<String, String>> it = madridDetailInfo.getBasicList().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> mapClass = it.next();
            String no_ = mapClass.getKey();
            String desc_ = mapClass.getValue();

            MClass mClass = mClassHeaderRepository.findFirstByNo(Integer.valueOf(no_));//classService.findFirstByNo(Integer.valueOf(no_) );
            //HashMap<Integer, MClassDetail> hMClassDetail = new HashMap<>();
            String[] arrDesc = desc_.split("\\;");
            MClassDetail existMClassDetail = null;
            MClassDetail mClassDetail = null;
            List<KeyValue> searchCriteria = null;
            String translateID = "";
            for (int i = 0; i < arrDesc.length; i++) {
                searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("parentClass", mClass, true));
                translateID = languageTranslatorEnum.getTranslation("en", "id", arrDesc[i].toString().trim());
                searchCriteria.add(new KeyValue("desc", translateID.trim(), false));
                existMClassDetail = mClassDetailCustomRepository.selectOne(searchCriteria, null, null);

                mClassDetail = new MClassDetail();
                if (existMClassDetail == null) {
                    mClassDetail.setParentClass(mClass);
                    mClassDetail.setDesc(translateID);
                    mClassDetail.setDescEn(arrDesc[i].toString().trim());
                    mClassDetail.setStatusFlag(false);
                    mClassDetail.setSerial2("Dari Madrid DCP");
                    mClassDetail.setNotes("Diterjemahkan oleh Microsoft Bing/Google Translate");
                    mClassDetail.setCreatedBy(txReception.getCreatedBy());
                    mClassDetail.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    mClassDetailCustomRepository.saveOrUpdate(mClassDetail);
                }

                TxTmClass txTmClass = new TxTmClass();
                txTmClass.setTxTmGeneral(txTmGeneral);
                txTmClass.setmClass(mClass);
                txTmClass.setmClassDetail(existMClassDetail != null ? existMClassDetail : mClassDetail);
                //txTmClass.setmClassDetail(mClassDetail);
                txTmClass.setCreatedBy(txReception.getCreatedBy());
                txTmClass.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txTmClass.setTransactionStatus("PENDING");
                txTmClass.setCorrectionFlag(false);
                txTmClass.setXmlFileId(txReception.getXmlFileId());
                txTmClassCustomRepository.saveOrUpdate(txTmClass);
            }
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void mappingMadriBasedOnHash(HashMap<TxReception, TxMadrid> hTemp) {
		/*List<TxReception> txrList = new ArrayList<>();
		List<TxMadrid> txmList = new ArrayList<>();
		List<MadridDetailInfo> mdiList = new ArrayList<>();				
		
		
		MadridDetailInfo madridDetailInfo = new MadridDetailInfo();				
		madridDetailInfo = madridDetail(txMadrid.getDocXMLDetail() );			
		
		txrList.add(txReception);
		txmList.add(txMadrid);
		mdiList.add(madridDetailInfo);*/

        for (Map.Entry<TxReception, TxMadrid> e : hTemp.entrySet()) {
            TxReception txReception = e.getKey();
            TxMadrid txMadrid = e.getValue();


            txMadridCustomRepository.saveOrUpdate(txMadrid);

            MadridDetailInfo madridDetailInfo = new MadridDetailInfo();
            madridDetailInfo = madridDetail(txMadrid.getDocXMLDetail());

            txReceptionCustomRepository.saveOrUpdate(txReception);

            // insert to general
            TxTmGeneral txTmGeneral = new TxTmGeneral();
            txTmGeneral.setTxReception(txReception);
            txTmGeneral.setApplicationNo(txReception.getApplicationNo());
            txTmGeneral.setFilingDate(txReception.getCreatedDate());

            txTmGeneral.setmStatus(StatusEnum.M_NEW_APPLICATION.value());
            txTmGeneral.setCreatedBy(txReception.getCreatedBy());
            txTmGeneral.setCreatedDate(txReception.getCreatedDate());
            List<MLaw> mLawList = mLawRepository.findByStatusFlagTrue();
            for (MLaw ml : mLawList) {
                txTmGeneral.setmLaw(ml);
            }
            txTmGeneral.setXmlFileId(txReception.getXmlFileId());
            txTmGeneral.setIrn(txReception.getBankCode());
            txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);


            //mapping txtmOwner diambil dari xml tag HOLGR, tag ini bisa lebih dari 1 atau banyak owner.
            //jika TAG HOLGR lebih dari 1, maka setelah tag pertama / tag kedua dst dimasukkan ke tx_tm_owner_detail
            TxTmOwner ttoFirst = new TxTmOwner();
            for (MadridHolding mh : madridDetailInfo.getHolderList()) {
                if (madridDetailInfo.getHolderList().indexOf(mh) == 0) {
                    TxTmOwner txTmOwner = new TxTmOwner();
                    txTmOwner.setName(mh.getHoldName());
                    txTmOwner.setAddress(mh.getHoldAddress());
                    MCountry mco = mCountryCostumRepository.selectOne("id", mh.getHoldCountry().trim(), false);
                    if (mco != null) {
                        txTmOwner.setNationality(mco);
                        txTmOwner.setmCountry(mco);
                    }
                    txTmOwner.setTxTmGeneral(txTmGeneral);
                    txTmOwner.setCreatedBy(txReception.getCreatedBy());
                    txTmOwner.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    txTmOwner.setXmlFileId(txReception.getXmlFileId());
                    txTmOwnerCustomRepository.saveOrUpdate(txTmOwner);
                    ttoFirst = txTmOwner;
                } else {
                    TxTmOwnerDetail txTmOwnerDetail = new TxTmOwnerDetail();
                    txTmOwnerDetail.setName(mh.getHoldName());
                    txTmOwnerDetail.setStatus(true);
                    txTmOwnerDetail.setTxTmGeneral(txTmGeneral);
                    txTmOwnerDetail.setTxTmOwner(ttoFirst);
                    txTmOwnerDetail.setXmlFileId(txReception.getXmlFileId());

                    txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
                }
            }

            //mapping  MRepresentative
            for (MadridReprs mr : madridDetailInfo.getReprsList()) {
                MRepresentative mRepresentative = mrepresentativeCustomRepository.selectOne("name", mr.getRepName().toUpperCase().trim(), false); //reprsService.findOneByName(mr.getRepName());
                if (mRepresentative == null) {
                    mRepresentative = new MRepresentative();
                    mRepresentative.setNo(mr.getRepCLID());
                    mRepresentative.setName(mr.getRepName());
                    mRepresentative.setAddress(mr.getRepAddress());
                    MCountry mco = mCountryCostumRepository.selectOne("id", mr.getRepCountry().trim(), false);
                    if (mco != null) {
                        mRepresentative.setmCountry(mco);
                    }
                    mRepresentative.setStatusFlag(false);
                    mRepresentative.setCreatedBy(txReception.getCreatedBy());
                    mRepresentative.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    mrepresentativeCustomRepository.saveOrUpdate(mRepresentative);

                    TxTmRepresentative txTmRepresentative = new TxTmRepresentative();
                    txTmRepresentative.setTxTmGeneral(txTmGeneral);
                    txTmRepresentative.setmRepresentative(mRepresentative);
                    txTmRepresentative.setName(mRepresentative.getName());
                    txTmRepresentative.setAddress(mRepresentative.getAddress());
                    txTmRepresentative.setmCountry(mRepresentative.getmCountry());
                    txTmRepresentative.setCreatedBy(txReception.getCreatedBy());
                    txTmRepresentative.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    txTmRepresentative.setXmlFileId(txReception.getXmlFileId());
                    txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
                }
            }

            //mapping TxTmBrand
            TxTmBrand txTmBrand = new TxTmBrand();
            txTmBrand.setTxTmGeneral(txTmGeneral);
            txTmBrand.setmBrandType(madridDetailInfo.getBrandType());
            txTmBrand.setName(madridDetailInfo.getBrandName());
            txTmBrand.setFileName(madridDetailInfo.getFileName());
            txTmBrand.setDescription(madridDetailInfo.getMarkDescription());
            txTmBrand.setColor(madridDetailInfo.getColorsClaimed());
            txTmBrand.setDisclaimer(madridDetailInfo.getDisclaimer());
            txTmBrand.setTranslation(madridDetailInfo.getTranslation());
            txTmBrand.setDescChar(madridDetailInfo.getMarkDescriptionChar());
            txTmBrand.setCreatedBy(txReception.getCreatedBy());
            txTmBrand.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            txTmBrand.setXmlFileId(txReception.getXmlFileId());
            txTmBrandCustomRepository.saveOrUpdate(txTmBrand);
            //brandService.saveOrUpdate(txTmBrand);


            //mapping TxTmPrior
            for (MadridPrior mp : madridDetailInfo.getPriorityDetail()) {
                TxTmPrior txTmPrior = new TxTmPrior();
                txTmPrior.setTxTmGeneral(txTmGeneral);
                txTmPrior.setNo(mp.getPriorNo());
                try {
                    txTmPrior.setPriorDate(DateUtil.toDate("dd/MM/yyyy", mp.getPriorDate()));
                } catch (ParseException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                }
                MCountry mCountry = mCountryCostumRepository.selectOne("id", mp.getPriorCountry().trim(), false);
                if (mCountry != null) {
                    txTmPrior.setmCountry(mCountry);
                }
                txTmPrior.setStatus("PENDING");
                txTmPrior.setCreatedBy(txReception.getCreatedBy());
                txTmPrior.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txTmPrior.setXmlFileId(txReception.getXmlFileId());
                txTmPriorCustomRepository.saveOrUpdate(txTmPrior);
                //priorService.doSaveOrUpdate(txTmPrior);
            }

            //mapping class
            //-> mapping ke m_class_detail
            //penanda ; adalah new record
            //kolom notes diisi "Diterjemahkan oleh Microsoft Bing"
            LanguageTranslatorEnum languageTranslatorEnum = LanguageTranslatorEnum.BING;
            Iterator<Map.Entry<String, String>> it = madridDetailInfo.getBasicList().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> mapClass = it.next();
                String no_ = mapClass.getKey();
                String desc_ = mapClass.getValue();

                MClass mClass = mClassHeaderRepository.findFirstByNo(Integer.valueOf(no_));//classService.findFirstByNo(Integer.valueOf(no_) );
                //HashMap<Integer, MClassDetail> hMClassDetail = new HashMap<>();
                String[] arrDesc = desc_.split("\\;");
                MClassDetail existMClassDetail = null;
                MClassDetail mClassDetail = null;
                List<KeyValue> searchCriteria = null;
                String translateID = "";
                for (int i = 0; i < arrDesc.length; i++) {
                    searchCriteria = new ArrayList<>();
                    searchCriteria.add(new KeyValue("parentClass", mClass, true));
                    translateID = languageTranslatorEnum.getTranslation("en", "id", arrDesc[i].toString().trim());
                    searchCriteria.add(new KeyValue("desc", translateID.trim(), false));
                    existMClassDetail = mClassDetailCustomRepository.selectOne(searchCriteria, null, null);

                    mClassDetail = new MClassDetail();
                    if (existMClassDetail == null) {
                        mClassDetail.setParentClass(mClass);
                        mClassDetail.setDesc(translateID);
                        mClassDetail.setDescEn(arrDesc[i].toString().trim());
                        mClassDetail.setStatusFlag(false);
                        mClassDetail.setSerial2("Dari Madrid DCP");
                        mClassDetail.setNotes("Diterjemahkan oleh Microsoft Bing/Google Translate");
                        mClassDetail.setCreatedBy(txReception.getCreatedBy());
                        mClassDetail.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                        mClassDetailCustomRepository.saveOrUpdate(mClassDetail);
                    }

                    TxTmClass txTmClass = new TxTmClass();
                    txTmClass.setTxTmGeneral(txTmGeneral);
                    txTmClass.setmClass(mClass);
                    txTmClass.setmClassDetail(existMClassDetail != null ? existMClassDetail : mClassDetail);
                    //txTmClass.setmClassDetail(mClassDetail);
                    txTmClass.setCreatedBy(txReception.getCreatedBy());
                    txTmClass.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    txTmClass.setTransactionStatus("PENDING");
                    txTmClass.setCorrectionFlag(false);
                    txTmClass.setXmlFileId(txReception.getXmlFileId());
                    txTmClassCustomRepository.saveOrUpdate(txTmClass);
                }
            }
        }
	/*	
		MadridListObject madridListObject = new MadridListObject();
		madridListObject.setTxReceptionList(txrList);
		madridListObject.setTxMadridList(txmList);
		madridListObject.setMadridDetailInfoList(mdiList);

		//call store / pl-sql procedure ?????
		System.out.println("call store / pl-sql procedure");
	    
	    txMadridCustomRepository.processMadridProcedure(txrList,txmList ,mdiList);
	    */
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMadridBasedFileId(String xmlFileId) {
        txMadridCustomRepository.deleteAllTransactionFileId(xmlFileId);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMadridXMLFile(String xmlFileId) {
        txMadridCustomRepository.modifiedXMLFile(xmlFileId);
    }

    public MadridDetailInfo madridDetail(String xmlString) {
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

            //brand, default is MARCOLI
            MBrandType mBrandType = null;
            if ("MARCOLI".contains(xmlString)) {
                mBrandType = brandService.selectOne("xmlMadrid", "MARCOLI");
            } else if ("MARKVE".contains(xmlString)) {
                mBrandType = brandService.selectOne("xmlMadrid", "MARKVE");
            } else if ("SOUMARI".contains(xmlString)) {
                mBrandType = brandService.selectOne("xmlMadrid", "SOUMARI");
            } else if ("THRDMAR".contains(xmlString)) {
                mBrandType = brandService.selectOne("xmlMadrid", "THRDMAR");
            } else {
                mBrandType = brandService.selectOne("xmlMadrid", "MARCOLI");
            }


            mdi.setBrandType(mBrandType);
            mdi.setBrandTypeName(mBrandType.getName()); //display in user interface

            //<IMAGE NAME="1430298" TEXT="Youm Balance" COLOUR="N" TYPE="JPG" />
            String filename = "";
            String brandName = "";
            Element eImg = jdomDocument.getRootElement().getChild("IMAGE");
            if (eImg != null) {
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

            String tempClass = "";
            Element e = null;
            ElementFilter ef = new ElementFilter();

            e = jdomDocument.getRootElement().getChild("BASICGS");
            Map<String, String> hClass = new HashMap<String, String>();
            if (e != null) {
                List<KeyValue> searchCriteria = null;
                LanguageTranslatorEnum languageTranslatorEnum = LanguageTranslatorEnum.BING;

                ef = new ElementFilter("GSGR");
                for (Element c : e.getDescendants(ef)) {
                    tempClass = c.getAttributeValue("NICCLAI");
                    if (!tempClass.isEmpty()) {
                        ElementFilter efDetail = new ElementFilter("GSTERMEN");
                        for (Element elDetail : c.getDescendants(efDetail)) {
                            //hClass.put(tempClass, elDetail.getTextNormalize());

                            String classHeader = hListClass.get(Integer.valueOf(tempClass)).getId();
                            String[] arrDesc = (elDetail.getTextNormalize()).split("\\;");

                            for (int i = 0; i < arrDesc.length; i++) {
                                String descEn = arrDesc[i].toString().trim();
                                String descID = languageTranslatorEnum.getTranslation("en", "id", descEn);
                                searchCriteria = new ArrayList<>();
                                searchCriteria.add(new KeyValue("parentClass.id", classHeader, true));
                                searchCriteria.add(new KeyValue("desc", descID.trim(), false));

                                MClassDetail mClassDetail = mClassDetailCustomRepository.selectOne(searchCriteria, null, null);
                                String classDetail = mClassDetail == null ? "null" : mClassDetail.getId();
                                hClass.put(tempClass, classHeader + ";" + classDetail + ";" + descEn + ";" + descID);
                            }
                        }
                    }
                }
            }
            TreeMap<String, String> sortedMap = new TreeMap<>();
            sortedMap.putAll(hClass);
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

    @Transactional
    public String prosesCopyAndInsertMadridPDF(PDFFile file) throws IOException {
        String catatan = "";
        String fileName = file.getFileName().substring(0, file.getFileName().lastIndexOf(".")).toUpperCase();
        catatan += "Folder Utama : " + fileName + '\n';
        File files = new File(wipoMadridExtractLocationPdf + file.getModifiedYear() + File.separator + fileName);
        if (files.exists()) {
            String[] directories = files.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });
            File folderPdf = null;
            for (String dir : directories) {
                folderPdf = new File(wipoMadridExtractLocationPdf + file.getModifiedYear() + File.separator + fileName + File.separator + dir);
                if (folderPdf.exists()) {
                    File[] listOfFilesPdf = folderPdf.listFiles();
                    Arrays.stream(listOfFilesPdf).forEach(
                            data -> {
                                if (data.isFile()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
                                    sdf.format(data.lastModified());

                                    String pathFolder = sdf.format(data.lastModified());
                                    String target = uploadFileDocApplicationPath + pathFolder + data.getName();
                                    try {
                                        String pathFrom = UriUtils.decode(data.getPath(), "UTF-8");
                                        String pathTO = UriUtils.decode(target, "UTF-8");
                                        copy(pathFrom, pathTO);

                                        String[] parts = data.getName().split("-");
                                        if (parts.length == 3 || parts.length == 2) {
                                            insertTxTmDocWhenCopy(data, null);
                                        } else {
//                                                            throw new IllegalArgumentException("Nama File ("+ data.getName() + ")Tidak Sesuai Format atau Kasus Khusus");
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else if (data.isDirectory()) {
                                    //System.out.println("Directory " + data.getName());
                                }
                            }
                    );

                    catatan += "  - Total File di subFolder " + dir + " = " + listOfFilesPdf.length + '\n';
                } else {
                    catatan += "  - Tidak Ada File yang diCopy" + '\n';
                }
            }

            file.setProcessed(true);
            wipoService.savePdfFile(file);
        } else {
            catatan += "  - Tidak Ada File yang diCopy" + '\n';
        }
        //System.out.println(catatan);
        return catatan;
    }

    public List<PdfFile> listPdfFiles(PDFFile file) throws IOException {
        List<PdfFile> result = new ArrayList<>();

        String fileName = file.getFileName().substring(0, file.getFileName().lastIndexOf(".")).toUpperCase();
        File files = new File(wipoMadridExtractLocationPdf + file.getModifiedYear() + File.separator + fileName);
        if (files.exists()) {
            String[] directories = files.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });

            for (String dir : directories) {
                File folderPdf = new File(wipoMadridExtractLocationPdf + file.getModifiedYear() + File.separator + fileName + File.separator + dir);
                if (folderPdf.exists()) {
                    File[] listOfFilesPdf = folderPdf.listFiles();
                    Arrays.stream(listOfFilesPdf).forEach(
                            data -> {
                                if (data.isFile()) {
                                    try {
                                        String pathFrom = UriUtils.decode(data.getPath(), "UTF-8");

                                        String[] parts = data.getName().split("-");
                                        if (parts.length == 3) {
                                            PdfFile pdfFile = new PdfFile(parts[0], parts[1], parts[2].split("\\.")[0], pathFrom, data.length(), data.getName());
                                            result.add(pdfFile);
                                        } else if (parts.length == 2) {
                                            PdfFile pdfFile = new PdfFile(null, parts[0], parts[1].split("\\.")[0], pathFrom, data.length(), data.getName());
                                            result.add(pdfFile);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    );
                }
            }
        }

        return result;
    }


    @Transactional
    public void prosesCopyAndInsertMadridXML(XMLFile file) throws IOException {
        if (hListCountry == null) {
            hListCountry = new HashMap<String, MCountry>();
            List<MCountry> listCountry = mCountryRepository.findByStatusFlagTrue();
            for (MCountry mCountry : listCountry) {
                hListCountry.put(mCountry.getId(), mCountry);
            }
        }
        if (hListClass == null) {
            hListClass = new HashMap<Integer, MClass>();
            List<MClass> listClass = mClassHeaderRepository.findByStatusFlagTrue();
            for (MClass mClass : listClass) {
                hListClass.put(mClass.getNo(), mClass);
            }
        }

        if (hListReceptionMadrid == null) {
            hListReceptionMadrid = new HashMap<String, TxReception>();
            hListGeneralMadrid = new HashMap<String, TxTmGeneral>();
            hListMadrid = new HashMap<String, TxMadrid>();
            hListTxTmOwnerMadrid = new HashMap<String, TxTmOwner>();
            hListTxTmBrandMadrid = new HashMap<String, TxTmBrand>();

            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("mFileType.code", "M", true));
            List<TxReception> listReception = txReceptionCustomRepository.selectAllReception(searchCriteria, null, null, null, null);
            for (TxReception txReception : listReception) {
                hListReceptionMadrid.put(txReception.getBankCode(), txReception);
                TxTmGeneral txTmGeneral = txTmGeneralCustomRepository.select("applicationNo", txReception.getApplicationNo().trim());
                hListGeneralMadrid.put(txReception.getApplicationNo(), txTmGeneral);

                TxTmOwner txTmOwner = txTmOwnerCustomRepository.selectOne("txTmGeneral", txTmGeneral);
                hListTxTmOwnerMadrid.put(txTmGeneral.getId(), txTmOwner);

                TxTmRepresentative txTmRepresentative = txTmReprsCustomRepository.selectOne("txTmGeneral", txTmGeneral);
                hListTxTmReprsMadrid.put(txTmGeneral.getId(), txTmRepresentative);

                MRepresentative mRepresentative = mRepresentativeCustomRepository.selectOne("id", txTmRepresentative.getmRepresentative().getId(), true);
                hListMReprsMadrid.put(mRepresentative.getId(), mRepresentative);

                TxTmBrand txTmBrand = txTmBrandCustomRepository.selectOne("txTmGeneral", txTmGeneral);
                if (txTmBrand != null) {
                    hListTxTmBrandMadrid.put(txTmGeneral.getId(), txTmBrand);
                }
            }

            List<TxMadrid> listMadrid = txMadridCustomRepository.selectAll(null, null, null, null, null);
            for (TxMadrid txMadrid : listMadrid) {
                hListMadrid.put(txMadrid.getId(), txMadrid);
            }
        }

        try {
            //delete first transcation based on xml file id
            String xmlFileId = file.getId();
            deleteMadridBasedFileId(xmlFileId);

            String replaceFirst = file.getFileName().replaceAll("x", "N");
            String replaceExtension = replaceFirst.replaceAll(".zip", ".XML");

            File files = new File(wipoMadridExtractLocationXml + file.getModifiedYear() + File.separator + replaceExtension);
            if (files.exists()) {
                SAXBuilder saxBuilder = new SAXBuilder();
                Document document = (Document) saxBuilder.build(files);

                Element header = document.getRootElement();
                Document documentHeader = new Document(header.clone().detach());
                Element headerXML = documentHeader.getRootElement();
                headerXML.removeContent();

                Document docHeader = new Document(headerXML.clone());
                XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
                xmlOutputter.outputString(docHeader);    //create xml with all tag enotif or header tag.

                Element rootNode = document.getRootElement();
                //List<Element> nodes = rootNode.getChildren("RESTRICT");	 //FOR TESTING PER-TAG
                //HashMap<Element, TxMadrid> hElMadrid = new HashMap<>();
                List<Element> nodes = rootNode.getChildren();
                for (Element element : nodes) {
                    TxMadrid txMadrid = new TxMadrid();
                    txMadrid.setId(element.getAttribute("DOCID").getValue());
                    txMadrid.setNo(rootNode.getAttribute("GAZNO").getValue());
                    if (element.getAttribute("INTREGN") != null) {
                        txMadrid.setIntregn(element.getAttribute("INTREGN").getValue());
                    }
                    if (element.getAttribute("TRANTYP") != null) {
                        txMadrid.setTranTyp(element.getAttribute("TRANTYP").getValue());
                    }

                    txMadrid.setDocType(element.getName());
                    txMadrid.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

                    //header
                    txMadrid.setDocXMLHeader(xmlOutputter.outputString(docHeader));
                    txMadrid.setXmlFileId(xmlFileId);
                    //detail
                    Document docDetail = new Document(element.clone().detach());
                    XMLOutputter xmlOutputterDetail = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
                    xmlOutputterDetail.outputString(docDetail);
                    txMadrid.setDocXMLDetail(xmlOutputterDetail.outputString(docDetail));

                    if ("CORRECTION".equalsIgnoreCase(element.getName())) {
//						logger.debug(xmlOutputterDetail.outputString(docDetail));

                        Element elCorrect = element.getChild("CORRECT");
                        if (elCorrect != null) {
                            ElementFilter ef = new ElementFilter("BIRTH");
                            for (Element c : elCorrect.getDescendants(ef)) {
                                if (c.getAttribute("INTREGN") != null) {
                                    txMadrid.setIntregn(c.getAttribute("INTREGN").getValue());

                                }
                                if (c.getAttribute("TRANTYP") != null) {
                                    txMadrid.setTranTyp(c.getAttribute("TRANTYP").getValue());
                                }
                                txMadrid.setDocType(elCorrect.getName());

                                docDetail = new Document(c.clone().detach());
                                xmlOutputterDetail = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
                                xmlOutputterDetail.outputString(docDetail);    //create xml with all tag birth parent with child.
                                txMadrid.setDocXMLDetail(xmlOutputterDetail.outputString(docDetail));
                                mappingTxReception(txMadrid, c);

                            }
                        }
                    }

                    if (element.getAttribute("INTREGN") != null) {
                        mappingTxReception(txMadrid, element);
                    }
                }

                HashMap<String, MadridDetailInfo> hListProcessDetail = new HashMap<>();
                for (Map.Entry<TxReception, TxMadrid> e : hListProcess.entrySet()) {
                    TxReception txReception = e.getKey();
                    TxMadrid txMadrid = e.getValue();
                    hListProcessDetail.put(txReception.getId(), madridDetail(txMadrid.getDocXMLDetail()));
                }

                insertMappingMadrid(hListProcess, hListProcessDetail, hListCountry, hListGeneralMadrid, hListReceptionMadrid,
                        hListMadrid, hListTxTmOwnerMadrid, hListTxTmBrandMadrid);
                updateMadridXMLFile(file.getId());
                hListProcess = null;
                //madridService.mappingMadriBasedOnHash(hListProcess);
            }

            //madridService.updateMadridXMLFile(file.getId());

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
//			result.put("message", "Gagal membaca file. Pastikan file dalam format yang disediakan.");
        } catch (Exception e) {
            e.printStackTrace();
//			result.put("message", "Gagal membaca file. Pastikan file dalam format yang disediakan.");
        }
    }


    //function copied from madrid controller
    @Transactional
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
                txReception.setmFileSequence(fileSeqService.findOne("99"));
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
            txReception.setNotesIpas(txMadrid.getNo());
            txReception.setCreatedBy(mUser);
            txReception.setXmlFileId(txMadrid.getXmlFileId());
            //madridService.mappingMadrid(txReception, txMadrid);

            if (hListProcess == null) {
                hListProcess = new HashMap<>();
            }
            hListProcess.put(txReception, txMadrid);

        } catch (ParseException e) {
            e.printStackTrace();
//			logger.error(e);
        }

    }

    @Transactional
    public void prosesMadridImage(List<ImageFileList> listImage) {
        try {
            for (ImageFileList imageFileList: listImage) {
                TxReception txReception = txReceptionCustomRepository.selectOne("bankCode", imageFileList.getName(), true);
                if (txReception != null) {
                    TxTmGeneral txTmGeneral = txTmGeneralRepository.findByTxReception(txReception);
                    if (txTmGeneral != null) {
                        TxTmBrand txTmBrand = txTmGeneral.getTxTmBrand();
                        if (txTmBrand != null) {
                            //Format Image untuk brandlogo/ yg menggunakan "uploadFileBrandPath" adalah uuid.jpg ex. 8ad78671-009d-47b6-bb62-29f82fc97242.jpg
                            String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
                            String target = uploadFileBrandPath + pathFolder + txTmBrand.getId() + ".jpg";

                            //proses copy file
                            copy(imageFileList.getFilePath(), target);

                            //update is processes in tx_madrid_img_list
                            imageFileList.setProcessed(true);
                            imageFileListService.saveUpdateImageFileList(imageFileList);
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    @Transactional
    public void prosesCopyImageFromXML(ImageFileList file) throws IOException {
        try {
            //get application no from bank code
            TxReception txReception = permohonanOnlineService.selectOneTxReception("bankCode", file.getName());
            if (txReception != null) {
                String getApplicationNo = txReception.getApplicationNo();

                //get the file
                ImageFileList imageFileList = imageFileListService.selectOne(file.getName());

                //get application id from application no
                TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(getApplicationNo);
                //Format Image untuk brandlogo/ yg menggunakan "uploadFileBrandPath" adalah uuid.jpg ex. 8ad78671-009d-47b6-bb62-29f82fc97242.jpg
                String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                String target = uploadFileBrandPath + pathFolder + txTmGeneral.getTxTmBrand().getId() + ".jpg";

                //proses copy file
                copy(imageFileList.getFilePath(), target);

                //update is processes in tx_madrid_img_list
                imageFileList.setProcessed(true);
                imageFileListService.saveUpdateImageFileList(imageFileList);
            }
        } catch (Exception e) {
            // TODO: handle exception
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

    @Transactional
    public void insertMappingMadrid(HashMap<TxReception, TxMadrid> hProcess, HashMap<String, MadridDetailInfo> hProcessDetail, HashMap<String, MCountry> hCountry,
                                    HashMap<String, TxTmGeneral> hListGeneralMadrid, HashMap<String, TxReception> hListReceptionMadrid, HashMap<String, TxMadrid> hListMadrid,
                                    HashMap<String, TxTmOwner> hListTxTmOwnerMadrid, HashMap<String, TxTmBrand> hListTxTmBrandMadrid) {

        List<MLaw> mLawList = mLawRepository.findByStatusFlagTrue();
        int count = 1;

        MWorkflow wfMadrid = mWorkflowCustomRepository.selectOne("id", "WF_UU_20");

        for (Map.Entry<TxReception, TxMadrid> e : hProcess.entrySet()) {
            //System.out.println("Reception ke-" + count++);
            TxReception txReception = e.getKey();
            TxMadrid txMadrid = e.getValue();
            MadridDetailInfo madridDetailInfo = hProcessDetail.get(txReception.getId());
            txMadrid.setSkipLogAuditTrail(true);
            if (txMadrid.getCreatedBy() == null) {
                MUser mUser = new MUser();
                mUser.setId((userService.getUserByName("SYSTEM")).getId());
                txMadrid.setCreatedBy(mUser);
            }

            txReception.setSkipLogAuditTrail(true);
            //System.out.println("TxMadrid Id -" + txMadrid.getId());

            TxMadrid existTxMadrid = hListMadrid.get(txMadrid.getId());
            if (existTxMadrid != null) {
                txMadrid.setId(existTxMadrid.getId());
                txMadrid.setIntregn(existTxMadrid.getIntregn());
            }

            txMadridCustomRepository.saveOrUpdate(txMadrid);

            TxReception existTxReception = hListReceptionMadrid.get(txReception.getBankCode().trim());
            if (existTxReception != null) {
//                System.out.println("========================================================== DOUBLE");
//                System.out.println("========================================================== ::: " + existTxReception.getCurrentId());
                txReception.setId(existTxReception.getCurrentId());
                txReception.setApplicationNo(existTxReception.getApplicationNo());
                txReception.setApplicationDate(existTxReception.getApplicationDate());
                txReception.setBankCode(existTxReception.getBankCode());
                txReception.setCreatedBy(existTxReception.getCreatedBy());
                txReception.setNotesIpas(txMadrid.getNo());
                //txReceptionCustomRepository.saveOrUpdateReceptionMerge(txReception);
            } else {
                //System.out.println("========================================================== >>> " + txReception.getId()
                  //      + " ::: " + txReception.getBankCode());
                //txReceptionCustomRepository.saveOrUpdateReceptionPersist(txReception);
            }
            txReception.setmWorkflow(wfMadrid);
            txReceptionCustomRepository.saveOrUpdate(txReception);

            TxTmGeneral existTxTmGeneral = hListGeneralMadrid.get(txReception.getApplicationNo());
            TxTmGeneral txTmGeneral = new TxTmGeneral();
            if (existTxTmGeneral != null) {
                txTmGeneral.setTxReception(txReception);
                txTmGeneral.setId(existTxTmGeneral.getCurrentId());
                txTmGeneral.setApplicationNo(existTxTmGeneral.getTxReception().getApplicationNo());
                txTmGeneral.setFilingDate(existTxTmGeneral.getFilingDate());
            } else {
                // insert to general
                txTmGeneral.setTxReception(txReception);
                txTmGeneral.setApplicationNo(txReception.getApplicationNo());
                txTmGeneral.setFilingDate(txReception.getCreatedDate());
            }

            txTmGeneral.setmStatus(StatusEnum.M_NEW_APPLICATION.value());
            txTmGeneral.setCreatedBy(txReception.getCreatedBy());
            txTmGeneral.setCreatedDate(txReception.getCreatedDate());
            for (MLaw ml : mLawList) {
                txTmGeneral.setmLaw(ml);
            }
            txTmGeneral.setXmlFileId(txReception.getXmlFileId());
            txTmGeneral.setIrn(txReception.getBankCode());
            txTmGeneral.setSkipLogAuditTrail(true);
            txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);

            MWorkflowProcess mWorkflowProcess = mWorkflowProcessCustomRepository.selectOne("status.id", txTmGeneral.getmStatus().getId(), true);
            if (mWorkflowProcess != null) {
                TxMonitor txMonitor = new TxMonitor();
                txMonitor.setTxTmGeneral(txTmGeneral);
                txMonitor.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txMonitor.setCreatedBy(txReception.getCreatedBy());
                txMonitor.setmWorkflowProcess(mWorkflowProcess);
                txMonitorRepository.save(txMonitor);
            }


            //existing data will be set non active / status false. diremarks karena membuat menjadi request time out
            TxTmOwner existTxTmOwner = hListTxTmOwnerMadrid.get(txTmGeneral.getId());
            if (existTxTmOwner != null) {
                existTxTmOwner.setStatus(false);
                existTxTmOwner.setXmlFileId(txReception.getXmlFileId());
                txTmOwnerCustomRepository.saveOrUpdate(existTxTmOwner);
            }

            //mapping txtmOwner diambil dari xml tag HOLGR, tag ini bisa lebih dari 1 atau banyak owner.
            //jika TAG HOLGR lebih dari 1, maka setelah tag pertama / tag kedua dst dimasukkan ke tx_tm_owner_detail
            TxTmOwner ttoFirst = new TxTmOwner();
            for (MadridHolding mh : madridDetailInfo.getHolderList()) {
                if (madridDetailInfo.getHolderList().indexOf(mh) == 0) {
                    TxTmOwner txTmOwner = new TxTmOwner();
                    txTmOwner.setName(mh.getHoldName());
                    txTmOwner.setOwnerType("Badan Hukum");
                    txTmOwner.setAddress(mh.getHoldAddress());
                    MCountry mco = hCountry.get(mh.getHoldCountry().trim());
                    if (mco != null) {
                        txTmOwner.setNationality(mco);
                        txTmOwner.setmCountry(mco);
                    }
                    txTmOwner.setTxTmGeneral(txTmGeneral);
                    txTmOwner.setCreatedBy(txReception.getCreatedBy());
                    txTmOwner.setCreatedDate(new Timestamp(System.currentTimeMillis()));

                    txTmOwner.setXmlFileId(txReception.getXmlFileId());
                    txTmOwner.setSkipLogAuditTrail(true);
                    txTmOwner.setStatus(true);
                    txTmOwnerCustomRepository.saveOrUpdate(txTmOwner);
                    ttoFirst = txTmOwner;
                    hListTxTmOwnerMadrid.put(txTmGeneral.getId(), txTmOwner);
                } else {
                    TxTmOwnerDetail txTmOwnerDetail = new TxTmOwnerDetail();
                    txTmOwnerDetail.setName(mh.getHoldName());
                    txTmOwnerDetail.setStatus(true);
                    txTmOwnerDetail.setTxTmGeneral(txTmGeneral);
                    txTmOwnerDetail.setTxTmOwner(ttoFirst);
                    txTmOwnerDetail.setXmlFileId(txReception.getXmlFileId());
                    txTmOwnerDetail.setSkipLogAuditTrail(true);
                    txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
                }
            }

            for (MadridReprs mr : madridDetailInfo.getReprsList()) {
                MRepresentative mRepresentative = new MRepresentative();
                mRepresentative.setId(mr.getRepId());
				/*if(mr.isRepExist()) {
					mrepresentativeCustomRepository.delete(mr.getRepId());	 
					mRepresentative.setNo(mr.getRepCLID()); 
					mRepresentative.setName(mr.getRepName());
					mRepresentative.setAddress(mr.getRepAddress());
					MCountry mco = hCountry.get(mr.getRepCountry().trim());			
					if(mco != null) {
						mRepresentative.setmCountry(mco);
					}				
					mRepresentative.setStatusFlag(false);
					mRepresentative.setCreatedBy(txReception.getCreatedBy());
					mRepresentative.setCreatedDate(new Timestamp(System.currentTimeMillis()));
					mRepresentative.setSkipLogAuditTrail(true);	
					mrepresentativeCustomRepository.saveOrUpdate(mRepresentative);								
				}*/

                if (!mr.isRepExist()) {
                    mRepresentative.setNo(mr.getRepCLID());
                    mRepresentative.setName(mr.getRepName());
                    mRepresentative.setAddress(mr.getRepAddress());
                    MCountry mco = hCountry.get(mr.getRepCountry().trim());
                    if (mco != null) {
                        mRepresentative.setmCountry(mco);
                    }
                    mRepresentative.setStatusFlag(true);
                    mRepresentative.setCreatedBy(txReception.getCreatedBy());
                    mRepresentative.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    mRepresentative.setSkipLogAuditTrail(true);
                    mrepresentativeCustomRepository.saveOrUpdate(mRepresentative);
                }

                TxTmRepresentative txTmRepresentative = new TxTmRepresentative();
                txTmRepresentative.setTxTmGeneral(txTmGeneral);
                txTmRepresentative.setmRepresentative(mRepresentative);
                txTmRepresentative.setName(mRepresentative.getName());
                txTmRepresentative.setAddress(mRepresentative.getAddress());
                txTmRepresentative.setmCountry(mRepresentative.getmCountry());
                txTmRepresentative.setCreatedBy(txReception.getCreatedBy());
                txTmRepresentative.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txTmRepresentative.setXmlFileId(txReception.getXmlFileId());
                txTmRepresentative.setSkipLogAuditTrail(true);
                txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
            }

            //mapping TxTmBrand
            TxTmBrand existTxTmBrand = hListTxTmBrandMadrid.get(txTmGeneral.getId());
            if (existTxTmBrand == null) {
                TxTmBrand txTmBrand = new TxTmBrand();
                txTmBrand.setTxTmGeneral(txTmGeneral);
                txTmBrand.setmBrandType(madridDetailInfo.getBrandType());
                txTmBrand.setName(madridDetailInfo.getBrandName());
                txTmBrand.setFileName(madridDetailInfo.getFileName());
                txTmBrand.setDescription(madridDetailInfo.getMarkDescription());
                txTmBrand.setColor(madridDetailInfo.getColorsClaimed());
                txTmBrand.setDisclaimer(madridDetailInfo.getDisclaimer());
                txTmBrand.setTranslation(madridDetailInfo.getTranslation());
                txTmBrand.setDescChar(madridDetailInfo.getMarkDescriptionChar());
                txTmBrand.setCreatedBy(txReception.getCreatedBy());
                txTmBrand.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txTmBrand.setXmlFileId(txReception.getXmlFileId());
                txTmBrand.setSkipLogAuditTrail(true);
                txTmBrandCustomRepository.saveOrUpdate(txTmBrand);
            }


            //mapping TxTmPrior
            for (MadridPrior mp : madridDetailInfo.getPriorityDetail()) {
                TxTmPrior txTmPrior = new TxTmPrior();
                txTmPrior.setTxTmGeneral(txTmGeneral);
                txTmPrior.setNo(mp.getPriorNo());
                try {
                    txTmPrior.setPriorDate(DateUtil.toDate("dd/MM/yyyy", mp.getPriorDate()));
                } catch (ParseException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                }
                MCountry mCountry = hCountry.get(mp.getPriorCountry().trim());
                if (mCountry != null) {
                    txTmPrior.setmCountry(mCountry);
                }
                txTmPrior.setStatus("PENDING");
                txTmPrior.setCreatedBy(txReception.getCreatedBy());
                txTmPrior.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txTmPrior.setXmlFileId(txReception.getXmlFileId());
                txTmPrior.setSkipLogAuditTrail(true);
                txTmPriorCustomRepository.saveOrUpdate(txTmPrior);
            }

            //mapping class
            //-> mapping ke m_class_detail
            //penanda ; adalah new record
            //kolom notes diisi "Diterjemahkan oleh Microsoft Bing"
            Iterator<Map.Entry<String, String>> it = madridDetailInfo.getBasicList().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> mapClass = it.next();
                //String no_ = mapClass.getKey();
                String desc_ = mapClass.getValue();
                String[] arr = desc_.split("\\;");

                MClass mClass = new MClass();
                mClass.setId(arr[0].trim());
                MClassDetail mClassDetail = new MClassDetail();

                if (arr[1].trim().equals("null")) {
                    mClassDetail.setParentClass(mClass);
                    mClassDetail.setDesc(arr[3].trim());
                    mClassDetail.setDescEn(arr[2].trim());
                    mClassDetail.setStatusFlag(false);
                    mClassDetail.setSerial2("Dari Madrid DCP");
                    mClassDetail.setNotes("Diterjemahkan oleh Microsoft Bing/Google Translate");
                    mClassDetail.setCreatedBy(txReception.getCreatedBy());
                    mClassDetail.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    mClassDetail.setSkipLogAuditTrail(true);
                    mClassDetailCustomRepository.saveOrUpdate(mClassDetail);
                } else {
                    mClassDetail.setId(arr[1].toString().trim());
                }


                TxTmClass txTmClass = new TxTmClass();
                txTmClass.setTxTmGeneral(txTmGeneral);
                txTmClass.setmClass(mClass);
                txTmClass.setmClassDetail(mClassDetail);
                txTmClass.setCreatedBy(txReception.getCreatedBy());
                txTmClass.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txTmClass.setTransactionStatus("PENDING");
                txTmClass.setCorrectionFlag(false);
                txTmClass.setXmlFileId(txReception.getXmlFileId());
                txTmClass.setSkipLogAuditTrail(true);
                txTmClassCustomRepository.saveOrUpdate(txTmClass);
            }
        }
    }

    @Transactional
    private void insertTxTmDoc(TxMadrid txMadrid,
                               TxTmGeneral txTmGeneral,
                               List<PdfFile> listPdf,
                               MDocType txTmDocMDocType
    ) {
        List<PdfFile> pdfs = listPdf.stream()
                .filter(x -> x.getDocid().equalsIgnoreCase(txMadrid.getId()))
                .collect(Collectors.toList());

        Date now = new Date();
        Timestamp createdDate = new Timestamp(now.getTime());

        for (PdfFile pdf : pdfs) {
            TxTmDoc txTmDocHolder = new TxTmDoc(); //uuid generated

            //Not Null Value
            txTmDocHolder.setCreatedDate(createdDate);
            txTmDocHolder.setStatus(true);
            txTmDocHolder.setCreatedBy(txMadrid.getCreatedBy());
            txTmDocHolder.setmDocType(txTmDocMDocType);
            txTmDocHolder.setTxTmGeneral(txTmGeneral);

            //Other Value
            txTmDocHolder.setDescription(null);
            txTmDocHolder.setFileName(pdf.getFileName());
            txTmDocHolder.setFileNameTemp(pdf.getFileName());
            txTmDocHolder.setFileSize(String.valueOf(pdf.getByteSize()));
            txTmDocHolder.setXmlFileId(txTmGeneral.getXmlFileId());
            txTmDocHolder.setSkipLogAuditTrail(false);

            txTmDocCustomRepository.saveOrUpdate(txTmDocHolder);

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
                String pathFolder = sdf.format(now);

                String pathTo = uploadFileDocApplicationPath + pathFolder + pdf.getFileName();;
                copy(pdf.getPathFrom(), pathTo);
            } catch (IOException e) {

            }
        }
    }

    @Transactional
    private boolean alreadyInsertTxMadrid(TxMadrid txMadrid) {
        // Cek already insert
        List<KeyValue> criteriaList = new ArrayList<>();
        criteriaList.add(new KeyValue("no", txMadrid.getNo(), true));
        criteriaList.add(new KeyValue("intregn", txMadrid.getIntregn(), true));
        criteriaList.add(new KeyValue("id", txMadrid.getId(), true));
        criteriaList.add(new KeyValue("tranTyp", txMadrid.getTranTyp(), true));
        List<TxMadrid> existMadrid = txMadridCustomRepository.selectAll(criteriaList, null, null, null, null);
        if (existMadrid.size() > 0) {
            return true;
        }

        return false;
    }

    @Transactional
    public void insertBirth(TxMadrid txMadrid
            , Element element
            , Map<String, Object> listLookup
    ) {
        try {
            // Cek already insert
            if (alreadyInsertTxMadrid(txMadrid)) {
                return;
            }

            MUser currentUser = (MUser) listLookup.get("currentUser");
            MWorkflow wfMadrid = (MWorkflow) listLookup.get("wfMadrid");
            Map<String, MFileSequence> lookupSequence = (Map<String, MFileSequence>) listLookup.get("lookupSequence");
            Map<String, MFileType> lookupFileType = (Map<String, MFileType>) listLookup.get("lookupFileType");
            MFileTypeDetail fileTypeDetailMD = (MFileTypeDetail) listLookup.get("fileTypeDetailMD");
            MFileTypeDetail fileTypeDetailMO = (MFileTypeDetail) listLookup.get("fileTypeDetailMO");
            MLaw mLaw = (MLaw) listLookup.get("mLaw");
            MWorkflowProcess mWorkflowProcess = (MWorkflowProcess) listLookup.get("mWorkflowProcess");
            Map<String, MCountry> lookupCountry = (Map<String, MCountry>) listLookup.get("lookupCountry");
            MBrandType brandTypeSigvrbl = (MBrandType) listLookup.get("brandTypeSigvrbl");
            MBrandType brandTypeMarkve = (MBrandType) listLookup.get("brandTypeMarkve");
            MBrandType brandTypeSoumari = (MBrandType) listLookup.get("brandTypeSoumari");
            MBrandType brandTypeThrdmar = (MBrandType) listLookup.get("brandTypeThrdmar");
            MBrandType brandDefault = (MBrandType) listLookup.get("brandDefault");
            Map<Integer, MClass> lookupClassHeader = (Map<Integer, MClass>) listLookup.get("lookupClassHeader");

            // CASE CPN
            List<TxReception> listTxReception = txReceptionRepository.findByBankCode(txMadrid.getIntregn());

            if (txMadrid.getTranTyp().equals("CPN")) {
                if (listTxReception.size() > 0) {
                    TxReception txReception = listTxReception.get(0);
                    TxTmGeneral txTmGeneral = txTmGeneralRepository.findByTxReception(txReception);
                    if (txTmGeneral != null) {
                        List<KeyValue> criteriaList = new ArrayList<>();
                        criteriaList.add(new KeyValue("txTmGeneral.applicationNo", txTmGeneral.getApplicationNo(), true));
                        criteriaList.add(new KeyValue("status", true, true));
                        List<TxTmOwner> currentOwner = txTmOwnerCustomRepository.selectAll(criteriaList, null, null, 0, 100);

                        Element corrgr = element.getChild("CORRGR");

                        IteratorIterable<Element> ownerIterable = element.getDescendants(new ElementFilter("HOLGR"));
                        int ownerCounter = 0;
                        TxTmOwner txTmOwner = null;
                        while (ownerIterable.hasNext()) {
                            Element owner = ownerIterable.next();
                            String ownerClid = owner.getAttributeValue("CLID") + "-MADRID";

                            if (ownerCounter == 0) {
                                Optional<TxTmOwner> sameOwner = currentOwner.stream().filter(x -> ownerClid.equals(x.getNo())).findFirst();
                                if (sameOwner.isPresent()) {
                                    break;
                                } else {
                                    txTmGeneral.getTxTmOwner().forEach(x -> {
                                        x.getTxTmOwnerDetails().forEach(detail -> {
                                            detail.setStatus(true);
                                            txTmOwnerDetailCustomRepository.saveOrUpdate(detail);
                                        });
                                        x.setStatus(true);
                                        txTmOwnerCustomRepository.saveOrUpdate(x);
                                    });
                                }
                            }

                            ownerCounter++;
                            if (ownerCounter <= 1) {
                                txTmOwner = new TxTmOwner();
                                txTmOwner.setName(getNestedMultipleElementValue(owner, "NAME|NAMEL"));
                                txTmOwner.setOwnerType(getNestedElementValue(owner, "LEGNATU|LEGNATT"));
                                txTmOwner.setAddress(getNestedMultipleElementValue(owner, "ADDRESS|ADDRL"));
                                MCountry mco = lookupCountry.get(getNestedElementValue(owner, "ADDRESS|COUNTRY"));
                                if (mco != null) {
                                    txTmOwner.setmCountry(mco);
                                }
                                MCountry nationality = lookupCountry.get(getNestedElementValue(owner, "NATLTY"));
                                if (nationality == null) {
                                    nationality = lookupCountry.get(getNestedElementValue(owner, "ENTNATL"));
                                    if (nationality == null) {
                                        nationality = lookupCountry.get(getNestedElementValue(owner, "ENTEST"));
                                    }
                                }
                                txTmOwner.setNationality(nationality);
                                txTmOwner.setNo(ownerClid);
                                txTmOwner.setTxTmGeneral(txTmGeneral);
                                txTmOwner.setCreatedBy(txMadrid.getCreatedBy());
                                txTmOwner.setUpdatedBy(txMadrid.getCreatedBy());
                                txTmOwner.setXmlFileId(txMadrid.getXmlFileId());
                                txTmOwner.setSkipLogAuditTrail(false);
                                txTmOwner.setStatus(true);

                                if (corrgr != null) {
                                    txTmOwner.setPostAddress(getNestedMultipleElementValue(corrgr, "ADDRESS|ADDRL"));
                                    MCountry postCountry = lookupCountry.get(getNestedElementValue(corrgr, "ADDRESS|COUNTRY"));
                                    if (postCountry != null) {
                                        txTmOwner.setPostCountry(postCountry);
                                    }
                                }

                                txTmOwnerCustomRepository.saveOrUpdate(txTmOwner);
                            } else {
                                TxTmOwnerDetail txTmOwnerDetail = new TxTmOwnerDetail();
                                txTmOwnerDetail.setName(getNestedMultipleElementValue(owner, "NAME|NAMEL"));
                                txTmOwnerDetail.setStatus(true);
                                txTmOwnerDetail.setTxTmGeneral(txTmGeneral);
                                txTmOwnerDetail.setTxTmOwner(txTmOwner);
                                txTmOwnerDetail.setXmlFileId(txMadrid.getXmlFileId());
                                txTmOwnerDetail.setSkipLogAuditTrail(false);
                                txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
                            }
                        }

                        Element repgr = element.getChild("REPGR");
                        if (repgr != null) {
                            String clid = repgr.getAttributeValue("CLID");
                            String clidMadrid = clid + "-MADRID";
                            MRepresentative mRepresentative = mRepresentativeRepository.findOne(clidMadrid);
                            if (mRepresentative == null) {
                                mRepresentative = new MRepresentative();
                                mRepresentative.setId(clidMadrid);
                                mRepresentative.setNo(clid);
                                mRepresentative.setName(getNestedMultipleElementValue(repgr, "NAME|NAMEL"));
                                mRepresentative.setAddress(getNestedMultipleElementValue(repgr, "ADDRESS|ADDRL"));
                                MCountry mco = lookupCountry.get(getNestedElementValue(repgr, "ADDRESS|COUNTRY"));
                                if (mco != null) {
                                    mRepresentative.setmCountry(mco);
                                }
                                mRepresentative.setStatusFlag(false);
                                mRepresentative.setCreatedBy(txMadrid.getCreatedBy());
                                mRepresentative.setUpdatedBy(txMadrid.getCreatedBy());
                                mRepresentative.setSkipLogAuditTrail(false);
                                mRepresentativeCustomRepository.saveOrUpdate(mRepresentative);
                            }

                            criteriaList = new ArrayList<>();
                            criteriaList.add(new KeyValue("mRepresentative.id", clidMadrid, true));
                            criteriaList.add(new KeyValue("txTmGeneral.applicationNo", txTmGeneral.getApplicationNo(), true));
                            TxTmRepresentative current = txTmReprsCustomRepository.selectOne(criteriaList, null, null);

                            if (current == null) {
                                txTmGeneral.getTxTmRepresentative().forEach(txTmRepresentative -> {
                                    txTmRepresentative.setStatus(false);
                                    txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
                                });

                                TxTmRepresentative txTmRepresentative = new TxTmRepresentative();
                                txTmRepresentative.setTxTmGeneral(txTmGeneral);
                                txTmRepresentative.setmRepresentative(mRepresentative);
                                txTmRepresentative.setName(mRepresentative.getName());
                                txTmRepresentative.setAddress(mRepresentative.getAddress());
                                txTmRepresentative.setmCountry(mRepresentative.getmCountry());
                                txTmRepresentative.setStatus(false);
                                txTmRepresentative.setCreatedBy(txMadrid.getCreatedBy());
                                txTmRepresentative.setUpdatedBy(txMadrid.getCreatedBy());
                                txTmRepresentative.setXmlFileId(txMadrid.getXmlFileId());
                                txTmRepresentative.setSkipLogAuditTrail(false);
                                txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
                            }
                        }

                        return;
                    }
                }
            }

            // CASE EXN
            else if (txMadrid.getTranTyp().equals("EXN")) {
                if (listTxReception.size() > 0) {
                    txMadrid.setIntregn(txMadrid.getIntregn() + "-" + listTxReception.size() + 1);
                }
            }

            TxReception txReception = new TxReception();
            txReception.setCreatedBy(currentUser);
            txReception.setXmlFileId(txMadrid.getXmlFileId());
            txReception.setSkipLogAuditTrail(true);
            txReception.setmWorkflow(wfMadrid);
            txReception.setNotesIpas(txMadrid.getNo());

            if (element.getAttribute("INTREGN") != null) {
                txReception.setBankCode(txMadrid.getIntregn());
                txReception.seteFilingNo(txMadrid.getIntregn());
                txReception.setApplicationNo("M00".concat(String.valueOf(DateUtil.currentYear())).concat(txMadrid.getIntregn()));
            }

            if (element.getAttribute("NOTDATE") != null) {
                txReception.setApplicationDate(DateUtil.toDate("yyyyMMdd", element.getAttributeValue("NOTDATE")));
                txReception.setPaymentDate(DateUtil.toDate("yyyyMMdd", element.getAttributeValue("INTREGD")));
            } else if (element.getAttribute("REGRDAT") != null) {
                txReception.setApplicationDate(DateUtil.toDate("yyyyMMdd", element.getAttributeValue("REGRDAT")));
                txReception.setPaymentDate(DateUtil.toDate("yyyyMMdd", element.getAttributeValue("INTREGD")));
            }

            if (element.getAttribute("OOCD") != null) {
                txReception.setmFileSequence(lookupSequence.get(element.getAttributeValue("OOCD")));
                txReception.setmFileType(lookupFileType.get(element.getName()));
                if (!"ID".equalsIgnoreCase(element.getAttribute("OOCD").getValue())) {
                    txReception.setmFileTypeDetail(fileTypeDetailMD);
                } else {
                    txReception.setmFileTypeDetail(fileTypeDetailMO);
                }
            } else {
                txReception.setmFileSequence(lookupSequence.get("WO"));
                txReception.setmFileType(lookupFileType.get(element.getName()));
                txReception.setmFileTypeDetail(fileTypeDetailMD);
            }

            txReception.setTotalPayment(new BigDecimal(0));

            txMadridCustomRepository.saveOrUpdate(txMadrid);
            txReceptionCustomRepository.saveOrUpdate(txReception);

            // Table TX_TM_GENERAL
            TxTmGeneral txTmGeneral = new TxTmGeneral();
            txTmGeneral.setMadrid_id(txMadrid);
            txTmGeneral.setTxReception(txReception);
            txTmGeneral.setApplicationNo(txReception.getApplicationNo());
            if (element.getAttribute("REGEDAT") != null) {
                txTmGeneral.setFilingDate(DateUtil.toDate("yyyyMMdd", element.getAttributeValue("REGEDAT")));
            }
            txTmGeneral.setmStatus(StatusEnum.M_NEW_APPLICATION.value());
            txTmGeneral.setCreatedBy(txReception.getCreatedBy());
            txTmGeneral.setUpdatedBy(txReception.getCreatedBy());
            txTmGeneral.setmLaw(mLaw);
            txTmGeneral.setXmlFileId(txMadrid.getXmlFileId());
            txTmGeneral.setSkipLogAuditTrail(false);
            txTmGeneral.setIrn(txReception.getBankCode());
            txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);

            insertTxTmDoc(txMadrid, txTmGeneral, (List<PdfFile>) listLookup.get("listPdf"), (MDocType) listLookup.get("txTmDocMDocType"));

            // TABLE TX_MONITOR
            if (mWorkflowProcess != null) {
                TxMonitor txMonitor = new TxMonitor();
                txMonitor.setTxTmGeneral(txTmGeneral);
                txMonitor.setCreatedBy(txReception.getCreatedBy());
                txMonitor.setmWorkflowProcess(mWorkflowProcess);
                txMonitorRepository.save(txMonitor);
            }

            // TABLE TX_TM OWNER dan TX_TM OWNER_DETAIL
            Element corrgr = element.getChild("CORRGR");

            IteratorIterable<Element> ownerIterable = element.getDescendants(new ElementFilter("HOLGR"));
            int ownerCounter = 0;
            TxTmOwner txTmOwner = null;
            while (ownerIterable.hasNext()) {
                Element owner = ownerIterable.next();
                String ownerClid = owner.getAttributeValue("CLID") + "-MADRID";
                ownerCounter++;
                if (ownerCounter <= 1) {
                    txTmOwner = new TxTmOwner();
                    txTmOwner.setName(getNestedMultipleElementValue(owner, "NAME|NAMEL"));
                    txTmOwner.setOwnerType(getNestedElementValue(owner, "LEGNATU|LEGNATT"));
                    txTmOwner.setAddress(getNestedMultipleElementValue(owner, "ADDRESS|ADDRL"));
                    MCountry mco = lookupCountry.get(getNestedElementValue(owner, "ADDRESS|COUNTRY"));
                    if (mco != null) {
                        txTmOwner.setmCountry(mco);
                    }
                    MCountry nationality = lookupCountry.get(getNestedElementValue(owner, "NATLTY"));
                    if (nationality == null) {
                        nationality = lookupCountry.get(getNestedElementValue(owner, "ENTNATL"));
                        if (nationality == null) {
                            nationality = lookupCountry.get(getNestedElementValue(owner, "ENTEST"));
                        }
                    }
                    txTmOwner.setNationality(nationality);
                    txTmOwner.setNo(ownerClid);
                    txTmOwner.setTxTmGeneral(txTmGeneral);
                    txTmOwner.setCreatedBy(txReception.getCreatedBy());
                    txTmOwner.setUpdatedBy(txReception.getCreatedBy());
                    txTmOwner.setXmlFileId(txMadrid.getXmlFileId());
                    txTmOwner.setSkipLogAuditTrail(false);
                    txTmOwner.setStatus(true);

                    if (corrgr != null) {
                        txTmOwner.setPostAddress(getNestedMultipleElementValue(corrgr, "ADDRESS|ADDRL"));
                        MCountry postCountry = lookupCountry.get(getNestedElementValue(corrgr, "ADDRESS|COUNTRY"));
                        if (postCountry != null) {
                            txTmOwner.setPostCountry(postCountry);
                        }
                    }

                    txTmOwnerCustomRepository.saveOrUpdate(txTmOwner);
                } else {
                    TxTmOwnerDetail txTmOwnerDetail = new TxTmOwnerDetail();
                    txTmOwnerDetail.setName(getNestedMultipleElementValue(owner, "NAME|NAMEL"));
                    txTmOwnerDetail.setStatus(true);
                    txTmOwnerDetail.setTxTmGeneral(txTmGeneral);
                    txTmOwnerDetail.setTxTmOwner(txTmOwner);
                    txTmOwnerDetail.setXmlFileId(txMadrid.getXmlFileId());
                    txTmOwnerDetail.setSkipLogAuditTrail(false);
                    txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
                }
            }

            Element repgr = element.getChild("REPGR");
            if (repgr != null) {
                String clid = repgr.getAttributeValue("CLID");
                String clidMadrid = clid + "-MADRID";
                MRepresentative mRepresentative = mRepresentativeRepository.findOne(clidMadrid);
                if (mRepresentative == null) {
                    mRepresentative = new MRepresentative();
                    mRepresentative.setId(clidMadrid);
                    mRepresentative.setNo(clid);
                    mRepresentative.setName(getNestedMultipleElementValue(repgr, "NAME|NAMEL"));
                    mRepresentative.setAddress(getNestedMultipleElementValue(repgr, "ADDRESS|ADDRL"));
                    MCountry mco = lookupCountry.get(getNestedElementValue(repgr, "ADDRESS|COUNTRY"));
                    if (mco != null) {
                        mRepresentative.setmCountry(mco);
                    }
                    mRepresentative.setStatusFlag(false);
                    mRepresentative.setCreatedBy(txMadrid.getCreatedBy());
                    mRepresentative.setUpdatedBy(txMadrid.getCreatedBy());
                    mRepresentative.setSkipLogAuditTrail(false);
                    mRepresentativeCustomRepository.saveOrUpdate(mRepresentative);
                }

                TxTmRepresentative txTmRepresentative = new TxTmRepresentative();
                txTmRepresentative.setTxTmGeneral(txTmGeneral);
                txTmRepresentative.setmRepresentative(mRepresentative);
                txTmRepresentative.setName(mRepresentative.getName());
                txTmRepresentative.setAddress(mRepresentative.getAddress());
                txTmRepresentative.setmCountry(mRepresentative.getmCountry());
                txTmRepresentative.setStatus(false);
                txTmRepresentative.setCreatedBy(txMadrid.getCreatedBy());
                txTmRepresentative.setUpdatedBy(txMadrid.getCreatedBy());
                txTmRepresentative.setXmlFileId(txMadrid.getXmlFileId());
                txTmRepresentative.setSkipLogAuditTrail(false);
                txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
            }

            // TABLE TX_TM_BRAND
            //brand, default is MEREK_LUKISAN
            String xmlString = element.toString();
            MBrandType mBrandType = null;
            if ("SIGVRBL".contains(xmlString)) {
                mBrandType = brandTypeSigvrbl;
            } else if ("MARKVE".contains(xmlString)) {
                mBrandType = brandTypeMarkve;
            } else if ("SOUMARI".contains(xmlString)) {
                mBrandType = brandTypeSoumari;
            } else if ("THRDMAR".contains(xmlString)) {
                mBrandType = brandTypeThrdmar;
            } else {
                mBrandType = brandDefault;
            }

            TxTmBrand txTmBrand = new TxTmBrand();
            txTmBrand.setTxTmGeneral(txTmGeneral);
            txTmBrand.setmBrandType(mBrandType);

            Element image = element.getChild("IMAGE");
            if (image != null) {
                Attribute text = image.getAttribute("TEXT");
                if (text != null) {
                    txTmBrand.setName(text.getValue());
                }

                txTmBrand.setFileName(image.getAttributeValue("NAME") + "." + image.getAttributeValue("TYPE"));
            }

            txTmBrand.setDescription(getNestedElementValue(element, "MARDESGR|MARDESEN"));
            txTmBrand.setColor(getNestedElementValue(element, "COLCLAGR|COLCLAEN"));
            txTmBrand.setDisclaimer(getNestedElementValue(element, "DISCLAIMGR|DISCLAIMEREN"));
            txTmBrand.setTranslation(getNestedElementValue(element, "MARTRGR|MARTREN"));
            txTmBrand.setDescChar(getNestedElementValue(element, "MARTRAN"));
            txTmBrand.setCreatedBy(txReception.getCreatedBy());
            txTmBrand.setUpdatedBy(txReception.getCreatedBy());
            txTmBrand.setXmlFileId(txMadrid.getXmlFileId());
            txTmBrand.setSkipLogAuditTrail(false);
            txTmBrandCustomRepository.saveOrUpdate(txTmBrand);

            // TABLE TX_TM_PRIOR
            IteratorIterable<Element> priorIterable = element.getDescendants(new ElementFilter("PRIGR"));
            while (priorIterable.hasNext()) {
                Element prior = priorIterable.next();

                TxTmPrior txTmPrior = new TxTmPrior();
                txTmPrior.setTxTmGeneral(txTmGeneral);
                txTmPrior.setNo(getNestedElementValue(prior, "PRIAPPN"));
                try {
                    SimpleDateFormat priorFormat = new SimpleDateFormat("yyyyMMdd");
                    Timestamp priorDate = new Timestamp(priorFormat.parse(getNestedElementValue(prior, "PRIAPPD")).getTime());
                    txTmPrior.setPriorDate(priorDate);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                MCountry mco = lookupCountry.get(getNestedElementValue(prior, "PRICP"));
                if (mco != null) {
                    txTmPrior.setmCountry(mco);
                }
                txTmPrior.setStatus("PENDING");
                txTmPrior.setCreatedBy(txReception.getCreatedBy());
                txTmPrior.setUpdatedBy(txReception.getCreatedBy());
                txTmPrior.setXmlFileId(txMadrid.getXmlFileId());
                txTmPrior.setSkipLogAuditTrail(false);
                txTmPriorCustomRepository.saveOrUpdate(txTmPrior);
            }

            // Limitasi Kelas
            Map<Integer, Boolean> listLimit = new HashMap<>();

            Element lio = element.getChild("LIO");
            if (lio != null) {
                IteratorIterable<Element> limtoIterable = lio.getDescendants(new ElementFilter("LIMTO"));
                while (limtoIterable.hasNext()) {
                    Element limto = limtoIterable.next();
                    listLimit.put(NumberUtil.tryParseInt(limto.getAttributeValue("NICCLAI")), true);
                }

                Element gsheaden = lio.getChild("GSHEADEN");
                if (gsheaden != null) {
                    String text = gsheaden.getTextNormalize();
                    for (String kata : text.split(" ")) {
                        kata = kata.trim().replace(",", "");
                        kata = kata.trim().replace(".", "");
                        if (NumberUtil.tryParseInt(kata) != null) {
                            int kelas = NumberUtil.tryParseInt(kata);
                            listLimit.put(kelas, true);
                        }
                    }
                }
            }

            Element limgr = element.getChild("LIMGR");
            if (limgr != null) {
                IteratorIterable<Element> limtoIterable = limgr.getDescendants(new ElementFilter("LIMTO"));
                while (limtoIterable.hasNext()) {
                    Element limto = limtoIterable.next();
                    listLimit.put(NumberUtil.tryParseInt(limto.getAttributeValue("NICCLAI")), true);
                }

                Element gsheaden = limgr.getChild("GSHEADEN");
                if (gsheaden != null) {
                    String text = gsheaden.getTextNormalize();
                    for (String kata : text.split(" ")) {
                        kata = kata.trim().replace(",", "");
                        kata = kata.trim().replace(".", "");
                        if (NumberUtil.tryParseInt(kata) != null) {
                            int kelas = NumberUtil.tryParseInt(kata);
                            listLimit.put(kelas, true);
                        }
                    }
                }
            }

            //mapping class
            int classCount = 0;
            IteratorIterable<Element> classIterable = element.getDescendants(new ElementFilter("BASICGS"));
            while (classIterable.hasNext()) {
                Element kelas = classIterable.next();
                for (Element gsgr : kelas.getDescendants(new ElementFilter("GSGR"))) {
                    String nicclai = gsgr.getAttributeValue("NICCLAI");
                    if (listLimit.size() > 0) {
                        if (!listLimit.containsKey(NumberUtil.tryParseInt(nicclai))) {
                            continue;
                        }
                    }

                    MClass mClass = lookupClassHeader.get(NumberUtil.tryParseInt(nicclai));
                    classCount++;
                    for (Element gstermen : gsgr.getDescendants(new ElementFilter("GSTERMEN"))) {
                        String value = gstermen.getTextNormalize();

                        String[] arrDesc = value.split("\\;");

                        for (String desc : arrDesc) {
                            if (desc.length() > 4000) {
                                desc = desc.substring(0, 3999);
                            }

                            MClassDetail mClassDetail = new MClassDetail();
                            mClassDetail.setParentClass(mClass);
                            mClassDetail.setDescEn(desc.trim());
                            mClassDetail.setStatusFlag(false);
                            mClassDetail.setSerial2("Dari Madrid DCP");
                            mClassDetail.setNotes("Menunggu untuk diterjemahkan");
                            mClassDetail.setCreatedBy(txReception.getCreatedBy());
                            mClassDetail.setUpdatedBy(currentUser);
                            mClassDetail.setSkipLogAuditTrail(false);
                            mClassDetail.setTranslationFlag(false);
                            mClassDetailCustomRepository.saveOrUpdate(mClassDetail);

                            TxTmClass txTmClass = new TxTmClass();
                            txTmClass.setTxTmGeneral(txTmGeneral);
                            txTmClass.setmClass(mClass);
                            txTmClass.setmClassDetail(mClassDetail);
                            txTmClass.setCreatedBy(txReception.getCreatedBy());
                            txTmClass.setUpdatedBy(txReception.getCreatedBy());
                            txTmClass.setTransactionStatus("ACCEPT");
                            txTmClass.setCorrectionFlag(false);
                            txTmClass.setXmlFileId(txReception.getXmlFileId());
                            txTmClass.setSkipLogAuditTrail(false);
                            txTmClassCustomRepository.saveOrUpdate(txTmClass);
                        }
                    }
                }
            }

            txReception.setTotalClass(classCount);
            txReceptionCustomRepository.saveOrUpdate(txReception);
        } catch (ParseException pe) {
            pe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void insertDeath(TxMadrid txMadrid, Element element, Map<String, Object> listLookup) {
        if (alreadyInsertTxMadrid(txMadrid)) {
            return;
        }

        txMadridCustomRepository.saveOrUpdate(txMadrid);

        List<TxReception> listTxReception = txReceptionRepository.findByBankCode(txMadrid.getIntregn());
        if (listTxReception.size() > 0) {
            for (TxReception txReception: listTxReception) {
                TxTmGeneral txTmGeneral = txTmGeneralRepository.findByTxReception(txReception);
                if (txTmGeneral != null) {
                    insertTxTmDoc(txMadrid, txTmGeneral, (List<PdfFile>) listLookup.get("listPdf"), (MDocType) listLookup.get("txTmDocMDocType"));
                }
            }
        }
    }

    @Transactional
    public void insertNewname(TxMadrid txMadrid, Element element, Map<String, Object> listLookup) {
        if (alreadyInsertTxMadrid(txMadrid)) {
            return;
        }

        txMadridCustomRepository.saveOrUpdate(txMadrid);

        List<TxReception> listTxReception = txReceptionRepository.findByBankCode(txMadrid.getIntregn());
        if (listTxReception.size() > 0) {
            for (TxReception txReception: listTxReception) {
                TxTmGeneral txTmGeneral = txTmGeneralRepository.findByTxReception(txReception);
                if (txTmGeneral != null) {
                    insertTxTmDoc(txMadrid, txTmGeneral, (List<PdfFile>) listLookup.get("listPdf"), (MDocType) listLookup.get("txTmDocMDocType"));
                    Map<String, MCountry> lookupCountry = (Map<String, MCountry>) listLookup.get("lookupCountry");

                    List<KeyValue> criteriaList = new ArrayList<>();
                    criteriaList.add(new KeyValue("txTmGeneral.applicationNo", txTmGeneral.getApplicationNo(), true));
                    criteriaList.add(new KeyValue("status", true, true));
                    List<TxTmOwner> currentOwner = txTmOwnerCustomRepository.selectAll(criteriaList, null, null, 0, 100);

                    Element corrgr = element.getChild("CORRGR");

                    IteratorIterable<Element> ownerIterable = element.getDescendants(new ElementFilter("HOLGR"));
                    int ownerCounter = 0;
                    TxTmOwner txTmOwner = null;
                    while (ownerIterable.hasNext()) {
                        Element owner = ownerIterable.next();
                        String ownerClid = owner.getAttributeValue("CLID") + "-MADRID";

                        if (ownerCounter == 0) {
                            Optional<TxTmOwner> sameOwner = currentOwner.stream().filter(x -> ownerClid.equals(x.getNo())).findFirst();
                            if (sameOwner.isPresent()) {
                                break;
                            } else {
                                txTmGeneral.getTxTmOwner().forEach(x -> {
                                    x.getTxTmOwnerDetails().forEach(detail -> {
                                        detail.setStatus(true);
                                        txTmOwnerDetailCustomRepository.saveOrUpdate(detail);
                                    });
                                    x.setStatus(true);
                                    txTmOwnerCustomRepository.saveOrUpdate(x);
                                });
                            }
                        }

                        ownerCounter++;
                        if (ownerCounter <= 1) {
                            txTmOwner = new TxTmOwner();
                            txTmOwner.setName(getNestedMultipleElementValue(owner, "NAME|NAMEL"));
                            txTmOwner.setOwnerType(getNestedElementValue(owner, "LEGNATU|LEGNATT"));
                            txTmOwner.setAddress(getNestedMultipleElementValue(owner, "ADDRESS|ADDRL"));
                            MCountry mco = lookupCountry.get(getNestedElementValue(owner, "ADDRESS|COUNTRY"));
                            if (mco != null) {
                                txTmOwner.setmCountry(mco);
                            }
                            MCountry nationality = lookupCountry.get(getNestedElementValue(owner, "NATLTY"));
                            if (nationality == null) {
                                nationality = lookupCountry.get(getNestedElementValue(owner, "ENTNATL"));
                                if (nationality == null) {
                                    nationality = lookupCountry.get(getNestedElementValue(owner, "ENTEST"));
                                }
                            }
                            txTmOwner.setNationality(nationality);
                            txTmOwner.setNo(ownerClid);
                            txTmOwner.setTxTmGeneral(txTmGeneral);
                            txTmOwner.setCreatedBy(txMadrid.getCreatedBy());
                            txTmOwner.setUpdatedBy(txMadrid.getCreatedBy());
                            txTmOwner.setXmlFileId(txMadrid.getXmlFileId());
                            txTmOwner.setSkipLogAuditTrail(false);
                            txTmOwner.setStatus(true);

                            if (corrgr != null) {
                                txTmOwner.setPostAddress(getNestedMultipleElementValue(corrgr, "ADDRESS|ADDRL"));
                                MCountry postCountry = lookupCountry.get(getNestedElementValue(corrgr, "ADDRESS|COUNTRY"));
                                if (postCountry != null) {
                                    txTmOwner.setPostCountry(postCountry);
                                }
                            }

                            txTmOwnerCustomRepository.saveOrUpdate(txTmOwner);
                        } else {
                            TxTmOwnerDetail txTmOwnerDetail = new TxTmOwnerDetail();
                            txTmOwnerDetail.setName(getNestedMultipleElementValue(owner, "NAME|NAMEL"));
                            txTmOwnerDetail.setStatus(true);
                            txTmOwnerDetail.setTxTmGeneral(txTmGeneral);
                            txTmOwnerDetail.setTxTmOwner(txTmOwner);
                            txTmOwnerDetail.setXmlFileId(txMadrid.getXmlFileId());
                            txTmOwnerDetail.setSkipLogAuditTrail(false);
                            txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
                        }
                    }

                    Element repgr = element.getChild("REPGR");
                    if (repgr != null) {
                        String clid = repgr.getAttributeValue("CLID");
                        String clidMadrid = clid + "-MADRID";
                        MRepresentative mRepresentative = mRepresentativeRepository.findOne(clidMadrid);
                        if (mRepresentative == null) {
                            mRepresentative = new MRepresentative();
                            mRepresentative.setId(clidMadrid);
                            mRepresentative.setNo(clid);
                            mRepresentative.setName(getNestedMultipleElementValue(repgr, "NAME|NAMEL"));
                            mRepresentative.setAddress(getNestedMultipleElementValue(repgr, "ADDRESS|ADDRL"));
                            MCountry mco = lookupCountry.get(getNestedElementValue(repgr, "ADDRESS|COUNTRY"));
                            if (mco != null) {
                                mRepresentative.setmCountry(mco);
                            }
                            mRepresentative.setStatusFlag(false);
                            mRepresentative.setCreatedBy(txMadrid.getCreatedBy());
                            mRepresentative.setUpdatedBy(txMadrid.getCreatedBy());
                            mRepresentative.setSkipLogAuditTrail(false);
                            mRepresentativeCustomRepository.saveOrUpdate(mRepresentative);
                        }

                        criteriaList = new ArrayList<>();
                        criteriaList.add(new KeyValue("mRepresentative.id", clidMadrid, true));
                        criteriaList.add(new KeyValue("txTmGeneral.applicationNo", txTmGeneral.getApplicationNo(), true));
                        TxTmRepresentative current = txTmReprsCustomRepository.selectOne(criteriaList, null, null);

                        if (current == null) {
                            txTmGeneral.getTxTmRepresentative().forEach(txTmRepresentative -> {
                                txTmRepresentative.setStatus(false);
                                txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
                            });

                            TxTmRepresentative txTmRepresentative = new TxTmRepresentative();
                            txTmRepresentative.setTxTmGeneral(txTmGeneral);
                            txTmRepresentative.setmRepresentative(mRepresentative);
                            txTmRepresentative.setName(mRepresentative.getName());
                            txTmRepresentative.setAddress(mRepresentative.getAddress());
                            txTmRepresentative.setmCountry(mRepresentative.getmCountry());
                            txTmRepresentative.setStatus(false);
                            txTmRepresentative.setCreatedBy(txMadrid.getCreatedBy());
                            txTmRepresentative.setUpdatedBy(txMadrid.getCreatedBy());
                            txTmRepresentative.setXmlFileId(txMadrid.getXmlFileId());
                            txTmRepresentative.setSkipLogAuditTrail(false);
                            txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
                        }
                    }
                }
            }
        }
    }

    @Transactional
    public void insertRestrict(TxMadrid txMadrid, Element element, Map<String, Object> listLookup) {
        if (alreadyInsertTxMadrid(txMadrid)) {
            return;
        }

        txMadridCustomRepository.saveOrUpdate(txMadrid);

        List<TxReception> listTxReception = txReceptionRepository.findByBankCode(txMadrid.getIntregn());
        if (listTxReception.size() > 0) {
            for (TxReception txReception: listTxReception) {
                TxTmGeneral txTmGeneral = txTmGeneralRepository.findByTxReception(txReception);
                if (txTmGeneral != null) {
                    insertTxTmDoc(txMadrid, txTmGeneral, (List<PdfFile>) listLookup.get("listPdf"), (MDocType) listLookup.get("txTmDocMDocType"));
                }
            }
        }
    }

    @Transactional
    public void insertNewbase(TxMadrid txMadrid, Element element, Map<String, Object> listLookup) {
        if (alreadyInsertTxMadrid(txMadrid)) {
            return;
        }

        txMadridCustomRepository.saveOrUpdate(txMadrid);

        List<TxReception> listTxReception = txReceptionRepository.findByBankCode(txMadrid.getIntregn());
        if (listTxReception.size() > 0) {
            for (TxReception txReception: listTxReception) {
                TxTmGeneral txTmGeneral = txTmGeneralRepository.findByTxReception(txReception);
                if (txTmGeneral != null) {
                    insertTxTmDoc(txMadrid, txTmGeneral, (List<PdfFile>) listLookup.get("listPdf"), (MDocType) listLookup.get("txTmDocMDocType"));
                }
            }
        }
    }

    @Transactional
    public void insertProlong(TxMadrid txMadrid, Element element, Map<String, Object> listLookup) {
        if (alreadyInsertTxMadrid(txMadrid)) {
            return;
        }

        txMadridCustomRepository.saveOrUpdate(txMadrid);

        List<TxReception> listTxReception = txReceptionRepository.findByBankCode(txMadrid.getIntregn());
        if (listTxReception.size() > 0) {
            for (TxReception txReception: listTxReception) {
                TxTmGeneral txTmGeneral = txTmGeneralRepository.findByTxReception(txReception);
                if (txTmGeneral != null) {
                    TxRegistration txRegistration = txRegistrationRepository.findByTxTmGeneral(txTmGeneral);
                    if (txRegistration == null) {
                        txRegistration = new TxRegistration();
                        txRegistration.setTxTmGeneral(txTmGeneral);
                        txRegistration.setCreatedBy(txMadrid.getCreatedBy());
                        txRegistration.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                        try {
                            Date start = new SimpleDateFormat("yyyyMMdd").parse(element.getAttributeValue("REGRDAT"));
                            txRegistration.setStartDate(new Timestamp(start.getTime()));
                        } catch (Exception e) {

                        }

                        try {
                            Date expired = new SimpleDateFormat("yyyyMMdd").parse(element.getAttributeValue("EXPDATE"));
                            txRegistration.setEndDate(new Timestamp(expired.getTime()));
                        } catch (Exception e) {

                        }

                        txRegistration.setDownloadFlag(false);
                        txRegistration.setStatus(true);
                        txRegistration.setNo(txMadrid.getIntregn());
                        txRegistration.setSkipLogAuditTrail(false);
                    }

                    try {
                        Date start = new SimpleDateFormat("yyyyMMdd").parse(element.getAttributeValue("REGRDAT"));
                        txRegistration.setStartDate(new Timestamp(start.getTime()));
                    } catch (Exception e) {

                    }

                    try {
                        Date expired = new SimpleDateFormat("yyyyMMdd").parse(element.getAttributeValue("EXPDATE"));
                        txRegistration.setEndDate(new Timestamp(expired.getTime()));
                    } catch (Exception e) {

                    }
                    txRegistration.setUpdatedBy(txMadrid.getCreatedBy());
                    txRegistration.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
                    txRegistrationRepository.save(txRegistration);

                    TxRegistrationDetail txRegistrationDetail = new TxRegistrationDetail();
                    txRegistrationDetail.setTxRegistration(txRegistration);
                    txRegistrationDetail.setStartDate(txRegistration.getStartDate());
                    txRegistrationDetail.setEndDate(txRegistration.getEndDate());
                    txRegistrationDetail.setDownloadFlag(false);
                    txRegistrationDetail.setStatus(true);
                    txRegistrationDetail.setCreatedBy(txMadrid.getCreatedBy());
                    txRegistrationDetail.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    txRegistrationDetail.setSkipLogAuditTrail(false);
                    txRegistrationDetailRepository.save(txRegistrationDetail);
                }
            }
        }
    }

    @Transactional
    public void insertCorrection(TxMadrid txMadrid, Element element, Map<String, Object> listLookup) {
        if (alreadyInsertTxMadrid(txMadrid)) {
            return;
        }

        txMadridCustomRepository.saveOrUpdate(txMadrid);

        List<TxReception> listTxReception = txReceptionRepository.findByBankCode(txMadrid.getIntregn());
        if (listTxReception.size() > 0) {
            for (TxReception txReception: listTxReception) {
                TxTmGeneral txTmGeneral = txTmGeneralRepository.findByTxReception(txReception);
                if (txTmGeneral != null) {
                    insertTxTmDoc(txMadrid, txTmGeneral, (List<PdfFile>) listLookup.get("listPdf"), (MDocType) listLookup.get("txTmDocMDocType"));
                }
            }
        }
    }

    @Transactional
    public void insertCreated(TxMadrid txMadrid, Element element, Map<String, Object> listLookup) {
        if (alreadyInsertTxMadrid(txMadrid)) {
            return;
        }

        txMadridCustomRepository.saveOrUpdate(txMadrid);

        List<TxReception> listTxReception = txReceptionRepository.findByBankCode(txMadrid.getIntregn());
        if (listTxReception.size() > 0) {
            for (TxReception txReception: listTxReception) {
                TxTmGeneral txTmGeneral = txTmGeneralRepository.findByTxReception(txReception);
                if (txTmGeneral != null) {
                    //System.out.println("YANG DOUBLE "+txReception.getId());
                    insertTxTmDoc(txMadrid, txTmGeneral, (List<PdfFile>) listLookup.get("listPdf"), (MDocType) listLookup.get("txTmDocMDocType"));
                }
            }
        }
    }

    @Transactional
    public void insertProcessed(TxMadrid txMadrid, Element element) {
        if (alreadyInsertTxMadrid(txMadrid)) {
            return;
        }

        txMadridCustomRepository.saveOrUpdate(txMadrid);
    }

    private String getNestedElementValue(Element element, String filter) {
        String[] split = filter.split("\\|");
        Element e = element.clone();

        for (int i = 0; i < split.length; i++) {
            e = e.getChild(split[i]);
            if (e == null) {
                return "";
            }

            if (i == split.length - 1) {
                return e.getTextNormalize();
            }

        }

        return "";
    }

    private String getNestedMultipleElementValue(Element element, String filter) {
        String[] split = filter.split("\\|");
        Element e = element.clone();

        for (int i = 0; i < split.length; i++) {
            if (i == split.length - 1) {
                StringBuilder sb = new StringBuilder();

                IteratorIterable<Element> el = e.getDescendants(new ElementFilter(split[i]));
                while (el.hasNext()) {
                    Element line = el.next();
                    sb.append(line.getTextNormalize());
                    sb.append(" ");
                }

                return sb.toString();
            } else {
                e = e.getChild(split[i]);
                if (e == null) {
                    return "";
                }
            }
        }

        return "";
    }

    @Transactional
    public void saveTranslatedClassDesc(MClassDetail mClassDetail) {
        mClassDetailRepository.save(mClassDetail);
    }

    public void prosesCopyMadridPDF(PDFFile file) throws IOException {
        String fileName = file.getFileName().substring(0, file.getFileName().lastIndexOf(".")).toUpperCase();
        File files = new File(wipoMadridExtractLocationPdf + file.getModifiedYear() + File.separator + fileName);
        if (files.exists()) {
            String[] directories = files.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });

            for (String dir : directories) {
                File folderPdf = new File(wipoMadridExtractLocationPdf + file.getModifiedYear() + File.separator + fileName + File.separator + dir);
                if (folderPdf.exists()) {
                    File[] listOfFilesPdf = folderPdf.listFiles();
                    Arrays.stream(listOfFilesPdf).forEach(
                            data -> {
                                if (data.isFile()) {
                                    String[] parts = data.getName().split("-");
                                    if (parts.length == 3 || parts.length == 2) {
                                        List<TxTmDoc> docs = txTmDocCustomRepository.selectAll("fileName", data.getName(), true, 0, 1000);

                                        docs.forEach(doc -> {
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
                                            String pathFolder = sdf.format(new Date(doc.getCreatedDate().getTime()));

                                            String target = uploadFileDocApplicationPath + pathFolder + data.getName();
                                            if (Files.exists(Paths.get(target))) {
                                                return;
                                            }
                                            try {
                                                String pathFrom = UriUtils.decode(data.getPath(), "UTF-8");
                                                String pathTO = UriUtils.decode(target, "UTF-8");

                                                copy(pathFrom, pathTO);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    }
                                }
                            }
                    );
                }
            }

            file.setProcessed(true);
            wipoService.savePdfFile(file);
        }
    }
 
    @Transactional
    public void saveOrUpdate(TxTmReference txTmReference, List<TxTmClass> txTmClasses, TxTmGeneral txtmRefAppId) {

        TxTmReference existing = txTmReferenceCustomRepository.selectOne("txTmGeneral", txTmReference.getTxTmGeneral(), true);
        
        if(txTmClasses != null) {
			List<TxTmClass> txTmClasses2 = classService.findTxTmClassByGeneralId(txtmRefAppId);
			if (txTmClasses2 != null) {
				txTmClassRepository.delete(txTmClasses2);
			}
			List<TxTmClass> txTmClassNew = new ArrayList<TxTmClass>();				
			for (TxTmClass txTmClassx : txTmClasses) {
				TxTmClass txTmClass = new TxTmClass();
				//txTmClass.setId("");
				txTmClass.setTxTmGeneral(txtmRefAppId);
				txTmClass.setmClass(txTmClassx.getmClass());
				txTmClass.setEdition(txTmClassx.getEdition());
				txTmClass.setVersion(txTmClassx.getVersion());
				txTmClass.setmClassDetail(txTmClassx.getmClassDetail());
				txTmClass.setTransactionStatus(txTmClassx.getTransactionStatus());
				txTmClass.setCorrectionFlag(txTmClassx.isCorrectionFlag());
				txTmClass.setXmlFileId(txTmClassx.getXmlFileId());
				txTmClass.setNotes(txTmClassx.getNotes());
				txTmClassNew.add(txTmClass);
			}
			txTmClassRepository.save(txTmClassNew);
		}
        
        if (existing != null) {
            txTmReferenceCustomRepository.delete(existing);
        }
        txTmReferenceCustomRepository.saveOrUpdate(txTmReference);
    }
    
    @Transactional
    public void saveOrUpdate2(TxTmReference txTmReference) {

        TxTmReference existing = txTmReferenceCustomRepository.selectOne("txTmGeneral", txTmReference.getTxTmGeneral(), true);        
        if (existing != null) {
            txTmReferenceCustomRepository.delete(existing);
        }
        txTmReferenceCustomRepository.saveOrUpdate(txTmReference);
    }

    @Transactional
    public void saveAll(List<TxTmReference> txTmReferences){
    	List<TxTmReference> all = txTmReferenceCustomRepository.selectAll("txTmGeneral", txTmReferences.get(0).getTxTmGeneral(), true, null , null);
    	for(TxTmReference current : txTmReferences) { //ex: a b c
    	    boolean exists = false;
            for (TxTmReference ttr : all) { //ex: b c d (+a)
                //check existense
                if(current.getRefApplicationId_2() == ttr.getRefApplicationId_2()){
                    exists = true;
                    break;
                }
            }
            if(!exists){
                txTmReferenceCustomRepository.saveOrUpdate(current); //add new replacemment
            }
        }
        for (TxTmReference ttr : all) { //ex: b c d
            boolean exists = false;
            for(TxTmReference current : txTmReferences) { //ex: a b c (-d)
                //check existense
                if(current.getRefApplicationId_2() == ttr.getRefApplicationId_2()){
                    exists = true;
                    break;
                }
            }
            if(!exists){
                txTmReferenceCustomRepository.delete(ttr); //remove old replacemment
            }
        }
    }
    
    
    
}