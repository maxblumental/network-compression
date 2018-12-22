from pathlib import Path

import numpy as np
import pandas as pd
from keras_preprocessing.image import img_to_array, load_img
from tensorflow.python.keras.utils.data_utils import Sequence


def idx_to_name(id_: int):
    return f"{id_:06d}.jpg"


def name_to_idx(name: str):
    return int(name[:-4])


def find_range_of_partition(partition_path: str, partition: int):
    partition_df = pd.read_csv(partition_path)
    sub_df = partition_df[partition_df.partition == partition]
    start = name_to_idx(sub_df.iloc[0].image_id)
    end = name_to_idx(sub_df.iloc[-1].image_id)
    return start, end


class CelebaSequence(Sequence):

    def __init__(self, partition: int, batch: int, img_path: str, attrs_path: str, partition_path: str):
        self.image_dir = Path(img_path)
        self.attrs_df = pd.read_csv(attrs_path)
        self.start_idx, self.end_idx = find_range_of_partition(partition_path, partition)
        self.batch = batch

    def __len__(self):
        return int(np.ceil((self.end_idx - self.start_idx + 1) / float(self.batch)))

    def __getitem__(self, idx):
        indices = np.arange(idx * self.batch + 1, min(1 + (idx + 1) * self.batch, self.end_idx))
        x = np.array([img_to_array(load_img(self.image_dir / idx_to_name(idx))) for idx in indices])
        y = np.array([self.attrs_df[self.attrs_df.image_id == idx_to_name(idx)].Male.item() for idx in indices])
        x /= 255
        y = (y + 1) / 2
        return x, y

    def __iter__(self):
        for item in (self[i] for i in range(len(self))):
            yield item


class FiniteCelebaSequence(CelebaSequence):

    def __iter__(self):  # run just one epoch
        for item in (self[i] for i in range(len(self))):
            yield item
