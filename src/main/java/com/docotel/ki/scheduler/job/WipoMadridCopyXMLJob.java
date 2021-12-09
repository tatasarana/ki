package com.docotel.ki.scheduler.job;

import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.wipo.PDFFile;
import com.docotel.ki.model.wipo.XMLFile;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.custom.master.MDocTypeCustomRepository;
import com.docotel.ki.repository.custom.master.MWorkflowProcessCustomRepository;
import com.docotel.ki.repository.master.MLawRepository;
import com.docotel.ki.repository.master.MWorkflowRepository;
import com.docotel.ki.service.WipoService;
import com.docotel.ki.service.master.BrandService;
import com.docotel.ki.service.master.FileSeqService;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.MadridService;
import com.docotel.ki.model.transaction.TxMadrid;
import com.docotel.ki.repository.custom.master.MClassDetailCustomRepository;
import com.docotel.ki.repository.custom.master.MUserCustomRepository;
import com.docotel.ki.repository.custom.transaction.MrepresentativeCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxMadridCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxReceptionCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmBrandCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmOwnerCustomRepository;
import com.docotel.ki.repository.master.MClassHeaderRepository;
import com.docotel.ki.repository.master.MCountryRepository;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class WipoMadridCopyXMLJob implements Job {
    private static boolean synchXmlOnProgress = false;

    private static Logger logger = LoggerFactory.getLogger(WipoMadridCopyXMLJob.class);

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private MClassDetailCustomRepository mClassDetailCustomRepository;
    @Autowired
    private MUserCustomRepository mUserCustomRepository;
    @Autowired
    private MrepresentativeCustomRepository mRepresentativeCustomRepository;
    @Autowired
    private MClassHeaderRepository mClassHeaderRepository;
    @Autowired
    private MCountryRepository mCountryRepository;
    @Autowired
    private TxReceptionCustomRepository txReceptionCustomRepository;
    @Autowired
    private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    private TxTmBrandCustomRepository txTmBrandCustomRepository;
    @Autowired
    private TxTmOwnerCustomRepository txTmOwnerCustomRepository;
    @Autowired
    private TxMadridCustomRepository txMadridCustomRepository;
    @Autowired
    private WipoService wipoService;
    @Autowired
    BrandService brandService;
    @Autowired
    FileSeqService fileSeqService;
    @Autowired
    FileService fileService;
    @Autowired
    UserService userService;
    @Autowired
    private MadridService madridService;
    @Autowired
    private MLawRepository mLawRepository;
    @Autowired
    private MWorkflowProcessCustomRepository mWorkflowProcessCustomRepository;
    @Autowired
    private MWorkflowRepository mWorkflowRepository;
    @Autowired
    private MDocTypeCustomRepository mDocTypeCustomRepository;

    @Value("${wipo.madrid.extract.location.xml}")
    private String wipoMadridExtractLocationXml;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (synchXmlOnProgress) {
            logger.info("- PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
        } else {
            synchXmlOnProgress = true;

            logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS DISTRIBUTE XML STARTING -");

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
            MBrandType brandDefault = brandService.selectOne("id", "MEREK_LUKISAN");

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
                    } catch (JDOMException | IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            synchXmlOnProgress = false;
            logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS COPYING DONE -");
        }
    }
}
