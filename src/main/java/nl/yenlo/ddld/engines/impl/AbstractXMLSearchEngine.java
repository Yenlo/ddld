package nl.yenlo.ddld.engines.impl;

import nl.yenlo.ddld.engines.SearchEngineClient;
import nl.yenlo.ddld.engines.SearchResult;
import nl.yenlo.ddld.importing.exceptions.ImportException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
 * Utilities for talking to web services that do not have a WSDL or whatsoever defined. ( Bing, Newz, etc.. )
 *
 * @author Philipp Gayret
 */
public abstract class AbstractXMLSearchEngine implements SearchEngineClient {

    /**
     * For adding per-request headers, like authentication, etc.
     *
     * @param httpget the request
     */
    public void prepare(HttpGet httpget) {
    }

    abstract public SearchResult process(OMElement element);

    /**
     * Creates an {@link HttpGet} using base to create a {@link URIBuilder} with and adding the given map's entries as URL parameters.
     *
     * @param base   the base string
     * @param params the parameters
     * @return an http get request object
     * @throws URISyntaxException
     */
    private HttpGet buildHttpGet(String base, Map<String, Object> params) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(base);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder = builder.setParameter(entry.getKey(), entry.getValue().toString());
            }
        }
        HttpGet httpget = new HttpGet(builder.build());
        httpget.addHeader("User-Agent", "DDLD-search");
        return httpget;
    }

    /**
     * Takes an {@link InputStream}, reads it expecting it to be XML to create an {@link OMElement} with.
     *
     * @param stream the input stream
     * @return the stream parsed as xml
     * @throws XMLStreamException
     */
    private OMElement asElement(InputStream stream) throws XMLStreamException {
        BufferedInputStream bis = new BufferedInputStream(stream);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(bis);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        return builder.getDocumentElement();
    }

    /**
     * Performs the httpget, expecting the response to be XML.
     *
     * @param base   the base string
     * @param params the parameters
     * @return an http get request object
     * @throws ImportException
     */
    public OMElement getRaw(String base, Map<String, Object> params) throws ImportException {
        try {
            HttpGet httpget = buildHttpGet(base, params);
            HttpClient httpclient = new DefaultHttpClient();
            this.prepare(httpget);
            HttpResponse response;
            response = httpclient.execute(httpget);
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
        } catch (URISyntaxException e) {
            throw new ImportException("Erred while constructing uri for endpoint: " + base, e);
        }
    }

    public SearchResult get(String base, Map<String, Object> params) throws ImportException {
        return process(this.getRaw(base, params));

    }

}