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
package com.squid.core.jdbc.vendor.mysql.render;

import java.io.IOException;
import java.sql.Types;

import com.squid.core.database.model.DatabaseProduct;
import com.squid.core.database.model.Table;
import com.squid.core.domain.CustomTypes;
import com.squid.core.domain.operators.ExtendedType;
import com.squid.core.domain.operators.IntrinsicOperators;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.FromTablePiece;
import com.squid.core.sql.db.templates.DefaultJDBCSkin;
import com.squid.core.sql.db.templates.ISkinProvider;
import com.squid.core.sql.render.DelegateSamplingDecorator;
import com.squid.core.sql.render.ISamplingDecorator;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class MySQLSkin extends DefaultJDBCSkin {

	protected MySQLSkin(ISkinProvider provider, DatabaseProduct product) {
		super(provider, product);
		//
	}

	@Override
	protected void initFormat() {
		super.initFormat();	
		// use backtick "`" by default
		setIdentifier_quote("`");
		setLiteral_quote("\'");
	}

	@Override
	public ISamplingDecorator createSamplingDecorator(DelegateSamplingDecorator sampling) {
		return new SamplingPiece(sampling);
	}

	@Override
	protected String render(SQLSkin skin,FromTablePiece piece) throws RenderingException, IOException {
		String render = "";
		final Table table = piece.getTable();
		if (table==null) {
			throw new RenderingException("table definition is null");
		}
		//
		if (piece.getSamplingDecorator()!=null) {
			// sampling version
			// select Y.x from (select * from table where random()<.y) as Y
			//
			render += " ( select * from ";
			if (table.getSchema()!=null&&!table.getSchema().isNullSchema()) {
				render += skin.quoteSchemaIdentifier(table.getSchema());
				render += ".";
			}
			render += skin.quoteTableIdentifier(table);
			// sampling
			if (piece.getSamplingDecorator().getMode()==ISamplingDecorator.FRACTION) {
				render += " where rand()<"+piece.getSamplingDecorator().getPercent()/100;
			} else {
				render += " limit "+(int)piece.getSamplingDecorator().getSize();
			}
			render += " ) as "+ piece.getAlias();
		} else {
			if (table.getSchema()!=null&&!table.getSchema().isNullSchema()) {
				render += skin.quoteSchemaIdentifier(table.getSchema());
				render += ".";
			}
			render += skin.quoteTableIdentifier(table);
			//
			// alias
			render += " "+piece.getAlias();
		}
		//
		// joining
		render += renderJoinDecorator(skin,piece);
		return render;
	}
	/**
	 * Overwrite default types for rendering issue: for ex, DATE(10) can't be used in a create statement
	 * @Override
	 */
	@Override
	public String getTypeDefinition(ExtendedType type) {
		if (type==null) {
			return "NULL";
		}
		switch (type.getDataType()) {
		case Types.BIT:
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.BIGINT:
		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			return type.getName();
		case CustomTypes.INTERVAL:
			return "INTEGER";
		case Types.TINYINT:
			return "SMALLINT";
		default:
			if (type.getName().equalsIgnoreCase("nvarchar")) {
				return "VARCHAR("+type.getSize()+")";
			} else if (type.getName().equalsIgnoreCase("nchar")) {
				return "CHAR("+type.getSize()+")";
			} else if (type.getName().equalsIgnoreCase("text")) {
				return "TEXT";
			}
			return super.getTypeDefinition(type);
		}
	}
	
	@Override
	public String render(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args) throws RenderingException {
		if (opDef.getId()==IntrinsicOperators.MODULO) {
			return opDef.prettyPrint("MOD", OperatorDefinition.PREFIX_POSITION, args, true);
		} else if (opDef.getId()==IntrinsicOperators.CONCAT) {
			return opDef.prettyPrint("||", OperatorDefinition.INFIX_POSITION, args, true);
		} else {
			return super.render(skin, piece, opDef, args);
		}
	}

	/* (non-Javadoc)
	 * @see com.squid.core.sql2.template.DefaultSQLSkin#getSkinPrefix()
	 */
	@Override
	public String getSkinPrefix() {
		return this.getProvider().getSkinPrefix(getProduct());
	}

	@Override
	public String quoteComment(String text) {
		return "\n/*\n"+comment(text)+"\n*/\n";
	}
	
}
