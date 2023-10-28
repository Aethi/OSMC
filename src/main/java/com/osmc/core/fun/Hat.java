package com.osmc.core.fun;

import com.osmc.core.MinecraftCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.logging.Level;

public class Hat implements CommandExecutor {
    private MinecraftCore plugin;

    public Hat( MinecraftCore plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String s, String[] args ) {
        if ( !( sender instanceof Player ) )
            return false;

        Player player = ( Player )sender;
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
            if ( helmet.getType( ) != Material.AIR ) {
                if ( inventory.contains( new ItemStack( Material.AIR ) ) ) {
                    inventory.addItem( helmet );
                } else {
                    player.getWorld( ).dropItem( player.getLocation( ), helmet );
                }
            }

            ItemStack portal = new ItemStack( Material.PORTAL );
            inventory.setHelmet( portal );

            if ( !setPlayer )
                plugin.sendMessage( Level.INFO, player, "Portal helmet set." );
            else {
                plugin.sendMessage( Level.INFO, ( Player )sender, "Portal helmet set for " + player.getName( ) );
                plugin.sendMessage( Level.INFO, player, ( ( Player )sender ).getName( ) + " has blessed you." );
            }

            return true;
        } else if ( Arrays.toString( args ).contains( "portal" ) && !player.isOp( ) ) {
            plugin.sendMessage( Level.WARNING, player, "Must have OP permissions." );
            return true;
        }

        if (player.getItemInHand().getType() != Material.AIR) {
            ItemStack hand = player.getItemInHand( );
            ItemStack helmet = inventory.getHelmet( );

            inventory.removeItem( hand );
            inventory.setHelmet( hand );
            if ( helmet.getType( ) != Material.AIR )
                inventory.setItemInHand( helmet );

            plugin.sendMessage( Level.INFO, player, "New helmet has been set." );
        } else {
            plugin.sendMessage( Level.WARNING, player, "You need to have an item in your hand!" );
        }

        return true;
    }
}
