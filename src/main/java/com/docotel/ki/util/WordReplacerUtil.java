package com.docotel.ki.util;

import com.docotel.ki.model.transaction.TxTmBrand;
import com.docotel.ki.repository.transaction.TxTmBrandRepository;
import com.xandryex.utils.TextReplacer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordReplacerUtil {
    private TxTmBrandRepository brandRepository;

    private String uploadFileBrandPath;

    private XWPFDocument document;
    private TextReplacer replacer;
    public static final double DEFAULT_SCALE = 0.5;

    /**
     * Creates WordReplacer with file to modify.
     *
     * @param docxFile file of type docx.
     * @throws IOException thrown if file is not found or is not required type.
     */
    public WordReplacerUtil(File docxFile) throws IOException {
        InputStream inputStream = new FileInputStream(docxFile);
        init(new XWPFDocument(inputStream));
    }

    public WordReplacerUtil(File docxFile, TxTmBrandRepository brandRepository, String uploadFileBrandPath) throws IOException {
        this(docxFile);
        this.brandRepository = brandRepository;
        this.uploadFileBrandPath = uploadFileBrandPath;
    }

    /**
     * Creates WordReplacer with XWPFDocument to modify.
     * @param xwpfDoc to modify.
     */
    public WordReplacerUtil(XWPFDocument xwpfDoc) {
        init(xwpfDoc);
    }

    public WordReplacerUtil(XWPFDocument xwpfDoc, TxTmBrandRepository brandRepository, String uploadFileBrandPath) {
        this(xwpfDoc);
        this.brandRepository = brandRepository;
        this.uploadFileBrandPath = uploadFileBrandPath;
    }

    private void init(XWPFDocument xwpfDoc) {
        if (xwpfDoc == null) throw new NullPointerException();
        document = xwpfDoc;
        replacer = new TextReplacer();
    }

    /**
     * Replaces all occurrences of a bookmark only in the text of the file with a replacement string.
     * @param bookmark word to replace.
     * @param replacement word of replacement.
     */
    public void replaceWordsInText(String bookmark, String replacement) {
        replacer.replaceInText(document, bookmark.toLowerCase(), replacement);
        replacer.replaceInText(document, bookmark.toUpperCase(), replacement);
    }

    /**
     * Replaces all occurrences of a bookmark only in tables of the file with a replacement string.
     * @param bookmark word to replace.
     * @param replacement word of replacement.
     */
    public void replaceWordsInTables(String bookmark, String replacement) {
    	replacer.replaceInTable(document, bookmark.toLowerCase(), replacement);
    	replacer.replaceInTable(document, bookmark.toUpperCase(), replacement);
    }

    /**
     * Most of the time we want our template files untouched. Creates file from path, saves the modified document to it and returns it.
     * @param path filepath (dirs + filename).
     * @return modified file.
     * @throws Exception thrown if some issues while saving occur - mostly due to unavailable file or permissions.
     */
    public File saveAndGetModdedFile(String path) throws Exception {
        File file = new File(path);
        return saveToFile(file);
    }

    /**
     * Most of the time we want our template files untouched. Saves the modified document to the given file and returns it.
     * @param file to save to.
     * @return modified file.
     * @throws Exception thrown if some issues while saving occur - mostly due to unavailable file or permissions.
     */
    public File saveAndGetModdedFile(File file) throws Exception {
        return saveToFile(file);
    }

    public XWPFDocument getModdedXWPFDoc() {
        return document;
    }

    private File saveToFile(File file) throws Exception {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, false);
            document.write(out);
            document.close();
            return file;
        } catch (Exception e) {
            throw e;
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
    
    public void replaceImageInTable(String variable, File figureFile, int maxSize) throws Exception {
        List<XWPFTable> tables = this.document.getTables();
        for (XWPFTable tbl : tables) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        for (XWPFRun r : p.getRuns()) {
                            String text = r.getText(0);
                            if (text != null && text.contains(variable)) {
                                addImageToRun(figureFile, maxSize, r);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void replaceNewlineTabInTable() throws InvalidFormatException, FileNotFoundException, IOException {
        List<XWPFTable> tables = this.document.getTables();
        for (XWPFTable tbl : tables) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        for (XWPFRun r : p.getRuns()) {
                            String text = r.getText(0);
                            if (text != null && (text.contains("\n") || text.contains("\t") || text.contains(".jpg"))) {
                                int pos = 0;
                                String[] sp1s = text.split("\n");
                                String spliter = "(([\\w-]*)\\.jpg)";
                                for (String sp1: sp1s) {
                                     String[] sp2s = sp1.split("\t");
                                     for (String sp2: sp2s) {
                                         Matcher matcher = Pattern.compile(spliter).matcher(sp2);
                                         while(matcher.find()) {
                                             String imageFull = matcher.group(1);
                                             String imageID = matcher.group(2);
                                             TxTmBrand txTmBrand = brandRepository.findOne(imageID);
                                             String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
                                             String imgFile = uploadFileBrandPath + pathFolder + imageID + ".jpg";
                                             r.setText(sp2.substring(0, sp2.indexOf(imageFull)), pos++);
                                             r.addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_JPEG, imgFile, Units.toEMU(100), Units.toEMU(100));
                                             sp2 = sp2.substring(sp2.indexOf(imageID) + imageFull.length());
                                         }
                                         r.setText(sp2, pos++);
                                         r.addTab();
                                     }
                                     r.addBreak();
                                 }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addImageToRun(File image, int maxSize, XWPFRun run) throws Exception {
        FileInputStream inputStream = new FileInputStream(image);
        final BufferedImage bufferedImage = ImageIO.read(inputStream);
        final int widthImage = bufferedImage.getWidth();
        final int heightImage = bufferedImage.getHeight();
        double scale = 1;
        
        if (widthImage > maxSize || heightImage > maxSize) {
            if (widthImage > heightImage) {
            	scale = (double) (100 / (widthImage / maxSize)) / 100;
            } else {
            	scale = (double) (100 / (heightImage / maxSize)) / 100;
            }
        }
        
        final int width = (int) Math.ceil(Units.toEMU(bufferedImage.getWidth() * scale));
        final int height = (int) Math.ceil(Units.toEMU(bufferedImage.getHeight() * scale));
        inputStream.close();

        run.setText("", 0);
        inputStream = new FileInputStream(image);
        //run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, image.getName(), width, height);
        run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PICT, image.getName(), width, height);
        inputStream.close();
    }

    public void replaceImageInTable(String variable, File figureFile) throws Exception {
        List<XWPFTable> tables = this.document.getTables();
        for (XWPFTable tbl : tables) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        for (XWPFRun r : p.getRuns()) {
                            String text = r.getText(0);
                            if (text != null && text.contains(variable)) {
                                addImageToRun(figureFile, DEFAULT_SCALE, r);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addImageToRun(File image, double scale, XWPFRun run) throws Exception {
        FileInputStream inputStream = new FileInputStream(image);
        final BufferedImage bufferedImage = ImageIO.read(inputStream);
        final int width = (int) Math.ceil(Units.toEMU(bufferedImage.getWidth() * scale));
        final int height = (int) Math.ceil(Units.toEMU(bufferedImage.getHeight() * scale));
        inputStream.close();

        run.setText("", 0);
        inputStream = new FileInputStream(image);
        //run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, image.getName(), width, height);
        run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PICT, image.getName(), width, height);
        inputStream.close();
    }

    public void replaceTextInFooter(String findText, String replacement){
        this.replaceTextInFooter(document, findText.toLowerCase(), replacement);
        this.replaceTextInFooter(document, findText.toUpperCase(), replacement);
    }
    public void replaceTextInFooter(XWPFDocument doc, String findText, String replaceText) {
        for (XWPFFooter footer : doc.getFooterList()) {
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String text = run.text();
                    if (text.contains(findText)) {
                        run.setText(replaceText, 0);
                    }
                }
            }
        }
    }
}
