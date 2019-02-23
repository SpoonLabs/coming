package prophet4j.unused;

// based on pdiffer.cpp, ASTDiffer.cpp
public class CodeDiffer {
//
//    private static final Logger logger = LogManager.getLogger(CodeDiffer.class.getName());
//
//    private Set<CtElement> lookupCandidateAtoms(Repair rc, Set<CtElement> expressions) {
//        Set<CtElement> atoms = rc.getCandidateAtoms();
//        // get the intersection of two sets
//        // fixme: ...
//        expressions.retainAll(atoms);
//        return expressions;
//    }
//
//    private Set<CtElement> matchCandidateWithHumanFix(Repair rc, DiffEntry res0) {
//        Set<CtElement> insMatchSet = new HashSet<>();
//
//        switch (rc.kind) {
//            case IfExitKind: // INSERT_CONTROL_RF
//                // add if-exit
//                if (res0.kind == DiffActionType.InsertAction || res0.kind == DiffActionType.ReplaceAction) {
//                    //XXX: We specially handle the case where the support replaces the break/return statement
//                    boolean yes = true;
//                    CtStatement S1 = (CtStatement) res0.srcCommonAncestor;
//                    CtStatement S2 = (CtStatement) res0.dstCommonAncestor;
//                    if (res0.kind == DiffActionType.ReplaceAction) {
//                        if (!(S1 instanceof CtBreak || S1 instanceof CtReturn))
//                            yes = false;
//                        if (!(S2 instanceof CtBreak || S2 instanceof CtReturn))
//                            yes = false;
//                    }
//                    if (yes) {
//                        // We have to dig make sure this
//                        CtStatement S = S2;
//                        if (S2 instanceof CtIf) {
//                            CtIf IF2 = (CtIf) S2;
//                            Set<CtElement> tmp = new HashSet<>();
//                            if (IF2.getElseStatement() == null) {
//                                S = IF2.getThenStatement();
//                                List<CtExpression> tmpList = IF2.getCondition().getElements(new TypeFilter<>(CtExpression.class));
//                                if (IF2.getCondition() instanceof CtExpression) {
//                                    tmpList = tmpList.subList(1, tmpList.size());
//                                }
//                                tmp.addAll(tmpList);
//                            }
//                            // Another tricky case is that S is wrapped by a compound statemt {}
//                            // Sometimes human patch will have this dummy stuff
//                            if (S instanceof CtStatementList) {
//                                CtStatementList CS = (CtStatementList) S;
//                                if (CS.getStatements().size() == 1)
//                                    S = CS.getLastStatement();
//                            }
//                            assert(rc.actions.size() == 2);
//                            assert(rc.actions.get(0).kind == RepairActionKind.InsertMutationKind);
//                            CtIf rc_IFS = (CtIf) rc.actions.get(0).dstCommonAncestor;
//                            CtStatement rc_S = rc_IFS.getThenStatement();
//                            if (S instanceof CtBreak || S instanceof CtReturn)
//                                if (rc_S.equals(S)) {
//                                    if (tmp.size() == 0) {
//                                        insMatchSet = rc.getCandidateAtoms();
//                                    } else {
//                                        insMatchSet = lookupCandidateAtoms(rc, tmp);
//                                    }
//                                }
//                        }
//                    }
//                }
//                break;
//            case GuardKind: // INSERT_GUARD_RF
//                // add guard
//                if (res0.kind == DiffActionType.ReplaceAction) {
//                    CtStatement S1 = (CtStatement) res0.srcCommonAncestor;
//                    if (res0.dstCommonAncestor instanceof CtIf) {
//                        CtIf IF2 = (CtIf) res0.dstCommonAncestor;
//                        CtStatement ThenS = IF2.getThenStatement();
//                        // Avoid common single statement compound stmt
//                        if (ThenS instanceof CtStatementList && S1 instanceof CtStatementList) {
//                            CtStatementList CS = (CtStatementList) ThenS;
//                            if (CS.getStatements().size() == 1)
//                                ThenS = CS.getLastStatement();
//                        }
//                        if (S1.equals(ThenS) && (IF2.getElseStatement() == null)) {
//                            List<CtExpression> tmpList = IF2.getCondition().getElements(new TypeFilter<>(CtExpression.class));
//                            if (IF2.getCondition() instanceof CtExpression) {
//                                tmpList = tmpList.subList(1, tmpList.size());
//                            }
//                            Set<CtElement> vars2 = new HashSet<>(tmpList);
//                            insMatchSet = lookupCandidateAtoms(rc, vars2);
//                        }
//                    }
//                } else if (res0.kind == DiffActionType.DeleteAction) {
//                    insMatchSet = rc.getCandidateAtoms();
//                }
//                break;
//            case SpecialGuardKind: // INSERT_GUARD_RF
//                // add special guard
//                if (res0.kind == DiffActionType.ReplaceAction) {
//                    if (res0.srcCommonAncestor instanceof CtIf && res0.dstCommonAncestor instanceof CtIf) {
//                        CtIf IF1 = (CtIf) res0.srcCommonAncestor;
//                        CtIf IF2 = (CtIf) res0.dstCommonAncestor;
//                        if (IF2.getCondition() instanceof CtBinaryOperator) {
//                            CtBinaryOperator condE = (CtBinaryOperator) IF2.getCondition();
//                            if (condE.getKind() == BinaryOperatorKind.AND)
//                                if (IF1.getCondition().equals(condE.getRightHandOperand())) {
//                                    List<CtExpression> tmpList = condE.getLeftHandOperand().getElements(new TypeFilter<>(CtExpression.class));
//                                    if (condE.getLeftHandOperand() instanceof CtExpression) {
//                                        tmpList = tmpList.subList(1, tmpList.size());
//                                    }
//                                    Set<CtElement> vars2 = new HashSet<>(tmpList);
//                                    insMatchSet = lookupCandidateAtoms(rc, vars2);
//                                }
//
//                        }
//                    }
//                }
//                break;
//            case AddInitKind: // INSERT_STMT_RF
//            case AddAndReplaceKind: // INSERT_STMT_RF
//                // all rest of replace case
//                if (res0.kind == DiffActionType.InsertAction) {
//                    assert(rc.actions.size() == 1);
//                    assert(rc.actions.get(0).kind == RepairActionKind.InsertMutationKind);
//                    CtStatement S1 = (CtStatement) rc.actions.get(0).dstCommonAncestor;
//                    CtStatement S2 = (CtStatement) res0.dstCommonAncestor;
//                    if(S1.equals(S2)) {
//                        insMatchSet.add(null);
//                    }
//                }
//                break;
//            case LoosenConditionKind: // REPLACE_COND_RF
//            case TightenConditionKind: // REPLACE_COND_RF
//                // change a condition
//                if (res0.kind == DiffActionType.ReplaceAction) {
//                    if (res0.srcCommonAncestor instanceof CtIf && res0.dstCommonAncestor instanceof CtIf) {
//                        CtIf IF1 = (CtIf) res0.srcCommonAncestor;
//                        CtIf IF2 = (CtIf) res0.dstCommonAncestor;
//                        assert(rc.actions.size() > 0);
//                        CtIf locIF = (CtIf) rc.actions.get(0).srcCommonAncestor;
//                        if (locIF.equals(IF1))
//                            if (IF1.getThenStatement().equals(IF2.getThenStatement()) &&
//                                    IF1.getElseStatement().equals(IF2.getElseStatement()))
//                                // the new condition should be equal to InSearchSpaceHelper()
//                                if (IF1.getElements(new TypeFilter<>(CtInvocation.class)).size()==0) {
//                                    CtExpression Cond1 = IF1.getCondition();
//                                    CtExpression Cond2 = IF2.getCondition();
//                                    // We first break two conditions with &&, || and () as separator
//                                    List<CtExpression> tmpList;
//                                    tmpList = Cond1.getElements(new TypeFilter<>(CtExpression.class));
//                                    if (Cond1 instanceof CtExpression) {
//                                        tmpList = tmpList.subList(1, tmpList.size());
//                                    }
//                                    Set<CtExpression> varm1 = new HashSet<>(tmpList);
//                                    tmpList = Cond2.getElements(new TypeFilter<>(CtExpression.class));
//                                    if (Cond2 instanceof CtExpression) {
//                                        tmpList = tmpList.subList(1, tmpList.size());
//                                    }
//                                    Set<CtExpression> varm2 = new HashSet<>(tmpList);
//                                    // Find out changed small clauses and get variables inside
//                                    Set<CtElement> tmp = new HashSet<>();
//                                    for (CtExpression it: varm1) {
//                                        if (!varm2.contains(it)) {
//                                            tmp.add(it);
//                                        }
//                                    }
//                                    for (CtExpression it: varm2) {
//                                        if (!varm1.contains(it)) {
//                                            tmp.add(it);
//                                        }
//                                    }
//                                    if (tmp.size() == 0) {
//                                        // we assume it is operator changes, and we put null into the set
//                                        insMatchSet.add(null);
//                                    } else {
//                                        insMatchSet = lookupCandidateAtoms(rc, tmp);
//                                    }
//                                }
//                    }
//                }
//                break;
//            case ReplaceKind: // REPLACE_STMT_RF
//                // all rest of replace case
//                if (res0.kind == DiffActionType.ReplaceAction) {
//                    assert(rc.actions.size() == 1);
//                    assert(rc.actions.get(0).kind == RepairActionKind.ReplaceMutationKind);
//                    CtStatement S1 = (CtStatement) rc.actions.get(0).dstCommonAncestor;
//                    CtStatement S2 = (CtStatement) res0.dstCommonAncestor;
//                    if (S1.equals(S2)) {
//                        insMatchSet.add(null);
//                    }
//                }
//                break;
//            case ReplaceStringKind: // REPLACE_STMT_RF
//                // replace string kind
//                if (res0.kind == DiffActionType.ReplaceAction) {
//                    if (rc.oldRExpr instanceof CtLiteral && res0.srcCommonAncestor instanceof CtLiteral) {
//                        CtLiteral SL1 = (CtLiteral) rc.oldRExpr;
//                        CtLiteral SL2 = (CtLiteral) res0.srcCommonAncestor;
//                        if (SL1.equals(SL2)) {
//                            insMatchSet.add(null);
//                        }
//                    }
//                }
//                break;
//        }
//        return insMatchSet;
//    }
//
//    // based on LocationFuzzer class
//    private Map<CtElement, Integer> fuzzyLocator(CtElement statement) {
//        Map<CtElement, Integer> locations = new HashMap<>();
//        List<CtStatement> statements = statement.getParent().getElements(new TypeFilter<>(CtStatement.class));
//        if (statement.getParent() instanceof CtStatement) {
//            statements = statements.subList(1, statements.size());
//        }
//        int idx = statements.indexOf(statement);
//        if (idx > 0)
//            locations.put(statements.get(idx - 1), 0);
//        locations.put(statements.get(idx), 0);
//        if (idx < statements.size() - 1)
//            locations.put(statements.get(idx + 1), 0);
//        return locations;
//    }
//
//    private DiffEntry generateDiffResultEntry(Diff diff, CtElement srcRoot, CtElement dstRoot) throws IndexOutOfBoundsException {
//        CtElement ancestor = diff.commonAncestor();
//        if (ancestor instanceof CtExpression) {
//            while (!(ancestor instanceof CtStatement)){
//                ancestor = ancestor.getParent();
//            }
//        }
//        // p & p' in Feature Extraction Algorithm
//        // we have to handle the CtPath here because evaluateOn() would be invalid when it meets #subPackage
//        CtPath ancestorPath = ancestor.getPath();
//        String ancestorPathString = ancestorPath.toString();
//        ancestorPathString = ancestorPathString.substring(ancestorPathString.indexOf("#containedType"));
//        ancestorPath = new CtPathStringBuilder().fromString(ancestorPathString);
//        List<CtElement> srcStmtList = new ArrayList<>(ancestorPath.evaluateOn(srcRoot));
//        assert srcStmtList.size() == 1;
//        CtElement srcAncestor = srcStmtList.get(0);
////        srcStmtList = getStmtList(srcAncestor);
//        List<CtElement> dstStmtList = new ArrayList<>(ancestorPath.evaluateOn(dstRoot));
//        assert dstStmtList.size() == 1;
//        CtElement dstAncestor = dstStmtList.get(0);
////        dstStmtList = getStmtList(dstAncestor);
//
//        // here is the DiffActionType wrapper for Spoon OperationKind
//        List<Operation> operations = diff.getRootOperations();
//        boolean existDelete = false;
//        boolean existInsert = false;
//        boolean existUpdate = false;
////        boolean existMove = false; // it seems not meaningful to us right now
//        /* https://github.com/SpoonLabs/gumtree-spoon-ast-diff/issues/55
//        In Gumtree, an "Update" operation means that:
//        - either the it's a string based element and the string has changed
//        - or that only a small fraction of children has changed (to be verified).
//        Assume that we have one literal replaced by a method call. This is represented by one deletion and one addition. We can have a higher-level operation "Replace" instead.
//         */
//        for (Operation operation: operations) {
//            if (operation instanceof DeleteOperation) {
//                existDelete = true;
//            } else if (operation instanceof InsertOperation) {
//                existInsert = true;
//            } else if (operation instanceof UpdateOperation) {
//                existUpdate = true;
////            } else if (operation instanceof MoveOperation) {
////                existMove = true;
//            }
//        }
//        DiffActionType kind = DiffActionType.UnknownAction;
//        if (existDelete && existInsert || existUpdate) {
//            kind = DiffActionType.ReplaceAction;
//        } else if (existDelete) {
//            kind = DiffActionType.DeleteAction;
//        } else if (existInsert) {
//            kind = DiffActionType.InsertAction;
//        }
//        // todo: add more asserts in other places to help debug
//        assert kind != DiffActionType.UnknownAction;
//        return new DiffEntry(srcAncestor, dstAncestor, kind);
//    }
//
//    public List<FeatureVector> func4Demo(File file0, File file1) throws Exception {
//        List<FeatureVector> featureVectors = new ArrayList<>();
//
//        AstComparator comparator = new AstComparator();
//        Diff diff = comparator.compare(file0, file1);
////        System.out.println("========");
////        System.out.println(diff.getRootOperations());
////        System.out.println(diff.commonAncestor());
//        CtElement srcRoot = comparator.getCtType(file0).getParent();
//        CtElement dstRoot = comparator.getCtType(file1).getParent();
//
////        System.out.println(srcRoot);
////        System.out.println(dstRoot);
//
//        try {
//            DiffEntry res0 = generateDiffResultEntry(diff, srcRoot, dstRoot);
//            // todo: check all cast operations
//            CtStatement locStmt = (CtStatement) res0.srcCommonAncestor;
//            Map<CtElement, Integer> locations = fuzzyLocator(locStmt);
//
//            RepairGenerator G = new RepairGenerator(srcRoot, locations, false, false);
//            List<Repair> spaces = G.genRepairCandidates();
//            FeatureExtractor featureExtractor = new FeatureExtractor();
////        FeatureResolver featureResolver = new FeatureResolver();
//            for (Repair rc: spaces) {
//                Set<CtElement> insSet = rc.getCandidateAtoms();
//                Set<CtElement> insMatchSet = new HashSet<>();
//                assert(rc.actions.size() > 0);
//                if (rc.actions.get(0).srcCommonAncestor.equals(locStmt)) {
//                    insMatchSet = matchCandidateWithHumanFix(rc, res0);
//                }
//                System.out.println(insSet.size() + "@@@@" + insSet);
//                System.out.println(insMatchSet.size() + "$$$$" + insMatchSet);
//                // null is the only value in insSet (to implement repair-features)
//                for (CtElement expr : insSet) {
//                    FeatureVector featureVector = featureExtractor.extractFeature(rc, expr).getFeatureVector();
////                FeatureVector featureVector = featureResolver.easyExtractor(file0, file1).getFeatureVector();
//                    if (insMatchSet.contains(expr)) {
//                    featureVector.setMark();
//                    logger.log(Level.INFO, "CandidateType: " + rc.kind + "Found for:\n " + expr);
//                    }
//                    featureVectors.add(featureVector);
//                }
//            }
//        } catch (IndexOutOfBoundsException e) {
//            logger.log(Level.WARN, "diff.commonAncestor() returns null value");
//        }
//
//        return featureVectors;
//    }
//
//    // for FeatureExtractorTest.java
//    public List<FeatureVector> func4Test(String str0, String str1, RepairCandidateKind kind) {
//        List<FeatureVector> featureVectors = new ArrayList<>();
//
//        AstComparator comparator = new AstComparator();
//        Diff diff = comparator.compare(str0, str1);
//        CtElement srcRoot = comparator.getCtType(str0).getParent();
//        CtElement dstRoot = comparator.getCtType(str1).getParent();
//
//        try {
//            DiffEntry res0 = generateDiffResultEntry(diff, srcRoot, dstRoot);
//            Repair rc = new Repair(res0, kind);
//            Set<CtElement> insSet = rc.getCandidateAtoms();
//            assert(rc.actions.size() > 0);
//            FeatureExtractor featureExtractor = new FeatureExtractor();
//            for (CtElement expr : insSet) {
//                System.out.println("********");
//                System.out.println(expr);
//                FeatureVector featureVector = featureExtractor.extractFeature(rc, expr).getFeatureVector();
//                featureVectors.add(featureVector);
//            }
//        } catch (IndexOutOfBoundsException e) {
//            logger.log(Level.WARN, "diff.commonAncestor() returns null value");
//        }
//        return featureVectors;
//    }
}
