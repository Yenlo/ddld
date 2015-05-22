import sys
import json

from scipy import sparse

from sklearn.preprocessing import LabelEncoder
from sklearn.naive_bayes import MultinomialNB
from sklearn.feature_extraction import DictVectorizer
from sklearn.feature_extraction.text import CountVectorizer, TfidfVectorizer

fname = sys.argv[1]

raw_X = []
raw_Y = []

docs = []

# for doc in etree.parse(docs_fname).xpath('//DOC'):
#     doc_id = unicode(doc.xpath('string(DOCNO)'))
#     doc_text = unicode(doc.xpath('string(TEXT)'))
#     authors = sorted(doc_authors[doc_id])

#     # raw_X.append({'doc_id':doc_id, 'text':doc_text})
#     raw_X.append(doc_text)
#     raw_Y.append([author_mapping[author] for author in authors])

#     docs.append({'doc_id': doc_id,
#                  'authors': authors,
#                  'text': doc_text})

author_id = {}

print >> sys.stderr, "read data"
with open(fname) as f:
    for line in f:
        hsh = json.loads(line)
        doc_id = hsh['persistent_id']
        text = hsh['abstract']
        authors = hsh['authors']

        raw_X.append(text)
        raw_Y.append(authors)

        docs.append({'doc_id': doc_id,
                     'authors': authors,
                     'text': text})

print >> sys.stderr, "init objects"
cl = MultinomialNB(alpha=1e-7)
tfidf = TfidfVectorizer(ngram_range=(1, 1), stop_words='english')

print >> sys.stderr, "convert labels"
for lst in raw_Y:
    for a in lst:
        if not a in author_id:
            author_id[a] = len(author_id)

print >> sys.stderr, "processing text"

X = tfidf.fit_transform(raw_X)
# Y = countvec.fit_transform(raw_Y)
print >> sys.stderr, "processing labels"
lol = [[author_id[e] for e in lst] for lst in raw_Y]
print >> sys.stderr, "creating label matrix"
Y = lol

print >> sys.stderr, "training classifier"

# print X.shape
# print len(Y)
cl.fit(X, Y)

print >> sys.stderr, "performing query"
raw_test = ["information retrieval"]
test = tfidf.transform(raw_test)

# inv_author_mapping = { n:author for author, n in author_mapping.items()}

res = cl.predict_proba(test)
res = res[0]

inv_author = {}
for author, i in author_id.items():
    inv_author[i] = author

def report(labels, res, n=25):
    res = enumerate(res)
    score_expert = [(score, inv_author[i]) for i, score in res]
    score_expert = sorted(score_expert, reverse=True)

    readable = ["%.4f %s" % e for e in score_expert]

    return "\n".join(readable[:n])

print 'query: "%s"' % raw_test[0]
print report([], res)

# res = sorted(enumerate(res), key=lambda t:t[1], reverse=True)

# print "query: '%s'" % raw_test[0]
# for i, score in res[:20]:
#     print "%.4f %s" % (score, inv_author_mapping[i])
