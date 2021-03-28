
Camera2BasicFragment.java

Methods:

1. loadModel(model_name,device):

Initializes the model with model_name on the device specified (CPU, GPU or DSP)

2. start2():
	
	One background handler thread for each model is started and the online scheduler algorithm is run.

	int timings[]: timings of the arrival of different models.
	

	string arrivals[]: names of the models arriving in the above timings


	batch_execs_$device_name_$model_name: Execution times of the models on different devices with different batch sizes starting from 0

	$modelname_loadtimes[]: indicates if a model is loaded or not on CPU, GPU or DSP

	$modelname_queue: a queue for different models initialized with size = wait time before they are to be batched and executed

	Logic inside for loop to implement online scheduler:

		For a time t

		A. Remove an element from the top of each model's queue.
		B. For each element removed in step A, if it is not an empty element:
			B1: Remove all other elements in the queue which are not empty and fill their places with empty elements.
			Batch these elements with the model extracted in B
		C. Execute each batched model in B.
		D. If a model instance arrives at time t, add it to its respective queue.
		Or else add empty element to each queue because we removed an element from each queue in B.

ImageClassifier.java:
	
	Abstract class provided by TFLite documentation which contains some methods that are implemented by respective models' classes.

