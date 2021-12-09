package com.docotel.ki.model.transaction;

import javax.persistence.*;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

@Entity
@Table(name = "tx_tm_brand_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmBrandDetail extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private TxTmBrand txTmBrand;
    private Timestamp uploadDate;
    private String fileName;
    private String size;
    private String fileDescription;
    private String fileDataType;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String uploadDateTemp;

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_brand_detail_id", length = 36)
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
    @JoinColumn(name = "tm_brand_id", nullable = false)
    public TxTmBrand getTxTmBrand() {
        return txTmBrand;
    }

    public void setTxTmBrand(TxTmBrand txTmBrand) {
        this.txTmBrand = txTmBrand;
    }

    @Column(name = "tm_brand_upload_date", nullable = false)
    public Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
        this.uploadDateTemp = DateUtil.formatDate(this.uploadDate, "dd/MM/yyyy");
    }

    @Column(name = "tm_brand_filename", length = 255, nullable = false)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "tm_brand_size", length = 255, nullable = false)
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Column(name = "tm_brand_file_desc", length = 255, nullable = false)
    public String getFileDescription() {
        return fileDescription;
    }

    public void setFileDescription(String fileDescription) {
        this.fileDescription = fileDescription;
    }

    @Column(name = "tm_brand_file_data_type", length = 255)
    public String getFileDataType() {
        return fileDataType;
    }

    public void setFileDataType(String fileDataType) {
        this.fileDataType = fileDataType;
    }


	/*private MultipartFile fileSuratOperasi;

	private String suratOperasi;

	public String getSuratOperasi() { return suratOperasi; }

	public void setSuratOperasi(String suratOperasi) { this.suratOperasi = suratOperasi ; }

	public MultipartFile getFileSuratOperasi() { return fileSuratOperasi; }

	public void setFileSuratOperasi(MultipartFile fileSuratOperasi) {
		this.fileSuratOperasi = fileSuratOperasi;
		if (this.fileSuratOperasi != null && this.fileSuratOperasi.getOriginalFilename() != null && !this.fileSuratOperasi.getOriginalFilename().equalsIgnoreCase("")) {
			setSuratOperasi(this.id + "-suratOperasi" +this.fileSuratOperasi.getOriginalFilename().substring(this.fileSuratOperasi.getOriginalFilename().lastIndexOf(".")));
		}
	}*/


	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    @Transient
    public String getUploadDateTemp() {
        return uploadDateTemp;
    }

    public void setUploadDateTemp(String uploadDateTemp) {
        this.uploadDateTemp = uploadDateTemp;
        try {
            this.uploadDate = DateUtil.toDate("dd/MM/yyyy", this.uploadDateTemp);
        } catch (ParseException e) {
        }
    }

    /***************************** - OTHER METHOD SECTION - ****************************/

    @Transient
    public String getCurrentId() {
        return this.id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        sb.append("^ Nama File: " + getFileName());
        sb.append("^ Deskripsi: " + getFileDescription());
        sb.append("^ Tanggal Upload: " + getUploadDate());
        return sb.toString();
    }
}
