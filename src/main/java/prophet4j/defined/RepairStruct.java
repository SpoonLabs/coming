package prophet4j.defined;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import prophet4j.defined.RepairType.DiffActionType;
import prophet4j.defined.RepairType.RepairActionKind;
import prophet4j.defined.RepairType.RepairCandidateKind;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public interface RepairStruct {

    class DiffEntry { // DiffResultEntry
        // the reason why CtElement is used here is because clang::Expr isa clang::Stmt
        public DiffActionType type;
        public CtElement srcElem, dstElem;
        public DiffEntry(DiffActionType type, CtElement srcElem, CtElement dstElem) {
            this.type = type;
            this.srcElem = srcElem;
            this.dstElem = dstElem;
        }
    }

    class RepairAction {
        public RepairActionKind kind;
        // loc.stmt from "public ASTLocTy loc;"
        public CtElement loc_stmt;
        // It is a clang::Stmt or clang::Expr
        // todo: this should just be one placeholder, as replaceExprInCandidate() in CodeRewrite.cpp
        public CtElement ast_node;
        // This will only be used for expr level mutations
        List<CtElement> candidate_atoms;

        public RepairAction(RepairActionKind kind, CtElement loc_stmt, CtElement new_elem) {
            this.kind = kind;
            this.loc_stmt = loc_stmt;
            this.ast_node = new_elem;
            this.candidate_atoms = new ArrayList<>();
        }

        public RepairAction(CtElement loc_stmt, CtElement expr, List<CtElement> candidate_atoms) {
            this.kind = RepairActionKind.ExprMutationKind;
            this.loc_stmt = loc_stmt;
            this.ast_node = expr;
            this.candidate_atoms = candidate_atoms;
        }
    }

    class Repair {
        public RepairCandidateKind kind;
        public CtExpression oldRExpr, newRExpr; // info for replace only
        public List<RepairAction> actions;

        public Repair() {
            this.kind = null;
            this.oldRExpr = null;
            this.newRExpr = null;
            this.actions = new ArrayList<>();
        }

        // fixme: "candidate_atoms"
        public Set<CtElement> getCandidateAtoms() {
            Set<CtElement> ret = new HashSet<>();
            ret.add(null);
            for (RepairAction action: actions) {
                if (action.kind == RepairActionKind.ExprMutationKind) {
                    ret.addAll(action.candidate_atoms);
                    return ret;
                }
            }
            return ret;
        }
    }
}
