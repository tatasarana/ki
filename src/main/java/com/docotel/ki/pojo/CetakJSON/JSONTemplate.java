package com.docotel.ki.pojo.CetakJSON;

// JSON template
        /*
        {
            "results":[
                {
                    "no_permohonan":"TX_TM_GENERAL.APPLICATION_NO",
                    "no_sertifikat":".REG_NO",
                    "publication_nbr":"TX_PUBTX_REGISTRATIONS_JOURNAL.JOURNAL_NO",
                    "pemohon":"TX_TM_OWNER.TM_OWNER_NAME",
                    "title":"",
                    "reception_date":"TX_RECEPTION.APPLICATION_DATE",
                    "filing_date":"TX_TM_GENERAL.FILING_DATE",
                    "registration_date":"TX_REGISTRATION.REG_START_DATE",
                    "expiration_date":"TX_REGISTRATION.EXPIRATION_DATE",
                    "status":"TX_TM_GENERAL.STATUS_ID JOIN KE M_STATUS.STATUS_NAME",
                    "publication_date":"TX_PUBS_JOURNAL.JOURNAL_START_DATE",
                    "entitlement_date":"0000-00-00",
                    "translation":"TX_TM_BRAND.TM_BRAND_TRANSLATION",
                    "jenis":"TX_TM_BRAND.BRAND_TYPE_ID JOIN KE M_BRAND_TYPE.BRAND_DESCRIPTION",
                    "nice_class_code":"TX_TM_CLASS.CLASS_ID JOIN KE M_CLASS.CLASS_NO",
                    "nice_class_descr":"(TX_TM_CLASS.CLASS_DETAIL_ID JOIN KE M_CLASS_DETAIL.CLASS_ID) TX_TM_CLASS.CLASS_DETAIL_ID JOIN KE M_CLASS_DETAIL.CLASS_DESC"
                }
            ]
        }
        * */

public class JSONTemplate {
    String no_permohonan;
    String no_sertifikat;
    String publication_nbr;
    String pemohon;
    String title;
    String reception_date;
    String filing_date;
    String registration_date;
    String expiration_date;
    String status;
    String publication_date;
    String entitlement_date;
    String translation;
    String jenis;
    String nice_class_code;
    String nice_class_descr;

    public String getNo_permohonan() {
        return no_permohonan;
    }

    public void setNo_permohonan(String no_permohonan) {
        this.no_permohonan = no_permohonan;
    }

    public String getNo_sertifikat() {
        return no_sertifikat;
    }

    public void setNo_sertifikat(String no_sertifikat) {
        this.no_sertifikat = no_sertifikat;
    }

    public String getPublication_nbr() {
        return publication_nbr;
    }

    public void setPublication_nbr(String publication_nbr) {
        this.publication_nbr = publication_nbr;
    }

    public String getPemohon() {
        return pemohon;
    }

    public void setPemohon(String pemohon) {
        this.pemohon = pemohon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReception_date() {
        return reception_date;
    }

    public void setReception_date(String reception_date) {
        this.reception_date = reception_date;
    }

    public String getFiling_date() {
        return filing_date;
    }

    public void setFiling_date(String filing_date) {
        this.filing_date = filing_date;
    }

    public String getRegistration_date() {
        return registration_date;
    }

    public void setRegistration_date(String registration_date) {
        this.registration_date = registration_date;
    }

    public String getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(String expiration_date) {
        this.expiration_date = expiration_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPublication_date() {
        return publication_date;
    }

    public void setPublication_date(String publication_date) {
        this.publication_date = publication_date;
    }

    public String getEntitlement_date() {
        return entitlement_date;
    }

    public void setEntitlement_date(String entitlement_date) {
        this.entitlement_date = entitlement_date;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public String getNice_class_code() {
        return nice_class_code;
    }

    public void setNice_class_code(String nice_class_code) {
        this.nice_class_code = nice_class_code;
    }

    public String getNice_class_descr() {
        return nice_class_descr;
    }

    public void setNice_class_descr(String nice_class_descr) {
        this.nice_class_descr = nice_class_descr;
    }

}
