package couk.rob4001.iauction.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import junit.framework.Assert;

import net.minecraft.server.EnchantmentThorns;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import couk.rob4001.util.cardboard.CardboardBox;


@RunWith(PowerMockRunner.class)
public class CardboardBoxTests {

	@Test
	public void test() {
		//Implement mock server
		TestServer.getInstance();
		
		for(Material mat:Material.values()){
			if(mat == Material.AIR) continue;
			System.out.println("Testing : " + mat.toString());
			ItemStack item = new ItemStack(mat);
			
			ItemMeta im = Bukkit.getServer().getItemFactory().getItemMeta(mat);
			if (im instanceof BookMeta)
				((BookMeta)im).addPage("Hello World");
			if (im instanceof LeatherArmorMeta)
				((LeatherArmorMeta)im).setColor(Color.AQUA);
			if (im instanceof MapMeta)
				((MapMeta)im).setScaling(true);
			if (im instanceof SkullMeta)
				((SkullMeta)im).setOwner("Rob4001");
			if (im instanceof FireworkMeta)
				((FireworkMeta)im).setPower(2);
			if (im instanceof FireworkEffectMeta)
				((FireworkEffectMeta)im).setEffect(FireworkEffect.builder().flicker(true).withColor(Color.GREEN).build());
			if (im instanceof EnchantmentStorageMeta)
				((EnchantmentStorageMeta)im).addStoredEnchant(Enchantment.DIG_SPEED, 1, true);
			item.setItemMeta(im);
			
			CardboardBox box = new CardboardBox(item);
			
			try {
				FileOutputStream fos = new FileOutputStream(new File("lots.auction"));
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(box);
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			CardboardBox newbox = null;
			if (new File("lots.auction").exists()) {
				try {
					FileInputStream fis = new FileInputStream(new File("lots.auction"));
					ObjectInputStream ois = new ObjectInputStream(fis);
					newbox = (CardboardBox) ois
							.readObject();
					ois.close();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			
			ItemStack newitem = newbox.unbox();
			
			Assert.assertEquals(item, newitem);
		}
	}

}
