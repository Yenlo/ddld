import sys
import json
from collections import defaultdict

from joblib import Parallel, delayed
from topia.termextract import extract


docs = ["""Research

While much of the research that I did in the past revolved around
formal aspects of knowledge representation formalisms, since the start
of the 21st century my research has been focused on algorithms and
models for intelligent information access.  Life without search
engines has become unthinkable. My work in information retrieval is
characterized by three broad strands of research: analysis (of
collections, documents, queries, and behavior), synthesis of the
outcomes of various analyses into a high-quality ranking of search
results, and evaluation methodology. In my work I combine theoretical
contributions to information retrieval, in terms of new analysis and
synthesis algorithms with large-scale evaluations and valorisation
activities in collaboration with external partners, both academic,
industrial and governmental.

In recent years, my most important contributions in terms of
algorithmic analysis of information have to do with effective search
methods for social media, recognizing semantic structure in noisy text
sources, click models for explaining user behavior, and robust methods
for recognizing subjective aspects of information (such as sentiment,
personality, reputation). Part of this work is being carried in
collaboration with colleagues the humanities and social sciences.  My
work on synthesizing the outcomes of these types of analysis has
revolved around fusion methods and aggregated search over large
numbers of sources, semantic search (where heterogeneous information
related to meaningful units such as people, events or cultural
artefacts are gathered and made searchable), and algorithms for
self-learning search engines that automatically improve the quality of
their combined results through interactions with users.  As to
evaluation, I have recently worked on methods for automatically
generating training material for learning to rank, on using graphical
models that capture user behavior to create model-based metrics, and
on the foundations of effective online models for determining the
quality of ranking algorithms from implicit signals.  Please consult
the web site of my research group for details on research projects
that I am currently involved with.""",

        """My current work focuses on applying information retrieval models,
natural language processing, machine learning, and semantic approaches
to offer intelligent access to information in social media. Recently
my work is moving towards life mining, in which we try to identify
stages in life from social media, transaction logs, and other sources
to improve information suggestions to users.
"""]


def get_docs(fname):
    with open(fname) as f:
        for line in f:
            yield json.loads(line)


def custom_filter(phrase, occurrence, length):
    return length > 1


def process(doc):
    res = []

    for field in ['title', 'abstract']:
        text = doc[field]
        extractor = extract.TermExtractor()
        extractor.filter = custom_filter

        res.extend(extractor(text))

    return res
    
    
if __name__ == '__main__':
    fname = sys.argv[1]

    keyphrase_lists = Parallel(n_jobs=-1)(delayed(process)(doc) for doc in get_docs(fname))

    keywords = defaultdict(int)
    for lst in keyphrase_lists:
        for keyword, count, length in lst:
            keywords[keyword] += count

    for k in sorted(keywords, key=lambda e:keywords[e], reverse=True):
        out = "%s\t%d" % (k, keywords[k])
        print out.encode("utf-8")
