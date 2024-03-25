package fr.inria.coming.codefeatures;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.files.FileDiff;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import org.apache.commons.io.IOUtils;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.Patch;
import com.google.gson.JsonObject;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

public class RepairPatternFeatureAnalyzer {
	 static int dupArgsInvocation = 0; //duplicate variables in method invocation
	 static int updIfFalse = 0;  //if false to replace if condition
	 static int insertIfFalse = 0;  //

	 static int ifTrue = 0;  //if true to replace if condition
	 static int condLogicReduce = 0;  //if false to replace if condition
	 static int insertBooleanLiteral = 0; //a varaible is replaced by true or false
	 static int insertNewConstLiteral = 0; // const, false, true
	 static int UpdateLiteral = 0 ;
	 static int removeNullinCond = 0 ; //remove null in if
	 static int	patchedFileNo=0;
	 static int	rmLineNo=0;
	 static int	addLineNo=0;
	 static int	 addThis=0;



	public static JsonObject analyze(IRevision revision, Diff diff, String targetFile) {
		if (!(revision instanceof FileDiff)) {
			throw new IllegalArgumentException("The input should be a DifFolder");
		}
		String folder = revision.getFolder();


		dupArgsInvocation = 0;
		updIfFalse = 0; 
		insertIfFalse = 0;
		ifTrue = 0;  
		condLogicReduce = 0;  
		insertBooleanLiteral = 0; 
		insertNewConstLiteral = 0;
		UpdateLiteral = 0;
		removeNullinCond = 0;
		patchedFileNo=0;
		rmLineNo=0;
		addLineNo=0;
		addThis=0;

		// todo should iterate over all pairs
		Map<String, File> filePaths = new P4JFeatureAnalyzer().fileSrcTgtPaths(revision.getChildren().get(0));
		File src = filePaths.get("src");
		File target = filePaths.get("target");
		System.out.println(src+" "+target);
		
      patchAnalysis((FileDiff) revision);
	  // if(false) in one line conditional replacement
	  ifFalseAnalyzer(folder, diff, target);
	  ifTrueAnalyzer(folder, diff, target);
	  //dupArgsInvocation if duplicated arguments in one method invocation 
	  dupArgsInvocation(folder, diff, target);
	  condLogicReduceAnalyzer(folder, diff, src,target);
	  insertLiteralAnalyzer(folder, diff,src, target);
	  rmNullAnalyzer(folder, diff, src,target);
	  addThisAnalyzer(folder, diff, src,target);


      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("updIfFalse", updIfFalse);
      jsonObject.addProperty("insertIfFalse", insertIfFalse);
      jsonObject.addProperty("ifTrue", ifTrue);
      jsonObject.addProperty("dupArgsInvocation", dupArgsInvocation);
      jsonObject.addProperty("condLogicReduce", condLogicReduce);
      jsonObject.addProperty("insertNewConstLiteral", insertNewConstLiteral);
      jsonObject.addProperty("insertBooleanLiteral", insertBooleanLiteral);
      jsonObject.addProperty("UpdateLiteral", UpdateLiteral);
      jsonObject.addProperty("removeNullinCond", removeNullinCond);
      jsonObject.addProperty("patchedFileNo", patchedFileNo);
      jsonObject.addProperty("rmLineNo", rmLineNo);
      jsonObject.addProperty("addLineNo", addLineNo);
      jsonObject.addProperty("addThis", addThis);

	  return jsonObject;

	}

	private static void addThisAnalyzer(String folderPath, Diff diff, File src, File target) {
		List<Operation> operations = diff.getRootOperations();
		for (Operation opt : operations) {
			if (opt.toString().contains("Insert") ) {
				String newvar = opt.toString().split(":")[1].split("\n\t")[1];
				if(newvar.contains("this")) {
					addThis = 1;
				}
			}
		}
		
	}

	private static void patchAnalysis(FileDiff folderPath) {
		Map<String, File> pathmap = new HashMap();
		File pairFolder = new File(folderPath.getFolder());

		for (IRevisionPair o : folderPath.getChildren()) {

			if (".DS_Store".equals(o.getPreviousName())) {
				continue;
			}
				patchedFileNo ++;
				
				File src = (File) o.getPreviousVersion();
				File target = (File) o.getNextVersion();
				try {
				List<String> original = IOUtils.readLines(new FileInputStream(src), "UTF-8");
		        List<String> revised = IOUtils.readLines(new FileInputStream(target), "UTF-8");
		        Patch<String> diff = DiffUtils.diff(original, revised);
		        List<AbstractDelta<String>> deltas = diff.getDeltas();
		        deltas.forEach(delta -> {
		            switch (delta.getType()) {
		                case INSERT:
		                    Chunk<String> insert = delta.getTarget();
		                    addLineNo += insert.size();
		                    break;
		                case CHANGE:
		                    Chunk<String> source = delta.getSource();
		                    Chunk<String> target1 = delta.getTarget();
		                    rmLineNo += source.size();
		                    addLineNo += target1.size();
		                    break;
		                case DELETE:
		                    Chunk<String> delete = delta.getSource();
		                    rmLineNo += delete.size();
		                    break;
		            }

		        });
		        
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				
			}
				

	}
		


	private static void rmNullAnalyzer(String folderPath, Diff diff, File source, File target) {
		List<Operation> operations = diff.getAllOperations();
		for (Operation opt : operations) {
			if (opt.toString().contains("Delete BinaryOperator") ) {
				String deleted = opt.toString().split(":")[1].split("\n\t")[1];
				String specificNo = opt.toString().split(":")[1].split("\n\t")[0];
				String sourceline = getSpecificLine(source.getPath(), Integer.parseInt(specificNo) - 1);		
				String targetline = getSpecificLine(target.getPath(), Integer.parseInt(specificNo) - 1);
				if (deleted.contains("null") && sourceline.contains("if") && targetline.contains("if")) {
					removeNullinCond = 1;
				}
			}
		}
		
		
	}

	private static void insertLiteralAnalyzer(String folderPath, Diff diff, File source, File target) {
		// TODO Auto-generated method stub
		List<Operation> operations = diff.getAllOperations();
		for (Operation opt : operations) {
			if (opt.toString().contains("Insert Literal") ) {
				String specificNo = opt.toString().split(":")[1].split("\n\t")[0];
				String newLiteral = opt.toString().split(":")[1].split("\n\t")[1];
				if (!newLiteral.contains("true") && !newLiteral.contains("false")) {
					newLiteral = newLiteral.trim().replace("\n", "");
					
					try {
				        Double.parseDouble(newLiteral);
				    } catch (NumberFormatException nfe) {
				        return;
				    }
					
					for(int i = -2; i<3;i++) {
						String sourceline = getSpecificLine(source.getPath(), Integer.parseInt(specificNo) +i);	
						if (sourceline.contains(newLiteral)) {
							return;
						}
					}
					insertNewConstLiteral = 1;
					
					
					break;
				} else {
					String targetline = getSpecificLine(target.getPath(), Integer.parseInt(specificNo) - 1);
					targetline=targetline.replace(" ", "");
					if (targetline.contains("==true") && targetline.contains("==false")) {
						insertBooleanLiteral = 1;
					}
				}
			}	else if 	(opt.toString().contains("Update Literal") ) {
				UpdateLiteral = 1;
			}
		}
	}


	private static void condLogicReduceAnalyzer(String folderPath, Diff diff, File source,  File target) {
		List<Operation> operations = diff.getRootOperations();
		for (Operation opt : operations) {
			if (opt.toString().contains("Delete BinaryOperator") ) {
				String specificNo = opt.toString().split(":")[1].split("\n\t")[0];
				String deletedItem = opt.toString().split("\n\t")[1];
				String sourceline = getSpecificLine(source.getPath(), Integer.parseInt(specificNo) - 1);		
				String targetline = getSpecificLine(target.getPath(), Integer.parseInt(specificNo) - 1);
				if (sourceline.contains("if") && (deletedItem.contains("&") || deletedItem.contains("||"))) {
					if(targetline.contains("if")) {
						condLogicReduce = 1;
					}
				}

			}
		}
	}



	private static void dupArgsInvocation(String folderPath, Diff diff, File target) {
		List<Operation> operations = diff.getRootOperations();
		for (Operation opt : operations) {
			if (opt.toString().contains("Update VariableRead") || opt.toString().contains("Update BinaryOperator") ) {
				String specificNo = opt.toString().split(":")[1].split("\n\t")[0];
				String oldVar = opt.toString().split(":")[1].split("\n\t")[1].split("to")[0];
				String newVar = opt.toString().split(":")[1].split("\n\t")[1].split("to")[1];
				newVar = newVar.replace("\n", "").trim();
				String targetline = getSpecificLine(target.getPath(), Integer.parseInt(specificNo) - 1);			    
				targetline = targetline.trim();
				if(!targetline.startsWith("/")) {
				int count = (targetline.split(newVar,-1).length ) - 1;
				if (count >= 2) {
					if (!targetline.contains("?") && !targetline.contains(":")) {
						dupArgsInvocation = 1;
					}
				}				
			}		
			}
		}		
	}


	private static void ifFalseAnalyzer(String folderPath, Diff diff, File target) {
		List<Operation> operations = diff.getRootOperations();
		try {
		for (Operation opt : operations) {
			if (opt.getAction().getName().contains("INS")) {
				String specificNo = opt.toString().split(":")[1].split("\n\t")[0];
				String targetline = getSpecificLine(target.getPath(), Integer.parseInt(specificNo) - 1);
				targetline = targetline.replace(" ", "");
				if (opt.toString().contains("Insert If")) {
					if ((targetline).contains("if(false)")) {
						insertIfFalse = 1;
					}
					if(diff.toString().contains("if (false)")) {
						insertIfFalse = 1;
					}
				}else {				
				if ((targetline).contains("if(false)")) {
					updIfFalse = 1;
				}
				if(diff.toString().contains("if (false)")) {
					updIfFalse = 1;
				}
			}		
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	private static void ifTrueAnalyzer(String folderPath, Diff diff, File target) {
		List<Operation> operations = diff.getRootOperations();
		try {
		for (Operation opt : operations) {
			if (opt.getAction().getName().contains("INS")) {
				String specificNo = opt.toString().split(":")[1].split("\n\t")[0];
				String targetline = getSpecificLine(target.getPath(), Integer.parseInt(specificNo) - 1);
				targetline = targetline.replace(" ", "");
				if ((targetline).contains("if(true)")) {
					ifTrue = 1;
				}
				if(diff.toString().contains("if (true)")) {
					ifTrue = 1;
				}
			}						
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	}
		
	
	

	public static String getSpecificLine(String filePath, int lineNo) {
		String targetline = "";
		try {
			targetline = Files.readAllLines(Paths.get(filePath)).get(lineNo);
		} catch (IOException e) {
			e.printStackTrace();
			return targetline;
		}
		return targetline;
	}
}
