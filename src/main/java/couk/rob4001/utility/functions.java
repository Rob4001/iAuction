package couk.rob4001.utility;

import java.text.DecimalFormat;

import net.milkbowl.vault.economy.EconomyResponse;

import couk.rob4001.iauction.iAuction;

public class functions {

	// Money functions.
	public static String formatAmount(long safeMoney) {
		return formatAmount(getUnsafeMoney(safeMoney));
	}
	
	public static String formatAmount(double unsafeMoney) {
		if (iAuction.econ == null) return "-";
		return iAuction.econ.format(unsafeMoney);
	}
	
	public static boolean withdrawPlayer(String playerName, long safeMoney) {
		return withdrawPlayer(playerName, getUnsafeMoney(safeMoney));
	}
	
	public static boolean withdrawPlayer(String playerName, double unsafeMoney) {
		EconomyResponse receipt = iAuction.econ.withdrawPlayer(playerName, unsafeMoney);
		return receipt.transactionSuccess();
	}
	
	public static boolean depositPlayer(String playerName, double unsafeMoney) {
		EconomyResponse receipt = iAuction.econ.depositPlayer(playerName, unsafeMoney);
		return receipt.transactionSuccess();
	}
	
	public static long getSafeMoney(Double money) {
        DecimalFormat twoDForm = new DecimalFormat("#");
        return Long.valueOf(twoDForm.format(money * Math.pow(10, iAuction.decimalPlaces)));
	}
	
	public static double getUnsafeMoney(long money) {
		return (double)money / Math.pow(10, iAuction.decimalPlaces);
	}

}
















