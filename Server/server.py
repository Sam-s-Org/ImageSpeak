#Loading the necessary Libraries
from flask import Flask,request,jsonify
import torch
from PIL import Image
from transformers import AutoModelForCausalLM,AutoTokenizer
from PIL import Image
import io
import base64
print(torch.cuda.is_available())


img=None

#Loading the Model as Global
revision = "2024-04-02"
model = AutoModelForCausalLM.from_pretrained('Model Path', trust_remote_code=True, revision=revision)
#Sending the model to GPU
device = "cuda" if torch.cuda.is_available() else "cpu"
model.to(device)
tokenizer = AutoTokenizer.from_pretrained('Tokenizer Path', revision=revision)



app=Flask(__name__)

#Routes


#Route for generating captions
@app.route('/caps',methods=['POST'])
def caps():
    # Saving the current image as global
    global img
    a=request.json
    #Converting the base64 string to image
    decoded_bytes = base64.b64decode(a)
    image = Image.open(io.BytesIO(decoded_bytes))
    image = image.rotate(-90, expand=True)
    # Finding the image encoding
    enc_image = model.encode_image(image)
    img=enc_image
    # Generating Captions
    ans=model.answer_question(enc_image, "Generate Captions", tokenizer)
    print(ans)
    response={'answer':ans}
    # Sending the response to app in json format
    return jsonify(response)


#Route for question answering
@app.route('/query',methods=['POST'])
def query():
    #Loading the encoding of current image
    global img
    #Loading the question from the request
    b=request.json
    #Answering the question
    ans=model.answer_question(img, b, tokenizer)
    print(ans)
    response={'answer':ans}
    # Sending the response to app in json format
    return jsonify(response)


# Initializing the server
if __name__=="__main__":
    app.run(port=5001,use_reloader=False)