package prophet4j.defined;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import prophet4j.defined.RepairType.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public interface RepairStruct {

    class DiffResultEntry {
        // the reason why CtElement is used here is because clang::Expr isa clang::Stmt
        public CtElement srcElem, dstElem;
        public DiffActionKindTy kind;
        public DiffResultEntry(CtElement srcElem, CtElement dstElem, DiffActionKindTy kind) {
            this.srcElem = srcElem;
            this.dstElem = dstElem;
            this.kind = kind;
        }
    }

    /*
    typedef std::vector<clang::Stmt*> StmtListTy;
    typedef std::vector<clang::Expr*> ExprListTy;
     */
    class RepairAction {
        // tag = 1, means a statement level mutation
        // tag = 2, means a expr level mutation
        public RepairActionKind kind;
        // loc.stmt from "public ASTLocTy loc;"
        public CtElement loc_stmt;
        // It is a clang::Stmt or clang::Expr
        // todo: this should just be one placeholder, as replaceExprInCandidate() in CodeRewrite.cpp
        public CtElement ast_node;
        // This will only be used for expr level mutations
        List<CtElement> candidate_atoms;

        // This is a tag to indicate which subroutine created this action
        ExprTagTy tag;

        public RepairAction(CtElement loc_stmt, RepairActionKind kind, CtElement new_elem) {
            this.loc_stmt = loc_stmt;
            this.kind = kind;
            this.ast_node = new_elem;
            this.candidate_atoms = new ArrayList<>();// = null;
            this.tag = ExprTagTy.InvalidTag;
        }

        public RepairAction(CtElement loc_stmt, CtElement expr, List<CtElement> candidate_atoms) {
            this.loc_stmt = loc_stmt;
            this.kind = RepairActionKind.ExprMutationKind;
            this.ast_node = expr;
            this.candidate_atoms = candidate_atoms;
            this.tag = ExprTagTy.CondTag;
        }

        public RepairAction(CtElement loc_stmt, CtElement expr, List<CtElement> candidate_atoms, ExprTagTy tag) {
            this.loc_stmt = loc_stmt;
            this.kind = RepairActionKind.ExprMutationKind;
            this.ast_node = expr;
            this.candidate_atoms = candidate_atoms;
            assert tag.equals(ExprTagTy.StringConstantTag);
            this.tag = tag;
        }
    }

    class RepairCandidate {

        public List<RepairAction> actions;
        // Below are required information to calculate the property
        // of the support candidate

        public CandidateKind kind;
        public boolean is_first; // start of a block? not including condition changes
        public CtExpression oldRExpr, newRExpr; // info for replace only
        // score-related properties are not useful to us
        // This should be the human localization score for learning
        // or the pre-fixed score if not using learning
//        public double score;
//        Map<CtExpression, Double> scoreMap;

        public RepairCandidate() {
            this.actions = new ArrayList<>();
            this.kind = null;
            this.is_first = false;
            this.oldRExpr = null;
            this.newRExpr = null;
        }

        // only for test
        public RepairCandidate(DiffResultEntry res0, CandidateKind kind) {
            // todo: actually, candidates should be
            //  List<CtExpression> candidateVars = L.getCondCandidateVars(ori_cond.getLocEnd());
            List<CtElement> candidates = res0.dstElem.getElements(new TypeFilter<>(CtElement.class));
            this.actions = new ArrayList<>();
            this.actions.add(new RepairAction(res0.srcElem, res0.dstElem, candidates));
            List<CtElement> srcStmtList = getStmtList(res0.srcElem);
            List<CtElement> dstStmtList = getStmtList(res0.dstElem);
            int pivot = getPivot(srcStmtList, dstStmtList);
            this.is_first = pivot == 0;

            this.kind = kind;
            this.oldRExpr = null;
            this.newRExpr = null;
        }

        // only for test
        private List<CtElement> getStmtList(CtElement statement) {
            List<CtElement> stmtList = new ArrayList<>();
            if (statement instanceof CtClass || statement instanceof CtMethod) {
                stmtList.add(statement);
            } else {
                CtElement parent = statement.getParent();
                List<CtStatement> tmpList = parent.getElements(new TypeFilter<>(CtStatement.class));
                if (parent instanceof CtStatement) {
                    tmpList.remove(0);
                }
                stmtList.addAll(tmpList);
            }
            return stmtList;
        }

        // only for test
        private int getPivot(List<CtElement> srcStmtList, List<CtElement> dstStmtList) {
            int pivot = Math.min(srcStmtList.size(), dstStmtList.size());
            for (int i = 0; i < Math.min(srcStmtList.size(), dstStmtList.size()); i++) {
                if (!srcStmtList.get(i).equals(dstStmtList.get(i))) {
                    pivot = i;
                    break;
                }
            }
            return pivot;
        }

        // fixme: Repeated Calculation here (check ret)
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

//        String toString(SourceContextManager M) { }

//        void dump() { }

//        class RepairCandidateGeneratorImpl { }

//        // for reference
//        @Override
//        public String toString() {
//            return "Context " + features + "\n";
//        }
    }

//    class RepairCandidateGeneratorImpl;
//
//    typedef std::pair<RepairCandidate, double> RepairCandidateWithScore;
//
//    struct RepairComp {
//        bool operator () (const RepairCandidateWithScore &a, const RepairCandidateWithScore &b) {
//            return a.second < b.second;
//        }
//    };
//
//    typedef std::priority_queue<RepairCandidateWithScore, std::vector<RepairCandidateWithScore>,
//    RepairComp> RepairCandidateQueue;
//
//    class RepairCandidateGenerator {
//        RepairCandidateGeneratorImpl *impl;
//        public:
//        RepairCandidateGenerator(SourceContextManager &M, const std::string &file,
//            const std::map<clang::Stmt*, unsigned long> &locs,
//                                 bool naive, bool learning);
//
//        void setFlipP(double GeoP);
//
//    ~RepairCandidateGenerator();
//
//        std::vector<RepairCandidate> run();
//    };
}
