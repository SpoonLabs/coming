package prophet4j.support;

// based on LocalAnalyzer.cpp (maybe not necessary to implement)
public class LocalAnalyzer {
//
//    List<CtStatement> StmtStackTy = new ArrayList<>();
//    List<CtExpression> ExprListTy = new ArrayList<>();
//    // todo: maybe i should not remove this
//    clang::ASTContext *ctxt;
//    ASTLocTy loc;
//    GlobalAnalyzer G;
//    clang::FunctionDecl curFunc;
//    Set<clang::VarDecl> LocalVarDecls;
//    Set<Integer> IntegerConstants = new HashSet<>();
//    Set<CtExpression> MemberStems = new HashSet<>();
//    Map<String, Set<Integer> > ExprDis = new HashMap<>();
//    List<clang::LabelDecl> LocalLabels;
//    boolean inside_loop;
//    boolean naive;
//
//    LocalAnalyzer(clang::ASTContext *ctxt, GlobalAnalyzer G, ASTLocTy loc, boolean naive) {
//        this.ctxt = ctxt;
//        this.G = G;
//        this.loc = loc;
//        this.naive = naive;
//        this.curFunc = null;
//
//        StmtStackVisitor visitor1(loc);
//        TranslationUnitDecl *TransUnit = ctxt->getTranslationUnitDecl();
//        visitor1.TraverseDecl(TransUnit);
//        curFunc = visitor1.getEnclosingFunctionDecl();
//
//        StmtStackTy stmtStack = visitor1.getStmtStack();
//        inside_loop = false;
//        // Note that we exclude the last statement (itself)
//        for (size_t i = 0; i < stmtStack.size() - 1; i++ ) {
//            if (llvm::isa<ForStmt>(stmtStack[i]) || llvm::isa<WhileStmt>(stmtStack[i]) ||
//                    llvm::isa<DoStmt>(stmtStack[i])) {
//                inside_loop = true;
//                break;
//            }
//        }
//
//        LocalActiveVarVisitor visitor2(stmtStack, curFunc);
//        visitor2.TraverseDecl(TransUnit);
//        LocalVarDecls = visitor2.getValidLocalVarDeclSet();
//    /*for (std::set<VarDecl*>::iterator it = LocalVarDecls.begin(); it != LocalVarDecls.end(); ++it)
//        (*it)->dump();*/
//
//        MemberExprStemVisitor visitor3(ctxt);
//        visitor3.TraverseFunctionDecl(curFunc);
//        MemberStems = visitor3.getStemExprSet();
//
//        ExprDis.clear();
//        ExprDisVisitor visitor4(ctxt, ExprDis, loc);
//        visitor4.TraverseFunctionDecl(curFunc);
//        //for (std::set<Expr*>::iterator it = MemberStems.begin(); it != MemberStems.end(); ++it)
//        //(*it)->dump();
//
//        IntegerConstants.clear();
//        // We start with 0 by default
//        IntegerConstants.insert(0);
//        IntegerConstantVisitor visitor5(ctxt, IntegerConstants);
//        visitor5.TraverseFunctionDecl(curFunc);
//
//        LocalLabels.clear();
//        GotoLabelVisitor visitor6(LocalLabels);
//        visitor6.TraverseFunctionDecl(curFunc);
//    }
//
//    List<CtElement> getCondCandidateVars(SourceLocation SL) {
//        SourceManager &SM = ctxt->getSourceManager();
//        boolean invalid = false;
//        int line_number = SM.getExpansionLineNumber(SL, invalid);
//        assert(!invalid);
//        List<CtElement> exprs = genExprAtoms(QualType(), true, true, true, false, true);
//        List<CtElement> ret = new ArrayList<>();
//        List<HashMap.Entry<Integer, CtElement>> tmp_v = new ArrayList<>();
//        for (int i = 0; i < exprs.size(); i++) {
//            //exprs[i]->dump();
//            QualType QT = exprs.get(i)->getType();
//            if (!QT->isIntegerType() && !QT->isPointerType()) continue;
//            //llvm::errs() << "Type correct!\n";
//            MemberExpr ME = llvm::dyn_cast<MemberExpr>(exprs.get(i));
//            if (ME) {
//                int dis1 = getExprDistance(ME->getBase(), line_number);
//                //llvm::errs() << "Dis1: " << dis1 << "\n";
//                if (dis1 > 1)
//                    continue;
//            }
//            // todo: i commented just feel not so important
//            //llvm::errs() << "Member expr checking correct!\n";
//            if (exprs.get(i) instanceof CtReference) {
//                if (getExprDistance(DRE, loc.stmt) > 1000) continue;
//            }
//            //llvm::errs() << "Distance checking correct\n";
//            if (isValidStmt(exprs.get(i), null)) {
//                //llvm::errs() << "Valid, passed!\n";
//                tmp_v.add(new HashMap.Entry<Integer, CtElement>(getExprDistance(exprs.get(i), line_number), exprs.get(i)));
//            }
//        }
//        sort(tmp_v.begin(), tmp_v.end());
//        for (int i = 0; i < tmp_v.size(); i++)
//            ret.add(tmp_v.get(i).getValue());
//        return ret;
//    }
}
