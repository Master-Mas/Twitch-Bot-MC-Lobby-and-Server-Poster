package org.bitbucket.master_mas.mcBotMod;

import org.bitbucket.master_mas.twitchBotMC.Launcher;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid="MCBotMod", name="MCBotMod", version="1.0.0")
public class MCBotMod {
	
	@Instance(value = "MCBotMod")
	public static MCBotMod instance;
	
	@SidedProxy(clientSide="org.bitbucket.master_mas.mcBotMod.client.ClientProxy", serverSide="org.bitbucket.master_mas.mcBotMod.CommomProxy", modId="MCBotMod")
	public static CommonProxy proxy;
	
	private Launcher twitchBot;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		twitchBot = new Launcher(false);
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
	}
}
