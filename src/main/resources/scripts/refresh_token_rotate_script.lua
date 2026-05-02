local key = KEYS[1]
local expectedDigest = ARGV[1]
local newDigest = ARGV[2]
local ttlSeconds = ARGV[3]

local currentDigest = redis.call('GET', key)

if currentDigest == expectedDigest then
    redis.call('SET', key, newDigest, 'EX', ttlSeconds)
    return 1
end

return 0
