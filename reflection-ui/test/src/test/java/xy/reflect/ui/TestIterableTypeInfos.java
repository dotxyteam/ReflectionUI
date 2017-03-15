package xy.reflect.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.control.input.FieldControlData;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.TreeStructureDiscoverySettings;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TestIterableTypeInfos  extends AbstractTest{

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
		final InfoCustomizations customizations = new InfoCustomizations();
		ReflectionUI reflectionUI = new ReflectionUI() {
			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return customizations.get(this, super.getTypeInfo(typeSource));
			}
		};
		ITypeInfo typeInfo = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(this));

		IFieldInfo itemListInfo = ReflectionUIUtils.findInfoByName(typeInfo.getFields(), "itemList");
		IListTypeInfo itemListTypeInfo = (IListTypeInfo) itemListInfo.getType();
		
		ListCustomization itemListTypeCustomization = customizations
				.getListCustomization(itemListTypeInfo.getName(), itemListTypeInfo.getItemType().getName(), true);
		itemListTypeCustomization.setFieldColumnsAdded(true);
		itemListTypeCustomization.setItemTypeColumnAdded(true);
		TreeStructureDiscoverySettings treeSettings = new TreeStructureDiscoverySettings();
		itemListTypeCustomization.setTreeStructureDiscoverySettings(treeSettings);
		treeSettings.setHeterogeneousTree(false);

		Object[] itemLisRawValue = itemListTypeInfo.toArray(itemListInfo.getValue(this));
		Assert.assertArrayEquals(itemLisRawValue, itemList.toArray());
		Assert.assertEquals(itemListTypeInfo.fromArray(itemLisRawValue), itemList);

		IListStructuralInfo itemListStructuralInfo = itemListTypeInfo.getStructuralInfo();

		ItemPosition firstItemPosition = new ItemPosition(null, new FieldControlData(this,itemListInfo), 0);
		ITypeInfo firstItemType = reflectionUI.getTypeInfo(new JavaTypeInfoSource(itemList.get(0).getClass()));
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
		Assert.assertTrue(firstItemInfoSettings.excludeField(subItemsField));

		IFieldInfo subListField = itemListStructuralInfo.getItemSubListField(firstItemPosition);
		Assert.assertTrue(subListField.getCaption().contains(subItemsField.getCaption()));

		Object subListNameNodeList = subListField.getValue(firstItemPosition.getLastKnownItem());
		Object subListNameNode = ((IListTypeInfo) subListField.getType()).toArray(subListNameNodeList)[0];
		ITypeInfo subListNameNodeType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(subListNameNode));
		IFieldInfo actualSubListField = subListNameNodeType.getFields().get(0);
		Object subList = actualSubListField.getValue(subListNameNode);
		Assert.assertTrue(subList == null);
	}
}
