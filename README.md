rabbitmqApp
===========

Introduction
---
Messing around with RabbitMQ, TwitterAPI, and ElasticSearch in Java. 
- The receiver application reads messages off a RabbitMQ exchange-topic/queue. 
- If the message is "twitterid <twitter_id>" then the application proceeds to get the timeline (i.e. 20 most recent authored tweets) for the specified twitter_id
- Each JSON Tweet is serialized and indexed on the ElasticSearch instance


Two applications live in the `build` directory. The producer application in `sRankSEND.jar` can be used to send messages to the RabbitMQ system (hosted on CloudAMQP). The consumer application in `sRankRECV.jar` can be used to read messages and index the specified Twitter user ID's recent tweets to an ElasticSearch instance (hosted on FacetFlow). If you're feeling lucky, try running them.

Alternatively, build from source in Eclipse.

The following environment variables need to be exported before running the applications:

```
export CLOUDAMQP_URL="xxxxxxxxxxxxxxxxxx"
export FACETFLOW_API_KEY="xxxxxxxxxxxxxxxxxx"
export FACETFLOW_HOST="xxxxxxxxxxxxxxxxxx"
export TWITTER_CONSUMER_KEY="xxxxxxxxxxxxxxxxxx"
export TWITTER_SECRET_KEY="xxxxxxxxxxxxxxxxxx"
export TWITTER_ACCESS_TOKEN="xxxxxxxxxxxxxxxxxx"
export TWITTER_ACCESS_TOKEN_SECRET="xxxxxxxxxxxxxxxxxx"
```
To get the actual parameters, you'll need to sign up for the respective services:
* [CloudAMQP](https://www.cloudamqp.com/)
* [FacetFlow](https://facetflow.com/)
* [TwitterApps](https://apps.twitter.com/)


How To Run
---

**Producer: _sRankSEND.jar_**

Usage:
```
java -jar sRankSEND.jar [mode] [routeKey|queueName] [message]
```
- `mode`: `topic` or `queue` 
```
In `topic` mode, the message is sent with the given topic route
In `queue` mode, the message is sent to the specified queue name
```
- `routeKey|queueName`: a (may be arbitrary) topic route or queue name to send to
- `message`: whatever you want to send. (The receiver application only pays attention to "twitterid <id>" messages.)

Example:
````
java -jar sRankSEND.jar topic srank.tweets twitterid 191189983
```
Send the message "twitterid 191189983" via topic mode with route "srank.tweets"


**Receiver: _sRankRECV.jar_**

Usage:
```
java -jar sRankRECV.jar [mode] [bindingKey|queueName]
```
- `mode`: `topic` or `queue`
```
In `topic` mode, messages with topics matching the binding key are processed. Spawning multiple of the same receiver applications will result in each instance running the same task.
In `queue` mode, messages are processed from the given queue name. Spawning multiple of the same receiver applications will result in the default round-robin processing fashion. i.e. receiver 1 processes a message, then receiver 2 processes a message, etc.
```
- `bindingKey|queueName`: a (may be arbitrary) topic binding or queue name to send to

Example:
```
java -jar sRankRECV.jar topic srank.#
```
Receives messages matching srank._anythingAndMore_ and if the message is in the expected form then gets the id's latest tweets and indexes them in the ElasticSearch instance

Other Documentation
---
* [RabbitMQ Tutorial](http://www.rabbitmq.com/getstarted.html)
* [Twitter4J](http://twitter4j.org/en/)
