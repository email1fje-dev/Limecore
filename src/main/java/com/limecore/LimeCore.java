package com.limecore;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LimeCore extends JavaPlugin implements Listener, CommandExecutor {
    private static final String TITLE = ChatColor.DARK_GREEN + "LimeCore Admin Panel";
    private boolean eventRunning;

    @Override public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("ap")).setExecutor(this);
        getLogger().info("LimeCore enabled.");
    }
    @Override public void onDisable() { getLogger().info("LimeCore disabled."); }

    @Override public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player p)) { s.sendMessage("Only players can use /ap."); return true; }
        if (!p.isOp() && !p.hasPermission("limecore.admin")) {
            p.sendMessage(ChatColor.RED + "You do not have permission."); return true;
        }
        openPanel(p); return true;
    }

    private void openPanel(Player p) {
        Inventory inv=Bukkit.createInventory(null,27,TITLE);
        inv.setItem(10, button(Material.CLOCK, ChatColor.GOLD+"Random Event","Start a random event."));
        inv.setItem(12, button(Material.TNT, ChatColor.LIGHT_PURPLE+"Chaos Event","Trigger chaos."));
        inv.setItem(14, button(Material.ZOMBIE_HEAD, ChatColor.RED+"Mob Event","Trigger mob invasion."));
        inv.setItem(16, button(Material.BARRIER, ChatColor.RED+"Stop Event","Stop current event."));
        inv.setItem(22, button(Material.EMERALD, ChatColor.GREEN+"Status",
                eventRunning ? "An event is active." : "No event is active."));
        p.openInventory(inv);
    }
    private ItemStack button(Material m,String name,String lore){
        ItemStack i=new ItemStack(m); ItemMeta meta=i.getItemMeta();
        if(meta!=null){meta.setDisplayName(name);meta.setLore(List.of(ChatColor.GRAY+lore));i.setItemMeta(meta);} return i;
    }
    @EventHandler public void click(InventoryClickEvent e){
        if(!TITLE.equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        if(!(e.getWhoClicked() instanceof Player p) || (!p.isOp() && !p.hasPermission("limecore.admin"))) return;
        switch(e.getRawSlot()){case 10->randomEvent();case 12->chaos();case 14->mobEvent();case 16->stopEvent();default->{}}
        p.closeInventory();
    }
    private void randomEvent(){switch(ThreadLocalRandom.current().nextInt(3)){case 0->bloodMoon();case 1->mobEvent();default->chaos();}}
    private void bloodMoon(){
        eventRunning=true; Bukkit.broadcastMessage(ChatColor.DARK_RED+"BLOOD MOON");
        Bukkit.broadcastMessage(ChatColor.RED+"Something has changed in the world...");
        for(World w:Bukkit.getWorlds()){w.setTime(18000);w.setStorm(true);w.setThundering(true);}
        laterStop(120);
    }
    private void mobEvent(){
        eventRunning=true; Bukkit.broadcastMessage(ChatColor.RED+"MOB INVASION");
        for(Player p:Bukkit.getOnlinePlayers()) p.getWorld().spawnEntity(p.getLocation(),EntityType.ZOMBIE);
        laterStop(60);
    }
    private void chaos(){
        eventRunning=true; Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE+"CHAOS EVENT");
        for(Player p:Bukkit.getOnlinePlayers()) p.sendTitle(ChatColor.LIGHT_PURPLE+"CHAOS",ChatColor.GRAY+"Something is wrong.",10,50,10);
        laterStop(30);
    }
    private void laterStop(long seconds){Bukkit.getScheduler().runTaskLater(this,()->{if(eventRunning)stopEvent();},20L*seconds);}
    private void stopEvent(){eventRunning=false;Bukkit.broadcastMessage(ChatColor.GREEN+"The current LimeCore event has ended.");}
}