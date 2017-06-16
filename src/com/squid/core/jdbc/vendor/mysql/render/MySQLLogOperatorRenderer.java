package com.squid.core.jdbc.vendor.mysql.render;

import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.BaseOperatorRenderer;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class MySQLLogOperatorRenderer  extends BaseOperatorRenderer{

	@Override
	public String prettyPrint(SQLSkin skin, OperatorDefinition opDef, String[] args) throws RenderingException {
				return "LOG(10,"+ args[0] +")";
	}

}
