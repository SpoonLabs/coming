package fr.inria.coming.codefeatures;

import java.util.ArrayList;
import java.util.List;
import fr.inria.coming.codefeatures.codeanalyze.AbstractCodeAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.BinaryOperatorAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.ConstantAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.ConstructorAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.ExpressionAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.LogicalExpressionAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.MethodAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.TypeaccessAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.VariableAnalyzer;
import fr.inria.coming.codefeatures.codeanalyze.WholeStatementAnalyzer;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;


public class CodeFeatureDetector {
	
	public Cntx<?> analyzeFeatures(CtElement element, List<CtExpression> allExpressions, 
			List<CtExpression> allrootlogicalexpers, List<CtBinaryOperator> allBinOperators) {
		
		CodeElementInfo infoElementStudy = new CodeElementInfo (element, allExpressions, allrootlogicalexpers, allBinOperators);
		
		List<AbstractCodeAnalyzer> analyzers = new ArrayList<>();
		
		analyzers.add(new VariableAnalyzer(infoElementStudy));
		
		analyzers.add(new BinaryOperatorAnalyzer(infoElementStudy));
		
		analyzers.add(new ConstantAnalyzer(infoElementStudy));

		analyzers.add(new ConstructorAnalyzer(infoElementStudy));

	//	analyzers.add(new ExpressionAnalyzer(infoElementStudy));

		analyzers.add(new LogicalExpressionAnalyzer(infoElementStudy));
		
		analyzers.add(new TypeaccessAnalyzer(infoElementStudy));

		analyzers.add(new MethodAnalyzer(infoElementStudy));

		analyzers.add(new WholeStatementAnalyzer(infoElementStudy));
		
		for(int index=0; index<analyzers.size(); index++) {
			analyzers.get(index).analyze();
		}

		return infoElementStudy.context;
	}
}
