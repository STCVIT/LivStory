import spacy
import pytextrank
import networkx as nx
import math
import operator
import nltk
from nltk.tokenize import sent_tokenize, word_tokenize
from nltk.corpus import stopwords

# nltk.download()


# !python3 -m spacy download en_core_web_sm

nlp=spacy.load('en_core_web_sm')


"""# Text Rank Algorithm
"""
if __name__ == '__main__':
    text="""Lion and Lioness are roaring and sleeping"""

    punctuations="?:!.,;"

    # Tokenize everything

    sentence_words = nltk.word_tokenize(text)
    str2=''
    for word in sentence_words:
        if word not in punctuations and  word not in stopwords.words('english'):
            str2=str2+' '+word
    text=str2

    # Here we will add the text required on which NLP will take place
    # We add a pipeline at the start of every run
    nlp.add_pipe("textrank")

    #We reinitialise the model everytime
    doc=nlp(text)

#doc._.phrases

# Direct method of solving

# for phrase in doc._.phrases:
#     print(phrase.text,phrase.rank)
#     print(phrase.rank, phrase.count)
#     print(phrase.chunks)

# Alternate way of doing

# Here we will create the graph by making nodes as a tree

def increment_edge (graph, node0, node1):

    if graph.has_edge(node0, node1):
        # Here we form an in-memory graph just like a tree
        graph[node0][node1]["weight"] += 1.0

    else:
        # If there are no edges then this gets added
        graph.add_edge(node0, node1, weight=1.0)

POS_KEPT = ["ADJ", "NOUN", "PROPN", "VERB"]

# Here we construct the graph using the spacy tags
def link_sentence (doc, sent, lemma_graph, seen_lemma):
    visited_tokens = []
    visited_nodes = []

    for i in range(sent.start, sent.end):

        # Here we store each word as a token from the doc
        token = doc[i]

        if token.pos_ in POS_KEPT:
            # We then check the word and its properties
            key = (token.lemma_, token.pos_)

            if key not in seen_lemma:
                seen_lemma[key] = set([token.i])
            else:
                seen_lemma[key].add(token.i)

            node_id = list(seen_lemma.keys()).index(key)

            if not node_id in lemma_graph:
                lemma_graph.add_node(node_id)

            for prev_token in range(len(visited_tokens) - 1, -1, -1):

                if (token.i - visited_tokens[prev_token]) <= 3:
                    increment_edge(lemma_graph, node_id, visited_nodes[prev_token])
                else:
                    break

            visited_tokens.append(token.i)
            visited_nodes.append(node_id)


# This function will collect all the words and give its rank
def collect_phrases (chunk, phrases, counts):
    chunk_len = chunk.end - chunk.start
    sq_sum_rank = 0.0
    non_lemma = 0
    compound_key = set([])

    for i in range(chunk.start, chunk.end):
        token = doc[i]
        key = (token.lemma_, token.pos_)

        if key in seen_lemma:
            node_id = list(seen_lemma.keys()).index(key)
            rank = ranks[node_id]
            sq_sum_rank += rank
            # depending on its frequency and importance its given a rank
            compound_key.add(key)

        else:
            non_lemma += 1

    # although the noun chunking is greedy, we discount the ranks using a
    # point estimate based on the number of non-lemma tokens within a phrase
    non_lemma_discount = chunk_len / (chunk_len + (2.0 * non_lemma) + 1.0)

    # use root mean square (RMS) to normalize the contributions of all the tokens
    phrase_rank = math.sqrt(sq_sum_rank / (chunk_len + non_lemma))
    phrase_rank *= non_lemma_discount

    # remove spurious punctuation
    phrase = chunk.text.lower().replace("'", "")

    # create a unique key for the the phrase based on its lemma components
    compound_key = tuple(sorted(list(compound_key)))

    if not compound_key in phrases:
        phrases[compound_key] = set([ (phrase, phrase_rank) ])
        counts[compound_key] = 1

    else:
        phrases[compound_key].add( (phrase, phrase_rank) )
        counts[compound_key] += 1

def get_keywords(text):
    sentence_words = nltk.word_tokenize(text)
    str2=''
    for word in sentence_words:
        if word not in punctuations and  word not in stopwords.words('english'):
            str2=str2+' '+word
    text=str2

    # Here we will add the text required on which NLP will take place
    # We add a pipeline at the start of every run
    nlp.add_pipe("textrank")

    #We reinitialise the model everytime
    doc=nlp(text)

    lemma_graph = nx.Graph()
    seen_lemma = {}

    for sent in doc.sents:
        link_sentence(doc, sent, lemma_graph, seen_lemma)
        # break
        # only test one sentence


    labels = {}
    keys = list(seen_lemma.keys())

    # Here we iterate through each sentence to construct their graph
    for i in range(len(seen_lemma)):
        labels[i] = keys[i][0].lower()

    # This variable will store the rank of each node of the graph
    ranks = nx.pagerank(lemma_graph)

    phrases = {}
    counts = {}

    for chunk in doc.noun_chunks:

        # here we collect all the phrases along with their frequency
        collect_phrases(chunk, phrases, counts)

    for ent in doc.ents:
        collect_phrases(ent, phrases, counts)

    min_phrases = {}

    for compound_key, rank_tuples in phrases.items():
        l = list(rank_tuples)
        l.sort(key=operator.itemgetter(1), reverse=True)
        phrase, rank = l[0]
        count = counts[compound_key]
        min_phrases[phrase] = (rank, count)

    list1=[]
    for node_id, rank in sorted(ranks.items(), key=lambda x: x[1], reverse=True):
        list1.append(labels[node_id])
    #    print(labels[node_id], rank)

    """ # Returns the list of the ranked words """

    return list1

""" 
TODO:

1)Get the paragraph from the app.
2)Return the paragraph to the app

"""
