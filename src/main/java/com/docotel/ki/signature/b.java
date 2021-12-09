package com.docotel.ki.signature;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTCTime;
import org.bouncycastle.asn1.pkcs.IssuerAndSerialNumber;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.X509Name;

public final class b implements PKCSObjectIdentifiers {
    private int a;
    private int b;
    private Set c;
    private Collection d;
    private Collection e;
    private X509Certificate f;
    private byte[] g;
    private String h;
    private String i;
    private Signature j;
    private Calendar k;

    public b(PrivateKey var1, Certificate[] var2, String var3) throws Exception {
        this(var1, var2, var3, "BC");
    }

    private b(PrivateKey var1, Certificate[] var2, String var3, String var4) throws Exception {
        this(var1, var2, (CRL[])null, var3, var4);
    }

    private b(PrivateKey var1, Certificate[] var2, CRL[] var3, String var4, String var5) throws Exception {
        if (var4.equals("MD5")) {
            this.h = "1.2.840.113549.2.5";
        } else if (var4.equals("MD2")) {
            this.h = "1.2.840.113549.2.2";
        } else if (var4.equals("SHA")) {
            this.h = "1.3.14.3.2.26";
        } else {
            if (!var4.equals("SHA1")) {
                throw new NoSuchAlgorithmException("Unknown Hash Algorithm " + var4);
            }

            this.h = "1.3.14.3.2.26";
        }

        this.a = this.b = 1;
        this.d = new ArrayList();
        this.e = new ArrayList();
        this.c = new HashSet();
        this.c.add(this.h);
        this.f = (X509Certificate)var2[0];

        for(int var6 = 0; var6 < var2.length; ++var6) {
            this.d.add(var2[var6]);
        }

        this.i = var1.getAlgorithm();
        if (this.i.equals("RSA")) {
            this.i = "1.2.840.113549.1.1.1";
        } else {
            if (!this.i.equals("DSA")) {
                throw new NoSuchAlgorithmException("Unknown Key Algorithm " + this.i);
            }

            this.i = "1.2.840.10040.4.1";
        }

        String var7 = this.h;
        var4 = this.i;
        if (this.h.equals("1.2.840.113549.2.5")) {
            var7 = "MD5";
        } else if (this.h.equals("1.2.840.113549.2.2")) {
            var7 = "MD2";
        } else if (this.h.equals("1.3.14.3.2.26")) {
            var7 = "SHA1";
        }

        if (this.i.equals("1.2.840.113549.1.1.1")) {
            var4 = "RSA";
        } else if (this.i.equals("1.2.840.10040.4.1")) {
            var4 = "DSA";
        }

        this.j = Signature.getInstance(var7 + "with" + var4, var5);
        this.j.initSign(var1);
    }

    public final void a (byte[] var1, int var2, int var3) throws Exception {
        this.j.update(var1, 0, var3);
    }

    private static DERObject c(byte[] var0) {
        try {
            ASN1Sequence var2;
            return (DERObject)(var2 = (ASN1Sequence)(new ASN1InputStream(new ByteArrayInputStream(var0))).readObject()).getObjectAt(var2.getObjectAt(0) instanceof DERTaggedObject ? 3 : 2);
        } catch (IOException var1) {
            throw new Error("IOException reading from ByteArray: " + var1);
        }
    }

    public final DERSet a(byte[] var1) throws Exception {
        try {
            ASN1EncodableVector var2 = new ASN1EncodableVector();
            ASN1EncodableVector var3;
            (var3 = new ASN1EncodableVector()).add(new DERObjectIdentifier("1.2.840.113549.1.9.3"));
            var3.add(new DERSet(new DERObjectIdentifier("1.2.840.113549.1.7.1")));
            var2.add(new DERSequence(var3));
            (var3 = new ASN1EncodableVector()).add(new DERObjectIdentifier("1.2.840.113549.1.9.5"));
            var3.add(new DERSet(new DERUTCTime(this.k.getTime())));
            var2.add(new DERSequence(var3));
            (var3 = new ASN1EncodableVector()).add(new DERObjectIdentifier("1.2.840.113549.1.9.4"));
            var3.add(new DERSet(new DEROctetString(var1)));
            var2.add(new DERSequence(var3));
            return new DERSet(var2);
        } catch (Exception var4) {
            throw new Exception(var4);
        }
    }

    public final byte[] b(byte[] var1) {
        try {
            this.g = this.j.sign();
            ASN1EncodableVector var2 = new ASN1EncodableVector();
            Iterator var3 = this.c.iterator();

            while(var3.hasNext()) {
                AlgorithmIdentifier var4 = new AlgorithmIdentifier(new DERObjectIdentifier((String)var3.next()), (DEREncodable)null);
                var2.add(var4);
            }

            DERSet var14 = new DERSet(var2);
            DERSequence var17 = new DERSequence(new DERObjectIdentifier("1.2.840.113549.1.7.1"));
            var2 = new ASN1EncodableVector();
            Iterator var5 = this.d.iterator();

            while(var5.hasNext()) {
                ASN1InputStream var6 = new ASN1InputStream(new ByteArrayInputStream(((X509Certificate)var5.next()).getEncoded()));
                var2.add(var6.readObject());
            }

            DERSet var18 = new DERSet(var2);
            ASN1EncodableVector var19;
            (var19 = new ASN1EncodableVector()).add(new DERInteger(this.b));
            IssuerAndSerialNumber var13 = new IssuerAndSerialNumber(new X509Name((ASN1Sequence)c(this.f.getTBSCertificate())), new DERInteger(this.f.getSerialNumber()));
            var19.add(var13);
            var19.add(new AlgorithmIdentifier(new DERObjectIdentifier(this.h), new DERNull()));
            if (var1 != null && this.k != null) {
                var19.add(new DERTaggedObject(false, 0, this.a(var1)));
            }

            var19.add(new AlgorithmIdentifier(new DERObjectIdentifier(this.i), new DERNull()));
            var19.add(new DEROctetString(this.g));
            ASN1EncodableVector var11;
            (var11 = new ASN1EncodableVector()).add(new DERInteger(this.a));
            var11.add(var14);
            var11.add(var17);
            var11.add(new DERTaggedObject(false, 0, var18));
            if (this.e.size() > 0) {
                var2 = new ASN1EncodableVector();
                Iterator var8 = this.e.iterator();

                while(var8.hasNext()) {
                    ASN1InputStream var15 = new ASN1InputStream(new ByteArrayInputStream(((X509CRL)var8.next()).getEncoded()));
                    var2.add(var15.readObject());
                }

                DERSet var9 = new DERSet(var2);
                var11.add(new DERTaggedObject(false, 1, var9));
            }

            var11.add(new DERSet(new DERSequence(var19)));
            ASN1EncodableVector var10;
            (var10 = new ASN1EncodableVector()).add(new DERObjectIdentifier("1.2.840.113549.1.7.2"));
            var10.add(new DERTaggedObject(0, new DERSequence(var11)));
            ByteArrayOutputStream var16 = new ByteArrayOutputStream();
            DEROutputStream var12;
            (var12 = new DEROutputStream(var16)).writeObject(new DERSequence(var10));
            var12.close();
            return var16.toByteArray();
        } catch (Exception var7) {
            throw new RuntimeException(var7.toString());
        }
    }

    public final void a(Calendar var1) {
        this.k = var1;
    }
}
