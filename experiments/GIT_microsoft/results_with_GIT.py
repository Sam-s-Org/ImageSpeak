from transformers import AutoModelForCausalLM, AutoProcessor
from PIL import Image
import os
import torch
import pandas as pd

model_path='path to weights for model'
processor_path='path to processor'

model = AutoModelForCausalLM.from_pretrained(model_path)
processor = AutoProcessor.from_pretrained(processor_path)

device = "cuda:1" if torch.cuda.is_available() else "cpu"
model.to(device)
dir_path='path to dir of images'
dir=os.listdir(dir_path)

images=[]
captions=[]

for i in dir:
    image = Image.open(f'{dir_path}/{i}')

    pixel_values = processor(images=image, return_tensors="pt").pixel_values
    pixel_values = pixel_values.to(device)  # pixel values

    generated_ids = model.generate(pixel_values=pixel_values, max_length=20) # generated captions
    gen_cap=processor.batch_decode(generated_ids, skip_special_tokens=True)[0]

    images.append(i)
    captions.append(gen_cap)
    
df=pd.DataFrame({'images':images,'text':captions})
out_path='path to out_dir'
df.to_csv(f'out_path/git_results.csv')
''' code ends here :) '''