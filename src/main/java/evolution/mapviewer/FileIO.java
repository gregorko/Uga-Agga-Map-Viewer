/*
 * DownloadFile.java
 *
 * Created on 6. September 2003, 12:32
 */
package evolution.mapviewer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;


/**
 *
 * @author  tj99de
 */
public class FileIO {
    
    public FileIO() {}

    /** Creates a new instance of DownloadFile */
    public File download(String url, String proxyHost, int proxyPort, File localCaveFileGz,
        boolean downloadForced)
    {
        // Create an instance of HttpClient.
        SSLContextBuilder builder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslsf;
        try
        {
            builder.loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true);
            sslsf = new SSLConnectionSocketFactory(
                builder.build());
        }
        catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e)
        {
            e.printStackTrace();
            return localCaveFileGz;
        }

        String caveFileURL = url;
        if (caveFileURL == null || caveFileURL.isEmpty())
        {
            caveFileURL = Cfg.DEFAULT_CAVE_FILE_URL;
        }


        try (CloseableHttpClient client = HttpClients.custom()
            .setSSLSocketFactory(sslsf).build())
        {
            HttpGet request = buildRequest(caveFileURL, proxyHost, proxyPort, localCaveFileGz,
                downloadForced);

            try (CloseableHttpResponse response = client.execute(request);)
            {
                // the file on file system is still up to date
                System.out.println("response: " + response.getStatusLine().toString());
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_NOT_MODIFIED)
                {
                    System.out.println("File is still up to date.");
                    localCaveFileGz.setLastModified(System.currentTimeMillis());
                    return localCaveFileGz;
                }
                // request failed
                if (statusCode != HttpStatus.SC_OK)
                {
                    System.out.println("Request failed: " + response.getStatusLine().toString());
                    return null;
                }

                // write response to HDD
                File localCaveFileGzBak = new File(localCaveFileGz.getAbsolutePath() + ".bak");
                if (localCaveFileGzBak.exists() && localCaveFileGzBak.isFile())
                {
                    localCaveFileGzBak.delete();
                }
                if (localCaveFileGz.exists() && localCaveFileGz.isFile())
                {
                    localCaveFileGz.renameTo(localCaveFileGzBak);
                }

                try (InputStream in = response.getEntity().getContent())
                {
                    Files.copy(in, localCaveFileGz.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }

        System.out.println("Download abgeschlossen.");
        return localCaveFileGz;
    }

    private HttpGet buildRequest(String caveFileURL, String proxyHost, int proxyPort,
        File localCaveFileGz, boolean downloadForced)
    {
        HttpGet request = new HttpGet(caveFileURL);

        if (proxyHost.length() > 0)
        {
            RequestConfig config = RequestConfig.custom()
                .setProxy(new HttpHost(proxyHost, proxyPort))
                .build();
            request.setConfig(config);
        }

        Instant lastModified = null;
        if( !downloadForced && localCaveFileGz.exists() && localCaveFileGz.isFile())
        {
            lastModified = Instant.ofEpochMilli(localCaveFileGz.lastModified());
        }
        if (lastModified!=null)
        {
            String lastModStr = DateTimeFormatter.RFC_1123_DATE_TIME.format(
                lastModified.atZone(ZoneOffset.UTC));
            System.out.println("download if file newer than: " + lastModStr);

            request.addHeader("If-Modified-Since", lastModStr);
        }
        return request;
    }
}
