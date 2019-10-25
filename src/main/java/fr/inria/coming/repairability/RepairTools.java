package fr.inria.coming.repairability;

import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.repairability.repairtools.AbstractRepairTool;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is a unique subclass of AbstractRepairTool and it should be the most used interface for using any repair tool classes.
 * - It abstracts a group of Repair Tools instead of just one.
 * - This class also acts a book keeper of the supported repair tools.
 * - It also provides utilities for the handling Repair Tool.
 */
public class RepairTools extends AbstractRepairTool {

    /**
     * Names of all the repair-tools supported by this module.
     * The names should be same as the classname(case sensitive) present in fr.inria.coming.repairability.repairtools
     */
    private static final String[] supportedTools = {
            "JMutRepair",
            "Nopol",
            "JKali",
            "JGenProg",
            "NPEfix",
            "Arja",
            "Elixir",
            "Cardumen"
    };

    /**
     * A list that contains repair-tools(in form of their object) to be considered in this repairability-analysis instance
     */
    private List<AbstractRepairTool> toolsToBeConsidered;


    private String repairToolArg; // The repair-tool argument provided in CLI

    /**
     * Default Constructor.
     * Uses CLI argument "repairtool" to get tools to be considered
     */
    public RepairTools() {
        this(ComingProperties.getProperty("repairtool"));
    }

    /**
     * @param args "" | ALL | <any repair tool name> | <a list of repair tool names>
     *             Example: "JMutRepair:Npool"
     */
    public RepairTools(String args) {

        // initialise class properties
        this.repairToolArg = args;
        toolsToBeConsidered = new ArrayList<>();

        // input checks and loading class objects for the specified repair tools
        if (repairToolArg == null) {
            // consider default/ALL Case
            toolsToBeConsidered = getRepairToolsInstance(Arrays.asList(supportedTools));
        } else if (repairToolArg.equals("") || repairToolArg.equals("ALL")) {
            // consider default/ALL Case
            toolsToBeConsidered = getRepairToolsInstance(Arrays.asList(supportedTools));
        } else {
            // consider a list of repair-tool case
            String[] toolsArr = repairToolArg.split(File.pathSeparator);
            for (String s : toolsArr) {
                toolsToBeConsidered.add(getRepairToolInstance(s));
            }
        }

    }

    /**
     * Returns all the patterns that needs to mined in this instance of repairability-analysis
     *
     * @return A list of ChangePatternSpecification
     */
    public List<ChangePatternSpecification> readPatterns() {
        List<ChangePatternSpecification> patterns = new ArrayList<>();
        for (AbstractRepairTool tool : this.toolsToBeConsidered) {
            patterns.addAll(tool.getPatterns());
        }

        return patterns;
    }

    /**
     * We don't wanna modify anything in patterns (like pattern names) in this particular case. Therefore, we override it.
     *
     * @return
     */
    @Override
    public List<ChangePatternSpecification> getPatterns() {
        return this.readPatterns();
    }

    /**
     * returns list of pattern specifications for a particular repair tool
     *
     * @param toolName name of the repair tool(classname)
     * @return list of corresponding pattern specifications
     */
    public static List<ChangePatternSpecification> getPatternsForReapiarTool(String toolName) {
        return getRepairToolInstance(toolName).getPatterns();
    }

    /**
     * returns list of pattern specifications for a list of repair tool
     *
     * @param toolNames a list of string containing repair tool name(classname)
     * @return list of corresponding pattern specifications
     */
    public static List<ChangePatternSpecification> getPatternsForReapiarTools(List<String> toolNames) {
        List<ChangePatternSpecification> patterns = new ArrayList<>();
        for (AbstractRepairTool tool : getRepairToolsInstance(toolNames)) {
            patterns.addAll(tool.getPatterns());
        }
        return patterns;
    }


    /**
     * @param toolName name of the repair-tool. Same as the simple class-name
     * @return An object corresponding to rep
     */
    public static AbstractRepairTool getRepairToolInstance(String toolName) {
        AbstractRepairTool object = null;
        try {
            if (supportsTool(toolName) < 0) {
                throw new IllegalArgumentException("Repairability doesn't support " + toolName + " yet");
            }
            Class classDefinition = Class.forName("fr.inria.coming.repairability.repairtools." + toolName);
            object = (AbstractRepairTool) classDefinition.newInstance();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Can't load the class corresponding to " + toolName);
        }
        return object;
    }

    /**
     * @param toolNames a list of string containing repair tool name(classname)
     * @return a list of objects corresponding to the
     */
    public static List<AbstractRepairTool> getRepairToolsInstance(List<String> toolNames) {
        List<AbstractRepairTool> list = new ArrayList<>();
        for (String toolName : toolNames) {
            list.add(getRepairToolInstance(toolName));
        }
        return list;
    }

    /**
     * @param toolName The name of the tool
     * @return index of the tool if present in supportedTools,
     * -1 if it is not present
     */
    public static int supportsTool(String toolName) {
        for (int i = 0; i < supportedTools.length; i++) {
            if (toolName.equals(supportedTools[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @return Returns a string showing the "patterns names" supported by it
     */
    public static String getCLISupportString() {
        StringBuilder tools = new StringBuilder();
        tools.append("ALL | ");

        for (int i = 0; i < supportedTools.length; i++) {
            tools.append(supportedTools[i]);

            if (i != (supportedTools.length - 1)) {
                tools.append(" | ");
            }
        }

        return tools.toString();
    }


    public static String[] getSupportedTools() {
        return supportedTools;
    }

    public String getRepairToolArg() {
        return repairToolArg;
    }

    public List<AbstractRepairTool> getToolsToBeConsidered() {
        return toolsToBeConsidered;
    }
}
