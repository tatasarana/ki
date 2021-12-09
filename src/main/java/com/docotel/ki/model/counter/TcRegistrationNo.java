package com.docotel.ki.model.counter;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "tc_reg_no")
public class TcRegistrationNo implements Serializable {
	/***************************** - CONSTANT SECTION - ****************************/
	private static final String REG_NO_CHAR = "IDM";
	private static final short SEQUENCE_LENGTH = 9;

	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private long sequence;
	
	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	/***************************** - CONSTRUCTOR SECTION - ****************************/

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "counter_reg_no_id", length = 36)
	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Column(name = "sequence", nullable = false)
	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}
	
	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/

	/***************************** - OTHER METHOD SECTION - ****************************/
	public String toString() {
		return REG_NO_CHAR + StringUtils.leftPad(getSequence() + "", SEQUENCE_LENGTH, "0");
	}

	public void increaseSequence() {
		this.sequence++;
	}
}
