package test.quxiqi.sharding.sphere.utils;

import com.google.common.base.Stopwatch;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis RedLock Distribute Lock Java Implement<br>
 * Theoretically not safe due to heavily depend on local and remote system time,thus any time jump will cause a failure.<br>
 * see http://antirez.com/news/101<br>
 * since there isn't any better distribute lock algorithm base on Redis,thus using RedLock as a compromise.
 */
@SuppressWarnings("Duplicates")
public final class RedisLockBaseOnRedisTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockBaseOnRedisTemplate.class);

    private static final int ONE_SECOND = 1000;

    private static final int DEFAULT_EXPIRY_TIME_MILLIS = 60 * ONE_SECOND;
    private static final int DEFAULT_ACQUIRE_TIMEOUT_MILLIS = 20 * ONE_SECOND;
    private static final int DEFAULT_ACQUIRE_RESOLUTION_MILLIS = 100;
    private static final int DEFAULT_OPERATE_TIME_LIMIT_MILLIS = 50;

    private final String lockKey;

    private final int lockExpiryInMillis;
    private final int acquireTimeoutInMillis;
    private final int acquireResolutionMillis;
    private final UUID lockUUID;

    private boolean isLocked;

    private long lockAcquiredTime;

    /**
     * the Millis time limit before committing a operate<br>
     * if a operate commit when the lock has a lockExpiryInMillis less than this time limit,the program will consider it lose the lock<br>
     * and refuse to commit and return false immediately.
     */
    private final int operateTimeLimit;

    private static final DefaultRedisScript<Long> deleteIfOwnedLuaSnippet = new DefaultRedisScript<>();
    private static final DefaultRedisScript<String> getIfNotOwnedByOthersLuaSnippet = new DefaultRedisScript<>();
    private static final DefaultRedisScript<String> renewIfOwnedLuaSnippet = new DefaultRedisScript<>();


    static {
        try {
            initRenewIfOwnedluaSnippet();
            initDeleteIfOwnedLuaSnippet();
            initGetIfNotOwnedbyOthersLuaSnippet();
        } catch (Exception e) {
            LOGGER.error("RedisLockBaseOnRedisTemplate : Error When initialize RedisScript,due to:", e);
        }
    }

    /**
     * Lua Script:delete resource's lock only if owning it(Atomic)
     */
    private static final String DELETE_IF_OWNED_LUA_SNIPPET =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return -1 end";

    /**
     * Lua Script:getting resource's lock when it's not own by other(Atomic)
     * SET key value [EX seconds] [PX milliseconds] [NX|XX]
     */
    private static final String GET_IF_NOT_OWNED_BY_OTHERS_LUA_SNIPPET =
//            "local result = redis.call('set', KEYS[1], ARGV[1], 'PX', ARGV[2] ,'NX') if result == 'OK' return true else return -1 end";
            "return redis.call('set', KEYS[1], ARGV[1], 'PX', ARGV[2] ,'NX')";

    /**
     * Lua Script:renew a lock when own it(Atomic)
     * SET key value [EX seconds] [PX milliseconds] [NX|XX]
     */
    private static final String RENEW_IF_OWNED_LUA_SNIPPET =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('set',KEYS[1],ARGV[1],'PX',ARGV[2],'XX') " +
                    "else return 'Lock Owned By Others' end";

    /**
     * initialize psetexCASLuaScript
     *
     * @throws Exception in case of thread interruption
     */
    private static void initRenewIfOwnedluaSnippet() throws Exception {
        renewIfOwnedLuaSnippet.setScriptText(RENEW_IF_OWNED_LUA_SNIPPET);
        renewIfOwnedLuaSnippet.setResultType(String.class);
        renewIfOwnedLuaSnippet.afterPropertiesSet();
    }

    /**
     * get psetexCASLuaScript
     *
     * @return DefaultRedisScript
     */
    public DefaultRedisScript<String> getRenewIfOwnedluaSnippet() {
        return renewIfOwnedLuaSnippet;
    }

    /**
     * initialize psetexCASLuaScript
     *
     * @throws Exception DefaultRedisScript Config Exception
     */
    private static void initDeleteIfOwnedLuaSnippet() throws Exception {
        deleteIfOwnedLuaSnippet.setScriptText(DELETE_IF_OWNED_LUA_SNIPPET);
        deleteIfOwnedLuaSnippet.setResultType(Long.class);
        deleteIfOwnedLuaSnippet.afterPropertiesSet();
    }

    /**
     * get psetexCASLuaScript
     *
     * @return DefaultRedisScript
     */
    public DefaultRedisScript<Long> getDeleteIfOwnedLuaSnippet() {
        return deleteIfOwnedLuaSnippet;
    }

    /**
     * initialize setCASLuaScript
     *
     * @throws Exception in case of thread interruption
     */
    private static void initGetIfNotOwnedbyOthersLuaSnippet() throws Exception {
        getIfNotOwnedByOthersLuaSnippet.setScriptText(GET_IF_NOT_OWNED_BY_OTHERS_LUA_SNIPPET);
        getIfNotOwnedByOthersLuaSnippet.setResultType(String.class);
        getIfNotOwnedByOthersLuaSnippet.afterPropertiesSet();
    }

    /**
     * get setCASLuaScript
     *
     * @return DefaultRedisScript
     */
    public DefaultRedisScript<String> getGetIfNotOwnedbyOthersLuaSnippet() {
        return getIfNotOwnedByOthersLuaSnippet;
    }


    /**
     * Detailed constructor with default acquire timeout 10000 millis and lock
     * expiration of 60000 millis.
     *
     * @param lockKey lock key (ex. account:1, ...)
     */
    public RedisLockBaseOnRedisTemplate(String lockKey) {
        this(lockKey, DEFAULT_ACQUIRE_TIMEOUT_MILLIS, DEFAULT_EXPIRY_TIME_MILLIS, DEFAULT_OPERATE_TIME_LIMIT_MILLIS);
    }

    /**
     * Detailed constructor with default acquire timeout 10000 millis and lock
     * expiration of 60000 millis.
     *
     * @param lockKey          lock key (ex. account:1, ...)
     * @param operateTimeLimit the Millis time limit before committing a operate (default: 50 millis)
     */
    public RedisLockBaseOnRedisTemplate(String lockKey, int operateTimeLimit) {
        this(lockKey, DEFAULT_ACQUIRE_TIMEOUT_MILLIS, DEFAULT_EXPIRY_TIME_MILLIS, operateTimeLimit);
    }

    /**
     * Detailed constructor with default lock expiration of 60000 millis.
     *
     * @param lockKey              lock key (ex. account:1, ...)
     * @param acquireTimeoutMillis acquire timeout in milliseconds (default: 10000 millis)
     * @param operateTimeLimit     the Millis time limit before committing a operate (default: 50 millis)
     */
    public RedisLockBaseOnRedisTemplate(String lockKey, int acquireTimeoutMillis, int operateTimeLimit) {
        this(lockKey, acquireTimeoutMillis, DEFAULT_EXPIRY_TIME_MILLIS, operateTimeLimit);
    }

    /**
     * Detailed constructor.
     *
     * @param lockKey              lock key (ex. account:1, ...)
     * @param acquireTimeoutMillis acquire timeout in milliseconds (default: 10000 millis)
     * @param expiryTimeMillis     lock expiration in milliseconds (default: 60000 millis)
     * @param operateTimeLimit     the Millis time limit before committing a operate (default: 50 millis)
     */
    public RedisLockBaseOnRedisTemplate(String lockKey, int acquireTimeoutMillis, int expiryTimeMillis, int operateTimeLimit) {
        this(lockKey, acquireTimeoutMillis, expiryTimeMillis, DEFAULT_ACQUIRE_RESOLUTION_MILLIS, operateTimeLimit, UUID.randomUUID());
    }


    /**
     * Detailed constructor.
     *
     * @param lockKey                 lock key (ex. account:1, ...)
     * @param acquireTimeoutMillis    acquire timeout in milliseconds (default: 10000 millis)
     * @param expiryTimeMillis        lock expiration in milliseconds (default: 60000 millis)
     * @param acquireResolutionMillis acquire retry gap in milliseconds (default: 100 millis)
     * @param operateTimeLimit        the Millis time limit before committing a operate (default: 50 millis)
     */
    public RedisLockBaseOnRedisTemplate(String lockKey, int acquireTimeoutMillis, int expiryTimeMillis, int acquireResolutionMillis, int operateTimeLimit) {
        this(lockKey, acquireTimeoutMillis, expiryTimeMillis, acquireResolutionMillis, operateTimeLimit, UUID.randomUUID());
    }

    /**
     * Detailed constructor.
     *
     * @param lockKey                 lock key (ex. account:1, ...)
     * @param acquireTimeoutMillis    acquire timeout in milliseconds (default: 10000 millis)
     * @param expiryTimeMillis        lock expiration in milliseconds (default: 60000 millis)
     * @param acquireResolutionMillis acquire retry gap in milliseconds (default: 100 millis)
     * @param operateTimeLimit        the Millis time limit before committing a operate (default: 50 millis)
     * @param uuid                    unique identification of this lock
     */
    public RedisLockBaseOnRedisTemplate(String lockKey, int acquireTimeoutMillis, int expiryTimeMillis, int acquireResolutionMillis, int operateTimeLimit,
                                        UUID uuid) {
        if (expiryTimeMillis == 0) {
            throw new RuntimeException("Could not acquire redis distributed lock due to expiryTimeMillis is zero");
        }
        if (acquireTimeoutMillis == 0) {
            throw new RuntimeException("Could not acquire redis distributed lock due to acquireTimeoutMillis is zero");
        }
        if (operateTimeLimit == 0) {
            throw new RuntimeException("Could not acquire redis distributed lock due to operateTimeLimit is zero");
        }
        if (StringUtils.isBlank(lockKey)) {
            throw new RuntimeException("Could not acquire redis distributed lock due to empty lockKey");
        }
        if (uuid == null) {
            throw new RuntimeException("Could not acquire redis distributed lock due to empty uuid");
        }
        this.lockKey = lockKey;
        this.acquireTimeoutInMillis = acquireTimeoutMillis;
        this.lockExpiryInMillis = expiryTimeMillis + 1;
        this.acquireResolutionMillis = acquireResolutionMillis + 1;
        this.operateTimeLimit = operateTimeLimit;
        this.lockUUID = uuid;
        this.isLocked = false;
    }

    /**
     * @return lock uuid
     */
    public UUID getLockUUID() {
        return lockUUID;
    }

    /**
     * @return lock key
     */
    public String getLockKey() {
        return lockKey;
    }


    /**
     * Acquire lock.
     *
     * @return true if lock is acquired, false acquire timeout
     * @throws Exception in case of thread interruption
     */
    public synchronized boolean acquire(RedisTemplate<String, Object> redisTemplate) throws Exception {
//        LOGGER.info("Acquire Redis Distributed Lock....");
        if (isLocked()) {
            return renew(redisTemplate);
        }
        Stopwatch timer = Stopwatch.createStarted();
        int timeout = acquireTimeoutInMillis;
        String result = null;
        int count = 0;
        while (timeout >= 0) {
//            redis.set(lockKey, lockUUID.toString(), "NX", "PX", lockExpiryInMillis);
            DefaultRedisScript<String> getIfNotOwnedbyOthersLuaSnippet = getGetIfNotOwnedbyOthersLuaSnippet();
            result = redisTemplate.execute(getIfNotOwnedbyOthersLuaSnippet, redisTemplate.getStringSerializer(), new StringRedisSerializer(),
                    Collections.singletonList(lockKey), lockUUID.toString(), lockExpiryInMillis);
//            if ("OK".equals(result+ "")) {
            if ("OK".equals(result)) {
                this.isLocked = true;
                LOGGER.debug("Redis Distributed Lock Acquired Successfully After {} milliseconds with {} try count.lock:{}.",
                        timer.stop().elapsed(TimeUnit.MILLISECONDS), count, this);
                lockAcquiredTime = System.currentTimeMillis();
                return this.isLocked;
            }
            timeout -= acquireResolutionMillis;
            count++;
            Thread.sleep(acquireResolutionMillis);
        }
        LOGGER.error("Could Not Acquire Redis Distributed Lock After {} milliseconds,Due to:{}.Lock:{}.", timer.stop().elapsed(TimeUnit.MILLISECONDS),
                result, this);
        return false;
    }

    /**
     * Renew lock.
     *
     * @return {@code false} if lock is not currently owned <br>
     * false if the lock is currently owned by remote owner <br>
     * ,return {@code true} otherwise
     * @throws Exception in case of thread interruption
     */
    public synchronized boolean renew(RedisTemplate<String, Object> redisTemplate) throws Exception {
        if (!isLocked() || !isRemoteLocked(redisTemplate)) {
            LOGGER.info("Redis Distributed Lock Status is Invalid.");
            return false;
        }
        Stopwatch timer = Stopwatch.createStarted();
        int timeout = acquireTimeoutInMillis;
        String result = null;
        while (timeout >= 0) {
//            redis.set(lockKey, lockUUID.toString(), "XX", "PX", lockExpiryInMillis);
            result = redisTemplate.execute(getRenewIfOwnedluaSnippet(), Collections.singletonList(lockKey), lockUUID.toString(), lockExpiryInMillis);
            if ("OK".equals(result)) {
                LOGGER.debug("Redis Distributed Lock Renewed after ", timer.stop().elapsed(TimeUnit.MILLISECONDS));
                lockAcquiredTime = System.currentTimeMillis();
                return true;
            }
            timeout -= acquireResolutionMillis;
            Thread.sleep(acquireResolutionMillis);
        }
        LOGGER.error(" Could Not Renew Redis Distributed Lock After {} ms,Due to :{}.", timer.stop().elapsed(TimeUnit.MILLISECONDS), result);
        return false;
    }

    /**
     * expire a lock
     */
    public synchronized void expire() {
        this.isLocked = false;
    }

    /**
     * release a lock
     *
     * @throws Exception in case of thread interruption
     */
    public synchronized void release(RedisTemplate<String, Object> redisTemplate) throws Exception {
        if (isLocked()) {

            redisTemplate.execute(getDeleteIfOwnedLuaSnippet(), Collections.singletonList(lockKey), lockUUID.toString());
//            redis.execute(DELETE_IF_OWNED_LUA_SNIPPET, Collections.singletonList(lockKey), Collections.singletonList(lockUUID.toString()));
//            if (redis instanceof Jedis) {
//                ((Jedis) redis).eval(DELETE_IF_OWNED_LUA_SNIPPET, Collections.singletonList(lockKey), Collections.singletonList(lockUUID.toString()));
//            } else if (redis instanceof JedisCluster) {
//                ((JedisCluster) redis).eval(DELETE_IF_OWNED_LUA_SNIPPET, Collections.singletonList(lockKey), Collections.singletonList(lockUUID.toString()));
//            }

            LOGGER.debug("Lock Released: {} .", lockKey);
            this.isLocked = false;
        }
    }

    /**
     * check lock status,whether own it or not,base on local lock status(System Time)
     *
     * @return {@code false} if lock is not currently owned ,return {@code true} otherwise
     */
    public synchronized boolean isLocked() {
        if (this.isLocked) {
            long passtime = System.currentTimeMillis() - lockAcquiredTime;
            int excutableLimite = lockExpiryInMillis - operateTimeLimit;
            if (passtime > excutableLimite) {
                return false;
            }
        }
        return this.isLocked;
    }

    /**
     * remotely check lock status.
     *
     * @return {@code false} if lock is not currently owned <br>
     * false if the lock is currently owned by remote owner <br>
     * ,return {@code true} otherwise
     */
    public synchronized boolean isRemoteLocked(RedisTemplate<String, Object> redisTemplate) {
        long passtime = System.currentTimeMillis() - lockAcquiredTime;
        int excutableLimite = lockExpiryInMillis - operateTimeLimit;
        if (passtime > excutableLimite) {
            return false;
        }
        if (this.isLocked()) {
            return false;
        }
        Object result = redisTemplate.opsForValue().get(lockKey);
        if (result != null) {
            return StringUtils.isNotBlank(result + "") && !"nil".equals(result + "");
        } else {
            return false;
        }
    }


    @Override
    public String toString() {
        return "RedisLockBaseOnRedisTemplate{" +
                "lockKey='" + lockKey + '\'' +
                ", lockExpiryInMillis=" + lockExpiryInMillis +
                ", acquireTimeoutInMillis=" + acquireTimeoutInMillis +
                ", acquireResolutionMillis=" + acquireResolutionMillis +
                ", lockUUID=" + lockUUID +
                ", isLocked=" + isLocked +
                ", lockAcquiredTime=" + lockAcquiredTime +
                ", operateTimeLimit=" + operateTimeLimit +
                '}';
    }
}
