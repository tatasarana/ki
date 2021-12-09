package com.docotel.ki.controller.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class CopyLogoIpasController extends BaseController {
	private static Logger logger = LoggerFactory.getLogger(CopyLogoIpasController.class);
	private static final String DIRECTORY_PAGE = "migrasi/";
	private static final String PAGE_COPY_LOGO_IPAS = DIRECTORY_PAGE + "copy-logo-ipas";
	private static final String PATH_COPY_LOGO_IPAS = "/copy-logo-ipas";

	@Value("${upload.file.brand.path:}")
	private String uploadFileBrandPath;

	@RequestMapping(value = PATH_COPY_LOGO_IPAS, method = {RequestMethod.GET})
	public String showPage(Model model) {
		return PAGE_COPY_LOGO_IPAS;
	}

	@RequestMapping(value = PATH_COPY_LOGO_IPAS, method = {RequestMethod.POST})
	public void doCopyLogoIpas(final HttpServletRequest request,  final HttpServletResponse response) throws SQLException {
		if (isAjaxRequest(request)) {
			setResponseAsJson(response);

			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				Timestamp startDate = request.getParameter("startDate") == null || request.getParameter("startDate").equals("") ? null : DateUtil.toDate("dd/MM/yyyy", request.getParameter("startDate"));
				Timestamp endDate = request.getParameter("endDate") == null || request.getParameter("endDate").equals("") ? null : DateUtil.toDate("dd/MM/yyyy", request.getParameter("endDate"));
				String[] appNoList = request.getParameter("applicationNo").split(";");
				String applicationNo = "";

				for (String appNo : appNoList) {
					if (!appNo.trim().equals("")) {
						applicationNo += ("'" + appNo.trim() + "',");
					}
				}

				String sql = "SELECT im.FILE_TYP, im.FILE_SEQ, im.FILE_SER, im.FILE_NBR, CASE WHEN im.CAPTURE_DATE IS NULL THEN im.FILING_DATE ELSE im.CAPTURE_DATE END AS \"CAPTURE_DATE\", il.LOGO_DATA FROM PRODDGIPR.IP_MARK im, PRODDGIPR.IP_LOGO il ";
				String where = "WHERE im.FILE_TYP=il.FILE_TYP AND im.FILE_SEQ=il.FILE_SEQ AND im.FILE_SER=il.FILE_SER AND im.FILE_NBR=il.FILE_NBR ";
				String order = "ORDER BY im.CAPTURE_DATE, (im.FILE_TYP||im.FILE_SEQ||im.FILE_SER||LPAD(im.FILE_NBR,6,'0'))";

				if (startDate != null && endDate != null) {
					where += " AND CASE WHEN im.CAPTURE_DATE IS NULL THEN im.FILING_DATE ELSE im.CAPTURE_DATE END BETWEEN ? AND (? + 1)  ";
				} else if (startDate != null || endDate != null) {
					where += " AND TO_CHAR(CASE WHEN im.CAPTURE_DATE IS NULL THEN im.FILING_DATE ELSE im.CAPTURE_DATE END, 'yyyy-MM-dd') = TO_CHAR(?, 'yyyy-MM-dd') ";
				}

				if (applicationNo != null && !applicationNo.equals("")) {
					if(isUniquePrefix(applicationNo) && applicationNo.length() > 16) {
						where += " AND (im.FILE_TYP||im.FILE_SEQ||im.FILE_SER||LPAD(im.FILE_NBR," + (applicationNo.length() - 10) + ",'0')) in (" + applicationNo.substring(0, applicationNo.length() - 1) + ") ";
					}else if(applicationNo.length() < 16) {
						where += " AND (im.FILE_TYP||im.FILE_SEQ||im.FILE_SER||im.FILE_NBR) in (" + applicationNo.substring(0, applicationNo.length() - 1) + ") ";
					}else {
						where += " AND (im.FILE_TYP||im.FILE_SEQ||im.FILE_SER||LPAD(im.FILE_NBR,6,'0')) in (" + applicationNo.substring(0, applicationNo.length() - 1) + ") ";
					}
				}

				int i = 0;
				//step1 load the driver class
				Class.forName("oracle.jdbc.driver.OracleDriver");
				//step2 create  the connection object  
				conn = DriverManager.getConnection("jdbc:oracle:thin:@10.1.18.30:1521:IPAS1","docotel","docotel123");
				//step3 create the statement object  
				ps = conn.prepareStatement(sql + where + order);
				//step4 set parameter
				if (startDate != null)
					ps.setTimestamp(++i, startDate);
				if (endDate != null)
					ps.setTimestamp(++i, endDate);
				//step5 execute query
				rs = ps.executeQuery();

				List<String[]> data = new ArrayList<>();
				int no = 1;
				while (rs.next()) {
					String result = "";
					String filename = isUniquePrefix(applicationNo) ? rs.getString("FILE_TYP") + rs.getString("FILE_SEQ") + rs.getString("FILE_SER") + rs.getString("FILE_NBR") :
							rs.getString("FILE_TYP") + rs.getString("FILE_SEQ") + rs.getString("FILE_SER") + StringUtils.leftPad(rs.getString("FILE_NBR"), 6, "0");
					String pathFolder = DateUtil.formatDate(rs.getDate("CAPTURE_DATE"), "yyyy/MM/dd/");
					Path paths = Paths.get(uploadFileBrandPath + pathFolder + filename + ".jpg");

					if (rs.getBlob("LOGO_DATA") == null) {
						result = "NULL";
					} else if (Files.exists(paths)) {
						result = "EXIST";
					} else {
						result = "COPY";

						java.sql.Blob blob = rs.getBlob("LOGO_DATA");
						byte[] bytes = blob.getBytes(1, (int) blob.length());

						if (!Files.exists(paths.getParent())) {
							Files.createDirectories(paths.getParent());
						}

						Files.write(paths, bytes);
					}

					data.add(new String[]{"" + no++, filename, rs.getBlob("LOGO_DATA") == null ? "" : paths.toString(), result});
				}

				rs.close();
				ps.close();
				conn.close();

				writeJsonResponse(response, data);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				rs.close();
				ps.close();
				conn.close();
			}
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	private static boolean isUniquePrefix(String applicationNo){
		if (applicationNo.length()>0){
			return  applicationNo.substring(1,2).contentEquals("M") ? true : false ;
		}
		return false;
	}
}
