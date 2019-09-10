package xy.reflect.ui.example;

import java.io.IOException;
import java.util.Objects;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * ATM simulator GUI generated using only the javax.sound.* API and the XML
 * declarative customizations. Inspired by:
 * https://www.youtube.com/watch?v=NAO2aTSz3mQ
 * 
 * @author olitank
 *
 */
public class ATMSimulator {

	private static final long MONEY_AMOUNT1 = 10;
	private static final long MONEY_AMOUNT2 = 20;
	private static final long MONEY_AMOUNT3 = 50;
	private static final long MONEY_AMOUNT4 = 100;
	private static final long MONEY_AMOUNT5 = 200;
	private static final long MONEY_AMOUNT6 = 500;
	private static final long MONEY_AMOUNT7 = 1000;
	private static final String CARD_CODE = "1234";

	public static void main(String[] args) throws IOException {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "atmSimulator.icu");
		renderer.openObjectFrame(new ATMSimulator());
	}

	private boolean cardInserted = false;
	private long moneyAmount = 0;
	private boolean receiptPrinted = false;
	private State state = State.IDLE;
	private String cardCode = "";
	private String otherMoneyAmountString = "";

	private void pushNumericPadButton(int n) {
		if (state == State.AUTHENTICATING) {
			cardCode += Objects.toString(n);
		} else if (state == State.OTHER_TRANSACTION_AMOUNT_CHOSEN) {
			otherMoneyAmountString += Objects.toString(n);
		}
	}

	public void pushButton0() {
		pushNumericPadButton(0);
	}

	public void pushButton1() {
		pushNumericPadButton(1);
	}

	public void pushButton2() {
		pushNumericPadButton(2);
	}

	public void pushButton3() {
		pushNumericPadButton(3);
	}

	public void pushButton4() {
		pushNumericPadButton(4);
	}

	public void pushButton5() {
		pushNumericPadButton(5);
	}

	public void pushButton6() {
		pushNumericPadButton(6);
	}

	public void pushButton7() {
		pushNumericPadButton(7);
	}

	public void pushButton8() {
		pushNumericPadButton(8);
	}

	public void pushButton9() {
		pushNumericPadButton(9);
	}

	public void pushButtonEnter() {
		if (state == State.AUTHENTICATING) {
			if (isCardCodeValid()) {
				state = State.AUTHENTICATED;
			} else {
				state = State.AUTHENTICATION_FAILED;
			}
		} else if (state == State.AUTHENTICATION_FAILED) {
			cardCode = "";
			state = State.AUTHENTICATING;
		} else if (state == State.OTHER_TRANSACTION_AMOUNT_CHOSEN) {
			moneyAmount = Long.valueOf(otherMoneyAmountString);
			receiptPrinted = true;
			otherMoneyAmountString = "";
			state = State.TRANSACTION_AMOUNT_CHOSEN;
		}
	}

	private boolean isCardCodeValid() {
		return CARD_CODE.equals(cardCode);
	}

	public void pushButtonCancel() {
		if (state != State.IDLE) {
			state = State.TRANSACTION_FINISHED;
		}
	}

	public void pushButtonClear() {
		if (state == State.AUTHENTICATING) {
			cardCode = "";
		} else if (state == State.OTHER_TRANSACTION_AMOUNT_CHOSEN) {
			otherMoneyAmountString = "";
		}
	}

	public String getScreenLine1() {
		if (state == State.IDLE) {
			return "Insert your card!";
		} else if (state == State.AUTHENTICATING) {
			return "Enter your card code:";
		} else if (state == State.AUTHENTICATION_FAILED) {
			return "Invalid code! Press 'Enter' to retry!";
		} else if (state == State.AUTHENTICATED) {
			return Objects.toString(MONEY_AMOUNT1);
		} else if (state == State.TRANSACTION_AMOUNT_CHOSEN) {
			return "Retrieve your money!";
		} else if (state == State.OTHER_TRANSACTION_AMOUNT_CHOSEN) {
			return "Type the amount and press 'Enter':";
		} else if (state == State.TRANSACTION_FINISHED) {
			return "Retrieve your card!";
		} else {
			return "";
		}
	}

	public String getScreenLine2() {
		if (state == State.AUTHENTICATING) {
			return cardCode.replaceAll(".", "*");
		} else if (state == State.AUTHENTICATED) {
			return Objects.toString(MONEY_AMOUNT2);
		} else if (state == State.OTHER_TRANSACTION_AMOUNT_CHOSEN) {
			return otherMoneyAmountString;
		} else {
			return "";
		}
	}

	public String getScreenLine3() {
		if (state == State.AUTHENTICATED) {
			return Objects.toString(MONEY_AMOUNT3);
		} else {
			return "";
		}
	}

	public String getScreenLine4() {
		if (state == State.AUTHENTICATED) {
			return Objects.toString(MONEY_AMOUNT4);
		} else {
			return "";
		}
	}

	public String getScreenLine5() {
		if (state == State.AUTHENTICATING) {
			return "(the code is " + CARD_CODE + ")";
		} else if (state == State.AUTHENTICATED) {
			return Objects.toString(MONEY_AMOUNT5);
		} else {
			return "";
		}
	}

	public String getScreenLine6() {
		if (state == State.AUTHENTICATED) {
			return Objects.toString(MONEY_AMOUNT6);
		} else {
			return "";
		}
	}

	public String getScreenLine7() {
		if (state == State.AUTHENTICATED) {
			return Objects.toString(MONEY_AMOUNT7);
		} else {
			return "";
		}
	}

	public String getScreenLine8() {
		if (state == State.AUTHENTICATED) {
			return "Other";
		} else {
			return "";
		}
	}

	private void moneyAmontChosen(long moneyAmount) {
		this.moneyAmount = moneyAmount;
		receiptPrinted = true;
		state = State.TRANSACTION_AMOUNT_CHOSEN;
	}

	public void pushButtonScreenLeftSide1() {
		if (state == State.AUTHENTICATED) {
			moneyAmontChosen(MONEY_AMOUNT1);
		}
	}

	public void pushButtonScreenLeftSide2() {
		if (state == State.AUTHENTICATED) {
			moneyAmontChosen(MONEY_AMOUNT2);
		}
	}

	public void pushButtonScreenLeftSide3() {
		if (state == State.AUTHENTICATED) {
			moneyAmontChosen(MONEY_AMOUNT3);
		}
	}

	public void pushButtonScreenLeftSide4() {
		if (state == State.AUTHENTICATED) {
			moneyAmontChosen(MONEY_AMOUNT4);
		}
	}

	public void pushButtonScreenRightSide1() {
		if (state == State.AUTHENTICATED) {
			moneyAmontChosen(MONEY_AMOUNT5);
		}
	}

	public void pushButtonScreenRightSide2() {
		if (state == State.AUTHENTICATED) {
			moneyAmontChosen(MONEY_AMOUNT6);
		}
	}

	public void pushButtonScreenRightSide3() {
		if (state == State.AUTHENTICATED) {
			moneyAmontChosen(MONEY_AMOUNT7);
		}
	}

	public void pushButtonScreenRightSide4() {
		if (state == State.AUTHENTICATED) {
			otherMoneyAmountString = "";
			state = State.OTHER_TRANSACTION_AMOUNT_CHOSEN;
		}
	}

	public String getCardStatus() {
		return cardInserted ? "Inserted" : "Removed";
	}

	public String getMoneyStatus() {
		return Objects.toString(moneyAmount);
	}

	public String getReceiptStatus() {
		return receiptPrinted ? "Printed" : "None";
	}

	public void insertCard() {
		cardInserted = true;
		if (state == State.IDLE) {
			cardCode = "";
			state = State.AUTHENTICATING;
		}
	}

	public void removeCard() {
		cardInserted = false;
		state = State.IDLE;
	}

	public void takeMoney() {
		moneyAmount = 0;
		if (state == State.TRANSACTION_AMOUNT_CHOSEN) {
			state = State.TRANSACTION_FINISHED;
		}
	}

	public void takeReceipt() {
		receiptPrinted = false;
	}

	private static enum State {
		IDLE, AUTHENTICATING, AUTHENTICATED, AUTHENTICATION_FAILED, TRANSACTION_AMOUNT_CHOSEN, OTHER_TRANSACTION_AMOUNT_CHOSEN, TRANSACTION_FINISHED
	}

}
