package de.jlo.talend.model.sqlparsing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jlo.talend.tweak.model.sql.ContextVarResolver;
import de.jlo.talend.tweak.model.sql.SQLCodeUtil;

public class TestConvertTalend2SQL {
	
	@Test
	public void testReplaceContext() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("DB1_Schema", "schema_1");
		r.addContextVar("DB2_Schema", "schema_2");
		String testSQL = "select * from \"\" + context.DB1_Schema + \"\".\"table1\",\n\" + context.DB2_Schema + \".table2";
		String expected = "select * from \"schema_1\".\"table1\",\nschema_2.table2";
		String actual = r.replaceContextVars(testSQL);
		assertEquals("Fail", expected, actual);
	}
	
	@Test
	public void testRetrievePureSQL() throws Exception {
		String tq = "\"SELECT\n\\\"\"+context.B17_MANAGEMENT_DB_Database+\"\\\".\\\"\" + context.B17_MANAGEMENT_DB_Schema +   \"\\\".\\\"measureconfig\\\".\\\"job_instance_id\\\"\nFROM \\\"\"+context.B17_MANAGEMENT_DB_Database+\"\\\".\\\"\"+context.B17_MANAGEMENT_DB_Schema+\"\\\".\\\"measureconfig\\\"\"";
		System.out.println(tq);
		// first replace the context vars
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("B17_MANAGEMENT_DB_Database", "nucleus");
		r.addContextVar("B17_MANAGEMENT_DB_Schema", "b17_management");
		String withoutContextActual = r.replaceContextVars(tq);
		String withoutContextExcepted = "\"SELECT\n\\\"nucleus\\\".\\\"b17_management\\\".\\\"measureconfig\\\".\\\"job_instance_id\\\"\nFROM \\\"nucleus\\\".\\\"b17_management\\\".\\\"measureconfig\\\"\"";
		assertEquals("Context replacement failed", withoutContextExcepted, withoutContextActual);
		// convert String to SQL
		String actual = SQLCodeUtil.convertJavaToSqlCode(withoutContextActual).trim();
		String expected = "SELECT\n\"nucleus\".\"b17_management\".\"measureconfig\".\"job_instance_id\"\nFROM \"nucleus\".\"b17_management\".\"measureconfig\"";
		assertEquals("Convert Java to SQL failed", expected, actual);
	}
	
	@Test
	public void testReplaceGlobalMapVars() throws Exception {
		String tq = "\"select * from schema_a.table_a where x = \" + ((String) globalMap.get(\"key\"))";
		String replacedVarsActual = SQLCodeUtil.replaceGlobalMapVars(tq);
		String replacedVarsExpected = "\"select * from schema_a.table_a where x = 999999";
		assertEquals("Replace globalMap failed", replacedVarsExpected, replacedVarsActual);
		String actual = SQLCodeUtil.convertJavaToSqlCode(replacedVarsActual);
		String expected = "select * from schema_a.table_a where x = 999999";
		assertEquals("Convert Java to SQL failed", expected, actual);
	}

}
