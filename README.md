# WebLoader
The WebLoader program loads a list url strings from a file, and when one of the Fetch buttons is clicked, it forks off one or more threads to download the HTML for each url. A Stop button can kill off the downloading threads if desired. A progress bar and some other status fields show the progress of the downloads -> the number of worker threads that have completed their run, the number of threads currently running, and (when done running) the elapsed time.

There are two main classes that make up the WebLoader program:

WebFrame - The WebFrame contains the GUI, keeps pointers to the main elements, and manages the overall program flow.

WebWorker - WebWorker is a subclass of Thread that downloads the content for one url. The "Fetch" buttons ultimately fork off a few WebWorkers.

A semaphore is used to limit the number of workers fetching at a time. 
