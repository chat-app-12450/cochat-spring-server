#!/usr/bin/env python3
from kafka import KafkaProducer, KafkaConsumer
import json
import time

# 메시지 형식화 함수
def serialize_message(message):
    return json.dumps(message).encode('utf-8')

# 프로듀서 생성 테스트
def test_producer():
    try:
        producer = KafkaProducer(
            bootstrap_servers=['localhost:29092'],
            value_serializer=serialize_message
        )
        print("프로듀서 연결 성공!")

        # 메시지 보내기
        for i in range(3):
            message = {'message': f'외부에서 보내는 테스트 메시지 {i+1}'}
            producer.send('chat-enter', message)
            print(f"메시지 전송: {message}")
            time.sleep(1)
        
        producer.flush()
        producer.close()
        print("프로듀서 테스트 완료!")
        return True
    except Exception as e:
        print(f"프로듀서 연결 실패: {e}")
        return False

# 컨슈머 생성 테스트
def test_consumer():
    try:
        consumer = KafkaConsumer(
            'chat-enter',
            bootstrap_servers=['localhost:29092'],
            auto_offset_reset='latest',
            enable_auto_commit=True,
            group_id='test-external-group',
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )
        print("컨슈머 연결 성공! 5초 동안 메시지를 기다립니다...")

        # 5초 동안 메시지 수신 시도
        start_time = time.time()
        message_received = False
        
        while time.time() - start_time < 5:
            msgs = consumer.poll(timeout_ms=1000)
            if msgs:
                for tp, messages in msgs.items():
                    for message in messages:
                        print(f"메시지 수신: {message.value}")
                        message_received = True
        
        consumer.close()
        
        if not message_received:
            print("5초 동안 메시지를 받지 못했습니다.")
        
        print("컨슈머 테스트 완료!")
        return True
    except Exception as e:
        print(f"컨슈머 연결 실패: {e}")
        return False

if __name__ == "__main__":
    print("=== 외부에서 Kafka 접속 테스트 시작 ===")
    print("\n1. 프로듀서 테스트 (메시지 보내기)")
    producer_success = test_producer()
    
    print("\n2. 컨슈머 테스트 (메시지 받기)")
    if producer_success:
        test_consumer()
    
    print("\n=== 테스트 완료 ===") 