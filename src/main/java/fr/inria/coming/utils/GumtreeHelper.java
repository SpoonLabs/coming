package fr.inria.coming.utils;

import fr.inria.coming.repairability.models.InstanceStats;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

/**
 * Created by khesoem on 10/4/2019.
 */
public class GumtreeHelper {

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

}
