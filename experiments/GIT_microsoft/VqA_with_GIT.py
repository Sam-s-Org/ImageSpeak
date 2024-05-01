from transformers import AutoProcessor, AutoModelForCausalLM
import torch
from huggingface_hub import hf_hub_download
from PIL import Image

def prepare_image():
  filepath = 'file paht for image' # in jpg here
  image = Image.open(filepath).convert("RGB")
  
  return image

# image = prepare_image()


device = "cuda" if torch.cuda.is_available() else "cpu"

processor_path="weights_vQa/processor"
processor = AutoProcessor.from_pretrained(processor_path)
model_path="weights_vQa/model"
model = AutoModelForCausalLM.from_pretrained(model_path)

pixel_values = processor(images=prepare_image(), return_tensors="pt").pixel_values

question = "Ask you question here"

''' processing you question  '''
input_ids = processor(text=question, add_special_tokens=False).input_ids
input_ids = [processor.tokenizer.cls_token_id] + input_ids
input_ids = torch.tensor(input_ids).unsqueeze(0)

''' generating response for you question '''
generated_ids = model.generate(pixel_values=pixel_values, input_ids=input_ids, max_length=50)

print("Generated answer:", processor.batch_decode(generated_ids[:, input_ids.shape[1]:], skip_special_tokens=True))

''' code ends here :)  '''