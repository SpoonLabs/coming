package fr.inria.prophet4j.feature;

import fr.inria.prophet4j.utility.Structure.Repair;

import java.util.*;

// based on RepairGenerator.cpp
public interface RepairGenerator {
    Repair obtainHumanRepair();

    // https://people.csail.mit.edu/fanl/papers/spr-fse15.pdf <3.2 Transformation Schemas> Figure 4
    List<Repair> obtainRepairCandidates();
}
