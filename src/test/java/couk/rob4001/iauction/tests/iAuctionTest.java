package couk.rob4001.iauction.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.bukkit.Server;
import org.bukkit.craftbukkit.Main;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class iAuctionTest {

	@BeforeClass
	public static void startServer(){
		Server server = TestServer.getInstance();
		server.getPluginManager().registerInterface(JavaPluginLoader.class);
				try {
					server.getPluginManager().loadPlugin(new File("target/test/iAuction.jar"));
				} catch (UnknownDependencyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidPluginException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidDescriptionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}


}
