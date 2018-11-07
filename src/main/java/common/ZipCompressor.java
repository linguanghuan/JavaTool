package common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressor {

    static final int BUFFER = 8192;

    private File zipFile;

    public ZipCompressor(String pathName) {
        zipFile = new File(pathName);
    }

    public void compress(Set<String> pathList, String prefix, Set<String> ignoreSuffixSet) {
        ZipOutputStream out = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
            out = new ZipOutputStream(cos);
            int i = 0;
            for (String path : pathList) {
                String basedir = path.substring(prefix.length());
                compress(new File(path), out, basedir, ignoreSuffixSet);
                if (i % 10 == 0) {
                    System.out.println("===============:" +i + "/" + pathList.size() + ", " + ((double) i / pathList.size()));
                }
                i++;
            }
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void compress(File file, ZipOutputStream out, String basedir,Set<String> ignoreSuffixSet) {
        if (file.isDirectory()) {
            System.out.println("压缩：" + basedir + file.getName());
            this.compressDirectory(file, out, basedir, ignoreSuffixSet);
        } else {
            if (ignoreSuffixSet != null) {
                for (String ignoreSuffix: ignoreSuffixSet) {
                    if (file.getName().endsWith(ignoreSuffix) ) {
                        System.out.println("忽略:" + basedir + file.getName());
                        return;
                    }
                }
            }
            System.out.println("压缩：" + basedir + file.getName());
            this.compressFile(file, out, basedir);
        }
    }

    /**
     * 压缩一个目录
     */
    private void compressDirectory(File dir, ZipOutputStream out, String basedir, Set<String> ignoreSuffixSet) {
        if (!dir.exists())
            return;

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            /* 递归 */
            compress(files[i], out, basedir + "/", ignoreSuffixSet);
        }
    }

    /**
     * 压缩一个文件
     */
    private void compressFile(File file, ZipOutputStream out, String basedir) {
        if (!file.exists()) {
            return;
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry(basedir + file.getName());
            entry.setTime(file.lastModified());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            bis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
