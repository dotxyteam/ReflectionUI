package xy.reflect.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.structure.TabularTreetStructuralInfo;
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

	public List<AbstractItem> itemList = new ArrayList<AbstractItem>(
			Arrays.asList(new Item(1), new Item(2), new OtherItem(3),
					new OtherItem(4)));

	@Test
	public void test() {
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));

		IFieldInfo itemListInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "itemList");
		IListTypeInfo itemListTypeInfo = (IListTypeInfo) itemListInfo.getType();

		Object[] itemListArray = itemListTypeInfo.toArray(itemListInfo
				.getValue(this));
		Assert.assertArrayEquals(itemListArray, itemList.toArray());
		Assert.assertEquals(itemListTypeInfo.fromArray(itemListArray), itemList);

		TabularTreetStructuralInfo itemListStructuralInfo = (TabularTreetStructuralInfo) itemListTypeInfo
				.getStructuralInfo();

		ItemPosition firstItemPosition = new ItemPosition(itemListInfo, null,
				0, this);
		ITypeInfo firstItemType = reflectionUI
				.getTypeInfo(new JavaTypeInfoSource(itemList.get(0).getClass()));
		Assert.assertEquals(
				itemListStructuralInfo.getCellValue(firstItemPosition, 0),
				firstItemType.getCaption());

		IFieldInfo valueField = ReflectionUIUtils.findInfoByName(
				firstItemType.getFields(), "value");
		for (int i = 0; i < itemListStructuralInfo.getColumnCount(); i++) {
			if (itemListStructuralInfo.getColumnCaption(i).equals(
					valueField.getCaption())) {
				Assert.fail();
			}
		}

		IInfoCollectionSettings firstItemInfoSettings = itemListStructuralInfo
				.getItemInfoSettings(firstItemPosition);
		Assert.assertTrue(!firstItemInfoSettings.excludeField(valueField));

		IFieldInfo subItemsField = ReflectionUIUtils.findInfoByName(
				firstItemType.getFields(), "subItems");
		Assert.assertTrue(firstItemInfoSettings.excludeField(subItemsField));

		IFieldInfo subListField = itemListStructuralInfo
				.getItemSubListField(firstItemPosition);
		Assert.assertTrue(subListField.getCaption().contains(
				subItemsField.getCaption()));

		Object subListNameNodeList = subListField.getValue(firstItemPosition
				.getItem());
		Object subListNameNode = ((IListTypeInfo) subListField.getType())
				.toArray(subListNameNodeList)[0];
		ITypeInfo subListNameNodeType = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(subListNameNode));
		IFieldInfo actualSubListField = subListNameNodeType.getFields().get(0);
		Object subList = actualSubListField.getValue(subListNameNode);
		Assert.assertTrue(subList == null);
	}
}
