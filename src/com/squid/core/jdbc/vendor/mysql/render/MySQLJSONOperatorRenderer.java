package com.squid.core.jdbc.vendor.mysql.render;

import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.OperatorRenderer;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class MySQLJSONOperatorRenderer implements OperatorRenderer {


	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args) throws RenderingException {
		String txt = "";
		if (opDef.getName().equals("JSON_ARRAY_LENGTH")) {
			txt = "JSON_LENGTH";
		} else if (opDef.getName().equals("JSON_EXTRACT_ARRAY_ELEMENT_TEXT")) {
			txt = "TRIM(BOTH '\"' FROM (JSON_EXTRACT";
		} else if (opDef.getName().equals("JSON_EXTRACT_PATH_TEXT")) {
			txt = "TRIM(BOTH '\"' FROM (JSON_EXTRACT";
		}
		txt = txt + "(";
		String separator = "";
		boolean isPath = false;
		for (String arg : args) {
			if (opDef.getName().equals("JSON_EXTRACT_ARRAY_ELEMENT_TEXT")) {
				arg = "["+arg+"]";
			} else if (opDef.getName().equals("JSON_EXTRACT_PATH_TEXT")) {
				arg = arg.replaceAll("'", "");
			}
			txt += separator + arg;
			if (!isPath) {
				separator = ", ";
				if (args.length>1) {
					separator = ", '$";
				}
				if (opDef.getName().equals("JSON_EXTRACT_PATH_TEXT"))  {
					separator += ".";
				}
				isPath = true;
			} else {
				separator = ".";
			}
		}
		txt += "";
		if (opDef.getName().equals("JSON_EXTRACT_PATH_TEXT") || opDef.getName().equals("JSON_EXTRACT_ARRAY_ELEMENT_TEXT")) {
			txt+= "'))";
		} else {
			txt+= ")";
		}
		return txt;
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorDefinition opDef, String[] args) throws RenderingException {
		return prettyPrint(skin, null, opDef, args);
	}

}
