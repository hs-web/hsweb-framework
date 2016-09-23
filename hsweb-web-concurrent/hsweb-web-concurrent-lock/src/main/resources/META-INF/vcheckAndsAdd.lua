--
-- Created by IntelliJ IDEA.
-- User: aaa
-- Date: 2016/9/19
-- Time: 17:31
-- To change this template use File | Settings | File Templates.
--

local v = redis.call('GET',KEYS[1])
if(v)
then
    return false
else
    redis.call('SADD',KEYS[2],ARGV[1])
    return true
end
