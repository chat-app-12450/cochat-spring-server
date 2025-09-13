from dotenv import load_dotenv
import os
from opensearchpy import OpenSearch, RequestsHttpConnection

load_dotenv(dotenv_path=os.path.join(os.path.dirname(__file__), ".env"))


OPENSEARCH_HOST = os.getenv("OPENSEARCH_HOST", "localhost")
OPENSEARCH_PORT = int(os.getenv("OPENSEARCH_PORT", 9200))
OPENSEARCH_USER = os.getenv("OPENSEARCH_USER", "admin")
OPENSEARCH_PASSWORD = os.getenv("OPENSEARCH_PASSWORD", "Developer@123")


# INDEX_NAME = "chat-index"
# PIPELINE_ID = "chat-ingest-pipeline"

def create_ingest_pipeline(client, pipeline_id, embedding_model_id):
    ingest_pipeline_body = {
        "description": "chat data ingest pipeline",
        "processors": [
            {
                "text_embedding": {
                    "model_id": embedding_model_id,
                    "field_map": {
                        "chat_content": "chat_content_embedding"
                    },
                    "ignore_failure": True
                }
            }
        ]
    }
    client.ingest.put_pipeline(id=pipeline_id, body=ingest_pipeline_body)
    print(f"💙 Ingest pipeline '{pipeline_id}' created/updated.")


def create_chat_index(client, index_name, pipeline_id, embedding_dim):
    index_body = {
        "settings": {
            "index.knn": True,
            "default_pipeline": pipeline_id
        },
        "mappings": {
            "dynamic": False,
            "properties": {
                "id": {"type": "keyword"},
                "chat_content_embedding": {
                    "type": "knn_vector",
                    "dimension": embedding_dim,
                    "method": {
                        "engine": "lucene",
                        "space_type": "l2",
                        "name": "hnsw",
                        "parameters": {
                            "ef_construction": 128,
                            "m": 16
                        }
                    }
                },
                "chat_content": {"type": "text"},
                "timestamp": {"type": "date"},
                "user": {"type": "keyword"}
            }
        }
    }
    if not client.indices.exists(index=index_name):
        response = client.indices.create(index=index_name, body=index_body)
        print(f"💙 Index '{index_name}' created:", response)
    else:
        print(f"💡 Index '{index_name}' already exists.")

