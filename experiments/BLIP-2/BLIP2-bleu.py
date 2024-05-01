from PIL import Image
from nltk.translate.bleu_score import sentence_bleu
import torch
from transformers import AutoProcessor, Blip2ForConditionalGeneration
import pandas as pd
import numpy as np



def find_score(reference,hypothesis):
    bleu_score = sentence_bleu([reference], hypothesis)
    return bleu_score

epoch='3'
processor = AutoProcessor.from_pretrained("/path/to/your/processor")
model = Blip2ForConditionalGeneration.from_pretrained(f"/path/to/your/model/epoch_{epoch}", device_map="auto", load_in_8bit=True, torch_dtype=torch.float16,)

device = "cuda" if torch.cuda.is_available() else "cpu"
model.to(device)
print('loading completed')
bleu_list=[]
results=[]
data=pd.read_csv('/path/to/your/csv/VizWiz__train_dataset.csv')
for i in range(0,len(data)):
    image_id=i
    image = Image.open('/path to you dataset/train/'+data.iloc[image_id].images).convert("RGB")
    inputs = processor(image, return_tensors="pt").to("cuda", torch.float16)
    generated_ids = model.generate(**inputs)
    gen_cap = processor.batch_decode(generated_ids, skip_special_tokens=True)[0].strip()
    results.append(gen_cap)
    if(i%100==0):
        print('Current position: ',i)
    sc_per=[]
    for j in range(0,5):
        real_cap = data.iloc[image_id][f"captions{j+1}"]
        score=find_score(real_cap,gen_cap)
        sc_per.append(score)
    bleu_list.append(max(sc_per))
    
avg_score=sum(bleu_list)/len(bleu_list)

print("Average Score of all images is: ", avg_score)
