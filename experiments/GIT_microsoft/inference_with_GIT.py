import torch
from transformers import AutoModelForCausalLM
from transformers import AutoProcessor
from PIL import Image


image_path='paht to jpg image here'
model_path='path to weights for model'
processor_path='path to weights for processor path'

image = Image.open(image_path).convert("RGB")

# loading processor fro images 
processor = AutoProcessor.from_pretrained(processor_path)
pixel_values = processor(images=image, return_tensors="pt").pixel_values # image ready to be fed in model

# loading model from weights saved in local
model = AutoModelForCausalLM.from_pretrained(model_path)

# run on the GPU if we have one
device = "cuda" if torch.cuda.is_available() else "cpu"
model.to(device)
pixel_values = pixel_values.to(device)

# generating captions for images
generated_ids = model.generate(pixel_values=pixel_values, max_length=20)
print("Generated caption:", processor.batch_decode(generated_ids, skip_special_tokens=True))

''' code ens here :)  '''