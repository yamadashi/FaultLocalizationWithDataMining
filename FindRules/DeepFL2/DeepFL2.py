from keras.models import Model
from keras.layers import Input, Dense, Activation

input1 = Input(input_dim=10)
input2 = Input(input_dim=10)
input3 = Input(input_dim=10)

FC1 = Dense(10, activation='relu')(input1)
FC2 = Dense(10, activation='relu')(input2)
FC3 = Dense(10, activation='relu')(input3)

FC23 = Dense(10, activation='relu')(keras.layers.concatenate([FC2, FC3]))

output = Dense(10, activation='softmax')(keras.layers.concatenate([FC1, FC23]))

model = Model(inputs=[input1,input2,input3], outputs=output)

model.xompile(optimizer='rmsprop', loss='categorical_crossentropy')
model.fit({},,epochs,batch_size)
# model_input = Input(shape=(5,))
# mid = Dense(10)(model_input)
# output = Activation('softmax')(mid)
# model = Model(inputs=model_input, outputs=output)