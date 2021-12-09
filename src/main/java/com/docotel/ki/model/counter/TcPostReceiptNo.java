package com.docotel.ki.model.counter;

import com.docotel.ki.model.master.MFileType;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tc_post_receipt_no")
public class TcPostReceiptNo implements Serializable {
	/***************************** - CONSTANT SECTION - ****************************/
	private static final short SEQUENCE_LENGTH = 5;

	/***************************** - FIELD SECTION - ****************************/
	private String id;	
	private MFileType mFileType;
	private long sequence;
	private long extensionSequence;
	private int year;	
	

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	/***************************** - CONSTRUCTOR SECTION - ****************************/

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "counter_post_rec_no_id", length = 36)
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

	@Transient
	public MFileType getmFileType() {
		return mFileType;
	}

	public void setmFileType(MFileType mFileType) {
		this.mFileType = mFileType;
	}

	@Column(name = "sequence")
	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}
	
	@Column(name = "extension_sequence")
	public long getExtensionSequence() {
		return extensionSequence;
	}

	public void setExtensionSequence(long extensionSequence) {
		this.extensionSequence = extensionSequence;
	}
	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
	

	/***************************** - OTHER METHOD SECTION - ****************************/
	public String toString() {
//		format perpanjangan R7847/2018, R7848/2018, R7849/2018, dst..
//		format bukan perpanjangan 18821/2018, 18822/2018, 18823/2018, dst..
		if(getmFileType().getCurrentId().equals("PERPANJANGAN") ||getmFileType().getCurrentId().equals("PERPANJANGAN_6_BULAN_KADALUARSA")) {
			return "R" + StringUtils.leftPad(getExtensionSequence() + "", SEQUENCE_LENGTH, "0") +"/"+getYear();
		} else {
			return StringUtils.leftPad(getSequence() + "", SEQUENCE_LENGTH, "0") +"/"+getYear();
		}
		
	}
	
	public void increaseSequence() {
		this.sequence++;
	}
	
	public void increaseExtensionSequence() {
		this.extensionSequence++;
	}
}
