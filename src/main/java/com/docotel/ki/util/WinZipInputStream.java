package com.docotel.ki.util;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

public class WinZipInputStream extends FilterInputStream {
    public static final byte[] ZIP_LOCAL = { 0x50, 0x4b, 0x03, 0x04 };
    protected int ip;
    protected int op;

    public WinZipInputStream(InputStream is) {
        super(is);
    }

    public int read() throws IOException {
        while(ip < ZIP_LOCAL.length) {
            int c = super.read();
            if (c == ZIP_LOCAL[ip]) {
                ip++;
            }
            else ip = 0;
        }

        if (op < ZIP_LOCAL.length)
            return ZIP_LOCAL[op++];
        else
            return super.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (op == ZIP_LOCAL.length) return super.read(b, off, len);
        int l = 0;
        while (l < Math.min(len, ZIP_LOCAL.length)) {
            b[l++] = (byte)read();
        }
        return l;
    }
}
