package net.petercashel.contentsync.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class WebHelper {

    public static String DownloadURLAsString(String url) throws IOException {
        String result = "";
        InputStream in = new URL( url ).openStream();


        try {
            result = IOUtils.toString(in, (Charset) null);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return result;
    }

    public static String DownloadURLtoFile(String URL, String FilePath) throws IOException {
        URL url = new URL(URL);
        File file = new File(FilePath);
        FileUtils.copyURLToFile(url, file, 10 * 1000,10 * 1000);
        String absPath = file.getAbsolutePath();
        return absPath;
    }

}
