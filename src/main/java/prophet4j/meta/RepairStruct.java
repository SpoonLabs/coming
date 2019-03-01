package prophet4j.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import prophet4j.meta.RepairType.DiffActionType;
import prophet4j.meta.RepairType.RepairActionKind;
import prophet4j.meta.RepairType.RepairCandidateKind;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;

public interface RepairStruct {

    class DiffEntry { // DiffResultEntry
        // the reason why CtElement is used here is because clang::Expr isa clang::Stmt
        public DiffActionType type;
        public CtElement srcNode, dstNode;
        public DiffEntry(DiffActionType type, CtElement srcNode, CtElement dstNode) {
            this.type = type;
            this.srcNode = srcNode;
            this.dstNode = dstNode;
            if (this.srcNode instanceof CtExpression) {
                while (!(this.srcNode instanceof CtStatement)){
                    CtElement parent = this.srcNode.getParent();
                    // avoid analyzing all body of CtIf or CtLoop, caused by head expression
                    if (parent instanceof CtIf || parent instanceof CtLoop) break;
                    this.srcNode = parent;
                }
            }
            if (this.dstNode instanceof CtExpression) {
                while (!(this.dstNode instanceof CtStatement)){
                    CtElement parent = this.dstNode.getParent();
                    // avoid analyzing all body of CtIf or CtLoop, caused by head expression
                    if (parent instanceof CtIf || parent instanceof CtLoop) break;
                    this.dstNode = parent;
                }
            }
        }
    }

    class RepairAction {
        public RepairActionKind kind;
        // loc.stmt from "public ASTLocTy loc;"
        public CtElement srcElem;
        // It is a clang::Stmt or clang::Expr
        // todo: this should just be one placeholder, as replaceExprInCandidate() in CodeRewrite.cpp
        public CtElement dstElem;
        // This will only be used for expr level mutations
        List<CtElement> atoms;

        public RepairAction(RepairActionKind kind, CtElement srcElem, CtElement dstElem) {
            this.kind = kind;
            this.srcElem = srcElem;
            this.dstElem = dstElem;
            this.atoms = new ArrayList<>();
        }

        public RepairAction(CtElement srcElem, CtElement dstElem, List<CtElement> atoms) {
            this.kind = RepairActionKind.ExprMutationKind;
            this.srcElem = srcElem;
            this.dstElem = dstElem;
            this.atoms = atoms;
        }
    }

    class Repair {
        public RepairCandidateKind kind;
        public CtElement oldRExpr, newRExpr; // info for replace only
        public List<RepairAction> actions;

        public Repair() {
            this.kind = null;
            this.oldRExpr = null;
            this.newRExpr = null;
            this.actions = new ArrayList<>();
        }

        public Set<CtElement> getCandidateAtoms() {
            Set<CtElement> ret = new HashSet<>();
            ret.add(null);
            for (RepairAction action: actions) {
                if (action.kind == RepairActionKind.ExprMutationKind) {
                    ret.addAll(action.atoms);
                    return ret;
                }
            }
            return ret;
        }
    }
}
