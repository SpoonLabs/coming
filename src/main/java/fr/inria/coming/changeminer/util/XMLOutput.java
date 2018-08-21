package fr.inria.coming.changeminer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Update;

import fr.inria.coming.core.interfaces.Commit;
import gumtree.spoon.diff.operations.Operation;

/**
 * Export the result to XML format
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class XMLOutput {

	public static void print(Map<Commit, List<Operation>> result) {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document root = docBuilder.newDocument();
			Element rootElement = root.createElement("data");
			root.appendChild(rootElement);

			for (Commit o : result.keySet()) {
				List<Operation> actionsfc = result.get(o);

				Element commitfile = root.createElement("commitFile");
				rootElement.appendChild(commitfile);

				Attr attr = root.createAttribute("id");
				attr.setValue(o.getName());
				commitfile.setAttributeNode(attr);

				// Attr attr2 = root.createAttribute("file");
				// attr2.setValue(fc.getFileName());
				// commitfile.setAttributeNode(attr2);

				Element commitmess = root.createElement("message");
				commitfile.appendChild(commitmess);
				commitmess.setTextContent(o.getFullMessage().replace('\n', ' '));

				Element link = root.createElement("link");
				commitfile.appendChild(link);
				link.setTextContent("https://github.com/apache/commons-math/commit/" + o.getName());

				Element actions = root.createElement("actions");
				commitfile.appendChild(actions);

				for (Operation op : actionsfc) {

					Action action = op.getAction();
					if (action instanceof Update) {
						Update up = (Update) action;

						Element ae = root.createElement("action");
						actions.appendChild(ae);

						Element pre = root.createElement("pre");
						ae.appendChild(pre);
						pre.setTextContent(up.getNode().getLabel());

						Element post = root.createElement("post");
						ae.appendChild(post);
						post.setTextContent(up.getValue());

						/*
						 * Element pattern = root.createElement("pattern");
						 * ae.appendChild(pattern); pattern.setTextContent("-");
						 * 
						 * Element order = root.createElement("order");
						 * ae.appendChild(order); order.setTextContent("-");
						 */

					}
				}

				// staff elements
			}

			// set attribute to staff element

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(root);
			StreamResult result1 = new StreamResult(System.out);

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			// ----

			transformer.transform(source, result1);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	public void results(String path) throws Exception {

		Map<String, Integer> result = new HashMap<String, Integer>();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(path);

		doc.getDocumentElement().normalize();

		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

		NodeList nList = doc.getElementsByTagName("commitFile");

		System.out.println("----------------------------");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			// System.out.println("\nCurrent Element :" + nNode.getNodeName()+"
			// "+temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				NodeList atrs = eElement.getElementsByTagName("actions");

				NodeList atr = ((Element) atrs.item(0)).getElementsByTagName("action");

				// For each action
				for (int temp1 = 0; temp1 < atr.getLength(); temp1++) {

					Node atti = atr.item(temp1);
					// System.out.println("attr"+atti );
					// System.out.println("pattern : " + ((Element)
					// atti).getElementsByTagName("pattern").item(0).getTextContent());
					NodeList pats = ((Element) atti).getElementsByTagName("pattern");

					for (int pat = 0; pat < pats.getLength(); pat++) {
						Node pati = pats.item(pat);
						String patContent = pati.getTextContent();

						putPatternValue(result, patContent);
					}
				}

			}
		}
		System.out.println(result);
		for (String key : result.keySet()) {
			System.out.println(key + "&" + result.get(key) + "\\\\");
		}
	}

	public void resultsPattern(String path) throws Exception {

		Map<String, Integer> result = new HashMap<String, Integer>();
		Map<String, List<String>> pcommits = new HashMap<String, List<String>>();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(path);
		// Document doc = dBuilder.parse("C:\\tmp\\outxml.xml");

		doc.getDocumentElement().normalize();

		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

		NodeList nList = doc.getElementsByTagName("commitFile");

		System.out.println("----------------------------");
		int commitWithOutPattern = 0;
		int commitWithPattern = 0;

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			String commit = nNode.getAttributes().getNamedItem("id").getNodeValue();

			// System.out.println("\nCurrent Element :" + nNode.getNodeName()+"
			// "+temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				NodeList patternInstance = eElement.getElementsByTagName("patternInstances");

				// For each action
				for (int temp1 = 0; temp1 < patternInstance.getLength(); temp1++) {

					NodeList patternList = ((Element) patternInstance.item(0)).getElementsByTagName("pattern");
					Node pattern = patternList.item(0);
					if (pattern == null) {
						commitWithOutPattern++;
						putPatternCommits(pcommits, "no-pattern", commit);
						// No pattern
						continue;
					}
					commitWithPattern++;

					String pname = pattern.getAttributes().getNamedItem("value").getNodeValue();
					NodeList vals = ((Element) pattern).getElementsByTagName("values");

					NodeList pats = ((Element) vals.item(0)).getElementsByTagName("value");

					putPatternCommits(pcommits, pname, commit);

					if (!pname.equals("UpBinary")) {
						// if(!pname.contains("Binary") &&
						// !pname.contains("Unary")){
						putPatternValue(result, pname);

					} else
						for (int pat = 0; pat < pats.getLength(); pat++) {

							Node value = pats.item(pat);
							String patContent = "";
							NodeList old = ((Element) value).getElementsByTagName("oldvalue");
							String oldV = old.item(0).getTextContent();

							NodeList curr = ((Element) value).getElementsByTagName("newvalue");
							String currV = curr.item(0).getTextContent();
							patContent += pname + (("".equals(oldV)) ? "" : ("-" + oldV))
									+ (("".equals(currV)) ? "" : ("->" + currV));
							putPatternValue(result, patContent);

						}
				}

			}
		}
		for (String pattern : pcommits.keySet()) {
			List<String> commits = pcommits.get(pattern);
			System.out.println("pattern: " + pattern);
			for (String c : commits) {
				System.out.println("--" + c);
			}
		}

		// System.out.println(result);

		List<String> l = new ArrayList(result.keySet());
		ResultOrder ro = new ResultOrder();
		ro.result = result;
		Collections.sort(l, ro);
		int total = 0;
		for (String key : l) {
			int t = result.get(key);
			System.out.println(key + "&" + t + "\\\\");
			total += t;
		}
		System.out.println("total " + total);
		System.out.println("Commits with patterns " + commitWithPattern);
		System.out.println("Commits without patterns " + commitWithOutPattern);

	}

	public void putPatternValue(Map<String, Integer> result, String patContent) {
		if (!patContent.equals("-")) {
			Integer oc = result.get(patContent);
			if (oc == null) {
				result.put(patContent, 1);
			} else
				result.put(patContent, oc + 1);
		}
	}

	public void putPatternCommits(Map<String, List<String>> result, String patContent, String commit) {
		List oc = null;
		if (!result.containsKey(patContent)) {
			oc = new ArrayList<String>();
			result.put(patContent, oc);
		} else {
			oc = result.get(patContent);
		}
		oc.add(commit);
	}

	public class ResultOrder implements Comparator<String> {
		public Map<String, Integer> result = null;

		@Override
		public int compare(String o1, String o2) {
			int r1 = result.get(o1);
			int r2 = result.get(o2);
			return Integer.compare(r2, r1);
		}
	}
}
