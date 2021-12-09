package com.docotel.ki.model.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "m_operation")
public class MOperation implements GrantedAuthority, Serializable {
	/***************************** - FIELD SECTION - ****************************/
	private String authority;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/
 
	/***************************** - CONSTRUCTOR SECTION - ****************************/
	public MOperation() {
	}

	public MOperation(String authority) {
		setAuthority(authority);
	}

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Override
	@Id
	@Column(name = "authority", length = 10)
	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	/***************************** - OTHER METHOD SECTION - ****************************/
}
