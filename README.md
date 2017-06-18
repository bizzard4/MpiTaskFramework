# MpiTaskFramework
MPI task framework is an experimental concurrency model using message passing and task only. The goal is to think "concurrent-first" when designing your software. The basic concept of this method is to design using task and messages.
A task is an independant process that can only communicate throught messages. This force the user to think more about the data that would be share.

# MPI
Message-passing interface is a common way to communicate between threads, process and nodes.

# Task
Task is a basic executable process. In this case, each task will be a thread. A task communicate only using messages.

# Scalability
One goal of this framework is to have deisgn reflex that favor scalability. The framework should work transparently for intra-thread, IPC and network.

# Design method
The goal of this framework is to show how good concurrent practices can lead to a more scalable software. At design phase, each software fuctionality must be seen as a task and communication between done using messages.

# Samples
Implementation example : 

1) Request Ack Response Ack

2) Samplesort algorithm (TODO) 

3) Echo server with n clients
