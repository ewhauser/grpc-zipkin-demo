This project demos integration between gRPC and Zipkin in Java.

To run the demo, start the [Zipkin docker container](https://github.com/openzipkin/docker-zipkin):

```
docker run -d -p 9411:9411 openzipkin/zipkin
```

Then run `./run-demo.sh` (the demo doesn't terminate cleanly so Ctrl-C after the RPC calls are run).

Then view the results in the browser:

```
open http://$(docker-machine ip default):9411/
```

Note: the instructions below assume your default Docker environment is named `default`.
