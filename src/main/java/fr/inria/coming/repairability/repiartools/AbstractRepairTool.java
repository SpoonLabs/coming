package fr.inria.coming.repairability.repiartools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;

import java.io.File;
import java.util.List;

/**
 * Each repair tool in RepairabilityFilterAnalyzer must be a subclass of Repair tool
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
     * @return boolean value
     */
    public boolean filter(ChangePatternInstance instance) {
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
        return getClass().getResource("/repairability/" + this.getClass().getSimpleName() + "/" + name).getFile();
    }
}
