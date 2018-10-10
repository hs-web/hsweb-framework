package org.hswebframework.web.authorization;

import org.hswebframework.web.authorization.token.*;
import org.hswebframework.web.id.IDGenerator;
import org.junit.Assert;
import org.redisson.Redisson;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.codec.FstCodec;
import org.redisson.codec.SerializationCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class RedisUserTokenManagerTests {

    static DefaultUserTokenManager userTokenManager;

    static String token = IDGenerator.MD5.generate();

    private static Logger logger = LoggerFactory.getLogger("hsweb.session");

    public static void main(String[] args) throws InterruptedException {
        RedissonClient client = Redisson.create();

        try {
            ConcurrentMap<String, SimpleUserToken> repo = client.getMap("hsweb.user-token", new SerializationCodec());
            ConcurrentMap<String, Set<String>> userRepo = client.getMap("hsweb.user-token-u", new SerializationCodec());

            userTokenManager = new DefaultUserTokenManager(repo, userRepo) {
                @Override
                protected Set<String> getUserToken(String userId) {
                    userRepo.computeIfAbsent(userId,u->new HashSet<>());

                    return client.getSet("hsweb.user-token-"+userId, new SerializationCodec());
                }

            };

            userTokenManager.setAllopatricLoginMode(AllopatricLoginMode.deny);
//            userTokenManager=new DefaultUserTokenManager();


//            userRepo.clear();
//            repo.clear();
//            for (int i = 0; i < 1000; i++) {
//                userTokenManager.signIn(IDGenerator.MD5.generate(), "sessionId", "admin", 60*3600*1000);
//            }
//            userTokenManager.signIn(IDGenerator.MD5.generate(), "sessionId", "admin2", 60*3600*1000);

            testGet();
            testGetAll();
            testSignOut();

            testGetAll();
        } finally {
            client.shutdown();
        }
    }
    public static void testSignOut(){
        userTokenManager.signOutByUserId("admin");

    }
    public static void testGet() {
        List<UserToken> userToken = userTokenManager.getByUserId("admin");
        Assert.assertTrue(!userToken.isEmpty());
    }

    public static void testGetAll() {
        logger.warn("total user : " + userTokenManager.totalUser());
        logger.warn("total token : " + userTokenManager.totalToken());

        userTokenManager.allLoggedUser(token -> System.out.println(token.getToken()));
    }
}
