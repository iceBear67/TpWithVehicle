package io.ib67.bukkit.tpwithvehicle;

import me.zombie_striker.qg.exp.cars.VehicleEntity;
import me.zombie_striker.qg.exp.cars.api.QualityArmoryVehicles;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TpWithVehicle extends JavaPlugin implements Listener{
    private final Map<UUID,UUID> ridingPlayers=new HashMap<>();
    private Field passagers;
    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            passagers = QualityArmoryVehicles.class.getField("passagers");
            passagers.setAccessible(true);
        }catch(Throwable f){
            f.printStackTrace();
            setEnabled(false);
        }
        Bukkit.getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    @EventHandler
    public void onMount(VehicleEnterEvent event){
        if(event.getEntered().getType()== EntityType.PLAYER){
            if(QualityArmoryVehicles.isVehicle(event.getVehicle())){
                ridingPlayers.put(event.getEntered().getUniqueId(),event.getVehicle().getUniqueId());
            }
        }
    }
    @EventHandler
    public void onDisMount(VehicleExitEvent event){
        if(event.getExited().getType()==EntityType.PLAYER){
            ridingPlayers.remove(event.getExited().getUniqueId());
        }
    }

    /**
     * Maybe cause problem in multi-player vehicles.
     * @param event
     * @throws IllegalAccessException
     */
    @EventHandler
    @SuppressWarnings("unchecked")
    public void onTeleport(PlayerTeleportEvent event) throws IllegalAccessException {
        if(ridingPlayers.containsKey(event.getPlayer().getUniqueId())){
            VehicleEntity vehicle=QualityArmoryVehicles.getVehicleEntity(Bukkit.getEntity(ridingPlayers.get(event.getPlayer().getUniqueId())));
            Map<Integer, Entity> pos= (Map<Integer, Entity>) passagers.get(vehicle);
            vehicle.teleport(event.getTo());
            passagers.set(vehicle,pos);
            vehicle.updateSeats();
        }
    }
}
