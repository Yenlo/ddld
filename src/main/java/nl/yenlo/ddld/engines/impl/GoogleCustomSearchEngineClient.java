package nl.yenlo.ddld.engines.impl;

import nl.yenlo.ddld.engines.SearchResult;
import nl.yenlo.ddld.importing.exceptions.ImportException;
import org.apache.axiom.om.OMElement;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the client to a Google's Custom Search Engine API.
 * 
 * @author Philipp
 * @deprecated Does not have a {@link #process(OMElement)} implementation as GCSE has been replaced with Bing.
 */
@Deprecated
public class GoogleCustomSearchEngineClient extends AbstractXMLSearchEngine {

	private final String key;

	@Override
	public SearchResult search(String query, int max, int offset) {
		Map<String, Object> params = new HashMap<>();
		params.put("start", offset);
		params.put("num", max);
		params.put("q", query);
		// @see https://developers.google.com/custom-search/docs/xml_results?hl=en&csw=1#languageCollections
		// params.put("cr", "countryCA");
		// @see https://developers.google.com/custom-search/docs/xml_results?hl=en&csw=1#countryCollections
		// params.put("lr", "lang_fr");
		params.put("client", "google-csbe");
		params.put("output", "xml_no_dtd");
		params.put("cx", key);
		try {
			return this.get("http://www.google.com/cse", params);
		} catch (ImportException e) {
			return SearchResult.ERRED;
		}
	}

	private GoogleCustomSearchEngineClient(String key) {
		this.key = key;
	}

	@Override
	public SearchResult process(OMElement element) {
		return SearchResult.ERRED;
	}

}
