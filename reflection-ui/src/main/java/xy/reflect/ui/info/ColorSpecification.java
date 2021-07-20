


package xy.reflect.ui.info;

import java.io.Serializable;

/**
 * This is a renderer-independent color specification class.
 * 
 * @author olitank
 *
 */
public class ColorSpecification implements Serializable {
	private static final long serialVersionUID = 1L;

	protected int red = 128;
	protected int green = 128;
	protected int blue = 128;

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blue;
		result = prime * result + green;
		result = prime * result + red;
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
		ColorSpecification other = (ColorSpecification) obj;
		if (blue != other.blue)
			return false;
		if (green != other.green)
			return false;
		if (red != other.red)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ColorSpecification [red=" + red + ", green=" + green + ", blue=" + blue + "]";
	}

}
