package fr.inria.coming.spoon.repairability.checkers.impl;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.spoon.repairability.checkers.DiffResultChecker;

public class DefaultDiffResultChecker implements DiffResultChecker {
    @Override
    public boolean isDiffResultCorrect(FinalResult result) {
        return true;
    }
}
