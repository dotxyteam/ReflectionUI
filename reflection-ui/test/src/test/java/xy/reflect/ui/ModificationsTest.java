package xy.reflect.ui;

import java.awt.Color;
import java.io.File;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ModificationsTest {

	private boolean canModify(String fieldName) {
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo thisType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(this));
		IFieldInfo field = ReflectionUIUtils.findInfoByName(thisType.getFields(), fieldName);
		boolean canCommit = !field.isGetOnly();
		ValueReturnMode childValueReturnMode = field.getValueReturnMode();
		return ReflectionUIUtils.canCloseValueEditSession(
				ReflectionUIUtils.isValueImmutable(reflectionUI, field.getValue(this)), childValueReturnMode,
				canCommit);
	}

	@Test
	public void test() {
		Assert.assertTrue(canModify("theNull"));
		Assert.assertTrue(canModify("theInt"));
		Assert.assertTrue(canModify("theInteger"));
		Assert.assertTrue(canModify("theString"));
		Assert.assertTrue(canModify("theFile"));
		Assert.assertTrue(canModify("theColor"));
		Assert.assertTrue(canModify("theDate"));

		Assert.assertTrue(!canModify("theGetOnlyNull"));
		Assert.assertTrue(!canModify("theGetOnlyInt"));
		Assert.assertTrue(!canModify("theGetOnlyInteger"));
		Assert.assertTrue(!canModify("theGetOnlyString"));
		Assert.assertTrue(canModify("theGetOnlyFile"));
		Assert.assertTrue(canModify("theGetOnlyColor"));
		Assert.assertTrue(canModify("theGetOnlyDate"));

		Assert.assertTrue(!canModify("thePublicFinalNull"));
		Assert.assertTrue(!canModify("thePublicFinalInt"));
		Assert.assertTrue(!canModify("thePublicFinalInteger"));
		Assert.assertTrue(!canModify("thePublicFinalString"));
		Assert.assertTrue(canModify("thePublicFinalFile"));
		Assert.assertTrue(canModify("thePublicFinalColor"));
		Assert.assertTrue(canModify("thePublicFinalDate"));

	}

	private Object theNull = null;
	private int theInt = 0;
	private Integer theInteger = new Integer(0);
	private String theString = "";
	private File theFile = new File("");
	private Color theColor = new Color(0, 0, 0);
	private Date theDate = new Date();

	public Object getTheNull() {
		return theNull;
	}

	public void setTheNull(Object theNull) {
		this.theNull = theNull;
	}

	public int getTheInt() {
		return theInt;
	}

	public void setTheInt(int theInt) {
		this.theInt = theInt;
	}

	public Integer getTheInteger() {
		return theInteger;
	}

	public void setTheInteger(Integer theInteger) {
		this.theInteger = theInteger;
	}

	public String getTheString() {
		return theString;
	}

	public void setTheString(String theString) {
		this.theString = theString;
	}

	public File getTheFile() {
		return theFile;
	}

	public void setTheFile(File theFile) {
		this.theFile = theFile;
	}

	public Color getTheColor() {
		return theColor;
	}

	public void setTheColor(Color theColor) {
		this.theColor = theColor;
	}

	public Date getTheDate() {
		return theDate;
	}

	public void setTheDate(Date theDate) {
		this.theDate = theDate;
	}

	private Object theGetOnlyNull = null;
	private int theGetOnlyInt = 0;
	private Integer theGetOnlyInteger = new Integer(0);
	private String theGetOnlyString = "";
	private File theGetOnlyFile = new File("");
	private Color theGetOnlyColor = new Color(0, 0, 0);
	private Date theGetOnlyDate = new Date();

	public Object getTheGetOnlyNull() {
		return theGetOnlyNull;
	}

	public int getTheGetOnlyInt() {
		return theGetOnlyInt;
	}

	public Integer getTheGetOnlyInteger() {
		return theGetOnlyInteger;
	}

	public String getTheGetOnlyString() {
		return theGetOnlyString;
	}

	public File getTheGetOnlyFile() {
		return theGetOnlyFile;
	}

	public Color getTheGetOnlyColor() {
		return theGetOnlyColor;
	}

	public Date getTheGetOnlyDate() {
		return theGetOnlyDate;
	}

	public final Object thePublicFinalNull = null;
	public final int thePublicFinalInt = 0;
	public final Integer thePublicFinalInteger = new Integer(0);
	public final String thePublicFinalString = "";
	public final File thePublicFinalFile = new File("");
	public final Color thePublicFinalColor = new Color(0, 0, 0);
	public final Date thePublicFinalDate = new Date();

}
