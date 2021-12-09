package com.docotel.ki.service.transaction;

import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.PriorStatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.repository.custom.master.MLookupCostumRepository;
import com.docotel.ki.repository.custom.transaction.*;
import com.docotel.ki.repository.master.MClassHeaderRepository;
import com.docotel.ki.repository.master.MDocumentRepository;
import com.docotel.ki.repository.transaction.*;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.util.ParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.jexl2.parser.ParserConstants.ge;

@Service
public class ParamSubtitudeService {

    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;

    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;

    @Autowired
    private MDocumentRepository documentRepository;
    @Autowired
    private TxDocumentParamCustomRepository txDocumentParamCustomRepository;
    @Autowired
    private TxTmReprsRepository txTmReprsRepository;
    @Autowired
    private TxTmReprsCustomRepository txTmReprsCustomRepository;
    @Autowired
    private TxTmOwnerRepository txTmOwnerRepository;
    @Autowired
    private TxTmOwnerCustomRepository txTmOwnerCustomRepository;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private TxGroupDetailRepository txGroupDetailRepository;
    @Autowired
    private TxPubsJournalRepository txPubsJournalRepository;
    @Autowired
    private TxTmClassRepository txTmClassRepository;
    @Autowired
    private TxTmClassCustomRepository txTmClassCustomRepository;
    @Autowired
    private TxTmPriorRepository txTmPriorRepository;
    @Autowired
    private TxTmPriorCustomRepository txTmPriorCustomRepository;
    @Autowired
    private TxPostRepresentativeRepository txPostRepresentativeRepository;
    @Autowired
    private TxPostReceptionRepository txPostReceptionRepository;
    @Autowired
    private TxPostOwnerCustomRepository txPostOwnerCustomRepository;
    @Autowired
    private TxPostReceptionCustomRepository txPostReceptionCustomRepository;
    @Autowired
    private TxPostReceptionDetailRepository txPostReceptionDetailRepository;
    @Autowired
    private TxPostRepresentativeCustomRepository txPostRepresentativeCustomRepository;
    @Autowired
    private TxRegistrationCustomRepository txRegistrationCustomRepository;
    @Autowired
    private MLookupCostumRepository mLookupCostumRepository;
    @Autowired
    private TxTmBrandCustomRepository txTmBrandCustomRepository;
    @Autowired
    private TxLicenseCustomRepository txLicenseCustomRepository;
    @Autowired
    private TxSimilarityResultRepository txSimilarityResultRepository;
    @Autowired
    private TxRegistrationRepository txRegistrationRepository;
    @Autowired
    private TxTmBrandRepository txTmBrandRepository;
    @Autowired
    private MClassHeaderRepository mClassRepository;
    @Autowired
    private ClassService classService;


    private SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");

    private SimpleDateFormat fmtDate = new SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"));

    private SimpleDateFormat fmtDateEn = new SimpleDateFormat("dd MMMM yyyy");

    private SimpleDateFormat fmtYear = new SimpleDateFormat("yyyy");

    private SimpleDateFormat fmtMonth = new SimpleDateFormat("MM");

    public  String getRoman(int number) {

        String riman[] = {"M","XM","CM","D","XD","CD","C","XC","L","XL","X","IX","V","IV","I"};
        int arab[] = {1000, 990, 900, 500, 490, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (number > 0 || arab.length == (i - 1)) {
            while ((number - arab[i]) >= 0) {
                number -= arab[i];
                result.append(riman[i]);
            }
            i++;
        }
        return result.toString();
    }

    public List<TxDocumentParams> subtitude(List<MDocumentParam> params, TxTmGeneral txTmGeneral, TxMonitor monitor) {
        List<TxDocumentParams> output = new ArrayList<>();
        for (MDocumentParam param : params) {
            TxDocumentParams out = new TxDocumentParams();
            out.setApplicationNo(txTmGeneral.getApplicationNo());
            out.setDocumentId(param.getmDocument().getId()+"-"+monitor.getId()); //overwrite docId
            out.setParamName(param.getName());
            out.setParamType(param.getTypes());
            //System.out.print(param.getName());
            if (param.getTypes().equalsIgnoreCase("TERDEFINISI")) {
                out.setParamValue(this.findTerdefinisiValue(txTmGeneral, monitor, param.getName()));
            } else {
                out.setParamValue("");
            }
            output.add(out);
        }

        return output;
    }

    public String findTerdefinisiValue(TxTmGeneral txTmGeneral, TxMonitor monitor, String paramName) {
        String result = "";
        if (paramName.equalsIgnoreCase("<<created_date>>")) {
            result = this.fmtDate.format(monitor.getCreatedDate());
        } else if (paramName.equalsIgnoreCase("<<created_date_en>>")) {
            result = this.fmtDateEn.format(monitor.getCreatedDate());
        } else if (paramName.equalsIgnoreCase("<<application_no>>")) {
            result = txTmGeneral.getApplicationNo();
        } else if (paramName.equalsIgnoreCase("<<application_no_digit>>") || paramName.equalsIgnoreCase("<<application_no_digit_f>>")) {
            String no = txTmGeneral.getApplicationNo();
            result = no.substring(no.length() - 6);
        } else if (paramName.equalsIgnoreCase("<<irn>>") ) {
            String irn = txTmGeneral.getIrn();
            result = irn;
        } else if (paramName.equalsIgnoreCase("<<application_no_year>>") || paramName.equalsIgnoreCase("<<application_no_year_f>>")) {
            result = this.fmtYear.format(txTmGeneral.getCreatedDate());
        } else if (paramName.equalsIgnoreCase("<<current_year>>")) {
            result = this.fmtYear.format((new Date()));
        } else if (paramName.equalsIgnoreCase("<<tm_brand_name>>")) {
            result = txTmGeneral.getTxTmBrand() == null ? "NA" : txTmGeneral.getTxTmBrand().getName();
        } else if (paramName.equalsIgnoreCase("<<tm_brand_color>>")) {
            result = txTmGeneral.getTxTmBrand() == null ? "NA" : txTmGeneral.getTxTmBrand().getColor();
        } else if (paramName.equalsIgnoreCase("<<tm_brand_description>>")) {
            result = txTmGeneral.getTxTmBrand() == null ? "NA" : txTmGeneral.getTxTmBrand().getDescription();
        } else if (paramName.equalsIgnoreCase("<<tm_brand_translation>>")) {
            result = txTmGeneral.getTxTmBrand() == null ? "NA" : txTmGeneral.getTxTmBrand().getTranslation();
        } else if (paramName.equalsIgnoreCase("<<qr_code>>")) {
            String brandName = txTmGeneral.getTxTmBrand() != null ? txTmGeneral.getTxTmBrand().getName() : "NA";
            result = txTmGeneral.getApplicationNo() + "-" + brandName;
            //result = "";
        } else if (paramName.equalsIgnoreCase("<<created_date_year>>")) {
            result = this.fmtYear.format(monitor.getCreatedDate());
        } else if (paramName.equalsIgnoreCase("<<created_date_month>>")) {
            int monthroman = Integer. parseInt(this.fmtMonth.format(monitor.getCreatedDate()));
            result=getRoman(monthroman);
        } else if (paramName.equalsIgnoreCase("<<application_date>>")) {
            result = this.fmtDate.format(txTmGeneral.getTxReception().getApplicationDate());
        } else if (paramName.equalsIgnoreCase("<<application_date_en>>")) {
            result = this.fmtDateEn.format(txTmGeneral.getTxReception().getApplicationDate());
        } else if (paramName.equalsIgnoreCase("<<download_date>>") || paramName.equalsIgnoreCase("<<download_date_f>>")) {
            result = this.fmt.format(new Date());
        } else if (paramName.equalsIgnoreCase("<<nama>>")) {
            /*List<TxTmRepresentative> reps = txTmReprsCustomRepository.selectAllReprsByIdGeneral(txTmGeneral.getId());
            if(reps==null){
                TxTmOwner owner = txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneral, true);
                result = owner.getName();
            }else{
                result = ((TxTmRepresentative) reps).getmRepresentative().getName();
            }*/

            List<TxTmRepresentative> txTmRepresentatives = txTmReprsCustomRepository.selectAllReprsByIdGeneral(txTmGeneral.getId());
            String reprsName = "";
            ArrayList<String> reprsTemp = new ArrayList<String>();
            if (txTmRepresentatives.size() == 0) {
                TxTmOwner owner = txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneral, true);
                result = owner.getName();
            } else {
                for (TxTmRepresentative reprs : txTmRepresentatives) {
                    if (reprs.isStatus() == true) {
                        reprsTemp.add("" + reprs.getmRepresentative().getName() == null ? "" : reprs.getmRepresentative().getName());

                    }
                }
                Set<String> temp = new LinkedHashSet<String>(reprsTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmRepresentatives.size() > 0) {
                    reprsName = String.join(", ", unique);
                }
                result = reprsName;
            }
        } else if (paramName.equalsIgnoreCase("<<alamat>>")) {
            List<TxTmRepresentative> txTmRepresentatives = txTmReprsCustomRepository.selectAllReprsByIdGeneral(txTmGeneral.getId());
            String reprsAddress = "";
            ArrayList<String> reprsTemp = new ArrayList<String>();
            if (txTmRepresentatives.size() == 0) {
                TxTmOwner owner = txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneral, true);
                if (owner.getPostAddress()!=null){
                    String fullAddress = "";
                    String address = owner.getPostAddress() == null ? "" : owner.getPostAddress().trim().replace("(\\r\\n|\\n)","");
                    String city = owner.getPostCity() == null ? "" : ", " + owner.getPostCity().getName();
                    String province = owner.getPostProvince() == null ? "" : ", " + "\n"+ owner.getPostProvince().getName();
                    String country = owner.getPostCountry() == null || owner.getPostCountry().getId().equalsIgnoreCase("ID") ? "" : ", " + owner.getPostCountry().getName();
                    String zipcode = owner.getPostZipCode() == null ? "" : " " + owner.getPostZipCode();

                    fullAddress = address + city + province + country + zipcode;
                    result =  fullAddress;
                } else {
                    String fullAddress = "";
                    String address = owner.getAddress() == null ? "" : owner.getAddress().trim().replace("(\\r\\n|\\n)","");
                    String city = owner.getmCity() == null ? "" : ", " + owner.getmCity().getName();
                    String province = owner.getmProvince() == null ? "" : ", " + "\n"+ owner.getmProvince().getName();
                    String country = owner.getmCountry() == null || owner.getmCountry().getId().equalsIgnoreCase("ID") ? "" : ", " + owner.getmCountry().getName();
                    String zipcode = owner.getZipCode() == null ? "" : " " + owner.getZipCode();

                    fullAddress = address + city + province + country + zipcode;
                    result = fullAddress;

                }
//                if (owner.getAddress() != null) {
//                    fullAddress += "" + owner.getAddress().replace("(\\r\\n|\\n)","");
//                    if(owner.getmProvince() != null) {
//                        fullAddress += ", " +owner.getmProvince().getName();
//                    }
//                    if(owner.getmCity() != null) {
//                        fullAddress += ", " + owner.getmCity().getName();
//                    }
//                    if(owner.getZipCode() != null) {
//                        fullAddress += ", " + owner.getZipCode();
//                    }
//                }
//                result = fullAddress.replaceAll(", $", "");
            } else {
                for (TxTmRepresentative reprs : txTmRepresentatives) {
                    String fullAddress = "";
                    if (reprs.isStatus() == true) {
                        String address = reprs.getAddress() == null ? "" : reprs.getAddress();
                        String city = reprs.getmCity() == null ? "" : ", "+ reprs.getmCity().getName();
                        String province = reprs.getmProvince() == null ? "" : ", "+reprs.getmProvince().getName();
                        String country = reprs.getmCountry() == null || reprs.getmCountry().getId().equalsIgnoreCase("ID") ? "" : ", "+ reprs.getmCountry().getName();
                        String zipcode = reprs.getZipCode() == null ? "" : reprs.getZipCode();

                        fullAddress=address + city + province + country + zipcode;
                        reprsTemp.add(fullAddress);

//                        if (reprs.getAddress() != null) {
//                           fullAddress += "" + reprs.getAddress().replace("(\\r\\n|\\n)","");
//                            if(reprs.getmRepresentative().getmProvince() != null) {
//                                fullAddress += ", " + reprs.getmProvince().getName();
//                            }
//                            if(reprs.getmRepresentative().getmCity() != null) {
//                                fullAddress += ", " + reprs.getmCity().getName();
//                            }
//                            if(reprs.getmRepresentative().getZipCode() != null) {
//                                fullAddress += ", " + reprs.getZipCode();
//                            }
//                            reprsTemp.add(fullAddress);
//                        }
                    }
                }
                Set<String> temp = new LinkedHashSet<String>(reprsTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmRepresentatives.size() > 0) {
                    reprsAddress = String.join("", unique);
                }
                result = reprsAddress;
            }
        }
        else if (paramName.equalsIgnoreCase("<<namapasca>>")) {
            TxPostOwner txPostOwner = txPostOwnerCustomRepository.selectOne("txPostReception.id", monitor.getTxPostReception().getId(), true);
            if ( monitor != null && monitor.getTxPostReception() != null && monitor.getTxPostReception().getTxPostRepresentative() == null ) {
                result = txPostOwner.getName();
            } else
            if ( monitor != null && monitor.getTxPostReception() != null && monitor.getTxPostReception().getTxPostRepresentative() != null ) {
                TxPostRepresentative txPostRepresentative = monitor.getTxPostReception().getTxPostRepresentative();
                result = txPostRepresentative != null ? txPostRepresentative.getmRepresentative().getName(): "" ;
            } else {
                List<TxPostRepresentative> txPostRepresentative = txPostRepresentativeCustomRepository.findAllTxPostReprsByTxTmGeneral(txTmGeneral.getId());
                String txPostReprsName = "";
                ArrayList<String> txPostReprsTemp = new ArrayList<>();
                if (txPostRepresentative == null) {
                    result = txPostOwner.getName();
                } else {
                    for (TxPostRepresentative txPostReprs : txPostRepresentative) {
                        MRepresentative mRepresentative = txPostReprs.getmRepresentative();
                        txPostReprsTemp.add("" + mRepresentative.getName());
                    }
                    Set<String> temp = new LinkedHashSet<String>(txPostReprsTemp);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (txPostRepresentative.size() > 0) {
                        txPostReprsName = String.join(", ", unique);
                    }
                }
                result = txPostReprsName;
            }
            }
            else if (paramName.equalsIgnoreCase("<<alamatpasca>>")) {
            TxPostOwner txPostOwner = txPostOwnerCustomRepository.selectOne("txPostReception.id", monitor.getTxPostReception().getId(), true);
            if ( monitor != null && monitor.getTxPostReception() != null && monitor.getTxPostReception().getTxPostRepresentative() == null ) {
                String postOwnerAddress= txPostOwner.getAddress() == null ? "" : txPostOwner.getAddress();
                String postOwnerCity = txPostOwner.getmCity() == null ? "" : ", "+txPostOwner.getmCity().getName();
                String postOwnerProvince = txPostOwner.getmProvince() == null ? "" : ", "+txPostOwner.getmProvince().getName();
                String postOwnerCountry = txPostOwner.getmCountry() == null  || txPostOwner.getmCountry().getId().equalsIgnoreCase("ID")  ? "" : ", "+txPostOwner.getmCountry().getName();
                String postOwnerZipcode = txPostOwner.getZipCode() == null ? "" : ", "+txPostOwner.getZipCode();
                postOwnerAddress = postOwnerAddress.trim(); //hapus spasi akhir
//                postOwnerAddress = postOwnerAddress.replaceAll("(\\r\\n|\\n)"," "); //hapus enter
                String postOwnerAddressComplete = postOwnerAddress + postOwnerCity + postOwnerProvince +postOwnerCountry+ postOwnerZipcode;
                result = txPostOwner.getAddress();
//                result=postOwnerAddressComplete;
            } else
            if ( monitor != null && monitor.getTxPostReception() != null && monitor.getTxPostReception().getTxPostRepresentative() != null ) {
                TxPostRepresentative txPostRepresentative = monitor.getTxPostReception().getTxPostRepresentative();
                result = txPostRepresentative != null ? txPostRepresentative.getmRepresentative().getAddress(): "" ;
            } else {
                List<TxPostRepresentative> txPostRepresentative = txPostRepresentativeCustomRepository.findAllTxPostReprsByTxTmGeneral(txTmGeneral.getId());
                String txPostReprsAddress = "";
                ArrayList<String> postReprsTemp = new ArrayList<String>();
                if (txPostRepresentative == null) {
                    result = "";
                } else {
                    for (TxPostRepresentative postReprs : txPostRepresentative) {
                        MRepresentative mRepresentative = postReprs.getmRepresentative();
                        if (mRepresentative.getAddress() != null) {
                            postReprsTemp.add("" + mRepresentative.getAddress());
                        }
//                        if(mRepresentative.getmCity() != null) {
//                            postReprsTemp.add(", " + mRepresentative.getmCity().getName());
//                        }
//                        if(mRepresentative.getmProvince() != null) {
//                            postReprsTemp.add(", " + mRepresentative.getmProvince().getName());
//                        }
//                    if(mRepresentative.getmCountry() != null && mRepresentative.getmCountry().getId() == "ID") {
//                        postReprsTemp.add("" + mRepresentative.getmCountry().getName());
//                    }
//                        if(mRepresentative.getZipCode() != null) {
//                            postReprsTemp.add(", " + mRepresentative.getZipCode());
//                        }
                    }
                    Set<String> temp = new LinkedHashSet<String>(postReprsTemp);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (txPostRepresentative.size() > 0) {
                        txPostReprsAddress = String.join("", unique);
                    }
                }
                result = txPostReprsAddress.replaceAll(", $", "");
            }
            }
            else if (paramName.equalsIgnoreCase("<<kota>>")) {
            TxTmRepresentative reps = txTmReprsRepository.findTxTmRepresentativeByTxTmGeneral(txTmGeneral);
            if (reps == null) {
                TxTmOwner owner = txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneral, true);
                result = owner.getmCity().getName();
            } else {
                result = reps.getmRepresentative().getmCity() != null ? reps.getmRepresentative().getmCity().getName() : "";
            }
        } else if (paramName.equalsIgnoreCase("<<provinsi>>")) {
            TxTmRepresentative reps = txTmReprsRepository.findTxTmRepresentativeByTxTmGeneral(txTmGeneral);
            if (reps == null) {
                TxTmOwner owner = txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneral, true);
                result = owner.getmProvince().getName().trim().length() > 0 ? ", " + owner.getmProvince().getName().trim() : "";
            } else {
                result = reps.getmRepresentative().getmProvince() != null ? ", " + reps.getmRepresentative().getmProvince().getName().trim() : "";
            }
        } else if (paramName.equalsIgnoreCase("<<negara>>")) {
            TxTmRepresentative reps = txTmReprsRepository.findTxTmRepresentativeByTxTmGeneral(txTmGeneral);
            if (reps == null) {
                TxTmOwner owner = txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneral, true);
                result = owner.getmCountry().getName().trim().length() > 0 ? ", " + owner.getmCountry().getName().trim() : "";
            } else {
                result = reps.getmRepresentative().getmCountry().getName().trim().length() > 0 ? ", " + reps.getmRepresentative().getmCountry().getName().trim() : "";
            }
        } else if (paramName.equalsIgnoreCase("<<kodepos>>")) {
            TxTmRepresentative reps = txTmReprsRepository.findTxTmRepresentativeByTxTmGeneral(txTmGeneral);
            if (reps == null) {
                TxTmOwner owner = txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneral, true);
                result = owner.getZipCode() != null ? ", " + owner.getZipCode().trim() : "";
            } else {
                result = reps.getmRepresentative().getZipCode() != null ? ", " + reps.getmRepresentative().getZipCode().trim() : "";
            }
        } else if (paramName.equalsIgnoreCase("<<created_by>>") || paramName.equalsIgnoreCase("<<created_by_f>>")) {
            result = monitor.getCreatedBy().getmEmployee() != null ? monitor.getCreatedBy().getmEmployee().getEmployeeName() : "NA";
        } else if (paramName.equalsIgnoreCase("<<created_by_nik>>")) {
            result = monitor.getCreatedBy().getmEmployee() != null ? monitor.getCreatedBy().getmEmployee().getNik() : "NA";
        } else if (paramName.equalsIgnoreCase("<<filing_date>>")) {
            result = fmtDate.format(txTmGeneral.getFilingDate());
        } else if (paramName.equalsIgnoreCase("<<filing_date_en>>")) {
            result = fmtDateEn.format(txTmGeneral.getFilingDate());
        } else if (paramName.equalsIgnoreCase("<<tm_owner_name>>")) {
            List<TxTmOwner> txTmOwner = txTmOwnerCustomRepository.selectTxTmOwnerByTxTmGeneral(txTmGeneral.getId(), true);
            String ownerName = "";
            ArrayList<String> ownerTemp = new ArrayList<String>();
            if (txTmOwner == null) {
                result = "";
            } else {
                for (TxTmOwner owners : txTmOwner) {
                    ownerTemp.add("" + owners.getName() == null ? "" : owners.getName());
                }
                Set<String> temp = new LinkedHashSet<String>(ownerTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmOwner.size() > 0) {
                    ownerName = String.join(", ", unique);
                }
            }
            result = ownerName;
            /*result = txTmGeneral.getTxTmOwner()!=null ? txTmGeneral.getTxTmOwner().getName():"";*/
        } else if (paramName.equalsIgnoreCase("<<tm_owner_address>>")) {
            List<TxTmOwner> txTmOwner = txTmOwnerCustomRepository.selectTxTmOwnerByTxTmGeneral(txTmGeneral.getId(), true);
            String ownerAddress = "";
            ArrayList<String> ownerTemp = new ArrayList<String>();
            if (txTmOwner == null) {
                result = "";
            } else {
                for (TxTmOwner owners : txTmOwner) {
                    if (owners.getAddress() == null)
                    { ownerTemp.add("");}
                    else
                    {
                        ownerTemp.add("" + owners.getAddress());
                    }
                    if(owners.getmCity() == null)
                    { ownerTemp.add("");}
                    else
                        {
                        ownerTemp.add(", " + owners.getmCity().getName());
                    }
            		if(owners.getmProvince() == null)
                    { ownerTemp.add("");}
                    else
                        {
            			ownerTemp.add(", " +owners.getmProvince().getName());
            		}
                    if((owners.getmCountry() == null) || (owners.getmCountry().getId().equalsIgnoreCase("ID")))
                    { ownerTemp.add("");}
                    else
                        {
                        ownerTemp.add(", " + owners.getmCountry().getName());
                    }
            		if(owners.getZipCode() == null)
                    { ownerTemp.add("");}
                    else
                        {
            			ownerTemp.add(" " + owners.getZipCode());
            		}
                }
                Set<String> temp = new LinkedHashSet<String>(ownerTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmOwner.size() > 0) {
                    ownerAddress = String.join("", unique);
                }
            }
            result = ownerAddress.replaceAll(", $", "");
        } else if (paramName.equalsIgnoreCase("<<tm_owner_address_only>>")) {
            List<TxTmOwner> txTmOwner = txTmOwnerCustomRepository.selectTxTmOwnerByTxTmGeneral(txTmGeneral.getId(), true);
            String ownerAddress = "";
            ArrayList<String> ownerTemp = new ArrayList<String>();
            if (txTmOwner == null) {
                result = "";
            } else {
                for (TxTmOwner owners : txTmOwner) {
                    if (owners.getAddress() == null)
                    { ownerTemp.add("");}
                    else
                    {
                        ownerTemp.add("" + owners.getAddress());
                    }
//                    if(owners.getmCity() == null)
//                    { ownerTemp.add("");}
//                    else
//                    {
//                        ownerTemp.add(", " + owners.getmCity().getName());
//                    }
//                    if(owners.getmProvince() == null)
//                    { ownerTemp.add("");}
//                    else
//                    {
//                        ownerTemp.add(", " +owners.getmProvince().getName());
//                    }
//                    if((owners.getmCountry() == null) || (owners.getmCountry().getId().equalsIgnoreCase("ID")))
//                    { ownerTemp.add("");}
//                    else
//                    {
//                        ownerTemp.add(", " + owners.getmCountry().getName());
//                    }
//                    if(owners.getZipCode() == null)
//                    { ownerTemp.add("");}
//                    else
//                    {
//                        ownerTemp.add(", " + owners.getZipCode());
//                    }
                }
                Set<String> temp = new LinkedHashSet<String>(ownerTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmOwner.size() > 0) {
                    ownerAddress = String.join("", unique);
                }
            }
            result = ownerAddress.replaceAll(", $", "");
        } else if (paramName.equalsIgnoreCase("<<tm_owner_address_ori>>")) {
            List<TxTmOwner> txTmOwner = txTmOwnerCustomRepository.selectTxTmOwnerByTxTmGeneral(txTmGeneral.getId(), true);
            String ownerAddress = "";
            ArrayList<String> ownerTemp = new ArrayList<String>();
            if (txTmOwner == null) {
                result = "";
            } else {
                for (TxTmOwner owners : txTmOwner) {
                    if (owners.getAddress() == null)
                    { ownerTemp.add("");}
                    else
                    {
                        ownerTemp.add("" + owners.getAddress());
                    }
                    if(owners.getmCity() == null)
                    { ownerTemp.add("");}
                    else
                    {
                        ownerTemp.add(", " + owners.getmCity().getName());
                    }
                    if(owners.getmProvince() == null)
                    { ownerTemp.add("");}
                    else
                    {
                        ownerTemp.add(", " +owners.getmProvince().getName());
                    }
                    if((owners.getmCountry() == null) || (owners.getmCountry().getId().equalsIgnoreCase("ID")))
                    { ownerTemp.add("");}
                    else
                    {
                        ownerTemp.add(", " + owners.getmCountry().getName());
                    }
                    if(owners.getZipCode() == null)
                    { ownerTemp.add("");}
                    else
                    {
                        ownerTemp.add(" " + owners.getZipCode());
                    }
                }
                Set<String> temp = new LinkedHashSet<String>(ownerTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmOwner.size() > 0) {
                    ownerAddress = String.join("", unique);
                }
            }
            result = ownerAddress.replaceAll(", $", "");
        }
          else if (paramName.equalsIgnoreCase("<<journal_no>>")) {
            TxGroupDetail txGroupDetail = txGroupDetailRepository.findTxGroupDetailByTxTmGeneral(txTmGeneral.getId(), "GrupPublikasi");
            TxPubsJournal txPubsJournal = txPubsJournalRepository.findTxPubsJournalByTxGroup(txGroupDetail.getTxGroup());
            result = (txPubsJournal == null || txPubsJournal.getJournalNo() == null) ? "" : txPubsJournal.getJournalNo();
        } else if (paramName.equalsIgnoreCase("<<journal_start>>")) {
            TxGroupDetail txGroupDetail = txGroupDetailRepository.findTxGroupDetailByTxTmGeneral(txTmGeneral.getId(), "GrupPublikasi");
            TxPubsJournal txPubsJournal = txPubsJournalRepository.findTxPubsJournalByTxGroup(txGroupDetail.getTxGroup());
            if (txPubsJournal == null || txPubsJournal.getJournalStart() == null) {
                result = "";
            } else {
                Date date = new Date(txPubsJournal.getJournalStart().getTime());
                result = fmtDate.format(date); //english
            }
        } else if (paramName.equalsIgnoreCase("<<journal_end>>")) {
            TxGroupDetail txGroupDetail = txGroupDetailRepository.findTxGroupDetailByTxTmGeneral(txTmGeneral.getId(), "GrupPublikasi");
            TxPubsJournal txPubsJournal = txPubsJournalRepository.findTxPubsJournalByTxGroup(txGroupDetail.getTxGroup());
            if (txPubsJournal == null || txPubsJournal.getJournalEnd() == null) {
                result = "";
            } else {
                Date date = new Date(txPubsJournal.getJournalEnd().getTime());
                result = fmtDate.format(date);
            }
        } else if (paramName.equalsIgnoreCase("<<tm_class_detail>>")) {
            List<TxTmClass> txTmClasses = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
            int key;
            String value = "";
            String desc = "";
            Map<Integer, String> mapClass = new HashMap<>();

            for (TxTmClass txTmClass : txTmClasses) {
                MClassDetail mClassDetail = txTmClass.getmClassDetail();
                key = txTmClass.getmClass().getNo();
                desc = txTmClass.getmClassDetail().getDesc();

                if (mapClass.containsKey(key)) {
                    value = mapClass.get(key);
                    value = value + "; " + desc.trim().replaceAll("(\\r\\n|\\n)","");
                } else {
                    value = desc.trim().replaceAll("(\\r\\n|\\n)","");;
                }

                mapClass.put(key, value);

            }

            Map<Integer, String> mapClassOrder = mapClass.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            StringBuffer sbClassNoList = new StringBuffer();
            StringBuffer sbClassDescList = new StringBuffer();
            for (Map.Entry<Integer, String> map : mapClassOrder.entrySet()) {
                sbClassNoList.append(map.getKey());
                sbClassNoList.append(", ");

                sbClassDescList.append("\nKelas " + map.getKey() + " :\n");
                sbClassDescList.append("=== ");
                sbClassDescList.append(map.getValue());
                sbClassDescList.append(" ===");
                sbClassDescList.append("\n");
            }
            String classNoList = "";
            String classDescList = "";
            if (sbClassNoList.length() > 0) {
                classNoList = sbClassNoList.substring(0, sbClassNoList.length() - 2);
            }
            if (sbClassDescList.length() > 0) {
                classDescList = sbClassDescList.substring(0, sbClassDescList.length() - 1);
            }
            result=classDescList;

//            List<TxTmClass> txTmClasses = txTmClassRepository.findTxTmClassByTxTmGeneralAndTransactionStatus(txTmGeneral, "ACCEPT");
//            String kelasUraian = "";
//            for (TxTmClass txTmClass : txTmClasses) {
//                MClassDetail mClassDetail = txTmClass.getmClassDetail();
//                kelasUraian = kelasUraian + mClassDetail.getDesc() + ", ";
//            }
//            kelasUraian = kelasUraian.trim(); //hapus spasi akhir
//            kelasUraian = kelasUraian.substring(0, kelasUraian.length() - 1); //hapus koma akhir
//            result = kelasUraian;
        } else if (paramName.equalsIgnoreCase("<<class_detail_reject>>")) {
            List<TxTmClass> txTmClasses = txTmClassRepository.findTxTmClassByTxTmGeneralAndTransactionStatus(txTmGeneral, "REJECT");
            int key;
            String value = "";
            String desc = "";
            Map<Integer, String> mapClass = new HashMap<>();

            for (TxTmClass txTmClass : txTmClasses) {
                MClassDetail mClassDetail = txTmClass.getmClassDetail();
                key = txTmClass.getmClass().getNo();
                desc = txTmClass.getmClassDetail().getDesc();

                if (mapClass.containsKey(key)) {
                    value = mapClass.get(key);
                    value = value + "; " + desc.trim().replaceAll("(\\r\\n|\\n)","");
                } else {
                    value = desc.trim().replaceAll("(\\r\\n|\\n)","");;
                }

                mapClass.put(key, value);

            }

            Map<Integer, String> mapClassOrder = mapClass.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            StringBuffer sbClassNoList = new StringBuffer();
            StringBuffer sbClassDescList = new StringBuffer();
            for (Map.Entry<Integer, String> map : mapClassOrder.entrySet()) {
                sbClassNoList.append(map.getKey());
                sbClassNoList.append(", ");

                sbClassDescList.append("Kelas " + map.getKey() + " :\n");
                sbClassDescList.append("=== ");
                sbClassDescList.append(map.getValue());
                sbClassDescList.append(" ===");
                sbClassDescList.append("\n");
            }
            String classNoList = "";
            String classDescList = "";
            if (sbClassNoList.length() > 0) {
                classNoList = sbClassNoList.substring(0, sbClassNoList.length() - 2);
            }
            if (sbClassDescList.length() > 0) {
                classDescList = sbClassDescList.substring(0, sbClassDescList.length() - 1);
            }
            result=classDescList;
        } else if (paramName.equalsIgnoreCase("<<class_no_reject>>")) {
            List<TxTmClass> classReject = txTmClassRepository.findTxTmClassByTxTmGeneralAndTransactionStatus(txTmGeneral, "REJECT");
            String kelasNoReject = "";
            for (TxTmClass txTmClass : classReject) {
                if (kelasNoReject == "") {
                    kelasNoReject += txTmClass.getmClass().getNo();
                }else{
                    kelasNoReject += "," + txTmClass.getmClass().getNo();
                }
            }
             result = kelasNoReject;
        } else if (paramName.equalsIgnoreCase("<<tm_prior_no>>")) {
            List<TxTmPrior> txTmPrior = txTmPriorCustomRepository.selectTxTmPriorByTxTmGeneral(txTmGeneral.getId(), PriorStatusEnum.ACCEPT.name());
            String priorNo = "";
            ArrayList<String> priorTemp = new ArrayList<String>();
            if (txTmPrior == null) {
                result = "";
            } else {
                for (TxTmPrior priors : txTmPrior) {
                    priorTemp.add("" + priors.getNo() == null ? "" : priors.getNo());
                }
                Set<String> temp = new LinkedHashSet<String>(priorTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmPrior.size() > 0) {
                    priorNo = String.join(", ", unique);
                }
            }
            result = priorNo;
        } else if (paramName.equalsIgnoreCase("<<tm_prior_date>>")) {
            List<TxTmPrior> txTmPrior = txTmPriorCustomRepository.selectTxTmPriorByTxTmGeneral(txTmGeneral.getId(), PriorStatusEnum.ACCEPT.name());
            if (txTmPrior == null) {
                result = "";
            } else {
                for (TxTmPrior priors : txTmPrior) {
                    Date date = new Date(priors.getPriorDate().getTime());
                    result = fmt.format(date);
                }
            }
        } else if (paramName.equalsIgnoreCase("<<tm_prior_country>>")) {
            List<TxTmPrior> txTmPrior = txTmPriorCustomRepository.selectTxTmPriorByTxTmGeneral(txTmGeneral.getId(), PriorStatusEnum.ACCEPT.name());
            String priorCountryName = "";
            ArrayList<String> priorTemp = new ArrayList<String>();
            if (txTmPrior == null) {
                result = "";
            } else {
                for (TxTmPrior priors : txTmPrior) {
                    MCountry mCountry = priors.getmCountry();
                    priorTemp.add("" + mCountry.getName() == null ? "" : mCountry.getName());
                }
                Set<String> temp = new LinkedHashSet<String>(priorTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmPrior.size() > 0) {
                    priorCountryName = String.join(", ", unique);
                }
            }
            result = priorCountryName;
        } else if (paramName.equalsIgnoreCase("<<tm_old_owner_name>>")) {
            TxPostReception post_reception = monitor.getTxPostReception();
            String postId = "";
            postId = monitor.getTxPostReception().getId();
            if(post_reception!=null){
            List<TxTmOwner> txTmOwner = txTmOwnerCustomRepository.selectTxTmOwnerByPostId(txTmGeneral.getId(), postId);
            String ownerName = "";
            ArrayList<String> ownerTemp = new ArrayList<String>();

                for (TxTmOwner owners : txTmOwner) {
                    ownerTemp.add("" + owners.getName() == null ? "" : owners.getName());
                }
                Set<String> temp = new LinkedHashSet<String>(ownerTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmOwner.size() > 0) {
                    ownerName = String.join(", ", unique);
                }

            result = ownerName; }
        } else if (paramName.equalsIgnoreCase("<<tm_old_owner_address>>")) {
            TxPostReception post_reception = monitor.getTxPostReception();
            String postId = "";
            postId = monitor.getTxPostReception().getId();
            String ownerAddress = "";
            if(post_reception!=null){
//            }
//            result = nodokumen;
                List<TxTmOwner> txTmOwner = txTmOwnerCustomRepository.selectTxTmOwnerByPostId(txTmGeneral.getId(), postId);
                ArrayList<String> ownerTemp = new ArrayList<String>();

                for (TxTmOwner owners : txTmOwner) {
                    if (owners.getAddress() == null) {
                        ownerTemp.add("");
                    } else {
                        ownerTemp.add("" + owners.getAddress());
                    }
                    if (owners.getmCity() == null) {
                        ownerTemp.add("");
                    } else {
                        ownerTemp.add(", " + owners.getmCity().getName());
                    }
                    if (owners.getmProvince() == null) {
                        ownerTemp.add("");
                    } else {
                        ownerTemp.add(", " + owners.getmProvince().getName());
                    }
                    if ((owners.getmCountry() == null) || (owners.getmCountry().getId().equalsIgnoreCase("ID"))) {
                        ownerTemp.add("");
                    } else {
                        ownerTemp.add(", " + owners.getmCountry().getName());
                    }
                    if (owners.getZipCode() == null) {
                        ownerTemp.add("");
                    } else {
                        ownerTemp.add(" " + owners.getZipCode());
                    }
                }
                Set<String> temp = new LinkedHashSet<String>(ownerTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmOwner.size() > 0) {
                    ownerAddress = String.join("", unique);
                }
            }   result = ownerAddress.replaceAll(", $", "");
        } else if (paramName.equalsIgnoreCase("<<reprs_name>>")) {
            List<TxTmRepresentative> txTmRepresentative = txTmReprsCustomRepository.selectTxTmReprsByTxTmGeneral(txTmGeneral.getId());
            String reprsName = "";
            ArrayList<String> reprsTemp = new ArrayList<>();
            if (txTmRepresentative == null) {
                result = "";
            } else {
                for (TxTmRepresentative reprs : txTmRepresentative) {
                    MRepresentative mRepresentative = reprs.getmRepresentative();
                    reprsTemp.add("" + mRepresentative.getName() == null ? "" : mRepresentative.getName());
                }
                Set<String> temp = new LinkedHashSet<>(reprsTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmRepresentative.size() > 0) {
                    reprsName = String.join(", ", unique);
                }
            }
            result = reprsName;
        } else if (paramName.equalsIgnoreCase("<<reprs_address>>")) {
            List<TxTmRepresentative> txTmRepresentative = txTmReprsCustomRepository.selectTxTmReprsByTxTmGeneral(txTmGeneral.getId());
            String reprsAddress = "";
            ArrayList<String> reprsTemp = new ArrayList<String>();
            if (txTmRepresentative == null) {
                result = "";
            } else {
                for (TxTmRepresentative reprs : txTmRepresentative) {
                    MRepresentative mRepresentative = reprs.getmRepresentative();
                    if (mRepresentative.getAddress() != null) {
                        reprsTemp.add("" + mRepresentative.getAddress());
                    }
//            		if(mRepresentative.getmCity() != null ) {
//            			reprsTemp.add("" + mRepresentative.getmCity().getName());
//            		}
//                    if(mRepresentative.getmProvince() != null ) {
//                        reprsTemp.add("" + mRepresentative.getmProvince().getName());
//                    }
//                    if(mRepresentative.getmCountry() != null && mRepresentative.getmCountry().getId() == "ID" ) {
//                        reprsTemp.add("" + mRepresentative.getmCountry().getName());
//                    }
//            		if(mRepresentative.getZipCode() != null ) {
//            			reprsTemp.add(mRepresentative.getZipCode());
//            		}
                }
                Set<String> temp = new LinkedHashSet<String>(reprsTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txTmRepresentative.size() > 0) {
                    reprsAddress = String.join(", ", unique);
                }
            }
            result = reprsAddress.replaceAll(", $", "");
        } else if (paramName.equalsIgnoreCase("<<post_reprs_name>>")) {
            if ( monitor != null && monitor.getTxPostReception() != null && monitor.getTxPostReception().getTxPostRepresentative() != null ) {
                TxPostRepresentative txPostRepresentative = monitor.getTxPostReception().getTxPostRepresentative();
                result = txPostRepresentative != null ? txPostRepresentative.getmRepresentative().getName(): "" ;
            }
//            else {
//                List<TxPostRepresentative> txPostRepresentative = txPostRepresentativeCustomRepository.findAllTxPostReprsByTxTmGeneral(txTmGeneral.getId());
//                String txPostReprsName = "";
//                ArrayList<String> txPostReprsTemp = new ArrayList<>();
//                if (txPostRepresentative == null) {
//                    result = "";
//                } else {
//                    for (TxPostRepresentative txPostReprs : txPostRepresentative) {
//                        MRepresentative mRepresentative = txPostReprs.getmRepresentative();
//                        txPostReprsTemp.add("" + mRepresentative.getName());
//                    }
//                    Set<String> temp = new LinkedHashSet<String>(txPostReprsTemp);
//                    String[] unique = temp.toArray(new String[temp.size()]);
//                    if (txPostRepresentative.size() > 0) {
//                        txPostReprsName = String.join(", ", unique);
//                    }
//                }
//                result = txPostReprsName;
//            }
        } else if (paramName.equalsIgnoreCase("<<post_reprs_address>>")) {
            if ( monitor != null && monitor.getTxPostReception() != null && monitor.getTxPostReception().getTxPostRepresentative() != null ) {
                TxPostRepresentative txPostRepresentative = monitor.getTxPostReception().getTxPostRepresentative();
//                result = txPostRepresentative.getmRepresentative().getAddress();
                result = txPostRepresentative != null ? txPostRepresentative.getmRepresentative().getAddress(): "" ;
            }
//            else {
//                List<TxPostRepresentative> txPostRepresentative = txPostRepresentativeCustomRepository.findAllTxPostReprsByTxTmGeneral(txTmGeneral.getId());
//                String txPostReprsAddress = "";
//                ArrayList<String> postReprsTemp = new ArrayList<String>();
//                if (txPostRepresentative == null) {
//                    result = "";
//                } else {
//                    for (TxPostRepresentative postReprs : txPostRepresentative) {
//                        MRepresentative mRepresentative = postReprs.getmRepresentative();
//                        if (mRepresentative.getAddress() != null) {
//                            postReprsTemp.add("" + mRepresentative.getAddress());
//                        }
//            		if(mRepresentative.getmCity() != null) {
//            			postReprsTemp.add("" + mRepresentative.getmCity().getName());
//            		}
//                    if(mRepresentative.getmProvince() != null) {
//                        postReprsTemp.add("" + mRepresentative.getmProvince().getName());
//                    }
//                    if(mRepresentative.getmCountry() != null && mRepresentative.getmCountry().getId() == "ID") {
//                        postReprsTemp.add("" + mRepresentative.getmCountry().getName());
//                    }
//            		if(mRepresentative.getZipCode() != null) {
//            			postReprsTemp.add("" + mRepresentative.getZipCode());
//            		}
//                    }
//                    Set<String> temp = new LinkedHashSet<String>(postReprsTemp);
//                    String[] unique = temp.toArray(new String[temp.size()]);
//                    if (txPostRepresentative.size() > 0) {
//                        txPostReprsAddress = String.join(", ", unique);
//                    }
//                }
//                result = txPostReprsAddress.replaceAll(", $", "");
//            }
        } else if (paramName.equalsIgnoreCase("<<post_owner_name>>")) {
            if ( monitor != null && monitor.getTxPostReception() != null && monitor.getTxPostReception().getTxPostOwner() != null ) {
                TxPostOwner txPostOwner = monitor.getTxPostReception().getTxPostOwner();
                result = txPostOwner.getName() ;
            }
        } else if (paramName.equalsIgnoreCase("<<post_owner_address>>")) {
            if ( monitor != null && monitor.getTxPostReception() != null && monitor.getTxPostReception().getTxPostOwner() != null ) {
                TxPostOwner txPostOwner = monitor.getTxPostReception().getTxPostOwner();
                result = txPostOwner.getAddress() ;
            }
        } else if (paramName.equalsIgnoreCase("<<reg_no>>")) {
            TxRegistration txRegistration = txRegistrationCustomRepository.selectActiveRegNo(txTmGeneral.getId(), true);
            result = (txRegistration != null ? (txRegistration.getNo() != null ? txRegistration.getNo() : "") : "");
        } else if (paramName.equalsIgnoreCase("<<reg_start_date>>")) {
            TxRegistration txRegistration = txRegistrationCustomRepository.selectActiveRegNo(txTmGeneral.getId(), true);
            if (txRegistration == null || txRegistration.getStartDate() == null) {
                result = "";
            } else {
                Date date = new Date(txRegistration.getStartDate().getTime());
                result = fmtDate.format(date);
            }
            /*result = (txRegistration != null ? (txRegistration.getStartDateTemp() !=null ? txRegistration.getStartDateTemp() : "") :"");*/
        } else if (paramName.equalsIgnoreCase("<<expiration_date>>")) {
            TxRegistration txRegistration = txRegistrationCustomRepository.selectActiveRegNo(txTmGeneral.getId(), true);
            if (txRegistration == null || txRegistration.getEndDate() == null) {
                result = "";
            } else {
                Date date = new Date(txRegistration.getEndDate().getTime());
                result = fmtDate.format(date);
            }
        } else if (paramName.equalsIgnoreCase("<<dirjen_name>>")) {
            MLookup mLookup = mLookupCostumRepository.selectLookName("DirjenName");
            result = mLookup.getName() != null ? mLookup.getName() : "";
        } else if (paramName.equalsIgnoreCase("<<dirjen_nip>>")) {
            MLookup mLookup = mLookupCostumRepository.selectLookName("DirjenNIP");
            result = mLookup.getName() != null ? mLookup.getName() : "";
        } else if (paramName.equalsIgnoreCase("<<dirmerek_name>>")) {
            MLookup mLookup = mLookupCostumRepository.selectLookName("DirMerekName");
            result = mLookup.getName() != null ? mLookup.getName() : "";
        } else if (paramName.equalsIgnoreCase("<<dirmerek_nip>>")) {
            MLookup mLookup = mLookupCostumRepository.selectLookName("DirMerekNIP");
            result = mLookup.getName() != null ? mLookup.getName() : "";
        } else if (paramName.equalsIgnoreCase("<<label_merek>>")) {
            TxTmBrand txTmBrand = txTmBrandCustomRepository.selectOne("txTmGeneral.id", txTmGeneral.getId());
            result = txTmBrand.getId();
        } else if (paramName.equalsIgnoreCase("<<class_no_all>>")) {
            String classNo = "";
            List<MClass> mClass = mClassRepository.findAll();
            if (mClass.size() > 0) {
                for (MClass mClassList : mClass) {
                    if (!classNo.contains(mClassList.getNo().toString())) { //supaya gak duplikatselectAllClassByGeneralId
                        classNo = classNo + mClassList.getNo() + ",";
                    }
                }
                classNo = classNo.substring(0, classNo.length() - 1);
            }
            result = classNo;
        } else if (paramName.equalsIgnoreCase("<<class_no_app>>")) {
            List<TxTmClass> txTmClasses = txTmClassRepository.findTxTmClassByTxTmGeneralAndTransactionStatus(txTmGeneral, "ACCEPT");
            int key;
            String value = "";
            Map<Integer, String> mapClass = new HashMap<>();

            for (TxTmClass txTmClass : txTmClasses) {
                key = txTmClass.getmClass().getNo();
                mapClass.put(key, value);
            }

            Map<Integer, String> mapClassOrder = mapClass.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            StringBuffer sbClassNoList = new StringBuffer();
            for (Map.Entry<Integer, String> map : mapClassOrder.entrySet()) {
                sbClassNoList.append(map.getKey());
                sbClassNoList.append(", ");
            }
            String classNoList = "";
            if (sbClassNoList.length() > 0) {
                classNoList = sbClassNoList.substring(0, sbClassNoList.length() - 2);
            }
            result=classNoList;
        }   else if (paramName.equalsIgnoreCase("<<edisi_kelas>>")) {
            List<String> classEd = txTmClassCustomRepository.findClassEditionTxTmClassByApplicationID(txTmGeneral.getId());
            String classEd1 = "";
            for (String classEd2 : classEd) {
                classEd1=classEd2;
            }
            result = classEd1;
        }  else if (paramName.equalsIgnoreCase("<<status_permohonan>>")) {
            String statusPermohonan = txTmGeneral.getmStatus().getName();
            result = statusPermohonan;
        } else if (paramName.equalsIgnoreCase("<<license_name>>")) {
            List<TxLicense> txLicense = txLicenseCustomRepository.findTxLicenseByTxTmGeneral(txTmGeneral.getId(), true);
            String licenseName = "";
            ArrayList<String> licenseTemp = new ArrayList<String>();
            if (txLicense == null) {
                result = "";
            } else {
                for (TxLicense license : txLicense) {
                    licenseTemp.add("" + license.getName() == null ? "" : license.getName());
                }
                Set<String> temp = new LinkedHashSet<String>(licenseTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txLicense.size() > 0) {
                    licenseName = String.join(", ", unique);
                }
            }
            result = licenseName;
        } else if (paramName.equalsIgnoreCase("<<license_address>>")) {
            List<TxLicense> txLicense = txLicenseCustomRepository.findTxLicenseByTxTmGeneral(txTmGeneral.getId(), true);
            String licenseAddress = "";
            ArrayList<String> licenseTemp = new ArrayList<String>();
            if (txLicense == null) {
                licenseAddress = "";
            } else {
                for (TxLicense license : txLicense) {
                    String address = license.getAddress() == null ? "" : license.getAddress();
                    String city = license.getmCity() == null ? "" : ", "+license.getmCity().getName();
                    String province = license.getmProvince() == null ? "" : ", "+license.getmProvince().getName()+", ";
                    String country = license.getmCountry() == null || license.getmCountry().getId().equalsIgnoreCase("ID") ? "" : ", "+license.getmCountry().getName();
                    String zipcode = license.getZipCode() == null ? "" : ", "+license.getZipCode();

                    licenseTemp.add(address + city + province + country + zipcode);
                }
                Set<String> temp = new LinkedHashSet<String>(licenseTemp);
                String[] unique = temp.toArray(new String[temp.size()]);
                if (txLicense.size() > 0) {
                    licenseAddress = String.join("", unique);
                }
            }
            result = licenseAddress;
      }
        else if (paramName.equalsIgnoreCase("<<sub_license_main_name>>")) {
            TxPostReception post_reception = monitor.getTxPostReception();
            String postId = "";
            postId = monitor.getTxPostReception().getId();
            if(post_reception!=null) {
                List<TxLicense> txLicense = txLicenseCustomRepository.findSubLicenseByTxTmGeneral(txTmGeneral.getId(), postId);
                String licenseName = "";
                ArrayList<String> licenseTemp = new ArrayList<String>();
                if (txLicense == null) {
                    result = "";
                } else {
                    for (TxLicense license : txLicense) {
                        licenseTemp.add("" + license.getName() == null ? "" : license.getName());
                    }
                    Set<String> temp = new LinkedHashSet<String>(licenseTemp);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (txLicense.size() > 0) {
                        licenseName = String.join(", ", unique);
                    }
                }
            result = licenseName;}
    } else if (paramName.equalsIgnoreCase("<<sub_license_main_address>>")) {
            TxPostReception post_reception = monitor.getTxPostReception();
            String postId = "";
            postId = monitor.getTxPostReception().getId();
            if(post_reception!=null) {
                List<TxLicense> txLicense = txLicenseCustomRepository.findSubLicenseByTxTmGeneral(txTmGeneral.getId(), postId);
                String licenseAddress = "";
                ArrayList<String> licenseTemp = new ArrayList<String>();
                if (txLicense == null) {
                    licenseAddress = "";
                } else {
                    for (TxLicense license : txLicense) {
                        String address = license.getAddress() == null ? "" : license.getAddress();
                        String city = license.getmCity() == null ? "" : ", "+license.getmCity().getName();
                        String province = license.getmProvince() == null ? "" : ", "+license.getmProvince().getName()+", ";
                        String country = license.getmCountry() == null || license.getmCountry().getId().equalsIgnoreCase("ID") ? "" : ", "+license.getmCountry().getName();
                        String zipcode = license.getZipCode() == null ? "" : ", "+license.getZipCode();

                        licenseTemp.add(address + city + province + country + zipcode);

                    }
                    Set<String> temp = new LinkedHashSet<String>(licenseTemp);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (txLicense.size() > 0) {
                        licenseAddress = String.join("", unique);
                    }
                }
                result = licenseAddress;}
        } else if (paramName.equalsIgnoreCase("<<sub_license_child_name>>")) {
            TxPostReception post_reception = monitor.getTxPostReception();
            String postId = "";
            postId = monitor.getTxPostReception().getId();
            if(post_reception!=null) {
                List<TxLicense> txLicense = txLicenseCustomRepository.findSubLicenseChildByTxTmGeneral(txTmGeneral.getId(), postId);
                String licenseName = "";
                ArrayList<String> licenseTemp = new ArrayList<String>();
                if (txLicense == null) {
                    result = "";
                } else {
                    for (TxLicense license : txLicense) {
                        licenseTemp.add("" + license.getName() == null ? "" : license.getName());
                    }
                    Set<String> temp = new LinkedHashSet<String>(licenseTemp);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (txLicense.size() > 0) {
                        licenseName = String.join(", ", unique);
                    }
                }
                result = licenseName;}
        } else if (paramName.equalsIgnoreCase("<<sub_license_child_address>>")) {
            TxPostReception post_reception = monitor.getTxPostReception();
            String postId = "";
            postId = monitor.getTxPostReception().getId();
            if (post_reception != null) {
                List<TxLicense> txLicense = txLicenseCustomRepository.findSubLicenseChildByTxTmGeneral(txTmGeneral.getId(), postId);
                String licenseAddress = "";
                ArrayList<String> licenseTemp = new ArrayList<String>();
                if (txLicense == null) {
                    licenseAddress = "";
                } else {
                    for (TxLicense license : txLicense) {
                        String address = license.getAddress() == null ? "" : license.getAddress();
                        String city = license.getmCity() == null ? "" : ", " + license.getmCity().getName();
                        String province = license.getmProvince() == null ? "" : ", " + license.getmProvince().getName() + ", ";
                        String country = license.getmCountry() == null || license.getmCountry().getId().equalsIgnoreCase("ID") ? "" : ", " + license.getmCountry().getName();
                        String zipcode = license.getZipCode() == null ? "" : ", " + license.getZipCode();

                        licenseTemp.add(address + city + province + country + zipcode);

                    }
                    Set<String> temp = new LinkedHashSet<String>(licenseTemp);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (txLicense.size() > 0) {
                        licenseAddress = String.join("", unique);
                    }
                }
                result = licenseAddress;
            }
        } else if (paramName.equalsIgnoreCase("<<created_by_sign>>")) {
            result = monitor.getCreatedBy().getmEmployee() != null ? monitor.getCreatedBy().getmEmployee().getSign() : "NA";
        } else if (paramName.equalsIgnoreCase("<<nomor_dokumen>>")) {
            TxPostReception post_reception = monitor.getTxPostReception();
            String nodokumen = "";
            if(post_reception!=null){
                nodokumen = monitor.getTxPostReception().getPostNo();
            }
            result = nodokumen;
        }
        else if (paramName.equalsIgnoreCase("<<catatan_monitor>>")) {
            String catatanmonitor = "";
            if(monitor.getNotes()!=null){
                catatanmonitor = monitor.getNotes();
            }
            result = catatanmonitor;
        } else if (paramName.equalsIgnoreCase("<<post_reception_date>>")) {
            String postdate = "";
            TxPostReception post_reception = monitor.getTxPostReception();
            if (post_reception == null) {
                postdate = "";
            } else {
                postdate = this.fmtDate.format(post_reception.getPostDate());
            }
            result = postdate;
        }  else if (paramName.equalsIgnoreCase("<<tx_similarity_result>>")) {
            List<TxSimilarityResult> txSimilarityResultList = txSimilarityResultRepository.findTxSimilarityResultByOriginTxTmGeneral(txTmGeneral);
            result = "";
            for (TxSimilarityResult txSimilarityResult : txSimilarityResultList) {
                TxTmGeneral txTmGeneralPembanding = txSimilarityResult.getSimilarTxTmGeneral();
                String noPemohon = txTmGeneralPembanding.getApplicationNo();
                String tglPenerimaan = fmt.format(txTmGeneralPembanding.getFilingDate());
                TxRegistration txRegistration = txRegistrationRepository.findTxRegistrationByTxTmGeneral(txTmGeneralPembanding);
                String noPendaftaran = txRegistration != null ? txRegistration.getNo() : "-\t\t";
                String tanggalPendaftaran = txRegistration != null ? fmt.format(txRegistration.getStartDate()) : "-";
                TxTmOwner txTmOwner = txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneralPembanding, true);
                String cityOwner = txTmOwner.getmCity() != null ? txTmOwner.getmCity().getName() : "";
                String ownerName = txTmOwner.getName() != null ? txTmOwner.getName() : "";
                String ownerAddress = txTmOwner.getAddress() != null ? txTmOwner.getAddress() : "";
                String ownerProvince = txTmOwner.getmProvince() != null ? txTmOwner.getmProvince().getName() : "";
                String ownerCountry = txTmOwner.getmCountry() != null ? txTmOwner.getmCountry().getName() : "";

                ownerAddress = ownerAddress.trim(); //hapus spasi akhir
                ownerAddress = ownerAddress.replaceAll("(\\r\\n|\\n)",""); //hapus enter
                String pemohon = ownerName + "\n\t\t\t" + justifyAddressInfo(ownerAddress,48) + cityOwner + " " + ownerProvince+ " " + ownerCountry;
//                String pemohon = txTmOwner.getName() + "\n\t\t\t" + justifyAddressInfo(txTmOwner.getAddress()) + "\n\t\t\t" + txTmOwner.getmCity().getName() + " " + txTmOwner.getmProvince().getName();
                List<TxTmRepresentative> reprs = txTmReprsCustomRepository.selectTxTmReprsByGeneral(txTmGeneralPembanding.getId(), true);
                String kuasa = "";
                ArrayList<String> kuasaTemp = new ArrayList<String>();
                if (reprs.isEmpty()) {
                    kuasa = "-";
                } else {
                    for (TxTmRepresentative txTmReprsList : reprs) {
                        String city = txTmReprsList.getmCity() != null ? txTmReprsList.getmCity().getName() : "";
                        String province = txTmReprsList.getmProvince() != null ? txTmReprsList.getmProvince().getName() : "";
                        String country = txTmReprsList.getmCountry() != null ? txTmReprsList.getmCountry().getName() : "";
                        String txtmreprsAddr = txTmReprsList.getAddress() != null ? txTmReprsList.getAddress(): "";
                        txtmreprsAddr = txtmreprsAddr.trim(); //hapus spasi akhir
                        txtmreprsAddr = txtmreprsAddr.replaceAll("(\\r\\n|\\n)",""); //hapus enter

                        kuasaTemp.add(txTmReprsList.getName() + "\n\t\t\t" +  justifyAddressInfo(txtmreprsAddr,48)+
                                ((city + " " + province).trim().equals("") ? "" : city + " " + province ) +
                                country );
                    }
                    Set<String> temp = new LinkedHashSet<String>(kuasaTemp);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (reprs.size() > 0) {
                        kuasa = String.join("", unique);
                    }
                }
                List<TxTmPrior> txTmPrior = txTmPriorCustomRepository.selectTxTmPriorByTxTmGeneral(txTmGeneralPembanding.getId(),PriorStatusEnum.ACCEPT.name());
                String prioritas = "";
                ArrayList<String> priorTemp = new ArrayList<String>();
                if (txTmPrior.isEmpty()) {
                    prioritas = "-";
                } else {
                    for (TxTmPrior priors : txTmPrior) {
                        String country = priors.getmCountry().getName() != null ? priors.getmCountry().getName() : "";
                        priorTemp.add(priors.getNo() == null ? "" : priors.getNo() + " | " + fmt.format(priors.getPriorDate()) + " | "+country + "\n\t\t\t");
                    }
                    Set<String> tempPrior = new LinkedHashSet<String>(priorTemp);
                    String[] uniquePrior = tempPrior.toArray(new String[tempPrior.size()]);
                    if (txTmPrior.size() > 0) {
                        prioritas = String.join("", uniquePrior);
                    }
                }
                TxTmBrand txTmBrand = txTmBrandRepository.findTxTmBrandByTxTmGeneral(txTmGeneralPembanding);
                String merek = txTmBrand.getName();
                String logomerek = txTmBrand.getId()+ ".jpg";

                List<TxTmClass> txTmClasses = classService.selectAllClassByIdGeneral(txTmGeneralPembanding.getId(), ClassStatusEnum.ACCEPT.name());
                int key;
                String value = "";
                String desc = "";
                Map<Integer, String> mapClass = new HashMap<>();

                for (TxTmClass txTmClass : txTmClasses) {
                    MClassDetail mClassDetail = txTmClass.getmClassDetail();
                    key = txTmClass.getmClass().getNo();
                    desc = txTmClass.getmClassDetail().getDesc();

                    if (mapClass.containsKey(key)) {
                        value = mapClass.get(key);
                        value = value + "; " + desc.trim().replaceAll("(\\r\\n|\\n)","");
                    } else {
                        value = desc.trim().replaceAll("(\\r\\n|\\n)","");;
                    }

                    mapClass.put(key, value);

//                    kelasUraian = kelasUraian + mClassDetail.getDesc() + "; ";
//                    kelasUraian = kelasUraian.trim(); //hapus spasi akhir
//                    kelasUraian =kelasUraian.replaceAll("(\\r\\n|\\n)","");
                }

//                kelasUraian = kelasUraian.trim(); //hapus spasi akhir
//                if (kelasUraian.length() > 1) {
//                    kelasUraian = kelasUraian.substring(0, kelasUraian.length() - 1); //hapus koma akhir
//                }
//
//                kelasUraian = "  " + kelasUraian + ".";

                Map<Integer, String> mapClassOrder = mapClass.entrySet().stream().sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                StringBuffer sbClassNoList = new StringBuffer();
                StringBuffer sbClassDescList = new StringBuffer();
                for (Map.Entry<Integer, String> map : mapClassOrder.entrySet()) {
                    sbClassNoList.append(map.getKey());
                    sbClassNoList.append(", ");

                    sbClassDescList.append("\nKelas " + map.getKey() + " :\n");
                    sbClassDescList.append("=== ");
                    sbClassDescList.append(map.getValue());
                    sbClassDescList.append(" ===");
                    sbClassDescList.append("\n");
                }
                String classNoList = "";
                String classDescList = "";
                if (sbClassNoList.length() > 0) {
                    classNoList = sbClassNoList.substring(0, sbClassNoList.length() - 2);
                }
                if (sbClassDescList.length() > 0) {
                    classDescList = sbClassDescList.substring(0, sbClassDescList.length() - 1);
                }

                result = result +
                        "No Permohonan\t" + noPemohon + "\t" + "Tanggal Penerimaan\t" + tglPenerimaan + "\n" +
                        "No Pendaftaran\t" + noPendaftaran + "\t" + "Tanggal Pendaftaran\t" + tanggalPendaftaran + "\n" +
                        "Pemohon/Pemilik\t" + pemohon +"\n" +
                        "Kuasa\t\t\t" + kuasa +"\n" +
                        "Prioritas\t\t" + prioritas + "\n" +
                        "Merek\t\t\t" + merek + "\n" +
//                        "\t\t\t" + txTmBrand.getId() + ".jpg\n" +
                        "\t\t\t" + logomerek + "\n" +
//                        "Kelas\t\t\t" + classIDordered + "\n" +
                        "Barang/Jasa     " + classDescList + "\n"+
                        "-----------------------------------------------------------------------------------------------------------------------\n"
                ;
            }
        } else if (paramName.equalsIgnoreCase("<<similarity_result_madrid>>")) {
            List<TxSimilarityResult> txSimilarityResultList = txSimilarityResultRepository.findTxSimilarityResultByOriginTxTmGeneral(txTmGeneral);
            result = "";
            for (TxSimilarityResult txSimilarityResult : txSimilarityResultList) {
                TxTmGeneral txTmGeneralPembanding = txSimilarityResult.getSimilarTxTmGeneral();
                String noPemohon = txTmGeneralPembanding.getApplicationNo();
                String tglPenerimaan = "\t"+fmt.format(txTmGeneralPembanding.getFilingDate());
                TxRegistration txRegistration = txRegistrationRepository.findTxRegistrationByTxTmGeneral(txTmGeneralPembanding);
                String noPendaftaran = txRegistration != null ? txRegistration.getNo() : "-\t\t";
                String tanggalPendaftaran = txRegistration != null ? fmt.format(txRegistration.getStartDate()) : "-";
                TxTmOwner txTmOwner = txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneralPembanding, true);
                String cityOwner = txTmOwner.getmCity() != null ? txTmOwner.getmCity().getName() : "";
                String ownerName = txTmOwner.getName() != null ? txTmOwner.getName() : "";
                String ownerAddress = txTmOwner.getAddress() != null ? txTmOwner.getAddress() : "";
                String ownerProvince = txTmOwner.getmProvince() != null ? txTmOwner.getmProvince().getName() : "";
                String ownerCountry = txTmOwner.getmCountry() != null ? txTmOwner.getmCountry().getName() : "";

                ownerAddress = ownerAddress.trim(); //hapus spasi akhir
                ownerAddress = ownerAddress.replaceAll("(\\r\\n|\\n)", ""); //hapus enter
                String pemohon = ownerName + "\n\t\t\t\t" + justifyAddressInfoMadrid(ownerAddress, 48) + cityOwner + " " + ownerProvince+ " " + ownerCountry;
//                String pemohon = txTmOwner.getName() + "\n\t\t\t" + justifyAddressInfo(txTmOwner.getAddress()) + "\n\t\t\t" + txTmOwner.getmCity().getName() + " " + txTmOwner.getmProvince().getName();
                List<TxTmRepresentative> reprs = txTmReprsCustomRepository.selectTxTmReprsByGeneral(txTmGeneralPembanding.getId(), true);
                String kuasa = "";
                ArrayList<String> kuasaTemp = new ArrayList<String>();
                if (reprs.isEmpty()) {
                    kuasa = "-";
                } else {
                    for (TxTmRepresentative txTmReprsList : reprs) {
                        String city = txTmReprsList.getmCity() != null ? txTmReprsList.getmCity().getName() : "";
                        String province = txTmReprsList.getmProvince() != null ? txTmReprsList.getmProvince().getName() : "";
                        String country = txTmReprsList.getmCountry() != null ? txTmReprsList.getmCountry().getName() : "";
                        String txtmreprsAddr = txTmReprsList.getAddress() != null ? txTmReprsList.getAddress() : "";
                        txtmreprsAddr = txtmreprsAddr.trim(); //hapus spasi akhir
                        txtmreprsAddr = txtmreprsAddr.replaceAll("(\\r\\n|\\n)", ""); //hapus enter

                        kuasaTemp.add(txTmReprsList.getName() + "\n\t\t\t\t" + justifyAddressInfoMadrid(txtmreprsAddr, 48) +
                                ((city + " " + province).trim().equals("") ? "" : city + " " + province) +
                                country);
                    }
                    Set<String> temp = new LinkedHashSet<String>(kuasaTemp);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (reprs.size() > 0) {
                        kuasa = String.join("", unique);
                    }
                }
                List<TxTmPrior> txTmPrior = txTmPriorCustomRepository.selectTxTmPriorByTxTmGeneral(txTmGeneralPembanding.getId(), PriorStatusEnum.ACCEPT.name());
                String prioritas = "";
                ArrayList<String> priorTemp = new ArrayList<String>();
                if (txTmPrior.isEmpty()) {
                    prioritas = "-";
                } else {
                    for (TxTmPrior priors : txTmPrior) {
                        String country = priors.getmCountry().getName() != null ? priors.getmCountry().getName() : "";
                        priorTemp.add(priors.getNo() == null ? "" : priors.getNo() + " | " + fmt.format(priors.getPriorDate()) + " | " + country + "\n\t\t\t");
                    }
                    Set<String> tempPrior = new LinkedHashSet<String>(priorTemp);
                    String[] uniquePrior = tempPrior.toArray(new String[tempPrior.size()]);
                    if (txTmPrior.size() > 0) {
                        prioritas = String.join("", uniquePrior);
                    }
                }
                TxTmBrand txTmBrand = txTmBrandRepository.findTxTmBrandByTxTmGeneral(txTmGeneralPembanding);
                String merek = txTmBrand.getName();
                String logomerek = txTmBrand.getId() + ".jpg";

                List<TxTmClass> txTmClasses = classService.selectAllClassByIdGeneral(txTmGeneralPembanding.getId(), ClassStatusEnum.ACCEPT.name());
                int key;
                String value = "";
                String desc = "";
                Map<Integer, String> mapClass = new HashMap<>();

                for (TxTmClass txTmClass : txTmClasses) {
                    MClassDetail mClassDetail = txTmClass.getmClassDetail();
                    key = txTmClass.getmClass().getNo();
                    desc = txTmClass.getmClassDetail().getDesc();

                    if (mapClass.containsKey(key)) {
                        value = mapClass.get(key);
                        value = value + "; " + desc.trim().replaceAll("(\\r\\n|\\n)","");
                    } else {
                        value = desc.trim().replaceAll("(\\r\\n|\\n)","");
                    }

                    mapClass.put(key, value);

//                    kelasUraian = kelasUraian + mClassDetail.getDesc() + "; ";
//                    kelasUraian = kelasUraian.trim(); //hapus spasi akhir
//                    kelasUraian =kelasUraian.replaceAll("(\\r\\n|\\n)","");
                }

//                kelasUraian = kelasUraian.trim(); //hapus spasi akhir
//                if (kelasUraian.length() > 1) {
//                    kelasUraian = kelasUraian.substring(0, kelasUraian.length() - 1); //hapus koma akhir
//                }
//
//                kelasUraian = "  " + kelasUraian + ".";

                Map<Integer, String> mapClassOrder = mapClass.entrySet().stream().sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                StringBuffer sbClassNoList = new StringBuffer();
                StringBuffer sbClassDescList = new StringBuffer();
                for (Map.Entry<Integer, String> map : mapClassOrder.entrySet()) {
                    sbClassNoList.append(map.getKey());
                    sbClassNoList.append(", ");

                    sbClassDescList.append("\nKelas " + map.getKey() + " :\n");
                    sbClassDescList.append("=== ");
                    sbClassDescList.append(map.getValue());
                    sbClassDescList.append(" ===");
                    sbClassDescList.append("\n");
                }
                String classNoList = "";
                String classDescList = "";
                if (sbClassNoList.length() > 0) {
                    classNoList = sbClassNoList.substring(0, sbClassNoList.length() - 2);
                }
                if (sbClassDescList.length() > 0) {
                    classDescList = sbClassDescList.substring(0, sbClassDescList.length() - 1);
                }

                result = result +
                        "Application No         \t\t" + noPemohon + "\t" + "Filing Date\t" + tglPenerimaan + "\n" +
                        "Registration No        \t\t" + noPendaftaran + "\t" + "Registration Date\t" + tanggalPendaftaran + "\n" +
                        "Applicant/Registrant   \t\t" + pemohon + "\n" +
                        "Representative/TM Agent\t" + kuasa + "\n" +
                        "Priority Claim          \t\t" + prioritas + "\n" +
                        "Reproduction of Mark    \t" + merek + "\n" +
//                        "\t\t\t" + txTmBrand.getId() + ".jpg\n" +
                        "\t\t\t\t" + logomerek + "\n" +
//                        "Class(es)               \t\t" + classIDordered + "\n" +
                        "Class Description     " + classDescList + "\n" +
                        "-----------------------------------------------------------------------------------------------------------------------\n"
                ;
            }
        }
            return result;
    }

    public String setParamType(String paramName) {
        String result = "";
        if (paramName.equalsIgnoreCase("<<created_date>>")
                || paramName.equalsIgnoreCase("<<created_date_en>>")
                || paramName.equalsIgnoreCase("<<application_no>>")
                || paramName.equalsIgnoreCase("<<application_no_digit>>")
                || paramName.equalsIgnoreCase("<<application_no_digit_f>>")
                || paramName.equalsIgnoreCase("<<application_no_year>>")
                || paramName.equalsIgnoreCase("<<application_no_year_f>>")
                || paramName.equalsIgnoreCase("<<irn>>")
                || paramName.equalsIgnoreCase("<<current_year>>")
                || paramName.equalsIgnoreCase("<<tm_brand_name>>")
                || paramName.equalsIgnoreCase("<<qr_code>>")
                || paramName.equalsIgnoreCase("<<created_date_year>>")
                || paramName.equalsIgnoreCase("<<created_date_month>>")
                || paramName.equalsIgnoreCase("<<application_date>>")
                || paramName.equalsIgnoreCase("<<application_date_en>>")
                || paramName.equalsIgnoreCase("<<download_date>>")
                || paramName.equalsIgnoreCase("<<download_date_f>>")
                || paramName.equalsIgnoreCase("<<nama>>")
                || paramName.equalsIgnoreCase("<<alamat>>")
                || paramName.equalsIgnoreCase("<<namapasca>>")
                || paramName.equalsIgnoreCase("<<alamatpasca>>")
                || paramName.equalsIgnoreCase("<<kota>>")
                || paramName.equalsIgnoreCase("<<provinsi>>")
                || paramName.equalsIgnoreCase("<<negara>>")
                || paramName.equalsIgnoreCase("<<kodepos>>")
                || paramName.equalsIgnoreCase("<<created_by>>")
                || paramName.equalsIgnoreCase("<<created_by_f>>")
                || paramName.equalsIgnoreCase("<<filing_date>>")
                || paramName.equalsIgnoreCase("<<filing_date_en>>")
                || paramName.equalsIgnoreCase("<<tm_owner_name>>")
                || paramName.equalsIgnoreCase("<<tm_owner_address>>")
                || paramName.equalsIgnoreCase("<<tm_owner_address_only>>")
                || paramName.equalsIgnoreCase("<<tm_owner_address_ori>>")
                || paramName.equalsIgnoreCase("<<class_no_app>>")
                || paramName.equalsIgnoreCase("<<edisi_kelas>>")
                || paramName.equalsIgnoreCase("<<journal_no>>")
                || paramName.equalsIgnoreCase("<<journal_start>>")
                || paramName.equalsIgnoreCase("<<journal_end>>")
                || paramName.equalsIgnoreCase("<<tm_class_detail>>")
                || paramName.equalsIgnoreCase("<<class_detail_reject>>")
                || paramName.equalsIgnoreCase("<<class_no_reject>>")
                || paramName.equalsIgnoreCase("<<tx_similarity_result>>")
                || paramName.equalsIgnoreCase("<<similarity_result_madrid>>")
                // Parameter Permohonan Pasca
                || paramName.equalsIgnoreCase("<<label_merek>>")
                || paramName.equalsIgnoreCase("<<tm_prior_no>>")
                || paramName.equalsIgnoreCase("<<tm_prior_date>>")
                || paramName.equalsIgnoreCase("<<tm_prior_country>>")
                || paramName.equalsIgnoreCase("<<reg_no>>")
                || paramName.equalsIgnoreCase("<<tm_old_owner_name>>")
                || paramName.equalsIgnoreCase("<<tm_old_owner_address>>")
                || paramName.equalsIgnoreCase("<<reprs_name>>")
                || paramName.equalsIgnoreCase("<<reprs_address>>")
                || paramName.equalsIgnoreCase("<<post_reprs_name>>")
                || paramName.equalsIgnoreCase("<<post_reprs_address>>")
                || paramName.equalsIgnoreCase("<<post_owner_name>>")
                || paramName.equalsIgnoreCase("<<post_owner_address>>")
                || paramName.equalsIgnoreCase("<<reg_start_date>>")
                || paramName.equalsIgnoreCase("<<expiration_date>>")
                || paramName.equalsIgnoreCase("<<tm_brand_color>>")
                || paramName.equalsIgnoreCase("<<tm_brand_description>>")
                || paramName.equalsIgnoreCase("<<tm_brand_translation>>")
                || paramName.equalsIgnoreCase("<<class_no_all>>")
                || paramName.equalsIgnoreCase("<<dirjen_name>>")
                || paramName.equalsIgnoreCase("<<dirjen_nip>>")
                || paramName.equalsIgnoreCase("<<dirmerek_name>>")
                || paramName.equalsIgnoreCase("<<dirmerek_nip>>")
                || paramName.equalsIgnoreCase("<<license_name>>")
                || paramName.equalsIgnoreCase("<<license_address>>")
                || paramName.equalsIgnoreCase("<<sub_license_main_name>>")
                || paramName.equalsIgnoreCase("<<sub_license_main_address>>")
                || paramName.equalsIgnoreCase("<<sub_license_child_name>>")
                || paramName.equalsIgnoreCase("<<sub_license_child_address>>")
                || paramName.equalsIgnoreCase("<<status_permohonan>>")
                || paramName.equalsIgnoreCase("<<created_by_sign>>")
                || paramName.equalsIgnoreCase("<<nomor_dokumen>>")
                || paramName.equalsIgnoreCase("<<catatan_monitor>>")
                || paramName.equalsIgnoreCase("<<post_reception_date>>")
                ) {
            result = ParamType.TERDEFINISI.toString();
        } else if (paramName.equalsIgnoreCase("<<penjelasan>>")
                || paramName.equalsIgnoreCase("<<nomor_surat>>")
                || paramName.equalsIgnoreCase("<<nama_pemohon_keberatan>>")
                || paramName.equalsIgnoreCase("<<alamat_pemohon_keberatan>>")
                || paramName.equalsIgnoreCase("<<nama_kuasa_keberatan>>")
                || paramName.equalsIgnoreCase("<<alamat_kuasa_keberatan>>")
                || paramName.equalsIgnoreCase("<<nama_pemeriksa>>")
                || paramName.equalsIgnoreCase("<<data_mutasi>>")
                || paramName.equalsIgnoreCase("<<document_no_digit>>")
                || paramName.equalsIgnoreCase("<<document_no_year>>")
                ) {
            result = ParamType.TEKS_BEBAS.toString();
        } else if (paramName.equalsIgnoreCase("<<doc_type_name>>")
                || paramName.equalsIgnoreCase("<<jenis_barang_jasa>>")
                || paramName.equalsIgnoreCase("<<class_desc_en>>")
                || paramName.equalsIgnoreCase("<<class_desc_accept>>")
                || paramName.equalsIgnoreCase("<<class_desc_reject>>")
                || paramName.equalsIgnoreCase("<<class_desc_correction>>")
                ) {
            result = ParamType.CHECKBOX.toString();
        } else if (paramName.equalsIgnoreCase("<<usulan_penolakan>>")
                || paramName.equalsIgnoreCase("<<alasan_penolakan>>")
                || paramName.equalsIgnoreCase("<<alasan_penolakan_madrid>>")
                || paramName.equalsIgnoreCase("<<status_keberatan>>")
                || paramName.equalsIgnoreCase("<<status_tanggapan>>")
                || paramName.equalsIgnoreCase("<<jenis_perubahan>>")
                || paramName.equalsIgnoreCase("<<invalid_madrid>>")
                || paramName.equalsIgnoreCase("<<reason_madrid>>")
                || paramName.equalsIgnoreCase("<<usulan_penolakan_madrid>>")
                || paramName.equalsIgnoreCase("<<class_no>>")
                ) {
            result = ParamType.DROPDOWN.toString();
        } else if (paramName.equalsIgnoreCase("<<tanggal_surat>>")
                || paramName.equalsIgnoreCase("<<tanggal_sidang>>")
                || paramName.equalsIgnoreCase("<<tanggal_putusan>>")
                || paramName.equalsIgnoreCase("<<tanggal_pelaksanaan_putusan>>")
                || paramName.equalsIgnoreCase("<<tanggal_dokumen_pasca>>")
                || paramName.equalsIgnoreCase("<<tanggal_banding>>")
                || paramName.equalsIgnoreCase("<<tanggal_kirim_surat>>")
                || paramName.equalsIgnoreCase("<<tanggal_pengantar_putusan>>")
                ) {
            result = ParamType.PILIH_TANGGAL.toString();
        } else {
            result = ParamType.TEKS_BEBAS.toString();
        }

        return result;
    }

    @Transactional
    public void doSaveOrUpdate(TxDocumentParams t) {
        txDocumentParamCustomRepository.saveOrUpdate(t);
    }

    public TxDocumentParams selectOneBy(String by, String value) {
        return txDocumentParamCustomRepository.selectOne(by, value, true);
    }

    private static String justifyAddressInfo(String address, int limit){
        StringBuilder justifiedText = new StringBuilder();
        StringBuilder justifiedLine = new StringBuilder();
        String[] words = address.split(" ");
        for (int i = 0; i < words.length; i++) {
            justifiedLine.append(words[i]).append(" ");
            if (i+1 == words.length || justifiedLine.length() + words[i+1].length() > limit) {
                justifiedLine.deleteCharAt(justifiedLine.length() - 1);
                justifiedText.append(justifiedLine.toString());
                justifiedText.append("\n\t\t\t");
                justifiedLine = new StringBuilder();
            }
        }

        return justifiedText.toString();
    }
    private static String justifyAddressInfoMadrid(String address, int limit){
        StringBuilder justifiedText = new StringBuilder();
        StringBuilder justifiedLine = new StringBuilder();
        String[] words = address.split(" ");
        for (int i = 0; i < words.length; i++) {
            justifiedLine.append(words[i]).append(" ");
            if (i+1 == words.length || justifiedLine.length() + words[i+1].length() > limit) {
                justifiedLine.deleteCharAt(justifiedLine.length() - 1);
                justifiedText.append(justifiedLine.toString());
                justifiedText.append("\n\t\t\t\t");
                justifiedLine = new StringBuilder();
            }
        }

        return justifiedText.toString();
    }
}
