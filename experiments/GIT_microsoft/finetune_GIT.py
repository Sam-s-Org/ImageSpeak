from torch.utils.data import DataLoader
from transformers import AutoProcessor
import torch
from torch.utils.data import Dataset
import pandas as pd
from PIL import Image
from transformers import AutoModelForCausalLM

processor_path='pretrained-git-textcaps/proce-base-textcaps' # path to saved weights of processor for GIT
processor = AutoProcessor.from_pretrained(processor_path) 


'''creating a dataloader for finetuning GIT on VIZWIZ dataset provided'''
class ImageCaptioningDataset(Dataset):
    def __init__(self, dataset, processor):
        self.dataset = pd.read_csv(dataset)
        self.processor = processor

    def __len__(self):
        return len(self.dataset)

    def __getitem__(self, idx):
        item = self.dataset.iloc[idx]
        img = Image.open('/scratch/jankita.scee.iitmandi/Image2Text/ImageSpeak/images/'+item['image']).convert("RGB")
        encoding = self.processor(images=img, text=item["text"], padding="max_length", return_tensors="pt")

        # remove batch dimension
        encoding = {k:v.squeeze() for k,v in encoding.items()}

        return encoding

dataset=ImageCaptioningDataset('VizWizfinal.csv',processor)
train_dataloader = DataLoader(dataset, shuffle=True, batch_size=10)


model_path="pretrained-git-textcaps/git-base-textcaps_8" # path of weights for model of GIT
model = AutoModelForCausalLM.from_pretrained(model_path)

optimizer = torch.optim.AdamW(model.parameters(), lr=1e-5)  # setting up optimizer for fientuning

device = "cuda" if torch.cuda.is_available() else "cpu"
model.to(device)

model.train()  # setting model to training mode

save_path='pretrained-git-textcaps/'
for epoch in range(0,10): # set number of epochs
  print("Epoch:", epoch)
  for idx, batch in enumerate(train_dataloader):
    input_ids = batch.pop("input_ids").to(device)
    pixel_values = batch.pop("pixel_values").to(device)

    outputs = model(input_ids=input_ids,
                    pixel_values=pixel_values,
                    labels=input_ids)
    
    loss = outputs.loss

    print(idx,"    Loss:", loss.item())

    loss.backward()

    optimizer.step()
    optimizer.zero_grad()
  model.save_pretrained(f'{save_path}git-base-textcaps_{epoch}') # saving weight after every epoch

  '''  code ends here  :)  '''