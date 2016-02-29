import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.imageio.ImageIO;

import xy.reflect.ui.ReflectionUI;

public class TestOtherControls {

	public Color color = Color.BLACK;
	public Date date = new Date();
	public Image image;

	public TestOtherControls() throws IOException {
		image = ImageIO.read(
				new URL("http://stories.gettyimages.com/wp-content/uploads/2015/08/GettyImages-557187411-11.jpg"));
	}

	public static void main(String[] args) throws IOException {
		new ReflectionUI().getSwingRenderer().openObjectDialog(null, new TestOtherControls(), true);
	}

}
