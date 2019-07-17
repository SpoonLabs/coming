package fr.inria.coming.codefeatures.codeanalyze;

import java.util.ArrayList;
import java.util.List;

import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

public class TypeaccessAnalyzer extends AbstractCodeAnalyzer {
	
	public TypeaccessAnalyzer (CodeElementInfo inputinfo) {
		super(inputinfo);
	}
	
	@Override
	public void analyze() {

		analyzeC3C4_SimilarTypeAccessActualVar(elementinfo.element, elementinfo.context, elementinfo.typeaccess, 
				elementinfo.parentClass);

	}
	
	private void analyzeC3C4_SimilarTypeAccessActualVar(CtElement element, Cntx<Object> context, 
			List<CtTypeAccess> typeaccessaaffected, CtClass parentClass) {
		
		try {
			List<CtTypeAccess> typeaccesss = new ArrayList();
			if(parentClass!=null)
			    typeaccesss = parentClass.getElements(new TypeFilter<>(CtTypeAccess.class));

			for (CtTypeAccess virtualtypeaccess : typeaccessaaffected) {
				
				boolean c3CurrentOtherTypeAccessActualVar = false;
				boolean c4CurrentOtherSimilarTypeAccessActualVar = false;
				
				if(isTypeAccessActualVar(virtualtypeaccess)) {
					c3CurrentOtherTypeAccessActualVar=true;
					
					for(CtTypeAccess certaintypeaccess: typeaccesss) {
						if(isTypeAccessActualVar(certaintypeaccess)) {
							if(whetherSimilarTypeAccessActualVar(virtualtypeaccess, certaintypeaccess)) {
								c4CurrentOtherSimilarTypeAccessActualVar=true;
								break;
							}
						}
					}
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(virtualtypeaccess), CodeFeatures.C3_TYPEACCESS_ACTUAL_VAR,
						c3CurrentOtherTypeAccessActualVar, "FEATURES_TYPEACCESS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(virtualtypeaccess), CodeFeatures.C4_SIMILAR_TYPEACCESS_ACTUAL_VAR,
						c4CurrentOtherSimilarTypeAccessActualVar, "FEATURES_TYPEACCESS");
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}	
	}

	public static boolean isTypeAccessActualVar(CtElement element) {
		
		if(element instanceof CtTypeAccess) {
			CtTypeAccess typeaccess= (CtTypeAccess) element;
			String fullname=typeaccess.getAccessedType().getQualifiedName();
			String[] splitname=fullname.split("\\.");
			if (splitname.length>1) {
				String simplename=splitname[splitname.length-1];
				if (simplename.toUpperCase().equals(simplename)) 
					return true;
			}		
		}
		return false;
	}
	
	private boolean whetherSimilarTypeAccessActualVar(CtTypeAccess access1, CtTypeAccess access2) {
		String name1=access1.getAccessedType().getQualifiedName();
		String name2=access2.getAccessedType().getQualifiedName();
		String[] splited1=name1.split("\\.");
		String[] splited2=name2.split("\\.");
        if(splited1.length>1 && splited2.length>1) {
        	if(splited1[splited1.length-2].equals(splited2[splited2.length-2])
        			&& !splited1[splited1.length-1].equals(splited2[splited2.length-1]))
        		return true;
        }
		
        return false;
	}
}
