package com.docotel.ki.pojo;

public class PdfFile {

    private String intregn;

    private String trantyp;

    private String docid;

    private String pathFrom;

    private String pathTo;

    private long byteSize;

    private String fileName;

    public PdfFile(String intregn, String trantyp, String docid, String pathFrom, long byteSize, String fileName) {
        this.intregn = intregn;
        this.trantyp = trantyp;
        this.docid = docid;
        this.pathFrom = pathFrom;
        this.byteSize = byteSize;
        this.fileName = fileName;
    }

    public String getIntregn() {
        return intregn;
    }

    public String getTrantyp() {
        return trantyp;
    }

    public String getDocid() {
        return docid;
    }

    public String getPathFrom() {
        return pathFrom;
    }

    public long getByteSize() {
        return byteSize;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "PdfFile{" +
                "intregn='" + intregn + '\'' +
                ", trantyp='" + trantyp + '\'' +
                ", docid='" + docid + '\'' +
                ", pathFrom='" + pathFrom + '\'' +
                ", byteSize=" + byteSize +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
