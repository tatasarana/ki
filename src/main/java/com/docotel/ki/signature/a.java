package com.docotel.ki.signature;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.awt.Color;
import java.awt.Rectangle;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageNode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class a {
    private COSDictionary a = new COSDictionary();
    private COSDictionary b = new COSDictionary();
    private Hashtable c = new Hashtable();
    private Rectangle d = new Rectangle(5, 5, 100, 100);
    private static boolean e = false;
    private static int f = 0;
    private boolean g = true;
    private PrivateKey h = null;
    private Certificate[] i = null;

    public a() {
    }

    private static synchronized void a() {
        if (!e) {
            e = true;
            Security.addProvider(new BouncyCastleProvider());
        }

    }

    public final void a(Certificate[] var1, PrivateKey var2) {
        this.h = var2;
        this.i = var1;
    }

    public final void a(String var1, String var2) throws Exception {
        a();
        KeyStore var3 = KeyStore.getInstance("PKCS12", "BC");
        FileInputStream var6 = new FileInputStream(new File(var1));
        char[] var8 = var2.toCharArray();
        var3.load(var6, var8);
        var6.close();
        Enumeration var7 = var3.aliases();

        while(var7.hasMoreElements()) {
            String var4 = (String)var7.nextElement();
            var3.isCertificateEntry(var4);
            if (var3.isKeyEntry(var4)) {
                Key var5;
                if ((var5 = var3.getKey(var4, var8)) instanceof PrivateKey) {
                    this.h = (PrivateKey)var5;
                }

                if (this.i == null) {
                    this.i = var3.getCertificateChain(var4);
                }
            }

            if (var3.isCertificateEntry(var4) && this.i == null) {
                this.i = var3.getCertificateChain(var4);
            }

            if (var3.isCertificateEntry(var4)) {
                var3.getCertificate(var4);
            }
        }

    }

    private static int[] a(byte[] var0, byte[] var1, int var2) {
        for(int var3 = 0; var3 < var0.length; ++var3) {
            if (var0[var3] != var1[var3]) {
                int[] var4;
                (var4 = new int[4])[0] = 0;
                var4[1] = var3 - 1;
                var4[2] = var3 + var2 + 1;
                var4[3] = var1.length - var4[2];
                return var4;
            }
        }

        return null;
    }

    private int[] a(int[] var1, int[] var2) {
        int var5 = a(var1);
        int var3 = a(var2);

        for(int var4 = 0; var4 < 10; ++var4) {
            var1[0] = var2[0];
            var1[1] = var2[1];
            var1[2] = var2[2];
            var1[3] = var2[3];
            if ((var5 = var3 - var5) == 0) {
                return var1;
            }

            var2[3] += var5;
            var5 = a(var1);
            var3 = a(var2);
        }

        return null;
    }

    private static int a(int[] var0) {
        String var1 = "";

        for(int var2 = 0; var2 < 4; ++var2) {
            if (var2 > 0) {
                var1 = var1 + " ";
            }

            var1 = var1 + var0[var2];
        }

        return var1.length();
    }

    private String a(byte[] var1, Calendar var2) throws Exception{
        MessageDigest var3;
        (var3 = MessageDigest.getInstance("SHA1")).update(var1, 0, var1.length);
        var1 = var3.digest();
        b var4;
        (var4 = new b(this.h, this.i, "SHA1")).a(var2);
        byte[] var7 = var4.a(var1).getEncoded("DER");
        var4.a(var7, 0, var7.length);
        byte[] var5 = var4.b(var1);
        String var6 = "";

        for(int var8 = 0; var8 < var5.length; ++var8) {
            if (var5[var8] < 0) {
                var5[var8] = (byte)(var5[var8] + 256);
            }

            String var9;
            if ((var9 = Integer.toHexString(255 & var5[var8])).length() == 1) {
                var9 = "0" + var9;
            }

            var6 = var6 + var9;
        }

        return var6;
    }

    public final void a(InputStream var1, OutputStream var2) {
//        ++f;
//        System.out.println("\n*\n* Evaluation version of J4L PDF signature component\n*");
//        if (f > 20) {
//            System.out.println("*** Maximum usage exceeded ***");
//        }

//        if (f <= 20 || true) {
        if (true) {
            String var3 = "";

            try {
                Object var4;
                PDDocument var57;
                for(var4 = (var57 = PDDocument.load(var1)).getDocumentCatalog().getPages().getKids().get(0); var4 instanceof PDPageNode; var4 = ((PDPageNode)var4).getKids().get(0)) {
                }

                PDPage var60 = (PDPage)var4;
                if (var57.isEncrypted()) {
                    var57.decrypt(var3);
                }

                COSDictionary var5;
                COSDictionary var58;
                if ((var5 = (COSDictionary)(var58 = (COSDictionary)var57.getDocument().getTrailer().getDictionaryObject(COSName.ROOT)).getDictionaryObject(COSName.ACRO_FORM)) == null) {
                    var5 = new COSDictionary();
                    var58.setItem(COSName.ACRO_FORM, var5);
                }

                COSArray var59 = (COSArray)var5.getDictionaryObject(COSName.FIELDS);
                var5.setItem("SigFlags", COSInteger.get(3L));
                if (var59 == null) {
                    var59 = new COSArray();
                    var5.setItem(COSName.FIELDS, var59);
                }

                (var5 = new COSDictionary()).setName("Type", "Sig");
                var5.setName(COSName.FILTER, "Adobe.PPKLite");
                var5.setName("SubFilter", "adbe.pkcs7.detached");
                Calendar var6 = Calendar.getInstance();
                byte[] var7 = new byte[10000];
                int var61 = this.a(var7, var6).length();
                StringBuffer var8 = new StringBuffer(var61);

                for(int var9 = 0; var9 < var61; ++var9) {
                    var8.append("0");
                }

                String var63 = var8.toString();
                var5.setItem(COSName.CONTENTS, new COSString("X"));
                COSArray var62;
                (var62 = new COSArray()).add(COSInteger.get(0L));
                var62.add(COSInteger.get(0L));
                var62.add(COSInteger.get(0L));
                var62.add(COSInteger.get(0L));
                var5.setItem("ByteRange", var62);
                Date var10 = Calendar.getInstance().getTime();
                SimpleDateFormat var11 = new SimpleDateFormat("yyyyMMddHHmmssZ");
                String var65 = "D:" + var11.format(var10);
                var65 = var65.substring(0, var65.length() - 2) + "'" + var65.substring(var65.length() - 2, var65.length()) + "'";
                var5.setItem("M", new COSString(var65));
                var5.setItem("Location", new COSString(""));
                var5.setItem("Reason", new COSString(""));
                var5.setItem("ContactInfo", new COSString(""));
                COSDictionary var66;
                (var66 = new COSDictionary()).setName("FT", "Sig");
                var66.setItem(COSName.T, new COSString("Signature"));
                var66.setItem(COSName.V, var5);
                COSArray var12;
                int var71;
                if (!this.g) {
                    (var12 = new COSArray()).add(COSInteger.get(0L));
                    var12.add(COSInteger.get(0L));
                    var12.add(COSInteger.get(0L));
                    var12.add(COSInteger.get(0L));
                    var66.setItem("Rect", var12);
                    var66.setItem("P", var60);
                    var66.setItem("F", COSInteger.get(1L));
                } else {
                    (var12 = new COSArray()).add(COSInteger.get((long)this.d.x));
                    var12.add(COSInteger.get((long)this.d.y));
                    var12.add(COSInteger.get((long)(this.d.x + this.d.width)));
                    var12.add(COSInteger.get((long)(this.d.y + this.d.height)));
                    var66.setName("Type", "Annot");
                    var66.setName("Subtype", "Widget");
                    var66.setItem("Rect", var12);
                    var66.setItem("P", var60);
                    var66.setItem("F", COSInteger.get(132L));
                    PDAnnotationWidget var13 = (PDAnnotationWidget)PDAnnotation.createAnnotation(var66);
                    BoundingBox var67;
                    (var67 = new BoundingBox()).setLowerLeftX((float)this.d.x);
                    var67.setLowerLeftY((float)this.d.y);
                    var67.setUpperRightX((float)(this.d.x + this.d.width));
                    var67.setUpperRightY((float)(this.d.y + this.d.height));
                    COSStream var14;
                    (var14 = new COSStream(new RandomAccessBuffer())).setItem(COSName.getPDFName("BBox"), new PDRectangle(var67));
                    var14.setItem(COSName.getPDFName("Type"), COSName.getPDFName("XObject"));
                    var14.setItem(COSName.getPDFName("Subtype"), COSName.getPDFName("Form"));
                    var14.setItem(COSName.getPDFName("FormType"), COSInteger.get(1L));
                    this.a.setItem(COSName.getPDFName("Font"), this.b);
                    var14.setItem(COSName.getPDFName("Resources"), this.a);
                    (var12 = new COSArray()).add(COSName.getPDFName("PDF"));
                    var12.add(COSName.getPDFName("Text"));
                    var14.setItem(COSName.getPDFName("ProcSet"), var12);
                    COSArray var15;
                    (var15 = new COSArray()).add(new COSFloat("1.0"));
                    var15.add(new COSFloat("0.0"));
                    var15.add(new COSFloat("0.0"));
                    var15.add(new COSFloat("1.0"));
                    var15.add(new COSFloat("0.0"));
                    var15.add(new COSFloat("0.0"));
                    var14.setItem(COSName.getPDFName("Matrix"), var15);
                    COSDictionary var76;
                    (var76 = new COSDictionary()).setItem(COSName.getPDFName("N"), var14);
                    PDAppearanceDictionary var16 = new PDAppearanceDictionary(var76);
                    var13.setAppearance(var16);
                    boolean var49 = false;
                    boolean var48 = false;
                    String var47 = "Times New Roman";
                    String var10000;
                    if (this.c.containsKey(var47)) {
                        var10000 = (String)this.c.get(var47);
                    } else {
                        String var51 = "J4LFONT" + this.c.size();
                        boolean var68 = false;
                        boolean var69 = false;
                        PDType1Font var52 = var47.equalsIgnoreCase("Arial") ? (var69 && var68 ? PDType1Font.HELVETICA_BOLD_OBLIQUE : (var69 ? PDType1Font.HELVETICA_BOLD : (var68 ? PDType1Font.HELVETICA_OBLIQUE : PDType1Font.HELVETICA))) : (var47.equalsIgnoreCase("ZapfDingbats") ? PDType1Font.ZAPF_DINGBATS : (var47.equalsIgnoreCase("Courier New") ? (var69 && var68 ? PDType1Font.COURIER_BOLD_OBLIQUE : (var69 ? PDType1Font.COURIER_BOLD : (var68 ? PDType1Font.COURIER_OBLIQUE : PDType1Font.COURIER))) : (var69 && var68 ? PDType1Font.TIMES_BOLD_ITALIC : (var69 ? PDType1Font.TIMES_BOLD : (var68 ? PDType1Font.TIMES_ITALIC : PDType1Font.TIMES_ROMAN)))));
                        this.b.setItem(COSName.getPDFName(var51), var52);
                        this.c.put(var47, var51);
                        var10000 = var51;
                    }

                    String var70 = var10000;
                    var71 = this.d.x;
                    int var77 = this.d.y + this.d.height - 10;
                    String var64 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(var10);
                    int var17;
                    String var81;
                    if ((var17 = (var81 = ((X509Certificate)this.i[0]).getSubjectDN().getName()).indexOf("CN=")) >= 0 && (var17 = (var81 = var81.substring(var17 + 3)).indexOf(",")) > 0) {
                        var81 = var81.substring(0, var17);
                    }

                    var81 = "Signed by " + var81;
                    Vector var83 = new Vector();

                    while(true) {
                        int var19;
                        while(var81.length() > 0) {
                            int var18 = (int)Math.floor((double)(this.d.width / 4));
                            if (var81.length() <= var18) {
                                var83.addElement(var81);
                                var81 = "";
                            } else {
                                var19 = var18;

                                for(int var20 = var18; var20 >= var18 / 2 && var81.charAt(var20) >= '0' && var81.charAt(var20) <= '9' && (var81.charAt(var20) < 'A' || var81.charAt(var20) > 'Z') && (var81.charAt(var20) < 'a' || var81.charAt(var20) > 'z'); --var20) {
                                    --var19;
                                }

                                var83.addElement(var81.substring(0, var19));
                                var81 = var81.substring(var19);
                            }
                        }

                        var83.addElement(" on " + var64);
                        StringBuffer var82 = (new StringBuffer()).append(" /Tx BMC BT ");
                        Color var85;
                        double var89 = (double)(var85 = Color.BLACK).getRed();
                        double var90 = (double)var85.getGreen();
                        double var91 = (double)var85.getBlue();
                        String var84 = var82.append(" " + var89 / 255.0D + " " + var90 / 255.0D + " " + var91 / 255.0D).append(" rg /").append(var70).append(" ").append(7).append(" Tf ").append(var71).append(" ").append(var77).append(" Td 10 TL ").toString();

                        for(var19 = 0; var19 < var83.size(); ++var19) {
                            var84 = var84 + " (" + (String)var83.elementAt(var19) + ") Tj " + " T* ";
                        }

                        var84 = var84 + "ET EMC";
                        String var87 = "q " + var84 + " Q";
                        OutputStream var88;
                        (var88 = var14.createFilteredStream()).write(var87.getBytes());
                        var88.flush();
                        var88.close();
                        var60.getAnnotations().add(var66);
                        break;
                    }
                }

                var59.add(var66);
                ByteArrayOutputStream var72 = new ByteArrayOutputStream();
                var57.save(var72);
                var5.setItem(COSName.CONTENTS, new COSString(var63));
                ByteArrayOutputStream var73 = new ByteArrayOutputStream();
                var57.save(var73);
                int[] var74 = a(var72.toByteArray(), var73.toByteArray(), var61);
                int[] var75 = new int[]{0, 0, 0, 0};
                var74 = this.a(var75, var74);
                var62.clear();
                var62.add(COSInteger.get((long)var74[0]));
                var62.add(COSInteger.get((long)var74[1]));
                var62.add(COSInteger.get((long)var74[2]));
                var62.add(COSInteger.get((long)var74[3]));
                ByteArrayOutputStream var79 = new ByteArrayOutputStream();
                var57.save(var79);
                byte[] var80 = var79.toByteArray();
                byte[] var86 = new byte[var74[1] + var74[3]];

                for(var71 = 0; var71 < var74[1]; ++var71) {
                    var86[var71] = var80[var71];
                }

                for(var71 = 0; var71 < var74[3]; ++var71) {
                    var86[var71 + var74[1]] = var80[var74[2] + var71];
                }

                String var78 = this.a(var86, var6);
                var78 = var78 + var63.substring(0, var63.length() - var78.length());
                var5.setItem(COSName.CONTENTS, new COSString((new BigInteger(var78, 16)).toByteArray()));
                var57.save(var2);
                var57.close();
            } catch (Exception var55) {
                var55.printStackTrace();
            } finally {
                ;
            }
        }
    }

    public final void a(boolean var1) {
        this.g = var1;
    }

    public final void a(Rectangle var1) {
        this.d = var1;
    }
}
