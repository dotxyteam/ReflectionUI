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
import xy.reflect.ui.info.type.BasicTypeInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.JDBCInfoFactory.Parameter.Kind;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.ListTypeInfoProxy;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class JDBCInfoFactory {

	protected ReflectionUI reflectionUI;

	public JDBCInfoFactory(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	public ITypeInfoSource getTypeInfoSource(final Object object) {
		if (object instanceof Table) {
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
			try {
				for (Column colum : table.getColumns()) {
					result.add(new ColumnFieldInfo(colum));
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
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
			try {
				return ((Row) object).getCell(column.getName()).getValue();
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		@Override
		public void setValue(Object object, Object value) {
			try {
				((Row) object).getCell(column.getName()).setValue(value);
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		@Override
		public boolean isGetOnly() {
			return false;
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.buildTypeInfo(new ColumnFieldTypeInfoSource(column));
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

	public class ColumnFieldTypeInfo extends BasicTypeInfoProxy {

		private Column column;

		public ColumnFieldTypeInfo(Column column) {
			super(ITypeInfo.NULL_BASIC_TYPE_INFO);
			this.column = column;
		}

		@Override
		public String getName() {
			return column.getJavaType().getName();
			// return "ColumnFieldTypeInfo [column=" + column.getName() + ", table=" +
			// column.table.getName() + ", schema="
			// + column.table.schema.getName() + ", catalog=" +
			// column.table.schema.catalog.getName() + "]";
		}

		@Override
		public String getCaption() {
			return column.getName();
		}

		@Override
		public String toString(Object object) {
			return (object == null) ? "" : object.toString();
		}

		@Override
		public boolean supports(Object object) {
			return column.getJavaType().isInstance(object);
		}

		@Override
		public ITypeInfoSource getSource() {
			return new ColumnFieldTypeInfoSource(column);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.emptyList();
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
			ColumnFieldTypeInfo other = (ColumnFieldTypeInfo) obj;
			if (column == null) {
				if (other.column != null)
					return false;
			} else if (!column.equals(other.column))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ColumnFieldTypeInfo [column=" + column + "]";
		}

	}

	public class ColumnFieldTypeInfoSource implements ITypeInfoSource {

		private Column column;

		public ColumnFieldTypeInfoSource(Column column) {
			this.column = column;
		}

		@Override
		public SpecificitiesIdentifier getSpecificitiesIdentifier() {
			return new SpecificitiesIdentifier(new RowTypeInfo(column.table).getName(),
					new ColumnFieldInfo(column).getName());
		}

		@Override
		public ITypeInfo getTypeInfo() {
			return new ColumnFieldTypeInfo(column);
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
			ColumnFieldTypeInfoSource other = (ColumnFieldTypeInfoSource) obj;
			if (column == null) {
				if (other.column != null)
					return false;
			} else if (!column.equals(other.column))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ColumnFieldTypeInfoSource [column=" + column + "]";
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
			try {
				return ((Table) object).getRows();
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.buildTypeInfo(new TableRowsFieldTypeInfoSource(table));
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
			try {
				return table.getRows().size() + " Row(s)";
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
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
			try {
				List<Row> replacingRows = new ArrayList<Row>();
				for (Object object : array) {
					Row row = (Row) object;
					replacingRows.add(row);
				}
				List<Row> rowsToDelete = new ArrayList<Row>(table.getRows());
				rowsToDelete.removeAll(replacingRows);
				Collections.reverse(rowsToDelete);
				for (Row row : rowsToDelete) {
					table.resultSet.absolute(row.getNumber());
					table.resultSet.deleteRow();
				}
				for (Row replacingRow : replacingRows) {
					if (replacingRow.getNumber() == Row.NEW_ROW_NUMBER) {
						table.resultSet.moveToInsertRow();
						for (Column column : table.getColumns()) {
							table.resultSet.updateObject(column.getName(),
									replacingRow.getCell(column.getName()).getValue());
						}
						table.resultSet.insertRow();
					}
				}
				table.refresh();
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
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
			try {
				for (Column column : table.getColumns()) {
					result.add(new TableColumnInfo(column));
				}
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
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
			Cell cell;
			try {
				cell = row.getCell(column.getName());
			} catch (SQLException e) {
				throw new ReflectionUIError(e);
			}
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

	public static class DatabaseManagedSystem {

		protected Connection connection;

		public DatabaseManagedSystem(String connectionURL) throws SQLException {
			connection = DriverManager.getConnection(connectionURL);
		}

		public DatabaseManagedSystem(String connectionURL, String user, String password) throws SQLException {
			connection = DriverManager.getConnection(connectionURL, user, password);
		}

		public void executeSQL(String sql) throws SQLException {
			connection.createStatement().execute(sql);
		}

		public List<Catalog> getCatalogs() throws SQLException {
			List<Catalog> result = new ArrayList<Catalog>();
			Catalog nullCatalog = new Catalog(this, null);
			if (nullCatalog.getSchemas().size() > 0) {
				result.add(nullCatalog);
			}
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet rs = metadata.getCatalogs();
			while (rs.next()) {
				String catalogName = rs.getString("TABLE_CAT");
				result.add(new Catalog(this, catalogName));
			}
			return result;
		}

		@Override
		public String toString() {
			return "DatabaseManagedSystem [connection=" + connection + "]";
		}

	}

	public static class Catalog {

		protected DatabaseManagedSystem databaseManagedSystem;
		protected String name;

		public Catalog(DatabaseManagedSystem databaseManagedSystem, String name) {
			this.databaseManagedSystem = databaseManagedSystem;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public List<Schema> getSchemas() throws SQLException {
			List<Schema> result = new ArrayList<Schema>();
			Schema nullSchema = new Schema(this, null);
			if (nullSchema.getTables().size() > 0) {
				result.add(nullSchema);
			}
			DatabaseMetaData metadata = databaseManagedSystem.connection.getMetaData();
			ResultSet rs = metadata.getSchemas(name, "%");
			while (rs.next()) {
				if (name == null) {
					String catalogName = rs.getString("TABLE_CATALOG");
					if (catalogName != null) {
						continue;
					}
				}
				String schemaName = rs.getString("TABLE_SCHEM");
				result.add(new Schema(this, schemaName));
			}
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((databaseManagedSystem == null) ? 0 : databaseManagedSystem.hashCode());
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
			if (databaseManagedSystem == null) {
				if (other.databaseManagedSystem != null)
					return false;
			} else if (!databaseManagedSystem.equals(other.databaseManagedSystem))
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
			return "Catalog [name=" + name + ", databaseManagedSystem=" + databaseManagedSystem + "]";
		}

	}

	public static class Schema {

		protected Catalog catalog;
		protected String name;

		public Schema(Catalog catalog, String name) {
			this.catalog = catalog;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public List<Table> getTables() throws SQLException {
			List<Table> result = new ArrayList<Table>();
			DatabaseMetaData metadata = catalog.databaseManagedSystem.connection.getMetaData();
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
			return result;
		}

		public List<Procedure> getProcedures() throws SQLException {
			List<Procedure> result = new ArrayList<Procedure>();
			DatabaseMetaData metadata = catalog.databaseManagedSystem.connection.getMetaData();
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
				String procedureSpecificName = rs.getString("SPECIFIC_NAME");
				result.add(new Procedure(this, procedureName, procedureSpecificName));
			}
			return result;
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
		protected String specificName;

		public Procedure(Schema schema, String name, String specificName) {
			this.schema = schema;
			this.name = name;
			this.specificName = specificName;
		}

		public String getName() {
			return name;
		}

		public String getSpecificName() {
			return specificName;
		}

		public List<Parameter> getParameters() throws SQLException {
			List<Parameter> result = new ArrayList<Parameter>();
			DatabaseMetaData metadata = schema.catalog.databaseManagedSystem.connection.getMetaData();
			ResultSet rs = metadata.getProcedureColumns(schema.catalog.name, schema.name, name, "%");
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				int columnPosition = rs.getInt("ORDINAL_POSITION");
				int sqlType = rs.getInt("DATA_TYPE");
				int parameterKind = rs.getInt("COLUMN_TYPE");
				result.add(new Parameter(this, columnName, columnPosition, sqlType,
						Parameter.Kind.fromInt(parameterKind)));
			}
			return result;
		}

		protected String getNamePrefix() throws SQLException {
			String result = "";
			if (schema.catalog.getName() != null) {
				result += schema.catalog.getName()
						+ schema.catalog.databaseManagedSystem.connection.getMetaData().getCatalogSeparator();
			}
			if (schema.getName() != null) {
				result += schema.getName() + ".";
			}
			return result;
		}

		public Map<String, Object> call(Map<String, Object> valueByInputParameter) throws SQLException {
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
			CallableStatement callableStatement = schema.catalog.databaseManagedSystem.connection
					.prepareCall(callString);
			for (Parameter parameter : getParameters()) {
				if ((parameter.getKind() == Kind.IN) || (parameter.getKind() == Kind.INOUT)) {
					callableStatement.setObject(parameter.getName(), valueByInputParameter.get(parameter.getName()));
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
			result = prime * result + ((specificName == null) ? 0 : specificName.hashCode());
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
			if (specificName == null) {
				if (other.specificName != null)
					return false;
			} else if (!specificName.equals(other.specificName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Procedure [name=" + name + ", specificName=" + specificName + ", schema=" + schema + "]";
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
		protected Statement statement;
		protected ResultSet resultSet;
		protected List<Row> rows;

		public Table(Schema schema, String name) {
			this.schema = schema;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public List<Column> getColumns() throws SQLException {
			List<Column> result = new ArrayList<Column>();
			DatabaseMetaData metadata = schema.catalog.databaseManagedSystem.connection.getMetaData();
			List<String> primaryKeyColumnNames = new ArrayList<String>();
			{
				ResultSet rs = metadata.getPrimaryKeys(schema.catalog.getName(), schema.getName(), name);
				while (rs.next()) {
					primaryKeyColumnNames.add(rs.getString("COLUMN_NAME"));
				}
			}
			ResultSet rs = metadata.getColumns(schema.catalog.name, schema.name, name, "%");
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				int columnPosition = rs.getInt("ORDINAL_POSITION");
				int sqlType = rs.getInt("DATA_TYPE");
				boolean primaryKey = primaryKeyColumnNames.contains(columnName);
				result.add(new Column(this, columnName, columnPosition, sqlType, primaryKey));
			}
			return result;
		}

		public Column getColumn(String columnName) throws SQLException {
			for (Column column : getColumns()) {
				if (column.getName().equals(columnName)) {
					return column;
				}
			}
			return null;
		}

		public List<Row> getRows() throws SQLException {
			if (rows == null) {
				statement = schema.catalog.databaseManagedSystem.connection
						.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				refresh();
				rows = new AbstractList<Row>() {

					@Override
					public Row get(int index) {
						int rowNumber = index + 1;
						return new Row(Table.this, rowNumber);
					}

					@Override
					public int size() {
						try {
							ResultSet countResultSet = statement
									.executeQuery("select count(*) from " + getNamePrefix() + name);
							countResultSet.first();
							return countResultSet.getInt(1);
						} catch (SQLException e) {
							throw new ReflectionUIError(e);
						}
					}
				};
			}
			return rows;
		}

		public void refresh() throws SQLException {
			resultSet = statement.executeQuery("select * from " + getNamePrefix() + name);
		}

		protected String getNamePrefix() throws SQLException {
			String result = "";
			if (schema.catalog.getName() != null) {
				result += schema.catalog.getName()
						+ schema.catalog.databaseManagedSystem.connection.getMetaData().getCatalogSeparator();
			}
			if (schema.getName() != null) {
				result += schema.getName() + ".";
			}
			return result;
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
		protected boolean primaryKey;

		public Column(Table table, String name, int position, int sqlType, boolean primaryKey) {
			this.table = table;
			this.name = name;
			this.position = position;
			this.sqlType = sqlType;
			this.primaryKey = primaryKey;
		}

		public Object extractCellValue(ResultSet rs) throws SQLException {
			return rs.getObject(position);
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

		public boolean isPrimaryKey() {
			return primaryKey;
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
		}

		public int getNumber() {
			return number;
		}

		public List<Cell> getCells() throws SQLException {
			List<Cell> result = new ArrayList<Cell>();
			for (Column column : table.getColumns()) {
				result.add(new Cell(this, column));
			}
			return result;
		}

		public Cell getCell(String columnName) throws SQLException {
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
				try {
					column.table.resultSet.absolute(row.getNumber());
					return column.table.resultSet.getObject(column.getName());
				} catch (SQLException e) {
					throw new ReflectionUIError(e);
				}
			}
		}

		public void setValue(Object value) {
			if (row.getNumber() == Row.NEW_ROW_NUMBER) {
				row.newRowValueMap.put(column.getName(), value);
			} else {
				try {
					column.table.resultSet.absolute(row.getNumber());
					column.table.resultSet.updateObject(column.getName(), value);
					column.table.resultSet.updateRow();
					column.table.refresh();

				} catch (SQLException e) {
					throw new ReflectionUIError(e);
				}
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
