from opensearchpy import OpenSearch, RequestsHttpConnection
import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../')))
from create_models import create_embedding_model
from create_chat_index import create_ingest_pipeline, create_chat_index

# Initialize the OpenSearch client
client = OpenSearch(
    hosts=[{'host': 'localhost', 'port': 9200}],
    http_auth=('admin', 'Developer@123'),  # docker-compose에 설정된 비밀번호
    use_ssl=True,
    verify_certs=False,
    ssl_show_warn=False,
    connection_class=RequestsHttpConnection
)

MODEL_NAME = "huggingface/sentence-transformers/all-MiniLM-L6-v2"
MODEL_VERSION = "1.0.1"
MODEL_DIM = 384

# === 모델 생성 ===
embedding_model_body = {
        "name": MODEL_NAME,
        "version": MODEL_VERSION,
        "model_format": "TORCH_SCRIPT"
    }

# embedding_model_id = "Y66FQ5cBnHkIP11G6R2Z"
embedding_model_id = None
print("embedding_model_id", embedding_model_id)
if embedding_model_id == None:
    print("Creating embedding model")
    embedding_model_id = create_embedding_model(os_client=client,model_body=embedding_model_body)
    print(f"Model ID: {embedding_model_id}")


# # === 파이프라인 생성 ===
# create_ingest_pipeline(client, "chat-ingest-pipeline", embedding_model_id)

# # === 인덱스 생성 (중복 체크) ===
# create_chat_index(client, "chat-index", "chat-ingest-pipeline", MODEL_DIM)

