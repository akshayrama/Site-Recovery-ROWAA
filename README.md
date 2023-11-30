# CS542-Site-Recovery-ROWAA

## Overview of the project:

This project is part of the CS 542 - Distributed Database Systems course taken by Prof. Bharat Bhargava in the Spring 2023 semester
at Purdue University. 

As part of this project, I have simulated the failure and recovery of sites 
in a partially replicated distributed database system. This project is implemented using Java RMI (Remote Method Invocation). The algorithm for transaction processing follows the ROWAA (Read Once Write All Available) 
Algorithm. The site recovery logic and procedure is implemented based
on the paper [Site Recovery in Replicated Distributed Database
Systems](https://www.cs.purdue.edu/homes/bb/cs542-23Fall/readings/reliability/Site%20Recovery%20in%20Replicated%20Distributed%20Database%20Systems.pdf).

For this project, I have set up three sites, each capable of accepting and executing
transactions. Each site contains five logical objects which would be updated
based on the supplied transactions. The three sites are (Site1, Site2 and Site3) and transactions
can be supplied to either site using the AcceptTransactions class.


## Notes on instructions to run:

After cloning this repository, kindly head to the root directory of 
the project and run:

``
./initialSetup.sh
``

If permissions disallow you to run this script, please use `chmod +x` to make it executable.

This script would compile all the Java classes and set up initial values for session numbers and backlog nominal session vectors for all the sites.
                                                                                                                                                   

Next, follow the steps given to execute in each terminal.

**NOTE:** Kindly please alter the `java.rmi.server.codebase` value in your runs to match the path to your cloned directory of this repository.


#### Terminal 1:

```
cd Site_1/
rmiregistry
```

This starts the RMI registry.

#### Terminal 2:

```
cd Site_1/
java -Djava.rmi.server.codebase=file:/Users/akshay/IdeaProjects/SiteRecoveryROWAA/Site_1 Site1
```

#### Terminal 3:

```
cd Site_2/
java -Djava.rmi.server.codebase=file:/Users/akshay/IdeaProjects/SiteRecoveryROWAA/Site_2 Site2
```
        
#### Terminal 4:

```
cd Site_3/
java -Djava.rmi.server.codebase=file:/Users/akshay/IdeaProjects/SiteRecoveryROWAA/Site_3 Site3
```

#### Terminal 5:

```
cd AcceptTransactions/
java -Djava.rmi.server.codebase=file:/Users/akshay/IdeaProjects/SiteRecoveryROWAA/AcceptTransactions AcceptTransactions
```

In the `AcceptTransactions` terminal, you will be able to follow the instructions and supply transactions to each site. I've enlisted
important test cases below that cover the edge cases of this system.

## Test Cases:

Test cases are given as per these rules:
```
1. To run a transaction on a site: Please use the format [<'TXN'>, <SITE NUMBER>,<OPERATION>, <OBJECT NUMBER>, <UPDATE VALUE>]
2. To enforce failure on a site: Please use the format [<'FAIL'>, <SITE NUMBER>]
3. To recover a site: Please use the format [<'RECOVER'>, <SITE NUMBER>]
4. To print the global view of all sites: PLease use the format ['PRINT']
```  
1. Normal transaction updates and print of three sites:
    ```  
    TXN,1,+,4,10
    PRINT
    TXN,2,-,3,15
    PRINT
    TXN,3,+,5,1
   ```        
   This shows that there are updates being performed on all three sites. Whichever site obtains the transaction
    remotely invokes other sites to write the updated value.


2. Failure of one site and execute transactions on the other:

    ```
   TXN,1,+,4,10
   FAIL,1
   TXN,2,-,3,15
   TXN,3,-,1,75
   PRINT
   ```  
    This shows that there are no updates being performed on the failed site.
             

3. Failure of one site and recovery of the same site

    ```           
   TXN,1,+,4,10
   FAIL,1
   TXN,2,-,3,15
   TXN,3,-,1,75
   RECOVER,1
   PRINT
   ```  
   This shows that the recovered site would execute copier transactions
    in order to get back the latest values for the logical objects.
                          

4.  Failure of one site and transactions being supplied to the failed site

    ```
    TXN,1,+,4,10
    FAIL,1
    TXN,1,-,3,15
    PRINT
    ```  
    
    This shows that the failed site cannot execute any transactions.


5.  Failure of `n-1` sites and recovering as long as there is one site up.

    ```
    TXN,1,-,3,15
    FAIL,1
    TXN,2,+,4,30
    FAIL,2
    TXN,3,+,1,1
    PRINT
    RECOVER,1
    PRINT
    RECOVER,2
    PRINT
    ```
         
    This shows that the system is recoverable as long as there is one site that is operational.
   

6. Failure of all `n` sites, showing that the system cannot be recovered.
       
    ```          
   TXN,1,-,3,15
   FAIL,1 
   TXN,2,+,4,30
   FAIL,2 
   TXN,3,+,1,1
   PRINT
   FAIL,3
   RECOVER,1
   PRINT 
    ```          
                