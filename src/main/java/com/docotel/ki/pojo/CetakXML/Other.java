package com.docotel.ki.pojo.CetakXML;

import com.beust.jcommander.internal.Lists;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.repository.custom.transaction.TxMonitorCustomRepository;
import com.docotel.ki.repository.transaction.TxPubsJournalRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

public class Other {

    @Autowired
    TxMonitorCustomRepository txMonitorCustomRepository;
    @Autowired
    private TxPubsJournalRepository txPubsJournalRepository;

    public PublicationDetails getPublicationDetails(TxTmGeneral result) {
        List<TxGroupDetail> groupDetails = result.getTxGroupDetail();
        PublicationDetails publicationDetails = new PublicationDetails();
        List<Publication> publications = new ArrayList<>();
        for (TxGroupDetail groupDetail : groupDetails) {
            if (groupDetail == null || groupDetail.getTxGroup() == null){
                continue;
            }
            try {
                TxPubsJournal journal = txPubsJournalRepository.findTxPubsJournalByTxGroup(groupDetail.getTxGroup());
                Publication publication = new Publication();
                if (journal != null) {
                    publication.setPublicationDate(journal.getJournalStart().toString());
                    publication.setPublicationIdentifier(journal.getJournalNo());
                    publications.add(publication);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        publicationDetails.setPublication(publications);
        return publicationDetails;
    }

    public RepresentativeDetails getRepresentativeDetails(TxTmGeneral result) {
        List<TxTmRepresentative> reps = Lists.newArrayList(result.getTxTmRepresentative());
        RepresentativeDetails representativeDetails = new RepresentativeDetails();
        List<Representative> representatives = new ArrayList<>();
        for(TxTmRepresentative rep : reps) {
            Representative representative = new Representative();
            RepresentativeAddressBook representativeAddressBook = new RepresentativeAddressBook();
            FreeFormatNameDetails freeFormatNameDetails = new FreeFormatNameDetails();
            FormattedNameAddress formattedNameAddress = new FormattedNameAddress();
            FreeFormatName freeFormatName = new FreeFormatName();
            FreeFormatAddress freeFormatAddress = new FreeFormatAddress();
            FreeFormatNameLine freeFormatNameLine = new FreeFormatNameLine();

            Name name = new Name();
            freeFormatNameLine.setLanguageCode(rep.getmCountry().getCode());
            freeFormatNameLine.setFreeFormatNameLine(rep.getName());
            freeFormatNameDetails.setFreeFormatNameLine(freeFormatNameLine);
            freeFormatName.setFreeFormatNameDetails(freeFormatNameDetails);
            name.setFreeFormatName(freeFormatName);
            formattedNameAddress.setName(name);

            Address address = new Address();
            freeFormatAddress.setFreeFormatAddressLine(rep.getAddress());
            address.setFreeFormatAddress(freeFormatAddress);
            address.setAddressCountryCode(rep.getmCountry().getCode());
            formattedNameAddress.setAddress(address);

            representativeAddressBook.setFormattedNameAddress(formattedNameAddress);
            representative.setRepresentativeAddressBook(representativeAddressBook);
            representatives.add(representative);
        }
        representativeDetails.setRepresentative(representatives);
        return representativeDetails;
    }

    public MarkEventDetails getMarkEventDetails(TxTmGeneral result){
        List<TxMonitor> monitors = new ArrayList<>();
        MarkEventDetails markEventDetails = new MarkEventDetails();
        try {
            monitors = txMonitorCustomRepository.findByTxTmGeneralId(result.getId());
            for (TxMonitor monitor : monitors) {
                if(monitor == null) {
                    continue;
                }
                MarkEvent markEvent = new MarkEvent();
                markEvent.setMarkEventCode(monitor.getId());
                markEvent.setMarkEventDate(monitor.getCreatedDate().toString());
                markEvent.setOfficeSpecificMarkEventName(monitor.getNotes());
            }
        } catch(Exception e){
            //e.printStackTrace();
        }
        return markEventDetails;
    }

    public ApplicantDetails getApplicantDetails(TxTmGeneral result) {
        ApplicantDetails applicantDetails = new ApplicantDetails();
        List<TxTmOwner> owners = Lists.newArrayList(result.getTxTmOwner());
        List<Applicant> applicants = new ArrayList<>();
        for(TxTmOwner owner: owners) {
            Applicant applicant = new Applicant();
            ApplicantAddressBook applicantAddressBook = new ApplicantAddressBook();
            FreeFormatNameDetails freeFormatNameDetails = new FreeFormatNameDetails();
            FormattedNameAddress formattedNameAddress = new FormattedNameAddress();
            FreeFormatName freeFormatName = new FreeFormatName();
            FreeFormatAddress freeFormatAddress = new FreeFormatAddress();
            FreeFormatNameLine freeFormatNameLine = new FreeFormatNameLine();

            Name name = new Name();

            freeFormatNameLine.setFreeFormatNameLine(owner.getName());
            freeFormatNameLine.setLanguageCode(owner.getmCountry().getCode());
            freeFormatNameDetails.setFreeFormatNameLine(freeFormatNameLine);
            freeFormatName.setFreeFormatNameDetails(freeFormatNameDetails);
            name.setFreeFormatName(freeFormatName);
            formattedNameAddress.setName(name);

            Address address = new Address();
            freeFormatAddress.setFreeFormatAddressLine(owner.getAddress());
            address.setFreeFormatAddress(freeFormatAddress);
            address.setAddressCountryCode(owner.getmCountry().getCode());
            formattedNameAddress.setAddress(address);
            applicantAddressBook.setFormattedNameAddress(formattedNameAddress);
            applicant.setApplicantAddressBook(applicantAddressBook);
            applicants.add(applicant);
        }
        applicantDetails.setApplicant(applicants);
        return applicantDetails;
    }

    public GoodsServicesDetails getGoodsServicesDetails(TxTmGeneral result) {
        List<TxTmClass> kelas = result.getTxTmClassList();
        GoodsServicesDetails goodsServicesDetails = new GoodsServicesDetails();
        GoodsServices goodsServices = new GoodsServices();
        ClassDescriptionDetails classDescriptionDetails = new ClassDescriptionDetails();
        List<ClassDescription> classDescriptions = new ArrayList<>();
        List<Integer> ignoreClass = new ArrayList<>();
        for(TxTmClass txTmClass: kelas) {
            if(ignoreClass.contains(txTmClass.getmClass().getNo())){
                continue;
            }
            ClassDescription classDescription = new ClassDescription();
            classDescription.setClassNumber(txTmClass.getmClass().getNo().toString());
            classDescription.setGoodsServicesDescription(txTmClass.getmClass().getDesc());
            classDescriptions.add(classDescription);
            ignoreClass.add(txTmClass.getmClass().getNo());
        }
        classDescriptionDetails.setClassDescription(classDescriptions);
        goodsServices.setClassDescriptionDetails(classDescriptionDetails);
        goodsServicesDetails.setGoodsServices(goodsServices);
        return goodsServicesDetails;
    }

    public MarkImageDetails getMarkImageDetails(TxTmGeneral result) {
        MarkImageDetails markImageDetails = new MarkImageDetails();
        MarkImage markImage = new MarkImage();
        markImage.setMarkImageFileFormat("JPG");
        if(result.getTxTmBrand()!=null) {
            markImage.setMarkImageFilename(result.getTxTmBrand().getFileName());
        } else{
            markImage.setMarkImageFilename("-");
        }

        MarkImageCategory markImageCategory = new MarkImageCategory();
        markImageCategory.setCategoryKind("TIDAK ADA");
        markImageCategory.setCategoryVersion("TIDAK ADA");

        CategoryCodeDetails categoryCodeDetails = new CategoryCodeDetails();
        List<String> categories = new ArrayList<String>();
        categories.add("TIDAK ADA");
        categoryCodeDetails.setCategoryCode(categories);

        markImageCategory.setCategoryCodeDetails(categoryCodeDetails);
        markImage.setMarkImageCategory(markImageCategory);
        markImageDetails.setMarkImage(markImage);
        return markImageDetails;
    }

    public WordMarkSpecification getWordMarkSpecification(TxTmGeneral result) {
        WordMarkSpecification wordMarkSpecification = new WordMarkSpecification();
        if(result.getTxTmBrand()==null) {
            wordMarkSpecification.setMarkSignificantVerbalElement("-");
        }else {
            wordMarkSpecification.setMarkSignificantVerbalElement(result.getTxTmBrand().getDescription());
        }
        return wordMarkSpecification;
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class MarkImageDetails{
        private MarkImage MarkImage;
        public MarkImage getMarkImage() {
            return MarkImage;
        }

        @XmlElement(name="MarkImage")
        public void setMarkImage(MarkImage markImage) {
            MarkImage = markImage;
        }

    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class MarkImage{
        private String MarkImageFilename; //TX_TM_BRAND.FILE_NAME
        private String MarkImageFileFormat; //JPG
        private MarkImageCategory MarkImageCategory;

        public String getMarkImageFilename() {
            return MarkImageFilename;
        }

        @XmlElement(name="MarkImageFilename")
        public void setMarkImageFilename(String markImageFilename) {
            MarkImageFilename = markImageFilename;
        }

        public String getMarkImageFileFormat() {
            return MarkImageFileFormat;
        }

        @XmlElement(name="MarkImageFileFormat")
        public void setMarkImageFileFormat(String markImageFileFormat) {
            MarkImageFileFormat = markImageFileFormat;
        }

        public MarkImageCategory getMarkImageCategory() {
            return MarkImageCategory;
        }

        @XmlElement(name="MarkImageCategory")
        public void setMarkImageCategory(MarkImageCategory markImageCategory) {
            MarkImageCategory = markImageCategory;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class MarkImageCategory {
        private String CategoryKind;
        private String CategoryVersion;
        private CategoryCodeDetails CategoryCodeDetails;

        public String getCategoryKind() {
            return CategoryKind;
        }

        @XmlElement(name="CategoryKind")
        public void setCategoryKind(String categoryKind) {
            CategoryKind = categoryKind;
        }

        public String getCategoryVersion() {
            return CategoryVersion;
        }

        @XmlElement(name="CategoryVersion")
        public void setCategoryVersion(String categoryVersion) {
            CategoryVersion = categoryVersion;
        }

        public CategoryCodeDetails getCategoryCodeDetails() {
            return CategoryCodeDetails;
        }

        @XmlElement(name="CategoryCodeDetails")
        public void setCategoryCodeDetails(CategoryCodeDetails categoryCodeDetails) {
            CategoryCodeDetails = categoryCodeDetails;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class CategoryCodeDetails {
        public List<String> CategoryCode;

        public List<String> getCategoryCode() {
            return CategoryCode;
        }

        @XmlElement(name="CategoryCode")
        public void setCategoryCode(List<String> categoryCodes) {
            CategoryCode = categoryCodes;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class GoodsServicesDetails {
        public GoodsServices GoodsServices;

        public GoodsServices getGoodsServices() {
            return GoodsServices;
        }

        @XmlElement(name="GoodsServices")
        public void setGoodsServices(GoodsServices goodsServices) {
            GoodsServices = goodsServices;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class GoodsServices {
        public ClassDescriptionDetails ClassDescriptionDetails;

        public ClassDescriptionDetails getClassDescriptionDetails() {
            return ClassDescriptionDetails;
        }

        @XmlElement(name="ClassDescriptionDetails")
        public void setClassDescriptionDetails(ClassDescriptionDetails classDescriptionDetails) {
            ClassDescriptionDetails = classDescriptionDetails;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class ClassDescriptionDetails {
        public List<ClassDescription> ClassDescription;

        public List<ClassDescription> getClassDescription() {
            return ClassDescription;
        }

        @XmlElement(name="ClassDescription")
        public void setClassDescription(List<ClassDescription> classDescriptions) {
            ClassDescription = classDescriptions;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class ClassDescription {
        private String ClassNumber; //TX_TM_CLASS.CLASS_ID JOIN KE M_CLASS.CLASS_NO]
        private String GoodsServicesDescription; //TX_TM_CLASS.CLASS_DETAIL_ID JOIN KE M_CLASS_DETAIL.CLASS_DESC]

        public String getClassNumber() {
            return ClassNumber;
        }

        @XmlElement(name="ClassNumber")
        public void setClassNumber(String classNumber) {
            ClassNumber = classNumber;

        }

        public String getGoodsServicesDescription() {
            return GoodsServicesDescription;
        }

        @XmlElement(name="GoodsServicesDescription")
        public void setGoodsServicesDescription(String goodsServicesDescription) {
            GoodsServicesDescription = goodsServicesDescription;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class PublicationDetails {
        private List<Publication> Publication;

        public List<Publication> getPublication() {
            return Publication;
        }

        @XmlElement(name="Publication")
        public void setPublication(List<Publication> publication) {
            Publication = publication;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class Publication {
        private String PublicationDate; //TX_PUBS_JOURNAL.JOURNAL_START_DATE]
        private String PublicationIdentifier; //TX_PUBS_JOURNAL.JOURNAL_NO]

        public String getPublicationDate() {
            return PublicationDate;
        }

        @XmlElement(name="PublicationDate")
        public void setPublicationDate(String publicationDate) {
            PublicationDate = publicationDate;
        }

        public String getPublicationIdentifier() {
            return PublicationIdentifier;
        }

        @XmlElement(name="PublicationIdentifier")
        public void setPublicationIdentifier(String publicationIdentifier) {
            PublicationIdentifier = publicationIdentifier;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class ApplicantDetails {
        private List<Applicant> Applicant;

        public List<Applicant> getApplicant() {
            return Applicant;
        }

        @XmlElement(name="Applicant")
        public void setApplicant(List<Applicant> applicants) {
            Applicant = applicants;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class Applicant {
        private ApplicantAddressBook ApplicantAddressBook;

        public ApplicantAddressBook getApplicantAddressBook() {
            return ApplicantAddressBook;
        }

        @XmlElement(name="ApplicantAddressBook")
        public void setApplicantAddressBook(ApplicantAddressBook applicantAddressBook) {
            ApplicantAddressBook = applicantAddressBook;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class ApplicantAddressBook {
        private FormattedNameAddress FormattedNameAddress;

        public FormattedNameAddress getFormattedNameAddress() {
            return FormattedNameAddress;
        }

        @XmlElement(name="FormattedNameAddress")
        public void setFormattedNameAddress(FormattedNameAddress formattedNameAddress) {
            FormattedNameAddress = formattedNameAddress;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class FormattedNameAddress {
        private Name Name;
        private Address Address;

        public Name getName() {
            return Name;
        }

        @XmlElement(name="Name")
        public void setName(Name name) {
            Name = name;
        }

        public Address getAddress() {
            return Address;
        }

        @XmlElement(name="Address")
        public void setAddress(Address address) {
            Address = address;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class Name {
        private FreeFormatName FreeFormatName;

        public FreeFormatName getFreeFormatName() {
            return FreeFormatName;
        }

        @XmlElement(name="FreeFormatName")
        public void setFreeFormatName(FreeFormatName freeFormatName) {
            FreeFormatName = freeFormatName;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class FreeFormatName {
        private FreeFormatNameDetails FreeFormatNameDetails;

        public FreeFormatNameDetails getFreeFormatNameDetails() {
            return FreeFormatNameDetails;
        }

        @XmlElement(name="FreeFormatNameDetails")
        public void setFreeFormatNameDetails(FreeFormatNameDetails freeFormatNameDetails) {
            FreeFormatNameDetails = freeFormatNameDetails;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class FreeFormatNameDetails {
        private FreeFormatNameLine FreeFormatNameLine; //TX_TM_REPRS.REPRS_NAME]

        public FreeFormatNameLine getFreeFormatNameLine() {
            return FreeFormatNameLine;
        }

        @XmlElement(name="FreeFormatNameLine")
        public void setFreeFormatNameLine(FreeFormatNameLine freeFormatNameLine) {
            FreeFormatNameLine = freeFormatNameLine;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class FreeFormatNameLine {
        private String FreeFormatNameLine;
        private String languageCode;

        public String getFreeFormatNameLine() {
            return FreeFormatNameLine;
        }

        @XmlValue()
        public void setFreeFormatNameLine(String freeFormatNameLine) {
            FreeFormatNameLine = freeFormatNameLine;
        }


        public String getLanguageCode() {
            return languageCode;
        }

        @XmlAttribute(name="languageCode")
        public void setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
        }

    }


    @XmlAccessorType(XmlAccessType.NONE)
    public static class Address {
        private FreeFormatAddress FreeFormatAddress;
        private String AddressCountryCode;

        public String getAddressCountryCode() {
            return AddressCountryCode;
        }

        @XmlElement(name="AddressCountryCode")
        public void setAddressCountryCode(String addressCountryCode) {
            AddressCountryCode = addressCountryCode;
        }

        public FreeFormatAddress getFreeFormatAddress() {
            return FreeFormatAddress;
        }

        @XmlElement(name="FreeFormatAddress")
        public void setFreeFormatAddress(FreeFormatAddress freeFormatAddress) {
            FreeFormatAddress = freeFormatAddress;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class FreeFormatAddress {
        private String FreeFormatAddressLine;  //TX_TM_REPRS.REPRS_ADDRESS]

        public String getFreeFormatAddressLine() {
            return FreeFormatAddressLine;
        }

        @XmlElement(name="FreeFormatAddressLine")
        public void setFreeFormatAddressLine(String freeFormatAddressLine) {
            FreeFormatAddressLine = freeFormatAddressLine;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class RepresentativeDetails {
        private List<Representative> Representative;

        public List<Representative> getRepresentative() {
            return Representative;
        }

        @XmlElement(name="Representative")
        public void setRepresentative(List<Representative> representatives) {
            Representative = representatives;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class Representative {
        private RepresentativeAddressBook RepresentativeAddressBook;

        public RepresentativeAddressBook getRepresentativeAddressBook() {
            return RepresentativeAddressBook;
        }

        @XmlElement(name="RepresentativeAddressBook")
        public void setRepresentativeAddressBook(RepresentativeAddressBook representativeAddressBook) {
            RepresentativeAddressBook = representativeAddressBook;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class RepresentativeAddressBook {
        private FormattedNameAddress FormattedNameAddress;

        public FormattedNameAddress getFormattedNameAddress() {
            return FormattedNameAddress;
        }

        @XmlElement(name="FormattedNameAddress")
        public void setFormattedNameAddress(FormattedNameAddress formattedNameAddress) {
            FormattedNameAddress = formattedNameAddress;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class WordMarkSpecification{
        private String MarkSignificantVerbalElement; // TX_TM_BRAND.DESCRIPTION]

        public String getMarkSignificantVerbalElement() {
            return MarkSignificantVerbalElement;
        }

        @XmlElement(name="wo:MarkSignificantVerbalElement")
        public void setMarkSignificantVerbalElement(String markSignificantVerbalElement) {
            MarkSignificantVerbalElement = markSignificantVerbalElement;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class MarkEventDetails {
        private List<MarkEvent> markEvent;

        public List<MarkEvent> getMarkEvent() {
            return markEvent;
        }

        @XmlElement(name="MarkEvent")
        public void setMarkEvent(List<MarkEvent> markEvent) {
            this.markEvent = markEvent;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class MarkEvent {
        private String MarkEventDate;
        private String MarkEventCode;
        private String OfficeSpecificMarkEventName;

        public String getMarkEventDate() {
            return MarkEventDate;
        }

        @XmlElement(name="wo:MarkEventDate")
        public void setMarkEventDate(String markEventDate) {
            MarkEventDate = markEventDate;
        }

        public String getMarkEventCode() {
            return MarkEventCode;
        }

        @XmlElement(name="wo:MarkEventCode")
        public void setMarkEventCode(String markEventCode) {
            MarkEventCode = markEventCode;
        }

        public String getOfficeSpecificMarkEventName() {
            return OfficeSpecificMarkEventName;
        }

        @XmlElement(name="wo:OfficeSpecificMarkEventName")
        public void setOfficeSpecificMarkEventName(String officeSpecificMarkEventName) {
            OfficeSpecificMarkEventName = officeSpecificMarkEventName;
        }
    }
}
