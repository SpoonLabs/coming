package fr.inria.coming.repairability;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromDiff;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.output.JSonPatternInstanceOutput;
import gumtree.spoon.diff.operations.Operation;
import org.apache.log4j.Logger;

import java.io.File;

public class JSONRepairabilityOutput extends JSonPatternInstanceOutput {

    Logger log = Logger.getLogger(FineGrainDifftAnalyzer.class.getName());

    public void getInstancesOfRevision(RevisionResult revisionResult, JsonArray instances) {

        String revisionIdentifier = null;
        if (revisionResult.getRelatedRevision() != null) {
            revisionIdentifier = revisionResult.getRelatedRevision().getName();
        }

        PatternInstancesFromRevision result = (PatternInstancesFromRevision) revisionResult
                .getResultFromClass(PatternInstanceAnalyzer.class);

        for (PatternInstancesFromDiff pi : result.getInfoPerDiff()) {
            if (pi.getInstances().size() > 0) {

                JsonObject instance = new JsonObject();

                instance.addProperty("revision", revisionIdentifier.toString());

//                log.info("\n--------\ncommit with instance:\n " + revisionIdentifier);
                System.out.println("\n--------\ncommit with instance:\n " + revisionIdentifier);
//                log.info(pi.getInstances());
                System.out.println(pi.getInstances());

                JsonArray repair_tools = new JsonArray();
                for (ChangePatternInstance instancePattern : pi.getInstances()) {

                    JsonObject repair = new JsonObject();
                    repair.addProperty("tool-name", (instancePattern.getPattern().getName().split(File.pathSeparator)[0]));
                    repair.addProperty("pattern-name", (instancePattern.getPattern().getName()));

                    JsonArray ops = new JsonArray();

                    for (PatternAction pa : instancePattern.getActionOperation().keySet()) {
                        Operation op = instancePattern.getActionOperation().get(pa);
                        JsonObject opjson = new JsonObject();
                        opjson.addProperty("pattern_action", pa.getAction().toString());
                        opjson.add("pattern_entity", getJSONFromEntity(pa.getAffectedEntity()));
                        opjson.add("concrete_change", getJSONFromOperator(op));

                        if (op.getNode().getPosition() != null) {
                            if (op.getNode().getPosition() != null) {
                                try {
                                    opjson.addProperty("line", op.getNode().getPosition().getLine());
                                } catch (UnsupportedOperationException e) {
                                    e.printStackTrace();
                                    opjson.addProperty("line", -1);
                                }
                                if (op.getNode().getPosition().getFile() != null) {
                                    opjson.addProperty("file", op.getNode().getPosition().getFile().getAbsolutePath());
                                }
                            }
                        }
                        ops.add(opjson);
                    }

                    repair.add("instance_detail", ops);
                    repair_tools.add(repair);

                }
                instance.add("repairability", repair_tools);
                instances.add(instance);

            }
        }
    }
}
