package com.sns.project.chat.service.dto;

public class LastReadIdInfo {
    private Long prevLastReadId;
    private Long newLastReadId;
    
    // 기본 생성자
    public LastReadIdInfo() {
    }
    
    // 모든 필드를 포함한 생성자
    public LastReadIdInfo(Long prevLastReadId, Long newLastReadId) {
        this.prevLastReadId = prevLastReadId;
        this.newLastReadId = newLastReadId;
    }
    
    // Getter와 Setter 메서드
    public Long getPrevLastReadId() {
        return prevLastReadId;
    }
    
    public void setPrevLastReadId(Long prevLastReadId) {
        this.prevLastReadId = prevLastReadId;
    }
    
    public Long getNewLastReadId() {
        return newLastReadId;
    }
    
    public void setNewLastReadId(Long newLastReadId) {
        this.newLastReadId = newLastReadId;
    }
    
    // Builder 패턴 구현
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long prevLastReadId;
        private Long newLastReadId;
        
        public Builder prevLastReadId(Long prevLastReadId) {
            this.prevLastReadId = prevLastReadId;
            return this;
        }
        
        public Builder newLastReadId(Long newLastReadId) {
            this.newLastReadId = newLastReadId;
            return this;
        }
        
        public LastReadIdInfo build() {
            return new LastReadIdInfo(prevLastReadId, newLastReadId);
        }
    }
    
    @Override
    public String toString() {
        return "LastReadIdInfo{" +
               "prevLastReadId=" + prevLastReadId +
               ", newLastReadId=" + newLastReadId +
               '}';
    }
} 
