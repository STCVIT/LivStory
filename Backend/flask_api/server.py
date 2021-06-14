from flask import Flask, request, jsonify
#import keyword_extractor
import audio_finder
import model
from flask_cors import CORS,cross_origin

app = Flask(__name__)
cors=CORS(app)
app.config['CORS_HEADERS']='Content-Type'
#POST_PATH = '/text'

@app.route('/',methods=["POST"])
@cross_origin()
def home():
    data = request.json
    paragraph = data['text']
    response = dict()
#   keywords = keyword_extractor.extract_keywords(paragraph)
    keywords = model.get_keywords(paragraph)
    print(keywords)

    audio_strings = audio_finder.get_audio(keywords)
    response["sound"] = dict() # audio_strings

    response["sound"][keywords[0]] = audio_strings[0]
    return jsonify(response)


@app.route('/text',methods=["POST"])
@cross_origin()
def text():
    data = request.json
    paragraph = data['text']
    response = dict()
#   keywords = keyword_extractor.extract_keywords(paragraph)
    keywords = model.get_keywords(paragraph)
    print(keywords)

    audio_strings = audio_finder.get_audio(keywords)
    response["sounds"] = dict() # audio_strings
    for i in range(len(keywords)):
        response["sounds"][keywords[i]] = audio_strings[i]
    return jsonify(response)

@app.route('/texts',methods=["POST"])
@cross_origin()
def texts():
    data = request.json
    paragraph = data['text']
    response = dict()
#   keywords = keyword_extractor.extract_keywords(paragraph)
    keywords = model.get_keywords(paragraph)
    print(keywords)

    audio_strings = audio_finder.get_audio(keywords)
    response["keywords"] = keywords # audio_strings
    return jsonify(response)



if __name__ == "__main__":
    app.run(debug=True)
