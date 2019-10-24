package fr.inria.coming.utils;

import fr.inria.coming.repairability.models.InstanceStats;
import gumtree.spoon.diff.operations.Operation;
import org.reflections.Reflections;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by khesoem on 10/4/2019.
 */
public class GumtreeHelper {
    private static GumtreeHelper _instance = null;
    private static final String CLASSES_HIERARCHY_PATH = "src/main/resources/gumtree-inheritance-relations.txt";

    private Map<String, Set<String>> childrenToParents;

    public static GumtreeHelper getInstance(){
        if(_instance == null)
            _instance = new GumtreeHelper();
        return _instance;
    }

    public GumtreeHelper(){
        loadClassToParents();
    }

    private void loadClassToParents() {
        childrenToParents = new HashMap<>();

        try {
            Scanner sc = new Scanner(new File(CLASSES_HIERARCHY_PATH));

            while(sc.hasNextLine()){
                String line = sc.nextLine();
                String[] parts = line.split(":");

                String child = parts[0];
                String[] parents = parts[1].split(" ");
                childrenToParents.put(child, new HashSet<>(Arrays.asList(parents)));
            }

            sc.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Gumtree inheritance information file not found");
        }
    }

    // says whether parent is an ancestor of or equal to child.
    public boolean isAChildOf(String child, String parent){
        if(!childrenToParents.containsKey(child))
            return false;
        return childrenToParents.get(child).contains(parent);
    }

//    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
//        ClassLoader classLoader = new GumtreeHelper().getClass().getClassLoader();
//        String inheritanceRelationsFilePath = CLASSES_HIERARCHY_PATH;
//        extractAndSaveCtElementsHierarchyModel(inheritanceRelationsFilePath);
//    }

    public static List<CtElement> getPathToRootNode(CtElement element) {
        CtElement par = element.getParent();
        if (par == null || par instanceof CtPackage || element == par) {
            List<CtElement> res = new ArrayList<>();
            res.add(element);
            return res;
        }
        List<CtElement> pathToParent = getPathToRootNode(par);
        pathToParent.add(element);
        return pathToParent;
    }

    private static void extractAndSaveCtElementsHierarchyModel(String outputPath) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(outputPath));

        Map<String, Set> childrenToParents = new HashMap<>();

        Reflections reflections = new Reflections("spoon");
        Set<Class<? extends CtElement>> allClasses = reflections.getSubTypesOf(CtElement.class);
        allClasses.add(CtElement.class);

        // initializing childrenToParents
        Set<Class> toBeIgnored = new HashSet<>();
        for(Class clazz : allClasses){
            if(!clazz.getSimpleName().startsWith("Ct")) {
                toBeIgnored.add(clazz);
                continue;
            }
            childrenToParents.put(clazz.getSimpleName().substring(2), new HashSet<>());
        }
        allClasses.removeAll(toBeIgnored);

        for(Class clazz : allClasses){
            String currentClassName = clazz.getSimpleName().substring(2);
            Set<Class<? extends CtElement>> childrenOfCurrentClass = reflections.getSubTypesOf(clazz);
            for(Class childOfCurrentClass : childrenOfCurrentClass){
                if(!childOfCurrentClass.getSimpleName().startsWith("Ct"))
                    continue;
                String currentChildName = childOfCurrentClass.getSimpleName().substring(2);
                childrenToParents.get(currentChildName).add(currentClassName);
            }
            childrenToParents.get(currentClassName).add(currentClassName); // each class is considered as an ancestor of itself
        }

        for(Map.Entry<String, Set> childToParents : childrenToParents.entrySet()){
            String className = childToParents.getKey();
            pw.print(className + ":");
            Set<String> parents = childToParents.getValue();
            for(String parent : parents){
                pw.print(" " + parent);
            }
            pw.print("\n");
            pw.flush();
        }

        pw.close();
    }

    public static InstanceStats getStats(Operation operation) {
        InstanceStats stats = new InstanceStats();
        if(operation.getSrcNode() != null) {
            stats.setSrcEntityTypes(operation.getSrcNode().getReferencedTypes());
            try { // FIXME: exception should not be thrown
                stats.setNumberOfSrcEntities(operation.getSrcNode().getElements(null).size());
            }catch (Exception e){
//                e.printStackTrace();
            }
        }
        if(operation.getDstNode() != null) {
            stats.setDstEntityTypes(operation.getDstNode().getReferencedTypes());
            try { // FIXME: exception should not be thrown
                stats.setNumberOfDstEntities(operation.getDstNode().getElements(null).size());
            }catch (Exception e){
//                e.printStackTrace();
            }
        }
        return stats;
    }

    public Map<String, Set<String>> getChildrenToParents() {
        return childrenToParents;
    }

    public void setChildrenToParents(Map<String, Set<String>> childrenToParents) {
        this.childrenToParents = childrenToParents;
    }

    /**
     * The label of a CtElement is the simple name of the class without the CT
     * prefix.
     *
     * @param element
     * @return
     */
    public static String getNodeLabelFromCtElement(CtElement element) {
        String typeFromCt = element.getClass().getSimpleName();
        if (typeFromCt.trim().isEmpty())
            return typeFromCt;
        return typeFromCt.substring(2, typeFromCt.endsWith("Impl") ? typeFromCt.length() - 4 : typeFromCt.length());
    }
}
