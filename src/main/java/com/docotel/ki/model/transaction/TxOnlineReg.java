package com.docotel.ki.model.transaction;

import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.*;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.enumeration.RegistrasiStatusEnum;
import com.docotel.ki.model.master.*;

import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

@Entity
@Table(name = "tx_online_reg")
@EntityListeners(EntityAuditTrailListener.class)
public class TxOnlineReg extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private String name;
	private String no;
	private MCountry nationality;
    //private String ownerType;
    private String applicantType;
    private Timestamp birthDate;
	private MLookup gender;
	private String address;
	private String zipCode;
	private MProvince mProvince;
	private MCity mCity;
	private MRepresentative mReprs;
	private String email;
	private String phone;
	private String fileNameNik;
    private String fileNameCard;
    private String password;
    private String approvalStatus;
	private Timestamp createdDate;
	private String fileSeqId;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	private String noTemp;
	private String passwordTemp;
	private String birthDateTemp;
	private String approvalStatusLabel;
    private MultipartFile fileKtp;
    private MultipartFile fileCard;

	/***************************** - CONSTRUCTOR SECTION - ****************************/

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "action_id", length = 36)
	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    @Column(name = "online_name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "online_id_card", nullable = false)
    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
        try {
            this.noTemp = no.substring(0, no.length() - 6) + "******";
        } catch (Exception e) {
            this.noTemp = "";
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "online_nationality")
    public MCountry getNationality() {
        return nationality;
    }

    public void setNationality(MCountry nationality) {
        this.nationality = nationality;
    }

    /*@Column(name = "online_company_type", length = 50)
    public String getownerType() {
        return ownerType;
    }

    public void setownerType(String ownerType) {
        this.ownerType = ownerType;
    }*/
    
    @Column(name = "online_applicant_type", length = 50)
    public String getApplicantType() {
        return applicantType;
    }

    public void setApplicantType(String applicantType) {
        this.applicantType = applicantType;
    }
    
    @Column(name = "online_birth_date")
    public Timestamp getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Timestamp birthDate) {
        this.birthDate = birthDate;
        this.birthDateTemp = DateUtil.formatDate(this.birthDate, "dd/MM/yyyy");
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "online_gender", nullable = false)
    public MLookup getGender() {
        return gender;
    }

    public void setGender(MLookup gender) {
        this.gender = gender;
    }

    @Column(name = "online_address", nullable = false)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    @Column(name = "online_zip_code")
    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "online_province")
    public MProvince getmProvince() {
        return mProvince;
    }

    public void setmProvince(MProvince mProvince) {
        this.mProvince = mProvince;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "online_city")
    public MCity getmCity() {
        return mCity;
    }

    public void setmCity(MCity mCity) {
        this.mCity = mCity;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reprs_no")
    public MRepresentative getmReprs() {
        return mReprs;
    }

    public void setmReprs(MRepresentative mReprs) {
        this.mReprs = mReprs;
    }

    @Column(name = "online_email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "online_phone", nullable = false)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Column(name = "online_filename_id_card")
    public String getFileNameNik() {
        return fileNameNik;
    }

    public void setFileNameNik(String fileNameNik) {
        this.fileNameNik = fileNameNik;
    }

    @Column(name = "online_filename_reprs_card")
    public String getFileNameCard() {
        return fileNameCard;
    }

    public void setFileNameCard(String fileNameCard) {
        this.fileNameCard = fileNameCard;
    }
    
    @Column(name = "online_password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.passwordTemp = password;
    }

    @Column(name = "approval_status", length = 50)
    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
        this.approvalStatusLabel = RegistrasiStatusEnum.valueOf(approvalStatus).getLabel();
    }

    @Column(name = "created_date", nullable = false)
    public Timestamp getCreatedDate() {
        if (createdDate == null) {
            createdDate =  new Timestamp(System.currentTimeMillis());
        }
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name = "file_seq_id")
    public String getFileSeqId() {
		return fileSeqId;
	}

	public void setFileSeqId(String fileSeqId) {
		this.fileSeqId = fileSeqId;
	}

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    @Transient
    public String getNoTemp() {
        noTemp =  this.no == null ? "-" : this.no.substring(0, this.no.length()-6) + "******";
        return noTemp;
    }
    
	public void setNoTemp(String noTemp) {
    	this.noTemp = noTemp;
    }
    
    @Transient
    public String getPasswordTemp() {
        return passwordTemp;
    }

    public void setPasswordTemp(String passwordTemp) {
        this.passwordTemp = passwordTemp;
    }
    
    @Transient
    public String getApprovalStatusLabel() {
        return approvalStatusLabel;
    }

    public void setApprovalStatusLabel(String approvalStatusLabel) {
        this.approvalStatusLabel = approvalStatusLabel;
    }
    
    @Transient
    public String getBirthDateTemp() {
        birthDateTemp = DateUtil.formatDate(this.birthDate, "dd/MM/yyyy");
        return birthDateTemp;
    }

    public void setBirthDateTemp(String birthDateTemp) {
        this.birthDateTemp = birthDateTemp;
        try {
            this.birthDate = DateUtil.toDate("dd/MM/yyyy", this.birthDateTemp);
        } catch (ParseException e) {
//            System.out.println(e);
        }
    }
    
    @Transient
    public MultipartFile getFileKtp() {
        return fileKtp;
    }

    public void setFileKtp(MultipartFile fileKtp) {
        this.fileKtp = fileKtp;

        if (this.fileNameNik != null && this.fileKtp.getOriginalFilename() != null && !this.fileKtp.getOriginalFilename().equalsIgnoreCase("")) {
            setFileNameNik(this.id + "-fileKtp" + this.fileKtp.getOriginalFilename().substring(this.fileKtp.getOriginalFilename().lastIndexOf(".")));
        }
    }

    @Transient
    public MultipartFile getFileCard() {
        return fileCard;
    }

    public void setFileCard(MultipartFile fileCard) {
        this.fileCard = fileCard;

        if (this.fileNameCard != null && this.fileCard.getOriginalFilename() != null && !this.fileCard.getOriginalFilename().equalsIgnoreCase("")) {
            setFileNameCard(this.id + "-fileCard" + this.fileCard.getOriginalFilename().substring(this.fileCard.getOriginalFilename().lastIndexOf(".")));
        }
    }


	/***************************** - OTHER METHOD SECTION - ****************************/

    @Transient
    public String getCurrentId() {
        return this.id;
    }

	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
		sb.append("UUID: " + getCurrentId());
		sb.append("^ Jenis Pemohon: " + getGender().getGroups());
		sb.append("^ Username: " + getEmail());
		sb.append("^ Nama Pemohon: " + getName());
		sb.append("^ Jenis Kelamin: " + getGender().getName());
		sb.append("^ Kewarganegaraan: " + getNationality());
		sb.append("^ Telepon: " + getPhone());
		sb.append("^ Email: " + getEmail());
		return sb.toString();
	}

    /*@Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append(", Name: " + getName());
        sb.append(", No: " + getNo());
        return sb.toString();
    }*/
}
