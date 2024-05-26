import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D, MaxPooling2D, Flatten, Dense, TimeDistributed, LSTM
from tensorflow.keras.optimizers import Adam
import numpy as np
import cv2
import json

# Function to load images and labels from JSON
def load_images_from_json(file_path):
    with open(file_path, 'r') as f:
        data = json.load(f)
    
    images = []
    labels = []
    for item in data:
        image_path = item['file']
        label = int(item['label'].split('-')[1])  # Extract the digit from label
        image = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
        image = cv2.resize(image, (28, 28))  # Resize the image to (28, 28)
        image = np.expand_dims(image, axis=-1)  # Add channel dimension
        images.append(image)
        labels.append(label)
    
    images = np.array(images, dtype=np.float32) / 255.0  # Normalize pixel values
    labels = tf.keras.utils.to_categorical(labels, 10)  # Assuming you have 10 classes
    return images, labels

# Load training data from JSON
train_images, train_labels = load_images_from_json('./dataset-bw/labelsTrain.json')

# Load validation data from JSON
validation_images, validation_labels = load_images_from_json('./dataset-bw/labelsValidation.json')

# Build the CNN-LSTM model
model = Sequential()

# TimeDistributed layer to apply a Conv2D layer to each frame of the sequence
model.add(TimeDistributed(Conv2D(32, (3, 3), activation='relu'), input_shape=(None, 28, 28, 1)))
model.add(TimeDistributed(MaxPooling2D(pool_size=(2, 2))))

model.add(TimeDistributed(Conv2D(64, (3, 3), activation='relu')))
model.add(TimeDistributed(MaxPooling2D(pool_size=(2, 2))))

# Flatten the output from TimeDistributed layers
model.add(TimeDistributed(Flatten()))

# LSTM layer
model.add(LSTM(128, activation='relu', return_sequences=False))  # Set return_sequences=False for the last LSTM layer

# Fully Connected Layer
model.add(Dense(128, activation='relu'))

# Output Layer
model.add(Dense(10, activation='softmax'))

# Compile the model
model.compile(optimizer=Adam(), loss='categorical_crossentropy', metrics=['accuracy'])

# Train the model
history = model.fit(
    train_images, train_labels,
    epochs=10,  # Adjust the number of epochs as needed
    batch_size=32,
    validation_data=(validation_images, validation_labels)
)

# Evaluate the model
loss, accuracy = model.evaluate(validation_images, validation_labels)
print(f'Validation loss: {loss}')
print(f'Validation accuracy: {accuracy}')

# Save the model in .keras format
model.save('digit_cnn_lstm_model.keras')
