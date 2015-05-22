package nl.yenlo.ddld.importing.narcis;

import nl.yenlo.ddld.importing.exceptions.ImportException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

/**
 * Processes Narcis' responses to JSON.
 * 
 * @author Philipp Gayret
 *
 * @deprecated use the Python libraries
 */
@SuppressWarnings({ "all" })
@Deprecated()
public class NarcisProcessor {

	public static final String ENDPOINT_NARCIS = "http://oai.narcis.nl/oai";
	public static final String NS_M = "http://www.loc.gov/mods/v3";
	public static final String NS_OAI = "http://www.openarchives.org/OAI/2.0/";
	public static final String NS_DIDL = "urn:mpeg:mpeg21:2002:02-DIDL-NS";
	public static final String NS_MODS = "http://www.loc.gov/mods/v3";
	public static final String NS_DAI = "info:eu-repo/dai";
	public static final String NS_DII = "urn:mpeg:mpeg21:2002:01-DII-NS";

	private static final AXIOMXPath XPATH_RECORD = xpath("//oai:record");
	private static final AXIOMXPath XPATH_HEADER_DELETED = xpath("oai:header[@status=\"deleted\"]");
	private static final AXIOMXPath XPATH_RECORD_ID = xpath("string(oai:header/oai:identifier)");
	private static final AXIOMXPath XPATH_PERSISTENT_ID = xpath("string(oai:metadata/didl:DIDL/didl:Item/didl:Descriptor/didl:Statement[@mimeType=\"application/xml\"]/dii:Identifier)");
	private static final AXIOMXPath XPATH_MODS = xpath(".//mods:mods");
	private static final AXIOMXPath XPATH_MODS_NAME = xpath("mods:name[@type=\"personal\"]");
	private static final AXIOMXPath XPATH_MODS_FAMILY = xpath("mods:namePart[@type=\"family\"]");
	private static final AXIOMXPath XPATH_MODS_GIVEN = xpath("mods:namePart[@type=\"given\"]");
	private static final AXIOMXPath XPATH_MODS_DISPLAY_NAME = xpath("mods:displayForm");
	private static final AXIOMXPath XPATH_MODS_ABSTRACT = xpath("normalize-space(mods:abstract)");
	private static final AXIOMXPath XPATH_MODS_TITLE = xpath("normalize-space(mods:titleInfo/mods:title)");
	private static final AXIOMXPath XPATH_MODS_DATE_STR = xpath("normalize-space(mods:originInfo/mods:dateIssued)");
	private static final AXIOMXPath XPATH_MODS_TOPIC = xpath("mods:subject/mods:topic");

	private static AXIOMXPath xpath(String xpathString) {
		try {
			AXIOMXPath xpath = new AXIOMXPath(xpathString);
			xpath.addNamespace("oai", NS_OAI);
			xpath.addNamespace("didl", NS_OAI);
			xpath.addNamespace("mods", NS_OAI);
			xpath.addNamespace("dai", NS_OAI);
			xpath.addNamespace("dii", NS_OAI);
			return xpath;
		} catch (JaxenException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public void process(OMElement oaiRecords) throws ImportException {
		try {
			for (Object objRecord : XPATH_RECORD.selectNodes(oaiRecords)) {
				OMElement record = (OMElement) objRecord;
				if (XPATH_HEADER_DELETED.selectSingleNode(record) == null) {
					System.out.print(record.toString());
				} else {
					System.err.print(".");
				}
			}
			System.out.println();
		} catch (JaxenException e) {
			throw new ImportException("Unable to process, some Jaxen exception:", e);
		}
	}

}
