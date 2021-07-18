from os import error
from flask import Flask, request, jsonify
import functions
import model
from flask_cors import CORS, cross_origin

app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

@app.route('/', methods=["POST"])
@cross_origin()
def home():
    data = request.json
    paragraph = data['text']
    response = dict()
    keywords = model.get_keywords(paragraph)
    print(keywords)
    if len(keywords) == 0:
        return jsonify([])

    audio_strings = functions.get_audio(keywords)
    response["sound"] = dict()  
    response["sound"][keywords[0]] = audio_strings[0]
    return jsonify(response)


@app.route('/text', methods=["POST"])
@cross_origin()
def text():
    data = request.json
    paragraph = data['text']
    response = dict()
    keywords = model.get_keywords(paragraph)
    print(keywords)

    audio_strings = functions.get_audio(keywords)
    response["sounds"] = dict() 
    for i in range(len(keywords)):
        if audio_strings[i] != '':
            response["sounds"][keywords[i]] = audio_strings[i]
    return jsonify(response)



@app.route('/report', methods=["POST"])
@cross_origin()
def report():
    data = request.json
    word = data['text']
    return jsonify(functions.report(word))

if __name__ == "__main__":
    app.run(debug=True)
