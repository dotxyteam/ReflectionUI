package xy.reflect.ui;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MiscTests {

	@Test
	public void testMethodSignature() throws Exception {
		String signature = "void add(java.awt.Point)";
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo rectangleTypeInfo = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(new Rectangle()));
		IMethodInfo addPointMethodInfo = ReflectionUIUtils.findMethodBySignature(rectangleTypeInfo.getMethods(),
				signature);
		Assert.assertNotNull(addPointMethodInfo);
	}

	@Test
	public void testDefaultPersistence() throws Exception {
		Date objectToSave = new Date();
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(objectToSave));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		type.save(objectToSave, out);
		Date objectToLoad = new Date(0);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		type.load(objectToLoad, in);

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

		System.gc();
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
		System.gc();
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
		final SwingRenderer renderer = new SwingRenderer(new ReflectionUI());
		final WeakReference<?>[] objectWeakRef = new WeakReference[1];
		final WeakReference<?>[] formWeakRef = new WeakReference[1];
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				StandardEditorBuilder builder = renderer.openObjectFrame(new TestObject());
				objectWeakRef[0] = new WeakReference<TestObject>((TestObject) builder.getCurrentValue());
				formWeakRef[0] = new WeakReference<Form>(builder.getCreatedEditorForm());
				builder.getCreatedFrame().dispose();
			}
		});
		System.gc();
		Assert.assertTrue(objectWeakRef[0].get() == null);
		Assert.assertTrue(formWeakRef[0].get() == null);
	}

	public static class TestObject {

	}

}
