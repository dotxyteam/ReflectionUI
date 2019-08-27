package xy.reflect.ui.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * Restaurant management application GUI generated with the XML declarative
 * customizations.
 * 
 * @author olitank
 *
 */
public class Restaurant {

	public static void main(String[] args) throws IOException {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "restaurant.icu");
		renderer.openObjectFrame(new Restaurant());
	}

	private List<Order> orders = Arrays.asList(
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Delivered),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Delivered),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Delivered),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Delivered),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Delivered),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.OnHold),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.OnHold),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.OnHold),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.OnHold),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.OnHold),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Pending),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Pending),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Pending),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Pending),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Pending),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Pending),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Pending),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Pending),
			new Order("Chicken Sandwich", 5, "Dotxyteam", new Date(), OrderStatus.Pending));

	public List<Order> getOrders() {
		return orders;
	}

	public List<Order> getFilteredOrders(String filter) {
		if ((filter == null) || (filter.length() == 0)) {
			return orders;
		}
		List<Order> result = new ArrayList<Order>();
		for (Order order : orders) {
			if (order.getProductName().contains(filter)) {
				result.add(order);
			} else if (order.getCompanyName().contains(filter)) {
				result.add(order);
			} else if (order.getStatus().name().contains(filter)) {
				result.add(order);
			}
		}
		return result;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public int getTotalOrder() {
		return orders.size();
	}

	public int getTotalDelivered() {
		int result = 0;
		for (Order order : orders) {
			if (order.getStatus() == OrderStatus.Delivered) {
				result++;
			}
		}
		return result;
	}

	public int getTotalPending() {
		int result = 0;
		for (Order order : orders) {
			if (order.getStatus() == OrderStatus.Pending) {
				result++;
			}
		}
		return result;
	}

	public int getTotalOnHold() {
		int result = 0;
		for (Order order : orders) {
			if (order.getStatus() == OrderStatus.OnHold) {
				result++;
			}
		}
		return result;
	}

	public List<String> getCustomers() {
		return Arrays.asList("Dotxyteam");
	}

	public List<String> getMenus() {
		return Arrays.asList("menu1");
	}

	public List<String> getPackages() {
		return Arrays.asList("package1");
	}

	public List<String> getSettings() {
		return Arrays.asList("setting1");
	}

	public void logOut() {

	}

	public static class Order {
		private String companyName;
		private Date deliveryDate;
		private int amount;
		private String productName;
		private OrderStatus status = OrderStatus.Delivered;

		public Order(String productName, int amount, String companyName, Date deliveryDate, OrderStatus status) {
			super();
			this.productName = productName;
			this.amount = amount;
			this.companyName = companyName;
			this.deliveryDate = deliveryDate;
			this.status = status;
		}

		public String getCompanyName() {
			return companyName;
		}

		public void setCompanyName(String companyName) {
			this.companyName = companyName;
		}

		public Date getDeliveryDate() {
			return deliveryDate;
		}

		public void setDeliveryDate(Date deliveryDate) {
			this.deliveryDate = deliveryDate;
		}

		public int getAmount() {
			return amount;
		}

		public void setAmount(int amount) {
			this.amount = amount;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public OrderStatus getStatus() {
			return status;
		}

		public void setStatus(OrderStatus status) {
			this.status = status;
		}

	}

	public enum OrderStatus {
		Delivered, Pending, OnHold
	}
}
