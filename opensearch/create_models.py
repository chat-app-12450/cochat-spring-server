from opensearchpy import OpenSearch, RequestsHttpConnection
import time
    
def create_model_group(os_client, model_group_name):
    model_group_response = os_client.transport.perform_request(
        method='POST',
        url='/_plugins/_ml/model_groups/_register',
        body={"name": model_group_name, "description": "A model group for local models"}
    )
    return model_group_response['model_group_id']

def create_embedding_model(os_client, model_body):

    # 클러스터 세팅 업데이트 (모델 등록 관련 권한 등)
    os_client.cluster.put_settings(body={
        "persistent": {
            "plugins": {
                "ml_commons": {
                    "allow_registering_model_via_url": "true",
                    "only_run_on_ml_node": "false",
                    "model_access_control_enabled": "true",
                    "native_memory_threshold": "99"
                }
            }
        }
    })

    # 모델 등록 (모델 그룹 없이)
    register_response = os_client.transport.perform_request(
        method='POST',
        url='/_plugins/_ml/models/_register',
        body=model_body
    )

    # task_id 추출
    register_task_id = register_response['task_id']

    # task 상태 확인 (완료될 때까지 대기)
    while True:
        task_status = os_client.transport.perform_request(
            method='GET',
            url=f'/_plugins/_ml/tasks/{register_task_id}'
        )
        print(task_status)
        if task_status['state'] == 'COMPLETED':
            model_id = task_status['model_id']
            break
        time.sleep(10)  # 10초 대기

    # 모델 배포
    deploy_response = os_client.transport.perform_request(
        method='POST',
        url=f'/_plugins/_ml/models/{model_id}/_deploy'
    )
    print(deploy_response)
    deploy_task_id = deploy_response['task_id']

    # 배포 상태 확인 (완료될 때까지 대기)
    while True:
        deployment_status = os_client.transport.perform_request(
            method='GET',
            url=f'/_plugins/_ml/tasks/{deploy_task_id}'
        )
        print(deployment_status)
        if deployment_status['state'] == 'COMPLETED':
            break
        time.sleep(10)  # 10초 대기

    # 예측 테스트 (모델이 잘 동작하는지 확인)
    prediction = os_client.transport.perform_request(
        method='POST',
        url=f'/_plugins/_ml/_predict/text_embedding/{model_id}',
        body={
            "text_docs": ["today is sunny"],
            "return_number": True,
            "target_response": ["sentence_embedding"]
        }
    )
    print(prediction)
    print(f"Model ID: {model_id}")
    return model_id