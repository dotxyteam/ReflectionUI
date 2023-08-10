package xy.reflect.ui.info.type.factory;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.BasicTypeInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.JDBCInfoFactory.Parameter.Kind;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.ListTypeInfoProxy;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class JDBCInfoFactory {

	protected ReflectionUI reflectionUI;

	public JDBCInfoFactory(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	public ITypeInfoSource getTypeInfoSource(final Object object) {
		if (object instanceof Database) {
			return new DatabaseTypeInfoSource((Database) object);
		} else if (object instanceof Catalog) {
			return new DatabaseCatalogTypeInfoSource((Catalog) object);
		} else if (object instanceof Schema) {
			return new CatalogSchemaTypeInfoSource((Schema) object);
		} else if (object instanceof Table) {
			return new TableTypeInfoSource((Table) object);
		} else if (object instanceof Row) {
			return new RowTypeInfoSource(((Row) object).table);
		} else {
			return null;
		}
	}

	public class RowTypeInfoSource implements ITypeInfoSource {

		protected Table table;

		public RowTypeInfoSource(Table table) {
			this.table = table;
		}

		@Override
		public ITypeInfo getTypeInfo() {
			return new RowTypeInfo(table);
		}

		@Override
		public SpecificitiesIdentifier getSpecificitiesIdentifier() {
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RowTypeInfoSource other = (RowTypeInfoSource) obj;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "RowTypeInfoSource [table=" + table + "]";
		}
	}

	public class RowTypeInfo extends BasicTypeInfoProxy {

		protected Table table;

		public RowTypeInfo(Table table) {
			super(ITypeInfo.NULL_BASIC_TYPE_INFO);
			this.table = table;
		}

		@Override
		public String getName() {
			return "RowTypeInfo [table=" + table.getName() + ", schema=" + table.schema.getName() + ", catalog="
					+ table.schema.catalog.getName() + "]";
		}

		@Override
		public String getCaption() {
			return table.getName() + " Row";
		}

		@Override
		public String toString(Object object) {
			return table.getName() + " Row N°" + (((Row) object).getNumber() + 1);
		}

		@Override
		public boolean supports(Object object) {
			return (object instanceof Row) && ((Row) object).table.equals(table);
		}

		@Override
		public ITypeInfoSource getSource() {
			return new RowTypeInfoSource(table);
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (Column colum : table.getColumns()) {
				result.add(new ColumnFieldInfo(colum));
			}
			return result;
		}

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.singletonList(new AbstractConstructorInfo() {

				@Override
				public Object invoke(Object object, InvocationData invocationData) {
					return new Row(table, Row.NEW_ROW_NUMBER);
				}

				@Override
				public ITypeInfo getReturnValueType() {
					return reflectionUI.buildTypeInfo(new RowTypeInfoSource(table));
				}
			});
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			RowTypeInfo other = (RowTypeInfo) obj;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "RowTypeInfo [table=" + table + "]";
		}

	}

	public class CatalogSchemaFieldInfo extends FieldInfoProxy {

		protected Schema schema;

		public CatalogSchemaFieldInfo(Schema schema) {
			super(IFieldInfo.NULL_FIELD_INFO);
			this.schema = schema;
		}

		@Override
		public String getCaption() {
			if (schema.getName() == null) {
				return "";
			} else {
				return schema.getName();
			}
		}

		@Override
		public String getName() {
			if (schema.getName() == null) {
				return "<null>";
			} else {
				return schema.getName();
			}
		}

		@Override
		public Object getValue(Object object) {
			return schema;
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.buildTypeInfo(new CatalogSchemaTypeInfoSource(schema));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			CatalogSchemaFieldInfo other = (CatalogSchemaFieldInfo) obj;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CatalogSchemaFieldInfo [schema=" + schema + "]";
		}

	}

	public class DatabaseSQLExecutionMethodInfo extends MethodInfoProxy {

		protected Database database;

		public DatabaseSQLExecutionMethodInfo(Database database) {
			super(IMethodInfo.NULL_METHOD_INFO);
			this.database = database;
		}

		@Override
		public String getName() {
			return "executeSQL";
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.singletonList(new ParameterInfoProxy(IParameterInfo.NULL_PARAMETER_INFO) {

				@Override
				public String getName() {
					return "sql";
				}

				@Override
				public String getCaption() {
					return "SQL";
				}

				@Override
				public ITypeInfo getType() {
					return reflectionUI.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, String.class, null));
				}

			});
		}

		@Override
		public String getSignature() {
			return ReflectionUIUtils.buildMethodSignature(this);
		}

		@Override
		public String getCaption() {
			return "Execute SQL";
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return null;
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			database.executeSQL((String) invocationData.getParameterValue(0));
			return null;
		}

	}

	public class SchemaTableMethodInfo extends MethodInfoProxy {

		protected Table table;

		public SchemaTableMethodInfo(Table table) {
			super(IMethodInfo.NULL_METHOD_INFO);
			this.table = table;
		}

		@Override
		public String getName() {
			return table.getName();
		}

		@Override
		public String getSignature() {
			return ReflectionUIUtils.buildMethodSignature(this);
		}

		@Override
		public String getCaption() {
			return "Table " + table.getName();
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return reflectionUI.buildTypeInfo(new TableTypeInfoSource(table));
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return table;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SchemaTableMethodInfo other = (SchemaTableMethodInfo) obj;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SchemaTableMethodInfo [table=" + table + "]";
		}

	}

	public class SchemaProcedureMethodInfo extends MethodInfoProxy {

		protected Procedure procedure;

		public SchemaProcedureMethodInfo(Procedure procedure) {
			super(IMethodInfo.NULL_METHOD_INFO);
			this.procedure = procedure;
		}

		@Override
		public String getName() {
			return procedure.getName();
		}

		@Override
		public String getSignature() {
			return procedure.getSignature();
		}

		@Override
		public String getCaption() {
			return "Procedure " + procedure.getName();
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return reflectionUI.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, Procedure.class, null));
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return procedure;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((procedure == null) ? 0 : procedure.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SchemaProcedureMethodInfo other = (SchemaProcedureMethodInfo) obj;
			if (procedure == null) {
				if (other.procedure != null)
					return false;
			} else if (!procedure.equals(other.procedure))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SchemaProcedureMethodInfo [procedure=" + procedure + "]";
		}

	}

	public class DatabaseCatalogFieldInfo extends FieldInfoProxy {

		protected Catalog catalog;

		public DatabaseCatalogFieldInfo(Catalog catalog) {
			super(IFieldInfo.NULL_FIELD_INFO);
			this.catalog = catalog;
		}

		@Override
		public String getCaption() {
			if (catalog.getName() == null) {
				return "";
			} else {
				return catalog.getName();
			}
		}

		@Override
		public String getName() {
			if (catalog.getName() == null) {
				return "<null>";
			} else {
				return catalog.getName();
			}
		}

		@Override
		public Object getValue(Object object) {
			return catalog;
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.buildTypeInfo(new DatabaseCatalogTypeInfoSource(catalog));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((catalog == null) ? 0 : catalog.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			DatabaseCatalogFieldInfo other = (DatabaseCatalogFieldInfo) obj;
			if (catalog == null) {
				if (other.catalog != null)
					return false;
			} else if (!catalog.equals(other.catalog))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DatabaseCatalogFieldInfo [catalog=" + catalog + "]";
		}

	}

	public class ColumnFieldInfo extends FieldInfoProxy {

		protected Column column;

		public ColumnFieldInfo(Column column) {
			super(IFieldInfo.NULL_FIELD_INFO);
			this.column = column;
		}

		@Override
		public String getCaption() {
			return column.getName();
		}

		@Override
		public String getName() {
			return column.getName();
		}

		@Override
		public Object getValue(Object object) {
			Object result = ((Row) object).getCell(column.getName()).getValue();
			if (result == null) {
				return null;
			}
			if (column.getJavaType() == java.sql.Date.class) {
				result = new java.util.Date(((java.sql.Date) result).getTime());
			}
			if (column.getJavaType() == java.sql.Time.class) {
				result = new java.util.Date(((java.sql.Time) result).getTime());
			}
			if (column.getJavaType() == java.sql.Timestamp.class) {
				result = new java.util.Date(((java.sql.Timestamp) result).getTime());
			}
			return result;
		}

		@Override
		public void setValue(Object object, Object value) {
			if (value != null) {
				if (column.getJavaType() == java.sql.Date.class) {
					value = new java.sql.Date(((java.util.Date) value).getTime());
				}
				if (column.getJavaType() == java.sql.Time.class) {
					value = new java.sql.Time(((java.util.Date) value).getTime());
				}
				if (column.getJavaType() == java.sql.Timestamp.class) {
					value = new java.sql.Timestamp(((java.util.Date) value).getTime());
				}
			}
			((Row) object).getCell(column.getName()).setValue(value);
		}

		@Override
		public ITypeInfo getType() {
			Class<?> javaType = column.getJavaType();
			if (javaType == java.sql.Date.class) {
				javaType = java.util.Date.class;
			}
			if (javaType == java.sql.Time.class) {
				javaType = java.util.Date.class;
			}
			if (javaType == java.sql.Timestamp.class) {
				javaType = java.util.Date.class;
			}
			return reflectionUI
					.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, javaType, new SpecificitiesIdentifier(
							new RowTypeInfo(column.table).getName(), ColumnFieldInfo.this.getName())));
		}

		@Override
		public boolean isGetOnly() {
			return column.isAutoIncrement();
		}

		@Override
		public boolean isNullValueDistinct() {
			return column.isNullable();
		}

		@Override
		public boolean hasValueOptions(Object object) {
			return column.getForeignKeyOriginColumn() != null;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			Column foreignColumn = column.getForeignKeyOriginColumn();
			List<Object> result = new ArrayList<Object>();
			for (Row row : foreignColumn.table.getRows()) {
				result.add(row.getCell(foreignColumn.getName()).getValue());
			}
			return result.toArray();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((column == null) ? 0 : column.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ColumnFieldInfo other = (ColumnFieldInfo) obj;
			if (column == null) {
				if (other.column != null)
					return false;
			} else if (!column.equals(other.column))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ColumnFieldInfo [column=" + column + "]";
		}

	}

	public class CatalogSchemaTypeInfoSource implements ITypeInfoSource {

		protected Schema schema;

		public CatalogSchemaTypeInfoSource(Schema schema) {
			this.schema = schema;
		}

		@Override
		public ITypeInfo getTypeInfo() {
			return new CatalogSchemaTypeInfo(schema);
		}

		@Override
		public SpecificitiesIdentifier getSpecificitiesIdentifier() {
			return new SpecificitiesIdentifier(new DatabaseCatalogTypeInfo(schema.catalog).getName(),
					new CatalogSchemaFieldInfo(schema).getName());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CatalogSchemaTypeInfoSource other = (CatalogSchemaTypeInfoSource) obj;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CatalogSchemaTypeInfoSource [schema=" + schema + "]";
		}

	}

	public class DatabaseCatalogTypeInfoSource implements ITypeInfoSource {

		protected Catalog catalog;

		public DatabaseCatalogTypeInfoSource(Catalog catalog) {
			this.catalog = catalog;
		}

		@Override
		public ITypeInfo getTypeInfo() {
			return new DatabaseCatalogTypeInfo(catalog);
		}

		@Override
		public SpecificitiesIdentifier getSpecificitiesIdentifier() {
			return new SpecificitiesIdentifier(new DatabaseTypeInfo(catalog.database).getName(),
					new DatabaseCatalogFieldInfo(catalog).getName());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((catalog == null) ? 0 : catalog.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DatabaseCatalogTypeInfoSource other = (DatabaseCatalogTypeInfoSource) obj;
			if (catalog == null) {
				if (other.catalog != null)
					return false;
			} else if (!catalog.equals(other.catalog))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DatabaseCatalogTypeInfoSource [catalog=" + catalog + "]";
		}

	}

	public class DatabaseTypeInfoSource implements ITypeInfoSource {

		protected Database database;

		public DatabaseTypeInfoSource(Database database) {
			this.database = database;
		}

		@Override
		public ITypeInfo getTypeInfo() {
			return new DatabaseTypeInfo(database);
		}

		@Override
		public SpecificitiesIdentifier getSpecificitiesIdentifier() {
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((database == null) ? 0 : database.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DatabaseTypeInfoSource other = (DatabaseTypeInfoSource) obj;
			if (database == null) {
				if (other.database != null)
					return false;
			} else if (!database.equals(other.database))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DatabaseTypeInfoSource [database=" + database + "]";
		}

	}

	public class TableTypeInfoSource implements ITypeInfoSource {

		protected Table table;

		public TableTypeInfoSource(Table table) {
			this.table = table;
		}

		@Override
		public ITypeInfo getTypeInfo() {
			return new TableTypeInfo(table);
		}

		@Override
		public SpecificitiesIdentifier getSpecificitiesIdentifier() {
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TableTypeInfoSource other = (TableTypeInfoSource) obj;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TableTypeInfoSource [table=" + table + "]";
		}
	}

	public class CatalogSchemaTypeInfo extends BasicTypeInfoProxy {

		protected Schema schema;

		public CatalogSchemaTypeInfo(Schema schema) {
			super(ITypeInfo.NULL_BASIC_TYPE_INFO);
			this.schema = schema;
		}

		@Override
		public String getName() {
			return "CatalogSchemaTypeInfo [schema=" + schema.getName() + ", catalog=" + schema.catalog.getName() + "]";
		}

		@Override
		public String getCaption() {
			return schema.getName();
		}

		@Override
		public boolean supports(Object object) {
			return schema.equals(object);
		}

		@Override
		public ITypeInfoSource getSource() {
			return new CatalogSchemaTypeInfoSource(schema);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (Table table : schema.getTables()) {
				result.add(new SchemaTableMethodInfo(table));
			}
			for (Procedure procedure : schema.getProcedures()) {
				result.add(new SchemaProcedureMethodInfo(procedure));
			}
			return result;
		}

		@Override
		public String toString(Object object) {
			return schema.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CatalogSchemaTypeInfo other = (CatalogSchemaTypeInfo) obj;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CatalogSchemaTypeInfo [schema=" + schema + "]";
		}

	}

	public class DatabaseCatalogTypeInfo extends BasicTypeInfoProxy {

		protected Catalog catalog;

		public DatabaseCatalogTypeInfo(Catalog catalog) {
			super(ITypeInfo.NULL_BASIC_TYPE_INFO);
			this.catalog = catalog;
		}

		@Override
		public String getName() {
			return "DatabaseCatalogTypeInfo [catalog=" + catalog.getName() + "]";
		}

		@Override
		public String getCaption() {
			return catalog.getName();
		}

		@Override
		public boolean supports(Object object) {
			return catalog.equals(object);
		}

		@Override
		public ITypeInfoSource getSource() {
			return new DatabaseCatalogTypeInfoSource(catalog);
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (Schema schema : catalog.getSchemas()) {
				result.add(new CatalogSchemaFieldInfo(schema));
			}
			return result;
		}

		@Override
		public String toString(Object object) {
			return catalog.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((catalog == null) ? 0 : catalog.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DatabaseCatalogTypeInfo other = (DatabaseCatalogTypeInfo) obj;
			if (catalog == null) {
				if (other.catalog != null)
					return false;
			} else if (!catalog.equals(other.catalog))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DatabaseCatalogTypeInfo [catalog=" + catalog + "]";
		}

	}

	public class DatabaseTypeInfo extends BasicTypeInfoProxy {

		protected Database database;

		public DatabaseTypeInfo(Database database) {
			super(ITypeInfo.NULL_BASIC_TYPE_INFO);
			this.database = database;
		}

		@Override
		public String getName() {
			return "DatabaseTypeInfo []";
		}

		@Override
		public String getCaption() {
			return database.toString();
		}

		@Override
		public boolean supports(Object object) {
			return database.equals(object);
		}

		@Override
		public ITypeInfoSource getSource() {
			return new DatabaseTypeInfoSource(database);
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (Catalog catalog : database.getCatalogs()) {
				result.add(new DatabaseCatalogFieldInfo(catalog));
			}
			return result;
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.singletonList(new DatabaseSQLExecutionMethodInfo(database));
		}

		@Override
		public String toString(Object object) {
			return database.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((database == null) ? 0 : database.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			DatabaseTypeInfo other = (DatabaseTypeInfo) obj;
			if (database == null) {
				if (other.database != null)
					return false;
			} else if (!database.equals(other.database))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DatabaseTypeInfo [database=" + database + "]";
		}

	}

	public class TableTypeInfo extends BasicTypeInfoProxy {

		protected Table table;

		public TableTypeInfo(Table table) {
			super(ITypeInfo.NULL_BASIC_TYPE_INFO);
			this.table = table;
		}

		@Override
		public String getName() {
			return "TableTypeInfo [table=" + table.getName() + ", schema=" + table.schema.getName() + ", catalog="
					+ table.schema.catalog.getName() + "]";
		}

		@Override
		public String getCaption() {
			return table.getName();
		}

		@Override
		public String toString(Object object) {
			return "Table " + table.getName();
		}

		@Override
		public boolean supports(Object object) {
			return table.equals(object);
		}

		@Override
		public ITypeInfoSource getSource() {
			return new TableTypeInfoSource(table);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.singletonList(new TableRowsFieldInfo(table));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			TableTypeInfo other = (TableTypeInfo) obj;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TableTypeInfo [table=" + table + "]";
		}

	}

	public class TableRowsFieldInfo extends FieldInfoProxy {

		protected Table table;

		public TableRowsFieldInfo(Table table) {
			super(IFieldInfo.NULL_FIELD_INFO);
			this.table = table;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public String getName() {
			return "rows";
		}

		@Override
		public Object getValue(Object object) {
			return ((Table) object).getRows();
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.buildTypeInfo(new TableRowsFieldTypeInfoSource(table));
		}

		@Override
		public boolean isTransient() {
			return true;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.DIRECT_OR_PROXY;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			TableRowsFieldInfo other = (TableRowsFieldInfo) obj;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TableRowsFieldInfo [table=" + table + "]";
		}
	}

	public class TableRowsFieldTypeInfo extends ListTypeInfoProxy {

		protected Table table;

		public TableRowsFieldTypeInfo(Table table) {
			super(IListTypeInfo.NULL_LIST_TYPE_INFO);
			this.table = table;
		}

		@Override
		public String getCaption() {
			return ReflectionUIUtils.identifierToCaption(getName());
		}

		@Override
		public String getName() {
			return "TableRowsFieldTypeInfo [table=" + table.getName() + ", schema=" + table.schema.getName()
					+ ", catalog=" + table.schema.catalog.getName() + "]";
		}

		@Override
		public String toString(Object object) {
			return table.getRows().size() + " Row(s)";
		}

		@Override
		public boolean supports(Object object) {
			return object instanceof List;
		}

		@Override
		public Object[] toArray(Object listValue) {
			return ((List<?>) listValue).toArray();
		}

		@Override
		public ITypeInfoSource getSource() {
			return new TableRowsFieldTypeInfoSource(table);
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return new TableRowsFieldTypeStructuralInfo(table);
		}

		@Override
		public boolean isInsertionAllowed() {
			return true;
		}

		@Override
		public boolean isRemovalAllowed() {
			return true;
		}

		@Override
		public boolean canReplaceContent() {
			return true;
		}

		@Override
		public void replaceContent(Object listValue, Object[] array) {
			List<Row> replacingRows = new ArrayList<Row>();
			for (Object object : array) {
				Row row = (Row) object;
				replacingRows.add(row);
			}

			List<Row> rowsToDelete = new ArrayList<Row>(table.getRows());
			rowsToDelete.removeAll(replacingRows);
			Collections.reverse(rowsToDelete);

			List<Row> rowsToInsert = new ArrayList<Row>(replacingRows);
			rowsToInsert.removeAll(table.getRows());

			for (Row row : rowsToDelete) {
				table.deleteRow(row.getNumber());
			}
			for (Row row : rowsToInsert) {
				Map<String, Object> newCellValues = new HashMap<String, Object>();
				for (Column column : table.getColumns()) {
					newCellValues.put(column.getName(), row.getCell(column.getName()).getValue());
				}
				table.insertRow(newCellValues);
			}
		}

		@Override
		public ITypeInfo getItemType() {
			return reflectionUI.buildTypeInfo(new RowTypeInfoSource(table));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			TableRowsFieldTypeInfo other = (TableRowsFieldTypeInfo) obj;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TableRowsFieldTypeInfo [table=" + table + "]";
		}

	}

	public class TableRowsFieldTypeStructuralInfo implements IListStructuralInfo {

		protected Table table;

		public TableRowsFieldTypeStructuralInfo(Table table) {
			this.table = table;
		}

		@Override
		public int getLength() {
			return -1;
		}

		@Override
		public List<IColumnInfo> getColumns() {
			List<IColumnInfo> result = new ArrayList<IColumnInfo>();
			for (Column column : table.getColumns()) {
				result.add(new TableColumnInfo(column));
			}
			return result;
		}

		@Override
		public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
			return null;
		}

		@Override
		public IInfoFilter getItemInfoFilter(ItemPosition itemPosition) {
			return IInfoFilter.DEFAULT;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TableRowsFieldTypeStructuralInfo other = (TableRowsFieldTypeStructuralInfo) obj;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TableRowsFieldTypeStructuralInfo [table=" + table + "]";
		}

	}

	public class TableColumnInfo implements IColumnInfo {

		protected Column column;

		public TableColumnInfo(Column column) {
			this.column = column;
		}

		@Override
		public String getName() {
			return column.getName();
		}

		@Override
		public String getCaption() {
			return column.getName();
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String getCellValue(ItemPosition itemPosition) {
			Row row = (Row) itemPosition.getItem();
			Cell cell = row.getCell(column.getName());
			return (cell.getValue() == null) ? "" : cell.getValue().toString();
		}

		@Override
		public boolean hasCellValue(ItemPosition itemPosition) {
			return true;
		}

		@Override
		public int getMinimalCharacterCount() {
			return 20;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((column == null) ? 0 : column.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TableColumnInfo other = (TableColumnInfo) obj;
			if (column == null) {
				if (other.column != null)
					return false;
			} else if (!column.equals(other.column))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TableColumnInfo [column=" + column + "]";
		}

	}

	public class TableRowsFieldTypeInfoSource implements ITypeInfoSource {

		protected Table table;

		public TableRowsFieldTypeInfoSource(Table table) {
			this.table = table;
		}

		@Override
		public ITypeInfo getTypeInfo() {
			return new TableRowsFieldTypeInfo(table);
		}

		@Override
		public SpecificitiesIdentifier getSpecificitiesIdentifier() {
			return new SpecificitiesIdentifier(new TableTypeInfo(table).getName(),
					new TableRowsFieldInfo(table).getName());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TableRowsFieldTypeInfoSource other = (TableRowsFieldTypeInfoSource) obj;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TableRowsFieldTypeInfoSource [table=" + table + "]";
		}
	}

	/*
	 * --------------------------------------------------------
	 */

	public static class Database {

		protected String connectionURL;
		protected Connection connection;
		protected Object connectionMutex = new Object();
		protected List<Catalog> catalogs;

		public Database(String connectionURL) {
			this.connectionURL = connectionURL;
			try {
				connection = DriverManager.getConnection(connectionURL);
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public Database(String connectionURL, String user, String password) {
			this.connectionURL = connectionURL;
			try {
				connection = DriverManager.getConnection(connectionURL, user, password);
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public String getConnectionURL() {
			return connectionURL;
		}

		public void executeSQL(String sql) {
			synchronized (connectionMutex) {
				try {
					connection.createStatement().execute(sql);
				} catch (SQLException e) {
					throw new ReflectionUIError(e);
				}
			}
		}

		public List<Catalog> getCatalogs() {
			try {
				synchronized (connectionMutex) {
					if (catalogs == null) {
						List<Catalog> result = new ArrayList<Catalog>();
						Catalog nullCatalog = new Catalog(this, null);
						if (!nullCatalog.isEmpty()) {
							result.add(nullCatalog);
						}
						DatabaseMetaData metadata = connection.getMetaData();
						ResultSet rs = metadata.getCatalogs();
						while (rs.next()) {
							String catalogName = rs.getString("TABLE_CAT");
							Catalog newCatalog = new Catalog(this, catalogName);
							if (!newCatalog.isEmpty()) {
								result.add(newCatalog);
							}
						}
						catalogs = result;
					}
					return catalogs;
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public Catalog getCatalog(String name) {
			for (Catalog candidate : getCatalogs()) {
				if (MiscUtils.equalsOrBothNull(name, candidate.getName())) {
					return candidate;
				}
			}
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((connectionURL == null) ? 0 : connectionURL.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Database other = (Database) obj;
			if (connectionURL == null) {
				if (other.connectionURL != null)
					return false;
			} else if (!connectionURL.equals(other.connectionURL))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DatabaseManagedSystem [connectionURL=" + connectionURL + "]";
		}

	}

	public static class Catalog {

		protected Database database;
		protected String name;
		protected List<Schema> schemas;

		public Catalog(Database databaseManagedSystem, String name) {
			this.database = databaseManagedSystem;
			this.name = name;
		}

		public boolean isEmpty() {
			return getSchemas().size() == 0;
		}

		public String getName() {
			return name;
		}

		public List<Schema> getSchemas() {
			try {
				synchronized (database.connectionMutex) {
					if (schemas == null) {
						List<Schema> result = new ArrayList<Schema>();
						Schema nullSchema = new Schema(this, null);
						if (!nullSchema.isEmpty()) {
							result.add(nullSchema);
						}
						DatabaseMetaData metadata = database.connection.getMetaData();
						ResultSet rs = metadata.getSchemas(name, "%");
						while (rs.next()) {
							if (name == null) {
								String catalogName = rs.getString("TABLE_CATALOG");
								if (catalogName != null) {
									continue;
								}
							}
							String schemaName = rs.getString("TABLE_SCHEM");
							Schema newSchema = new Schema(this, schemaName);
							if (!newSchema.isEmpty()) {
								result.add(newSchema);
							}
						}
						schemas = result;
					}
					return schemas;
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public Schema getSchema(String name) {
			for (Schema candidate : getSchemas()) {
				if (MiscUtils.equalsOrBothNull(name, candidate.getName())) {
					return candidate;
				}
			}
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((database == null) ? 0 : database.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Catalog other = (Catalog) obj;
			if (database == null) {
				if (other.database != null)
					return false;
			} else if (!database.equals(other.database))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Catalog [name=" + name + ", databaseManagedSystem=" + database + "]";
		}

	}

	public static class Schema {

		protected Catalog catalog;
		protected String name;
		protected List<Table> tables;
		protected List<Procedure> procedures;

		public Schema(Catalog catalog, String name) {
			this.catalog = catalog;
			this.name = name;
		}

		public boolean isEmpty() {
			return (getTables().size() == 0) && (getProcedures().size() == 0);
		}

		public String getName() {
			return name;
		}

		public List<Table> getTables() {
			try {
				synchronized (catalog.database.connectionMutex) {
					if (tables == null) {
						List<Table> result = new ArrayList<Table>();
						DatabaseMetaData metadata = catalog.database.connection.getMetaData();
						ResultSet rs = metadata.getTables(catalog.getName(), name, "%", null);
						while (rs.next()) {
							if (catalog.getName() == null) {
								String catalogName = rs.getString("TABLE_CAT");
								if (catalogName != null) {
									continue;
								}
							}
							if (name == null) {
								String schemaName = rs.getString("TABLE_SCHEM");
								if (schemaName != null) {
									continue;
								}
							}
							String tableName = rs.getString("TABLE_NAME");
							result.add(new Table(this, tableName));
						}
						tables = result;
					}
					return tables;
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public Table getTable(String name) {
			for (Table candidate : getTables()) {
				if (MiscUtils.equalsOrBothNull(name, candidate.getName())) {
					return candidate;
				}
			}
			return null;
		}

		public List<Procedure> getProcedures() {
			try {
				synchronized (catalog.database.connectionMutex) {
					if (procedures == null) {
						List<Procedure> result = new ArrayList<Procedure>();
						DatabaseMetaData metadata = catalog.database.connection.getMetaData();
						ResultSet rs = metadata.getProcedures(catalog.getName(), name, "%");
						while (rs.next()) {
							if (catalog.getName() == null) {
								String catalogName = rs.getString("PROCEDURE_CAT");
								if (catalogName != null) {
									continue;
								}
							}
							if (name == null) {
								String schemaName = rs.getString("PROCEDURE_SCHEM");
								if (schemaName != null) {
									continue;
								}
							}
							String procedureName = rs.getString("PROCEDURE_NAME");
							result.add(new Procedure(this, procedureName));
						}
						procedures = result;
					}
					return procedures;
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public Procedure getProcedure(String signature) {
			for (Procedure candidate : getProcedures()) {
				if (MiscUtils.equalsOrBothNull(signature, candidate.getSignature())) {
					return candidate;
				}
			}
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((catalog == null) ? 0 : catalog.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Schema other = (Schema) obj;
			if (catalog == null) {
				if (other.catalog != null)
					return false;
			} else if (!catalog.equals(other.catalog))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Schema [name=" + name + ", catalog=" + catalog + "]";
		}

	}

	public static class Procedure {
		protected Schema schema;
		protected String name;
		protected List<Parameter> parameters;

		public Procedure(Schema schema, String name) {
			this.schema = schema;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getSignature() {
			String result = name;
			result += "(";
			int iParameter = 0;
			for (Parameter parameter : getParameters()) {
				if (iParameter > 0) {
					result += ", ";
				}
				result += parameter.getSignature();
				iParameter++;
			}
			result += ")";
			return result;

		}

		public List<Parameter> getParameters() {
			try {
				synchronized (schema.catalog.database.connectionMutex) {
					if (parameters == null) {
						List<Parameter> result = new ArrayList<Parameter>();
						DatabaseMetaData metadata = schema.catalog.database.connection.getMetaData();
						/*
						 * Potential issue: there may be many procedures that have the same name but a
						 * different signature. This case is not handled below.
						 */
						ResultSet rs = metadata.getProcedureColumns(schema.catalog.name, schema.name, name, "%");
						while (rs.next()) {
							String columnName = rs.getString("COLUMN_NAME");
							int columnPosition = rs.getInt("ORDINAL_POSITION");
							int sqlType = rs.getInt("DATA_TYPE");
							int parameterKind = rs.getInt("COLUMN_TYPE");
							result.add(new Parameter(this, columnName, columnPosition, sqlType,
									Parameter.Kind.fromInt(parameterKind)));
						}
						parameters = result;
					}
					return parameters;
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		protected String getNamePrefix() {
			try {
				String result = "";
				if (schema.catalog.getName() != null) {
					result += schema.catalog.getName()
							+ schema.catalog.database.connection.getMetaData().getCatalogSeparator();
				}
				if (schema.getName() != null) {
					result += schema.getName() + ".";
				}
				return result;
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public Map<String, Object> call(Map<String, Object> valueByInputParameter) {
			try {
				synchronized (schema.catalog.database.connectionMutex) {
					String callString = "{call ";
					callString += getNamePrefix() + name + "(";
					for (int i = 0; i < getParameters().size(); i++) {
						if (i > 0) {
							callString += ", ";
						}
						callString += "?";
						i++;
					}
					callString += ")}";
					CallableStatement callableStatement = schema.catalog.database.connection.prepareCall(callString);
					for (Parameter parameter : getParameters()) {
						if ((parameter.getKind() == Kind.IN) || (parameter.getKind() == Kind.INOUT)) {
							callableStatement.setObject(parameter.getName(),
									valueByInputParameter.get(parameter.getName()));
						}
						if ((parameter.getKind() == Kind.OUT) || (parameter.getKind() == Kind.INOUT)) {
							callableStatement.registerOutParameter(parameter.getName(), parameter.getSqlType());
						}
					}
					callableStatement.execute();
					Map<String, Object> result = new HashMap<String, Object>();
					for (Parameter parameter : getParameters()) {
						if ((parameter.getKind() == Kind.OUT) || (parameter.getKind() == Kind.INOUT)) {
							result.put(parameter.getName(), callableStatement.getObject(parameter.getName()));
						}
					}
					return result;
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
			result = prime * result + ((getSignature() == null) ? 0 : getSignature().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Procedure other = (Procedure) obj;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			if (getSignature() == null) {
				if (other.getSignature() != null)
					return false;
			} else if (!getSignature().equals(other.getSignature()))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Procedure [name=" + name + ", signature=" + getSignature() + ", schema=" + schema + "]";
		}

	}

	public static class Parameter {

		public static enum Kind {
			UNKNOWN, IN, INOUT, OUT, RETURNVALUE, RESULTSETVALUE;

			public static Kind fromInt(int intValue) {
				switch (intValue) {
				case DatabaseMetaData.procedureColumnIn:
					return IN;
				case DatabaseMetaData.procedureColumnInOut:
					return INOUT;
				case DatabaseMetaData.procedureColumnOut:
					return OUT;
				case DatabaseMetaData.procedureColumnReturn:
					return RETURNVALUE;
				case DatabaseMetaData.procedureColumnResult:
					return RESULTSETVALUE;
				default:
					return UNKNOWN;
				}
			}
		}

		protected Procedure procedure;
		protected String name;
		protected int position;
		protected int sqlType;
		protected Kind kind;

		public Parameter(Procedure procedure, String name, int position, int sqlType, Kind kind) {
			this.procedure = procedure;
			this.name = name;
			this.position = position;
			this.sqlType = sqlType;
			this.kind = kind;
		}

		public String getSignature() {
			return kind + " SQLTYpe" + sqlType + " " + name;
		}

		public String getName() {
			return name;
		}

		public int getPosition() {
			return position;
		}

		public int getSqlType() {
			return sqlType;
		}

		public Kind getKind() {
			return kind;
		}

		public Class<?> getJavaType() {
			// https://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
			switch (sqlType) {
			case Types.CHAR:
				return String.class;
			case Types.VARCHAR:
				return String.class;
			case Types.LONGVARCHAR:
				return String.class;
			case Types.NUMERIC:
				return BigDecimal.class;
			case Types.DECIMAL:
				return BigDecimal.class;
			case Types.BIT:
				return Boolean.class;
			case Types.TINYINT:
				return Byte.class;
			case Types.SMALLINT:
				return Short.class;
			case Types.INTEGER:
				return Integer.class;
			case Types.BIGINT:
				return Long.class;
			case Types.REAL:
				return Float.class;
			case Types.FLOAT:
				return Double.class;
			case Types.DOUBLE:
				return Double.class;
			case Types.BINARY:
				return byte[].class;
			case Types.VARBINARY:
				return byte[].class;
			case Types.LONGVARBINARY:
				return byte[].class;
			case Types.DATE:
				return java.sql.Date.class;
			case Types.TIME:
				return java.sql.Time.class;
			case Types.TIMESTAMP:
				return java.sql.Timestamp.class;
			case Types.CLOB:
				return Clob.class;
			case Types.BLOB:
				return Blob.class;
			case Types.ARRAY:
				return Array.class;
			case Types.DISTINCT:
				return Object.class;
			case Types.STRUCT:
				return Struct.class;
			case Types.REF:
				return Ref.class;
			case Types.JAVA_OBJECT:
				return Object.class;
			case Types.BOOLEAN:
				return Boolean.class;
			default:
				return Object.class;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((procedure == null) ? 0 : procedure.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Parameter other = (Parameter) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (procedure == null) {
				if (other.procedure != null)
					return false;
			} else if (!procedure.equals(other.procedure))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Parameter [name=" + name + ", position=" + position + ", kind=" + kind + ", javaType="
					+ getJavaType() + ", procedure=" + procedure + "]";
		}

	}

	public static class Table {

		protected Schema schema;
		protected String name;
		protected Statement rowListStatement;
		protected ResultSet rowListResultSet;
		protected List<Row> rows;
		protected List<Column> columns;

		public Table(Schema schema, String name) {
			this.schema = schema;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public List<Column> getColumns() {
			try {
				synchronized (schema.catalog.database.connectionMutex) {
					if (columns == null) {
						List<Column> result = new ArrayList<Column>();
						DatabaseMetaData metadata = schema.catalog.database.connection.getMetaData();
						ResultSet rs = metadata.getColumns(schema.catalog.name, schema.name, name, "%");
						while (rs.next()) {
							String columnName = rs.getString("COLUMN_NAME");
							int columnPosition = rs.getInt("ORDINAL_POSITION");
							int sqlType = rs.getInt("DATA_TYPE");
							boolean nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
							boolean autoIncrement = rs.getString("IS_AUTOINCREMENT").equals("YES");
							result.add(new Column(this, columnName, columnPosition, sqlType, nullable, autoIncrement));
						}
						columns = result;
					}
					return columns;
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public Column getColumn(String columnName) {
			for (Column column : getColumns()) {
				if (column.getName().equals(columnName)) {
					return column;
				}
			}
			return null;
		}

		public List<Row> getRows() {
			if (rows == null) {
				synchronized (schema.catalog.database.connectionMutex) {
					rows = new AbstractList<Row>() {
						{
							refresh();
						}

						@Override
						public Row get(int index) {
							int rowNumber = index + 1;
							return new Row(Table.this, rowNumber);
						}

						@Override
						public int size() {
							synchronized (schema.catalog.database.connectionMutex) {
								try {
									Statement rowCountStatement = schema.catalog.database.connection.createStatement(
											ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
									ResultSet countResultSet = rowCountStatement
											.executeQuery("select count(*) from " + getNamePrefix() + name);
									countResultSet.first();
									int result = countResultSet.getInt(1);
									return result;
								} catch (SQLException e) {
									throw new ReflectionUIError(e);
								}
							}
						}
					};
				}
			}
			return rows;
		}

		protected void refresh() {
			try {
				rowListStatement = schema.catalog.database.connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
				rowListResultSet = rowListStatement.executeQuery("select * from " + getNamePrefix() + name);
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		protected String getNamePrefix() {
			try {
				String result = "";
				if (schema.catalog.getName() != null) {
					result += schema.catalog.getName()
							+ schema.catalog.database.connection.getMetaData().getCatalogSeparator();
				}
				if (schema.getName() != null) {
					result += schema.getName() + ".";
				}
				return result;
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public Object getCellValue(int rowNumber, String columnName) {
			try {
				rowListResultSet.absolute(rowNumber);
				return rowListResultSet.getObject(columnName);
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public void setCellValue(int rowNumber, String columnName, Object value) {
			try {
				rowListResultSet.absolute(rowNumber);
				rowListResultSet.updateObject(columnName, value);
				rowListResultSet.updateRow();
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			} finally {
				refresh();
			}
		}

		public void insertRow(Map<String, Object> newCellValues) {
			try {
				rowListResultSet.moveToInsertRow();
				for (Column column : getColumns()) {
					rowListResultSet.updateObject(column.getName(), newCellValues.get(column.getName()));
				}
				rowListResultSet.insertRow();
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			} finally {
				refresh();
			}
		}

		public void deleteRow(int rowNumber) {
			try {
				rowListResultSet.absolute(rowNumber);
				rowListResultSet.deleteRow();
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			} finally {
				refresh();
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Table other = (Table) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Table [name=" + name + ", schema=" + schema + "]";
		}

	}

	public static class Column {

		protected Table table;
		protected String name;
		protected int position;
		protected int sqlType;
		protected boolean nullable;
		protected boolean autoIncrement;

		public Column(Table table, String name, int position, int sqlType, boolean nullable, boolean autoIncrement) {
			this.table = table;
			this.name = name;
			this.position = position;
			this.sqlType = sqlType;
			this.nullable = nullable;
			this.autoIncrement = autoIncrement;
		}

		public Object extractCellValue(ResultSet rs) {
			try {
				return rs.getObject(position);
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public String getName() {
			return name;
		}

		public int getPosition() {
			return position;
		}

		public Class<?> getJavaType() {
			// https://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
			switch (sqlType) {
			case Types.CHAR:
				return String.class;
			case Types.VARCHAR:
				return String.class;
			case Types.LONGVARCHAR:
				return String.class;
			case Types.NUMERIC:
				return BigDecimal.class;
			case Types.DECIMAL:
				return BigDecimal.class;
			case Types.BIT:
				return Boolean.class;
			case Types.TINYINT:
				return Byte.class;
			case Types.SMALLINT:
				return Short.class;
			case Types.INTEGER:
				return Integer.class;
			case Types.BIGINT:
				return Long.class;
			case Types.REAL:
				return Float.class;
			case Types.FLOAT:
				return Double.class;
			case Types.DOUBLE:
				return Double.class;
			case Types.BINARY:
				return byte[].class;
			case Types.VARBINARY:
				return byte[].class;
			case Types.LONGVARBINARY:
				return byte[].class;
			case Types.DATE:
				return java.sql.Date.class;
			case Types.TIME:
				return java.sql.Time.class;
			case Types.TIMESTAMP:
				return java.sql.Timestamp.class;
			case Types.CLOB:
				return Clob.class;
			case Types.BLOB:
				return Blob.class;
			case Types.ARRAY:
				return Array.class;
			case Types.DISTINCT:
				return Object.class;
			case Types.STRUCT:
				return Struct.class;
			case Types.REF:
				return Ref.class;
			case Types.JAVA_OBJECT:
				return Object.class;
			case Types.BOOLEAN:
				return Boolean.class;
			default:
				return Object.class;
			}
		}

		public boolean isAutoIncrement() {
			return autoIncrement;
		}

		public boolean isPrimaryKey() {
			try {
				synchronized (table.schema.catalog.database.connectionMutex) {
					DatabaseMetaData metadata = table.schema.catalog.database.connection.getMetaData();
					ResultSet rs = metadata.getPrimaryKeys(table.schema.catalog.getName(), table.schema.getName(),
							table.getName());
					while (rs.next()) {
						if (name.equals(rs.getString("COLUMN_NAME"))) {
							return true;
						}
					}
					return false;
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public Column getForeignKeyOriginColumn() {
			try {
				synchronized (table.schema.catalog.database.connectionMutex) {
					DatabaseMetaData metadata = table.schema.catalog.database.connection.getMetaData();
					ResultSet rs = metadata.getImportedKeys(table.schema.catalog.getName(), table.schema.getName(),
							table.getName());
					while (rs.next()) {
						if (name.equals(rs.getString("FKCOLUMN_NAME"))) {
							return table.schema.catalog.database.getCatalog(rs.getString("PKTABLE_CAT"))
									.getSchema(rs.getString("PKTABLE_SCHEM")).getTable(rs.getString("PKTABLE_NAME"))
									.getColumn(rs.getString("PKCOLUMN_NAME"));
						}
					}
					return null;
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		public boolean isNullable() {
			return nullable;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Column other = (Column) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Column [name=" + name + ", position=" + position + ", javaType=" + getJavaType() + ", table="
					+ table + "]";
		}
	}

	public static class Row {

		protected static final int NEW_ROW_NUMBER = -1;
		protected Table table;
		protected int number;
		protected Map<String, Object> newRowValueMap = new HashMap<String, Object>();

		public Row(Table table, int number) {
			this.table = table;
			this.number = number;
			if (number == NEW_ROW_NUMBER) {
				for (Column column : table.getColumns()) {
					if (column.isAutoIncrement()) {
						continue;
					}
					if (column.isNullable()) {
						continue;
					}
					try {
						newRowValueMap.put(column.getName(),
								ReflectionUIUtils.createDefaultInstance(
										ReflectionUIUtils.STANDARD_REFLECTION.buildTypeInfo(new JavaTypeInfoSource(
												ReflectionUIUtils.STANDARD_REFLECTION, column.getJavaType(), null))));
					} catch (Throwable ignore) {
					}
				}
			}
		}

		public int getNumber() {
			return number;
		}

		public List<Cell> getCells() {
			List<Cell> result = new ArrayList<Cell>();
			for (Column column : table.getColumns()) {
				result.add(new Cell(this, column));
			}
			return result;
		}

		public Cell getCell(String columnName) {
			Column column = table.getColumn(columnName);
			if (column == null) {
				return null;
			}
			return new Cell(this, column);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + number;
			result = prime * result + ((table == null) ? 0 : table.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Row other = (Row) obj;
			if (number != other.number)
				return false;
			if (table == null) {
				if (other.table != null)
					return false;
			} else if (!table.equals(other.table))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Row [number=" + number + ", table=" + table + "]";
		}

	}

	public static class Cell {

		protected Column column;
		protected Row row;

		public Cell(Row row, Column column) {
			this.column = column;
			this.row = row;
		}

		public Column getColumn() {
			return column;
		}

		public Row getRow() {
			return row;
		}

		public Object getValue() {
			if (row.getNumber() == Row.NEW_ROW_NUMBER) {
				return row.newRowValueMap.get(column.getName());
			} else {
				if (!row.newRowValueMap.containsKey(column.getName())) {
					Object value = column.table.getCellValue(row.getNumber(), column.getName());
					row.newRowValueMap.put(column.getName(), value);
				}
				return row.newRowValueMap.get(column.getName());
			}
		}

		public void setValue(Object value) {
			if (row.getNumber() == Row.NEW_ROW_NUMBER) {
				row.newRowValueMap.put(column.getName(), value);
			} else {
				column.table.setCellValue(row.getNumber(), column.getName(), value);
				row.newRowValueMap.put(column.getName(), value);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((column == null) ? 0 : column.hashCode());
			result = prime * result + ((row == null) ? 0 : row.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Cell other = (Cell) obj;
			if (column == null) {
				if (other.column != null)
					return false;
			} else if (!column.equals(other.column))
				return false;
			if (row == null) {
				if (other.row != null)
					return false;
			} else if (!row.equals(other.row))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Cell [column=" + column + ", row=" + row + "]";
		}

	}

}
