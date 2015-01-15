/*
 * Twitch MCBot - Posts which servers you are in, lobbies and mini-games.
 * Copyright (C) 2015 Sam Murphy
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.bitbucket.master_mas.twitchBotMC.commands;

import org.bitbucket.master_mas.twitchBotMC.Launcher;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.MessageEvent;

public class CommandHandler {

	private Launcher launcher;

	public enum CommandsList {
		SERVER,
		LOBBY,
		MINIGAME,
		MCBOTNOT,
		MCBOTGETUSER,
		MCBOTDISCONNECT,
		MCBOTMUTE;
	}
	
	public CommandHandler(Launcher launcher) {
		this.launcher = launcher;
	}
	
	public void handle(String command, String[] args, MessageEvent<PircBotX> event) {
		CommandsList commandV = CommandsList.valueOf(command.replace("!", "").split(" ")[0].toUpperCase());
		if(commandV == null)
			return;
		
		switch(commandV) {
			case LOBBY:
				new CommandLocation(command, args, launcher, event);
				break;
			case MCBOTDISCONNECT:
				new CommandMCBotDisconnect(command, args, launcher, event);
				break;
			case MCBOTGETUSER:
				new CommandMCBotGetUser(command, args, launcher, event);
				break;
			case MCBOTMUTE:
				new CommandMCBotMute(command, args, launcher, event);
				break;
			case MCBOTNOT:
				new CommandMCBotNot(command, args, launcher, event);
				break;
			case MINIGAME:
				new CommandLocation(command, args, launcher, event);
				break;
			case SERVER:
				new CommandServer(command, args, launcher, event);
				break;
			default:
				System.err.println(commandV + " doesn't have a registered command class");
				break;
			
		}
	}
}