local ids = redis.call('KEYS', 'http.session.user:*');
local ids_ = {};
for i, v in ipairs(ids) do

    local userId = v:sub(19,-1);
    local sessoinId = redis.call('GET', v);
    local sessionExists = redis.call('EXISTS', 'spring:session:sessions:' .. sessoinId);
    if (sessionExists== 1)
    then
        table.insert(ids_, userId);
    else
        return sessoinId;
--        redis.call("DEL", 'http.session.user:' .. userId);
    end
end
return ids_;