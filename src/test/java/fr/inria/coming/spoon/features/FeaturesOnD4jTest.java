package fr.inria.coming.spoon.features;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import java.io.File;
//
//import org.apache.log4j.ConsoleAppender;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PatternLayout;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonPrimitive;
//
//import fr.inria.coming.codefeatures.CodeFeatures;
//import fr.inria.coming.codefeatures.FeatureAnalyzer;
//
///**
// * Testing features on D4j
// *
// * @author Matias Martinez
// *
// */
//public class FeaturesOnD4jTest {
//
//	FeatureAnalyzer featureAnalyzer = new FeatureAnalyzer();
//
//	private JsonElement getJsonOfBugId(String diffId) {
//		String input = "Defects4J_all_pairs/" + diffId;
//		File file = new File("./src/main/resources/" + input);
//		JsonElement resultjson = featureAnalyzer.processFilesPair(file);
//		return resultjson;
//	}
//
//	private static void asserFeatureValue(JsonElement resultjson, CodeFeatures name, Boolean b) {
//		assertMarkedlAST(resultjson, name.name(), b);
//	}
//
//	private static void assertMarkedlAST(JsonElement resultjson, String name, Boolean b) {
//
//		// System.out.println("**************** finding " + name);
//
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		String prettyJsonString = gson.toJson(resultjson);
//
//		// System.out.println(prettyJsonString);
//		boolean found = false;
//		JsonArray affected = (JsonArray) resultjson;
//
//		for (JsonElement jsonElement : affected) {
//
//			JsonObject jo = (JsonObject) jsonElement;
//			JsonElement elAST = jo.get("features");
//
//			assertNotNull(elAST);
//			assertTrue(elAST instanceof JsonArray);
//			JsonArray featuresOperationList = (JsonArray) elAST;
//			assertTrue(featuresOperationList.size() > 0);
//
//			for (JsonElement featuresOfOperation : featuresOperationList) {
//
//				JsonObject jso = featuresOfOperation.getAsJsonObject();
//				JsonElement property = jso.get(name.toString());
//				if (property != null) {
//					JsonPrimitive value = property.getAsJsonPrimitive();
//
//					// System.out.println(name + " " + value.getAsString());
//					found = found || Boolean.parseBoolean(value.getAsString());
//				}
//			}
//
//		}
//
//		assertEquals(b, found);
//	}
//
//	private static void asserFeatureValue(JsonElement resultjson, String typeProperty, CodeFeatures nameProperty,
//			String elementname, Boolean b) {
//
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		String prettyJsonString = gson.toJson(resultjson);
//
//		// System.out.println(prettyJsonString);
//		boolean found = false;
//		JsonArray affected = (JsonArray) resultjson;
//
//		for (JsonElement jsonElement : affected) {
//
//			JsonObject jo = (JsonObject) jsonElement;
//			JsonElement elAST = jo.get("features");
//
//			assertNotNull(elAST);
//			assertTrue(elAST instanceof JsonArray);
//			JsonArray featuresOperationList = (JsonArray) elAST;
//			assertTrue(featuresOperationList.size() > 0);
//
//			for (JsonElement featuresOfOperation : featuresOperationList) {
//
//				JsonObject jso = featuresOfOperation.getAsJsonObject();
//				JsonObject typePropertyJSon = (JsonObject) jso.get(typeProperty);
//				if (typePropertyJSon == null)
//					continue;
//				JsonObject elementPropertyJSon = (JsonObject) typePropertyJSon.get(elementname);
//				if (elementPropertyJSon != null) {
//					JsonElement property = elementPropertyJSon.get(nameProperty.toString());
//					if (property != null) {
//						JsonPrimitive value = property.getAsJsonPrimitive();
//
//						// System.out.println(nameProperty + " " + value.getAsString());
//						found = found || Boolean.parseBoolean(value.getAsString());
//					}
//				}
//			}
//
//		}
//
//		assertEquals(b, found);
//	}
//
//	@Before
//	public void setUp() throws Exception {
//
//		ConsoleAppender console = new ConsoleAppender();
//		String PATTERN = "%m%n";
//		console.setLayout(new PatternLayout(PATTERN));
//		console.setThreshold(Level.INFO);
//		console.activateOptions();
//		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
//		Logger.getRootLogger().addAppender(console);
//
//	}
//
//	@Test
//	public void testContext_M4_Closure9() {
//
//		String diffId = "Closure_9";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//
//		asserFeatureValue(resultjson, CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY, Boolean.TRUE);
//		asserFeatureValue(resultjson, CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY, Boolean.TRUE);
//
//		asserFeatureValue(resultjson, "FEATURES_METHODS", CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY,
//				"normalizeSourceName(java.lang.String)", true);
//
//		asserFeatureValue(resultjson, CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY, Boolean.TRUE);
//
//		asserFeatureValue(resultjson, "FEATURES_METHODS", CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN,
//				"normalizeSourceName(java.lang.String)", false);
//
//		asserFeatureValue(resultjson, CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, Boolean.FALSE);
//
//	}
//
//	@Test
//	public void testContext_M1_Math_58() {
//
//		String diffId = "Math_58";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//
//		// System.out.println(resultjson);
//
//		asserFeatureValue(resultjson, CodeFeatures.M1_OVERLOADED_METHOD, Boolean.TRUE);
//
//		asserFeatureValue(resultjson, "FEATURES_METHODS", CodeFeatures.M1_OVERLOADED_METHOD, "fit(double[])",
//				Boolean.TRUE);
//
//	}
//
//	@Test
//	public void testContefxt_L1_Closure20() {
//
//		String diffId = "Closure_20";
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//
//		asserFeatureValue(resultjson, CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION, Boolean.TRUE);
//	}
//
//	@Test
//	public void testContext_L2_Closure51() {
//
//		String diffId = "Closure_51";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		//
//		asserFeatureValue(resultjson, CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR, Boolean.TRUE);
//	}
//
//	@Test
//	public void testContext_L3_Chart_9() {
//
//		String diffId = "Chart_9";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		//
//		asserFeatureValue(resultjson, CodeFeatures.LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED, Boolean.TRUE);
//	}
//
//	@Test
//	public void testContext_L4_Closure_38() {
//
//		String diffId = "Closure_38";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		//
//		asserFeatureValue(resultjson, CodeFeatures.LE4_EXISTS_LOCAL_UNUSED_VARIABLES, Boolean.TRUE);
//	}
//
//	@Test
//	public void testContext_L5_Closure_38() {
//
//		String diffId = "Closure_38";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		//
//	//	asserFeatureValue(resultjson, CodeFeatures.LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY, Boolean.TRUE);
//	}
//
//	@Test
//	public void testContext_L6_Closure_31() {
//
//		String diffId = "Closure_31";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		//
//		asserFeatureValue(resultjson, CodeFeatures.LE6_HAS_NEGATION, Boolean.TRUE);
//	}
//
//	@Test
//	public void testContext_L7_Closure_18() {
//
//		String diffId = "Closure_18";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		//
//		asserFeatureValue(resultjson, CodeFeatures.LE7_SIMPLE_VAR_OR_METHOD_IN_LOGIC, Boolean.TRUE);
//	}
//
//	@Test
//	public void testContext_S1_Chart_4() {
//
//		String diffId = "Chart_4";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		// it's used in statement
//		// assertMarkedlAST(resultjson, CodeFeatures.S1_LOCAL_VAR_NOT_USED,
//		// Boolean.TRUE);
//	}
//
//	@Test
//	public void testContext_S2_Closure_60() {
//
//		String diffId = "Closure_60";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		// it's used in statement
//		// assertMarkedlAST(resultjson,
//		// CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_GUARD, Boolean.FALSE);
//	}
//
//	@Test
//	public void testContext_S2_Closure_111() {
//
//		String diffId = "Closure_111";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		// The condition has an invocation which return type is not known
////		asserFeatureValue(resultjson, CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_GUARD, Boolean.TRUE);
////		asserFeatureValue(resultjson, CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_GUARD, Boolean.FALSE);
//	}
//
//	@Test
//	public void testContext_S6_Closure_83() {
//
//		String diffId = "Closure_83";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		asserFeatureValue(resultjson, CodeFeatures.S6_METHOD_THROWS_EXCEPTION, Boolean.TRUE);
//	}
//
//	@Test
//	public void testContext_v1() {
//		String diffId = "Math_24";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		asserFeatureValue(resultjson, CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN, Boolean.TRUE);
//
//	}
//
//	@Test
//	public void testContext_v2_2() {
//
//		String diffId = "Math_26";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		//
//		asserFeatureValue(resultjson, CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN, Boolean.FALSE);
//		// the property is not boolean any more:
//		// assertMarkedlAST(resultjson, CodeFeatures.S3_TYPE_OF_FAULTY_STATEMENT);
//		// + "_BinaryOperator"
//
//	}
//
//	@Test
//	@Ignore
//	public void testContext_m1_1() {
//		// To refactor
//		String diffId = null;// "Math_58";
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//		//
//		// assertMarkedlAST(resultjson, CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN,
//		// Boolean.FALSE);
//	}
//
//	@Test
//	public void testContext_Chart_7() {
//
//		String diffId = "Chart_7";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//
//	}
//
//	@Test
//	public void testContext_Closure_20() {
//
//		String diffId = "Closure_20";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//
//	}
//
//	@Test
//	public void testContext_Time_15() {
//		String diffId = "Time_15";
//
//		JsonElement resultjson = getJsonOfBugId(diffId);
//		// System.out.println(resultjson);
//
//	}
//
//}
