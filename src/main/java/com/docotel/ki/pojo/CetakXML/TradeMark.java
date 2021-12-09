package com.docotel.ki.pojo.CetakXML;

import javax.xml.bind.annotation.*;

public class TradeMark {

    private String RegistrationOfficeCode;  // ID (NEGARA YANG MENGAJUKAN]
    private String ApplicationNumber;       // TX_TM_GENERAL.APPLICATION_NO (TANPA D/J 00 YYYY, HANYA NOMOR TERAKHIR)
    private String ApplicationDate;         // TX_RECEPTION.APPLICATION_DATE
    private String RegistrationDate;        // TX_REGISTRATION.REG_START_DATE
    private String ApplicationLanguageCode; // ID (HARDCODE)]
    private String ExpiryDate;              // TX_REGISTRATION.EXPIRATION_DATE]
    private String MarkCurrentStatusDate;   // TX_MONITOR.CREATED_DATE PALING TERAKHIR]
    private String KindMark;                // TX_TM_OWNER.OWNER_TYPE, JIKA Perorangan=Individual, Badan Hukum=Corporation]
    private Other.MarkImageDetails MarkImageDetails;
    private Other.GoodsServicesDetails GoodsServicesDetails;
    private Other.PublicationDetails PublicationDetails;
    private Other.ApplicantDetails ApplicantDetails;
    private Other.RepresentativeDetails RepresentativeDetails;
    private Other.MarkEventDetails MarkEventDetails;
    private String MarkCurrentStatusCode;
    private Other.WordMarkSpecification WordMarkSpecification;

    public Other.WordMarkSpecification getWordMarkSpecification() {
        return WordMarkSpecification;
    }

    @XmlElement(name="wo:WordMarkSpecification")
    public void setWordMarkSpecification(Other.WordMarkSpecification wordMarkSpecification) {
        WordMarkSpecification = wordMarkSpecification;
    }

    public String getApplicationNumber() {
        return ApplicationNumber;
    }

    @XmlElement(name="ApplicationNumber")
    public void setApplicationNumber(String applicationNumber) {
        ApplicationNumber = applicationNumber;
    }

    @XmlElement(name="RegistrationOfficeCode")
    public void setRegistrationOfficeCode(String registrationOfficeCode) {
        this.RegistrationOfficeCode = registrationOfficeCode;
    }

    public String getRegistrationOfficeCode() {
        return RegistrationOfficeCode;
    }

    @XmlElement(name="ApplicationDate")
    public void setApplicationDate(String applicationDate) {
        this.ApplicationDate = applicationDate;
    }

    public String getApplicationDate() {
        return ApplicationDate;
    }

    @XmlElement(name="RegistrationDate")
    public void setRegistrationDate(String registrationDate) {
        this.RegistrationDate = registrationDate;
    }

    public String getRegistrationDate() {
        return RegistrationDate;
    }

    @XmlElement(name="ApplicationLanguageCode")
    public void setApplicationLanguageCode(String applicationLanguageCode) {
        this.ApplicationLanguageCode = applicationLanguageCode;
    }

    public String getApplicationLanguageCode() {
        return ApplicationLanguageCode;
    }

    @XmlElement(name="ExpiryDate")
    public void setExpiryDate(String expiryDate) {
        this.ExpiryDate = expiryDate;
    }

    public String getExpiryDate() {
        return ExpiryDate;
    }

    @XmlElement(name="KindMark")
    public void setKindMark(String kindMark) {
        this.KindMark = kindMark;
    }

    public String getKindMark() {
        return KindMark;
    }

    @XmlElement(name="MarkCurrentStatusDate")
    public void setMarkCurrentStatusDate(String markCurrentStatusDate) {
        this.MarkCurrentStatusDate = markCurrentStatusDate;
    }

    public String getMarkCurrentStatusDate() {
        return MarkCurrentStatusDate;
    }

    public Other.MarkImageDetails getMarkImageDetails() {
        return MarkImageDetails;
    }

    @XmlElement(name="MarkImageDetails")
    public void setMarkImageDetails(Other.MarkImageDetails markImageDetails) {
        this.MarkImageDetails = markImageDetails;
    }

    public Other.GoodsServicesDetails getGoodsServicesDetails() {
        return GoodsServicesDetails;
    }

    @XmlElement(name="GoodsServicesDetails")
    public void setGoodsServicesDetails(Other.GoodsServicesDetails goodsServicesDetails) {
        GoodsServicesDetails = goodsServicesDetails;
    }

    public Other.PublicationDetails getPublicationDetails() {
        return PublicationDetails;
    }

    @XmlElement(name="PublicationDetails")
    public void setPublicationDetails(Other.PublicationDetails publicationDetails) {
        PublicationDetails = publicationDetails;
    }

    public Other.ApplicantDetails getApplicantDetails() {
        return ApplicantDetails;
    }

    @XmlElement(name="ApplicantDetails")
    public void setApplicantDetails(Other.ApplicantDetails applicantDetails) {
        ApplicantDetails = applicantDetails;
    }

    public Other.RepresentativeDetails getRepresentativeDetails() {
        return RepresentativeDetails;
    }

    @XmlElement(name="RepresentativeDetails")
    public void setRepresentativeDetails(Other.RepresentativeDetails representativeDetails) {
        RepresentativeDetails = representativeDetails;
    }

    public Other.MarkEventDetails getMarkEventDetails() {
        return MarkEventDetails;
    }

    @XmlElement(name="wo:MarkEventDetails")
    public void setMarkEventDetails(Other.MarkEventDetails markEventDetails) {
        MarkEventDetails = markEventDetails;
    }

    public String getMarkCurrentStatusCode() {
        return MarkCurrentStatusCode;
    }

    @XmlElement(name="wo:MarkCurrentStatusCode")
    public void setMarkCurrentStatusCode(String markCurrentStatusCode) {
        MarkCurrentStatusCode = markCurrentStatusCode;
    }

}