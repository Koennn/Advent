package me.koenn.advent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public final class Advent extends JavaPlugin implements Listener {

    private final HashMap<UUID, List<String>> retrievedDates = new HashMap<>();

    private JSONManager days;
    private JSONManager jsonManager;

    @Override
    public void onEnable() {
        days = new JSONManager(this, "days.json");
        jsonManager = new JSONManager(this, "data.json");

        this.loadRetrievedDates();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.saveRetrievedDates();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!this.retrievedDates.containsKey(player.getUniqueId())) {
            this.retrievedDates.put(player.getUniqueId(), new ArrayList<>());
        }

        String currentDate = new SimpleDateFormat("dd_MM").format(new Date());
        if (this.retrievedDates.get(player.getUniqueId()).contains(currentDate)) {
            return;
        }

        this.retrievedDates.get(player.getUniqueId()).add(currentDate);
        this.saveRetrievedDates();

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () ->
                ((JSONArray) this.days.getFromBody(currentDate)).forEach(command ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ((String) command).replace("%player%", player.getName()))), 20);
    }

    private void saveRetrievedDates() {
        JSONObject json = new JSONObject();
        for (UUID uuid : this.retrievedDates.keySet()) {
            JSONArray dates = new JSONArray();
            dates.addAll(this.retrievedDates.get(uuid));
            json.put(uuid.toString(), dates);
        }
        this.jsonManager.setInBody("data", json);
    }

    private void loadRetrievedDates() {
        JSONObject json = (JSONObject) this.jsonManager.getFromBody("data");
        if (json == null) {
            return;
        }

        for (Object user : json.keySet()) {
            UUID uuid = UUID.fromString((String) user);
            JSONArray datesJson = (JSONArray) json.get(user);
            List<String> dates = new ArrayList<>();
            for (Object date : datesJson) {
                dates.add((String) date);
            }
            this.retrievedDates.put(uuid, dates);
        }
    }
}
