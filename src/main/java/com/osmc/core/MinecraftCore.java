package com.osmc.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MinecraftCore extends JavaPlugin implements Listener {
    public static void main( String[] arguments ) {

    }

    private Logger logger;
    private String updateURL;

    private boolean isOutdated( ) {
        try {
            URL url = new URL("https://api.github.com/repos/Aethi/OSMC/releases");

            HttpURLConnection conn = ( HttpURLConnection ) url.openConnection( );
            conn.setRequestMethod( "GET" );
            conn.addRequestProperty( "Accept", "application/vnd.github+json" );
            conn.addRequestProperty( "X-GitHub-Api-Version", "2022-11-28" );
            conn.connect( );

            log( Level.INFO, "Connection: " + conn.getResponseMessage() );
            if ( conn.getResponseCode( ) == 200 ) {
                /*
                 * Yeah, this is fucking horrible
                 */
                BufferedReader buffer = new BufferedReader( new InputStreamReader( url.openStream( ), StandardCharsets.UTF_8 ) );
                String readAPIResponse;
                StringBuilder jsonString = new StringBuilder( );
                while ( ( readAPIResponse = buffer.readLine( ) ) != null ) {
                    jsonString.append( readAPIResponse );
                }

                JSONArray jsonArray = new JSONArray( jsonString.toString( ) );
                JSONObject json = jsonArray.getJSONObject( 0 );

                String remoteVersion  = json.get( "tag_name" ).toString( ).substring( 8 );
                String currentVersion = this.getDescription( ).getVersion( );

                updateURL = json.getJSONArray( "assets" ).getJSONObject( 0 ).get( "browser_download_url" ).toString( );

                conn.disconnect( );
                return !currentVersion.equals( remoteVersion );
            }
        } catch ( Exception e ) {
            log( Level.SEVERE, e.getMessage( ) );
            return false;
        }

        return false;
    }

    @Override
    public void onEnable( ) {
        logger = this.getServer( ).getLogger( );

        log( Level.INFO, "Checking for new version..." );
        if ( isOutdated( ) ) {
            log( Level.WARNING, "Plugin is outdated, run /osmc update" );
        } else {
            log( Level.INFO, "Plugin is up-to-date." );
        }

        log( Level.INFO, "Plugin has started." );
    }

    @Override
    public void onDisable( ) {
        log( Level.INFO, "Plugin shutting down." );
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
        if ( !( sender instanceof Player ) )
            return false;

        Player player = ( Player )sender;
        switch ( command.getLabel( ).toLowerCase( ) ) {
            case "osmc":
                if ( args.length > 0 && args[0].equals( "update" ) && player.isOp( ) ) {
                    if ( this.updateURL.isEmpty( ) ) {
                        sendMessage( Level.INFO, player, "Checking for new version..." );
                        if ( isOutdated( ) ) {
                            sendMessage( Level.WARNING, player, "New version found!" );
                        } else {
                            sendMessage( Level.INFO, player, "Plugin is up-to-date." );

                            this.updateURL = "";
                            return true;
                        }
                    }

                    sendMessage( Level.INFO, player, "Downloading update..." );

                    try ( BufferedInputStream in = new BufferedInputStream( new URL( updateURL ).openStream( ) );
                          FileOutputStream fileOutputStream = new FileOutputStream( this.getFile( ).getAbsolutePath( ) ) ) {

                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        while ( ( bytesRead = in.read( dataBuffer, 0, 1024 ) ) != -1 ) {
                            fileOutputStream.write( dataBuffer, 0, bytesRead );
                        }

                        sendMessage( Level.INFO, player, "OSMC updated, please /reload the server." );

                        this.updateURL = "";
                        return true;
                    } catch ( Exception e ) {
                        sendMessage( Level.WARNING, player, e.getMessage( ) );
                    }
                }

                sendMessage( Level.INFO, player, "OSMC Version: " + this.getDescription( ).getVersion( ) );

                this.updateURL = "";
                return true;
            case "hat":
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
            default:
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
