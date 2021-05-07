package xy.reflect.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TreeStructureDiscoverySettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.item.AbstractItemPositionFactory;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TestIterableTypeInfos {

	public abstract class AbstractItem {
		public int abstractValue;
		public int abstractValue2;
		public int abstractValue3;
		public int abstractValue4;
		public int abstractValue5;
	}

	public class Item extends AbstractItem {
		public int value;
		public List<AbstractItem> subItems;

		public Item(int value) {
			this.value = value;
		}

	}

	public class OtherItem extends AbstractItem {
		public int otherValue;

		public OtherItem(int otherValue) {
			this.otherValue = otherValue;
		}

	}

	public ArrayList<AbstractItem> itemList = new ArrayList<AbstractItem>(
			Arrays.asList(new Item(1), new Item(2), new OtherItem(3), new OtherItem(4)));

	@Test
	public void test() {
		CustomizedUI customizedUI = new CustomizedUI();
		ITypeInfo typeInfo = customizedUI.getTypeInfo(customizedUI.getTypeInfoSource(this));

		final IFieldInfo itemListFieldInfo = ReflectionUIUtils.findInfoByName(typeInfo.getFields(), "itemList");
		IListTypeInfo itemListTypeInfo = (IListTypeInfo) itemListFieldInfo.getType();

		ListCustomization itemListTypeCustomization = InfoCustomizations.getListCustomization(
				customizedUI.getInfoCustomizations(), itemListTypeInfo.getName(),
				itemListTypeInfo.getItemType().getName(), true);
		itemListTypeCustomization.setFieldColumnsAdded(true);
		itemListTypeCustomization.setItemTypeColumnAdded(true);
		TreeStructureDiscoverySettings treeSettings = new TreeStructureDiscoverySettings();
		itemListTypeCustomization.setTreeStructureDiscoverySettings(treeSettings);
		treeSettings.setHeterogeneousTree(false);
		treeSettings.setSingleSubListFieldNameNeverDisplayedAsTreeNode(false);
		
		final Object itemListValue = itemListFieldInfo.getValue(this);
		Object[] itemLisRawValue = itemListTypeInfo.toArray(itemListValue);
		Assert.assertArrayEquals(itemLisRawValue, itemList.toArray());
		Assert.assertEquals(itemListTypeInfo.fromArray(itemLisRawValue), itemList);

		IListStructuralInfo itemListStructuralInfo = itemListTypeInfo.getStructuralInfo();

		ItemPosition firstItemPosition = new AbstractItemPositionFactory() {

			@Override
			public Object retrieveRootListValue() {
				return itemListValue;
			}

			@Override
			public void commitRootListValue(Object rootListValue) {
				throw new UnsupportedOperationException();
			}

			@Override
			public IListTypeInfo getRootListType() {
				return (IListTypeInfo) itemListFieldInfo.getType();
			}

			@Override
			public ValueReturnMode getRootListValueReturnMode() {
				return itemListFieldInfo.getValueReturnMode();
			}

			@Override
			public boolean isRootListGetOnly() {
				return itemListFieldInfo.isGetOnly();
			}

			@Override
			public String getRootListTitle() {
				return itemListFieldInfo.getCaption();
			}
		}.getRootItemPosition(0);
		ITypeInfo firstItemType = customizedUI.getTypeInfo(new JavaTypeInfoSource(itemList.get(0).getClass(), null));
		Assert.assertEquals(itemListStructuralInfo.getColumns().get(0).getCellValue(firstItemPosition),
				firstItemType.getCaption());

		IFieldInfo valueField = ReflectionUIUtils.findInfoByName(firstItemType.getFields(), "value");
		for (int i = 0; i < itemListStructuralInfo.getColumns().size(); i++) {
			if (itemListStructuralInfo.getColumns().get(i).getCaption().equals(valueField.getCaption())) {
				Assert.fail();
			}
		}

		IInfoFilter firstItemInfoSettings = itemListStructuralInfo.getItemInfoFilter(firstItemPosition);
		Assert.assertTrue(!firstItemInfoSettings.excludeField(valueField));

		IFieldInfo subItemsField = ReflectionUIUtils.findInfoByName(firstItemType.getFields(), "subItems");
		IFieldInfo subListField = itemListStructuralInfo.getItemSubListField(firstItemPosition);
		Assert.assertTrue(subListField.getCaption().contains(subItemsField.getCaption()));

		Object subListNameNodeList = subListField.getValue(firstItemPosition.getItem());
		Object subListNameNode = ((IListTypeInfo) subListField.getType()).toArray(subListNameNodeList)[0];
		ITypeInfo subListNameNodeType = customizedUI.getTypeInfo(customizedUI.getTypeInfoSource(subListNameNode));
		IFieldInfo actualSubListField = subListNameNodeType.getFields().get(0);
		Object subList = actualSubListField.getValue(subListNameNode);
		Assert.assertTrue(subList == null);
	}
}
