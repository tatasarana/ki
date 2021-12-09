package com.docotel.ki.model.master;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "m_pnbp_fee_code")
public class MPnbpFeeCode implements Serializable {
	private String id;
	private String code;
	private MFileSequence mFileSequence;
	private MFileType mFileType;
    private MFileTypeDetail mFileTypeDetail;
	
	@Id
    @Column(name = "pnbp_fee_code_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @Column(name = "pnbp_fee_code", length = 10, nullable = false)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_seq_id")
    public MFileSequence getmFileSequence() {
        return mFileSequence;
    }

    public void setmFileSequence(MFileSequence mFileSequence) {
        this.mFileSequence = mFileSequence;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_type_id")
    public MFileType getmFileType() {
        return mFileType;
    }

    public void setmFileType(MFileType mFileType) {
        this.mFileType = mFileType;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_type_detail_id")
    public MFileTypeDetail getmFileTypeDetail() {
        return mFileTypeDetail;
    }

    public void setmFileTypeDetail(MFileTypeDetail mFileTypeDetail) {
        this.mFileTypeDetail = mFileTypeDetail;
    }

}
