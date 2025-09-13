from opensearchpy import OpenSearch, RequestsHttpConnection
from opensearchpy.helpers import bulk

embedding_model_id = "Y66FQ5cBnHkIP11G6R2Z"

client = OpenSearch(
    hosts=[{'host': 'localhost', 'port': 9200}],
    http_auth=('admin', 'Developer@123'),  # docker-compose에 설정된 비밀번호
    use_ssl=True,
    verify_certs=False,
    ssl_show_warn=False,
    connection_class=RequestsHttpConnection
)

chat_content_query = "다음주에 머할래"

documents = [
    {
        "chat_content": "안녕하세요",
        "timestamp": "2025-06-06"
    },
    {
        "chat_content": "다음주에 뭐할래",
        "timestamp": "2025-06-06"
    },
    {
        "chat_content": "다음주에 등산하로 가자",
        "timestamp": "2025-06-06"
    },
    {
        "chat_content": "등산",
        "timestamp": "2025-06-06"
    },
    {
        "chat_content": "등대로 가자",
        "timestamp": "2025-06-06"
    },
    {
        "chat_content": "싫어",
        "timestamp": "2025-06-06"
    }
    
]

actions = [
    {
        "_op_type": "index",
        "_index": "chat-index",
        "_id": i +1,
        "_source": doc
    }
    for i, doc in enumerate(documents)
]

response = bulk(client, actions)
print("💎 Documents indexed:", response)

# Search using text query
search_query = {
    "_source": {
        "excludes": ["chat_content_embedding"]
    },
    "query": {
        "match": {
            "chat_content": {
                "query": chat_content_query
            }
        }
    }
}
response = client.search(index="chat-index", body=search_query)
print("💎 Text query search results:")
for res in response['hits']['hits']:
    print(res['_source']['chat_content'])

# Semantic Search
semantic_search_query = {
    "_source": {
        "excludes": ["chat_content_embedding"]
    },
    "query": {
        "neural": {
            "chat_content_embedding": {
                "query_text": chat_content_query,
                "model_id": embedding_model_id,
                "k": 5
            }
        }
    }
}

response = client.search(index="chat-index", body=semantic_search_query)
print("💎 Semantic search results:")
for res in response['hits']['hits']:
    print(res['_source']['chat_content'])



# Hybrid Search - create normalizer pipeline
normalizer_pipeline_body = {
    "description": "Post processor for hybrid search",
    "phase_results_processors": [
        {
            "normalization-processor": {
                "normalization": {
                    "technique": "min_max"
                },
                "combination": {
                    "technique": "arithmetic_mean",
                    "parameters": {
                    # "weights": [0.0, 1.0]
                        "weights": [0.5, 0.5]
                    }
                }
            }
        }
    ]
}
client.transport.perform_request('PUT', '/_search/pipeline/nlp-search-pipeline', body=normalizer_pipeline_body)

# Hybrid search query
hybrid_search_query = {
  "_source": {
    "excludes": ["chat_content_embedding"]
  },
  "query": {
    "bool": {
      "must": [
        {
          "neural": {
            "chat_content_embedding": {
              "query_text": chat_content_query,
              "model_id": embedding_model_id,
              "k": 5
            }
          }
        },
        {
          "range": {
            "timestamp": {
              "gte": "2025-06-06T00:00:00",
              "lte": "2025-06-06T23:59:59"
            }
          }
        }
      ]
    }
  }
}


response = client.search(index="chat-index", body=hybrid_search_query, search_pipeline="nlp-search-pipeline")
print("💎 Hybrid search results:")
for res in response['hits']['hits']:
    print(res['_source']['chat_content'])
 