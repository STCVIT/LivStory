import firebase_admin
from firebase_admin import credentials,firestore
# from firebase_admin import firestore

cred = credentials.Certificate('./creds/google-services.json')
firebase_admin.initialize_app(cred)

print("initializing storage access")
db = firestore.client()

def get_audio(keywords: list):
    sounds = []    
    for word in keywords:
        flag=0
        print(f"searching for sounds related to {word}")
        audio_ref = db.collection(u'sounds')
        docs=audio_ref.where(u'keywords',u'array_contains',word).stream()
        for doc in docs:
            flag=1
            sounds.append(doc.to_dict()['media'])
        if flag==0:
            sounds.append('')
    
    return sounds


if __name__ == "__main__":
    print("finding docs")
