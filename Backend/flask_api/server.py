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
    response["sounds"] = audio_strings
    return jsonify(response)

if __name__ == "__main__":
    app.run(debug=True)
