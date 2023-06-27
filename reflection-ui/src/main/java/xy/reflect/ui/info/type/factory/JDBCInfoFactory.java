package xy.reflect.ui.info.type.factory;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.BasicTypeInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
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
			return table.getName() + " Row N°" + (((Row) object).getIndex() + 1);
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
			return ((Row) object).getCell(column.getName()).getValue();
		}

		@Override
		public void setValue(Object object, Object value) {
			((Row) object).getCell(column.getName()).setValue(value);
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
			// return column.getJavaType().getName();
			return "ColumnFieldTypeInfo [column=" + column.getName() + ", table=" + column.table.getName() + ", schema="
					+ column.table.schema.getName() + ", catalog=" + column.table.schema.catalog.getName() + "]";
		}

		@Override
		public String getCaption() {
			return "Column " + column.getName() + " Type";
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

		@SuppressWarnings("unchecked")
		@Override
		public void setValue(Object object, Object value) {
			try {
				((Table) object).setRows((List<Row>) value);
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
			return reflectionUI.buildTypeInfo(new TableRowsFieldTypeInfoSource(table));
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
		public boolean canInstanciateFromArray() {
			return true;
		}

		@Override
		public Object fromArray(Object[] array) {
			List<Row> result = new ArrayList<Row>();
			for(Object object: array) {
				result.add((Row)object);
			}
			return result;
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

	public static class DatabaseManagedSystem {

		protected Connection connection;

		public DatabaseManagedSystem(String connectionURL) throws SQLException {
			connection = DriverManager.getConnection(connectionURL);
		}

		public DatabaseManagedSystem(String connectionURL, String user, String password) throws SQLException {
			connection = DriverManager.getConnection(connectionURL, user, password);
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

	public static class Table {

		protected Schema schema;
		protected String name;

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

		public List<Row> getRows() throws SQLException {
			List<Row> result = new ArrayList<Row>();
			Statement statement = schema.catalog.databaseManagedSystem.connection.createStatement();
			ResultSet rs = statement.executeQuery("select * from " + getNamePrefix() + name);
			int iRow = 0;
			while (rs.next()) {
				Row row = new Row(this, iRow);
				for (Column column : getColumns()) {
					row.getCells().add(new Cell(row, column, column.extractCellValue(rs)));
				}
				result.add(row);
				iRow++;
			}
			return result;
		}

		/*
		 * add setRows(List<Row> rows) that will compute the list of rows to add, to
		 * remove and to update. The row index allows to know which row to add and which
		 * row to update. Rows to update will be identified by cells marked as dirty
		 * (modified).
		 */

		public void setRows(List<Row> newRows) throws SQLException {
			List<Row> oldRows = getRows();
			List<Row> oldRowsToDelete = new ArrayList<Row>();
			for (Row oldRow : oldRows) {
				boolean oldRowFoundInNewRows = false;
				for (Row newRow : newRows) {
					if (oldRow.getIndex() == newRow.getIndex()) {
						oldRowFoundInNewRows = true;
						break;
					}
				}
				if (!oldRowFoundInNewRows) {
					oldRowsToDelete.add(oldRow);
				}
			}
			List<Row> newRowsToInsert = new ArrayList<Row>();
			for (Row newRow : newRows) {
				boolean newRowFoundInOldRows = false;
				for (Row oldRow : oldRows) {
					if (newRow.getIndex() == oldRow.getIndex()) {
						newRowFoundInOldRows = true;
						break;
					}
				}
				if (!newRowFoundInOldRows) {
					newRowsToInsert.add(newRow);
				}
			}
			List<Row> rowsToUpdate = new ArrayList<Row>();
			for (Row oldRow : oldRows) {
				boolean oldRowFoundInNewRows = false;
				for (Row newRow : newRows) {
					if (oldRow.getIndex() == newRow.getIndex()) {
						oldRowFoundInNewRows = true;
						break;
					}
				}
				if (oldRowFoundInNewRows) {
					for (Cell cell : oldRow.getCells()) {
						if (cell.isModified()) {
							rowsToUpdate.add(oldRow);
						}
					}
				}
			}
			Statement statement = schema.catalog.databaseManagedSystem.connection.createStatement();
			for (Row row : rowsToUpdate) {
				String updateQueryString = "update " + getNamePrefix() + name;
				for (int i = 0; i < row.getCells().size(); i++) {
					Cell cell = row.getCells().get(i);
					updateQueryString += ((i == 0) ? " set " : ", ") + cell.getColumn().getName() + "="
							+ cell.getValue();
				}
				for (int i = 0; i < row.getCells().size(); i++) {
					Cell cell = row.getCells().get(i);
					if (cell.getColumn().isPrimaryKey()) {
						updateQueryString += ((i == 0) ? " where " : " and ") + cell.getColumn().getName() + "="
								+ cell.getValue();
					}
				}
				statement.executeUpdate(updateQueryString);
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
			result = prime * result + position;
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
			if (position != other.position)
				return false;
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
			return "Column [name=" + name + ", index=" + position + ", table=" + table + "]";
		}
	}

	public static class Row {

		protected Table table;
		protected int index;
		protected List<Cell> cells = new ArrayList<Cell>();

		public Row(Table table, int index) {
			this.table = table;
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public List<Cell> getCells() {
			return cells;
		}

		public Cell getCell(String columnName) {
			for (Cell cell : cells) {
				if (cell.getColumn().getName().equals(columnName)) {
					return cell;
				}
			}
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
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
			if (index != other.index)
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
			return "Row [index=" + index + ", cells=" + cells + ", table=" + table + "]";
		}

	}

	public static class Cell {

		protected Column column;
		protected Row row;
		protected Object value;
		protected boolean modified = false;

		public Cell(Row row, Column column, Object value) {
			this.column = column;
			this.row = row;
			this.value = value;
		}

		public Column getColumn() {
			return column;
		}

		public Row getRow() {
			return row;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
			this.modified = true;
		}

		public boolean isModified() {
			return modified;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((column == null) ? 0 : column.hashCode());
			result = prime * result + ((row == null) ? 0 : row.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Cell [value=" + value + ", column=" + column + "]";
		}

	}

}
