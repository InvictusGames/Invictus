package cc.invictusgames.invictus.totp;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.10.2020 / 01:44
 * Invictus / cc.invictusgames.invictus.spigot.totp
 */

@RequiredArgsConstructor
public class ImageMapRenderer extends MapRenderer {

    private final UUID targetPlayer;
    private final BufferedImage image;

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (!player.getUniqueId().equals(targetPlayer)) {
            return;
        }

        mapCanvas.drawImage(0, 0, this.image);
    }
}
