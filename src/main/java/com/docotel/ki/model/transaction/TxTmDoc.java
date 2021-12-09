package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MDocType;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "tx_tm_doc")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmDoc extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private MDocType mDocType;
    private String description;
    private boolean status;
//    private Timestamp uploadsDate;
    private String fileName;
    private String fileNameTemp;
    private String fileSize;
    private MUser createdBy;
    private Timestamp createdDate;
    private String xmlFileId;


    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private MultipartFile fileImageTtd;

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_doc_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    public TxTmGeneral getTxTmGeneral() {
        return txTmGeneral;
    }

    public void setTxTmGeneral(TxTmGeneral txTmGeneral) {
        this.txTmGeneral = txTmGeneral;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_type_id", nullable = false)
    public MDocType getmDocType() {
        return mDocType;
    }

    public void setmDocType(MDocType mDocType) {
        this.mDocType = mDocType;
    }

    @Column(name = "tm_doc_desc", length = 4000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "tm_doc_status", nullable = false)
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

//    @Column(name = "tm_doc_upload_date")
//    public Timestamp getUploadsDate() {
//        return uploadDate;
//    }

//    public void setUploasdDate(Timestamp uploadsDate) {
//        this.uploadsDate = uploadsDate;
//    }

    @Column(name = "tm_doc_filename")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "tm_doc_filename_temp")
    public String getFileNameTemp() {
        return fileNameTemp;
    }

    public void setFileNameTemp(String fileNameTemp) {
        this.fileNameTemp = fileNameTemp;
    }

    @Column(name = "tm_doc_file_size")
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    public MUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(MUser createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "created_date", nullable = false)
    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name = "xml_file_id", length = 36)
    public String getXmlFileId() {
        return xmlFileId;
    }

    public void setXmlFileId(String xmlFileId) {
        this.xmlFileId = xmlFileId;
    }




    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/

    /***************************** - OTHER METHOD SECTION - ****************************/
    
    
    @Transient
    public MultipartFile getFileImageTtd() {
		return fileImageTtd;
	}

	public void setFileImageTtd(MultipartFile fileImageTtd) {
		this.fileImageTtd = fileImageTtd;
		
		if (this.fileName != null && this.fileImageTtd.getOriginalFilename() != null && !this.fileImageTtd.getOriginalFilename().equalsIgnoreCase("")) {
		    if(!this.fileImageTtd.getOriginalFilename().contains(".")){
                setFileName(this.id + "-fileImageTtd" + this.fileName.substring(this.fileName.lastIndexOf(".")));
                return;
            }
            setFileName(this.id + "-fileImageTtd" + this.fileImageTtd.getOriginalFilename().substring(this.fileImageTtd.getOriginalFilename().lastIndexOf(".")));
        }
	}
    
    @Transient
    public String getCurrentId() {
        return this.id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        sb.append("^ Tipe Dokumen: " + getmDocType().getName());
        sb.append("^ Nama File: " + getFileName());
        sb.append("^ Deskripsi: " + getDescription());
        return sb.toString();
    }

}
