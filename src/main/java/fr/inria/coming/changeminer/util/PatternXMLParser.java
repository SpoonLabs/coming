package fr.inria.coming.changeminer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.ActionType;
import fr.inria.coming.core.extensionpoints.changepattern.PatternFileParser;

/**
 * 
 * @author Matias Martinez
 *
 */

public class PatternXMLParser implements PatternFileParser {

	public static final String ENTITY = "entity";
	public static final String ACTION = "action";
	public static final String PARENT = "parent";
	public static final String ROLE = "role";

	public static ChangePatternSpecification parseFile(String patternFile) {

		PatternXMLParser parser = new PatternXMLParser();
		return parser.parse(new File(patternFile));
	}

	@SuppressWarnings({ "unchecked", "null" })
	public ChangePatternSpecification parse(File fXmlFile) {
		if (!fXmlFile.exists())
			throw new IllegalArgumentException("File does not exist " + fXmlFile.getAbsolutePath());

		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			Element docElement = doc.getDocumentElement();

			String nameFromSpecification = docElement.getAttribute("name");

			String namePattern = null;
			if (nameFromSpecification != null && !nameFromSpecification.isEmpty())
				namePattern = nameFromSpecification;
			else
				namePattern = fXmlFile.getName().replace(".xml", "");

			// Get all entities tags
			NodeList nList = doc.getElementsByTagName(ENTITY);

			// Temporal structure to store parent of a node id.
			Map<String, List> elementParents = new HashMap<String, List>();
			Map<String, PatternEntity> idEntities = new HashMap<String, PatternEntity>();
			ChangePatternSpecification pattern = new ChangePatternSpecification(namePattern);

			// Collecting ENTITIES
			// For each entity tag
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					String idEntity = eElement.getAttribute("id");
					String type = eElement.getAttribute("type");
					String value = eElement.getAttribute("value");
					String role = eElement.getAttribute("role");

					type = (type != null && !type.trim().isEmpty()) ? type : PatternEntity.ANY;
					value = (value != null && !value.trim().isEmpty()) ? value : PatternEntity.ANY;
					role = (role != null && !role.trim().isEmpty()) ? role : PatternEntity.ANY;

					PatternEntity pEntity = new PatternEntity(type, value);
					pEntity.setRoleInParent(role);

					idEntities.put(idEntity, pEntity);
					if (idEntity != null && !idEntity.isEmpty())
						pEntity.setId(Integer.valueOf(idEntity));

					NodeList nListParent = eElement.getElementsByTagName(PARENT);
					List<String> parentsOfTheElement = new ArrayList<String>();

					// For each parent tag inside the entity tag:
					for (int ptemp = 0; ptemp < nListParent.getLength(); ptemp++) {

						// Take the tag parent.
						Node nNodeParent = nListParent.item(ptemp);
						Element eParentElement = (Element) nNodeParent;
						// Get Attributes of the parent
						String idParent = eParentElement.getAttribute("parentId");
						String distanceParent = eParentElement.getAttribute("distance");
						// We save the information of the parents
						parentsOfTheElement.add(idParent + "@" + distanceParent);
						// We save all parents info of the entity to be processed once all entities are
						// parser
						elementParents.put(idEntity, parentsOfTheElement);
					}

				}
			}
			// Parent reification
			// Now, for each entity, let's find the entities that correspond to its parents.
			for (String idEntity : elementParents.keySet()) {
				PatternEntity entity = idEntities.get(idEntity);
				List<String> parents = elementParents.get(idEntity);
				for (String parent : parents) {
					String[] pspl = parent.split("@");
					// Let's find the entity according to the id.
					PatternEntity entParent = idEntities.get(pspl[0]);
					if (entParent == null) {
						throw new Exception("Parent not identified");
					}
					entity.setParent(entParent, Integer.valueOf(pspl[1]));
				}

			}

			// Collecting ACTIONS
			NodeList nActionList = doc.getElementsByTagName(ACTION);
			for (int temp = 0; temp < nActionList.getLength(); temp++) {

				Node nNode = nActionList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String idEnt = eElement.getAttribute("entityId");
					String action = eElement.getAttribute("type");

					ActionType type = "*".equals(action) ? ActionType.ANY : ActionType.valueOf(action);
					if (type == null)
						throw new Exception("Action Type not identified");

					PatternEntity entity = idEntities.get(idEnt);
					if (entity == null) {
						throw new Exception("Entity not identified");
					}

					PatternAction patternAction = new PatternAction(entity, type);
					pattern.addChange(patternAction);
				}
			}
			return pattern;
		} catch (Exception e) {
			System.err.println("Problems parsing file " + fXmlFile);
			e.printStackTrace();
			return null;
		}

	}

}
