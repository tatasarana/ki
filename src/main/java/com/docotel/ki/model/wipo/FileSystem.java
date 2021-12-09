package com.docotel.ki.model.wipo;

import com.docotel.ki.util.DateUtil;
import com.docotel.ki.model.master.MUser;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

@MappedSuperclass
public abstract class FileSystem {
	private String id;
	private String access;
	private int numLink;
	private String ownerAccount;
	private String groupName;
	private int fileSize;
	private Timestamp dateModified;
	private String fileName;
	private int modifiedYear;
	private int modifiedMonth;
	private int modifiedDate;
	private int modifiedHour;
	private int modifiedMinute;
	private int week;
	private boolean downloaded;
	private boolean processed;
	private Timestamp createdTime;
	private MUser createdBy;
	private List<ImageFileList> imageFileList;

	@OneToMany(mappedBy = "imageFile")
	public List<ImageFileList> getImageFileList() {
		return imageFileList;
	}

	public void setImageFileList(List<ImageFileList> imageFileList) {
		this.imageFileList = imageFileList;
	}

	public FileSystem() {
	}

	public FileSystem(String fileLine) {
		String[] fileAttributeArr = fileLine.split(" ");
		List<String> fileAttributes = new ArrayList<>();
		for (String fileAttribute : fileAttributeArr) {
			if (!fileAttribute.equalsIgnoreCase("")) {
				fileAttributes.add(fileAttribute);
			}
		}

		try {
			setAccess(fileAttributes.get(0));
			setNumLink(fileAttributes.get(1));
			setOwnerAccount(fileAttributes.get(2));
			setGroupName(fileAttributes.get(3));
			try {
				setFileSize(Integer.parseInt(fileAttributes.get(4)));
			} catch (NumberFormatException e) {
				setFileSize(0);
			}

			switch (fileAttributes.get(5).toLowerCase()) {
				case "feb": {
					setModifiedMonth(2);
					break;
				}
				case "mar": {
					setModifiedMonth(3);
					break;
				}
				case "apr": {
					setModifiedMonth(4);
					break;
				}
				case "may": {
					setModifiedMonth(5);
					break;
				}
				case "jun": {
					setModifiedMonth(6);
					break;
				}
				case "jul": {
					setModifiedMonth(7);
					break;
				}
				case "aug": {
					setModifiedMonth(8);
					break;
				}
				case "sep": {
					setModifiedMonth(9);
					break;
				}
				case "oct": {
					setModifiedMonth(10);
					break;
				}
				case "nov": {
					setModifiedMonth(11);
					break;
				}
				case "dec": {
					setModifiedMonth(12);
					break;
				}
				default: {
					setModifiedMonth(1);
				}
			}
			try {
				setModifiedDate(Integer.parseInt(fileAttributes.get(6)));
			} catch (NumberFormatException e) {
				setModifiedDate(1);
			}
			try {
				String modifiedTime = fileAttributes.get(7);
				String[] modifiedTimeParts = modifiedTime.split(":");
				setModifiedHour(Integer.parseInt(modifiedTimeParts[0]));
				setModifiedMinute(Integer.parseInt(modifiedTimeParts[1]));
			} catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
			}

			setFileName(fileAttributes.get(8));
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Id
	@Column(name = "file_id", length = 36)
	public String getId() {
		if (this.id == null) {
			return UUID.randomUUID().toString();
		}
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "access_right", nullable = false)
	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		if (access != null) {
			this.access = access.toLowerCase();
		} else {
			this.access = "";
		}
	}

	@Column(name = "num_link", nullable = false)
	public int getNumLink() {
		return numLink;
	}

	public void setNumLink(int numLink) {
		this.numLink = numLink;
	}

	public void setNumLink(String numLink) {
		try {
			this.numLink = Integer.parseInt(numLink);
		} catch (NullPointerException | NumberFormatException e) {
			this.numLink = 0;
		}
	}

	@Column(name = "owner_account", nullable = false)
	public String getOwnerAccount() {
		return ownerAccount;
	}

	public void setOwnerAccount(String ownerAccount) {
		this.ownerAccount = ownerAccount;
	}

	@Column(name = "group_name", nullable = false)
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Column(name = "file_size", nullable = false)
	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	@Column(name = "date_modified", nullable = false)
	public Timestamp getDateModified() {
		return dateModified;
	}

	public void setDateModified(Timestamp dateModified) {
		this.dateModified = dateModified;
	}

	@Column(name = "file_name", nullable = false)
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name = "modified_year", nullable = false)
	public int getModifiedYear() {
		if (modifiedYear == 0) {
			modifiedYear = DateUtil.currentYear();
		}
		return modifiedYear;
	}

	public void setModifiedYear(int modifiedYear) {
		this.modifiedYear = modifiedYear;
	}

	@Column(name = "modified_month", nullable = false)
	public int getModifiedMonth() {
		return modifiedMonth;
	}

	public void setModifiedMonth(int modifiedMonth) {
		this.modifiedMonth = modifiedMonth;
	}

	@Column(name = "modified_date", nullable = false)
	public int getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(int modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	@Column(name = "modified_hour", nullable = false)
	public int getModifiedHour() {
		return modifiedHour;
	}

	public void setModifiedHour(int modifiedHour) {
		this.modifiedHour = modifiedHour;
	}

	@Column(name = "modified_minute", nullable = false)
	public int getModifiedMinute() {
		return modifiedMinute;
	}

	public void setModifiedMinute(int modifiedMinute) {
		this.modifiedMinute = modifiedMinute;
	}

	@Column(name = "week", nullable = false)
	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	@Column(name = "is_downloaded", nullable = false)
	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}

	@Column(name = "is_processed", nullable = false)
	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	@Column(name = "created_time", nullable = false)
	public Timestamp getCreatedTime() {
		if (createdTime == null) {
			createdTime = DateUtil.currentDate();
		}
		return createdTime;
	}

	public void setCreatedTime(Timestamp createdTime) {
		this.createdTime = createdTime;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	public MUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(MUser createdBy) {
		this.createdBy = createdBy;
	}

	@Transient
	public boolean isDirectory() {
		return getAccess().startsWith("d");
	}

	public String currentId() {
		return this.id;
	}
}