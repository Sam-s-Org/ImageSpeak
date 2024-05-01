from transformers import AutoProcessor, Blip2ForConditionalGeneration
from PIL import Image
import os
import torch
import pandas as pd
epoch=3
processor = AutoProcessor.from_pretrained("/path/to/your/processor")
model = Blip2ForConditionalGeneration.from_pretrained(f"/path/to/your/model/epoch_{epoch}", device_map="auto", load_in_8bit=True, torch_dtype=torch.float16,)

print('model_loaded')
device = "cuda:1" if torch.cuda.is_available() else "cpu"
model.to(device)

dir=os.listdir('/path/to/your/images')
images=[]
captions=[]
for i in dir:
    image = Image.open(f'/path/to/your/images/{i}')
    inputs = processor(image, return_tensors="pt").to("cuda", torch.float16)

    generated_ids = model.generate(**inputs)
    gen_cap = processor.batch_decode(generated_ids, skip_special_tokens=True)[0].strip()
    images.append(i)
    captions.append(gen_cap)
    
df=pd.DataFrame({'images':images,'text':captions})
df.to_csv('/path/to/your/directory/blip2_results.csv')