package prophet4j.defined;

public interface RepairType {
    enum DiffActionType {
        DeleteAction,
        InsertAction,
        ReplaceAction,
        UnknownAction,
    }

    // originally defined at struct RepairAction
    enum RepairActionKind { // what are their differences?
        ReplaceMutationKind,
        InsertMutationKind,
        InsertAfterMutationKind,
        ExprMutationKind
    }

    // originally defined at struct Repair
    enum RepairCandidateKind { // implementation is at RepairGenerator.java
        // INSERT_CONTROL_RF
        IfExitKind,             // genAddIfExit()
        // INSERT_GUARD_RF
        GuardKind,              // genAddIfGuard()
        SpecialGuardKind,       // genAddIfGuard()
        // INSERT_STMT_RF
        AddInitKind,            // genAddMemset() seems inapplicable to Java?
        AddAndReplaceKind,      // genAddStatement()
        // REPLACE_COND_RF
        TightenConditionKind,   // genTightCondition()
        LoosenConditionKind,    // genLooseCondition()
        // REPLACE_STMT_RF
        ReplaceKind,            // genReplaceStmt()
        ReplaceStringKind,      // genReplaceStmt()
    }
}
