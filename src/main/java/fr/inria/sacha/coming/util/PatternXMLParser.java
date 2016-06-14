package fr.inria.sacha.coming.util;

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

import fr.inria.sacha.coming.analyzer.treeGenerator.ChangePattern;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternAction;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternEntity;
import fr.inria.sacha.coming.entity.ActionType;

/**
 * 
 * @author Matias Martinez
 *
 */

public class PatternXMLParser {
  static final String ELEMENT= "element";
  static final String ACTION = "action";
  static final String PARENT = "parent";


  @SuppressWarnings({ "unchecked", "null" })

  
  public static ChangePattern parseFile(String configFile) {
  
	  try {
		  
			File fXmlFile = new File(configFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
		 	doc.getDocumentElement().normalize();
				 
			NodeList nList = doc.getElementsByTagName("entity");
		
		 
			Map<String, List> elementParents = new HashMap<String, List>();
			Map<String, PatternEntity> idEntities = new HashMap<String, PatternEntity>();
			ChangePattern pattern = new ChangePattern();
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
		 
				Node nNode = nList.item(temp);
		 		//System.out.println("\nCurrent Element :" + nNode.getNodeName());
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
					String id = eElement.getAttribute("id");
					String type = eElement.getAttribute("type");
					String value = eElement.getAttribute("value");
							
					PatternEntity pEntity = new PatternEntity(type,value );
					idEntities.put(id, pEntity);
												
					NodeList nListParent =	eElement.getElementsByTagName("parent");
					List<String> parents = new ArrayList<String>();
										
					//
					for (int ptemp = 0; ptemp < nListParent.getLength(); ptemp++) {
							 
							System.out.println("parent ");
							Node nNodeParent = nListParent.item(ptemp);
							Element eParentElement = (Element) nNodeParent;
							String idParent = eParentElement.getAttribute("parentId");
							String distanceParent = eParentElement.getAttribute("distance");
							parents.add(idParent+"@"+distanceParent);
							elementParents.put(id, parents);
					}
							
				
			}
			}
			//
			for(String idEntity : elementParents.keySet()){
				PatternEntity entity = idEntities.get(idEntity);
				List<String> parents = elementParents.get(idEntity);
				for (String parent : parents) {
					String[] pspl = parent.split("@");
					PatternEntity entParent = idEntities.get(pspl[0]);
					if(entParent == null){
						throw new Exception("Parent not identified");
					}
					entity.setParent(entParent,Integer.valueOf(pspl[1]));
				}
				
			}
			
			//System.out.println("Result: "+idEntities.values() );
			
			NodeList nActionList = doc.getElementsByTagName("action");
			for (int temp = 0; temp < nActionList.getLength(); temp++) {
				 
				Node nNode = nActionList.item(temp);
		 
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 			Element eElement = (Element) nNode;
		 			String idEnt = eElement.getAttribute("entityId");
					String action = eElement.getAttribute("type");
					
					ActionType type = ActionType.valueOf(action);
					if(type == null)
						throw new Exception("Action Type not identified");
					
					PatternEntity entity = idEntities.get(idEnt);
					if(entity  == null){
						throw new Exception("Parent not identified");
					}
					
					PatternAction patternAction = new PatternAction(entity, type);
					pattern.addChange(patternAction);
				}
			}
			return pattern;
		    } catch (Exception e) {
			e.printStackTrace();
		    }
	  		return null;
		  }
  
	  
	  public static void main(String args[]){
		  PatternXMLParser x = new PatternXMLParser();
		  ChangePattern cp =  x.parseFile("/home/matias/Desktop/test.xml");
		  if(cp != null){
			  System.out.println(cp);
		  }
	  }
  }

 
