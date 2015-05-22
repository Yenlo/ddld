import sys
import json

from pyelasticsearch import ElasticSearch


def get_docs(fname):
    with open(fname) as f:
        for line in f:
            yield json.loads(line)


if __name__ == '__main__':
    fname = sys.argv[1]
    es = ElasticSearch('http://localhost:8004/')

    index = 'documents'
    doc_type = 'publication'
    mapping = {
        doc_type: {
            "properties": {
                "persistent_id": {
                    "type": "string",
                    "index": "not_analyzed",
                    },
                "authors": {
                    "type": "multi_field",
                    "fields": {
                        "authors": {
                            "type": "string",
                            "index": "analyzed"
                            },
                        "untouched": {
                            "type": "string",
                            "index": "not_analyzed"
                            }
                        }
                   },
                "topics": {
                    "type": "multi_field",
                    "fields": {
                        "topics": {
                            "type": "string",
                            "index": "analyzed"
                            },
                        "untouched": {
                            "type": "string",
                            "index": "not_analyzed"
                            }
                        }
                   }
                }
            }
        }

    es.create_index(index, {"mappings": mapping})
    # es.put_mapping(index, doc_type, mapping)

    es.bulk_index(index, doc_type, get_docs(fname), 'persistent_id')
