''' generatng results with moondream on the given test dataset from yamaha '''
from transformers import AutoModelForCausalLM, AutoTokenizer
from PIL import Image
import os
import pandas as pd


model_path='weights for model of moondream'
tokenizer_path='weights for tokenizer of moondream'
revision = "2024-04-02"

model = AutoModelForCausalLM.from_pretrained(
    model_path, trust_remote_code=True, revision=revision
)

device='cuda' # define device for compuitation
model.to(device)

tokenizer = AutoTokenizer.from_pretrained(tokenizer_path, revision=revision)

df=pd.DataFrame()
dir_path='path to directory having all the images for generating results'

dir=os.listdir(dir_path)

img_ls=[]
cap_ls=[]
for i in dir:
    image = Image.open(f'{dir_path}/{i}') # opening image
    enc_image = model.encode_image(image) # encoding image for genrating response
    img_ls.append(i)
    cap_ls.append(model.answer_question(enc_image, "Generate caption.", tokenizer))

df['image']=img_ls
df['captions']=cap_ls
out_path='path for saving results'
# saving results of generated response in csv format
df.to_csv('out_path')

''' code ends here :)  '''