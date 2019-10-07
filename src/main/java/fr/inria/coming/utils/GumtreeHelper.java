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
        stats.setNumberOfSrcEntities(operation.getSrcNode().getElements(null).size());
        if (operation.getDstNode() != null)
            stats.setNumberOfDstEntities(operation.getDstNode().getElements(null).size());
        stats.setSrcEntityTypes(operation.getSrcNode().getReferencedTypes());
        if (operation.getDstNode() != null)
            stats.setDstEntityTypes(operation.getDstNode().getReferencedTypes());
        return stats;
    }

}
