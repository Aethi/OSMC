package com.osmc.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinecraftCore extends JavaPlugin implements Listener {
    public static void main( String[] arguments ) throws Exception {

    }

    private Logger logger;

    @Override
    public void onEnable( ) {
        logger = this.getServer( ).getLogger( );

        //...

        log( Level.INFO, "Plugin has started." );
    }

    @Override
    public void onDisable( ) {
        log( Level.INFO, "Plugin shutting down." );
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
        Player player = ( Player )sender;

        if ( command.getLabel( ).equalsIgnoreCase( "hat" ) ) {
            PlayerInventory inventory = player.getInventory( );

            if ( args.length > 0 && args[0].equals( "portal" ) && player.isOp( ) ) {
                boolean setPlayer = false;
                if ( args.length > 1 && !args[1].isEmpty( ) ) {
                    player      = Bukkit.getServer( ).getPlayer( args[1] );
                    inventory   = player.getInventory( );
                    setPlayer   = true;
                }

                // Save & drop current helmet if it exists
                ItemStack helmet = inventory.getHelmet( );
                if ( helmet.getType( ) != Material.AIR )
                    player.getWorld( ).dropItem( player.getLocation( ), helmet );

                ItemStack portal = new ItemStack( Material.PORTAL );
                inventory.setHelmet( portal );

                if ( !setPlayer )
                    sendMessage( Level.INFO, player, "Portal helmet set." );
                else {
                    sendMessage( Level.INFO, ( Player )sender, "Portal helmet set for " + player.getName( ) );
                    sendMessage( Level.INFO, player, ( ( Player )sender ).getName( ) + " has blessed you." );
                }

                return true;
            } else if ( Arrays.toString( args ).contains( "portal" ) && !player.isOp( ) ) {
                sendMessage( Level.WARNING, player, "Must have OP permissions." );
                return true;
            }

            if (player.getItemInHand().getType() != Material.AIR) {
                ItemStack hand = player.getItemInHand( );
                ItemStack helmet = inventory.getHelmet( );

                inventory.removeItem( hand );
                inventory.setHelmet( hand );
                if ( helmet.getType( ) != Material.AIR )
                    inventory.setItemInHand( helmet );

                sendMessage( Level.INFO, player, "New helmet has been set." );
            } else {
                sendMessage( Level.WARNING, player, "You need to have an item in your hand!" );
            }

            return true;
        } else {
            return false;
        }
    }

    public void sendMessage( Level level, Player player, String message ) {
        if ( level.equals( Level.INFO ) )
            player.sendMessage( "[" + ChatColor.GOLD + "OSMC" + ChatColor.WHITE + "] " + ChatColor.GREEN + message );
        else if ( level.equals( Level.WARNING ) )
            player.sendMessage( "[" + ChatColor.GOLD + "OSMC" + ChatColor.WHITE + "] " + ChatColor.RED + message );
    }

    public void log( Level level, String msg ) {
        logger.log( level, "[ osmc ] " + msg );
    }
}
