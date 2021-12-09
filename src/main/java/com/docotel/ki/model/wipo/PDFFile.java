package com.docotel.ki.model.wipo;

import com.docotel.ki.util.DateUtil;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Calendar;

@Entity
@Table(name = "tx_madrid_pdf")
public class PDFFile extends FileSystem {
	public PDFFile() {
		super();
	}

	public PDFFile(String fileLine) {
		super(fileLine);
	}

	public PDFFile(FileSystem fileSystem) {
		setAccess(fileSystem.getAccess());
		setNumLink(fileSystem.getNumLink());
		setOwnerAccount(fileSystem.getOwnerAccount());
		setGroupName(fileSystem.getGroupName());
		setFileSize(fileSystem.getFileSize());
		setModifiedMonth(fileSystem.getModifiedMonth());
		setModifiedDate(fileSystem.getModifiedDate());
		setModifiedHour(fileSystem.getModifiedHour());
		setModifiedMinute(fileSystem.getModifiedMinute());
		setFileName(fileSystem.getFileName());

		try {
			setModifiedYear(Integer.parseInt(getFileName().substring(1, 5)));
		} catch (NumberFormatException e) {
		}
		try {
			setWeek(Integer.parseInt(getFileName().substring(5, getFileName().indexOf(".", 5))));
		} catch (NumberFormatException e) {
		}

		Calendar calendar = Calendar.getInstance(DateUtil.LOCALE_ID);
		calendar.set(Calendar.YEAR, getModifiedYear());
		calendar.set(Calendar.MONTH, getModifiedMonth() - 1);
		calendar.set(Calendar.DATE, getModifiedDate());
		calendar.set(Calendar.HOUR_OF_DAY, getModifiedHour());
		calendar.set(Calendar.MINUTE, getModifiedMinute());
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		setDateModified(new Timestamp(calendar.getTimeInMillis()));
	}
}
