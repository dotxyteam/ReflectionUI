package xy.reflect.ui.example;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * Fast Food Restaurant GUI generated using only the XML declarative
 * customizations.
 * 
 * @author olitank
 *
 */
public class FastFood implements Serializable {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		final FastFood fastFood = new FastFood();

		final File fastFoodFile = new File("fastFood.db");
		if (fastFoodFile.exists()) {
			fastFood.load(fastFoodFile);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				fastFood.save(fastFoodFile);
			}

		});

		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "fastFood.icu");
		renderer.openObjectFrame(fastFood);
	}

	private List<Product> products = new ArrayList<Product>();
	private String currency = "$";
	private transient Cart cart = new Cart();
	private List<Sale> sales = new ArrayList<Sale>();

	public List<Sale> getSales() {
		return sales;
	}

	public void setSales(List<Sale> sales) {
		this.sales = sales;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public Cart getCart() {
		return cart;
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public List<ProductSuggestion> getProductSuggestions(ProductCategory category) {
		List<ProductSuggestion> result = new ArrayList<ProductSuggestion>();
		for (Product p : products) {
			if (p.getCategory() == category) {
				ProductSuggestion ps = new ProductSuggestion(this, p);
				result.add(ps);
			}
		}
		return result;
	}

	public int checkOut(CreditCardData crediCardData) throws Exception {
		crediCardData.validate();
		Sale newSale = new Sale(cart, new Date(), crediCardData.getOwnerName());
		sales.add(newSale);
		cart = new Cart();
		return newSale.hashCode();
	}

	public void save(File file) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(this);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to serialize object: " + t.toString());
		} finally {
			try {
				oos.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void load(File file) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(file));
			FastFood loaded = (FastFood) ois.readObject();
			sales = loaded.sales;
			currency = loaded.currency;
			products = loaded.products;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to deserialize object: " + t.toString());
		} finally {
			try {
				ois.close();
			} catch (Exception ignore) {
			}
		}
	}

	public enum ProductCategory {
		Menu, Sandwich, Drink
	}

	public static class Product implements Serializable {

		private static final long serialVersionUID = 1L;

		private String name;
		private double price;
		private transient Image image;
		private ProductCategory category;

		private void writeObject(ObjectOutputStream oos) throws IOException {
			oos.defaultWriteObject();
			if (image == null) {
				oos.writeObject(null);
			} else {
				oos.writeObject(new ImageIcon(image));
			}
		}

		private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
			ois.defaultReadObject();
			ImageIcon tmpImageIcon = (ImageIcon) ois.readObject();
			if (tmpImageIcon == null) {
				image = null;
			} else {
				image = tmpImageIcon.getImage();
			}
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public Image getImage() {
			return image;
		}

		public void setImage(Image image) {
			this.image = image;
		}

		public ProductCategory getCategory() {
			return category;
		}

		public void setCategory(ProductCategory category) {
			this.category = category;
		}

		@Override
		public String toString() {
			return "[" + category + "] " + name;
		}

	}

	public static class CartEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		private Product product;
		private int quantity;

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		public Product getProduct() {
			return product;
		}

		public void setProduct(Product product) {
			this.product = product;
		}

		public double getTotalPrice() {
			return product.getPrice() * quantity;
		}

		@Override
		public String toString() {
			return quantity + " x " + product;
		}

	}

	public static class Cart implements Serializable {

		private static final long serialVersionUID = 1L;

		private List<CartEntry> entries = new ArrayList<CartEntry>();

		public List<CartEntry> getEntries() {
			return entries;
		}

		public void setEntries(List<CartEntry> entries) {
			this.entries = entries;
		}

		public double getTotalPrice() {
			double result = 0;
			for (CartEntry e : entries) {
				result += e.getTotalPrice();
			}
			return result;
		}

		@Override
		public String toString() {
			return "Total price=" + getTotalPrice() + ": " + entries;
		}

	}

	public static class ProductSuggestion {

		private FastFood fastFood;
		private Product product;

		public ProductSuggestion(FastFood fastFood, Product product) {
			super();
			this.fastFood = fastFood;
			this.product = product;
		}

		public String getName() {
			return product.getName();
		}

		public String getFormattedPrice() {
			return product.getPrice() + " " + fastFood.getCurrency();
		}

		public Image getImage() {
			return product.getImage();
		}

		public CartEntry addToCart() {
			Cart cart = fastFood.getCart();
			for (CartEntry entry : cart.getEntries()) {
				if (entry.getProduct() == product) {
					entry.setQuantity(entry.getQuantity() + 1);
					return entry;
				}
			}
			CartEntry newEntry = new CartEntry();
			newEntry.setProduct(product);
			newEntry.setQuantity(1);
			List<CartEntry> entries = cart.getEntries();
			entries = new ArrayList<CartEntry>(entries);
			entries.add(newEntry);
			cart.setEntries(entries);
			return newEntry;
		}

	}

	public static class Sale implements Serializable {

		private static final long serialVersionUID = 1L;

		private Cart cart;
		private Date date;
		private String creditCardOwnerName;

		public Sale(Cart cart, Date date, String creditCardOwnerName) {
			super();
			this.cart = cart;
			this.date = date;
			this.creditCardOwnerName = creditCardOwnerName;
		}

		public Cart getCart() {
			return cart;
		}

		public Date getDate() {
			return date;
		}

		public String getCreditCardOwnerName() {
			return creditCardOwnerName;
		}
	}

	public static class CreditCardData implements Serializable {

		private static final long serialVersionUID = 1L;

		private String ownerName;
		private Type type;
		private String number;
		private String code;
		private int expirationMonth;
		private int expirationYear;

		public String getOwnerName() {
			return ownerName;
		}

		public void validate() throws Exception {
			if (type == null) {
				throw new Exception("Card type name not specified!");
			}
			if ((ownerName == null) || (ownerName.length() == 0)) {
				throw new Exception("Owner name not specified!");
			}
			if ((number == null) || (number.length() == 0)) {
				throw new Exception("Card number not specified!");
			}
			if ((code == null) || (code.length() == 0)) {
				throw new Exception("Code name not specified!");
			}
			if (expirationMonth == 0) {
				throw new Exception("Expiration month not specified!");
			}
			if (expirationYear == 0) {
				throw new Exception("Expiration year not specified!");
			}
		}

		public void setOwnerName(String ownerName) {
			this.ownerName = ownerName;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public String getNumber() {
			return number;
		}

		public void setNumber(String number) {
			this.number = number;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public int getExpirationMonth() {
			return expirationMonth;
		}

		public void setExpirationMonth(int expirationMonth) {
			this.expirationMonth = expirationMonth;
		}

		public int getExpirationYear() {
			return expirationYear;
		}

		public void setExpirationYear(int expirationYear) {
			this.expirationYear = expirationYear;
		}

		public enum Type {
			Visa, Mastercard
		}
	}

}