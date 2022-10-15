package net.petercashel.contentsync.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
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

}
