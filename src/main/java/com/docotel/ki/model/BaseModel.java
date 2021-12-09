package com.docotel.ki.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;

@MappedSuperclass
public abstract class BaseModel extends DefaultModel implements Serializable {
	@Transient
	public abstract String getCurrentId();

	public abstract String logAuditTrail();

	public boolean skipLogAuditTrail;

	@Column(name = "skip_audit_trail",nullable = true)
	public Boolean isSkipLogAuditTrail() {
		return skipLogAuditTrail;
	}

	public void setSkipLogAuditTrail(boolean skipLogAuditTrail) {
		this.skipLogAuditTrail = skipLogAuditTrail;
	}

}
