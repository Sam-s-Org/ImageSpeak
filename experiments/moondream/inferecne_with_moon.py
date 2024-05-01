from transformers import AutoModelForCausalLM, AutoTokenizer
from PIL import Image

model_path='path to saved weights of model'
tokenizer_path='path to saved weights fro tokenizer'
revision = "2024-04-02"

# model 
model = AutoModelForCausalLM.from_pretrained(
    model_path, trust_remote_code=True, revision=revision
)

device ='cuda:1' # define device for computation
model.to(device) 
# tokenizer
tokenizer = AutoTokenizer.from_pretrained('moondream2_best/tokenizer', revision=revision)

image_path='add image path from which you want to generate captions'
image = Image.open(image_path)
enc_image = model.encode_image(image) # encoding image completed

print(model.answer_question(enc_image, "Generate Captions", tokenizer))

''' code ends here :) :) :)  helped us generate best response''' 