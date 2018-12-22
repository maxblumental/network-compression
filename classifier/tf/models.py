import tensorflow as tf


def initial_model(images_batch):
    """
    Construct a convolutional network for
    classification of face images by gender.
    :param images_batch: tf.placeholder(tf.float32, [None, 218, 178, 3])
    :return: a model calculating predictions of shape [None] (batch size)
    """
    convolve = convolutions(images_batch)
    pool2_flat = tf.reshape(convolve, [-1, 2288])

    dense1 = tf.layers.dense(inputs=pool2_flat, units=256, activation=tf.nn.relu)
    dense2 = tf.layers.dense(inputs=dense1, units=256, activation=tf.nn.relu)
    dense3 = tf.layers.dense(inputs=dense2, units=32, activation=tf.nn.relu)
    predictions = tf.layers.dense(inputs=dense3, units=1, activation=tf.nn.sigmoid)

    predictions = tf.reshape(predictions, [-1])
    return predictions


def cut_model(images_batch):
    """
    Initial model without all dense layers except the last one. (See initial_model().)
    """
    convolve = convolutions(images_batch)
    pool2_flat = tf.reshape(convolve, [-1, 2288])

    predictions = tf.layers.dense(inputs=pool2_flat, units=1, activation=tf.nn.sigmoid)

    predictions = tf.reshape(predictions, [-1])
    return predictions


def convolutions(images_batch):
    conv1 = tf.layers.conv2d(
        inputs=images_batch,
        filters=8,
        kernel_size=5,
        padding="same",
        activation=tf.nn.relu)
    pool1 = tf.layers.max_pooling2d(inputs=conv1, pool_size=4, strides=4)
    conv2 = tf.layers.conv2d(
        inputs=pool1,
        filters=16,
        kernel_size=5,
        padding="same",
        activation=tf.nn.relu)
    pool2 = tf.layers.max_pooling2d(inputs=conv2, pool_size=4, strides=4)
    return pool2
