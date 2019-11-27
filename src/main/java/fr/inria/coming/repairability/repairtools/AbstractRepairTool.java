package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.utils.ASTInfoResolver;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Each repair tool in RepairabilityAnalyzer must be a subclass of Repair tool
 */
public abstract class AbstractRepairTool {

    /**
     * Each repair tool has a particular search space, i.e, the patches produced by it are supposed to have a certain kind of characteristics.
     * This method is supposed to encode such patterns in form ChangePatternSpecification and return it.
     * The list returned will be modified by AbstractRepairTool.getPatterns() and given to PatternInstanceAnalyser to mine the encoded patterns.
     * <p>
     * Ways to create ChangePatternSpecification:
     * - Manually create an instance
     * - Use PatternXMLParser where input can be a
     * - string store in the source code file
     * - string read .xml file stored TODO: Decided the location (Preferred way)
     *
     * @return a List of ChangePatternSpecifications that are supposed to be passed to getPatterns()
     */
    protected abstract List<ChangePatternSpecification> readPatterns();

    /**
     * Certain patterns/characteristics of search-space of a repair tool can't be represented by ChangePatternSpecification
     * This filter is supposed to delete/remove such instances from the results given by PatternInstanceAnalyser.
     *
     * @param instance
     * @param revision
     * @param diff
     * @return boolean value
     */
    public boolean filter(ChangePatternInstance instance, IRevision revision, Diff diff) {
        return true;
    }

    /**
     * An abstraction over readPatterns(). It invokes readPatterns() and apply checks on the ChangePatternSpecifications given by readPatterns() and apply
     * modifications to it so that it suits the contract with other part of the module.
     * <p>
     * Example: Modify name of each pattern specification so that it follows a certain protocol which can used later to extract information from name itselg
     *
     * @return a List of ChangePatternSpecifications that are supposed to be mined by PatternInstanceAnalyzer
     */
    public List<ChangePatternSpecification> getPatterns() {
        List<ChangePatternSpecification> patterns = this.readPatterns();
        patterns.forEach(this::modifyName);
        return patterns;
    }

    /**
     * Modifies name of each pattern specification.
     * <p>
     * Right now, it sets the name in the following format:
     * - "<Repair Tool Name>:<XML Pattern Name>"
     *
     * @param pattern ChangePatternSpecification to be modified
     */
    private void modifyName(ChangePatternSpecification pattern) {

        pattern.setName(
                this.getClass().getSimpleName() + File.pathSeparator + pattern.getName()
        );

    }

    public String getPathFromResources(String name) {
        String rootInputFile = "/repairability/" + this.getClass().getSimpleName() + "/" + name;
        String outFilePath = null;
        try {
            // read the file into buffer
            InputStream inputStream = this.getClass().getResourceAsStream(rootInputFile);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            // make a tmp file
            File outFile = File.createTempFile("tmp-" + this.getClass().getSimpleName() + name, null);
            outFilePath = outFile.getPath();
            OutputStream outStream = new FileOutputStream(outFilePath);
            outStream.write(buffer);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return outFilePath;
    }

    public boolean coversTheWholeDiff(ChangePatternInstance instancePattern, Diff diff) {
        Set<CtElement> instanceNodes = getInstanceCoveredNodes(instancePattern, diff);
        
        for (Operation diffOperation : diff.getRootOperations()) {
            boolean found = coveredByInstanceNodes(instancePattern, instanceNodes, diffOperation);
            
            if(found == false)
                return false;
        }
        
        return true;
    }

    protected boolean coveredByInstanceNodes
            (
                    ChangePatternInstance instance,
                    Set<CtElement> instanceCoveredNodes,
                    Operation diffOperation
            ) {
        CtElement opAffectedNode = diffOperation.getDstNode() == null
                ? diffOperation.getSrcNode()
                : diffOperation.getDstNode();
        return coveredByInstanceNodes(instanceCoveredNodes, opAffectedNode);
    }

    protected boolean coveredByInstanceNodes
            (
                    Set<CtElement> instanceCoveredNodes,
                    CtElement node
            ) {
        List<CtElement> pathToDiffRoot =
                ASTInfoResolver.getPathToRootNode(node);
        for(CtElement element : pathToDiffRoot){
            for(CtElement instanceNode : instanceCoveredNodes) {
                if (element == instanceNode)
                    return true;
            }
        }
        return false;
    }

    // the abstract implementation only returns nodes in the Dst AST.
    protected Set<CtElement> getInstanceCoveredNodes(ChangePatternInstance instance, Diff diff) {
        return instance.getActions().stream()
                .map(action -> (action.getDstNode() != null ? action.getDstNode() : action.getSrcNode()))
                .collect(Collectors.toSet());
    }

    public List<ChangePatternInstance> filterSelectedInstances(List<ChangePatternInstance> lst, Diff diff){
        return lst;
    }
}
