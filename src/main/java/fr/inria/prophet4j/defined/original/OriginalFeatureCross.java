package fr.inria.prophet4j.defined.original;

import java.util.ArrayList;
import java.util.List;

import fr.inria.prophet4j.defined.Feature;
import fr.inria.prophet4j.defined.FeatureCross;

import static fr.inria.prophet4j.defined.original.OriginalFeature.*;

public class OriginalFeatureCross implements FeatureCross {
    private Integer id;
    private List<Feature> features;

    public OriginalFeatureCross(Integer id) {
        this.id = id;
        this.features = new ArrayList<>();
        if (id >= FEATURE_BASE_3) {
            int tmp = id - FEATURE_BASE_3;
            int ordinal0 = tmp / VF_SIZE;
            int ordinal1 = tmp % VF_SIZE;
            this.features.add(AtomicFeature.values()[ordinal0]);
            this.features.add(ValueFeature.values()[ordinal1]);
        } else if (id >= FEATURE_BASE_2) {
            int tmp = id - FEATURE_BASE_2;
            int ordinal0 = tmp / (AF_SIZE * AF_SIZE);
            int ordinal1 = (tmp % (AF_SIZE * AF_SIZE)) / AF_SIZE;
            int ordinal2 = (tmp % (AF_SIZE * AF_SIZE)) % AF_SIZE;
            this.features.add(Position.values()[ordinal0]);
            this.features.add(AtomicFeature.values()[ordinal1]);
            this.features.add(AtomicFeature.values()[ordinal2]);
        } else if (id >= FEATURE_BASE_1) {
            int tmp = id - FEATURE_BASE_1;
            int ordinal0 = tmp / (AF_SIZE * RF_SIZE);
            int ordinal1 = (tmp % (AF_SIZE * RF_SIZE)) / RF_SIZE;
            int ordinal2 = (tmp % (AF_SIZE * RF_SIZE)) % RF_SIZE;
            this.features.add(Position.values()[ordinal0]);
            this.features.add(AtomicFeature.values()[ordinal1]);
            this.features.add(RepairFeature.values()[ordinal2]);
        } else if (id >= FEATURE_BASE_0) {
            int ordinal0 = id - FEATURE_BASE_0;
            this.features.add(RepairFeature.values()[ordinal0]);
        }
    }

    public OriginalFeatureCross(CrossType crossType, List<Feature> features) {
        int ordinal0, ordinal1, ordinal2;

        switch (crossType) {
            case RF_CT:
                assert features.size() == 1;
                assert features.get(0) instanceof RepairFeature;
                ordinal0 = ((RepairFeature) features.get(0)).ordinal();
                this.id = FEATURE_BASE_0 + ordinal0;
                break;
            case POS_AF_RF_CT:
                assert features.size() == 3;
                assert features.get(0) instanceof Position;
                assert features.get(1) instanceof AtomicFeature;
                assert features.get(2) instanceof RepairFeature;
                ordinal0 = ((Position) features.get(0)).ordinal();
                ordinal1 = ((AtomicFeature) features.get(1)).ordinal();
                ordinal2 = ((RepairFeature) features.get(2)).ordinal();
                this.id = FEATURE_BASE_1 + ordinal0 * AF_SIZE * RF_SIZE + ordinal1 * RF_SIZE + ordinal2;
                break;
            case POS_AF_AF_CT:
                assert features.size() == 3;
                assert features.get(0) instanceof Position;
                assert features.get(1) instanceof AtomicFeature;
                assert features.get(2) instanceof AtomicFeature;
                ordinal0 = ((Position) features.get(0)).ordinal();
                ordinal1 = ((AtomicFeature) features.get(1)).ordinal();
                ordinal2 = ((AtomicFeature) features.get(2)).ordinal();
                this.id = FEATURE_BASE_2 + ordinal0 * AF_SIZE * AF_SIZE + ordinal1 * AF_SIZE + ordinal2;
                break;
            case AF_VF_CT:
                assert features.size() == 2;
                assert features.get(0) instanceof AtomicFeature;
                assert features.get(1) instanceof ValueFeature;
                ordinal0 = ((AtomicFeature) features.get(0)).ordinal();
                ordinal1 = ((ValueFeature) features.get(1)).ordinal();
                this.id = FEATURE_BASE_3 + ordinal0 * VF_SIZE + ordinal1;
                break;
        }
        this.features = features;
    }

    public Integer getId() {
        return id;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public boolean containFeature(Feature feature) {
        return features.contains(feature);
    }

    @Override
    public String toString() {
        return "FeatureCross: " + features;
    }
}
