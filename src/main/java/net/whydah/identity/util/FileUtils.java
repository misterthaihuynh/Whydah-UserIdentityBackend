package net.whydah.identity.util;

import net.whydah.identity.config.AppConfig;
import net.whydah.identity.config.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        if(!file.delete()) {
                            log.warn("Unable to delete directory file " + file);
                        }
                    }
                }
            }
        }
        boolean exist = path.exists();
        boolean deleted = path.delete();
        if(exist && !deleted)  {
            log.warn("Unable to delete directory " + path);
        }

        log.info("Folder {} was deleted successfully.", path.getAbsolutePath());
    }

    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                log.trace("Exception closing InputStream. Doing nothing.", e);
            }
        }
    }

    public static boolean localFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static InputStream openLocalFile(String fileName) {
        File file = new File(fileName);
        FileInputStream fis = null;
        try {
            if (file.exists()) {
                fis = new FileInputStream(file);
            } else {
                throw new ConfigurationException("Config file " + fileName + " does not exist.");
            }
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Config file " + fileName +  " not found.", e);
        } catch (IOException e) {
            throw new ConfigurationException("Error reading " + fileName , e);
        }
        return fis;

    }

    public static InputStream openFileOnClasspath(String fileName) {
        InputStream is = AppConfig.class.getClassLoader().getResourceAsStream(fileName);
        if(is == null) {
            throw new ConfigurationException("Error reading " + fileName + " from classpath.");
        }
        return is;
    }
}
