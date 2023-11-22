# CS542-Site-Recovery-ROWAA

### Notes on instructions to run:

#### Terminal 1:

```
cd Site_1/
rmiregistry
```

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
cd AcceptTransactions/
java -Djava.rmi.server.codebase=file:/Users/akshay/IdeaProjects/SiteRecoveryROWAA/AcceptTransactions AcceptTransactions
```
