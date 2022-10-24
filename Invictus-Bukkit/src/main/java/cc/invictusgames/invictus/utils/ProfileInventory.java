package cc.invictusgames.invictus.utils;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import cc.invictusgames.invictus.profile.Profile;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Credits for the idea of this system go to FrozenOrb (https://frozenorb.net)
 *
 * @author Emilxyz (langgezockt@gmail.com)
 * 22.06.2020 / 20:03
 * Invictus / cc.invictusgames.invictus.spigot.utils
 */

public class ProfileInventory extends PlayerInventory {

    private static final Invictus invictus = Invictus.getInstance();
    @Getter
    private static final Map<UUID, ProfileInventory> cache = new HashMap<>();
    @Getter
    private static final List<UUID> open = new ArrayList<>();

    public static ProfileInventory getInventory(Profile profile, Player player) {
        cache.putIfAbsent(player.getUniqueId(), new ProfileInventory(profile, player));
        return cache.get(player.getUniqueId());
    }

    private Profile profile;
    private CraftPlayer owner;
    private boolean online;
    private CraftInventory inventory;
    private ItemStack[] extra = new ItemStack[5];

    private ProfileInventory(Profile profile, Player player) {
        super(((CraftPlayer) player).getHandle());
        this.profile = profile;
        this.inventory = new CraftInventory(this);
        this.owner = (CraftPlayer) player;
        this.online = player.isOnline();
        this.items = this.player.inventory.items;
        this.armor = this.player.inventory.armor;
        for (int i = 0; i < extra.length; i++) {
            extra[i] =
                    CraftItemStack.asNMSCopy(new ItemBuilder(Material.STAINED_GLASS_PANE, 15).setDisplayName(" ").build());
        }
        ProfileInventory.cache.put(player.getUniqueId(), this);
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] contents = new ItemStack[this.getSize()];
        System.arraycopy(this.items, 0, contents, 0, this.items.length);
        System.arraycopy(this.items, 0, contents, this.items.length, this.armor.length);
        return contents;
    }

    public void handelJoin(Player joined) {
        if (!online) {
            CraftPlayer player = (CraftPlayer) joined;
            player.getHandle().inventory.items = items;
            player.getHandle().inventory.armor = armor;
            online = true;
            Bukkit.getScheduler().scheduleAsyncDelayedTask(InvictusBukkitPlugin.getInstance(), () -> owner.saveData());
        }
    }

    public void handleQuit(Player quit) {
        online = false;
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        super.onClose(who);
        if ((who instanceof Player) && (!online)) {
            ((Player) who).sendMessage(CC.RED + "Saving inventory of offline player...");
        }
        ProfileInventory.open.remove(who.getUniqueId());
        Bukkit.getScheduler().scheduleAsyncDelayedTask(InvictusBukkitPlugin.getInstance(), () -> owner.saveData());
    }

    public ItemStack getItem(int i) {
        if (i <= 35) {
            return items[getReversedItemSlotNum(i)];
        } else if (i <= 39) {
            return armor[getReversedArmorSlotNum(i - 36)];
        } else {
            if (i == 43) {
                return CraftItemStack.asNMSCopy(new ItemBuilder(Material.SPECKLED_MELON)
                        .setDisplayName(CC.GOLD + "Health: " + CC.WHITE + (int) owner.getHealth())
                        .setAmount((int) owner.getHealth())
                        .build());
            } else if (i == 44) {
                List<String> lore = new ArrayList<>();
                owner.getActivePotionEffects().forEach(effect -> {
                    String effectName = WordUtils.capitalizeFully(effect.getType().getName().replace("_", " "));
                    String effectDuration = TimeUtils.formatHHMMSS(effect.getDuration() / 20, false, TimeUnit.SECONDS);
                    lore.add(CC.GOLD + effectName + " " + (effect.getAmplifier() + 1) + ": " + CC.WHITE + effectDuration);
                });
                return CraftItemStack.asNMSCopy(new ItemBuilder(Material.BLAZE_POWDER)
                        .setDisplayName(CC.GOLD + "Active Effects")
                        .setLore(lore)
                        .build());
            }
            return extra[i - 40];
        }
    }

    public void setItem(int i, ItemStack itemstack) {
        if (i <= 35) {
            items[getReversedItemSlotNum(i)] = itemstack;
        } else if (i <= 39) {
            armor[getReversedArmorSlotNum(i - 36)] = itemstack;
        }
        this.owner.getHandle().defaultContainer.b();
    }

    public ItemStack splitStack(int i, final int j) {
        ItemStack item;
        ItemStack[] items;
        int realSlot;
        if (i <= 35) {
            realSlot = getReversedItemSlotNum(i);
            items = this.items;
            item = this.items[realSlot];
        } else if (i <= 39) {
            realSlot = getReversedArmorSlotNum(i - 36);
            items = armor;
            item = armor[realSlot];
        } else {
            return null;
            /*realSlot = i - 40;
            items = extra;
            item = extra[realSlot];*/
        }

        if (item == null) {
            return null;
        }

        if (item.count <= j) {
            items[realSlot] = null;
            return item;
        }

        ItemStack itemStack = item.cloneAndSubtract(j);

        if (item.count == 0) {
            items[realSlot] = null;
        }
        return itemStack;
    }

    public ItemStack splitWithoutUpdate(int i) {
        ItemStack item;
        ItemStack[] items;
        int realSlot;
        if (i <= 35) {
            realSlot = getReversedItemSlotNum(i);
            items = this.items;
            item = this.items[realSlot];
        } else if (i <= 39) {
            realSlot = getReversedArmorSlotNum(i - 36);
            items = armor;
            item = armor[realSlot];
        } else {
            return null;
            /*realSlot = i - 40;
            items = extra;
            item = extra[realSlot];*/
        }

        if (item == null) {
            return null;
        }

        items[realSlot] = null;
        return item;
    }

    public int getSize() {
        return super.getSize() + 5;
    }

    public Inventory getBukkitInventory() {
        return inventory;
    }

    public String getInventoryName() {
        return "Inventory: " + profile.getDisplayName((CommandSender) null);
    }

    private int getReversedItemSlotNum(final int i) {
        if (i >= 27) {
            return i - 27;
        }
        return i + 9;
    }

    private int getReversedArmorSlotNum(final int i) {
        if (i == 0) {
            return 3;
        }
        if (i == 1) {
            return 2;
        }
        if (i == 2) {
            return 1;
        }
        if (i == 3) {
            return 0;
        }
        return i;
    }
}
