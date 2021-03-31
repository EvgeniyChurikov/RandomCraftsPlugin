package me.needenoughsleep.randomcraftsplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;

public final class RandomCraftsPlugin extends JavaPlugin implements Listener {
    Map<ItemStack, ItemStack> data = new HashMap<>();

    @Override
    public void onEnable() {
        configInit();
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("randomcrafts")).setExecutor(this);
        randomize();
    }

    private void configInit() {
        getConfig().addDefault("randomize-furnace-recipes", false);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        ItemStack newResult = data.get(e.getInventory().getResult());
        if (newResult != null) {
            e.getInventory().setResult(newResult);
        }
    }

    @EventHandler
    public void onSmelt(FurnaceSmeltEvent e) {
        if (!getConfig().getBoolean("randomize-furnace-recipes"))
            return;
        ItemStack newResult = data.get(e.getResult());
        if (newResult != null) {
            e.setResult(newResult);
        }
    }

    public void randomize() {
        data.clear();
        Iterator<Recipe> recipeIterator = getServer().recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe instanceof ShapedRecipe
                    || recipe instanceof ShapelessRecipe
                    || (recipe instanceof FurnaceRecipe && getConfig().getBoolean("randomize-furnace-recipes"))) {
                ItemStack newResult = new ItemStack(recipe.getResult().getType(), recipe.getResult().getAmount());
                data.put(newResult, newResult);
            }
        }
        List<ItemStack> newResults = new ArrayList<>(data.values());
        Collections.shuffle(newResults);
        Iterator<ItemStack> newResultIterator = newResults.iterator();
        data.replaceAll((r, v) -> newResultIterator.next());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (args.length < 1) {
            sender.sendMessage("missed argument");
            return false;
        }
        String command = args[0];
        if (command.equalsIgnoreCase("randomize")) {
            randomize();
            getServer().sendMessage(Component.text(data.size() + " crafts updated").color(TextColor.color(0, 255, 0)));
            return true;
        }
        if (command.equalsIgnoreCase("reload")) {
            reloadConfig();
            getServer().sendMessage(Component.text("Config reloaded").color(TextColor.color(0, 255, 0)));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("randomize", "reload");
        }
        return new ArrayList<>();
    }
}
