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

import com.squid.core.domain.IDomain;
import com.squid.core.domain.extensions.date.DateTruncateOperatorDefinition;
import com.squid.core.domain.operators.ExtendedType;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.DateTruncateOperatorRenderer;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

/**
 * @author luatnn
 *
 */
public class MySQLDateTruncateOperatorRenderer extends DateTruncateOperatorRenderer {

	@Override
	protected String prettyPrintTwoArgs(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {
		ExtendedType[] extendedTypes = null;
		extendedTypes = getExtendedPieces(piece);
		if (DateTruncateOperatorDefinition.WEEK.equalsIgnoreCase(args[1].replaceAll("'", ""))) {
			return "CAST(SUBDATE(" + args[0] + ", INTERVAL weekday(" + args[0] + ") DAY) as DATE)";
		} else if (DateTruncateOperatorDefinition.MONTH.equals(args[1].replaceAll("'", ""))) {
			return "CAST(DATE_FORMAT(" + args[0] + " ,'%Y-%m-01') as DATE)";
		} else if (DateTruncateOperatorDefinition.QUARTER.equalsIgnoreCase(args[1].replaceAll("'", ""))) {
			return "MAKEDATE(YEAR(" + args[0] + "), 1) + INTERVAL QUARTER(" + args[0]
					+ ") QUARTER - INTERVAL 1 QUARTER";
		} else if (DateTruncateOperatorDefinition.YEAR.equalsIgnoreCase(args[1].replaceAll("'", ""))) {
			return "CAST(DATE_FORMAT(" + args[0] + " ,'%Y-01-01') as DATE)";
		} else if (extendedTypes[0].getDomain().isInstanceOf(IDomain.TIMESTAMP)) {
			// a timestamp has to be truncated so it becomes a date
			return "CAST(DATE_FORMAT(" + args[0] + " ,'%Y-%m-%d') as DATE)";
		} else if (extendedTypes[0].getDomain().isInstanceOf(IDomain.DATE)) {
			// If it is already a date, no transformation is required
			return args[0];
		}
		return opDef.getSymbol() + "(" + args[0] + "," + args[1] + ")";
	}

}