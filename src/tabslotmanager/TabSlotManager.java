/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package tabslotmanager;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class TabSlotManager extends JavaPlugin implements Listener {

	private ProtocolManager pManager;

	public void onEnable() {
		pManager = ProtocolLibrary.getProtocolManager();
		loadConfig();
		initLoginListener();
		for (Player player : getServer().getOnlinePlayers()) {
			limitTablistName(player);
		}
		getServer().getPluginManager().registerEvents(this, this);
	}

	public void onDisable() {
		pManager.removePacketListeners(this);
		pManager = null;
	}


	private int slots = 100;
	private void loadConfig() {
		File configfile = new File(this.getDataFolder(),"config.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configfile);
		slots = config.getInt("slots", slots);
		config = new YamlConfiguration();
		config.set("slots", slots);
		try {config.save(configfile);} catch (IOException e) {}
	}


	private void initLoginListener() {
		pManager.addPacketListener(
			new PacketAdapter(
				PacketAdapter
				.params(this, PacketType.Play.Server.LOGIN)
				.optionAsync()
			) {
				@Override
				public void onPacketSending(PacketEvent event) {
					event.getPacket().getIntegers().write(2, slots);
				}
			}
		);
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		limitTablistName(event.getPlayer());
	}

	private void limitTablistName(Player player) {
		if (!player.hasPermission("tabslotmanager.unlimitedlength")) {
			int limit = 16;
			if (slots > 60 && slots <= 80) {
				limit = 13;
			} else if (slots > 80 && slots <= 100) {
				limit = 10;
			} else if (slots > 100 && slots <= 120) {
				limit = 8;
			} else if (slots > 120 && slots <= 140) {
				limit = 6;
			} else if (slots > 140 && slots <= 160) {
				limit = 5;
			}
			limit = Math.min(limit, player.getPlayerListName().length());
			player.setPlayerListName(player.getPlayerListName().substring(0, limit));
		}
	}

}
