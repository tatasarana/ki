package com.docotel.ki.model.master;

import com.docotel.ki.model.DefaultModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.*;


@Entity
@Table(name = "m_doc_type_detail")
public class MDocTypeDetail extends DefaultModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private MDocType mDocType;
	private MFileType mFileType;
	
	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "doc_type_detail_id", length = 36, nullable = false)
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
	@JoinColumn(name = "doc_type_id", nullable = false)
	public MDocType getmDocType() {
		return mDocType;
	}
	
	public void setmDocType(MDocType mDocType) {
		this.mDocType = mDocType;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name= "file_type_id", nullable = false)
	public MFileType getmFileType() {
		return mFileType;
	}
	
	public void setmFileType(MFileType mFileType) {
		this.mFileType = mFileType;
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

	/***************************** - OTHER METHOD SECTION - ****************************/
	
	@Transient
	public String getCurrentId() {
		return this.id;
	}
	
	

}
