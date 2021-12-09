package com.docotel.ki.model.transaction;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.*;

import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MBrandType;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.ObjectMapperUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "tx_tm_brand")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmBrand extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private MBrandType mBrandType;
    private String name;
    private String keywordMerek;
    private String fileName;
    private String description;
    private String color;
    private String disclaimer;
    private String translation;
    private String descChar;
    private String xmlFileId;
    private Boolean agreeDisclaimer;
    private Boolean standardChar;
    private Boolean colorCombination;
    private Boolean type3d;
    private Boolean typeSound;
    private Boolean typeCollective;
    private String colorIndication;
    private String translationFr;
    private String translationSp;

	private List<TxTmBrandDetail> txTmBrandDetailList;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
	/*@Transient?
	@OneToMany(targetEntity=MBrandType.class, mappedBy="txTmBrand", fetch=FetchType.EAGER)
	private List<String> listBrandTypeId;*/

    private MultipartFile fileMerek;
    /***************************** - CONSTRUCTOR SECTION - ****************************/

    public TxTmBrand() {}
    
    public TxTmBrand(TxTmBrand txTmBrand, TxTmGeneral txTmGeneral) {
    	this.setTxTmGeneral(txTmGeneral);
    	this.setmBrandType(txTmBrand.getmBrandType());
    	this.setName(txTmBrand.getName());
    	this.setDescription(txTmBrand.getDescription());
    	this.setTranslation(txTmBrand.getTranslation());
    	this.setDescChar(txTmBrand.getDescChar());
    	this.setColor(txTmBrand.getColor());
    	this.setDisclaimer(txTmBrand.getDisclaimer());
    	this.setAgreeDisclaimer(txTmBrand.isAgreeDisclaimer());
    	this.setColorIndication(txTmBrand.getColorIndication());
        this.setTranslationFr(txTmBrand.getTranslationFr());
        this.setTranslationSp(txTmBrand.getTranslationSp());
        this.setTypeCollective(txTmBrand.getTypeCollective());
        this.setStandardChar(txTmBrand.getStandardChar());
        this.setColorCombination(txTmBrand.getColorCombination());
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
	
	/*public List<String> getListBrandTypeId() {
		return listBrandTypeId;
	}

	public void setListBrandTypeId(List<String> listBrandTypeId) {
		this.listBrandTypeId = listBrandTypeId;
	}*/
    @Id
    @Column(name = "tm_brand_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    public TxTmGeneral getTxTmGeneral() {
        return txTmGeneral;
    }

    public void setTxTmGeneral(TxTmGeneral txTmGeneral) {
        this.txTmGeneral = txTmGeneral;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_type_id", nullable = false)
    public MBrandType getmBrandType() {
        return mBrandType;
    }

    public void setmBrandType(MBrandType mBrandType) {
        this.mBrandType = mBrandType;
    }


	/*@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brand_var_id", nullable = false)
	public MBrandVar getmBrandVar() {
		return mBrandVar;
	}

	public void setmBrandVar(MBrandVar mBrandVar) {
		this.mBrandVar = mBrandVar;
	}*/

    @Column(name = "tm_brand_name", length = 1000, nullable = true)
    public String getName() {

        if (name == null || name.equalsIgnoreCase("") || name.isEmpty() )
            return name ;
        else
            return name.replaceAll("\\\\","\"");
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Column(name = "tm_brand_keyword", length = 255, nullable = true)
    public String getKeywordMerek() {

        if (keywordMerek == null || keywordMerek.equalsIgnoreCase("") || keywordMerek.isEmpty() )
            return keywordMerek ;
        else
            return keywordMerek.replaceAll("\\\\","\"");
	}

	public void setKeywordMerek(String keywordMerek) {
		this.keywordMerek = keywordMerek;
	}

	@Column(name = "tm_brand_label", length = 255, nullable = true)
    public String getFileName() {

        if (fileName == null || fileName.equalsIgnoreCase("") || fileName.isEmpty() )
            return fileName ;
        else
            return fileName.replaceAll("\\\\","\"");
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "tm_brand_description", length = 4000, nullable = true)
    public String getDescription() {

        if (description == null || description.equalsIgnoreCase("") || description.isEmpty() )
            return description ;
        else
            return description.replaceAll("\\\\","\"");
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "tm_brand_color", length = 500, nullable = true)
    public String getColor() {
        if (color == null || color.equalsIgnoreCase("") || color.isEmpty() )
            return color ;
        else
            return color.replaceAll("\\\\","\"");
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Column(name = "tm_brand_disclaimer", length = 500)
    public String getDisclaimer() {

        if (disclaimer == null || disclaimer.equalsIgnoreCase("") || disclaimer.isEmpty() )
            return disclaimer ;
        else
            return disclaimer.replaceAll("\\\\","\"");
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    @Column(name = "tm_brand_translation", length = 4000)
    public String getTranslation() {

        if (translation == null || translation.equalsIgnoreCase("") || translation.isEmpty() )
            return translation ;
        else
            return translation.replaceAll("\\\\","\"");
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @Column(name = "tm_brand_desc_char", length = 500)
    public String getDescChar() {

        if (descChar == null || descChar.equalsIgnoreCase("") || descChar.isEmpty() )
            return descChar ;
        else
            return descChar.replaceAll("\\\\","\"");
    }

    public void setDescChar(String descChar) {
        this.descChar = descChar;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @JsonIgnore
    public MUser getCreatedBy() {
        if (createdBy == null) {
            Object principal = null;
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } catch (NullPointerException e) {
            }
            if (principal != null && principal instanceof UserDetails) {
                createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                createdBy = UserEnum.SYSTEM.value();
            }
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
            Object principal = null;
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } catch (NullPointerException e) {
            }
            if (principal != null && principal instanceof UserDetails) {
                updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                updatedBy = UserEnum.SYSTEM.value();
            }
        }
        return updatedBy;
    }

    public void setUpdatedBy(MUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Column(name = "xml_file_id", nullable = true)
	public String getXmlFileId() {
		return xmlFileId;
	}

	public void setXmlFileId(String xmlFileId) {
		this.xmlFileId = xmlFileId;
	}
	
	@Column(name = "tm_brand_agree_disclaimer")
	public Boolean isAgreeDisclaimer() {
		return agreeDisclaimer;
	}

	public void setAgreeDisclaimer(Boolean agreeDisclaimer) {
		this.agreeDisclaimer = agreeDisclaimer;
	}

	@OneToMany(mappedBy = "txTmBrand", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    public List<TxTmBrandDetail> getTxTmBrandDetailList() {
        return txTmBrandDetailList;
    }

	public void setTxTmBrandDetailList(List<TxTmBrandDetail> txTmBrandDetailList) {
        this.txTmBrandDetailList = txTmBrandDetailList;
    }

	@Column(name = "tm_standard_char")
	public Boolean getStandardChar() {
		return standardChar;
	}

	public void setStandardChar(Boolean standardChar) {
		this.standardChar = standardChar;
	}

	@Column(name = "tm_color_combination")
	public Boolean getColorCombination() {
		return colorCombination;
	}

	public void setColorCombination(Boolean colorCombination) {
		this.colorCombination = colorCombination;
	}

	@Column(name = "tm_type_3d")
	public Boolean getType3d() {
		return type3d;
	}

	public void setType3d(Boolean type3d) {
		this.type3d = type3d;
	}

	@Column(name = "tm_type_sound")
	public Boolean getTypeSound() {
		return typeSound;
	}

	public void setTypeSound(Boolean typeSound) {
		this.typeSound = typeSound;
	}

	@Column(name = "tm_type_collective")
	public Boolean getTypeCollective() {
		return typeCollective;
	}

	public void setTypeCollective(Boolean typeCollective) {
		this.typeCollective = typeCollective;
	}

	@Column(name = "tm_color_indication")
	public String getColorIndication() {
		return colorIndication;
	}

	public void setColorIndication(String colorIndication) {
		this.colorIndication = colorIndication;
	}

	@Column(name = "tm_translation_fr")
	public String getTranslationFr() {
		return translationFr;
	}

	public void setTranslationFr(String translationFr) {
		this.translationFr = translationFr;
	}

	@Column(name = "tm_translation_sp")
	public String getTranslationSp() {
		return translationSp;
	}

	public void setTranslationSp(String translationSp) {
		this.translationSp = translationSp;
	}

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/

    @Transient
    public MultipartFile getFileMerek() {
        return fileMerek;
    }

    public void setFileMerek(MultipartFile fileMerek) {
        this.fileMerek = fileMerek;
        //ignore filename is null
        if (this.fileName == null || this.fileName.equalsIgnoreCase("blob")){
            this.fileName = fileMerek.getOriginalFilename(); // "blob.jpg";
        }

        if (this.fileName != null && !this.fileMerek.getOriginalFilename().contains(".")){
            setFileName(this.id + "-fileMerek" + this.fileName.substring(this.fileName.lastIndexOf(".")));
            //System.out.println("SUCCESS setFileName!");
            return;
        }
        else if (this.fileName != null && this.fileMerek.getOriginalFilename() != null && !this.fileMerek.getOriginalFilename().equalsIgnoreCase("")) {
            setFileName(this.id + "-fileMerek" + this.fileMerek.getOriginalFilename().substring(this.fileMerek.getOriginalFilename().lastIndexOf(".")));
            //System.out.println("SUCCESS setFileName!");
        }
        else{
            //System.out.println("FAILED setFileName!");
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
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        sb.append("^ Tipe Merek: " + getmBrandType().getName());
        sb.append("^ Label Merek: " + getFileName());
        sb.append("^ Nama Merek: " + getName());
        sb.append("^ Deskripsi Merek: " + getDescription());
        sb.append("^ Warna Merek: " + getColor());
        sb.append("^ Terjemahan: " + getTranslation());
        sb.append("^ Terjemahan Karakter Asing: " + getDescChar());
        sb.append("^ Disclaimer: " + getDisclaimer());
        return sb.toString();
    }
    
    public void toJson(Map<String, Object> result) throws JsonProcessingException {
    	result.put("brandName", name);
    	result.put("keywordMerek", keywordMerek);
    	result.put("fileName", fileName);
    	result.put("description", description);
    	result.put("color", color);
    	result.put("disclaimer", disclaimer);
    	result.put("translation", translation);
    	result.put("descChar", disclaimer);
    	result.put("mBrandType", mBrandType.getId());
    	
    }
}
