package com.osmc.core.fun;

import com.osmc.core.MinecraftCore;
import org.bukkit.Effect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.logging.Level;

public class Particles extends PlayerListener implements CommandExecutor {
    private MinecraftCore plugin;

    public Particles( MinecraftCore plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String s, String[] strings ) {
        if ( !( sender instanceof Player ) )
            return false;

        Player player = ( Player ) sender;
        if ( !player.isOp( ) ) {
            plugin.sendMessage( Level.WARNING, player, "Certified operators only" );

            return true;
        }

        if ( plugin.particleUsers.contains( player.getUniqueId( ) ) ) {
            plugin.sendMessage( Level.INFO, player, "Smoke disabled" );

            plugin.particleUsers.remove( player.getUniqueId( ) );
        } else {
            plugin.sendMessage( Level.INFO, player, "Smoke enabled" );

            plugin.particleUsers.add( player.getUniqueId( ) );
        }

        return true;
    }

    @Override
    public void onPlayerMove( PlayerMoveEvent event ) {
        Player player = event.getPlayer( );

        // Yeah, I'm sure this is very resource efficient......
        if ( player.isOp( ) && plugin.particleUsers.contains( player.getUniqueId( ) ) )
            player.getWorld( ).playEffect( player.getLocation( ).add( 0, 1, 1 ), Effect.SMOKE, 1, 48 );
    }
}
