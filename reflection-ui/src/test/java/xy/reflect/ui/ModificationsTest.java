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

	private boolean mayModify(String fieldName) {
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo thisType = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(this));
		IFieldInfo field = ReflectionUIUtils.findInfoByName(thisType.getFields(), fieldName);
		boolean canCommit = !field.isGetOnly();
		ValueReturnMode childValueReturnMode = field.getValueReturnMode();
		return ReflectionUIUtils.mayModificationsHaveImpact(
				ReflectionUIUtils.isValueImmutable(reflectionUI, field.getValue(this)), childValueReturnMode,
				canCommit);
	}

	@Test
	public void test() {
		Assert.assertTrue(mayModify("theNull"));
		Assert.assertTrue(mayModify("theInt"));
		Assert.assertTrue(mayModify("theInteger"));
		Assert.assertTrue(mayModify("theString"));
		Assert.assertTrue(mayModify("theFile"));
		Assert.assertTrue(mayModify("theColor"));
		Assert.assertTrue(mayModify("theDate"));

		Assert.assertTrue(!mayModify("theGetOnlyNull"));
		Assert.assertTrue(!mayModify("theGetOnlyInt"));
		Assert.assertTrue(!mayModify("theGetOnlyInteger"));
		Assert.assertTrue(!mayModify("theGetOnlyString"));
		Assert.assertTrue(mayModify("theGetOnlyFile"));
		Assert.assertTrue(mayModify("theGetOnlyColor"));
		Assert.assertTrue(mayModify("theGetOnlyDate"));

		Assert.assertTrue(!mayModify("thePublicFinalNull"));
		Assert.assertTrue(!mayModify("thePublicFinalInt"));
		Assert.assertTrue(!mayModify("thePublicFinalInteger"));
		Assert.assertTrue(!mayModify("thePublicFinalString"));
		Assert.assertTrue(mayModify("thePublicFinalFile"));
		Assert.assertTrue(mayModify("thePublicFinalColor"));
		Assert.assertTrue(mayModify("thePublicFinalDate"));

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
