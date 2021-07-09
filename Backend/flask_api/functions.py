import firebase_admin
from firebase_admin import credentials, firestore
# from firebase_admin import firestore

cred = credentials.Certificate('./creds/google-services.json')
firebase_admin.initialize_app(cred)

print("initializing storage access")
db = firestore.client()


def get_audio(keywords: list):
    sounds = []
    for word in keywords:
        flag = 0
        print(f"searching for sounds related to {word}")
        audio_ref = db.collection(u'sounds')
        docs = audio_ref.where(u'keywords', u'array_contains', word).limit(1).stream()
        for doc in docs:
            if doc.exists:
                flag = 1
                sounds.append(doc.to_dict()['media'])
                break
        if flag == 0:
            sounds.append('')
            # Fetched from model but not present in database
            ## Add to database 
            db.collection(u'report').document(u'model').update(
                {word: firestore.Increment(1)})

    return sounds


def report(word):
    print(f'Adding {word} to firestore')
    try:
        db.collection(u'report').document(u'user').update(
            {word: firestore.Increment(1)})
        return "success"
    except:
        return "error"


if __name__ == "__main__":
    print("finding docs")
    print(get_audio(['lion','wolf','tiger']))