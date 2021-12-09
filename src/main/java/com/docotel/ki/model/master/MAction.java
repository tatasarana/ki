package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.text.WordUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "m_action")
@EntityListeners(EntityAuditTrailListener.class)
public class MAction extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private String name;
	private String code;
	private String type; // Standard, download, upload, automatic
	private MDocument document;
	private boolean statusFlag;
	private String duration;


	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	/***************************** - CONSTRUCTOR SECTION - ****************************/
	public MAction() {
	}

	public MAction(String id) {
		this.id = id;
	}

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "action_id", length = 36)
	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}

	@Column(name = "action_type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "action_code", length = 10, nullable = false)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "action_name", length = 255, nullable = false)
	public String getName() {
		return name;
	}



	public void setName(String name) {
		try {
			this.name = name.trim();
		} catch (NullPointerException e) {
			this.name = null;
		}

	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false, updatable = false)
	public MUser getCreatedBy() {
		if (createdBy == null) {
			createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}
		return createdBy;
	}

	public void setCreatedBy(MUser createdBy) {
		this.createdBy = createdBy;
	}

	@Column(name = "created_date", nullable = false, updatable = false)
	public Timestamp getCreatedDate() {
		if (createdDate == null) {
			createdDate = new Timestamp(System.currentTimeMillis());
		}
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}



	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id", nullable = true)
	public MDocument getDocument() {
		return document;
	}

	public void setDocument(MDocument document) {
		this.document = document;
	}

	@Column(name = "status_flag", nullable = false)
	public boolean isStatusFlag() {
		return statusFlag;
	}

	public void setStatusFlag(boolean statusFlag) {
		this.statusFlag = statusFlag;
	}

	@Column(name = "updated_date")
	public Timestamp getUpdatedDate() {
		if (updatedDate == null) {
			updatedDate = new Timestamp(System.currentTimeMillis());
		}
		return updatedDate;
	}

	public void setUpdatedDate(Timestamp updatedDate) {
		this.updatedDate = updatedDate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    public MUser getUpdatedBy() {
		if (updatedBy == null) {
			updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}
		return updatedBy;
	}

	public void setUpdatedBy(MUser updatedBy) {
		this.updatedBy = updatedBy;
	}


	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - ****************************/

	/***************************** - OTHER METHOD SECTION - ****************************/
	@Transient
	public String getCurrentId() {
		return this.id;
	}

	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
		sb.append("UUID: " + getCurrentId());
		sb.append("^ Kode Aksi: " + getCode());
		sb.append("^ Nama Aksi: " + getName());
		sb.append("^ Tipe Aksi: " + getType());
		if (getDocument() != null) {
			sb.append("^ Nama Dokumen: " + getDocument().getName());
		} else {
			MDocument mDoc = new MDocument();
			sb.append("^ Nama Dokumen: " + mDoc.getName());
		}
		

		sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
		return sb.toString();
	}

	@Column(length = 3, nullable = true, name = "duration")
	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}


}
