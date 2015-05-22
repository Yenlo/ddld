package nl.yenlo.ddld.importing.processing;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import nl.yenlo.ddld.model.search.BasicDocument;

import java.text.Normalizer;

/**
 * "Helps" with finds duplicates for documents.
 * 
 * @author Philipp Gayret
 *
 */
public class MatchingUtils {

	/**
	 * Normalized a string using:
	 * - it is normalized with {@link Normalizer.Form#NFD}
	 * - all combining diacritical marks are removed
	 * - it has all characters lowercased
	 * 
	 * @param text
	 * @return
	 */
	public static String normalize(String text) {
		String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
		normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		return normalized.toLowerCase();
	}

	/**
	 * Calculates md5 of the content, after:
	 * - it is normalized with {@link Normalizer.Form#NFD}
	 * - all combining diacritical marks are removed
	 * - it has all characters lowercased
	 * - all characters outside of the set [a-z0-9] are removed
	 * 
	 * @param baseDocument document to obtain hash from
	 * @return hash code
	 */
	public static String getSubsetHashcode(BasicDocument baseDocument) {
		String content = baseDocument.getContent() + "";
		content = Normalizer.normalize(content, Normalizer.Form.NFD);
		content = content.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		content = content.replaceAll("[^A-Za-z0-9]", "");
		content = content.toLowerCase();
		HashCode hashcode = Hashing.md5().hashBytes(content.getBytes());
		return hashcode.toString();
	}

}
