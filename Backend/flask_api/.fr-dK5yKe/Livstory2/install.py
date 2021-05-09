import os

download_nltk = "echo downloading nltk stuff"
punkt = "python3 -m nltk.downloader punkt"
stopwords = "python3 -m nltk.downloader stopwords"
spacey = "python3 -m spacy download en_core_web_sm"

list_commands = [download_nltk, punkt, stopwords, spacey]
for command in list_commands:
    os.system(command)