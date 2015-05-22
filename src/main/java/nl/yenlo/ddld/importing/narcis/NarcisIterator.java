package nl.yenlo.ddld.importing.narcis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import nl.yenlo.ddld.importing.ImportUtils;
import nl.yenlo.ddld.importing.exceptions.ImportException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

/**
 * Requests data from Narcis, until there is nothing left.
 * 
 * @author Philipp Gayret
 *
 */
public class NarcisIterator implements Iterator<OMElement> {

	public static final String ENDPOINT_NARCIS = "http://dare.uva.nl/cgi/arno/oai/uvapub";
	public static final String NS_M = "http://www.loc.gov/mods/v3";
	public static final String NS_OAI = "http://www.openarchives.org/OAI/2.0/";

	public static final String PARAM_VERB = "verb";
	public static final String PARAM_METADATA_PREFIX = "metadataPrefix";
	public static final String PARAM_RESUMPTION_TOKEN = "resumptionToken";

	private static final AXIOMXPath XPATH_RESUMPTION_TOKEN;

	static {
		try {
			XPATH_RESUMPTION_TOKEN = new AXIOMXPath("//oai:resumptionToken");
			XPATH_RESUMPTION_TOKEN.addNamespace("oai", NS_OAI);
		} catch (JaxenException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public NarcisIterator() {
		this.completed = false;
		this.resumptionToken = null;
	}

	/**
	 * In case you have a resumptionToken from a previous run, you can use it here to resume from where you left.
	 * 
	 * @param resumptionToken initial resumption token.
	 */
	public NarcisIterator(String resumptionToken) {
		this.completed = false;
		this.resumptionToken = resumptionToken;
	}

	private boolean processing = false;
	private boolean completed = false;
	private String resumptionToken = null;

	@Override
	public boolean hasNext() {
		return !completed;
	}

	@Override
	public OMElement next() {
		if (processing) {
			throw new IllegalStateException("OAI has a single-threaded nature with its resumptionTokens; only one thread can be performing the #next() call.");
		}
		try {
			processing = true;
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(PARAM_VERB, "ListRecords");
			if (resumptionToken == null) {
				params.put(PARAM_METADATA_PREFIX, "oai_dc");
			} else {
				params.put(PARAM_RESUMPTION_TOKEN, resumptionToken);
			}
			try {
				OMElement result = ImportUtils.get(ENDPOINT_NARCIS, params, 3);
				OMElement resumptionTokenElement = (OMElement) XPATH_RESUMPTION_TOKEN.selectSingleNode(result);
				if (resumptionTokenElement == null) {
					resumptionToken = null;
					completed = true;
				} else {
					resumptionToken = resumptionTokenElement.getText();
				}
				return result;
			} catch (JaxenException e) {
				throw new IllegalStateException("Erred while performing XPath on the resultset from Narcis.");
			} catch (ImportException e) {
				throw new IllegalStateException("Erred while loading a resultset from Narcis.");
			}
		} finally {
			processing = false;
		}
	}

	@Override
	public void remove() {
	}

	public boolean isProcessing() {
		return this.processing;
	}

	public boolean isCompleted() {
		return this.completed;
	}

	public String getResumptionToken() {
		return this.resumptionToken;
	}

}
