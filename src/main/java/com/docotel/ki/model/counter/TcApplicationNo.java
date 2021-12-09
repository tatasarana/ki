package com.docotel.ki.model.counter;

import com.docotel.ki.model.master.MFileSequence;
import com.docotel.ki.model.master.MFileType;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tc_app_no")
public class TcApplicationNo implements Serializable {
	/***************************** - CONSTANT SECTION - ****************************/
	private static final short SEQUENCE_LENGTH = 6;

	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private MFileSequence mFileSequence;
	private MFileType mFileType;
	private int year;
	private long sequence;
	private long sequenceMadridOO;
	private long sequenceMadridTransformation;
	private long sequenceMadridReplacement; 

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	/***************************** - CONSTRUCTOR SECTION - ****************************/

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "counter_app_no_id", length = 36)
	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "file_seq_id")
	@Transient
	public MFileSequence getmFileSequence() {
		return mFileSequence;
	}

	public void setmFileSequence(MFileSequence mFileSequence) {
		this.mFileSequence = mFileSequence;
	}

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "file_type_id")
	@Transient
	public MFileType getmFileType() {
		return mFileType;
	}

	public void setmFileType(MFileType mFileType) {
		this.mFileType = mFileType;
	}

	@Column(name = "year", nullable = false)
//	@Transient
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
	
	@Column(name = "sequence_madrid_oo", nullable = false)
	public long getSequenceMadridOO() {
		return sequenceMadridOO;
	}

	public void setSequenceMadridOO(long sequenceMadridOO) {
		this.sequenceMadridOO = sequenceMadridOO;
	}
	
	@Column(name = "sequence_madrid_transformation", nullable = false)
	public long getSequenceMadridTransformation() {
		return sequenceMadridTransformation;
	}

	public void setSequenceMadridTransformation(long sequenceMadridTransformation) {
		this.sequenceMadridTransformation = sequenceMadridTransformation;
	}
	
	@Column(name = "sequence_madrid_replacement", nullable = false)
	public long getSequenceMadridReplacement() {
		return sequenceMadridReplacement;
	}

	public void setSequenceMadridReplacement(long sequenceMadridReplacement) {
		this.sequenceMadridReplacement = sequenceMadridReplacement;
	} 

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/

	/***************************** - OTHER METHOD SECTION - ****************************/
	public String toString() {
		if(getmFileType().getCode().equals("M")) {
			return getmFileType().getCode() + getmFileSequence().getCode() + getYear() + StringUtils.leftPad(getSequenceMadridOO() + "", SEQUENCE_LENGTH, "0");
		}else if(getmFileType().getCode().equals("MT")) {
			return getmFileType().getCode() + getmFileSequence().getCode() + getYear() + StringUtils.leftPad(getSequenceMadridTransformation() + "", SEQUENCE_LENGTH, "0");
		}else if(getmFileType().getCode().equals("MR")) {
			return getmFileType().getCode() + getmFileSequence().getCode() + getYear() + StringUtils.leftPad(getSequenceMadridReplacement() + "", SEQUENCE_LENGTH, "0");
		}else {
			return getmFileType().getCode() + getmFileSequence().getCode() + getYear() + StringUtils.leftPad(getSequence() + "", SEQUENCE_LENGTH, "0");
		}
	}

	public String toStringPost() {
		return getmFileType().getCode() + getmFileSequence().getCode() + getYear() + StringUtils.leftPad(getSequence() + "", SEQUENCE_LENGTH, "0");
	}

	public void increaseSequence() {
		if(getmFileType().getCode().equals("M")) {
			this.sequenceMadridOO++; 
		} else if(getmFileType().getCode().equals("MT")) {
			this.sequenceMadridTransformation++;
		}else if (getmFileType().getCode().equals("MR")) {
			this.sequenceMadridReplacement++;
		}else {
			this.sequence++;
		}
	}
}
