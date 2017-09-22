package com.coresecure.brightcove.wrapper.utils;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coresecure.brightcove.wrapper.sling.CertificateListService;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

public class HttpServices {
    private static Proxy PROXY = Proxy.NO_PROXY;
    private static final String CERTIFICATE_TYPE = "X.509";
    private static final String CA = "ca";
    private static final String TLS = "TLS";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServices.class);

    public static void setProxy(Proxy proxy) {
        if (proxy == null) {
            PROXY = Proxy.NO_PROXY;
        } else {
            PROXY = proxy;
        }
    }

    public static String executeDelete(String targetURL,
                                      Map<String, String> headers) {
        URL url;
        HttpsURLConnection connection = null;
        String payload = "{}";
        String delResponse = null;
        BufferedReader rd = null;
        DataOutputStream wr = null;
        try {
            // Create connection
            url = new URL(targetURL.replaceAll(" ", "%20"));
            connection = getSSLConnection(url, targetURL);
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(payload.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(payload);
            // Fortify Fix for below two lines.
            // wr.flush();
            // wr.close();

            // Get Response
            LOGGER.debug("getResponseCode: " + connection.getResponseCode());
            LOGGER.debug("getResponseCode: " + connection.getResponseMessage());
            if (connection.getResponseCode() < 400) {
                InputStream is = connection.getInputStream();
                rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                // Fortify Fix for below two lines.
                // rd.close();
                // return response.toString();
                delResponse = response.toString();
            } else {
                delResponse= "{'error_code':"+connection.getResponseCode()+",'message':'"+connection.getResponseMessage()+"'}";
            }
        } catch (Exception e) {
            LOGGER.error("error! ",e);
            delResponse= "{'error_code':-1,'message':'Exception in executeDelete'}";

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
            // Fixed the Fortify Scan -- - HttpServices.java, line 50
            // (Unreleased Resource: Streams) [Hidden]
            if (null != rd) {
                try {
                    rd.close();
                } catch (IOException e) {
                    LOGGER.error("error! ",e);
                }
            }
            if (null != wr) {
                try {
                    wr.flush();
                    wr.close();
                } catch (IOException e) {
                    LOGGER.error("error! ",e);
                }
            }

        }
        LOGGER.debug("delResponse: " + delResponse);
        return delResponse;
    }

    public static String executePost(String targetURL, String payload,
                                    Map<String, String> headers) {

        return executePost(targetURL, payload,
                 headers,  "application/x-www-form-urlencoded");
    }
    public static String executePost(String targetURL, String payload,
                                     Map<String, String> headers, String contentType) {
        URL url;
        HttpsURLConnection connection = null;
        String exPostResponse = null;
        BufferedReader rd = null;
        DataOutputStream wr = null;
        try {

            // Create connection
            url = new URL(targetURL.replaceAll(" ", "%20"));
            LOGGER.debug("URL :"+targetURL);
            LOGGER.debug("payload :"+payload);

            connection = getSSLConnection(url, targetURL);

            connection = (HttpsURLConnection) url.openConnection(PROXY);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    contentType);
            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(payload.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(payload);
            // Fortify Fix for below commented two lines.
            // wr.flush();
            // wr.close();

            // Get Response
            if (connection.getResponseCode() < 400) {
                InputStream is = connection.getInputStream();
                rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append("\r\n");
                }
                // Fortify Fix for below two lines.
                // rd.close();
                // return response.toString();
                exPostResponse = response.toString();
            } else {
                LOGGER.debug("getResponseCode: " + connection.getResponseCode());
                LOGGER.debug("getResponseCode: " + connection.getResponseMessage());

            }

        } catch (Exception e) {
            LOGGER.error("error! ",e);

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
            // Fixed the Fortify Scan -- HttpServices.java, line 99 (Unreleased
            // Resource: Streams) [Hidden]
            if (null != rd) {
                try {
                    rd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != wr) {
                try {
                    wr.flush();
                    wr.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }
        LOGGER.debug("exPostResponse[1]: " + exPostResponse);

        return exPostResponse;
    }
    public static String executePatch(String targetURL, String payload,
                                    Map<String, String> headers) {
        URL url;
        HttpsURLConnection connection = null;
        String exPostResponse = null;
        BufferedReader rd = null;
        DataOutputStream wr = null;
        try {

            // Create connection
            url = new URL(targetURL.replaceAll(" ", "%20"));
            LOGGER.debug("URL :"+targetURL);
            LOGGER.debug("payload :"+payload);

            connection = getSSLConnection(url, targetURL);

            connection = (HttpsURLConnection) url.openConnection(PROXY);
            connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            setRequestMethod(connection,"PATCH");
            connection.setRequestProperty("Content-Type", "application/json");
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(payload);
            // Fortify Fix for below commented two lines.
            // wr.flush();
            // wr.close();
            if (200 == connection.getResponseCode()) {
                InputStream is = connection.getInputStream();
                rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append("\r\n");
                }
                // Fortify Fix for below two lines.
                // rd.close();
                // return response.toString();
                exPostResponse = response.toString();
            } else {
                LOGGER.debug("getResponseCode: " + connection.getResponseCode());
                LOGGER.debug("getResponseCode: " + connection.getResponseMessage());

            }
            LOGGER.debug("exPostResponse[2]: " + exPostResponse);

        } catch (Exception e) {
            LOGGER.error("error! ",e);

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
            // Fixed the Fortify Scan -- HttpServices.java, line 99 (Unreleased
            // Resource: Streams) [Hidden]
            if (null != rd) {
                try {
                    rd.close();
                } catch (IOException e) {
                    LOGGER.error("error! ",e);
                }
            }
            if (null != wr) {
                try {
                    wr.flush();
                    wr.close();
                } catch (IOException e) {

                    LOGGER.error("error! ",e);
                }
            }
        }
        return exPostResponse;
    }
    private static void setRequestMethod(final HttpURLConnection c, final String value) {
        try {
            final Object target;
            if (c instanceof HttpsURLConnectionImpl) {
                final Field delegate = HttpsURLConnectionImpl.class.getDeclaredField("delegate");
                delegate.setAccessible(true);
                target = delegate.get(c);
            } else {
                target = c;
            }
            final Field f = HttpURLConnection.class.getDeclaredField("method");
            f.setAccessible(true);
            f.set(target, value);
        } catch (IllegalAccessException ex){
            throw new AssertionError(ex);
        } catch ( NoSuchFieldException ex){
            throw new AssertionError(ex);
        }
    }
    public static String executeGet(String targetURL, String urlParameters,
                                   Map<String, String> headers) {
        String exGetResponse = null;
        try {
            JSONObject response = executeFullGet(targetURL, urlParameters, headers);
            exGetResponse = response.has("response") ? response.getString("response") : null;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("error! ",e);

        }
        return exGetResponse;
    }
    public static JSONObject executeFullGet(String targetURL, String urlParameters,
                                        Map<String, String> headers) {
        URL url;
        URLConnection connection = null;

        JSONObject exGetResponse = new JSONObject();
        try {
            // Create connection
            url = new URL(targetURL.replaceAll(" ", "%20") + "?" + urlParameters);
            LOGGER.trace("url: "+targetURL + "?" + urlParameters+" Protocol:"+url.getProtocol());
            if ("http".equals(url.getProtocol())) {
                connection = getSSLConnection(url, targetURL, HttpURLConnection.class);
            }else {
                connection = getSSLConnection(url, targetURL, HttpsURLConnection.class);

            }
            //connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
                LOGGER.trace("-H \""+key+": "+headers.get(key)+"\"");
            }
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            // Get Response
            InputStream is = connection.getInputStream();

            ByteArrayOutputStream response = new ByteArrayOutputStream();

            //StringBuffer response = new StringBuffer();

            byte[] buffer = new byte[4096];
            int n;

            while ((n = is.read(buffer)) != -1) {
                response.write(buffer, 0, n);
            }
            LOGGER.trace("response committed!");

            // Fortify Fix for below two lines.
            // rd.close();
            // return response.toString();
            exGetResponse.put("response",new String(response.toByteArray(), "UTF-8"));
            exGetResponse.put("binary",response.toByteArray());
            exGetResponse.put("mime_type", connection.getContentType());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("error! ",e);

        } finally {

            if (connection != null) {
                connection = null;
            }
            // Fixed the Fortify Scan -- - HttpServices.java, line 191
            // (Unreleased Resource: Streams) [Hidden]

        }
        return exGetResponse;
    }

    /**
     * This method provide the SSL connection based upon the enable trusted
     * certificate flag.
     *
     * @param url
     * @param targetURL
     * @return
     * @throws IOException
     */
    private static HttpsURLConnection getSSLConnection(URL url, String targetURL) throws IOException {
        return getSSLConnection(url, targetURL, HttpsURLConnection.class);
    }

    private static <T> T getSSLConnection(URL url, String targetURL, Class<T> classType)
            throws IOException {

        T connection = (T) url
                .openConnection(PROXY);
        if ("HttpsURLConnection".equals(classType.getClass().getName())) {
            CertificateListService certificateListService = getServiceReference();
            if (null != certificateListService) {
                String enableCert = certificateListService
                        .getEnableTrustedCertificate();
                String certPath = getCertificatePath(targetURL);

                if (null != enableCert && "YES".equalsIgnoreCase(enableCert)) {
                    SSLContext context = getSSlContext(certPath);
                    if (null != context) {
                        HttpsURLConnection sslConn = (HttpsURLConnection) connection;
                        sslConn.setSSLSocketFactory(context.getSocketFactory());
                        return (T) sslConn;
                    }
                }
            }
        }
        return connection;
    }

    /**
     * This method is used to find the certificate path for the requested url.
     *
     * @param targetURL
     * @return
     */
    private static String getCertificatePath(String targetURL) {
        String certsPath = StringUtils.EMPTY;
        CertificateListService certificateListService = getServiceReference();
        if (null != certificateListService) {
            Map<String, String> certMap = certificateListService
                    .getCertificatePaths();

            for (String urls : certMap.keySet()) {
                if (targetURL.contains(urls)) {
                    certsPath = certMap.get(urls);
                    break;
                }

            }
        }
        return certsPath;
    }

    /**
     * This method is used only to look up the CertificateListService to get the
     * certificate details and enable / disable flag of these certificates.
     *
     * @return
     */
    private static CertificateListService getServiceReference() {
        CertificateListService serviceRef = null;
        BundleContext bundleContext = FrameworkUtil.getBundle(
                CertificateListService.class).getBundleContext();
        if (null != bundleContext) {
            ServiceReference osgiRef = bundleContext
                    .getServiceReference(CertificateListService.class.getName());
            if (null != osgiRef) {
                serviceRef = (CertificateListService) bundleContext
                        .getService(osgiRef);
            }
        }

        return serviceRef;
    }

    /**
     * This method is used to initialize the SSL context to establish the SSL
     * api connection.
     *
     * @param certPath
     * @return
     */
    private static SSLContext getSSlContext(String certPath) {
        SSLContext context = null;
        InputStream caInput = null;
        try {
            if (null != certPath && certPath.length() > 0) {
                // Load CAs from an InputStream
                CertificateFactory cf = CertificateFactory
                        .getInstance(CERTIFICATE_TYPE);
                caInput = new BufferedInputStream(new FileInputStream(certPath));
                Certificate ca = cf.generateCertificate(caInput);
                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry(CA, ca);
                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory
                        .getInstance(tmfAlgorithm);
                tmf.init(keyStore);
                // Create an SSLContext that uses our TrustManager

                context = SSLContext.getInstance(TLS);
                if (null != context) {
                    context.init(null, tmf.getTrustManagers(), null);
                }

            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NoSuchAlgorithmException nae) {
            nae.printStackTrace();
        } catch (KeyStoreException kse) {
            kse.printStackTrace();
        }

        catch (CertificateException ce) {
            ce.printStackTrace();
        } catch (KeyManagementException ke) {
            ke.printStackTrace();
        } finally {
            if (null != caInput) {
                try {
                    caInput.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        return context;
    }
}
