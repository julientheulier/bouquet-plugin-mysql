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

import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.BaseOperatorRenderer;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;
import com.squid.core.sql.render.SimpleConstantValuePiece;

/**
 * Ticket #T3175 implements some ANSI functions
 * @author jtheulier
 * Translatee function for MySQL
 */
public class  MySQLTranslateOperatorRenderer
extends BaseOperatorRenderer
{
	public  MySQLTranslateOperatorRenderer() {
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef,
			String[] args) throws RenderingException {
		String parameter = args.length > 1 ? "parameter" : "parameters";
		if (args.length != 3) {
			throw new RenderingException(
					"Have not supported function TRANSLATE with " + args.length
					+ parameter);
		}
		if ((args[1].length() != args[2].length()) || !(piece.getParams()[1] instanceof SimpleConstantValuePiece) || !(piece.getParams()[2] instanceof SimpleConstantValuePiece)) {
			throw new RenderingException(
					"Replacement string & replaced string must have the same number of characters");

		}
		String replaced = (String) ((SimpleConstantValuePiece) piece.getParams()[1]).getValue();
		String replacement = (String) ((SimpleConstantValuePiece) piece.getParams()[2]).getValue();
		String txt = args[0];
		for (int i=0; i<replaced.length(); i++) {
			txt = "REPLACE("+txt+",'"+replaced.substring(i, i).replaceAll("'", "''")+",'"+replacement.substring(i, i).replaceAll("'", "''")+"')";
		}
		return txt;
	}


	@Override
	public String prettyPrint(SQLSkin skin,
			OperatorDefinition opDef, String[] args) throws RenderingException {
		return prettyPrint(skin, opDef, args);
	}

}
