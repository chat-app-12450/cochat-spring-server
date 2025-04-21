-- KEYS[1] = lastReadMessageKey
-- KEYS[2] = messageZSetKey
-- KEYS[3] = unreadCountHashKey

local lastReadMessageKey = KEYS[1]
local messageZSetKey = KEYS[2]
local unreadCountHashKey = KEYS[3]

redis.log(redis.LOG_WARNING, "==start==")

-- 최신 메시지 ID 가져오기
local latestMessageId = redis.call('ZRANGE', messageZSetKey, -1, -1)[1]

redis.log(redis.LOG_WARNING, "🔍 messageZSetKey: " .. messageZSetKey)
redis.log(redis.LOG_WARNING, "🔍 latestMessageId: " .. tostring(latestMessageId))
if not latestMessageId then
    return { -1, -1 }
end

local currentLastReadRaw = redis.call('GET', lastReadMessageKey)
local currentLastReadId = tonumber(currentLastReadRaw) or -1
local newLastReadId = tonumber(latestMessageId) or -1

redis.log(redis.LOG_WARNING, "🔍 currentLastReadRaw: " .. tostring(currentLastReadRaw))

-- 순위 계산
local startRank = -1
if currentLastReadId ~= -1 then
    startRank = redis.call('ZRANK', messageZSetKey, tostring(currentLastReadId))
end
local endRank = redis.call('ZRANK', messageZSetKey, tostring(newLastReadId))


redis.log(redis.LOG_WARNING, "☘️ startRank: " .. tostring(startRank))
redis.log(redis.LOG_WARNING, "☘️ endRank: " .. tostring(endRank))

if startRank and endRank and endRank > startRank then
    local midMessages = redis.call('ZRANGE', messageZSetKey, startRank + 1, endRank)
    for _, mid in ipairs(midMessages or {}) do
        redis.log(redis.LOG_WARNING, "🔍 mid: " .. tostring(mid))
        redis.call('HINCRBY', unreadCountHashKey, mid, -1)
    end
else
    redis.log(redis.LOG_WARNING, "❌ endRank <= startRank or startRank == -1 or endRank == -1")
    redis.log(redis.LOG_WARNING, "🔍 startRank: " .. tostring(startRank))
    redis.log(redis.LOG_WARNING, "🔍 endRank: " .. tostring(endRank))
end

redis.call('SET', lastReadMessageKey, tostring(newLastReadId))
return { currentLastReadId, newLastReadId }
