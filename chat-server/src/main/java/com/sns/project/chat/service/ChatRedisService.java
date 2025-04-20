package com.sns.project.chat.service;

import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.sns.project.domain.chat.ChatRoom;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatRedisService {

    private final RedisTemplate<String, String> chatRedisTemplate;

    public ChatRedisService(@Qualifier("chatRedisTemplate") RedisTemplate<String, String> chatRedisTemplate) {
        this.chatRedisTemplate = chatRedisTemplate;
    }


    // ===== Set Operations =====
    public void addToSet(String key, String value) {
        chatRedisTemplate.opsForSet().add(key, value);
    }

    public void removeFromSet(String key, String value) {
        chatRedisTemplate.opsForSet().remove(key, value);
    }

    public Set<String> getSetMembers(String key) {
        return chatRedisTemplate.opsForSet().members(key);
    }

    public boolean isSetMember(String key, String value) {
        return Boolean.TRUE.equals(chatRedisTemplate.opsForSet().isMember(key, value));
    }

    // ===== Hash Operations =====
    public void setHashValue(String key, String hashKey, String value) {
        chatRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Optional<String> getHashValue(String key, String hashKey) {
        return Optional.ofNullable((String) chatRedisTemplate.opsForHash().get(key, hashKey));
    }

    // ===== Sorted Set Operations =====
    public void addToZSet(String key, String value, double score) {
        chatRedisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<String> getZSetRange(String key, double min, double max) {
        return chatRedisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public Optional<String> getHighestScoreFromZSet(String key) {
        Set<String> values = chatRedisTemplate.opsForZSet().range(key, -1, -1);
        return Objects.requireNonNull(values).isEmpty() ? Optional.empty() : Optional.of(values.iterator().next());
    }

    public ZSetOperations.TypedTuple<String> getHighestScoreWithScoreFromZSet(String key) {
        Set<ZSetOperations.TypedTuple<String>> values = chatRedisTemplate.opsForZSet().rangeWithScores(key, -1, -1);
        return Objects.requireNonNull(values).isEmpty() ? null : values.iterator().next();
    }

    // ===== Basic Operations =====
    public void setValue(String key, String value) {
        chatRedisTemplate.opsForValue().set(key, value);
    }

    public Optional<String> getValue(String key) {
        return Optional.ofNullable(chatRedisTemplate.opsForValue().get(key));
    }

    public Boolean setIfAbsent(String key, String value, Duration timeout) {
        return chatRedisTemplate.opsForValue().setIfAbsent(key, value, timeout);
    }

    // ===== Utility Methods =====
    public void delete(String key) {
        chatRedisTemplate.delete(key);
    }

    public Boolean expire(String key, Duration timeout) {
        return chatRedisTemplate.expire(key, timeout);
    }

    public void setValueWithExpirationInSet(String key, String value, int seconds) {
        chatRedisTemplate.opsForSet().add(key, value);
        chatRedisTemplate.expire(key, Duration.ofSeconds(seconds));
    }

    public List<String> popMultipleFromSet(String chatReadQueueKey, int i) {
        return chatRedisTemplate.opsForSet().pop(chatReadQueueKey, i);
    }


  public boolean exists(String key) {
    return chatRedisTemplate.hasKey(key);
  }

    public void deletePattern(String pattern) {
        Set<String> keys = chatRedisTemplate.keys(pattern);
        chatRedisTemplate.delete(keys);
    }


    public boolean hasKey(String key) {
        return chatRedisTemplate.hasKey(key);
    }


    public Long getZSetSize(String key) {
        return chatRedisTemplate.opsForZSet().size(key);
    }


    public Long getSetSize(String key) {
        return chatRedisTemplate.opsForSet().size(key);
    }


    public List<String> getMidRanksFromZSet(String key,
    Long startRank, Long endRank) {

        log.info("START RANK : {}, END RANK : {}", startRank, endRank);

        return chatRedisTemplate.opsForZSet()
            .range(key, startRank, endRank).stream().toList();
    }


    public Optional<Long> getRank(String messageZSetKey, String messageId) {
        return Optional.ofNullable(chatRedisTemplate.opsForZSet().rank(messageZSetKey, messageId));
    }


    public void incrementHash(String unreadCountKey, String messageId, int i) {
        chatRedisTemplate.opsForHash().increment(unreadCountKey, messageId, i);
    }


} 