import pandas as pd
import numpy as np
from sklearn.metrics import roc_auc_score

df = pd.read_csv("raspberrypi.csv")
predictions = np.array(df['male'])
predictions[predictions >= 0.5] = 1
predictions[predictions < 0.5 ] = 0
            
celeba_attr = pd.read_csv('list_attr_celeba.csv')
celeba_attr = celeba_attr[['Male']]
celeba_attr['Male'] += 1
celeba_attr['Male'] /= 2
labels = np.array(celeba_attr['Male'])
idx_from = predictions.shape[0]
labels = labels[-idx_from-1:]

print('roc_auc_score:', roc_auc_score(labels, predictions))
print('time_mean:', df['time'].mean())
print('time_std:', df['time'].std())