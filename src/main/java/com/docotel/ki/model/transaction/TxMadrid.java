package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

@Entity
@Table(name = "tx_madrid")
@EntityListeners(EntityAuditTrailListener.class)
public class TxMadrid extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
	private String uuid;
	private String id;
    private String no;
    private String intregn;    
    private String docType;
    private String docXMLHeader;
    private String docXMLDetail;
    private Timestamp uploadDate;
    private Timestamp importDate;
    private String errorMessage;
    private String tranTyp;
    private MUser createdBy;
    private Timestamp createdDate;
    private String xmlFileId;
	
    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    private String createdDateTemp;
    
    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "madrid_id", length = 36)
    public String getUuid() {
		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
		}
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Column(name = "docid", length = 36)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Column(name = "intregn", length = 50)
	public String getIntregn() {
		return intregn;
	}

	public void setIntregn(String intregn) {
		this.intregn = intregn;
	}

	@Column(name = "gazno", length = 50)
	public String getNo() {
		return no;
	}
	
	public void setNo(String no) {
		this.no = no;
	}
	
	
	
	@Column(name = "doctype", length = 50)
	public String getDocType() {
		return docType;
	}
	
	public void setDocType(String docType) {
		this.docType = docType;
	}
	
	@Column(name = "xmldoc_header", columnDefinition = "CLOB") 
	public String getDocXMLHeader() {
		return docXMLHeader;
	}

	public void setDocXMLHeader(String docXMLHeader) {
		this.docXMLHeader = docXMLHeader;
	}

	@Column(name = "xmldoc_detail", columnDefinition = "CLOB")
	public String getDocXMLDetail() {
		return docXMLDetail;
	}

	public void setDocXMLDetail(String docXMLDetail) {
		this.docXMLDetail = docXMLDetail;
	}

	public Timestamp getUploadDate() {
		if (uploadDate == null) {
			uploadDate = new Timestamp(System.currentTimeMillis());
        }
		return uploadDate;
	}
	
	public void setUploadDate(Timestamp uploadDate) {
		this.uploadDate = uploadDate;
	}
	
	public Timestamp getImportDate() {
		if (importDate == null) {
			importDate = new Timestamp(System.currentTimeMillis());
        }
		return importDate;
	}
	
	public void setImportDate(Timestamp importDate) {
		this.importDate = importDate;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	@Column(name = "trantyp", length = 10)
	public String getTranTyp() {
		return tranTyp;
	}
	
	public void setTranTyp(String tranTyp) {
		this.tranTyp = tranTyp;
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
		if (createdDate == null) {
            createdDate = new Timestamp(System.currentTimeMillis());
        }		
		return createdDate;
	}
	
	
	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
		this.createdDateTemp = DateUtil.formatDate(this.createdDate, "dd/MM/yyyy");
	}
	
	
	
	public String getXmlFileId() {
		return xmlFileId;
	}
	
	@Column(name = "xml_file_id", nullable = true)
	public void setXmlFileId(String xmlFileId) {
		this.xmlFileId = xmlFileId;
	}

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    @Transient
    public String getCreatedDateTemp() {
        return createdDateTemp;
    }

    public void setCreatedDateTemp(String createdDateTemp) {
        this.createdDateTemp = createdDateTemp;
        try {
            this.createdDate = DateUtil.toDate("dd/MM/yyyy", this.createdDateTemp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

	 @Transient
	public String getCurrentId() {
		 return this.uuid;
	}

	@Override
	public String logAuditTrail() {
		 StringBuilder sb = new StringBuilder();
		    sb.append("UUID: " + getCurrentId());
		    sb.append("^ No Gazzate: " + getNo());
		    return sb.toString();
	}

}
