package couk.rob4001.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import net.milkbowl.vault.economy.EconomyResponse;

import couk.rob4001.iauction.AuctionLot;
import couk.rob4001.iauction.iAuction;

public class functions {

	// Money functions.
	public static String formatAmount(long safeMoney) {
		return formatAmount(getUnsafeMoney(safeMoney));
	}

	public static String formatAmount(double unsafeMoney) {
		if (iAuction.econ == null)
			return "-";
		return iAuction.econ.format(unsafeMoney);
	}

	public static boolean withdrawPlayer(String playerName, long safeMoney) {
		return withdrawPlayer(playerName, getUnsafeMoney(safeMoney));
	}

	public static boolean withdrawPlayer(String playerName, double unsafeMoney) {
		EconomyResponse receipt = iAuction.econ.withdrawPlayer(playerName,
				unsafeMoney);
		return receipt.transactionSuccess();
	}

	public static boolean depositPlayer(String playerName, double unsafeMoney) {
		EconomyResponse receipt = iAuction.econ.depositPlayer(playerName,
				unsafeMoney);
		return receipt.transactionSuccess();
	}

	public static long getSafeMoney(Double money) {
		DecimalFormat twoDForm = new DecimalFormat("#");
		return Long.valueOf(twoDForm.format(money
				* Math.pow(10, iAuction.decimalPlaces)));
	}

	public static double getUnsafeMoney(long money) {
		return (double) money / Math.pow(10, iAuction.decimalPlaces);
	}

	public static void saveObject(Object arraylist, String filename) {
		File saveFile = new File(iAuction.dataFolder, filename);

		try {
			// use buffering
			if (saveFile.exists())
				saveFile.delete();
			OutputStream file = new FileOutputStream(saveFile.getAbsolutePath());
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try {
				output.writeObject(arraylist);
			} finally {
				output.close();
			}
		} catch (IOException ex) {
			return;
		}
	}

	@SuppressWarnings({ "unchecked", "finally" })
	public static ArrayList<String> loadArrayListString(String filename) {
		File saveFile = new File(iAuction.dataFolder, filename);
		ArrayList<String> importedObjects = new ArrayList<String>();
		try {
			// use buffering
			InputStream file = new FileInputStream(saveFile.getAbsolutePath());
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			importedObjects = (ArrayList<String>) input.readObject();
			input.close();
		} finally {
			return importedObjects;
		}
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<AuctionLot> loadArrayListAuctionLot(String filename) {
		File saveFile = new File(iAuction.dataFolder, filename);
		ArrayList<AuctionLot> importedObjects = new ArrayList<AuctionLot>();
		try {
			// use buffering
			InputStream file = new FileInputStream(saveFile.getAbsolutePath());
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			importedObjects = (ArrayList<AuctionLot>) input.readObject();
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
		return importedObjects;
	}

}
