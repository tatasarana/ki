package com.docotel.ki.model.transaction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "tx_log_history")
public class TxLogHistory {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private Timestamp activityDate;
    private String userName;
    private String name;
    private String section;
    private String activity;
    private String objectClassName;
    private String objectId;
    private String oldData;
    private String newData;
    private String tableName;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/

    @Id
    @Column(name = "tx_log_id", length = 36, nullable = false)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "tx_activity_date", nullable = false)
    public Timestamp getActivityDate() {
        if (activityDate == null) {
            activityDate = new Timestamp(System.currentTimeMillis());
        }
        return activityDate;
    }

    public void setActivityDate(Timestamp activityDate) {
        this.activityDate = activityDate;
    }

    @Column(name = "tx_username", nullable = false)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "tx_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "tx_section")
    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Column(name = "tx_activity", nullable = false, length = 10)
    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

	@Column(name = "tx_obj_class_name", nullable = false, length = 500)
	public String getObjectClassName() {
		return objectClassName;
	}

	public void setObjectClassName(String objectClassName) {
		this.objectClassName = objectClassName;
	}

	@Column(name = "tx_obj_id", nullable = false, length = 36)
	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	@Column(name = "tx_old_data", columnDefinition = "CLOB")
    public String getOldData() {
        return oldData;
    }

    public void setOldData(String oldData) {
        this.oldData = oldData;
    }

    @Column(name = "tx_new_data", columnDefinition = "CLOB")
    public String getNewData() {
        return newData;
    }

    public void setNewData(String newData) {
        this.newData = newData;
    }
    
    @Column(name = "tx_table_name", nullable = false, length = 50)
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}