/*******************************************************************************
 * Copyright © Squid Solutions, 2016
 *
 * This file is part of Open Bouquet software.
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * There is a special FOSS exception to the terms and conditions of the 
 * licenses as they are applied to this program. See LICENSE.txt in
 * the directory of this program distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Squid Solutions also offers commercial licenses with additional warranties,
 * professional functionalities or services. If you purchase a commercial
 * license, then it supersedes and replaces any other agreement between
 * you and Squid Solutions (above licenses and LICENSE.txt included).
 * See http://www.squidsolutions.com/EnterpriseBouquet/
 *******************************************************************************/
package com.squid.core.jdbc.vendor.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.squid.core.database.impl.DatabaseServiceException;
import com.squid.core.database.metadata.ColumnData;
import com.squid.core.database.metadata.MetadataConst;
import com.squid.core.database.metadata.VendorMetadataSupport;
import com.squid.core.database.model.DatabaseFactory;
import com.squid.core.database.model.Schema;
import com.squid.core.database.model.Table;
import com.squid.core.database.model.TableType;

/**
 * the simple JDBC version
 * @author sergefantino
 *
 */
public class MySQLMetadataSupport implements VendorMetadataSupport {

    private Hashtable<String, String> m_definitions;

    private final String[] COLUMNS_CNAMES = new String[]{
			 getColumnDef(MetadataConst.COLUMN_NAME),  //0
			 getColumnDef(MetadataConst.TYPE_NAME),    //1
			 getColumnDef(MetadataConst.COLUMN_SIZE),  //2
			 getColumnDef(MetadataConst.IS_NULLABLE),  //3
			 getColumnDef(MetadataConst.COLUMN_DEF),   //4
			 getColumnDef(MetadataConst.DATA_TYPE),    //5
			 getColumnDef(MetadataConst.TABLE_NAME),    //6
			 getColumnDef(MetadataConst.DECIMAL_DIGITS), // 7
			 getColumnDef(MetadataConst.REMARKS) // 8
	 };

	 private int[] COLUMNS_CPOS = null;

	 private int[] computeColumnPos(String[] columns, ResultSet result) throws SQLException {
			ResultSetMetaData meta = result.getMetaData();
			List<String> lookup = Arrays.asList(columns);
			int[] indexes = new int[columns.length];
			Arrays.fill(indexes,-1);
			for (int i=1;i<=meta.getColumnCount();i++) {
				String cname = meta.getColumnLabel(i);
				int index = lookup.indexOf(cname.toUpperCase());
				if (index>=0) {
					indexes[index] = i;
				}
			}
			return indexes;
		}
	 
    private void loadColumnData(ResultSet res, ColumnData data) throws SQLException {
		 if (COLUMNS_CPOS==null) {
			 COLUMNS_CPOS = computeColumnPos(COLUMNS_CNAMES,res);
		 }
		 //
		 data.table_name = res.getString(COLUMNS_CPOS[6]);           // TABLE_NAME
		 data.column_name = res.getString(COLUMNS_CPOS[0]).trim();   // COLUMN_NAME
		 data.type_name = res.getString(COLUMNS_CPOS[1]);            // TYPE_NAME
		 data.column_size = res.getInt(COLUMNS_CPOS[2]);             // COLUMN_SIZE
		 data.decimal_digits = res.getInt(COLUMNS_CPOS[7]);			// DECIMAL_DIGITS
		 //
		 // Oracle: issue: jdbc driver throwing exception when getting the DEfault_value column value after
		 data.column_def = res.getString(COLUMNS_CPOS[4]);           // COLUMN_DEF
		 data.is_nullable = res.getString(COLUMNS_CPOS[3]);          // IS_NULLABLE
		 //
		 data.data_type = res.getInt(COLUMNS_CPOS[5]);               // DATA_TYPE
		 //
		 data.remarks = res.getString(COLUMNS_CPOS[8]); // remarks
	 }
    
	public List<ColumnData> getColumns(Connection conn, String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
		List<ColumnData> datas = new ArrayList<ColumnData>();
		ResultSet res = conn.getMetaData().getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
		while (res.next()) {
            ColumnData data = new ColumnData();
            loadColumnData(res,normalizeColumnData(data));
            datas.add(data);
        }
        return datas;
    }

    public ResultSet getIndexInfo(Connection conn, String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        return conn.getMetaData().getIndexInfo(catalog, schema, table, unique, approximate);
    }

    public ResultSet getPrimaryKeys(Connection conn, String catalog, String schema, String table) throws SQLException {
        return conn.getMetaData().getPrimaryKeys(catalog, schema, table);
    }

    @Override
    public ColumnData normalizeColumnData(ColumnData data) {
        // default do nothing
    	return data;
    }
    
    /**
     * check if the database support UTF-8 surrogate Characters - the default seems to be false anyway ?
     * @return
     */
    public boolean handleSurrogateCharacters() {
        return false;
    }

    @Override
	public  int[] normalizeColumnType(java.sql.ResultSet rs) throws SQLException{
		ResultSetMetaData metadata = rs.getMetaData();
		int columnCount =  metadata.getColumnCount();
		int[] columnTypes = new int[columnCount];
		for (int i=0;i<columnCount;i++) {
			columnTypes[i] = metadata.getColumnType(i+1);
		}
		return columnTypes;
	}

	@Override
	public List<Schema> getSchemas(DatabaseFactory df, Connection conn) throws DatabaseServiceException {
		return getCatalogs(df, conn);
	}

	@Override
	public List<Schema> getCatalogs(DatabaseFactory df, Connection conn) throws DatabaseServiceException {
		DatabaseMetaData metadata;
		List<Schema> result = new ArrayList<Schema>();
		try {
			metadata = conn.getMetaData();
			ResultSet res = metadata.getCatalogs();
			while (res.next()) {
				 String name = res.getString(getColumnDef(MetadataConst.TABLE_CATALOG));
				 //
				 Schema schema = df.createSchema();
				 schema.setName(name);
				 schema.setSystem(isSystemSchema(name));
				 result.add(schema);
			}
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DatabaseServiceException("Not able to get catalogs");
		}
	}
	
	@Override
	public boolean isSystemSchema(String name) {
		return false;
	}
	
	public void init() {
		 if (m_definitions==null) {
			 m_definitions = new Hashtable<String, String>();
			 for (String def : MetadataConst.definitions) {
				 m_definitions.put(def,def);
			 }
		 }
	 }
	
	 protected String getColumnDef(String def) {
		 if (m_definitions==null) {
			 init();
		 }
		 final String result = m_definitions.get(def);
		 return result!=null?result:def;
	 }

	@Override
	public List<Table> getTables(DatabaseFactory df, Connection conn, String catalog, String name, String tableName) throws DatabaseServiceException {
		List<Table> result = new ArrayList<Table>();
		try {
			ResultSet res = conn.getMetaData().getTables(catalog, name,tableName,null);
			 while (res.next()) {
				 String xcatalog = res.getString(getColumnDef(MetadataConst.TABLE_CATALOG));
				 //String xschema = res.getString(getColumnDef(TABLE_SCHEMA));
				 //conflict with name and tableName
				 String tablename = res.getString(getColumnDef(MetadataConst.TABLE_NAME));
				 String type = res.getString(getColumnDef(MetadataConst.TABLE_TYPE));
				 String remarks = res.getString(getColumnDef(MetadataConst.REMARKS));
				 //
				 TableType tableType = null;
				 if (type.compareTo("T")==0||getColumnDef(MetadataConst.TABLE_TYPE_TABLE).compareTo(type)==0) {
				     tableType = TableType.Table;
				 } else if (type.compareTo("V")==0||getColumnDef(MetadataConst.TABLE_TYPE_VIEW).compareTo(type)==0) {
					tableType = TableType.View;
				 } else {
					 // it's a procedure, skip it...
				 }
				 if (tableType!=null) {// skip if not set
					 Table table = df.createTable();
					 table.setType(tableType);
					 table.setName(tablename);
					 if (xcatalog!=null) table.setCatalog(xcatalog);
					 if (remarks!=null) table.setDescription(remarks);
					 result.add(table);
				 }
			 }
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DatabaseServiceException("Not able to get tables");
		}
	}

	@Override
	public ResultSet getImportedKeys(Connection conn, String catalog, String name, String tableName) throws DatabaseServiceException {
		try {
			return conn.getMetaData().getImportedKeys(catalog, name, tableName);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DatabaseServiceException("Not able to import keys");
		}
	}


}
