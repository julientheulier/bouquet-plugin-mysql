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

import java.util.List;

import com.squid.core.database.impl.DataSourceReliable;
import com.squid.core.database.metadata.IMetadataEngine;
import com.squid.core.database.model.DatabaseProduct;
import com.squid.core.domain.extensions.JSON.JSONOperatorDefinition;
import com.squid.core.domain.extensions.cast.CastOperatorDefinition;
import com.squid.core.domain.extensions.date.AddMonthsOperatorDefinition;
import com.squid.core.domain.extensions.date.DateTruncateOperatorDefinition;
import com.squid.core.domain.extensions.date.DateTruncateShortcutsOperatorDefinition;
import com.squid.core.domain.extensions.date.extract.ExtractOperatorDefinition;
import com.squid.core.domain.extensions.date.operator.DateOperatorDefinition;
import com.squid.core.domain.extensions.string.PosStringOperatorDefinition;
import com.squid.core.domain.extensions.string.SplitPartOperatorDefinition;
import com.squid.core.domain.extensions.string.SubstringOperatorDefinition;
import com.squid.core.domain.extensions.string.regex.RegexpOperatorDefinition;
import com.squid.core.domain.maths.RandOperatorDefinition;
import com.squid.core.domain.maths.SinhCoshTanhOperatorDefintion;
import com.squid.core.domain.maths.TruncateOperatorDefintion;
import com.squid.core.domain.operators.IntrinsicOperators;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.features.IGroupingSetSupport;
import com.squid.core.sql.db.features.IMetadataForeignKeySupport;
import com.squid.core.sql.db.features.IMetadataPrimaryKeySupport;
import com.squid.core.sql.db.features.IRollupStrategySupport;
import com.squid.core.sql.db.render.AddMonthsAsIntervalOperatorRenderer;
import com.squid.core.sql.db.render.DateAddSubOperatorRenderer;
import com.squid.core.sql.db.render.DateEpochOperatorRenderer;
import com.squid.core.sql.db.render.ExtractAsFunctionOperatorRenderer;
import com.squid.core.sql.db.render.MetatdataSearchFeatureSupport;
import com.squid.core.sql.db.render.RLikeOperatorRenderer;
import com.squid.core.sql.db.templates.DefaultJDBCSkin;
import com.squid.core.sql.db.templates.DefaultSkinProvider;
import com.squid.core.sql.db.templates.ISkinProvider;
import com.squid.core.sql.db.templates.SkinRegistry;
import com.squid.core.sql.render.ISkinFeatureSupport;
import com.squid.core.sql.render.SQLSkin;
import com.squid.core.sql.render.ZeroIfNullFeatureSupport;
import com.squid.core.sql.statements.SelectStatement;

public class MySQLSkinProvider extends DefaultSkinProvider {

	private static final ZeroIfNullFeatureSupport zeroIfNull = new ANSIZeroIfNullFeatureSupport();

	public MySQLSkinProvider() {
		//
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.DIVIDE), new MySQLDivideOperatorRenderer());
		//
		registerOperatorRender("com.sodad.domain.operator.density.EQWBUCKET", new EquiWidthBucketRenderer());
		registerOperatorRender(PosStringOperatorDefinition.STRING_POSITION, new PosStringRenderer());
		registerOperatorRender(SubstringOperatorDefinition.STRING_SUBSTRING, new SubStringRenderer());
		registerOperatorRender(SplitPartOperatorDefinition.STRING_SPLIT_PART, new MySQLSplitPartOperatorRenderer());
		registerOperatorRender(DateOperatorDefinition.DATE_MONTHS_BETWEEN, new MonthsBetweenRenderer());

		registerOperatorRender(CastOperatorDefinition.TO_CHAR, new MySQLCastOperatorRenderer());
		registerOperatorRender(CastOperatorDefinition.TO_DATE, new MySQLCastOperatorRenderer());
		registerOperatorRender(CastOperatorDefinition.TO_NUMBER, new MySQLCastOperatorRenderer());
		registerOperatorRender(CastOperatorDefinition.TO_INTEGER, new MySQLCastOperatorRenderer());
		registerOperatorRender(CastOperatorDefinition.TO_TIMESTAMP, new MySQLCastOperatorRenderer());
		registerOperatorRender(DateOperatorDefinition.DATE_INTERVAL, new MySQLDateIntervalOperatorRenderer());
		registerOperatorRender(DateOperatorDefinition.DATE_ADD, new MySQLDateAddSubOperatorRenderer(DateAddSubOperatorRenderer.OperatorType.ADD));
		registerOperatorRender(DateOperatorDefinition.DATE_SUB, new MySQLDateAddSubOperatorRenderer(DateAddSubOperatorRenderer.OperatorType.SUB));
		registerOperatorRender(ExtractOperatorDefinition.EXTRACT_DAY_OF_WEEK, new MySQLDayOfWeekOperatorRenderer("DAYOFWEEK"));
		registerOperatorRender(ExtractOperatorDefinition.EXTRACT_DAY_OF_YEAR, new ExtractAsFunctionOperatorRenderer("DAYOFYEAR"));
		registerOperatorRender(DateOperatorDefinition.FROM_UNIXTIME, new MySQLDateEpochOperatorRenderer(DateEpochOperatorRenderer.FROM));
		registerOperatorRender(DateOperatorDefinition.TO_UNIXTIME, new MySQLDateEpochOperatorRenderer(DateEpochOperatorRenderer.TO));
		//
		registerOperatorRender(TruncateOperatorDefintion.TRUNCATE, new MySQLTruncateOperatorRenderer());
		registerOperatorRender(RandOperatorDefinition.RAND, new MySQLRandOperatorRenderer());
		registerOperatorRender(SinhCoshTanhOperatorDefintion.SINH, new MySQLPosgresSinhOperatorRenderer());
		registerOperatorRender(SinhCoshTanhOperatorDefintion.COSH, new MySQLPosgresCoshOperatorRenderer());
		registerOperatorRender(SinhCoshTanhOperatorDefintion.TANH, new MySQLPosgresTanhOperatorRenderer());
		//
		registerOperatorRender(AddMonthsOperatorDefinition.ADD_MONTHS, new AddMonthsAsIntervalOperatorRenderer());
		//
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.AVG), new MySQLAvgRenderer());
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.VARIANCE), new MySQLVarStdevRenderer());
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.VAR_SAMP), new MySQLVarStdevRenderer());
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.STDDEV_POP), new MySQLVarStdevRenderer());
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.STDDEV_SAMP), new MySQLVarStdevRenderer());
		//
		registerOperatorRender(DateTruncateOperatorDefinition.DATE_TRUNCATE, new MySQLDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.HOURLY_ID, new MySQLDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.DAILY_ID, new MySQLDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.WEEKLY_ID, new MySQLDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.MONTHLY_ID, new MySQLDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.QUARTERLY_ID, new MySQLDateTruncateOperatorRenderer());
		registerOperatorRender(DateTruncateShortcutsOperatorDefinition.YEARLY_ID, new MySQLDateTruncateOperatorRenderer());
		//
		registerOperatorRender(IntrinsicOperators.INTRINSIC_EXTENDED_ID + "." + IntrinsicOperators.RLIKE, new RLikeOperatorRenderer("RLIKE"));
		//
		registerOperatorRender(JSONOperatorDefinition.JSON_ARRAY_LENGTH, new MySQLJSONOperatorRenderer());
		registerOperatorRender(JSONOperatorDefinition.JSON_EXTRACT_FROM_ARRAY, new MySQLJSONOperatorRenderer());
		registerOperatorRender(JSONOperatorDefinition.JSON_EXTRACT_PATH_TEXT, new MySQLJSONOperatorRenderer());
		//
		unregisterOperatorRender(RegexpOperatorDefinition.REGEXP_COUNT);
		unregisterOperatorRender(RegexpOperatorDefinition.REGEXP_INSTR);
		unregisterOperatorRender(RegexpOperatorDefinition.REGEXP_SUBSTR);
		unregisterOperatorRender(RegexpOperatorDefinition.REGEXP_REPLACE);
		unregisterOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.MEDIAN));

	}

	@Override
	public double computeAccuracy(DatabaseProduct product) {
		try {
			if (product != null) {
				if (IMetadataEngine.MYSQL_NAME.equalsIgnoreCase(product.getProductName())) {
					return PERFECT_MATCH;
				} else {
					return NOT_APPLICABLE;
				}
			} else {
				return NOT_APPLICABLE;
			}
		} catch (Exception e) {
			return NOT_APPLICABLE;
		}
	}

	@Override
	public SQLSkin createSkin(DatabaseProduct product) {
		return new MySQLSkin(this, product);
	}

	@Override
	public ISkinFeatureSupport getFeatureSupport(DefaultJDBCSkin skin, String featureID) {
		if (featureID == ZeroIfNullFeatureSupport.ID) {
			return zeroIfNull;
		} else if (featureID == DataSourceReliable.FeatureSupport.GROUPBY_ALIAS) {
			return ISkinFeatureSupport.IS_SUPPORTED;
		} else if (featureID == SelectStatement.SampleFeatureSupport.SELECT_SAMPLE) {
			return SAMPLE_SUPPORT;
		} else if (featureID == MetatdataSearchFeatureSupport.METADATA_SEARCH_FEATURE_ID) {
			return METADATA_SEARCH_SUPPORT;
		} else if (featureID == IGroupingSetSupport.ID) {
			return IGroupingSetSupport.IS_NOT_SUPPORTED;
		} else if (featureID == DataSourceReliable.FeatureSupport.AUTOCOMMIT) {
			return ISkinFeatureSupport.IS_SUPPORTED;
		} else if (featureID == IMetadataForeignKeySupport.ID) {
			return ISkinFeatureSupport.IS_SUPPORTED;
		} else if (featureID == IMetadataPrimaryKeySupport.ID) {
			return ISkinFeatureSupport.IS_SUPPORTED;
		} else if (featureID.equals(IRollupStrategySupport.ID)) {
			return IRollupStrategySupport.DO_NOT_OPTIMIZE_STRATEGY;
		}
		// else
		return super.getFeatureSupport(skin, featureID);
	}

	private SelectStatement.SampleFeatureSupport SAMPLE_SUPPORT = new SelectStatement.SampleFeatureSupport() {

		@Override
		public boolean isCountSupported() {
			return false;
		}

		@Override
		public boolean isPercentageSupported() {
			return true;
		}

	};

	private MetatdataSearchFeatureSupport METADATA_SEARCH_SUPPORT = new MetatdataSearchFeatureSupport() {
		@Override
		public String createTableSearch(List<String> schemas, String tableName, boolean isCaseSensitive) {
			StringBuilder sqlCode = new StringBuilder();
			sqlCode.append("SELECT TABLE_SCHEMA,TABLE_NAME,TABLE_COMMENT");
			sqlCode.append(CR_LF);
			sqlCode.append(" FROM INFORMATION_SCHEMA.TABLES TABS");
			sqlCode.append(CR_LF);
			sqlCode.append(" WHERE TABLE_SCHEMA IN (" + getGroupSchemaNames(schemas) + ")");
			sqlCode.append(CR_LF);
			sqlCode.append(" AND (" + applyCaseSensitive("TABLE_NAME", isCaseSensitive) + " LIKE " + applyCaseSensitive(tableName, isCaseSensitive) + " OR "
					+ applyCaseSensitive("TABLE_COMMENT", isCaseSensitive) + " LIKE " + applyCaseSensitive(tableName, isCaseSensitive) + ") ");
			sqlCode.append(CR_LF);
			sqlCode.append(" ORDER BY 1, 2");
			return sqlCode.toString();
		}

		@Override
		public String createColumnSearch(List<String> schemas, String tableName, String columnName, boolean isCaseSensitive) {
			StringBuilder sqlCode = new StringBuilder();
			sqlCode.append("SELECT TABLE_SCHEMA, TABLE_NAME,COLUMN_NAME,COLUMN_COMMENT");
			sqlCode.append(CR_LF);
			sqlCode.append(" FROM INFORMATION_SCHEMA.COLUMNS COLS");
			sqlCode.append(CR_LF);
			sqlCode.append(" WHERE TABLE_SCHEMA IN (" + getGroupSchemaNames(schemas) + ")");
			sqlCode.append(CR_LF);
			sqlCode.append(" AND (" + applyCaseSensitive("COLUMN_NAME", isCaseSensitive) + " LIKE " + applyCaseSensitive(columnName, isCaseSensitive) + " OR "
					+ applyCaseSensitive("COLUMN_COMMENT", isCaseSensitive) + " LIKE " + applyCaseSensitive(columnName, isCaseSensitive) + ")");
			sqlCode.append(CR_LF);
			if (tableName != null) {
				sqlCode.append(" AND (" + applyCaseSensitive("TABLE_NAME", isCaseSensitive) + " LIKE " + applyCaseSensitive(tableName, isCaseSensitive) + " OR "
						+ applyCaseSensitive("TABLE_NAME", isCaseSensitive) + " LIKE " + applyCaseSensitive(tableName, isCaseSensitive) + ") ");
				sqlCode.append(CR_LF);
			}
			sqlCode.append(" ORDER BY 1, 2,3");
			return sqlCode.toString();
		}

	};

	@Override
	public String getSkinPrefix(DatabaseProduct product) {
		return "mysql";
	}

	@Override
	public ISkinProvider getParentSkinProvider() {
		return SkinRegistry.INSTANCE.findSkinProvider(DefaultSkinProvider.class);
	}

}
