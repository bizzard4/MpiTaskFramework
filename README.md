# MpiTaskFramework
MPI task framework is an experimental concurrency model using message passing and task only. The goal is to think "concurrent-first" when designing your software. The basic concept of this method is to design using task and messages.
A task is non-blocking and can spawn other tasks. Data is shared among tasks using message. This force the user to think more about the data that would be share. This is experimental because it doesn't suit Java programming language. Task
system like this would need his own programming language that include tasking and messaging like classes.

More documentation to come.

# MPI
Message-passing interface is a common way to communicate between threads, process and nodes.

# Task
Task is a basic executable process. In this case, each task will be a thread. Custom sheduling can be implemented. A task can receive and send messages only. A new task is also registered to a repository.

# Design method
The goal of this framework is to show how good concurrent practices can lead to a more scalable software. At design phase, each software fuctionality must be seen as a task and communication between done using messages.

# Samples
Implementation example : 

1) Producer consumer (TODO) 

2) Samplesort algorithm (TODO) 

3) Request Ack Response Ack 
