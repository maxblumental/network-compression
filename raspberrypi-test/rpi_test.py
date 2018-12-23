import tensorflow as tf
from tensorflow.python.saved_model import tag_constants
import numpy as np
import sys
sys.path.insert(0,'..')
from celeba_reader import CelebaSequence, idx_to_name

from time import time
import pandas as pd

file_name = "raspberrypi_test.csv"

paths = {
    'img_path': 'path/to/img_align_celeba_',
    'attrs_path': 'list_attr_celeba.csv',
    'partition_path': 'list_eval_partition.csv'
}
batch = 1
test_data = CelebaSequence(partition=2, batch=batch, **paths)

names = np.array([])
times = np.array([], dtype=int)
predictions = np.array([])

graph = tf.Graph()
with graph.as_default():
    with tf.Session() as sess:
        tf.saved_model.loader.load(sess, [tag_constants.SERVING], './saved_model')
        tf.summary.FileWriter( './logs/1/train', graph) # $ tensorboard --logdir ./logs/1
        images_batch = graph.get_tensor_by_name('Placeholder:0')
        labels = graph.get_tensor_by_name('Placeholder_1:0')
        classifier = graph.get_tensor_by_name('Reshape_1:0')
        
        i = 0
        for x, y in test_data:
            t = time()
            predicted = sess.run(classifier, feed_dict={images_batch: x, labels: y})
            t = time() - t

            names = np.append(names, idx_to_name(test_data.start_idx + i))
            times = np.append(times, int(t * 1000))
            predictions = np.append(predictions, predicted)
            
            print(i, end='\r')
            i = i + 1
        
        print('Completed')
        print('Saved file: ' + file_name)

        header='image_id,time,male'
        results = np.array([names, times, predictions]).T
        np.savetxt(file_name, results, header=header, fmt='%s', delimiter=",", comments='')
