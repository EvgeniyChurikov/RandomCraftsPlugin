package me.needenoughsleep.randomcraftsplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.List;

public final class RandomCraftsPlugin extends JavaPlugin implements Listener {
    Map<ItemStack, ItemStack> data = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("randomcrafts")).setExecutor(this);
        getDataFolder().mkdir();
        load();
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        ItemStack newResult = data.get(e.getInventory().getResult());
        if (newResult != null) {
            e.getInventory().setResult(newResult);
        }
    }

    public void randomize() {
        data.clear();
        Iterator<Recipe> recipeIterator = getServer().recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe instanceof ShapedRecipe
                    || recipe instanceof ShapelessRecipe) {
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
        if (command.equalsIgnoreCase("save")) {
            save();
            getServer().sendMessage(Component.text(data.size() + " crafts saved").color(TextColor.color(0, 255, 0)));
            return true;
        }
        if (command.equalsIgnoreCase("load")) {
            load();
            getServer().sendMessage(Component.text(data.size() + " crafts loaded").color(TextColor.color(0, 255, 0)));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("randomize", "load", "save");
        }
        return new ArrayList<>();
    }

    private void save() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(getDataFolder() + "/recipes.txt"));
            for(Map.Entry<ItemStack, ItemStack> entry : data.entrySet()) {
                out.write(entry.getKey().getType().toString() + " " + entry.getKey().getAmount()
                        + " --> " + entry.getValue().getType().toString() + " " + entry.getValue().getAmount());
                out.newLine();
            }
            out.close();
        }
        catch (IOException e) {
            getServer().getLogger().warning("ERROR by saving recipes");
            getServer().getLogger().warning(e.getMessage());
        }
    }

    private void load() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(getDataFolder() + "/recipes.txt"));
            data.clear();
            String line = in.readLine();
            while (line != null) {
                String[] splited = line.split(" ");
                data.put(new ItemStack(Objects.requireNonNull(Material.getMaterial(splited[0])), Integer.parseInt(splited[1])),
                        new ItemStack(Objects.requireNonNull(Material.getMaterial(splited[3])), Integer.parseInt(splited[4])));
                line = in.readLine();
            }
        }
        catch (FileNotFoundException e) {
            data.clear();
        }
        catch (IOException e) {
            getServer().getLogger().warning("ERROR by loading recipes");
            getServer().getLogger().warning(e.getMessage());
        }
    }
}
