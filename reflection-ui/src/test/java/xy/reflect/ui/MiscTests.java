/*
 * 
 */
package xy.reflect.ui;

import java.awt.Rectangle;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.control.swing.builder.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.IOUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MiscTests {

	@Test
	public void testMethodSignatures() throws Exception {
		String signature = "void add(java.awt.Point)";
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo rectangleTypeInfo = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(new Rectangle()));
		IMethodInfo addPointMethodInfo = ReflectionUIUtils.findMethodBySignature(rectangleTypeInfo.getMethods(),
				signature);
		Assert.assertNotNull(addPointMethodInfo);

		{
			String complexSignature = "CapsuleFieldType [context=EncapsulationContext [objectType=xy.phoyo.album.Configuration], fieldName=printerSupportedPapers]"
					+ " " + "printerSupportedPapers.get()";
			Assert.assertEquals(
					"CapsuleFieldType [context=EncapsulationContext [objectType=xy.phoyo.album.Configuration], fieldName=printerSupportedPapers]",
					ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(complexSignature));
			Assert.assertEquals("printerSupportedPapers.get",
					ReflectionUIUtils.extractMethodNameFromSignature(complexSignature));
			Assert.assertArrayEquals(new String[] {},
					ReflectionUIUtils.extractMethodParameterTypeNamesFromSignature(complexSignature));
			Assert.assertEquals(complexSignature, ReflectionUIUtils.buildMethodSignature(
					ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(complexSignature),
					ReflectionUIUtils.extractMethodNameFromSignature(complexSignature),
					Arrays.asList(ReflectionUIUtils.extractMethodParameterTypeNamesFromSignature(complexSignature))));
		}

		{
			String complexSignature = "int" + " " + "test"
					+ "(CapsuleFieldType [context=EncapsulationContext [objectType=xy.phoyo.album.Configuration], fieldName=printerSupportedPapers]"
					+ ", " + "java.io.File" + ", "
					+ "CapsuleFieldType [context=EncapsulationContext [objectType=xy.phoyo.album.Configuration], fieldName=printerSupportedPapers])";
			Assert.assertEquals("int", ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(complexSignature));
			Assert.assertEquals("test", ReflectionUIUtils.extractMethodNameFromSignature(complexSignature));
			Assert.assertArrayEquals(new String[] {
					"CapsuleFieldType [context=EncapsulationContext [objectType=xy.phoyo.album.Configuration], fieldName=printerSupportedPapers]",
					"java.io.File",
					"CapsuleFieldType [context=EncapsulationContext [objectType=xy.phoyo.album.Configuration], fieldName=printerSupportedPapers]" },
					ReflectionUIUtils.extractMethodParameterTypeNamesFromSignature(complexSignature));
			Assert.assertEquals(complexSignature, ReflectionUIUtils.buildMethodSignature(
					ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(complexSignature),
					ReflectionUIUtils.extractMethodNameFromSignature(complexSignature),
					Arrays.asList(ReflectionUIUtils.extractMethodParameterTypeNamesFromSignature(complexSignature))));
		}
	}

	@Test
	public void testDefaultPersistence() throws Exception {
		Date objectToSave = new Date();
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(objectToSave));
		File tmpFile = IOUtils.createTemporaryFile();;
		type.save(objectToSave, tmpFile);
		Date objectToLoad = new Date(0);
		type.load(objectToLoad, tmpFile);

		Assert.assertEquals(objectToSave, objectToLoad);
	}

	@Test
	public void testAutoCleanUpCache() throws Exception {
		String cacheName = "testAutoCleanUpWeakKeysCache.map";
		Map<Object, Object> cache = MiscUtils.newAutoCleanUpCache(true, false, 1, 1000, cacheName);
		cache.put(new Object(), new Object());
		Assert.assertEquals(1, cache.size());

		cache.put(new Object(), new Object());
		Assert.assertEquals(1, cache.size());

		boolean cacheThreadFound;
		cacheThreadFound = false;
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if (t.getName().contains(cacheName)) {
				cacheThreadFound = true;
				break;
			}
		}
		Assert.assertTrue(cacheThreadFound);

		tryToForceGarbageCollection();
		Thread.sleep(2000);
		Assert.assertEquals(0, cache.size());

		String referencedCacheName = "testAutoCleanUpWeakKeysCache.referencedCache";
		Accessor<Map<Object, Object>> cacheReference = Accessor
				.returning(MiscUtils.newAutoCleanUpCache(true, false, 10, 1000, referencedCacheName), true);
		cacheThreadFound = false;
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if (t.getName().contains(referencedCacheName)) {
				cacheThreadFound = true;
				break;
			}
		}
		Assert.assertTrue(cacheThreadFound);

		cacheReference.set(null);
		tryToForceGarbageCollection();
		Thread.sleep(3000);
		cacheThreadFound = false;
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if (t.getName().contains(referencedCacheName)) {
				cacheThreadFound = true;
				break;
			}
		}
		Assert.assertTrue(!cacheThreadFound);
	}

	@Test
	public void testGarbageCollection() throws Exception {
		if (!System.getProperty("java.version").contains("1.8")) {
			return;
		}
		final SwingRenderer swingRenderer = new SwingRenderer(new ReflectionUI());
		final WeakReference<?>[] objectWeakRef = new WeakReference[1];
		final WeakReference<?>[] formWeakRefs = new WeakReference[2];
		final StandardEditorBuilder[] builders = new StandardEditorBuilder[2];
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				TestObject testObject = new TestObject();
				builders[0] = swingRenderer.openObjectFrame(testObject);
				builders[1] = swingRenderer.openObjectDialog(null, testObject, null, null, false, false);
				objectWeakRef[0] = new WeakReference<TestObject>(testObject);
			}
		});
		Thread.sleep(1000);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				Form frameForm = SwingRendererUtils.findObjectDisplayedForms(objectWeakRef[0].get(), swingRenderer)
						.get(0);
				Form dialogForm = SwingRendererUtils.findObjectDisplayedForms(objectWeakRef[0].get(), swingRenderer)
						.get(1);
				Assert.assertTrue(SwingUtilities.getWindowAncestor(frameForm) instanceof JFrame);
				Assert.assertTrue(SwingUtilities.getWindowAncestor(dialogForm) instanceof JDialog);
				formWeakRefs[0] = new WeakReference<Form>(frameForm);
				formWeakRefs[1] = new WeakReference<Form>(dialogForm);
				builders[0].getCreatedFrame().dispose();
				builders[1].getCreatedDialog().dispose();
			}
		});
		builders[0] = null;
		builders[1] = null;
		tryToForceGarbageCollection();
		assertGarbageCollected(objectWeakRef[0], 10, 1000);
		assertGarbageCollected(formWeakRefs[0], 10, 1000);
		assertGarbageCollected(formWeakRefs[1], 10, 1000);
	}

	private void assertGarbageCollected(WeakReference<?> weakReference, int gcCount, int delayMilliseconds)
			throws InterruptedException {
		for (int i = 0; i < gcCount; i++) {
			if (weakReference.get() == null) {
				return;
			}
			Thread.sleep(delayMilliseconds);
			tryToForceGarbageCollection();
		}
		Assert.fail();
	}

	private void tryToForceGarbageCollection() {
		System.gc();
	}

	@Test
	public void testObjectCycle() throws Exception {
		TestObject cycle = new TestObject(null);
		cycle.setOther(new TestObject(cycle));
		TestObject cycle2 = new TestObject(null);
		cycle2.setOther(new TestObject(cycle2));
		ReflectionUIUtils.copyFieldValuesAccordingInfos(ReflectionUI.getDefault(), cycle, cycle2, true);
		Assert.assertTrue(cycle.getOther().getOther() == cycle);
		Assert.assertTrue(cycle2.getOther().getOther() == cycle2);
		Assert.assertTrue(ReflectionUIUtils.equalsAccordingInfos(cycle, cycle2, ReflectionUI.getDefault(),
				IInfoFilter.DEFAULT));
	}

	public static class TestObject {
		TestObject other;

		public TestObject(TestObject other) {
			this.other = other;
		}
		public TestObject() {
			this(null);
		}
		public TestObject getOther() {
			return other;
		}

		public void setOther(TestObject other) {
			this.other = other;
		}

	}

}
