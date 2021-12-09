package com.docotel.ki.signature;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.awt.Rectangle;
import java.io.*;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class PDFSignatureFacade {
    public PDFSignatureFacade() {
    }

    public void sign(Certificate[] var1, PrivateKey var2, InputStream var3, OutputStream var4) {
        a var5;
        (var5 = new a()).a(false);
        var5.a(var1, var2);
        var5.a(var3, var4);
    }

    public void sign(Certificate[] var1, PrivateKey var2, String var3, String var4)  throws FileNotFoundException {
        this.sign((Certificate[])var1, (PrivateKey)var2, (InputStream)(new FileInputStream(var3)), (OutputStream)(new FileOutputStream(var4)));
    }

    public void sign(String var1, String var2, InputStream var3, OutputStream var4, boolean var5, Rectangle var6) throws Exception{
        a var7 = new a();
        if (var6 != null) {
            var7.a(var6);
        }

        var7.a(var5);
        var7.a(var1, var2);
        var7.a(var3, var4);
    }

    public void sign(String var1, String var2, String var3, String var4) throws Exception {
        this.sign(var1, var2, (InputStream)(new FileInputStream(var3)), (OutputStream)(new FileOutputStream(var4)), false, (Rectangle)null);
    }

    public void sign(String var1, String var2, String var3, String var4, boolean var5, Rectangle var6)  throws Exception {
        this.sign(var1, var2, (InputStream)(new FileInputStream(var3)), (OutputStream)(new FileOutputStream(var4)), var5, var6);
    }
}
