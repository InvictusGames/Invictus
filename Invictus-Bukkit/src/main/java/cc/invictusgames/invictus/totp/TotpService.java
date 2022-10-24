package cc.invictusgames.invictus.totp;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.10.2020 / 01:31
 * Invictus / cc.invictusgames.invictus.spigot.totp
 */

public class TotpService {

    private static final String TOTP_URL_FORMAT = "otpauth://totp/%s?secret=%s&issuer=%s";
    private static final String QR_CODE_URL_FORMAT = "https://www.google.com/chart?chs=130x130&chld=M%%7C0&cht=qr&chl=%s";

    private final InvictusBukkit invictus;
    @Getter
    private final GoogleAuthenticator authenticator;
    @Getter
    private final RedisCredentialRepository credentialRepository;

    public TotpService(InvictusBukkit invictus) {
        this.invictus = invictus;
        this.authenticator = new GoogleAuthenticator();
        this.credentialRepository = new RedisCredentialRepository(invictus);
        authenticator.setCredentialRepository(this.credentialRepository);
    }

    public ItemStack createQRMap(Player player, GoogleAuthenticatorKey key) {
        Escaper escaper = UrlEscapers.urlFragmentEscaper();
        String url = String.format(
                TotpService.TOTP_URL_FORMAT,
                escaper.escape(player.getName()),
                key.getKey(),
                escaper.escape(invictus.getMessageService().formatMessage("network-name")) + " Network");

        String qrUrl = String.format(TotpService.QR_CODE_URL_FORMAT, URLEncoder.encode(url));
        BufferedImage image = null;

        try {
            image = ImageIO.read(new URL(qrUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        MapView mapView = InvictusBukkitPlugin.getInstance().getServer().createMap(player.getWorld());
        mapView.getRenderers().forEach(mapView::removeRenderer);
        mapView.addRenderer(new ImageMapRenderer(player.getUniqueId(), image));
        player.sendMap(mapView);
        return new ItemBuilder(Material.MAP, mapView.getId())
                .setDisplayName(CC.PINK + "QR-Code")
                .setLore(Collections.singletonList(CC.GRAY + "QR-Code"))
                .build();
    }

}
