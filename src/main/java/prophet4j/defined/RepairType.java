package prophet4j.defined;

public interface RepairType {
    // i feel there are some definitions redundant
    enum DiffActionKindTy {
        DeleteAction,
        InsertAction,
        ReplaceAction,
        UnknownAction,
    }

    // originally defined at struct RepairAction
    enum ExprTagTy {
        InvalidTag,
        CondTag,
        StringConstantTag, // i do not know why this tag is specialized (refer to ExprSynthesizer.cpp someday)
        // tag is used in ExprSynthesizer.cpp
    }

    // originally defined at struct RepairAction
    enum RepairActionKind {
        ReplaceMutationKind,
        InsertMutationKind, // no need to consider memset as it does not apply to Java
        InsertAfterMutationKind,
        ExprMutationKind
    }

    // originally defined at struct RepairCandidate
    enum CandidateKind { // implementation is at RepairCandidateGenerator.java
        // INSERT_CONTROL_RF
        IfExitKind,             // genAddIfExit()
        // INSERT_GUARD_RF
        GuardKind,              // genAddIfGuard()
        SpecialGuardKind,       // genAddIfGuard()
        // INSERT_STMT_RF
        AddInitKind,            // genAddMemset() seems not apply to Java?
        AddAndReplaceKind,      // genAddStatement()
        // REPLACE_COND_RF
        TightenConditionKind,   // genTightCondition()
        LoosenConditionKind,    // genLooseCondition()
        // REPLACE_STMT_RF
        ReplaceKind,            // genReplaceStmt()
        ReplaceStringKind,      // genReplaceStmt()
    }
}
