package de.jlo.talend.model.sqlparsing;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.jlo.talend.tweak.model.sql.SQLParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TableAndProcedureNameFinder;

public class TestParseSQL {

	@Test
	public void testSelectsStrict() throws Exception {
		String sql1 = "with ws1 as (\n"
				    + "    select schema_c.function1(x) as alias_x from schema_c.table_c\n"
				    + "), \n"
				    + "ws2 as (\n"
				    + "    select y from schema_d.table_d\n"
				    + ") \n"
				    + "select \n"
				    + "    a as alias_a, \n"
				    + "    b as alias_b, \n"
				    + "    (select c from schema_e.table_e) as alias_c \n"
				    + "from schema_a.table_1 ta \n"
				    + "join schema_b.table_b tb using(c) \n"
				    + "join ws1 using(c)";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNames();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(5, tableList.size());
		List<String> functionNames = tablesNamesFinder.getListFunctionSignatures();
		for (String f : functionNames) {
			System.out.println(f);
		}
		assertEquals(1, functionNames.size());
	}

	@Test
	public void testSelectsFromMethod() throws Exception {
		String sql1 = "with ws1 as (\n"
				    + "    select schema_c.function1(x) as alias_x from schema_c.table_c\n"
				    + "), \n"
				    + "ws2 as (\n"
				    + "    select y from schema_d.table_d\n"
				    + ") \n"
				    + "select \n"
				    + "    a as alias_a, \n"
				    + "    b as alias_b, \n"
				    + "    (select c from schema_e.table_e) as alias_c \n"
				    + "from schema_a.table_1 ta \n"
				    + "join schema_b.table_b tb using(c) \n"
				    + "join ws1 using(c)";
		SQLParser parser = new SQLParser();
		List<String> tableList = parser.findFromTables(sql1);
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(5, tableList.size());
	}

	@Test
	public void testSelectProc() throws Exception {
		String sql1 = "select a from procedureCall(1,2)";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> functionNames = tablesNamesFinder.getListFunctionSignatures();
		for (String f : functionNames) {
			System.out.println(f);
		}
		assertEquals(1, functionNames.size());
	}

	@Test
	public void testTruncateTable() throws Exception {
		String sql1 = "truncate table schema_a.table_a";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNames();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
	}

	@Test
	public void testDeleteTable() throws Exception {
		String sql1 = "delete from schema_a.table_a a";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNames();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
	}

	@Test
	public void testUpdateTable() throws Exception {
		String sql1 = "update schema_a.table_a a set x = b.v from schema_a.table_b b";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNames();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(2, tableList.size());
	}

}
