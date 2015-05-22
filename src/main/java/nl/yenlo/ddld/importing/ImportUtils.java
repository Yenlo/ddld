package nl.yenlo.ddld.importing;

import nl.yenlo.ddld.importing.exceptions.ImportException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Utilities for talking to web services that do not have a WSDL or whatsoever defined. ( OAI... )
 * <p/>
 * Inspired by https://code.google.com/p/joailib.
 *
 * @author Philipp Gayret
 */
public class ImportUtils {

    /**
     * Creates an {@link HttpGet} using base to create a {@link URIBuilder} with and adding the given map's entries as URL parameters.
     *
     * @param base   the base url
     * @param params the url parameters
     * @return an httpget object
     * @throws URISyntaxException
     */
    public static HttpGet buildHttpGet(String base, Map<String, Object> params) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(base);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder = builder.setParameter(entry.getKey(), entry.getValue().toString());
            }
        }
        HttpGet httpget = new HttpGet(builder.build());
        httpget.addHeader("User-Agent", "DDLD-indexer");
        return httpget;
    }

    /**
     * Takes an {@link InputStream}, reads it expecting it to be XML to create an {@link OMElement} with.
     *
     * @param stream the stream
     * @return an xml element
     * @throws XMLStreamException
     */
    public static OMElement asElement(InputStream stream) throws XMLStreamException {
        BufferedInputStream bis = new BufferedInputStream(stream);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(bis);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        return builder.getDocumentElement();
    }

    /**
     * Performs the httpget, expecting the response to be XML, or for status 503; return with a Retry-After headerm, in which this will wait.
     *
     * @param base       the base url
     * @param params     the url parameters
     * @param maxRetries the maximum amount of times to retry
     * @return an xml element
     * @throws ImportException
     */
    public static OMElement get(String base, Map<String, Object> params, Integer maxRetries, org.apache.commons.httpclient.Header... reqheaders) throws ImportException {
        try {
            HttpGet httpget = buildHttpGet(base, params);
            HttpClient httpclient = new DefaultHttpClient();
            for (org.apache.commons.httpclient.Header header : reqheaders) {
                httpget.addHeader(header.getName(), header.getValue());
            }
            HttpResponse response;
            response = httpclient.execute(httpget);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == 503) {
                org.apache.http.Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("Retry-After")) {
                        String retry_time = header.getValue();
                        Thread.sleep(Integer.parseInt(retry_time) * 1000);
                        httpclient.getConnectionManager().shutdown();
                        if (maxRetries > 0) {
                            return get(base, params, maxRetries - 1);
                        } else {
                            throw new ImportException("Exceeded max retries");
                        }
                    }
                }
            }
            HttpEntity entity = response.getEntity();
            return asElement(entity.getContent());
        } catch (IOException e) {
            throw new ImportException("Unable to obtain the InputStream to the content response, or unble to obtain a response at all.", e);
        } catch (IllegalStateException e) {
            throw new ImportException("Unable to obtain the InputStream to the content response, or unble to obtain a response at all.", e);
        } catch (XMLStreamException e) {
            throw new ImportException("Erred while reading the response to XML.", e);
        } catch (NumberFormatException e) {
            throw new ImportException("Unable to parse the Retry-After timeout time.", e);
        } catch (InterruptedException e) {
            throw new ImportException("Probably erred while waiting for a Retry-After to time out.", e);
        } catch (URISyntaxException e) {
            throw new ImportException("Erred while constructing uri for endpoint: " + base, e);
        }

    }

}