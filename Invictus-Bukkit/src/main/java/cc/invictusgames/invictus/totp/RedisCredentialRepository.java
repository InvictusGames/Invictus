package cc.invictusgames.invictus.totp;

import cc.invictusgames.invictus.Invictus;
import com.warrenstrange.googleauth.ICredentialRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.10.2020 / 01:01
 * Invictus / cc.invictusgames.invictus.spigot.totp
 */

@RequiredArgsConstructor
public class RedisCredentialRepository implements ICredentialRepository {

    private final Invictus invictus;

    @Override
    public String getSecretKey(String userName) {
        return invictus.getRedisService().executeBackendCommand(redis -> redis.get("totp:" + userName + ":secretKey"));
    }

    @Override
    public void saveUserCredentials(String userName, String secretKey, int validationCode, List<Integer> scratchCodes) {
        invictus.getRedisService().executeBackendCommand(redis -> {
            redis.set("totp:" + userName + ":secretKey", secretKey);
            redis.set("totp:" + userName + ":validationCode", String.valueOf(validationCode));
            redis.set("totp:" + userName + ":scratchCodes", StringUtils.join(scratchCodes, ","));
            return null;
        });
    }
}
