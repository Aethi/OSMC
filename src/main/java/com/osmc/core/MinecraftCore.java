package com.osmc.core;

import com.osmc.core.fun.Hat;
import com.osmc.core.fun.Particles;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

public class MinecraftCore extends JavaPlugin implements Listener {
    /*
     * Core variables
     */
    public static void main( String[] arguments ) {

    }

    private Logger logger;
    private String updateURL;

    /*
     * [..].fun.* shared variables
     */
    public Set<UUID> particleUsers = new HashSet<>();

    /*
     * Plugin Core
     */

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

        // fun.Particles
        getServer( ).getPluginManager( ).registerEvent( Event.Type.PLAYER_MOVE, new Particles( this ), Event.Priority.Highest, this );
        getServer( ).getPluginCommand( "smoke" ).setExecutor( new Particles( this ) );

        // fun.hat
        getServer( ).getPluginCommand( "hat" ).setExecutor( new Hat( this ) );

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
        if ( command.getLabel( ).equalsIgnoreCase( "osmc" ) ) {
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
        }

        return false;
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
