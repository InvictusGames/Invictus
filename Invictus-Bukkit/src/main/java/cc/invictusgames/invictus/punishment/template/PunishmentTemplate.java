package cc.invictusgames.invictus.punishment.template;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ItemUtils;
import cc.invictusgames.invictus.punishment.Punishment;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Data
public class PunishmentTemplate {

    private String reason = "Example";
    private String material = Material.DIAMOND_SWORD.name();
    private List<String> description = Collections.singletonList("Example punishment");
    private List<Offense> offenses = Collections.singletonList(new Offense());

    public ItemStack toItem() {
        List<String> lore = new ArrayList<>();
        lore.add(" ");

        for (Offense offense : offenses) {
            if (offense.isHiddenInLore())
                continue;

            if (offense.getType() == Punishment.PunishmentType.BAN
                    || offense.getType() == Punishment.PunishmentType.MUTE)
                lore.add(CC.YELLOW + " - " + CC.RED + (offense.getDuration().equals("perm")
                        ? "Permanent" : offense.getDuration())
                        + " " + offense.getType().getName());
            else lore.add(CC.YELLOW + " - " + CC.RED + offense.getType().getName());
        }

        if (!description.isEmpty()) {
            lore.add(" ");
            lore.addAll(CC.translate(description));
        }

        return new ItemBuilder(ItemUtils.get(material))
                .setDisplayName(CC.YELLOW + CC.BOLD + reason)
                .setLore(lore)
                .build();
    }

    @NoArgsConstructor
    @Getter
    public class Offense {

        private String name = "1st";
        private String duration = "perm";
        private Punishment.PunishmentType type = Punishment.PunishmentType.BAN;
        private String material = Material.INK_SACK.name() + ":" + DyeColor.LIME.getDyeData();
        private boolean hiddenInLore = false;

        public ItemStack toItem() {
            List<String> lore = new ArrayList<>();
            lore.add(CC.YELLOW + "Type: " + CC.RED + type.getName());

            if (type == Punishment.PunishmentType.BAN || type == Punishment.PunishmentType.MUTE)
                lore.add(CC.YELLOW + "Duration: " + CC.RED + (duration.equals("perm") ? "Permanent" : duration));

            return new ItemBuilder(ItemUtils.get(material))
                    .setDisplayName(CC.YELLOW + name)
                    .setLore(lore)
                    .build();
        }

        public void execute(CommandSender sender, String reason, String target, boolean append) {
            StringBuilder command = new StringBuilder();
            switch (type) {
                case BAN:
                    command.append("tempban ")
                            .append(target)
                            .append(" ")
                            .append(duration);
                    break;
                case MUTE:
                    command.append("tempmute ")
                            .append(target)
                            .append(" ")
                            .append(duration);
                    break;
                case KICK:
                    command.append("warn ")
                            .append(target)
                            .append(" --kick");
                    break;
                case WARN:
                    command.append("warn ").append(target);
                    break;
                default:
                    return;
            }

            command.append(" ").append(reason);
            if (append)
                command.append(" (").append(name).append(")");

            Bukkit.dispatchCommand(sender, command.toString());
        }

    }

}
