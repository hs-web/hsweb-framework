--
-- Created by IntelliJ IDEA.
-- User: aaa
-- Date: 2016/9/19
-- Time: 17:31
-- To change this template use File | Settings | File Templates.
--

local size = redis.call('SCARD',KEYS[1])
if(size == 0)
then
    local flag = redis.call('SETNX',KEYS[2],ARGV[1])
    if(flag)
    then
        return true
    else
        return false
    end
else
    return false
end
