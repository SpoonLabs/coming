package fr.inria.coming.spoon.repairability.checkers;

import fr.inria.coming.changeminer.entity.FinalResult;

public interface DiffResultChecker {
    public boolean isDiffResultCorrect(FinalResult result);
}
