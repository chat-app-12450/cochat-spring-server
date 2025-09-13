from opensearchpy import OpenSearch, RequestsHttpConnection

def get_opensearch_client(host, port, username, password):
    client = OpenSearch(
        hosts=[{'host': host, 'port': port}],
        http_auth=(username, password),
        use_ssl=True,
        verify_certs=False,
        ssl_show_warn=False,
        connection_class=RequestsHttpConnection
    )
    return client

def search_opensearch(query, embedding_model_id, client, top_k=3):
    search_body = {
        "_source": {"excludes": ["chat_content_embedding"]},
        "query": {
            "neural": {
                "chat_content_embedding": {
                    "query_text": query,
                    "model_id": embedding_model_id,
                    "k": top_k
                }
            }
        }
    }
    response = client.search(index="chat-index", body=search_body)
    docs = [hit['_source']['chat_content'] for hit in response['hits']['hits']]
    return docs 