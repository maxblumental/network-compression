import tensorflow as tf


def face_classifier_reduced(sess, images_batch, layers, r):
    kernels, biases = weights_preprocessing(layers, r)
    conv1 = tf.layers.conv2d(
        name='conv1_reduced',
        inputs=images_batch,
        filters=8,
        kernel_size=5,
        padding="same",
        activation=tf.nn.relu)
    pool1 = tf.layers.max_pooling2d(inputs=conv1, pool_size=4, strides=4)

    conv2 = tf.layers.conv2d(
        name='conv2_reduced',
        inputs=pool1,
        filters=16,
        kernel_size=5,
        padding="same",
        activation=tf.nn.relu)
    pool2 = tf.layers.max_pooling2d(inputs=conv2, pool_size=4, strides=4)

    pool2_flat = tf.reshape(pool2, [-1, 2288])

    dense11 = tf.layers.dense(name='dense11_reduced', inputs=pool2_flat, units=r, activation=None, use_bias=False)
    dense12 = tf.layers.dense(name='dense12_reduced', inputs=dense11, units=r, activation=None, use_bias=False)
    dense13 = tf.layers.dense(name='dense13_reduced', inputs=dense12, units=256, activation=tf.nn.relu)

    dense21 = tf.layers.dense(name='dense21_reduced', inputs=dense13, units=r, activation=None, use_bias=False)
    dense22 = tf.layers.dense(name='dense22_reduced', inputs=dense21, units=r, activation=None, use_bias=False)
    dense23 = tf.layers.dense(name='dense23_reduced', inputs=dense22, units=256, activation=tf.nn.relu)

    dense31 = tf.layers.dense(name='dense31_reduced', inputs=dense23, units=r, activation=None, use_bias=False)
    dense32 = tf.layers.dense(name='dense32_reduced', inputs=dense31, units=r, activation=None, use_bias=False)
    dense33 = tf.layers.dense(name='dense33_reduced', inputs=dense32, units=32, activation=tf.nn.relu)

    predictions = tf.layers.dense(inputs=dense33, units=1, activation=tf.nn.sigmoid)

    predictions = tf.reshape(predictions, [-1])

    pos_kernel = 0
    pos_bias = 0
    for v in tf.get_collection(key=tf.GraphKeys.TRAINABLE_VARIABLES):
        if 'kernel' in v.name:
            print(v.name, kernels[pos_kernel].shape)
            v = tf.assign(v, kernels[pos_kernel])
            sess.run(v)
            pos_kernel = pos_kernel + 1
        else:
            print(v.name, biases[pos_bias].shape)
            v = tf.assign(v, biases[pos_bias])
            sess.run(v)
            pos_bias = pos_bias + 1
    return predictions


def weights_preprocessing(layers, r):
    kernels = []
    biases = []
    for name, val in layers.items():
        if 'conv' in name:
            if 'kernel' in name:
                kernels.append(val)
            else:
                biases.append(val)
        if 'dense' in name:
            if 'kernel' in name:
                if val.shape[1] > 1:
                    u, s, v = np.linalg.svd(val)
                    u = u[:, :r]
                    s = np.diag(s[:r])
                    v = v[:r, :]
                    kernels.append(u)
                    kernels.append(s)
                    kernels.append(v)
                else:
                    kernels.append(val)
            else:
                biases.append(val)
    return kernels, biases
