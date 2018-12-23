# network-compression

The project is dedicated to neural networks compression. A convolutional network for classification of faces by gender is used as an example. You can get Celeba dataset for experiments [here](https://www.kaggle.com/jessicali9530/celeba-dataset).

## How to run the experiments
There are considered 3 cases:
* **initial** - a convolutional network (2 Conv + 4 Dense);
* **cut** - initial version without dense layers with both dimensionalities `> 1` (2 Conv + 1 Dense);
* **compressed** - initial version where dense layers with both dimensionalities `> 1` are compressed (2 Conv + 3 compressed Dense + 1 Dense).

### 1. Building a model
In oreder to build initial or cut version, you can use according functions for their construction from `classifier/tf/models.py`. To get compressed model, you should first train initial, save it and use `classifier/tf/Compress.ipynb` for compression.

### 2. Training
Use `classifier/tf/Train.ipynb`. Don't forget to specify necessary paths to downloaded Celeba dataset. Once you trained the model and saved it, you can use `classifier/tf/convert_to_tflite.sh` script in order to get the model in `.tflite` format.

### 3. Measure accuracy
On PC you can do it with `classifier/tf/Test.ipynb`. For devices see instructions below.

#### 3.1 Android
Build in Android Studio and install on your device the application in `android-app/` project. Put your trained model to `assets/`.

In order to measure accuracy on the phone, it has to be connected via `adb` (type `$ adb devices` to ensure that the connection is established).

Then we need to make a simple server for taking test images from PC. We can use Python for that:
```python
from http.server import SimpleHTTPRequestHandler, HTTPServer


def run(name, port):
    server_address = (name, port)
    httpd = HTTPServer(server_address, SimpleHTTPRequestHandler)
    httpd.serve_forever()


if __name__ == "__main__":
    run('', 8000)
```
Run the server in the same directory as `img_align_celeba/` (the directory with Celeba images). In order to allow the phone to access the server, type:
```shell
$ adb reverse tcp:8000 tcp:8000
```
Finally, press `START` button in the application. After the process is complete, you can find a `celeba_test.csv` file with resutls in the phone's `Document/` folder.
