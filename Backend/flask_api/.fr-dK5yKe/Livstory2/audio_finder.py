import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

cred = credentials.Certificate('./creds/google-services.json')
firebase_admin.initialize_app(cred)

print("initializing storage access")
db = firestore.client()

def get_audio(keywords: list):
    sounds = []    
    for word in keywords:
        print(f"searching for sounds related to {word}")
        audio_ref = db.collection(u'sounds')
        audio_docs = audio_ref.stream()
        for audio_doc in audio_docs:
            audio_doc = audio_doc.to_dict()
            for reference_word in audio_doc['keywords']:
                if word == reference_word:
                    sounds.append(audio_doc['media'])
    
    return sounds


if __name__ == "__main__":
    for audio_doc in audio_docs:
        print("finding docs")
        print(f"{audio_doc.id} => {audio_doc.to_dict()}")
