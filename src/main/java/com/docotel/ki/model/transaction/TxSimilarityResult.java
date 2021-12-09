package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "tx_similarity_result")
@EntityListeners(EntityAuditTrailListener.class)
public class TxSimilarityResult extends BaseModel {
	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private TxTmGeneral originTxTmGeneral;
	private TxTmGeneral similarTxTmGeneral;
	private MUser createdBy;
	private Timestamp createdDate;
	private TxSubsCheck txSubsCheck;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	/***************************** - CONSTRUCTOR SECTION - ****************************/

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "similarity_id", length = 36)
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
	@JoinColumn(name = "origin_app_id", nullable = false)
	public TxTmGeneral getOriginTxTmGeneral() {
		return originTxTmGeneral;
	}

	public void setOriginTxTmGeneral(TxTmGeneral originTxTmGeneral) {
		this.originTxTmGeneral = originTxTmGeneral;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "similar_app_id", nullable = false)
	public TxTmGeneral getSimilarTxTmGeneral() {
		return similarTxTmGeneral;
	}

	public void setSimilarTxTmGeneral(TxTmGeneral similarTxTmGeneral) {
		this.similarTxTmGeneral = similarTxTmGeneral;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
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

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "check_id")
	public TxSubsCheck getTxSubsCheck() {
		return txSubsCheck;
	}

	public void setTxSubsCheck(TxSubsCheck txSubsCheck) {
		this.txSubsCheck = txSubsCheck;
	}

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/

	/***************************** - OTHER METHOD SECTION - ****************************/
	@Transient
	public String getCurrentId() {
		return this.id;
	}

	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
		sb.append("Nomor Permohonan: " + getOriginTxTmGeneral().getApplicationNo());
		sb.append("^ Nomor Permohonan Pembanding: " + getSimilarTxTmGeneral().getApplicationNo());
		return sb.toString();
	}
}
