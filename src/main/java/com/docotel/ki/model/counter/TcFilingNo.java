package com.docotel.ki.model.counter;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tc_efil_no")
public class TcFilingNo implements Serializable {
	/***************************** - CONSTANT SECTION - ****************************/
	private static final short SEQUENCE_LENGTH = 6;

	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private int year;
	private long sequence;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	/***************************** - CONSTRUCTOR SECTION - ****************************/

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "counter_efil_no_id", length = 36)
	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "year", nullable = false)
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
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
		return "IPT" + getYear() + StringUtils.leftPad(getSequence() + "", SEQUENCE_LENGTH, "0");
	}

	public void increaseSequence() {
		this.sequence++;
	}
}
