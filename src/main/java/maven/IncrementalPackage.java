package maven;

import common.ZipCompressor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 增量打包
 */
public class IncrementalPackage {

    private static final String REPOSITORY_DEFAULT_LOCATION = System.getProperty("user.home")
            + File.separator + ".m2"
            + File.separator + "repository";

    // 默认从当前时间的24小时前开始增量打包
    private static final int DEFAULT_START = 24;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static final String OUTPUT_FILE = System.getProperty("user.home")
            + File.separator + ".m2" + File.separator + "output.zip";

    public static void main(String[] args) throws IOException {
        int startHour = DEFAULT_START;
        String location = REPOSITORY_DEFAULT_LOCATION;
        if (args.length == 2) {
            startHour = Integer.valueOf(args[0]);
            location = args[1];
        }
        long begin = System.currentTimeMillis();
        Date startPoint = new Date(new Date().getTime() - startHour * 3600 * 1000);
        System.out.println("增量打包, 位置:"+ location + ", 开始时间：" + startPoint);
        Set<String> pathToPack = new HashSet<String>();
        File startDir = new File(location);
        walkForMavenFiles(startDir, startPoint, pathToPack);
        System.out.println("size:" + pathToPack.size());
        ZipCompressor zc = new ZipCompressor(OUTPUT_FILE);
        Set<String> ignoreSuffixSet = new HashSet<String>();
        ignoreSuffixSet.add(".repositories");
        ignoreSuffixSet.add(".lastUpdated");
        zc.compress(pathToPack, location.replace("repository", ""), ignoreSuffixSet);
        System.out.println("elapse:" + (System.currentTimeMillis() - begin));
    }

    private static void walkForMavenFiles(File startDir, Date startPoint, Set<String> pathToPack) {
        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    walkForMavenFiles(file, startPoint, pathToPack);
                } else {
                    if (file.getName().toLowerCase().endsWith(".jar") || file.getName().toLowerCase().endsWith(".pom")) {
                        if (file.lastModified() > startPoint.getTime()) {
//                            String dateStr = sdf.format(new Date(file.lastModified()));
//                            System.out.println("found new: " + file.getParent() + ", last modified:" + dateStr);
                            pathToPack.add(file.getParent());
                        }
                    }
                }
            }
        }
    }
}
