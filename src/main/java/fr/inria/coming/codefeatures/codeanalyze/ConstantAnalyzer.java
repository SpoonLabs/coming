package fr.inria.coming.codefeatures.codeanalyze;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;

public class ConstantAnalyzer extends AbstractCodeAnalyzer {
	
	public static final String CONSTANT_KEY = "CONSTANT";

	public ConstantAnalyzer (CodeElementInfo inputinfo) {
		super(inputinfo);
	}
	
	@Override
	public void analyze() {

		analyzeC1_Constant(elementinfo.element, elementinfo.context, elementinfo.parentClass, elementinfo.varsInScope, 
				elementinfo.varsAffected, elementinfo.literalsFromFaultyLine);
		
	}
	
	private void analyzeC1_Constant(CtElement element, Cntx<Object> context, CtClass parentClass,
			List<CtVariable> varsInScope, List<CtVariableAccess> varsAffected, List<CtLiteral> literalsFromFaultyLine) {
		
		try {

			List<CtLiteral> allConstant = new ArrayList();

			allConstant.addAll(literalsFromFaultyLine);
			
			
			List<CtLiteral> literalsFromClass = new ArrayList();
			if(parentClass!=null)
			  literalsFromClass = parentClass.getElements(e -> (e instanceof CtLiteral)).stream()
					.map(CtLiteral.class::cast).collect(Collectors.toList());
			
			List<CtVariable> constantVarsInScope = varsInScope.stream().filter(e -> isConstantVariable(e))
								.map(CtVariable.class::cast).collect(Collectors.toList());

			if (allConstant.size() > 0) {

				for (CtLiteral literalFormFaulty : allConstant) {
					
					boolean currentLiteralHasSimilarLiteral = false;
					String[] currentLiteralTypeAndValue=getLiteralTypeAndValue(literalFormFaulty);
					for (CtLiteral anotherConstant : literalsFromClass) {
						String[] anotherLiteralTypeAndValue=getLiteralTypeAndValue(anotherConstant);
						if (currentLiteralTypeAndValue[0].equals(anotherLiteralTypeAndValue[0])
								&& !currentLiteralTypeAndValue[1].equals(anotherLiteralTypeAndValue[1])) {
							currentLiteralHasSimilarLiteral = true;
							break;
						}
					}

					writeGroupedInfo(context, adjustIdentifyInJson(literalFormFaulty), CodeFeatures.C1_SAME_TYPE_CONSTANT,
							currentLiteralHasSimilarLiteral, CONSTANT_KEY);
					
					boolean currentLiteralHasSimilarConstantVar = false;
					for (CtVariable anotherConstant : constantVarsInScope) {
						if (compareLiteralAndConstantType(currentLiteralTypeAndValue[0], anotherConstant)) {
							currentLiteralHasSimilarConstantVar = true;
							break;
						}
					}
					
					writeGroupedInfo(context, adjustIdentifyInJson(literalFormFaulty), CodeFeatures.C2_SAME_TYPE_CONSTANT_VAR,
							currentLiteralHasSimilarConstantVar, CONSTANT_KEY);
					
					boolean currentLiteralHasSimilarVar = false;
					for (CtVariable anotherVar : varsInScope) {
						if (compareLiteralAndConstantType(currentLiteralTypeAndValue[0], anotherVar)) {
							currentLiteralHasSimilarVar = true;
							break;
						}
					}
					
					writeGroupedInfo(context, adjustIdentifyInJson(literalFormFaulty), CodeFeatures.C2_SAME_TYPE_VAR,
							currentLiteralHasSimilarVar, CONSTANT_KEY);
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
   
	public static boolean isConstantVariable(CtVariable ctVariable) {
		
		Set<ModifierKind> modifiers = ctVariable.getModifiers();
		if (modifiers.contains(ModifierKind.FINAL)) {
			return true;
		} else {
			String simpleName = ctVariable.getSimpleName();
			if (simpleName.toUpperCase().equals(simpleName)) {
				return true;
			}
		}

		return false;
	}
	   
		public boolean compareLiteralAndConstantType(String literaltype, CtVariable var) {
			
			Boolean typecompatiable=false;
			if(var.getType().toString().toLowerCase().endsWith("string")) {
				if(literaltype.equals("string"))
				    typecompatiable=true; 
			}
			else  {
			    if(var.getType().isPrimitive()) {
				   if(var.getType().toString().toLowerCase().endsWith("char")) {
					   if(literaltype.equals("char"))
						   typecompatiable=true; 
				   }
				   else if(var.getType().toString().toLowerCase().endsWith("boolean")) {
					   if(literaltype.equals("boolean"))
					       typecompatiable=true; 
				   }
				   else {
					   if(literaltype.equals("numerical"))
					       typecompatiable=true; 
				   }
			    }
			}
			return typecompatiable;
		}
}
