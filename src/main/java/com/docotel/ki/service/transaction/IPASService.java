package com.docotel.ki.service.transaction;

import com.docotel.ki.controller.migration.CopyLogoIpasController;
import com.docotel.ki.util.DateUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class IPASService {

    private static Logger logger = LoggerFactory.getLogger(CopyLogoIpasController.class);
    private static final String COLUMN_ALT_1  = "ALT_1";
    private static final String COLUMN_ALT_2  = "ALT_2";
    private static final String COLUMN_DATA  = "DATA";

    @Value("${upload.ipas.file.path}")
    private String uploadIPASDoc;

    @Value("${download.output.letters.file.path:}")
    private String downloadLettersFilePath;

    public String findDocIPASS(String applcationNo, String ccNotes) {

        String path = "" ;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String applicationNumber = applcationNo;
            String fileType = applicationNumber.substring(0, 1);
            String fileSeq = applicationNumber.substring(1,3);
            String fileSer = applicationNumber.substring(3,7);
            String fileNumber = applicationNumber.substring(7);


            String sql = null;

            if (ccNotes!=null){

                int ccNotesLength = ccNotes.length();
                String lowerCCNotes = ccNotes.toLowerCase();

                sql = "SELECT CASE WHEN (LOWER(SUBSTR(ALT_1 , -"+ccNotesLength+")) = :1 ) THEN ALT_1 ELSE ALT_2 END AS DATA FROM docotel.reg_to_path2 " +
                        "WHERE FILE_TYP = :2 AND FILE_SEQ = :3 AND FILE_SER = :4 AND FILE_NBR = :5  AND (LOWER(SUBSTR(ALT_1 , -"+ccNotesLength+")) = :6  OR " +
                        "LOWER(SUBSTR(ALT_2 , -"+ccNotesLength+")) = :7 )";


                int i = 0;
                //step1 load the driver class
                Class.forName("oracle.jdbc.driver.OracleDriver");
                //step2 create  the connection object
                conn = DriverManager.getConnection("jdbc:oracle:thin:@10.1.18.30:1521:IPAS1","docotel","docotel123");
                //step3 create the statement object
                ps = conn.prepareStatement(sql);

                //step4 insert parameter
                ps.setString(1,lowerCCNotes);
                ps.setString(2,fileType);
                ps.setString(3,fileSeq);
                ps.setString(4,fileSer);
                ps.setString(5,fileNumber);
                ps.setString(6,lowerCCNotes);
                ps.setString(7,lowerCCNotes);

                //step4 execute query
                rs = ps.executeQuery();

                if(rs!=null){
                    if(rs.next()){
                        path = rs.getString(rs.findColumn(COLUMN_DATA)) ;
                    }
                }
//                System.out.println(path+" "+sql+" "+lowerCCNotes+" "+fileType+" "+fileSeq+" "+fileSer+" "+fileNumber);
            } else {
                sql = "select * from docotel.reg_to_path2 " +
                        "WHERE FILE_TYP = :1 AND FILE_SEQ = :2 AND FILE_SER = :3 AND FILE_NBR = :4 ";
//                System.out.println("sqlelse "+ sql);
                int i = 0;
                //step1 load the driver class
                Class.forName("oracle.jdbc.driver.OracleDriver");
                //step2 create  the connection object
                conn = DriverManager.getConnection("jdbc:oracle:thin:@10.1.18.30:1521:IPAS1","docotel","docotel123");
                //step3 create the statement object
                ps = conn.prepareStatement(sql);

                //step4 insert parameter
                ps.setString(1,fileType);
                ps.setString(2,fileSeq);
                ps.setString(3,fileSer);
                ps.setString(4,fileNumber);

                //step4 execute query
                rs = ps.executeQuery();

                if(rs!=null){
                    if(rs.next()){
                        path = rs.getString(rs.findColumn(COLUMN_ALT_1)) ;
                        String fullPath = this.uploadIPASDoc + "/" + path;
//                        System.out.println("alt1 "+ fullPath);
                        File file = new File ( fullPath );
                        if (!file.exists() || file.isDirectory())  {
                            path = rs.getString(rs.findColumn(COLUMN_ALT_2)) ;
//                            System.out.println("alt2 "+path);

                        }
                    }
                }
            }

            rs.close();
            ps.close();
            conn.close();
            return path;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return path;
    }


    public String findUserDocIPASS(String applcationNo, String ccNotes) {

        String path = "" ;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String applicationNumber = applcationNo;
            String fileType = applicationNumber.substring(0, 1);
            String fileSeq = applicationNumber.substring(1,3);
            String fileSer = applicationNumber.substring(3,7);
            String fileNumber = applicationNumber.substring(7);


            String sql = null;

            if (ccNotes!=null){

                int ccNotesLength = ccNotes.length();
                String lowerCCNotes = ccNotes.toLowerCase();


                sql = "SELECT * FROM docotel.reg_to_path_readable " +
                        "WHERE FILE_TYP = :1 AND FILE_SEQ = :2 AND FILE_SER = :3 AND FILE_NBR = :4 " +
                        " AND (LOWER(SUBSTR(ALT_1 , -"+ccNotesLength+")) = :5  OR " +
                        "LOWER(SUBSTR(ALT_2 , -"+ccNotesLength+")) = :6 )";

//                System.out.println("pathnya"+path+" querynya"+sql+"ccnotesnya "+lowerCCNotes+" "+fileType+" "+fileSeq+" "+fileSer+" "+fileNumber);

                int i = 0;
                //step1 load the driver class
                Class.forName("oracle.jdbc.driver.OracleDriver");
                //step2 create  the connection object
                conn = DriverManager.getConnection("jdbc:oracle:thin:@10.1.18.30:1521:IPAS1","docotel","docotel123");
                //step3 create the statement object
                ps = conn.prepareStatement(sql);

                //step4 insert parameter
                ps.setString(1,fileType);
                ps.setString(2,fileSeq);
                ps.setString(3,fileSer);
                ps.setString(4,fileNumber);
                ps.setString(5,lowerCCNotes);
                ps.setString(6,lowerCCNotes);

                //step4 execute query
                rs = ps.executeQuery();

//                System.out.println("pathnya"+rs);
                if(rs!=null){
                    if(rs.next()){
                        path = rs.getString(rs.findColumn(COLUMN_ALT_1)) ;
                        String fullPath = this.uploadIPASDoc + "/" + path;
                        //System.out.println("path lg "+path);
                        File file = new File ( fullPath );
                        if (!file.exists() || file.isDirectory())  {
                            path = rs.getString(rs.findColumn(COLUMN_ALT_2)) ;
                            //System.out.println("path lg 2 "+path);
                        }
//                        else {path=null;}

                    }
                }

            }


            rs.close();
            ps.close();
            conn.close();
//            System.out.println("kenapa null "+path);
            return path;


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        System.out.println("kenapa null 2 "+path);
        return path;
    }

    public String findPDFDOCBlobIPASS(String applcationNo, String offiDoc) {

        String path = "" ;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String applicationNumber = applcationNo;
            String fileType = applicationNumber.substring(0, 1);
            String fileSeq = applicationNumber.substring(1,3);
            String fileSer = applicationNumber.substring(3,7);
            String fileNumber = applicationNumber.substring(7);


            String sql = null;

            if (offiDoc!=null){

                int offiDocLength = offiDoc.length();
                String lowerOffiDoc = offiDoc.toLowerCase();


                sql = "SELECT * FROM docotel.reg_to_path_readable " +
                        "WHERE FILE_TYP = :1 AND FILE_SEQ = :2 AND FILE_SER = :3 AND FILE_NBR = :4 " +
                        " AND (LOWER(SUBSTR(ALT_1 , -"+offiDocLength+")) = :5  OR " +
                        "LOWER(SUBSTR(ALT_2 , -"+offiDocLength+")) = :6 )";

//                System.out.println("pathnya"+path+" querynya"+sql+"findPDFDOCBlobIPASS "+lowerOffiDoc+" "+fileType+" "+fileSeq+" "+fileSer+" "+fileNumber);

                int i = 0;
                //step1 load the driver class
                Class.forName("oracle.jdbc.driver.OracleDriver");
                //step2 create  the connection object
                conn = DriverManager.getConnection("jdbc:oracle:thin:@10.1.18.30:1521:IPAS1","docotel","docotel123");
                //step3 create the statement object
                ps = conn.prepareStatement(sql);

                //step4 insert parameter
                ps.setString(1,fileType);
                ps.setString(2,fileSeq);
                ps.setString(3,fileSer);
                ps.setString(4,fileNumber);
                ps.setString(5,lowerOffiDoc);
                ps.setString(6,lowerOffiDoc);

                //step4 execute query
                rs = ps.executeQuery();

//                System.out.println("pathnya"+rs);
                if(rs!=null){
                    if(rs.next()){
                        path = rs.getString(rs.findColumn(COLUMN_ALT_1)) ;
                        String fullPath = this.uploadIPASDoc + "/" + path;

                        File file = new File ( fullPath );
                        if (!file.exists() || file.isDirectory())  {
                            path = rs.getString(rs.findColumn(COLUMN_ALT_2)) ;
                        }
//                        else {path=null;}

                    }
                }

            }


            rs.close();
            ps.close();
            conn.close();
//            System.out.println("kenapa null 2 "+path);
            return path;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return path;
    }

       public String findBlobDocIPASS(String officeDoc) {

        String path = "" ;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        if (isValidOfficeDocument(officeDoc)){

            try {

                String fixDocName = officeDoc.substring(0, officeDoc.length()-4);
                String contentType = officeDoc.substring(officeDoc.length()-3);

                String offidocOri = fixDocName.substring(0, 2);
                String offidocSer = fixDocName.substring(2,6);
                String offidocNumber = fixDocName.substring(6);

//                logger.info("OFFICEDOC NO : "+officeDoc);
//                logger.info("OFFICEDOC NBR : "+offidocNumber);

                String sql = "SELECT * FROM PRODDGIPR.IP_OFFIDOC a, PRODDGIPR.IP_ACTION b WHERE a.PROC_TYP=b.PROC_TYP and a.PROC_NBR=b.PROC_NBR and a.OFFIDOC_ORI=b.OFFIDOC_ORI and a.OFFIDOC_SER=b.OFFIDOC_SER and a.OFFIDOC_NBR=b.OFFIDOC_NBR and a.CONTENT_DATA IS NOT NULL AND a.PROC_TYP=1  AND b.OFFIDOC_ORI = :1 AND b.OFFIDOC_SER = :2 AND b.OFFIDOC_NBR = :3";

//                logger.info("QUERY == "+sql);

                int i = 0;
                //step1 load the driver class
                Class.forName("oracle.jdbc.driver.OracleDriver");
                //step2 create  the connection object
                conn = DriverManager.getConnection("jdbc:oracle:thin:@10.1.18.30:1521:IPAS1","docotel","docotel123");
                //step3 create the statement object
                ps = conn.prepareStatement(sql);

                //step4 insert parameter
                ps.setString(1,offidocOri);
                ps.setString(2,offidocSer);
                ps.setString(3,offidocNumber);

                //step4 execute query
                rs = ps.executeQuery();

                while (rs.next()) {

                    String filename = rs.getString("OFFIDOC_ORI") + rs.getString("OFFIDOC_SER") + rs.getString("OFFIDOC_NBR") ;
                    String pathFolder = DateUtil.formatDate(rs.getDate("ACTION_DATE"), "yyyy/MM/dd/");
                    Path paths = Paths.get(downloadLettersFilePath + pathFolder + filename + "." +contentType);
                    path = downloadLettersFilePath + pathFolder + filename + "." +contentType;
                    logger.info("PATH BLOB OFFICEDOC = " +path);

                    if (rs.getBlob("CONTENT_DATA") != null) {

                        java.sql.Blob blob = rs.getBlob("CONTENT_DATA");
                        byte[] bytes = blob.getBytes(1, (int) blob.length());

                        if (!Files.exists(paths.getParent())) {
                            Files.createDirectories(paths.getParent());
                        }
                        Files.write(paths, bytes);
                    }

                }

                rs.close();
                ps.close();
                conn.close();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return path ;
    }

    private static boolean isValidOfficeDocument(String officeDoc){
        return officeDoc.length() > 0 ? true : false ;
    }

}
