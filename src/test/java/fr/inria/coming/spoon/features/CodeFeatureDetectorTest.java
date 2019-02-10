package fr.inria.coming.spoon.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeFeatureDetector;
import fr.inria.coming.codefeatures.CodeFeatures;
import spoon.SpoonModelBuilder;
import spoon.compiler.SpoonResource;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.VirtualFile;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

/**
 * 
 * @author Matias Martinez
 *
 */
public class CodeFeatureDetectorTest {

	@Test
	public void testPropertyBinop() {

		String content = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1;" + " a = a + b / c +d  ; " //
				+ " return (a == b && b == c)? null: null;" + "}};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement stassig = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("a ="))
				.findFirst().get();
		System.out.println(stassig);
		assertTrue(stassig instanceof CtAssignment);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();

		Cntx cntx = cntxResolver.analyzeFeatures(stassig);

		Cntx binop = (Cntx) cntx.get(CodeFeatures.BIN_PROPERTIES);

		assertEquals(Boolean.TRUE, binop.get(CodeFeatures.involve_PLUS_relation_operators));
		assertEquals(Boolean.FALSE, binop.get(CodeFeatures.involve_MINUS_relation_operators));
		assertEquals(Boolean.TRUE, binop.get(CodeFeatures.involve_DIV_relation_operators));
		assertEquals(Boolean.FALSE, binop.get(CodeFeatures.involve_MUL_relation_operators));

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY));

		List<String> ops = (List<String>) binop.get(CodeFeatures.involved_relation_bin_operators);
		assertTrue(ops.contains(BinaryOperatorKind.PLUS.toString()));
		assertFalse(ops.contains(BinaryOperatorKind.MINUS.toString()));

		CtElement returnStmt = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(returnStmt);
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY));

	}

	@Test
	public void testPropertyUnaryOp() {

		String content = "" + "class X {" + "public Object foo() {" + " Integer.toString(10);"
				+ " int a = 1,b = 1,c = 1,d = 1;" + " a = a + b / c +d  ; if (!(a>0)){a++;} " + " return null;" + "}};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement stassig = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("if"))
				.findFirst().get();
		System.out.println(stassig);
		assertTrue(stassig instanceof CtIf);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();

		Cntx cntx = cntxResolver.analyzeFeatures(stassig);

		Cntx unopctxt = (Cntx) cntx.get(CodeFeatures.UNARY_PROPERTIES);

		List<String> ops = (List<String>) unopctxt.get(CodeFeatures.involved_relation_unary_operators);

		assertTrue(ops.contains(UnaryOperatorKind.NOT.toString()));
		assertFalse(ops.contains(UnaryOperatorKind.POSTDEC.toString()));

		assertEquals(Boolean.TRUE, unopctxt.get(CodeFeatures.involve_NOT_relation_operators));
		// assertEquals(Boolean.FALSE,
		// unopctxt.get(CNTX_Property.involve_INSTANCEOF_relation_operators));
		assertEquals(Boolean.FALSE, unopctxt.get(CodeFeatures.involve_POSTINC_relation_operators));

		CtElement postin = ((CtIf) stassig).getThenStatement();
		Cntx cntxposting = cntxResolver.analyzeFeatures((CtElement) ((CtBlock) postin).getStatement(0));
		Cntx unopctxtposting = (Cntx) cntxposting.get(CodeFeatures.UNARY_PROPERTIES);

		assertEquals(Boolean.FALSE, unopctxtposting.get(CodeFeatures.involve_NOT_relation_operators));
		// assertEquals(Boolean.FALSE,
		// cntxposting.get(CNTX_Property.involve_INSTANCEOF_relation_operators));
		assertEquals(Boolean.TRUE, unopctxtposting.get(CodeFeatures.involve_POSTINC_relation_operators));

	}

	@Test
	public void testProperty_V6_IS_METHOD_RETURN_TYPE_VAR() {

		String content = "" + "class X {" + "public Object foo() {" //
				+ " int a = 1;"//
				+ "int b = a;" + "float f = 0;" + "" + "return f;" + "}" //
				+ "public float getFloat(){return 1.0;}"//
				+ "public double getConvertFloat(int i){return 0.0;}"//
				+ "public double getConvert2Float(int i){String s2;Integer.valueOf(s2);return 0.0;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement stassig = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return f"))
				.findFirst().get();
		System.out.println(stassig);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(stassig);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.V6_IS_METHOD_RETURN_TYPE_VAR));
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN));

		stassig = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float f")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(stassig);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V6_IS_METHOD_RETURN_TYPE_VAR));
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN));

		///
		stassig = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int b")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(stassig);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.V6_IS_METHOD_RETURN_TYPE_VAR));
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN));

	}

	@Test
	public void testProperty_LE2_a() {

		String content = "" + "class X {" + //
				"public Object foo() {" //
				+ " int a = 1;"//
				+ "String s = null;" + "int b = (s == null)?2:1;" //
				+ "float f = 0; "//
				+ "double d = 0;" //
				+ "float f1 = f;" //
				+ "return f > 0;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "public float getSameFloat(float f){int i = 0;String name=null;Boolean.getBoolean(name);return 1;}"//
				+ "public boolean getBoolConvertFloat(float f){return false;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return f"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR));

		///
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float f")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR));
///
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("double d")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR));
///
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int b")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR));

		///
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float f1")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR));

		System.out.println(cntx.toJSON());

	}

	@Test
	@Ignore
	public void testProperty_V1_IS_METHOD_PARAM_TYPE_VAR() {

		String content = "" + "class X {" + //
				"public Object foo() {" //
				+ " int a = 1;"//
				+ "int b = a;" //
				+ "float f = 0; "//
				+ "double d = 0;" //
				+ "String s1 = null;" //
				+ "String s2 = s1;" //
				+ "return f;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "public float getSameFloat(float f){String s3 = null;String.format(s3, null);return 1;}"//
				+ "public boolean getBoolConvertFloat(float f){return false;}"//
				+ "};";

		CtType type = getCtType(content);
		// String.format(format, args);
		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return f"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN));
		assertEquals(Boolean.TRUE,
				retrieveFeatureVarProperty(cntx, CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN, "f"));

		///
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float f")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("double d")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN));

		//
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int b")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(element);

		// int matches with Object.wait(int, int)
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN));
		assertEquals(Boolean.FALSE,
				retrieveFeatureVarProperty(cntx, CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN, "a"));

		//
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("java.lang.String s2"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);

		// int matches with Object.wait(int, int)
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN));
		assertEquals(Boolean.TRUE,
				retrieveFeatureVarProperty(cntx, CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN, "s1"));

		System.out.println(cntx.toJSON());

	}

	@Test
	public void testProperty_V8_NUMBER_PRIMITIVE_VARS_IN_STMT() {

		String content = "" + "class X {" + "public Object foo() {" //
				+ " String s=null;"//
				+ " int a = 1;"//
				+ "int b = a;" + "b = b+a;" + "s.toString();" //
				+ "String d=s;" + "return d.equals(s) || a>b ;" + "}};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement stassig = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int a"))
				.findFirst().get();
		System.out.println(stassig);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(stassig);

		assertEquals(0, cntx.get(CodeFeatures.NUMBER_PRIMITIVE_VARS_IN_STMT));
		assertEquals(0, cntx.get(CodeFeatures.NUMBER_OBJECT_REFERENCE_VARS_IN_STMT));
		assertEquals(0, cntx.get(CodeFeatures.NUMBER_TOTAL_VARS_IN_STMT));

		//
		CtElement stm = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int b"))
				.findFirst().get();

		cntx = cntxResolver.analyzeFeatures(stm);

		assertEquals(1, cntx.get(CodeFeatures.NUMBER_PRIMITIVE_VARS_IN_STMT));
		assertEquals(0, cntx.get(CodeFeatures.NUMBER_OBJECT_REFERENCE_VARS_IN_STMT));
		assertEquals(1, cntx.get(CodeFeatures.NUMBER_TOTAL_VARS_IN_STMT));
		assertEquals(Boolean.TRUE, retrieveFeatureVarProperty(cntx, CodeFeatures.V8_VAR_PRIMITIVE, "a"));

		stm = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("b =")).findFirst().get();

		cntx = cntxResolver.analyzeFeatures(stm);

		assertEquals(2, cntx.get(CodeFeatures.NUMBER_PRIMITIVE_VARS_IN_STMT));
		assertEquals(0, cntx.get(CodeFeatures.NUMBER_OBJECT_REFERENCE_VARS_IN_STMT));
		assertEquals(2, cntx.get(CodeFeatures.NUMBER_TOTAL_VARS_IN_STMT));

		stm = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("s.to")).findFirst().get();

		cntx = cntxResolver.analyzeFeatures(stm);

		assertEquals(0, cntx.get(CodeFeatures.NUMBER_PRIMITIVE_VARS_IN_STMT));
		assertEquals(1, cntx.get(CodeFeatures.NUMBER_OBJECT_REFERENCE_VARS_IN_STMT));
		assertEquals(1, cntx.get(CodeFeatures.NUMBER_TOTAL_VARS_IN_STMT));

		stm = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return ")).findFirst()
				.get();

		cntx = cntxResolver.analyzeFeatures(stm);

		assertEquals(2, cntx.get(CodeFeatures.NUMBER_PRIMITIVE_VARS_IN_STMT));
		assertEquals(2, cntx.get(CodeFeatures.NUMBER_OBJECT_REFERENCE_VARS_IN_STMT));
		assertEquals(4, cntx.get(CodeFeatures.NUMBER_TOTAL_VARS_IN_STMT));

	}

	@Test
	public void testProperty_V2_HAS_VAR_SIM_NAME() {

		String content = "" + "class X {" + " int ffii = 1;"//
				+ "public Object foo() {" //
				+ " int mysimilar = 1;"//
				+ "int myzimilar = 2;"//
				+ "float fiii = (float)myzimilar;"//
				+ " double dother = 0;" //
				+ "return fiii;" + "}" + "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float fiii"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// affected myzimilar
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.HAS_VAR_SIM_NAME));
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE));
		assertEquals(Boolean.TRUE,
				retrieveFeatureVarProperty(cntx, CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE, "myzimilar"));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("double dother"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.HAS_VAR_SIM_NAME));
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return f")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.HAS_VAR_SIM_NAME));
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE));
		assertEquals(Boolean.FALSE,
				retrieveFeatureVarProperty(cntx, CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE, "fiii"));
		System.out.println(cntx.toJSON());

	}

	@Test
	public void testProperty_V4() {

		String content = "" + "class X {" + " int ffii = 1;"//
				+ "public Object foo() {" //
				+ " int mysimilar = 1;"//
				+ "int myzimilar = 2;"//
				+ "float fiii = (float)myzimilar;"//
				+ "int dother = max(mysimilar, myzimilar);" //
				+ "return max(mysimilar, mysimilar);" + "}" + "public float getFloat(){return 1.0;}"//
				+ "public int max(int m, int n){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int dother"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// affected myzimilar
		System.out.println(cntx.toJSON());
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V4B_USED_MULTIPLE_AS_PARAMETER));
		assertEquals(Boolean.FALSE,
				retrieveFeatureVarProperty(cntx, CodeFeatures.V4B_USED_MULTIPLE_AS_PARAMETER, "mysimilar"));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return max"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.V4B_USED_MULTIPLE_AS_PARAMETER));
		assertEquals(Boolean.TRUE,
				retrieveFeatureVarProperty(cntx, CodeFeatures.V4B_USED_MULTIPLE_AS_PARAMETER, "mysimilar"));
		System.out.println(cntx.toJSON());

		assertEquals(Boolean.TRUE,
				retrieveFeatureVarProperty(cntx, CodeFeatures.V4_FIRST_TIME_USED_AS_PARAMETER, "mysimilar"));
		System.out.println(cntx.toJSON());
		assertEquals(Boolean.FALSE,
				retrieveFeatureVarProperty(cntx, CodeFeatures.V4_FIRST_TIME_USED_AS_PARAMETER, "mysimilar_2"));
		System.out.println(cntx.toJSON());
	}

	@Test
	public void testProperty_C1() {

		String content = "" + "class X {" + " int ffii = 1;"//
				+ "public Object foo() {" //
				+ " int mysimilar = 1;"//
				+ "int myzimilar = 1;"//
				+ "String test = \"mycontant\";"//
				+ "String t2est2 = test + \"mycontant2\";" + "float fiii = (float)myzimilar;"//
				+ "int dother = max(mysimilar, myzimilar);" //
				+ "return max(mysimilar, mysimilar);" + "}" + "public float getFloat(){return 1.0;}"//
				+ "public int max(int m, int n){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("java.lang.String test")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// affected myzimilar
		System.out.println(cntx.toJSON());
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.C1_SAME_TYPE_CONSTANT));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int myzimilar "))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.C1_SAME_TYPE_CONSTANT));

	}

	@Test
	public void testProperty_C1_case2() {

		String content = "" + "class X {" //
				+ " final float DDCONSTANT = 1.0f; " //

				+ "public Object foo() {" //
				+ " int ffii = 1;"//
				+ "float fiii = 10.0f;"//
				+ "return null;};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float fiii"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// affected myzimilar
		System.out.println(cntx.toJSON());
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.C1_SAME_TYPE_CONSTANT));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int ffii "))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.C1_SAME_TYPE_CONSTANT));

	}

	@Test
	public void testProperty_V3() {

		String content = "" + "class X {" + //
				"public String SC = null;"//
				+ "public Object foo() {" //
				+ " int mysimilar = 1;"//
				+ "int myzimilar = 2;"//
				+ "float fiii = (float)myzimilar; " //
				+ "String s1 = SC;"//
				+ "String s2 = s1;"//
				+ "double dother = 0;" + "return fiii;" + "}" + "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("java.lang.String s1")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.V3_HAS_CONSTANT));
		assertEquals(Boolean.TRUE, retrieveFeatureVarProperty(cntx, CodeFeatures.V3_HAS_CONSTANT, "SC"));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("java.lang.String s2"))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.V3_HAS_CONSTANT));

	}

	@Test
	@Ignore
	public void testProperty_AE1() {

		String content = "" + "class X {" + //
				"public String SC = null;"//
				+ "public Object foo() {" //
				+ " int mysimilar = 1;"//
				+ "int myzimilar = 2 + 1;"//
				+ "float fiii = (float)myzimilar - 1.0f; " //
				+ "String s1 = SC;"//
				+ "String s2 = s1 + SC;"//
				+ "double dother = 0;" + "return fiii;" + "}" //
				+ "public float getFloat(){return 1.0;}"//
				+ "public float getConvertFloat(float f){return 1;}"//
				+ "public String getConvertSFloat(String f){return null;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("java.lang.String s2")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.AE1_COMPATIBLE_RETURN_TYPE));

		///
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float fiii"))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.AE1_COMPATIBLE_RETURN_TYPE));

		///
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int myzimilar"))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.AE1_COMPATIBLE_RETURN_TYPE));

	}

	public Object retrieveFeatureVarProperty(Cntx cntx, CodeFeatures property, String varName) {
		try {
			return ((Cntx) ((Cntx) cntx.getInformation().get("FEATURES_VARS")).getInformation().get(varName))
					.getInformation().get(property.toString());
		} catch (Exception e) {
			return null;
		}
	}

	public Object retrieveMethodsVarProperty(Cntx cntx, CodeFeatures property, String varName) {
		try {
			return ((Cntx) ((Cntx) cntx.getInformation().get("FEATURES_METHODS")).getInformation().get(varName))
					.getInformation().get(property.toString());
		} catch (Exception e) {
			return null;
		}
	}

	@Test
	public void testProperty_HAS_VAR_SIM_TYPE() {

		String content = "" + "class X {" + "public Object foo() {" //
				+ " int mysimilar = 1;"//
				+ "int myzimilar = 2;"//
				+ "float fiii = (float)myzimilar; " + "double dother = 0;" + "return fiii;" + "}"
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float fiii"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.HAS_VAR_SIM_TYPE));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.HAS_VAR_SIM_TYPE));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("double")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.HAS_VAR_SIM_TYPE));

	}

	@Test
	public void testProperty_LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED() {

		String content = "" + "class X {" + "public Object foo() {" //
				+ " int mysimilar = 1;"//
				+ "int myzimilar = 2;"//
				+ "float fiii = (float)myzimilar; "//
				+ "double dother = 0;" //
				+ "int f1 =  mysimilar + 1;" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "return 1;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int f1"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int f2")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED));

	}

	@Test
	public void testProperty_LE4_EXISTS_LOCAL_UNUSED_VARIABLES() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				//
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii = (float)myzimilar; "//
				+ "double dother = 0;" //
				+ "int f1 =  mysimilar + 1;" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("int myzimilar")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// all variables used
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.LE4_EXISTS_LOCAL_UNUSED_VARIABLES));

		// a local not used
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("if (avarb"))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE4_EXISTS_LOCAL_UNUSED_VARIABLES));

		// without the global
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE4_EXISTS_LOCAL_UNUSED_VARIABLES));

	}

	@Test
	public void testProperty_LE6_HAS_NEGATION() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii = (float)myzimilar; "//
				+ "double dother = 0;" //
				+ "int f1 =  mysimilar + 1;" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && !gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("if (avarb"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// all variables used
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE6_HAS_NEGATION));

		// without the global
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.LE6_HAS_NEGATION));

	}

	@Test
	public void testProperty_S2_if_condition() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" + "public boolean ddd =false;"//
				+ "public Object foo() {" //
				+ "int f1 =  mysimilar + 1;" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(isGuard()){f2 = f2;};" //
				+ "return f1;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public boolean isGuard(){return false;}"//
				+ "public int getConvertFloat(float f){return gvarb?1:0;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();

		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return"))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_GUARD));
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_GUARD));
	}

	@Test
	public void testProperty_S2_conditional_1line() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" + "public boolean ddd =false;"//
				+ "public Object foo() {" //
				+ "int f1 =  mysimilar + 1;" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "f2 = (isGuard())? f2: 1;" //
				+ "return f1;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public boolean isGuard(){return false;}"//
				+ "public int getConvertFloat(float f){return gvarb?1:0;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();

		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return"))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_GUARD));
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_GUARD));
	}

	@Test
	public void testProperty_S5() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" + "public boolean ddd =false;"//
				+ "public Object foo() {" //
				+ "X f1 =  new X();" //
				+ "X f2 =  new X();" //
				+ "if(isGuard()){f2 = f2;};" //
				+ "return f1;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public boolean isGuard(){return false;}"//
				+ "public int getConvertFloat(float f){return gvarb?1:0;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();

		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return"))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_GUARD));
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_GUARD));

	}

	@Test
	public void testProperty_S4_field() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" + "public boolean ddd =false;"//
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use the field
				+ "float fiii = (float)myzimilar; "//
				+ "double dother = 0;" //
				+ "int f1 =  mysimilar + 1;" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(ddd){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return gvarb?1:0;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("int myzimilar")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// all variables used
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.S4_USED_FIELD));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.S4_USED_FIELD));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("if")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.S4_USED_FIELD));

	}

	@Test
	public void testProperty_LE5() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii = (float)myzimilar; "//
				+ "double dother = 0;" //
				+ "int f1 =  mysimilar + 1;" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && !gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("int myzimilar")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// all variables used
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int f1")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY));

	}

	@Test
	public void testProperty_L7() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii =  getFloat(); "//
				+ "double dother = 0;" //
				+ "int f1 =  getConvertFloat(fiii);" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(getB(bvarb) && f1 > 0){};" //
				+ "if(avarb && f1> 0){};" //
				+ "if(!avarb && f1> 0){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getMFloat(){return 1.0;}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int fint(int i){return 1.0;}"//
				+ "public int getB(boolean i){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("if ((get"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// not method involve
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.LE7_SIMPLE_VAR_IN_LOGIC));

		//
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("if (avarb"))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);
		// statement with a similar method
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE7_SIMPLE_VAR_IN_LOGIC));

		//
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("if ((!avarb"))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);
		// statement with a similar method
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.LE7_SIMPLE_VAR_IN_LOGIC));
	}

	@Test
	public void testProperty_M3_relatedmethod() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii =  getFloat(); "//
				+ "double dother = 0;" //
				+ "int f1 =  getConvertFloat(fiii);" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getMFloat(float m){return 1.0;}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int fint(int i){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int f1"))
				.findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// not method involve
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP));
		assertEquals(Boolean.TRUE, retrieveMethodsVarProperty(cntx,
				CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP, "getConvertFloat(float)"));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float fiii ="))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);
		// statement with a similar method
		System.out.println(cntx.toJSON());
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP));
		assertEquals(Boolean.TRUE, retrieveMethodsVarProperty(cntx,
				CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP, "getFloat()"));

	}

	@Test
	public void testProperty_M3_invocation() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii =  getFloat(); "//
				+ "double dother = 0;" //
				+ "String f1 =  getFloat(1);" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getMFloat(float m){return 1.0;}"//
				+ "public String getFloat(int){return 1.0;}"//
				+ "public int fint(int i){return 1.0;}"//
				+ "public int getConvertFloat(float f){" + "String.valueOf(1);" + "return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("java.lang.String f1")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// not method involve
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP));
		// assertEquals(Boolean.FALSE, retrieveMethodsVarProperty(cntx,
		// CNTX_Property.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP,
		// "getFloat(float)"));

	}

	@Test
	public void testProperty_M4_invocation() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii =  getFloat(1.0f); "//
				+ "double dother = 0;" //
				+ "int f1 =  getConvertFloat(fiii);" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getFloat(Double d){return 1.0;}"//
				+ "public float getFloat(float f){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("float fiii =")).findFirst().get();
		System.out.println(element);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		System.out.println(cntx.toJSON());
		// not method involve
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY));
		retrieveMethodsVarProperty(cntx, CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY, "getFloat(float)");

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int f1")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);
		// all variables used
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.M1_OVERLOADED_METHOD));
	}

	@Test
	public void testProperty_M5_invocation_comp_var() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii =  getFloat(1.0f); "//
				+ "double dother = 0;" //
				+ "int f1 =  getConvertFloat(fiii);" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getFloat(Double d){return 1.0;}"//
				+ "public float getFloat(float f){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";
		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("float fiii =")).findFirst().get();
		System.out.println(element);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		System.out.println(cntx.toJSON());
		// not method involve
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.M5_MI_WITH_COMPATIBLE_VAR_TYPE));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int f1")).findFirst()
				.get();

		cntx = cntxResolver.analyzeFeatures(element);
		System.out.println(cntx.toJSON());
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.M5_MI_WITH_COMPATIBLE_VAR_TYPE));

	}

	@Test
	public void testProperty_M6_returnprimitive() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii =  getFloat(1.0f); "//
				+ "double dother = 0;" //
				+ "int f1 =  getConvertFloat(fiii);" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getFloat(Double d){return 1.0;}"//
				+ "public float getFloat(float f){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("float fiii =")).findFirst().get();
		System.out.println(element);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		System.out.println(cntx.toJSON());
		// not method involve
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.M6_RETURN_PRIMITIVE));

		method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("getConvertFloat")).findFirst().get();

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return 1")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);
		// all variables used
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.M6_RETURN_PRIMITIVE));
	}

	@Test
	public void testProperty_M1_OVERLOADED() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii =  getFloat(); "//
				+ "double dother = 0;" //
				+ "int f1 =  getConvertFloat(fiii);" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getFloat(Double d){return 1.0;}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("float fiii =")).findFirst().get();
		System.out.println(element);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		System.out.println(cntx.toJSON());
		// not method involve
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.M1_OVERLOADED_METHOD));
		retrieveMethodsVarProperty(cntx, CodeFeatures.M1_OVERLOADED_METHOD, "getFloat()");

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int f1")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);
		// all variables used
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.M1_OVERLOADED_METHOD));

	}

	@Test
	public void testProperty_M2_SIMILAR_METHOD_WITH_SAME_RETURN() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() {" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii =  getFloat(); "//
				+ "double dother = 0;" //
				+ "int f1 =  getConvertFloat(fiii);" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getMFloat(){return 1.0;}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("int myzimilar")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// not method involve
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN));

		/// SECOND CASE
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float fiii ="))
				.findFirst().get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);
		// statement with a similar method
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN));

		assertEquals(Boolean.TRUE,
				retrieveMethodsVarProperty(cntx, CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, "getFloat()"));
		assertEquals(Boolean.FALSE, retrieveMethodsVarProperty(cntx, CodeFeatures.M1_OVERLOADED_METHOD, "getFloat()"));

		// THIRD CASE:
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int f1")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);
		// all variables used
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN));

	}

	@Test
	public void testProperty_S6_Method_thr_exception() {

		String content = "" + "class X {" //
				+ "public boolean gvarb =false;" //
				+ "public Object foo() throws Exception{" //
				+ "boolean avarb =false;" //
				+ "boolean bvarb =false;" //
				+ "int mysimilar = 1;"//
				+ "int myzimilar = (gvarb && avarb && bvarb)? 2:1;"// Use of two booleans
				+ "float fiii =  getFloat(); "//
				+ "double dother = 0;" //
				+ "int f1 =  getConvertFloat(fiii);" //
				+ "int f2 =  mysimilar + myzimilar + f1 ;" //
				+ "if(avarb && gvarb){};" //
				+ "return (avarb && bvarb)? 2: 1;" + "}"//
				+ "public float getMFloat(){return 1.0;}"//
				+ "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("int myzimilar")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// not method involve
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.S6_METHOD_THROWS_EXCEPTION));

		method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("getConvertFloat")).findFirst().get();

		/// SECOND CASE
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return 1")).findFirst()
				.get();
		System.out.println(element);
		cntxResolver = new CodeFeatureDetector();
		cntx = cntxResolver.analyzeFeatures(element);
		// statement with a similar method
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.S6_METHOD_THROWS_EXCEPTION));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testProperty_HAS_VAR_IN_TRANSFORMATION() {

		String content = "" + "class X {" + "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "int myzimilar = 2;"
				+ "float fiii =  getConvertFloat(mysimilar); double dother = 0; double ddother = dother;"
				+ "return mysimilar;" + "}" + "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("return mysimilar")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.V5_HAS_VAR_IN_TRANSFORMATION));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("double ddother"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.HAS_VAR_SIM_NAME));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testProperty_USES_CONSTANT() {

		String content = "" + "class X {" + "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "int myzimilar = 2;" + "float fiii =  getConvertFloat(mysimilar);"//
				+ " double dother = 0;"//
				+ " double ddother = dother;"//
				+ "return mysimilar;" + "}" + //
				"public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(float f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);
		CtElement element = method.getBody().getStatements().stream()
				.filter(e -> e.toString().startsWith("int myzimilar")).findFirst().get();
		System.out.println(element);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		Cntx cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.USES_CONSTANT));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float fiii"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.USES_CONSTANT));

	}

	@Test
	public void testProperty_C2USES_ENUM() {

		String content = "" + "class X {" + "public enum MYEN  {ENU1, ENU2;}" + "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "int myzimilar = 2;" + //
				"float fiii =  getConvertFloat(MYEN.ENU1);"//
				+ " double dother=0l;"//
				+ " double ddother = dother;" + "return mysimilar;" + "}" + "public float getFloat(){return 1.0;}"//
				+ "public int getConvertFloat(MYEN f){return 1;}"//
				+ "};";

		CtType type = getCtType(content);
		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CtElement element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("float fiii"))
				.findFirst().get();
		System.out.println(element);
		Cntx cntx = cntxResolver.analyzeFeatures(element);
		// TODO: Failing:
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.C2_USES_ENUMERATION));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("int myzimilar"))
				.findFirst().get();
		System.out.println(element);

		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.C2_USES_ENUMERATION));

	}

	@Test
	public void testProperty_NR_OBJECT_ASSIGNED() {

		String content = "" + "class X {" + "public enum MYEN  {ENU1, ENU2;}" + "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "Object ob = null;" //
				+ "ob = new String();"//
				+ "String t= ob.toString();" // HERE: initialized
				+ "boolean com = (ob == t);" //
				+ "com = (t==true);" + "return ob;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("com =")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(0, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return ob"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(0, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean com"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(2, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(0, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

	}

	@Test
	public void testProperty_NR_OBJECT_ASSIGNED_Decl_notInit() {

		String content = "" + "class X {" + "public enum MYEN  {ENU1, ENU2;}" + "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "Object ob = null;" //
				+ "ob = new String();"//
				+ "String t= null;" // HERE: not init
				+ "boolean com = (ob == t);" //
				+ "com = (t==true);" + "return ob;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("com =")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(0, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return ob"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(0, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean com"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

	}

	@Test
	public void testProperty_S1_LocalVariable_ASSIGNED() {

		String content = "" + "class X {"
		//
				+ "public enum MYEN  {ENU1, ENU2;}" + //
				"public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "Object ob = null;" //
				+ "ob = new String();"//
				+ "String t= null;" // Not initialized (default expression == null)
				+ "boolean com = (ob == t);" //
				+ "if(t == null){}"// Control flow to ignore
				+ "com = (t==true);"//
				+ "return ob;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("com =")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(0, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));
		// All are local
		assertEquals(0, cntx.get(CodeFeatures.NR_OBJECT_ASSIGNED_LOCAL));
		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_NOT_ASSIGNED_LOCAL));

		//
		boolean existsNotAssigned = Boolean.parseBoolean(cntx.get(CodeFeatures.S1_LOCAL_VAR_NOT_ASSIGNED).toString());
		assertTrue(existsNotAssigned);

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return ob"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(0, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_ASSIGNED_LOCAL));
		assertEquals(0, cntx.get(CodeFeatures.NR_OBJECT_NOT_ASSIGNED_LOCAL));

		boolean existsAssigned = Boolean.parseBoolean(cntx.get(CodeFeatures.S1_LOCAL_VAR_NOT_ASSIGNED).toString());
		assertFalse(existsAssigned);

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean com"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_ASSIGNED_LOCAL));
		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_NOT_ASSIGNED_LOCAL));

	}

	@Test
	public void testProperty_NR_LocalVariable_ASSIGNED_withGlobalVars() {

		String content = "" + "class X {"
		//
				+ " String tconst = null;"//

				+ "public enum MYEN  {ENU1, ENU2;}" + //
				"public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "Object ob = null;" //
				+ "ob = new String();"//
				+ "String t= null;" // Not initialized (default expression == null)
				+ "boolean com = (ob == t);" //
				+ "com = (t==tconst);" // the tconst never assigned
				+ "tconst = t;" // assigning
				+ "t = ctconst+tconst;" //
				+ "return ob;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("com =")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(0, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(2, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));
		// All are local
		assertEquals(0, cntx.get(CodeFeatures.NR_OBJECT_ASSIGNED_LOCAL));
		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_NOT_ASSIGNED_LOCAL));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return ob"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(0, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_ASSIGNED_LOCAL));
		assertEquals(0, cntx.get(CodeFeatures.NR_OBJECT_NOT_ASSIGNED_LOCAL));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean com"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_ASSIGNED));
		assertEquals(1, cntx.get(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED));

		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_ASSIGNED_LOCAL));
		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_NOT_ASSIGNED_LOCAL));

	}

	@Test
	public void testProperty_NR_OBJECT_USED() {

		String content = "" + "class X {" + "public enum MYEN  {ENU1, ENU2;}" + "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "Object ob = null;" //
				+ "ob = new String();"//
				+ "String t= ob.toString();" //
				+ "String t2 = null;" //
				+ "boolean com = (ob == t) && (t2 == t);" //
				+ "return ob;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean com"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_USED));
		assertEquals(2, cntx.get(CodeFeatures.NR_OBJECT_NOT_USED));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return ob"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_USED));
		assertEquals(0, cntx.get(CodeFeatures.NR_OBJECT_NOT_USED));

	}

	@Test
	public void testProperty_S3_TYPE_OF_FAULTY_STATEMENT() {

		String content = "" + "class X {" +
		//
				"String tdef = \"hello\";" + // defined
				"String tco = null;" + //
				"public enum MYEN  {ENU1, ENU2;}" + "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "Object ob = null;" //
				+ "ob = new String();"//
				+ "String t= ob.toString();" //
				+ "String t2 = null;" //
				+ "boolean com = (ob == t) && (t2 == t);" //
				+ "String t4 = null;" // Never used
				+ "t2 = tco + t4 ;"// tco is not used, but it's not local, t4 never used but is local
				+ "t = tco + t4 + tdef + t2;"// one global used not
				+ "while (t != null){}" + "return ob;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean com"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals("LocalVariable", cntx.get(CodeFeatures.S3_TYPE_OF_FAULTY_STATEMENT));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return ob"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals("Return", cntx.get(CodeFeatures.S3_TYPE_OF_FAULTY_STATEMENT));

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("t2 =")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals("Assignment", cntx.get(CodeFeatures.S3_TYPE_OF_FAULTY_STATEMENT));
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("while")).findFirst()
				.get();
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals("While", cntx.get(CodeFeatures.S3_TYPE_OF_FAULTY_STATEMENT));

	}

	@Test
	public void testProperty_NR_OBJECT_USED_LOCAL_VARS() {

		String content = "" + "class X {" +
		//
				"String tdef = \"hello\";" + // defined
				"String tco = null;" + //
				"public enum MYEN  {ENU1, ENU2;}" + "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "Object ob = null;" //
				+ "ob = new String();"//
				+ "String t= ob.toString();" //
				+ "String t2 = null;" //
				+ "boolean com = (ob == t) && (t2 == t);" //
				+ "String t4 = null;" // Never used
				+ "t2 = tco + t4 ;"// tco is not used, but it's not local, t4 never used but is local
				+ "t = tco + t4 + tdef + t2;"// one global used not
				+ "return ob;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean com"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_USED));
		assertEquals(2, cntx.get(CodeFeatures.NR_OBJECT_NOT_USED));

		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_USED_LOCAL_VAR));
		assertEquals(2, cntx.get(CodeFeatures.NR_OBJECT_NOT_USED_LOCAL_VAR));

		boolean existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.S1_LOCAL_VAR_NOT_USED).toString());
		assertTrue(existsNotUsed);

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return ob"))
				.findFirst().get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_USED));
		assertEquals(0, cntx.get(CodeFeatures.NR_OBJECT_NOT_USED));

		existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.S1_LOCAL_VAR_NOT_USED).toString());
		assertFalse(existsNotUsed);

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("t2 =")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(0, cntx.get(CodeFeatures.NR_OBJECT_USED));
		assertEquals(2, cntx.get(CodeFeatures.NR_OBJECT_NOT_USED));

		assertEquals(0, cntx.get(CodeFeatures.NR_OBJECT_USED_LOCAL_VAR));
		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_NOT_USED_LOCAL_VAR));

		existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.S1_LOCAL_VAR_NOT_USED).toString());
		assertTrue(existsNotUsed);

		/////

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("t =")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);

		assertEquals(3, cntx.get(CodeFeatures.NR_OBJECT_USED));
		assertEquals(1, cntx.get(CodeFeatures.NR_OBJECT_NOT_USED));

		assertEquals(2, cntx.get(CodeFeatures.NR_OBJECT_USED_LOCAL_VAR));
		assertEquals(0, cntx.get(CodeFeatures.NR_OBJECT_NOT_USED_LOCAL_VAR));

		existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.S1_LOCAL_VAR_NOT_USED).toString());
		assertFalse(existsNotUsed);

	}

	@Test
	public void testProperty_L1() {

		String content = "" + "class X {" +
		//
				"String tdef = \"hello\";" + // defined
				"String tco = null;" + //
				"public enum MYEN  {ENU1, ENU2;}"//
				+ "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "if (mysimilar > 0){};" //
				+ "float f2 = 2;" //
				+ "boolean s1 = (mysimilar > 2) && true;" //
				+ "boolean s2 = (f2 > 2) && s1;" //
				+ "double d1 = 0;"//
				// + "double d2=0;"//
				+ "float f3 = (float) d1;" + // using d1 in not a binary
				"if(true && (true && true && (f3))){}" + //
				"boolean s3 = (d1 > 0)   ;"//
				+ "boolean s4 = f3 > 0) && s3 ;"//
				+ "return null;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;
		/// C1
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean s2"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);
		boolean existsNotUsed = Boolean
				.parseBoolean(cntx.get(CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION).toString());
		assertTrue(existsNotUsed);

		/// C2:
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean s1"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);

		existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION).toString());
		assertTrue(existsNotUsed);

		/// C3:
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean s3"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);

		existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION).toString());
		assertFalse(existsNotUsed);

		/// C4:
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean s4"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);
		// Now d1 is used in binary ()
		existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION).toString());
		assertTrue(existsNotUsed);

	}

	@Test
	@Ignore
	public void testProperty_L1_forbeforerestriction() {

		String content = "" + "class X {" +
		//
				"String tdef = \"hello\";" + // defined
				"String tco = null;" + //
				"public enum MYEN  {ENU1, ENU2;}"//
				+ "public Object foo() {" //
				+ " float mysimilar = 1;"//
				+ "if (mysimilar > 0){};" //
				+ "float f2 = 2;" //
				+ "boolean s1 = (mysimilar > 2);" //
				+ "boolean s2 = (f2 > 2) && s1;" //
				+ "double d1 = 0;"//
				+ "double d2=0;"//
				+ "float f3 = (float) d1;" + // using d1 in not a binary
				"if(true && (true && true && (f3))){}" + //
				"boolean s3 = (d1 > 0)   ;"//
				+ "boolean s4 = (d2 > 0) && s3 ;"//
				+ "return null;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean s2"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);
		boolean existsNotUsed = Boolean
				.parseBoolean(cntx.get(CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION).toString());
		assertTrue(existsNotUsed);

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean s1"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);

		existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION).toString());
		assertFalse(existsNotUsed);

		///
		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean s3"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);

		existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION).toString());
		assertFalse(existsNotUsed);

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("boolean s4"))
				.findFirst().get();
		cntx = cntxResolver.analyzeFeatures(element);
		// Now d1 is used in binary ()
		existsNotUsed = Boolean.parseBoolean(cntx.get(CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION).toString());
		assertTrue(existsNotUsed);

	}

	@Test
	public void testProperty_NR_FIElD_INIT_INCOMPLETE_1() {
		// Case: fx from fx (recursive reference)
		String content = "" + "class X {" //
				+ "public X fX = null;" + //
				"public int f1 = 0;" + //
				"private int f2 = 0;" + //

				"public Object foo() {" + //
				" fX = new X();"//
				+ "fX.f1 = 0;" //
				+ "f2 = fX.f2;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("f2 = ")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		// field not assigned fX
		// Strange behaviour: fails when running, works when debbuging
		// assertEquals(Boolean.TRUE, cntx.get(CNTX_Property.NR_FIELD_INCOMPLETE_INIT));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProperty_NR_FIElD_INIT_INCOMPLETE_2() {
		/// Case 2: all init (f2 is private so we dont initialize it)

		String content = "" + "class X {" //
				+ "public X fX = null;" + //
				"public int f1 = 0;" + //
				"private int f2 = 0;" + //

				"public Object foo() {" + //
				" fX = new X();"// init the field
				+ "fX.fX = null;"//
				+ "fX.f1 = 0;"//
				+ "int mv ;" + //
				"mv = fX.f2;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("mv = ")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.NR_FIELD_INCOMPLETE_INIT));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProperty_NR_FIElD_INIT_INCOMPLETE_3() {
		/// Case : missing init of f2

		String content = "" + "class X {" //
				+ "public X fX = null;" + //
				"public int f1 = 0;" + //
				"public int f2 = 0;" + //

				"public Object foo() {" + //
				" fX = new X();"// init the field
				+ "fX.fX = null;"//
				+ "fX.f1 = 0;"//
				+ "int mv ;" + //
				"mv = fX.f2;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("mv = ")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(Boolean.TRUE, cntx.get(CodeFeatures.NR_FIELD_INCOMPLETE_INIT));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProperty_NR_FIElD_INIT_INCOMPLETE_4() {
		/// Case all initialized

		String content = "" + "class X {" //
				+ "public X fX = null;" + //
				"public int f1 = 0;" + //
				"public int f2 = 0;" + //

				"public Object foo() {" + //
				" fX = new X();"// init the field
				+ "fX.fX = null;"//
				+ "fX.f1 = 0;"//
				+ "int mv ;" //
				+ "fX.f2 = 0;"//
				+ "mv = fX.f2;" + //
				"};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("mv = ")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		assertEquals(Boolean.FALSE, cntx.get(CodeFeatures.NR_FIELD_INCOMPLETE_INIT));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProperty_VAR_CNT() {
		/// Case all initialized

		String content = "" + "class X {" //
				+ "public X fX = null;" + //
				"public int f1 = 0;" + //
				"public int f2 = 0;" + //
				"public String s2;" //
				+ "public Object foo() {" + //
				" fX = new X();"// init the field
				+ "fX.fX = null;"//
				+ "fX.f1 = 0;"//
				+ "int mv ;" //
				+ "fX.f2 = 0;"//
				+ "mv = fX.f2;" + //
				"};" + "public X copy(X mx){return mx;}" + "};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("mv = ")).findFirst()
				.get();
		System.out.println(element);
		ConfigurationProperties.setProperty("max_synthesis_step", "100");
		cntx = cntxResolver.analyzeFeatures(element);

		// List<?> space = (List<?>) cntx.get(CNTX_Property.PSPACE);
		int i = 0;
		// for (Object spaceeleemnt : space) {
		// System.out.println((i++) + "--> " + spaceeleemnt);
		// }

		// assertEquals(Boolean.FALSE,
		// cntx.get(CNTX_Property.NR_FIELD_INCOMPLETE_INIT));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProperty_VAR_CNT_2_small() {
		/// Case all initialized

		String content = "" + "class X {" + //

				"public int f1 = 0;" + //
				"public boolean b2 = 0;" + //

				"public Object foo() {" + //
				"return null;"// init the field
				+ "};};";

		CtType type = getCtType(content);

		assertNotNull(type);
		CtMethod method = (CtMethod) type.getMethods().stream()
				.filter(e -> ((CtMethod) e).getSimpleName().equals("foo")).findFirst().get();

		assertNotNull(method);
		System.out.println(method);

		CodeFeatureDetector cntxResolver = new CodeFeatureDetector();
		CtElement element = null;
		Cntx cntx = null;

		element = method.getBody().getStatements().stream().filter(e -> e.toString().startsWith("return ")).findFirst()
				.get();
		System.out.println(element);
		cntx = cntxResolver.analyzeFeatures(element);
		// assertEquals(Boolean.FALSE,
		// cntx.get(CNTX_Property.NR_FIELD_INCOMPLETE_INIT));

		List<?> space = (List<?>) cntx.get(CodeFeatures.PSPACE);
		int i = 0;
		// for (Object spaceeleemnt : space) {
		// System.out.println((i++) + "--> " + spaceeleemnt);
		// }
	}

	protected CtType getCtType(File file) throws Exception {

		SpoonResource resource = SpoonResourceHelper.createResource(file);
		return getCtType(resource);
	}

	protected CtType getCtType(SpoonResource resource) {
		Factory factory = createFactory();
		factory.getModel().setBuildModelIsFinished(false);
		SpoonModelBuilder compiler = new JDTBasedSpoonCompiler(factory);
		compiler.getFactory().getEnvironment().setLevel("OFF");
		compiler.addInputSource(resource);
		compiler.build();

		if (factory.Type().getAll().size() == 0) {
			return null;
		}

		// let's first take the first type.
		CtType type = factory.Type().getAll().get(0);
		// Now, let's ask to the factory the type (which it will set up the
		// corresponding
		// package)
		return factory.Type().get(type.getQualifiedName());
	}

	protected CtType<?> getCtType(String content) {
		VirtualFile resource = new VirtualFile(content, "/test");
		return getCtType(resource);
	}

	protected Factory createFactory() {
		Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		factory.getEnvironment().setNoClasspath(true);
		factory.getEnvironment().setCommentEnabled(false);
		return factory;
	}

}
