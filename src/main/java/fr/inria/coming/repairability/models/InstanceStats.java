package fr.inria.coming.repairability.models;

import spoon.reflect.reference.CtTypeReference;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by khesoem on 10/4/2019.
 */
public class InstanceStats {
    private int numberOfSrcEntities;
    private Set<CtTypeReference<?>> srcEntityTypes;
    private int numberOfDstEntities;
    private Set<CtTypeReference<?>> dstEntityTypes;

    public InstanceStats(){
        numberOfDstEntities = -1;
        numberOfSrcEntities = -1;
        srcEntityTypes = new HashSet<>();
        dstEntityTypes = new HashSet<>();
    }

    public int getNumberOfSrcEntities() {
        return numberOfSrcEntities;
    }

    public void setNumberOfSrcEntities(int numberOfSrcEntities) {
        this.numberOfSrcEntities = numberOfSrcEntities;
    }

    public Set<CtTypeReference<?>> getSrcEntityTypes() {
        return srcEntityTypes;
    }

    public void setSrcEntityTypes(Set<CtTypeReference<?>> srcEntityTypes) {
        this.srcEntityTypes = srcEntityTypes;
    }

    public int getNumberOfDstEntities() {
        return numberOfDstEntities;
    }

    public void setNumberOfDstEntities(int numberOfDstEntities) {
        this.numberOfDstEntities = numberOfDstEntities;
    }

    public Set<CtTypeReference<?>> getDstEntityTypes() {
        return dstEntityTypes;
    }

    public void setDstEntityTypes(Set<CtTypeReference<?>> dstEntityTypes) {
        this.dstEntityTypes = dstEntityTypes;
    }
}
