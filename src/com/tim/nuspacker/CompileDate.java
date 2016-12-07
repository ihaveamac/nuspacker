package com.tim.nuspacker;

import java.io.IOException;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class CompileDate {
    public void printDate() {
        JarFile jf ;
        try {
            jf = new JarFile("nuspacker.jar");
            ZipEntry manifest = jf.getEntry("META-INF/MANIFEST.MF");
            long manifestTime = manifest.getTime();  //in standard millis
            jf.close();
            System.out.print(" - " +  new Date(manifestTime).toString());
        } catch (IOException e) {
        }
    }
}
