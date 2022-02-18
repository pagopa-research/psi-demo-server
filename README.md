# PSI Server Demo

Java Spring application that relies on the [PSI-SDK library](https://github.com/alessandropellegrini/psi-sdk)
to implement the server in a Private Set Intersection (PSI) computation.

The goal of this repo, in conjunction with the [Psi Client Demo repository](https://github.com/alessandropellegrini/psi-demo-client),
is to provide a demo of how a client and a server could use the PSI-SDK library
to implement an end-to-end PSI calculation. Given its instructional scope,
this code is not designed to be used in production environments, if not as a
starting point for more complete implementations.

This demo supports all the PSI algorithms (and key sizes) supported by the PSI-SDK library, namely,
Blind-Signature PSI, Diffie-Hellman PSI, Blind-Signature PSI based on elliptic curves and Diffie-Hellman PSI based
on elliptic curves. The comparison is outside the scope of this demo.

## Building and running the server
The build process is based on Maven. As anticipated, this program depends on the
PSI-SDK library. If it is not available in a remote Maven server, the jar of the library can
be generated locally by running the following command from the PSI-SDK root folder:

    mvn clean install

Once the PSI-SDK jar is available in either a remote ore local Maven repository, you can run the 
server by running the following command from the root folder of this repo:

    mvn spring-boot:run

## Key generation and storage
In this demo implementation, the server uses a local file to store the keys used in previous 
PSI sessions. Whenever the creation of a new PSI session is requested by a client, the server
reads the local file key.store to check whether a key with the requested parameters 
(algorithm and key size) has already been generated. If a key with matching parameters is found,
the server initializes the session using the found key. Conversely, if no key with 
matching parameters is found, a new key is generated in the key.store file. 

We note that in the context of this repository, we refer to as keys the different instances of
the <code>PsiServerKeyDescription</code> class, which contains as fields all the parameters of the 
encryption functions used by the server (private key) as well as the public parameters 
(public key) that should be sent to the clients to initialize the client-side encryption 
modules.

## Redis Cache Provider
Caching the result of previous encryption operations can result in a significant
performance speed-up for PSI calculations that use the same keys.
The PSI-SDK library offers a simple key-store interface that allows the
users of the library to select their preferred caching system. In this demo, we provide
an in-memory cache provider based on [Redis](https://github.com/redis/redis).

You can easily create  a local Redis server with Docker for testing purposes with the following command:

    docker run --name redis -p 6379:6379 -d redis

When the server is started, it checks a whether it can connect to a Redis server (by default at localhost:6379).
If the connection is successful, the server will enable caching for all the subsequent
PSI sessions. Conversely, if the connection fails, it disables caching for subsequent 
PSI sessions. We note that enabling the caching at the server side does not imply that 
also the client should enable caching, and vice versa.

## Bloom Filter implementation
A Bloom filter is a probabilistic data structure that is used to test whether an element is a member of a set.
Remarkable properties of this data structure are its high space efficiency, low computing cost and that, despite showing
false positives, it does not provide false negatives.

Despite being outside the scope of the PSI-SDK, this demo also includes a
Bloom Filter implementation (also implemented in the server repo) based on the
[Google Guava library](https://github.com/google/guava) to show how this data
structure could be used to reduce the execution time of the PSI computation.

The Bloom Filter of the entire server dataset is generated periodically by a cron job and 
stored on the database.
Whenever a client requests the creation of a new session, in addition to other session meta-data,
the server sends a serialized representation of the latest Bloom Filter with its associated
creation date. The asynchronous approach proposed in this demo implementation well fits a 
scenario where the server dataset is relatively large, but it is modified
infrequently. By also sending the creation date, the client can choose whether the age of the 
Bloom Filter can be considered reasonable for its specific-use case. Using a stale Bloom Filter 
of the server dataset (i.e., a Bloom Filter that does not reflect the updated state of the dataset) 
for filtering the client dataset could lead to excluding from the result of the PSI some items
which were added to the server dataset after the Bloom Filter creation.

## List of APIs
This server exposes the following API to its clients:
- **GET psi/parameters**: returns a list of pairs of supported PSI algorithms and key sizes.
- **POST /psi**: creates a new PSI session for the algorithm and key size passed in the body. Returns the session 
identifier and all the fields required to initialize the PSI-SDK client components as well as the serialized Bloom Filter
with its creation date.
- **POST /psi/{sessionId}/clientSet**: returns a server-side encryption of the values of the map passed in the body.
- **GET /psi/{sessionId}/serverSet**: returns a server-side encrypted page of the server dataset.
- **GET /psi/{sessionId}**: returns the description of the session identified by the sessionId. It is the same object 
returned by the PSI session creation.
- **POST /psi/dataset**: populates the server dataset by adding new entries based on the entries of the map passed 
in the body. For each entry of the map, a number of entries equal to the number expressed by the value is created, with 
the radix of the entry being the key and the last portion of the string being an increasing counter in the format 
KEY-COUNTER. This endpoint is only offered for testing purposes and should be excluded by any production environment.

We note that, in the context of this demo, all clients have unrestricted access to all APIs.
In actual implementations, some form of authentication should be introduced to only allow
authorized users to create new sessions.
Moreover, the APIs that refer to a specific session should only be accessible by the client that requested
the creation of the session (e.g., by making the server generate and send to the client a session-specific
key whenever a new session is created).

## Interaction with the PSI-SDK library
In the following we offer a high-level description of the main functional steps that characterize the APIs 
that implement the core PSI functionalities, giving a particular focus on the interactions with the PSI-SDK library.   

### POST /psi
1. The server creates a <code>PsiServer</code> object by calling the <code>initSession</code> method 
of the <code>PsiServerFactory</code> class passing the <code>PsiAlgorithmParameter</code> sent by the client.
It returns a PsiServerSession object.
2. The returned <code>PsiServerSession</code> object is stored on the database, 
which generates a session identifier.
3. A <code>PsiClientSession</code> object is created from the <code>PsiServerSession</code> by calling the method
<code>getFromServerSession</code> of the <code>PsiClientSession</code> class.
4. The server returns to the client a <code>PsiClientSessionDTO</code> object,
which is created from the <code>PsiClientSession</code>, a serialized representation of the latest Bloom Filter,
the session identifier and the session expiration time.

### POST /psi/{sessionId}/clientSet
1. The server queries the database to retrieve the PsiServerSession object identified by *sessionId*.
2. A <code>PsiServer</code> object is created by calling the <code>loadSession</code> method
of the <code>PsiServerFactory</code> class, passing as input the <code>PsiServerSession</code> retrieved from the database.
3. The server calls the method <code>encryptDatasetMap</code> on the <code>PsiServer</code> object, passing in input
the <code>PsiDatasetMapDTO</code> object sent by the client. The returned <code>PsiDatasetMapDTO</code> is returned to the client.

### GET /psi/{sessionId}/serverSet
1. The server queries the database to retrieve the PsiServerSession object identified by *sessionId*.
2. A <code>PsiServer</code> object is created by calling the <code>loadSession</code> method
   of the <code>PsiServerFactory</code> class, passing as input the <code>PsiServerSession</code> retrieved from the database.
3. The server queries the dataset to retrieve the requested page of the server dataset.
4. The server calls the method <code>encryptDataset</code> on the <code>PsiServer</code> object, passing as input
the page read from the database, which returns a set of encrypted entries. 
5. The server returns to the client a <code>PsiServerDatasetPageDTO</code> object, which contains the 
set of encrypted entries and paging metadata.

## General implementation notes
The library methods that return instances of the PsiServer class (<code>initSession</code> and 
<code>loadSession</code>) offer an overloaded signature that allows the users to configure how the actual 
PSI computations will be performed. In particular, the <code>initSession</code> method
allows the user to pass a key as a <code>PsiServerKeyDescription</code> object when creating a new
session instead than delegating the library to generate a new one. Similarly, if an object that
implements the <code>PsiCacheProvider</code> interface is passed to either the <code>initSession</code> or
<code>loadSession</code> methods, caching is enabled. As anticipated, in this demo implementation, the decision
on whether these objects should be passed as parameter when calling the library to create PsiServer instances 
is made dynamically based on whether a key for the requested parameter has already been generated,
or whether a connection to a Redis server is available.

