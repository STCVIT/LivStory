from flask import Flask, request, jsonify
import keyword_extractor
import audio_finder
import model

app = Flask(__name__)
POST_PATH = '/text'

@app.route(POST_PATH, methods=["POST"])
def api_call():
    data = request.json
    paragraph = data['text']
    response = dict()
#    keywords = keyword_extractor.extract_keywords(paragraph)
    keywords = model.get_keywords(paragraph)
    print(keywords)

    audio_strings = audio_finder.get_audio(keywords)
    response["sounds"] = audio_strings
    return jsonify(response)

if __name__ == "__main__":
    app.run(host='localhost', port=3000)