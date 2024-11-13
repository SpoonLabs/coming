package fr.inria.coming.codefeatures.codeanalyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtWhile;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.CtScanner;

public class BinaryOperatorAnalyzer extends AbstractCodeAnalyzer {
	
    List<BinaryOperatorKind> logicalOperator = Arrays.asList(BinaryOperatorKind.OR, BinaryOperatorKind.AND);
	
	List<BinaryOperatorKind> bitOperator = Arrays.asList(BinaryOperatorKind.BITOR, BinaryOperatorKind.BITXOR,
			BinaryOperatorKind.BITAND);
	
	List<BinaryOperatorKind> compareOperator = Arrays.asList(BinaryOperatorKind.EQ, BinaryOperatorKind.NE,
			BinaryOperatorKind.LT, BinaryOperatorKind.GT, BinaryOperatorKind.LE, BinaryOperatorKind.GE);
	
	List<BinaryOperatorKind> shiftOperator = Arrays.asList(BinaryOperatorKind.SL, BinaryOperatorKind.SR,
			BinaryOperatorKind.USR);
	
	List<BinaryOperatorKind> mathOperator = Arrays.asList(BinaryOperatorKind.PLUS, BinaryOperatorKind.MINUS,
			BinaryOperatorKind.MUL, BinaryOperatorKind.DIV, BinaryOperatorKind.MOD);
	
	List<CodeFeatures> binoperatortype = Arrays.asList(CodeFeatures.O1_IS_LOGICAL, CodeFeatures.O1_IS_BIT,
			CodeFeatures.O1_IS_COMPARE, CodeFeatures.O1_IS_SHIFT, CodeFeatures.O1_IS_MATH, CodeFeatures.O1_IS_OTHERS);
	
	public BinaryOperatorAnalyzer (CodeElementInfo inputinfo) {
		super(inputinfo);
	}
	
	@Override
	public void analyze() {

		analyzeBinary_BinarySides(elementinfo.element, elementinfo.elementToStudy, elementinfo.context, 
				elementinfo.binoperators);
	}

	private void analyzeBinary_BinarySides(CtElement wholeoriginal, CtElement elementtostudy, Cntx<Object> context, 
			List<CtBinaryOperator> allbinaryoperators) {

		List<CtBinaryOperator> binaryOperatorsFromFaultyLine = allbinaryoperators;
		
		for(int index=0; index<binaryOperatorsFromFaultyLine.size(); index++) {
			
			CtBinaryOperator specificBinOperator = binaryOperatorsFromFaultyLine.get(index);
			
			analyzeBinaryOperatorKind(specificBinOperator, index, context);
			
			analyzeBinaryLogicalOperator(specificBinOperator, index, context);
			
			analyzeBinaryOperatorInvolveNull(specificBinOperator, index, context);
			
			analyzeBinaryOperatorInvolve01(specificBinOperator, index, context);
			
			analyzeBinaryOperatorCompareInCondition(wholeoriginal, specificBinOperator, index, context); 
			
			analyzeBinaryWhetehrMathRoot(specificBinOperator, index, context);
		}
	}
	
   private void analyzeBinaryWhetehrMathRoot (CtBinaryOperator operatorunderstudy, int operatorindex, Cntx<Object> context) {
		
		boolean whethermathroot = false;
		
		BinaryOperatorKind operatorkind = operatorunderstudy.getKind();

		if(mathOperator.contains(operatorkind)) {
			
			whethermathroot = true;
			
			CtElement parent = operatorunderstudy.getParent(CtBinaryOperator.class);
			
			if(parent!=null && mathOperator.contains(((CtBinaryOperator)parent).getKind()))
				whethermathroot =false;
		}
		
		writeGroupedInfo(context, Integer.toString(operatorindex)+"_"+ getSafeStringRepr(operatorunderstudy), CodeFeatures.O5_IS_MATH_ROOT,
				whethermathroot, "FEATURES_BINARYOPERATOR");	
	}
	
	// 765534, 1503331
	private void analyzeBinaryLogicalOperator(CtBinaryOperator operatorunderstudy, int operatorindex, Cntx<Object> context) {
		
		boolean whethercontainnotoperator = false;
		
		BinaryOperatorKind operatorkind = operatorunderstudy.getKind();

		if(logicalOperator.contains(operatorkind)) {
			
			CtExpression leftexpression = operatorunderstudy.getLeftHandOperand();
			CtExpression rightexpression = operatorunderstudy.getRightHandOperand();
					
			List<CtBinaryOperator> logicalOperatorLeft = leftexpression.getElements(
			  e -> e instanceof CtBinaryOperator && logicalOperator.contains(((CtBinaryOperator) e).getKind()));
			
			List<CtBinaryOperator> logicalOperatorRight = rightexpression.getElements(
					  e -> e instanceof CtBinaryOperator && logicalOperator.contains(((CtBinaryOperator) e).getKind()));
			
			if(logicalOperatorLeft.size() == 0) {	
				if(scannotoperator(leftexpression))
					whethercontainnotoperator=true;
			}
				
			if(!whethercontainnotoperator && logicalOperatorRight.size() == 0)	{
				if(scannotoperator(rightexpression))
					whethercontainnotoperator=true;
			}
		}
		
		writeGroupedInfo(context, Integer.toString(operatorindex)+"_"+ getSafeStringRepr(operatorunderstudy), CodeFeatures.O2_LOGICAL_CONTAIN_NOT,
				whethercontainnotoperator, "FEATURES_BINARYOPERATOR");
		
	}
	
	private boolean scannotoperator (CtExpression expressiontostudy) {
		
		List<String> unaryOps = new ArrayList();
		
		CtScanner scanner = new CtScanner() {

			@Override
			public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {

				super.visitCtUnaryOperator(operator);
				unaryOps.add(operator.getKind().toString());
			}
		};
		
		scanner.scan(expressiontostudy);
		
		return unaryOps.contains(UnaryOperatorKind.NOT.toString());
	}
	
	private void analyzeBinaryOperatorKind(CtBinaryOperator operatorunderstudy, int operatorindex, Cntx<Object> context) {
		
		BinaryOperatorKind operatorkind = operatorunderstudy.getKind();
		
		String operatorstring="";
		
		if(logicalOperator.contains(operatorkind)) {
			operatorstring="logical";
		} else if (bitOperator.contains(operatorkind)) {
			operatorstring="bit";
		} else if (compareOperator.contains(operatorkind)) {
			operatorstring="compare";
		} else if (shiftOperator.contains(operatorkind)) {
			operatorstring="shift";
		} else if (mathOperator.contains(operatorkind)) {
			operatorstring="math";
		} else operatorstring="others";
		
		for(int index=0; index<binoperatortype.size(); index++) {
			CodeFeatures cerainfeature = binoperatortype.get(index);

			final String operatorunderstudyStr = getSafeStringRepr(operatorunderstudy);
			if(cerainfeature.toString().endsWith(operatorstring.toUpperCase()))
				writeGroupedInfo(context,  Integer.toString(operatorindex)+"_"+ operatorunderstudyStr, cerainfeature,
							true, "FEATURES_BINARYOPERATOR");
			else writeGroupedInfo(context,  Integer.toString(operatorindex)+"_"+ operatorunderstudyStr, cerainfeature,
					false, "FEATURES_BINARYOPERATOR");
		}	
	}

	public static String getSafeStringRepr(CtElement element) {
		// workaround for
//		at spoon.support.reflect.reference.CtTypeReferenceImpl.getAccessType(CtTypeReferenceImpl.java:774)
//		at spoon.reflect.visitor.ImportAnalyzer$ScannerListener.enter(ImportAnalyzer.java:135)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:124)
//		at spoon.reflect.visitor.CtScanner.scan(CtScanner.java:184)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:106)
//		at spoon.reflect.visitor.CtScanner.visitCtTypeReference(CtScanner.java:813)
//		at spoon.support.reflect.reference.CtTypeReferenceImpl.accept(CtTypeReferenceImpl.java:79)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.doScan(EarlyTerminatingScanner.java:145)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:127)
//		at spoon.reflect.visitor.CtScanner.scan(CtScanner.java:184)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:106)
//		at spoon.reflect.visitor.CtScanner.visitCtTypeAccess(CtScanner.java:825)
//		at spoon.support.reflect.code.CtTypeAccessImpl.accept(CtTypeAccessImpl.java:28)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.doScan(EarlyTerminatingScanner.java:145)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:127)
//		at spoon.reflect.visitor.CtScanner.scan(CtScanner.java:184)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:106)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:83)
//		at spoon.reflect.visitor.CtScanner.visitCtInvocation(CtScanner.java:528)
//		at spoon.support.reflect.code.CtInvocationImpl.accept(CtInvocationImpl.java:46)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.doScan(EarlyTerminatingScanner.java:145)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:127)
//		at spoon.reflect.visitor.CtScanner.scan(CtScanner.java:184)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:106)
//		at spoon.reflect.visitor.CtScanner.visitCtBinaryOperator(CtScanner.java:312)
//		at spoon.support.reflect.code.CtBinaryOperatorImpl.accept(CtBinaryOperatorImpl.java:34)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.doScan(EarlyTerminatingScanner.java:145)
//		at spoon.reflect.visitor.EarlyTerminatingScanner.scan(EarlyTerminatingScanner.java:127)
//		at spoon.reflect.visitor.ImportAnalyzer.process(ImportAnalyzer.java:48)
//		at spoon.reflect.visitor.ForceFullyQualifiedProcessor.process(ForceFullyQualifiedProcessor.java:28)
//		at spoon.reflect.visitor.DefaultJavaPrettyPrinter.applyPreProcessors(DefaultJavaPrettyPrinter.java:2136)
//		at spoon.reflect.visitor.DefaultJavaPrettyPrinter.printElement(DefaultJavaPrettyPrinter.java:281)
//		at spoon.support.reflect.declaration.CtElementImpl.toString(CtElementImpl.java:295)
//		at java.base/java.lang.StringConcatHelper.stringOf(StringConcatHelper.java:453)

		try {
			return element.toString();
		} catch (Exception e) {
			// fake string, please open an issue if this is a problem
			return "FIXME_oefa";
		}
	}

	//icse15dataset: 1367617, 267239, 597123, 1348493, 614068, 306964, 902094, 410960, 1456710, 1458106
	   private void analyzeBinaryOperatorInvolveNull(CtBinaryOperator operatorunderstudy, int operatorindex, Cntx<Object> context) {
			
			boolean whethercontainnull = false; 
					
			CtExpression leftexpression = operatorunderstudy.getLeftHandOperand();
			CtExpression rightexpression = operatorunderstudy.getRightHandOperand();

		   final String leftStr = getSafeStringRepr(leftexpression);
		   final String rightStr = getSafeStringRepr(rightexpression);
			if(leftStr.trim().equals("null") || rightStr.trim().equals("null"))
				whethercontainnull = true;
			
			writeGroupedInfo(context, Integer.toString(operatorindex)+"_"+ getSafeStringRepr(operatorunderstudy), CodeFeatures.O3_CONTAIN_NULL,
					whethercontainnull, "FEATURES_BINARYOPERATOR");
			
		}
	   
	   // ICSE15: 965894, 822931, 699512, 1187333, 1199874, 1163939, 1207508, 933249, 1301541, 785697, 949382, 1081884, 527944
	   // 1547911, 1508451, 1301466, 430825, 723149, 1132421, 1532321, 1151753, 149923, 545454, 306822, 908744, 1097097
	   private void analyzeBinaryOperatorInvolve01 (CtBinaryOperator operatorunderstudy, int operatorindex, Cntx<Object> context) {
			
			boolean whethercontain01 = false; 
					
			CtExpression leftexpression = operatorunderstudy.getLeftHandOperand();
			CtExpression rightexpression = operatorunderstudy.getRightHandOperand();

		   final String leftStr = getSafeStringRepr(leftexpression);
		   final String rightStr = getSafeStringRepr(rightexpression);
		   if(leftStr.trim().equals("0") || leftStr.trim().equals("0.0") ||
					leftStr.trim().equals("1.0") || leftStr.trim().equals("1")
					|| rightStr.trim().equals("0") || rightStr.trim().equals("0.0") ||
					rightStr.trim().equals("1.0") || rightStr.trim().equals("1")
					|| leftStr.trim().endsWith("1") || rightStr.trim().endsWith("1"))
				whethercontain01 = true;
			
			writeGroupedInfo(context, Integer.toString(operatorindex)+"_"+ getSafeStringRepr(operatorunderstudy), CodeFeatures.O3_CONTAIN_01,
					whethercontain01, "FEATURES_BINARYOPERATOR");
		}
	   
	   private void analyzeBinaryOperatorCompareInCondition (CtElement wholeoriginal, CtBinaryOperator operatorunderstudy, int operatorindex, Cntx<Object> context) {
			
			boolean whethercompareincondition = false; 
			
	        if(wholeoriginal instanceof CtIf || wholeoriginal instanceof CtWhile || wholeoriginal instanceof CtFor 
	        	|| wholeoriginal instanceof CtDo || wholeoriginal instanceof CtForEach || wholeoriginal instanceof CtSwitch) {
	        	
	    		BinaryOperatorKind operatorkind = operatorunderstudy.getKind();

	    		if (compareOperator.contains(operatorkind))
	    			whethercompareincondition = true;
	        }
			
	        writeGroupedInfo(context, Integer.toString(operatorindex)+"_"+ getSafeStringRepr(operatorunderstudy), CodeFeatures.O4_COMPARE_IN_CONDITION,
					whethercompareincondition, "FEATURES_BINARYOPERATOR");
		}	
}
