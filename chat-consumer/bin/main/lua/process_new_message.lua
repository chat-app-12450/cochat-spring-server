-- KEYS
-- [1] participantsKey (S): 채팅방 참여자 목록
-- [2] connectedUsersKey (S): 현재 접속 중인 유저들
-- [3] unreadCountHashKey (H): 메시지별 unread count 저장소
-- [4] lastReadKeyPattern (String pattern): ex. "chat:last_read:{userId}:{roomId}"
-- [5] messageZSetKey (Z): 채팅 메시지 ZSet (score = receivedAt, value = messageId)

-- ARGV
-- [1] roomId
-- [2] messageId
-- [3] senderId

local participants = redis.call('SMEMBERS', KEYS[1])
local connectedUsersKey = KEYS[2]
local unreadCountHashKey = KEYS[3]
local lastReadKeyPattern = KEYS[4]
local messageZSetKey = KEYS[5]

local roomId = ARGV[1]
local messageId = ARGV[2]
local senderId = ARGV[3]

local unreadCount = 0
local readUsers = {}

for _, participantId in ipairs(participants) do
    local isConnected = redis.call('SISMEMBER', connectedUsersKey, participantId)
    local isSender = (participantId == senderId)

    local lastReadMessageKey = string.gsub(lastReadKeyPattern, "{userId}", participantId)
    lastReadMessageKey = string.gsub(lastReadMessageKey, "{roomId}", roomId)

    local lastReadRaw = redis.call('GET', lastReadMessageKey)
    local lastReadId = tonumber(lastReadRaw)
    if lastReadId == nil then lastReadId = -1 end

    if isConnected == 1 or isSender then
        if lastReadId < tonumber(messageId) then
            redis.call('SET', lastReadMessageKey, messageId)
            table.insert(readUsers, participantId)

            -- ✅ ZRANK를 활용해 정확한 범위로 unread 메시지 정리
            local startRank = redis.call('ZRANK', messageZSetKey, tostring(lastReadId))
            local endRank = redis.call('ZRANK', messageZSetKey, tostring(messageId))

            if startRank and endRank and endRank - startRank > 1 then
                local midMessages = redis.call('ZRANGE', messageZSetKey, startRank + 1, endRank - 1)
                for _, mid in ipairs(midMessages or {}) do
                    redis.call('HINCRBY', unreadCountHashKey, mid, -1)
                end
            end
        end
    else
        unreadCount = unreadCount + 1
    end
end

redis.call('HSET', unreadCountHashKey, messageId, unreadCount)
return { unreadCount, readUsers }
