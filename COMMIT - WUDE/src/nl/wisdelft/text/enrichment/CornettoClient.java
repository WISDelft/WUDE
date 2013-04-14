package nl.wisdelft.text.enrichment;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 * 
 */

/**
 * @author oosterman
 */
public class CornettoClient {
	private static final String host = "http://apstwo.st.ewi.tudelft.nl";
	private static final int port = 5204;

	private static final String synonym = "synonym";
	private static final String hyponym = "has_hyponym";
	private static final String hyperonym = "has_hyperonym";
	private static final String synonymKey = "SYNONYM";
	private static final String hyponymKey = "HAS_HYPONYM";
	private static final String hyperonymKey = "HAS_HYPERONYM";

	private XmlRpcClient client;

	public static void main(String[] args) throws UnknownHostException {
		CornettoClient cc = new CornettoClient();
		String query = "aardewerk";
		
		Set<String> synonyms = cc.getSynonyms(query);
		System.out.println(synonyms);
		
		Set<String> hyponyms = cc.getHyponyms(query, 1);
		System.out.println(hyponyms);
		
		Set<String> hyperonyms = cc.getHyperonyms(query,1);
		System.out.println(hyperonyms);
	}

	public CornettoClient() throws UnknownHostException {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		String url = String.format("%s:%s", host, port);
		try {
			config.setServerURL(new URL(url));
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			throw new UnknownHostException(String.format("URL '%s' is not a valid Cornetto endpoint", url));
		}
		client = new XmlRpcClient();
		client.setConfig(config);
	}

	private String parseKey(String key) {
		// parse the key from word:form:sense
		int colon = key.indexOf(":");
		if (colon < 0) return key;
		else return key.substring(0, colon);
	}

	private Set<String> parseRelationMap(Map<String, Map> relationMap, String relationKey) {
		Set<String> words = new HashSet<String>();
		// if it is the map with synonyms
		if (!relationMap.containsKey(relationKey)) {
			for (String key : relationMap.keySet()) {
				// add the key to the synonym set
				words.add(parseKey(key));
				// recursively add the synonyms of this key
				words.addAll(parseRelationMap(relationMap.get(key), relationKey));
			}
		}
		else {
			// a hashmap containg one element <relationkey, Map>
			words.addAll(parseRelationMap(relationMap.get(relationKey), relationKey));
		}
		return words;
	}

	private Set<String> getRelations(String word, String relation, String relationKey, int recursiveDepth) {
		Object[] params = new Object[] { String.format("%s %s%s", word, relation, recursiveDepth) };
		try {
			Object result = client.execute("ask", params);
			if(result instanceof Map) 
				return parseRelationMap((Map)result, relationKey);
			else
				return new HashSet<String>();
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
			return new HashSet<String>();
		}
	}

	public Set<String> getSynonyms(String word) {
		return getRelations(word, synonym, synonymKey, 1);
	}
	public Set<String> getHyponyms(String word, int recursiveDepth) {
		return getRelations(word, hyponym, hyponymKey, recursiveDepth);
	}
	public Set<String> getHyperonyms(String word, int recursiveDepth) {
		return getRelations(word, hyperonym, hyperonymKey, recursiveDepth);
	}

}
